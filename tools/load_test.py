#!/usr/bin/env python3
"""
Навантажувальний тест каталогу (НФВ: відгук сервера <= 800 мс при 50 одночасних користувачах).

Лише стандартна бібліотека Python 3 (нічого встановлювати не треба).
Кожен «користувач» — окремий потік зі своєю сесією: входить у систему (з CSRF-токеном), після чого
одночасно з усіма надсилає запити до каталогу (/books) з різними параметрами пошуку, сортування та сторінок.

Запуск (застосунок має працювати, у БД бажано завантажити docs/demo_data.sql):
    python tools/load_test.py
    python tools/load_test.py --users 50 --requests 30 --url http://localhost:8082
"""
import argparse
import http.cookiejar
import re
import statistics
import threading
import time
import urllib.parse
import urllib.request

QUERIES = [
    "", "Збірник", "Кобзар", "Франко", "Програмування", "код", "задач",
    "?sort=author&dir=desc", "?sort=year&dir=asc", "?page=1", "?page=2", "?size=10",
]


def build_path(item: str, i: int) -> str:
    if item.startswith("?"):
        return "/books" + item
    params = {"q": item, "sort": ("title", "author", "year")[i % 3], "dir": ("asc", "desc")[i % 2], "page": i % 5}
    return "/books?" + urllib.parse.urlencode(params)


def login(base: str, email: str, password: str) -> urllib.request.OpenerDirector:
    jar = http.cookiejar.CookieJar()
    opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar))
    html = opener.open(base + "/login", timeout=30).read().decode("utf-8", "replace")
    token = re.search(r'name="_csrf"[^>]*value="([^"]+)"', html) or re.search(r'value="([^"]+)"[^>]*name="_csrf"', html)
    data = {"username": email, "password": password}
    if token:
        data["_csrf"] = token.group(1)
    body = urllib.parse.urlencode(data).encode()
    resp = opener.open(urllib.request.Request(base + "/login", data=body), timeout=30)
    if "/login" in resp.geturl():
        raise RuntimeError("Не вдалося увійти (перевірте email/пароль): " + resp.geturl())
    return opener


def worker(idx, args, barrier, results, errors):
    try:
        opener = login(args.url, args.email, args.password)
    except Exception as exc:  # noqa: BLE001
        errors.append(f"користувач {idx}: вхід — {exc}")
        barrier.wait()
        return
    barrier.wait()  # усі стартують разом
    for n in range(args.requests):
        path = build_path(QUERIES[(idx + n) % len(QUERIES)], idx + n)
        start = time.perf_counter()
        try:
            with opener.open(args.url + path, timeout=30) as resp:
                resp.read()
                status = resp.status
            elapsed = (time.perf_counter() - start) * 1000
            if status != 200:
                errors.append(f"{path}: HTTP {status}")
            else:
                results.append(elapsed)
        except Exception as exc:  # noqa: BLE001
            errors.append(f"{path}: {exc}")


def main():
    parser = argparse.ArgumentParser(description="Навантажувальний тест каталогу")
    parser.add_argument("--url", default="http://localhost:8082")
    parser.add_argument("--users", type=int, default=50, help="кількість одночасних користувачів")
    parser.add_argument("--requests", type=int, default=20, help="запитів на кожного користувача")
    parser.add_argument("--email", default="admin@library.com")
    parser.add_argument("--password", default="admin123")
    parser.add_argument("--limit-ms", type=float, default=800.0, help="допустимий час відгуку, мс")
    args = parser.parse_args()

    results, errors = [], []
    barrier = threading.Barrier(args.users)
    threads = [threading.Thread(target=worker, args=(i, args, barrier, results, errors)) for i in range(args.users)]
    t0 = time.perf_counter()
    for t in threads:
        t.start()
    for t in threads:
        t.join()
    total = time.perf_counter() - t0

    print(f"Користувачів: {args.users}, запитів на користувача: {args.requests}")
    if not results:
        print("Немає успішних відповідей.")
        for e in errors[:10]:
            print("  помилка:", e)
        raise SystemExit(1)

    results.sort()
    p95 = results[int(len(results) * 0.95) - 1] if len(results) > 1 else results[0]
    print(f"Успішних запитів: {len(results)}, помилок: {len(errors)}, загальний час: {total:.1f} с")
    print(f"Середній відгук: {statistics.mean(results):.0f} мс")
    print(f"Медіана:         {statistics.median(results):.0f} мс")
    print(f"95-й перцентиль: {p95:.0f} мс")
    print(f"Максимум:        {results[-1]:.0f} мс")
    for e in errors[:10]:
        print("  помилка:", e)
    ok = p95 <= args.limit_ms and not errors
    print(f"\nВимога «відгук <= {args.limit_ms:.0f} мс» (за 95-м перцентилем): {'ВИКОНАНО' if ok else 'НЕ ВИКОНАНО'}")
    raise SystemExit(0 if ok else 2)


if __name__ == "__main__":
    main()
