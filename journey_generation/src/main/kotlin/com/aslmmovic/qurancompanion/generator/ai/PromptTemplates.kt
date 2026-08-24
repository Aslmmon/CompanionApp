package com.aslmmovic.qurancompanion.generator.ai

object PromptTemplates {

    // System prompt instructing LLM on structured Sahaba story generation
    fun buildExtractionPrompt(rawText: String, language: String, startDayNumber: Int): String {
        val isArabic = language == "ar"

        val langInstruction = if (isArabic) {
            "Generate the output completely in pure, eloquent Arabic only (فصحى بليغة ورصينة تناسب جلال السيرة النبوية). Do not include any English translations, slashes (/), or English words in titles, categories, emotions, or persons."
        } else {
            "Generate the output completely in pure, dignified English only suitable for Islamic biographical storytelling. Do not include any Arabic words or slashes in titles."
        }

        val categoryDefault = if (isArabic) "الصحابة" else "The Companions"
        val personExample = if (isArabic) "سعيد بن عامر الجمحي" else "Saeed ibn Amir al-Jumahi"
        val emotionExample = if (isArabic) "الزهد، التقوى، الإيثار" else "Sacrifice, Truthfulness, Mercy"
        val introTitle = if (isArabic) "مقدمة" else "Introduction"
        val storyTitle = if (isArabic) "عنوان القصة والتحول الإيماني" else "Turning Point and Narrative"
        val lessonsTitle = if (isArabic) "الدروس المستفادة" else "Lessons Learned"
        val reflectionTitle = if (isArabic) "تأمل" else "Reflection"
        val actionTitle = if (isArabic) "عمل اليوم" else "Today's Action"
        val quoteExample = if (isArabic) "«سعيد بن عامر رجل اشترى الآخرة بالدنيا وآثر الله ورسوله على سواهما»" else "«Saeed ibn Amir was a man who chose the Hereafter over this world.»"
        val intentionExample = if (isArabic) "اقرأ رحلة اليوم لتتأمل في زهد الصحابة وتفضيل ما عند الله..." else "Read today's journey and reflect on sincerity and putting Allah first..."

        return """
You are an expert Islamic historian, scholar, and narrative writer specializing in the lives of the Companions of the Prophet Muhammad ﷺ (سير الصحابة والتابعين).

Analyze the provided source text extracted from a Sahaba biography book, identify each Sahabi discussed, and transform each companion's biography into an inspiring, reflective daily companion journey matching the JSON structure below.

Language instruction: $langInstruction

### Required JSON Schema for EACH Journey:
[
  {
    "id": "day_001",
    "dayNumber": $startDayNumber,
    "title": "Inspiring and catchy title capturing the core essence/turning point in target language",
    "subtitle": "A captivating one-sentence hook summarizing the journey in target language",
    "category": "$categoryDefault",
    "person": "$personExample",
    "emotion": "$emotionExample",
    "theme": "One of: 'Sunrise', 'Emerald', 'Midnight', 'Golden', 'Ocean', 'Mushaf'",
    "heroQuote": "$quoteExample",
    "intention": "$intentionExample",
    "durationMinutes": 10,
    "difficulty": "Easy",
    "estimatedReadingMinutes": 8,
    "cover": {
      "type": "illustration",
      "asset": "snake_case_name_of_sahabi_without_titles (e.g. saeed_ibn_amir, anas_ibn_malik)"
    },
    "steps": [
      {
        "type": "INTRO",
        "title": "$introTitle",
        "content": "A compelling 1-2 paragraph introduction setting the background, origin, character, and early life of the Sahabi before the major event."
      },
      {
        "type": "STORY",
        "title": "$storyTitle",
        "content": "Rich, detailed, narrative story divided into readable paragraphs with \\n\\n. Include authentic dialogues, historical setting, the turning point, challenges faced, steadfastness with the Prophet ﷺ, and their noble legacy until their passing."
      },
      {
        "type": "KEY_LESSONS",
        "title": "$lessonsTitle",
        "content": "1. First profound lesson derived directly from the narrative.\\n2. Second practical lesson.\\n3. Third spiritual lesson.\\n4. Fourth leadership or character lesson."
      },
      {
        "type": "REFLECTION",
        "title": "$reflectionTitle",
        "content": "A deep, personal contemplative question for the reader to reflect on their own life and deeds."
      },
      {
        "type": "ACTION",
        "title": "$actionTitle",
        "content": "A tangible, practical good deed or sunnah the reader can implement today inspired by the Sahabi."
      }
    ],
    "references": [
      "تاريخ الإسلام",
      "الإصابة في تمييز الصحابة",
      "سير أعلام النبلاء",
      "صفة الصفوة",
      "حلية الأولياء"
    ],
    "tags": [
      "Name of Sahabi",
      "Key Virtues",
      "Key Caliph/Battle/Event"
    ]
  }
]

### Strict Formatting Rules:
1. Return ONLY the valid JSON Array `[...]`. Do not include conversational preambles or explanations.
2. Maintain Islamic honorifics (ﷺ for the Prophet, رضي الله عنه / رضي الله عنها for Companions).
3. Ensure all 5 step types (`INTRO`, `STORY`, `KEY_LESSONS`, `REFLECTION`, `ACTION`) are present for every journey.
4. Titles MUST be purely in the target language ($language) without dual-language slashes (e.g., use strictly "$introTitle", "$lessonsTitle", "$reflectionTitle", "$actionTitle").
5. Each dayNumber should increment sequentially starting from $startDayNumber.

### Source Text to Process:
\"\"\"
$rawText
\"\"\"
""".trimIndent()
    }

    // System prompt for translating Arabic journeys to dignified English
    fun buildTranslationPrompt(arabicJsonChunk: String): String {
        return """
You are an expert Islamic scholar, translator, and English author specializing in the biographies of the Companions of the Prophet Muhammad ﷺ (The Sahaba).

Translate the following JSON array of Arabic Sahaba daily journeys into fluent, dignified, and inspiring English suitable for English-speaking readers.

### Strict Translation Rules:
1. Return ONLY the valid JSON Array `[...]` matching the exact schema.
2. Step types and titles MUST be strictly in English:
   - "INTRO" -> "Introduction"
   - "STORY" -> An engaging, catchy English title for the narrative
   - "KEY_LESSONS" -> "Lessons Learned"
   - "REFLECTION" -> "Reflection"
   - "ACTION" -> "Today's Action"
3. "category" MUST be "The Companions".
4. "emotion", "title", "subtitle", "heroQuote", and "intention" must be translated gracefully into English.
5. "person" should be the standard Romanized English transliteration (e.g. "Saeed ibn Amir al-Jumahi", "At-Tufayl ibn Amr ad-Dawsi", "Abu Ayyub al-Ansari").
6. Keep Islamic salutations and honorifics (ﷺ / Peace be upon him, May Allah be pleased with him/her / رضي الله عنه).
7. Preserve exact "id", "dayNumber", "durationMinutes", "difficulty", "estimatedReadingMinutes", "theme", and "cover" object (keeping same cover asset name).

### Arabic Journeys JSON to Translate:
$arabicJsonChunk
""".trimIndent()
    }
}
