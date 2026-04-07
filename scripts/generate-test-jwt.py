#!/usr/bin/env python3
"""
Mint a short-lived HS256 JWT for local development against the msa-poc gateway.

Usage:
  pip install pyjwt
  python scripts/generate-test-jwt.py
  # or override the secret:
  JWT_SECRET=<base64-secret> python scripts/generate-test-jwt.py

The printed token can be used as:
  curl -H "Authorization: Bearer <token>" http://localhost:8080/v1/accounts
"""
import base64
import datetime
import os
import sys

try:
    import jwt
except ImportError:
    print("pyjwt not found. Install it with:  pip install pyjwt", file=sys.stderr)
    sys.exit(1)

DEFAULT_SECRET = "bXNhLXBvYy1zZWNyZXQta2V5LWF0LWxlYXN0LTI1Ni1iaXRz"
secret_b64 = os.environ.get("JWT_SECRET", DEFAULT_SECRET)
secret_bytes = base64.b64decode(secret_b64)

payload = {
    "sub": "dev-user",
    "exp": datetime.datetime.now(datetime.timezone.utc) + datetime.timedelta(hours=8),
}

token = jwt.encode(payload, secret_bytes, algorithm="HS256")
print(token)
