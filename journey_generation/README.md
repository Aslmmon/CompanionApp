# Journey Generation Tool (`:journey_generation`)

A CLI & automation tool that extracts stories of the Sahaba (Companions) from PDF books using Google Gemini AI, structuring them into the exact JSON schema required by the Quran Companion mobile app.

---

## 1. Setup

### Set Gemini API Key
Provide your Gemini API key through one of the following methods:
- **local.properties**: Add `GEMINI_API_KEY=your_api_key` to your `local.properties` file in the root folder.
- **Environment Variable**: `export GEMINI_API_KEY="your_api_key"`
- **CLI Flag**: Pass `--api-key "your_api_key"` when executing the command.

---

## 2. Usage

Place your PDF books (e.g. *صور من حياة الصحابة* or English biographies) in `journey_generation/input_pdfs/`.

### Run via Helper Script:

```bash
# Generate 30 Arabic Sahaba Journeys & Sync directly into Mobile App:
./journey_generation/generate_journeys.sh --target-count 30 --lang ar --sync-app

# Generate 30 English Sahaba Journeys:
./journey_generation/generate_journeys.sh --target-count 30 --lang en

# Extract custom batch/page range:
./journey_generation/generate_journeys.sh --start-page 5 --end-page 60 --target-count 5 --lang ar
```

---

## 3. Available CLI Options

| Argument | Description | Default |
|---|---|---|
| `--pdf <path>` | Path to the input PDF file | First `.pdf` in `journey_generation/input_pdfs/` |
| `--target-count <int>` | Total number of unique Sahaba stories to extract | `30` |
| `--chunk-size <int>` | Number of pages to process per batch | `15` |
| `--lang <ar\|en>` | Output language (`ar` or `en`) | `ar` |
| `--start-day <int>` | Initial day number (`dayNumber`) | `1` |
| `--start-page <int>` | First page in PDF to begin extracting | `5` |
| `--end-page <int>` | Last page in PDF to extract | End of document |
| `--output <path>` | Destination output file path | `journey_generation/output_json/journeys_<lang>.json` |
| `--model <name>` | Gemini AI model name | `gemini-2.5-flash` |
| `--api-key <key>` | Gemini API Key | Resolved from local.properties / env |
| `--sync-app` | Sync directly into `shared/.../composeResources/files/<lang>/journeys.json` | `false` |
