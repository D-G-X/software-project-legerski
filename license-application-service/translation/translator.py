import json
import os
import time
import threading
from concurrent.futures import ThreadPoolExecutor, as_completed
from dotenv import load_dotenv
from openai import OpenAI
from typing import Any, Dict, Tuple, List

load_dotenv()

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

# Max workers defined in the .env if used
MAX_WORKERS = int(os.getenv("TRANSLATE_WORKERS", str(len(LANGUAGES))))

# Thread-safe printing
_print_lock = threading.Lock()


def log(msg: str):
    with _print_lock:
        print(msg, flush=True)


# Thread-local OpenAI client
_thread_local = threading.local()


def get_client() -> OpenAI:
    if not hasattr(_thread_local, "client"):
        _thread_local.client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))
    return _thread_local.client


# Validation

def count_lines(src_path: str, dst_path: str):
    with open(src_path, "r", encoding="utf-8") as f:
        src_lines = sum(1 for _ in f)
    with open(dst_path, "r", encoding="utf-8") as f:
        dst_lines = sum(1 for _ in f)
    if src_lines != dst_lines:
        log(f"⚠ Line count mismatch: {src_lines} (source) vs {dst_lines} (destination)")


def assert_same_structure(a: dict, b: dict, path: str = "root"):
    if type(a) is not type(b):
        raise ValueError(f"Type mismatch at {path}")

    if isinstance(a, dict):
        if a.keys() != b.keys():
            raise ValueError(f"Key mismatch at {path}: {list(a.keys())} != {list(b.keys())}")
        for k in a:
            assert_same_structure(a[k], b[k], f"{path}.{k}")

    elif isinstance(a, list):
        if len(a) != len(b):
            raise ValueError(f"List length mismatch at {path}")
        for i, (x, y) in enumerate(zip(a, b)):
            assert_same_structure(x, y, f"{path}[{i}]")


def safe_json_load_exact(output: str, input_obj: Dict[str, Any]) -> Dict[str, Any]:
    try:
        parsed_output = json.loads(output)
    except Exception:
        with open("last_raw_response.txt", "w", encoding="utf-8") as f:
            f.write(output)
        raise ValueError("Response is not valid JSON")

    if not isinstance(parsed_output, dict):
        raise ValueError("Response JSON is not an object")

    assert_same_structure(input_obj, parsed_output)
    return parsed_output


# Translation
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
            client = get_client()
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
                log(f"⚠ {section_name:<25} retry {attempt:>2}/{MAX_RETRIES:<2} ({e})")
                time.sleep(1)

    raise RuntimeError(f"Section '{section_name}' failed after {MAX_RETRIES} retries: {last_error}")


# Threaded worker
def translate_language(language: str, locale: str, source: Dict[str, Any], src_file_path: str) -> str:
    dst_file_path = f"{LOCALE_PATH}/{language}.json"
    log(f"→ translating {language}")

    result: Dict[str, Any] = {}
    # never translate the locale
    if "locale" in source:
        result["locale"] = locale

    sections: List[Tuple[str, Any]] = [(k, v) for k, v in source.items() if k != "locale"]
    for idx, (key, value) in enumerate(sections, start=1):
        log(f"  ↳ [{language}] section: {key:<25} ({idx}/{len(sections)})")
        if not isinstance(value, dict):
            result[key] = value
            continue

        result[key] = translate_section(
            section_name=key,
            data=value,
            target_language=language.capitalize(),
        )

    with open(dst_file_path, "w", encoding="utf-8") as f:
        json.dump(result, f, ensure_ascii=False, indent=2)
        f.write("\n")

    count_lines(src_file_path, dst_file_path)
    log(f"✔ written {dst_file_path}")
    return dst_file_path


# ─────────────────────────────────────────────────────────────
# MAIN (parallel per language)
# ─────────────────────────────────────────────────────────────

def main():
    src_file_path = f"{LOCALE_PATH}/{SOURCE_FILE}"

    with open(src_file_path, "r", encoding="utf-8") as f:
        source = json.load(f)

    failures: List[Tuple[str, str]] = []

    with ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
        futures = {
            executor.submit(translate_language, language, locale, source, src_file_path): language
            for language, locale in LANGUAGES.items()
        }

        for fut in as_completed(futures):
            lang = futures[fut]
            try:
                fut.result()
            except Exception as e:
                failures.append((lang, str(e)))
                log(f"✖ {lang} failed: {e}")

    if failures:
        msgs = "\n".join([f"- {lang}: {err}" for lang, err in failures])
        raise RuntimeError(f"Some translations failed:\n{msgs}")

    log("All translations completed successfully.")


if __name__ == "__main__":
    main()
