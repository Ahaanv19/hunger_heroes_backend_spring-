#!/usr/bin/env python3
import os
import sys
from http import cookiejar
from urllib import request, parse

BASE_URL = os.getenv("BASE_URL", "http://localhost:8585")
UID = os.getenv("UID", "toby")
PASSWORD = os.getenv("PASSWORD", "Admin14*&*41")

COOKIE_JAR = cookiejar.CookieJar()
OPENER = request.build_opener(request.HTTPCookieProcessor(COOKIE_JAR))


def http_request(method, url, data=None, headers=None):
    if headers is None:
        headers = {}
    req_data = None
    if data is not None:
        req_data = data.encode("utf-8")
    req = request.Request(url, data=req_data, headers=headers, method=method)
    try:
        with OPENER.open(req) as resp:
            return resp.status, resp.read(), resp.headers
    except Exception as exc:
        if hasattr(exc, "code"):
            return exc.code, b"", getattr(exc, "headers", {})
        raise


def print_status(label, method, path, json_body=None):
    url = f"{BASE_URL}{path}"
    headers = {}
    data = None
    if json_body is not None:
        headers["Content-Type"] = "application/json"
        data = json_body
    status, body, resp_headers = http_request(method, url, data=data, headers=headers)
    location = resp_headers.get("Location") if hasattr(resp_headers, "get") else None
    if location:
        print(f"{label} -> {status} (Location: {location})")
    else:
        print(f"{label} -> {status}")
    if status >= 400 and body:
        try:
            text = body.decode("utf-8", errors="replace")
        except Exception:
            text = "<non-text body>"
        print("  Error body:", text[:500])
    return status, body


def dump_cookies():
    if not COOKIE_JAR:
        print("Cookies: <none>")
        return
    print("Cookies:")
    for cookie in COOKIE_JAR:
        print(f"  {cookie.name}={cookie.value}; path={cookie.path}; secure={cookie.secure}")


def main():
    print("== Authenticate (JWT cookie) ==")
    auth_body = '{"uid":"%s","password":"%s"}' % (UID, PASSWORD)
    status, body = print_status("POST /authenticate", "POST", "/authenticate", json_body=auth_body)
    if body:
        try:
            print("Auth response:", body.decode("utf-8"))
        except Exception:
            print("Auth response: <non-text body>")
    dump_cookies()

    print("\n== Person APIs (auth required) ==")
    print_status("GET /api/person/get", "GET", "/api/person/get")
    print_status("GET /api/people", "GET", "/api/people")

    print("\n== Admin-only check ==")
    print_status("DELETE /api/person/6", "DELETE", "/api/person/6")

    print("\n== Analytics (auth required) ==")
    print_status("GET /api/analytics/", "GET", "/api/analytics/")

    print("\n== Code Runner (auth required) ==")
    print_status("GET /api/challenge-submission/my-submissions", "GET", "/api/challenge-submission/my-submissions")

    print("\n== Tinkle (auth required) ==")
    print_status("GET /api/tinkle/all", "GET", "/api/tinkle/all")

    print("\n== Export/Import (admin only) ==")
    print_status("GET /api/exports/getAll", "GET", "/api/exports/getAll")
    print_status("GET /api/imports/backups", "GET", "/api/imports/backups")

    print("\nDone.")


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print(f"Error: {exc}", file=sys.stderr)
        sys.exit(1)
