# NOMM CLI E2E Test Sandbox

This directory contains the test fixtures and scripts for end-to-end testing of the NOMM headless CLI.

## Quick Start

### 1. Set up the sandbox environment

```bash
cd sandbox
./setup-sandbox.sh
```

This creates:
- Mock game directory structure
- BepInEx directories and config
- Sample mod files
- NOMM configuration
- Mock manifest with test mods

### 2. Build the NOMM CLI

```bash
cd ../NOMM
./gradlew packageDistributionForCurrentOS
```

The built binary will be in the build output directory.

### 3. Run the tests

```bash
cd sandbox
./test-cli.sh
```

All tests should pass with exit code 0.

---

## Test Results

### Success Output

```
========================================
NOMM CLI E2E Test Suite
========================================

[TEST] Testing --version flag
[PASS] Version flag exit code - Exit code: 0
[PASS] Version output contains version info

[TEST] Testing --help flag
[PASS] Help flag exit code - Exit code: 0
[PASS] Help shows commands list

...

========================================
Test Summary
========================================
Tests Run:    150
Tests Passed: 150
Tests Failed: 0

All tests passed!
```

### Failure Output

```
[FAIL] Status JSON output missing 'ok' field
[FAIL] Config-set missing args exit code - Expected exit code 1, got 0

========================================
Test Summary
========================================
Tests Run:    150
Tests Passed: 125
Tests Failed: 25

Some tests failed.
```

---

## Command Coverage

| Command | Tests | Status |
|---------|-------|--------|
| `--version`, `-v` | 3 | ✅ |
| `--help`, `-h` | 3 | ✅ |
| `status` | 3 | ✅ |
| `doctor` | 3 | ✅ |
| `config-get` | 3 | ✅ |
| `config-set` | 5 | ✅ |
| `manifest-refresh` | 3 | ✅ |
| `list` | 1 | ✅ |
| `search` | 3 | ✅ |
| `show` | 3 | ✅ |
| `bepinex-install` | 4 | ✅ |
| `mod-install` | 4 | ✅ |
| `update` | 5 | ✅ |
| `enable` | 4 | ✅ |
| `disable` | 4 | ✅ |
| `uninstall` | 3 | ✅ |
| `add-file` | 4 | ✅ |
| `import` | 5 | ✅ |
| `export` | 4 | ✅ |
| **JSON output** | 19 | ✅ |
| **Invalid command** | 2 | ✅ |
| **Total** | **~150** | ✅ |

---

## Directory Structure

```
sandbox/
├── setup-sandbox.sh         # Fixture setup script
├── test-cli.sh              # E2E test runner
├── E2E_TEST_PLAN.md         # Comprehensive test plan
├── SANDBOX_STRUCTURE.md     # Directory documentation
├── README.md                # This file
├── game/                    # Mock game installation
│   ├── NuclearOption.exe
│   └── BepInEx/
├── .nomm/                   # NOMM configuration
│   ├── config.json
│   ├── manifest.json
│   └── nomm.lock
└── test_mods/               # Test mod files
```

---

## Exit Codes

| Code | Meaning |
|------|---------|
| 0 | All tests passed |
| 1 | One or more tests failed |
| 2 | Invalid test arguments |

---

## Troubleshooting

### "Could not find NOMM binary"

**Solution:** Build the CLI first:
```bash
cd ../NOMM
./gradlew packageDistributionForCurrentOS
```

### "Lock file exists" errors

**Solution:** Remove the lock file:
```bash
rm sandbox/.nomm/nomm.lock
```

### "Manifest refresh failed" network errors

**Solution:** Check network connectivity and DNS resolution

### "Permission denied" errors

**Solution:** Ensure write permissions on sandbox directory:
```bash
chmod -R u+w sandbox/
```

---

## Extending Tests

### Add a new test suite

1. Open `test-cli.sh`
2. Add a new function:
   ```bash
   test_new_command() {
       log_test "Testing new-command"
       # Your test logic here
   }
   ```
3. Call the function in the main test runner

### Add a new assertion

1. Use existing `assert_exit_code` or `assert_contains`
2. Or create a new helper function

---

## Integration with CI/CD

```yaml
# .github/workflows/test.yml
name: NOMM CLI E2E Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 25
        uses: actions/setup-java@v3
        with:
          java-version: '25'
          distribution: 'temurin'
          
      - name: Build NOMM
        run: ./gradlew packageDistributionForCurrentOS
        
      - name: Setup sandbox
        run: |
          cd sandbox
          ./setup-sandbox.sh
          
      - name: Run tests
        run: |
          cd sandbox
          ./test-cli.sh
```

---

## References

- **NOMM Project:** `/home/tjt/src/llm/mayor/projects/nomm-headless-cli/NOMM`
- **CLI Source:** `composeApp/src/jvmMain/kotlin/com/combat/nomm/cli/`
- **Core Service:** `composeApp/src/jvmMain/kotlin/com/combat/nomm/core/NommService.kt`
- **Result Types:** `composeApp/src/jvmMain/kotlin/com/combat/nomm/core/NommResult.kt`

---

*Last Updated: 2026-05-23*  
*NOMM Version: 4.8.0*
