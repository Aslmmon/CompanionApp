#!/usr/bin/env python3
"""
Interactive Telegram Release Manager for Google Play CI/CD.
1. Inspects git diff/commits.
2. Generates candidate release notes (EN & AR) using Gemini Flash API (with deterministic fallback).
3. Sends an interactive Telegram message with inline buttons [Approve] and [Cancel].
4. Allows replying directly with custom notes to edit them.
5. Writes distribution/whatsnew/whatsnew-en-US and whatsnew-ar on approval.
"""

import os
import sys
import re
import json
import time
import subprocess
import urllib.request
import urllib.parse

TELEGRAM_API_BASE = "https://api.telegram.org/bot"
GEMINI_API_BASE = "https://generativelanguage.googleapis.com/v1beta/models"

def run_cmd(cmd):
    try:
        return subprocess.check_output(cmd, shell=True, text=True).strip()
    except Exception as e:
        print(f"[WARN] Command failed ({cmd}): {e}", file=sys.stderr)
        return ""

def get_app_version():
    if os.path.exists("version.properties"):
        with open("version.properties", "r", encoding="utf-8") as f:
            content = f.read()
        major = re.search(r"MAJOR=(\d+)", content)
        minor = re.search(r"MINOR=(\d+)", content)
        patch = re.search(r"PATCH=(\d+)", content)
        code = re.search(r"VERSION_CODE=(\d+)", content)
        if major and minor and patch and code:
            return f"{major.group(1)}.{minor.group(1)}.{patch.group(1)}", code.group(1)
    return "1.0.0", "1"

def get_recent_commits():
    # Try getting commits since the previous release tag, or last 10 commits
    tags = run_cmd("git tag --sort=-creatordate").splitlines()
    if len(tags) >= 2:
        prev_tag = tags[1]
        log = run_cmd(f"git log {prev_tag}..HEAD --oneline --no-merges")
    else:
        log = run_cmd("git log -n 10 --oneline --no-merges")
    
    if not log:
        log = run_cmd("git log -n 5 --oneline")
    return log

def generate_ai_notes(commits_text, api_key, model_name):
    prompt = f"""You are a professional Android release manager for 'Sahaba Companions' (رفقاء الصحابة), an Islamic lifestyle & spiritual app.
Summarize the following git commits into concise, inspiring, and professional release notes for Google Play Store 'What's new'.

Google Play strictly limits release notes to 500 characters maximum per language.
Keep each version under 450 characters.

Return ONLY a JSON object in this exact structure without markdown ticks:
{{
  "en": "• Bullet 1\\n• Bullet 2",
  "ar": "• النقطة الأولى\\n• النقطة الثانية"
}}

Git commits:
{commits_text}
"""
    url = f"{GEMINI_API_BASE}/{model_name}:generateContent?key={api_key}"
    payload = {
        "contents": [{
            "parts": [{"text": prompt}]
        }],
        "generationConfig": {
            "temperature": 0.2,
            "responseMimeType": "application/json"
        }
    }
    
    req = urllib.request.Request(
        url,
        data=json.dumps(payload).encode("utf-8"),
        headers={"Content-Type": "application/json"}
    )
    
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            data = json.loads(resp.read().decode("utf-8"))
            candidate = data["candidates"][0]["content"]["parts"][0]["text"]
            # Clean up potential markdown code fences
            cleaned = re.sub(r"^```json\s*|\s*```$", "", candidate.strip(), flags=re.MULTILINE)
            parsed = json.loads(cleaned)
            en_notes = parsed.get("en", "").strip()[:490]
            ar_notes = parsed.get("ar", "").strip()[:490]
            if en_notes and ar_notes:
                return en_notes, ar_notes
    except Exception as e:
        print(f"[WARN] Gemini API call failed: {e}. Falling back to commit parser.", file=sys.stderr)
        
    return fallback_notes(commits_text)

def fallback_notes(commits_text):
    lines = commits_text.splitlines()
    en_bullets = []
    for line in lines:
        cleaned = re.sub(r"^[a-f0-9]+\s+", "", line)
        if any(cleaned.startswith(p) for p in ["feat", "fix", "refactor", "perf"]):
            title = re.sub(r"^(feat|fix|refactor|perf)(\([^)]+\))?:\s*", "", cleaned)
            en_bullets.append(f"• {title.capitalize()}")
    
    if not en_bullets:
        en_bullets = ["• Bug fixes and overall performance improvements."]
        
    en_text = "\n".join(en_bullets[:4])[:490]
    ar_text = "• تحسينات عامة وإصلاحات لتجربة استخدام أكثر سلاسة."
    return en_text, ar_text

def send_telegram_request(token, chat_id, text, keyboard=None):
    url = f"{TELEGRAM_API_BASE}{token}/sendMessage"
    payload = {
        "chat_id": chat_id,
        "text": text,
        "parse_mode": "HTML"
    }
    if keyboard:
        payload["reply_markup"] = json.dumps(keyboard)
        
    req = urllib.request.Request(
        url,
        data=urllib.parse.urlencode(payload).encode("utf-8")
    )
    with urllib.request.urlopen(req, timeout=10) as resp:
        return json.loads(resp.read().decode("utf-8"))

def answer_callback(token, callback_id, text):
    url = f"{TELEGRAM_API_BASE}{token}/answerCallbackQuery"
    payload = {
        "callback_query_id": callback_id,
        "text": text
    }
    req = urllib.request.Request(
        url,
        data=urllib.parse.urlencode(payload).encode("utf-8")
    )
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            pass
    except Exception as e:
        print(f"[WARN] Failed to answer callback: {e}", file=sys.stderr)

def write_whatsnew_files(en_notes, ar_notes):
    out_dir = os.path.join("distribution", "whatsnew")
    os.makedirs(out_dir, exist_ok=True)
    
    en_path = os.path.join(out_dir, "whatsnew-en-US")
    ar_path = os.path.join(out_dir, "whatsnew-ar")
    
    with open(en_path, "w", encoding="utf-8") as f:
        f.write(en_notes.strip() + "\n")
        
    with open(ar_path, "w", encoding="utf-8") as f:
        f.write(ar_notes.strip() + "\n")
        
    print(f"✅ Wrote release notes to {en_path} and {ar_path}")

def poll_telegram_decision(token, chat_id, initial_en, initial_ar, version_str, timeout_seconds=900):
    start_time = time.time()
    
    # Get current latest update_id so we only process new events
    updates_url = f"{TELEGRAM_API_BASE}{token}/getUpdates?offset=-1&timeout=0"
    last_update_id = 0
    try:
        with urllib.request.urlopen(updates_url, timeout=10) as resp:
            data = json.loads(resp.read().decode("utf-8"))
            if data.get("result"):
                last_update_id = data["result"][-1]["update_id"]
    except Exception as e:
        print(f"[WARN] Failed to get initial update id: {e}", file=sys.stderr)

    # Send the approval prompt
    message_text = (
        f"🚀 <b>Sahaba Companions — New Release Pending Approval!</b>\n\n"
        f"📦 <b>Version:</b> <code>{version_str}</code>\n"
        f"🏷 <b>Track:</b> <code>Open Testing (beta)</code>\n\n"
        f"🇬🇧 <b>Proposed Notes (English):</b>\n<pre>{initial_en}</pre>\n\n"
        f"🇸🇦 <b>Proposed Notes (Arabic):</b>\n<pre>{initial_ar}</pre>\n\n"
        f"💡 <i>Tap 'Approve' to deploy, 'Cancel' to abort, or reply to this message with custom notes to edit.</i>"
    )
    keyboard = {
        "inline_keyboard": [
            [
                {"text": "✅ Approve & Deploy", "callback_data": "approve_release"},
                {"text": "❌ Cancel Release", "callback_data": "cancel_release"}
            ]
        ]
    }
    
    send_telegram_request(token, chat_id, message_text, keyboard)
    print(f"⏳ Waiting for Telegram response from chat {chat_id} (timeout: {timeout_seconds}s)...")
    
    offset = last_update_id + 1
    current_en = initial_en
    current_ar = initial_ar

    while time.time() - start_time < timeout_seconds:
        poll_url = f"{TELEGRAM_API_BASE}{token}/getUpdates?offset={offset}&timeout=20"
        try:
            with urllib.request.urlopen(poll_url, timeout=25) as resp:
                data = json.loads(resp.read().decode("utf-8"))
                for update in data.get("result", []):
                    offset = max(offset, update["update_id"] + 1)
                    
                    # 1. Handle Inline Button Clicks
                    if "callback_query" in update:
                        cb = update["callback_query"]
                        cb_data = cb.get("data")
                        cb_id = cb.get("id")
                        
                        if cb_data == "approve_release":
                            answer_callback(token, cb_id, "Approved! Deploying to Open Testing...")
                            send_telegram_request(token, chat_id, "✅ <b>Release approved!</b> Proceeding with upload to Google Play Store...")
                            write_whatsnew_files(current_en, current_ar)
                            return True
                        elif cb_data == "cancel_release":
                            answer_callback(token, cb_id, "Release cancelled.")
                            send_telegram_request(token, chat_id, "❌ <b>Release cancelled.</b> The workflow has been stopped without uploading.")
                            return False

                    # 2. Handle Text Reply for Custom Release Notes
                    if "message" in update:
                        msg = update["message"]
                        sender_chat = str(msg.get("chat", {}).get("id"))
                        user_text = msg.get("text", "").strip()
                        
                        # Verify the message is from our chat and not a command
                        if sender_chat == str(chat_id) and user_text and not user_text.startswith("/"):
                            print(f"[INFO] User sent custom notes: {user_text}")
                            current_en = user_text[:490]
                            # Use custom text for EN, and keep AR or set note
                            current_ar = user_text[:490]
                            
                            send_telegram_request(
                                token,
                                chat_id,
                                f"✏️ <b>Custom notes received:</b>\n<pre>{current_en}</pre>\n\n🚀 Deploying to Open Testing now..."
                            )
                            write_whatsnew_files(current_en, current_ar)
                            return True

        except Exception as e:
            print(f"[WARN] Polling error: {e}", file=sys.stderr)
            time.sleep(2)

    # Timed out
    send_telegram_request(token, chat_id, "⏰ <b>Release approval timed out (15m).</b> Aborting deployment safely.")
    return False

def main():
    dry_run = "--dry-run" in sys.argv
    
    version_name, version_code = get_app_version()
    version_str = f"{version_name} ({version_code})"
    print(f"📦 Release Version: {version_str}")
    
    commits = get_recent_commits()
    print(f"📝 Recent Commits:\n{commits}")
    
    gemini_key = os.environ.get("GEMINI_API_KEY", "").strip()
    gemini_model = os.environ.get("GEMINI_MODEL", "gemini-2.5-flash").strip()
    
    if gemini_key:
        print(f"🤖 Generating release notes with Gemini ({gemini_model})...")
        en_notes, ar_notes = generate_ai_notes(commits, gemini_key, gemini_model)
    else:
        print("ℹ️ No GEMINI_API_KEY found, using conventional commit fallback...")
        en_notes, ar_notes = fallback_notes(commits)
        
    print(f"\n🇬🇧 English Notes:\n{en_notes}\n")
    print(f"🇸🇦 Arabic Notes:\n{ar_notes}\n")
    
    bot_token = os.environ.get("TELEGRAM_BOT_TOKEN", "").strip()
    chat_id = os.environ.get("TELEGRAM_CHAT_ID", "").strip()
    
    if dry_run or not bot_token or not chat_id:
        print("ℹ️ Dry-run mode or missing Telegram credentials. Writing files directly...")
        write_whatsnew_files(en_notes, ar_notes)
        sys.exit(0)
        
    # Run interactive approval loop
    approved = poll_telegram_decision(bot_token, chat_id, en_notes, ar_notes, version_str)
    if not approved:
        print("❌ Release not approved or cancelled.")
        sys.exit(1)
        
    print("🎉 Release approved and notes written successfully!")

if __name__ == "__main__":
    main()
