import json
import os
import time
from dotenv import load_dotenv
from openai import OpenAI
from typing import Any, Dict

load_dotenv()

client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

GPT_MODEL = "gpt-5-mini"
MAX_RETRIES = 3

LANGUAGES = {
  "german": "de-DE",
  "french": "fr-FR",
  "spanish": "es-ES",
  "hindi": "hi-IN",
  "indonesian": "id-ID",
}

LOCALE_PATH = "../src/main/webapp/locales"
SOURCE_FILE = "english.json"


# ─────────────────────────────────────────────────────────────
# STRUCTURE VALIDATION
# ─────────────────────────────────────────────────────────────

def assert_same_structure(a: dict, b: dict, path: str = "root"):
  if type(a) is not type(b):
    raise ValueError(f"Type mismatch at {path}")

  if isinstance(a, dict):
    if a.keys() != b.keys():
      raise ValueError(
        f"Key mismatch at {path}: {list(a.keys())} != {list(b.keys())}"
      )
    for k in a:
      assert_same_structure(a[k], b[k], f"{path}.{k}")

  elif isinstance(a, list):
    if len(a) != len(b):
      raise ValueError(f"List length mismatch at {path}")
    for i, (x, y) in enumerate(zip(a, b)):
      assert_same_structure(x, y, f"{path}[{i}]")


def safe_json_load_exact(output: str, input: Dict[str, Any]) -> Dict[str, Any]:
  try:
    parsed_output = json.loads(output)
  except Exception:
    with open("last_raw_response.txt", "w", encoding="utf-8") as f:
      f.write(output)
    raise ValueError("Response is not valid JSON")

  if not isinstance(parsed_output, dict):
    raise ValueError("Response JSON is not an object")

  assert_same_structure(input, parsed_output)
  return parsed_output


# ─────────────────────────────────────────────────────────────
# TRANSLATION
# ─────────────────────────────────────────────────────────────

def translate_section(
    section_name: str,
    data: Dict[str, Any],
    target_language: str
) -> Dict[str, Any]:
  section_wrapper = {section_name: data}

  last_error = None

  prompt = f"""
You are a deterministic JSON transformation engine.

Rules (MANDATORY):
- Translate ONLY JSON string VALUES from English to {target_language}.
- NEVER modify, translate, add, remove, or reorder JSON keys.
- NEVER modify the JSON structure.
- NEVER translate URLs, emails, numbers, IDs, addresses, or codes.
- Preserve punctuation, formatting, and special characters.
- Output VALID JSON ONLY.
- No markdown. No comments. No explanations.

If ANY rule cannot be satisfied:
Output the ORIGINAL JSON unchanged.

Input JSON (authoritative, must be preserved):
{json.dumps(section_wrapper, ensure_ascii=False)}
"""

  for attempt in range(1, MAX_RETRIES + 1):
    if last_error:
      prompt += f"\nPrevious error that absolutely needs to be avoided this time: {last_error}\n"
    try:
      response = client.responses.create(
        model=GPT_MODEL,
        input=prompt,
      )
      raw = response.output_text

      parsed = safe_json_load_exact(raw, section_wrapper)
      return parsed[section_name]

    except Exception as e:
      last_error = e
      if attempt < MAX_RETRIES:
        print(f"⚠ {section_name}: retry {attempt}/{MAX_RETRIES} ({e})")
        time.sleep(1)

  raise RuntimeError(
    f"Section '{section_name}' failed after {MAX_RETRIES} retries: {last_error}"
  )


# ─────────────────────────────────────────────────────────────
# MAIN
# ─────────────────────────────────────────────────────────────

def main():
  with open(f"{LOCALE_PATH}/{SOURCE_FILE}", "r", encoding="utf-8") as f:
    source = json.load(f)

  for language, locale in LANGUAGES.items():
    print(f"→ translating {language}")

    result: Dict[str, Any] = {}

    # never translate the locale code itself
    if "locale" in source:
      result["locale"] = locale

    sections = [(k, v) for k, v in source.items() if k != "locale"]
    for idx, (key, value) in enumerate(sections, start=1):
      print(f"\t↳ section: {key}\t\t({idx}/{len(sections)})")
      if not isinstance(value, dict):
        result[key] = value
        continue

      result[key] = translate_section(
        section_name=key,
        data=value,
        target_language=language.capitalize(),
      )

    output_file = f"{LOCALE_PATH}/{language}.json"
    with open(output_file, "w", encoding="utf-8") as f:
      json.dump(result, f, ensure_ascii=False, indent=2)

    print(f"✔ written {output_file}")

  print("All translations completed successfully.")


if __name__ == "__main__":
  main()
