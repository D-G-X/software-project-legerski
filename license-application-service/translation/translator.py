import argparse
import json
import os
import re
import sys
import time

from dotenv import load_dotenv
from openai import OpenAI
from ollama import chat

load_dotenv()

# TODO: Store OpenAI API key in .env file as OPENAI_API_KEY
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

USE_REMOTE_BACKEND = 0
GPT_MODEL = "gpt-5-mini"
MISTRAL_MODEL = "mistral-12k"
MAX_RETRIES = 3

# Mapping of target languages to their respective locale codes
LANGUAGES = {
  "german": "de-DE",
  "french": "fr-FR",
  "spanish": "es-ES",
  "hindi": "hi-IN",
  "indonesian": "id-ID",
}

LOCALE_PATH = "../src/main/webapp/locales"
SOURCE_FILE = "english.json"


# -- Translates the values of a JSON object to the target language using the selected GPT model. --
def translate_json_values(data: dict, target_language: str) -> dict:
  prompt = f"""
You are a translation engine.

Rules:
- Source language is English.
- Translate ONLY the JSON values to {target_language}.
- TRANSLATE THE FULL JSON
- DO NOT change keys.
- DO NOT add, remove, or reorder fields.
- DO NOT translate URLs, email addresses, numbers, IDs, or addresses.
- Output VALID JSON only.
- No markdown. No explanations.

JSON:
{json.dumps(data, ensure_ascii=False)}
"""

  last_error = None

  for attempt in range(1, MAX_RETRIES + 1):
    try:
      if USE_REMOTE_BACKEND:
        response = client.responses.create(
          model=GPT_MODEL,
          input=prompt,
        )
        return safe_json_load(response.output_text)
      else:
        response = chat(
          model=MISTRAL_MODEL,
          messages=[
            {"role": "user", "content": prompt}
          ],
          options={"num_ctx": 12288, "temperature": 0.1},
        )
        return safe_json_load(response["message"]["content"])

    except Exception as e:
      last_error = e
      if attempt < MAX_RETRIES:
        print(f"⚠ Retry {attempt}/{MAX_RETRIES} failed ({e}), retrying...")
        time.sleep(1)
      else:
        break

  raise RuntimeError(
    f"Translation failed after {MAX_RETRIES} retries: {last_error}"
  )


# -- Safely loads JSON from a string, returning an empty dict on failure. --
def safe_json_load(text: str) -> dict:
  match = re.search(r"\{.*\}", text, re.DOTALL)
  if not match:
    with open("last_raw_response.txt", "w", encoding="utf-8") as f:
      f.write(text)
    raise ValueError("No JSON found in response")

  return json.loads(match.group(0))


# -- Manually overrides the "locale" token in the JSON data to match the target language's locale. --
def override_locale_token(data: dict, language: str) -> dict:
  if "locale" in data:
    data["locale"] = LANGUAGES[language]
  return data


# -- Opens the source JSON file, translates it to each target language, and writes the results to separate files. --
def main():
  with open(f"{LOCALE_PATH}/{SOURCE_FILE}", "r", encoding="utf-8") as f:
    source_data = json.load(f)

  for language_name in LANGUAGES.keys():
    print(f"→ translating to {language_name}")

    translated = translate_json_values(
      source_data,
      target_language=language_name.capitalize(),
    )

    translated = override_locale_token(translated, language_name)

    output_file = f"{LOCALE_PATH}/{language_name}.json"
    with open(output_file, "w", encoding="utf-8") as f:
      json.dump(translated, f, ensure_ascii=False, indent=2)

    print(f"✔ written {output_file}")

  print("All translations completed successfully 🎉")


if __name__ == "__main__":
  main()
