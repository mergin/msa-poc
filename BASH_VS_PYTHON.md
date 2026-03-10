# Bash vs Python for Scripting in This Project

This document explains the trade-offs between **Bash** and **Python** for scripting tasks relevant to this Spring Boot microservices project.

---

## Overview

Both Bash and Python can automate operational tasks such as starting services, running smoke checks, seeding databases, and orchestrating CI steps. The right choice depends on the task's complexity, portability requirements, and team familiarity.

---

## Bash

### Strengths

- **Zero dependencies** – available by default on virtually all Linux/macOS environments and in most CI runners (including GitHub Actions `ubuntu-latest`).
- **Best for shell-native tasks** – launching processes, piping commands, redirecting output, and chaining CLI tools (`mvn`, `docker`, `curl`) is natural and concise.
- **Low overhead** – no interpreter startup cost beyond the shell itself.
- **Inline in CI YAML** – `run:` blocks in GitHub Actions workflows are executed directly by Bash; no extra setup step is needed.

### Weaknesses

- **Limited data manipulation** – parsing JSON, handling complex logic, or working with structured data is awkward without additional tools (`jq`, `awk`, `sed`).
- **Error handling is verbose** – robust error handling requires `set -euo pipefail` and explicit checks.
- **Portability pitfalls** – scripts written for GNU Bash can break on macOS's older `/bin/sh` or in Alpine-based containers.
- **Readability degrades quickly** – multi-step workflows with conditional logic become hard to maintain.

### When to use Bash in this project

- Starting or stopping services in local development (e.g., the three-terminal startup flow in the README).
- One-liner smoke checks (`curl` calls).
- Simple CI steps that call Maven or Docker Compose.
- Git hooks or pre-commit scripts.

**Example — start all services:**

```bash
#!/usr/bin/env bash
set -euo pipefail

docker compose up -d

mvn -pl customers-service spring-boot:run &
mvn -pl accounts-service spring-boot:run &
mvn -pl gateway-service spring-boot:run &

wait
```

---

## Python

### Strengths

- **Rich standard library** – HTTP requests (`urllib`/`requests`), JSON parsing, file I/O, and regex are all first-class without external tools.
- **Readability and maintainability** – complex conditional logic, loops, and data structures are cleaner than equivalent Bash.
- **Cross-platform** – the same script runs on Linux, macOS, and Windows (WSL or native).
- **Testing** – Python scripts can be unit-tested with `pytest`; Bash scripts are much harder to test.
- **Better error handling** – exceptions, try/except, and structured logging make failures easier to diagnose.

### Weaknesses

- **Requires a Python interpreter** – must be installed separately in environments that do not include it (though it is present in most CI images).
- **Heavier for trivial tasks** – importing modules and handling subprocess calls adds boilerplate compared to a one-liner shell command.
- **Dependency management** – third-party packages (`requests`, etc.) require a `requirements.txt` or virtual environment.

### When to use Python in this project

- Smoke-test scripts that parse JSON API responses and assert correctness.
- Database seed scripts or data-generation utilities.
- More complex CI automation (e.g., checking all 100 customer records are paginated correctly).
- Any script that needs to be maintained, extended, or tested over time.

**Example — smoke-test script that validates the customers API:**

```python
#!/usr/bin/env python3
"""
Smoke test: verify the /v1/customers endpoint returns expected data.
Usage: python3 smoke_test.py
"""
import sys
import urllib.request
import json

BASE_URL = "http://localhost:8080"


def get_json(path: str) -> dict | list:
    url = BASE_URL + path
    with urllib.request.urlopen(url, timeout=5) as resp:
        return json.loads(resp.read())


def main() -> None:
    # List customers (first page)
    customers = get_json("/v1/customers?page=0&size=10")
    assert isinstance(customers, list), "Expected a list of customers"
    assert len(customers) == 10, f"Expected 10 customers, got {len(customers)}"

    # Fetch a single customer
    first_id = customers[0]["id"]
    customer = get_json(f"/v1/customers/{first_id}")
    assert customer["id"] == first_id, "Customer id mismatch"

    print("✅ Smoke tests passed")


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print(f"❌ Smoke test failed: {exc}", file=sys.stderr)
        sys.exit(1)
```

---

## Decision guide

| Criterion | Bash | Python |
|---|---|---|
| Launching processes / piping commands | ✅ Preferred | ⚠️ Works but verbose |
| JSON / structured data handling | ⚠️ Needs `jq` | ✅ Preferred |
| Complex conditional logic | ⚠️ Error-prone | ✅ Preferred |
| Zero-dependency availability | ✅ | ⚠️ Usually present |
| Testability | ❌ Hard | ✅ Easy (`pytest`) |
| CI inline `run:` steps | ✅ Native | ⚠️ Needs `python3` step |
| Cross-platform (Windows) | ❌ | ✅ |
| Script length < ~20 lines | ✅ Preferred | ⚠️ Boilerplate overhead |
| Script length > ~50 lines | ⚠️ Maintain carefully | ✅ Preferred |

### Rule of thumb

> Use **Bash** for short, shell-native tasks that glue CLI tools together.  
> Use **Python** when you need data parsing, structured logic, error handling, or tests.

---

## Mixing both

It is perfectly valid — and common — to use both within the same project:

- A Bash entrypoint (`run.sh`) that starts services and then delegates to a Python smoke-test script for validation.
- GitHub Actions `run:` steps written in Bash, with a dedicated Python script invoked for assertion-heavy checks.

This project's `docker compose up -d` + `mvn` startup is a natural fit for Bash, while any automated validation of the paginated API responses is a natural fit for Python.
