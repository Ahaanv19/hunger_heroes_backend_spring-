#!/usr/bin/env python3

"""
db_restore-local2prod.py
Uploads local database to production server

Usages:
    python3 scripts/db_restore-local2prod.py
"""

import requests
import json
import os
import sys
from datetime import datetime
from pathlib import Path

# Configuration
LOCAL_URL = "http://localhost:8585"
PROD_URL = "https://spring.opencodingsociety.com"
PROD_LOGIN_URL = f"{PROD_URL}/login"
PROD_IMPORT_URL = f"{PROD_URL}/api/imports/manual"
LOCAL_LOGIN_URL = f"{LOCAL_URL}/login"
LOCAL_EXPORT_URL = f"{LOCAL_URL}/api/exports/getAll"

def load_env_credentials():
    """Load credentials from .env file"""
    env_file = Path(__file__).parent.parent / ".env"

    if not env_file.exists():
        print(f" Error: .env file not found at {env_file}")
        sys.exit(1)

    credentials = {}
    required_keys = ["ADMIN_PASSWORD", "LOCAL_ADMIN_UID", "PROD_ADMIN_UID"]

    with open(env_file, "r") as f:
        for line in f:
            line = line.strip()
            for key in required_keys:
                if line.startswith(f"{key}="):
                    value = line.split("=", 1)[1].strip()
                    # Remove quotes if present
                    value = value.strip('"').strip("'")
                    credentials[key] = value

    # Check if all required credentials are present
    missing_keys = [key for key in required_keys if key not in credentials]
    if missing_keys:
        print(f" Error: Missing required credentials in .env file: {', '.join(missing_keys)}")
        sys.exit(1)

    return credentials

def check_local_server():
    """Check if local Spring Boot server is running"""
    try:
        response = requests.get(f"{LOCAL_URL}/api/exports/getAll", timeout=5)
        return response.status_code != 404
    except requests.exceptions.RequestException:
        return False

def export_local_database(credentials):
    """Export local database to JSON file"""
    print(" Exporting local database...")

    if not check_local_server():
        print(" Error: Local Spring Boot server is not running on port 8585")
        print("   Please start it first: ./mvnw spring-boot:run")
        sys.exit(1)

    try:
        # Authenticate to local server via form login to obtain session cookies
        session = requests.Session()
        auth_data = {
            "username": credentials["LOCAL_ADMIN_UID"],
            "password": credentials["ADMIN_PASSWORD"],
        }

        auth_resp = session.post(LOCAL_LOGIN_URL, data=auth_data, timeout=10, allow_redirects=False)

        if not (auth_resp.status_code == 302 and session.cookies):
            msg_preview = auth_resp.text[:200] if auth_resp.text else "No response body"
            print(f" Error: Local authentication failed (HTTP {auth_resp.status_code}).")
            print("   Ensure the credentials are correct and login form is enabled.")
            if msg_preview:
                print(f"   Response preview: {msg_preview}")
            sys.exit(1)

        # Use authenticated session to access protected export endpoint
        response = session.get(LOCAL_EXPORT_URL, timeout=30)
        if response.status_code == 401:
            print(" Error: Unauthorized (401) when accessing local export endpoint.")
            print("   This endpoint requires login; authentication may have failed.")
            sys.exit(1)
        response.raise_for_status()
        data = response.json()

        # Create backups directory if it doesn't exist
        backups_dir = Path(__file__).parent.parent / "volumes" / "backups"
        backups_dir.mkdir(parents=True, exist_ok=True)

        # Save with timestamp
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        export_file = backups_dir / f"local_export_{timestamp}.json"

        with open(export_file, "w") as f:
            json.dump(data, f, indent=2)

        print(f" Local database exported to: {export_file}")
        print(f"  Tables exported: {len(data)}")
        print(f"  File size: {export_file.stat().st_size / 1024:.2f} KB")

        return export_file, data

    except requests.exceptions.RequestException as e:
        print(f" Error exporting local database: {e}")
        sys.exit(1)
    except Exception as e:
        print(f" Unexpected error: {e}")
        sys.exit(1)

def authenticate_to_production(credentials):
    """Authenticate to production server using JWT and return authenticated session"""
    print("\n Authenticating to production server...")

    # Use JWT authentication endpoint
    auth_url = f"{PROD_URL}/authenticate"
    auth_data = {
        "uid": credentials["PROD_ADMIN_UID"],
        "password": credentials["ADMIN_PASSWORD"]
    }

    try:
        # Create a session to handle cookies
        session = requests.Session()
        response = session.post(auth_url, json=auth_data, timeout=10)

        # JWT endpoint returns 200 on success with JWT token in cookie
        if response.status_code == 200:
            # Check if we got the JWT cookie
            if "jwt_java_spring" in session.cookies:
                print(f" Authenticated as '{credentials['PROD_ADMIN_UID']}'")
                return session
            else:
                print(" Authentication succeeded but no JWT token received")
                sys.exit(1)
        else:
            print(f" Authentication failed: HTTP {response.status_code}")
            sys.exit(1)

    except requests.exceptions.HTTPError as e:
        print(f" Authentication failed: HTTP {e.response.status_code}")
        sys.exit(1)
    except requests.exceptions.RequestException as e:
        print(f" Error connecting to production server: {e}")
        sys.exit(1)

def upload_to_production(export_file, session):
    """Upload JSON file to production server"""
    print(f"\n Uploading database to production...")
    print(f"   File: {export_file.name}")
    print(f"   Target: {PROD_IMPORT_URL}")

    try:
        # CRITICAL: Add X-Origin: client header to trigger JWT authentication
        # Without this header, the server expects session-based auth instead of JWT
        headers = {"X-Origin": "client"}

        with open(export_file, "rb") as f:
            files = {"file": (export_file.name, f, "application/json")}
            response = session.post(
                PROD_IMPORT_URL,
                files=files,
                headers=headers,
                timeout=1200  # 2 minute timeout for large uploads
            )
            response.raise_for_status()

            print(" Database uploaded successfully!")
            print(f"   Response status: {response.status_code}")

            # Try to show response content if it's text
            if "text/html" in response.headers.get("Content-Type", ""):
                if "success" in response.text.lower():
                    print("   Import status: SUCCESS")
                elif "error" in response.text.lower():
                    print("   Import status: ERROR (check production logs)")
                    print(f"   Response preview: {response.text[:200]}")
            else:
                print(f"   Response: {response.text[:200]}")

    except requests.exceptions.HTTPError as e:
        print(f" Upload failed: HTTP {e.response.status_code}")
        print(f" Response content: {e.response.text[:500]}")
        sys.exit(1)
    except requests.exceptions.RequestException as e:
        print(f" Error uploading to production: {e}")
        sys.exit(1)

def main():
    print("=" * 60)
    print("DATABASE RESTORE: Local → Production")
    print("=" * 60)
    print()

    # Step 1: Load credentials from .env
    credentials = load_env_credentials()

    # Step 2: Export local database
    export_file, data = export_local_database(credentials)

    # Step 3: Authenticate to production
    session = authenticate_to_production(credentials)

    # Step 4: Upload to production
    upload_to_production(export_file, session)

    print()
    print("=" * 60)
    print("RESTORE COMPLETE")
    print("=" * 60)
    print()
    print(f"Local database has been uploaded to production:")
    print(f"  Production URL: {PROD_URL}")
    print(f"  Backup saved: {export_file}")
    print()

if __name__ == "__main__":
    main()
