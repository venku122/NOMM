# NOMM Headless CLI - E2E QA Plan

## Prerequisites
- [ ] NOMM desktop app installed and working (GUI verified)
- [ ] Java 25+ runtime available
- [ ] Build succeeds: `./gradlew :composeApp:compileKotlinJvm`

## Test Strategy

### 1. Build Verification
```bash
# Verify build
./gradlew :composeApp:compileKotlinJvm --no-configuration-cache

# Verify CLI classes compiled
find composeApp/build/classes -name "*Cli*.class" 2>/dev/null
```

### 2. Help & Version
```bash
# Version flag
./gradlew :composeApp:run --args="--version"

# Help flag
./gradlew :composeApp:run --args="--help"

# No args
./gradlew :composeApp:run --args=""
```

### 3. Command Tests

For each command, test:
- Basic invocation
- `--json` flag
- Invalid args (missing required params)
- Missing game path scenario
- Missing BepInEx scenario

#### status
```bash
# Normal
./gradlew :composeApp:run --args="status"

# JSON
./gradlew :composeApp:run --args="--json status"

# Verify output contains:
# - Game path (or "Not set")
# - BepInEx installed flag
# - Mod counts
```

#### doctor
```bash
# Normal
./gradlew :composeApp:run --args="doctor"

# JSON
./gradlew :composeApp:run --args="--json doctor"

# Verify issues list populated when problems exist
```

#### config-get
```bash
# Normal
./gradlew :composeApp:run --args="config-get"

# JSON
./gradlew :composeApp:run --args="--json config-get"

# Verify all config keys present
```

#### config-set
```bash
# Missing args
./gradlew :composeApp:run --args="config-set"
# Exit code: 2

# Valid
./gradlew :composeApp:run --args="config-set gamePath /test/path"

# Verify via config-get
./gradlew :composeApp:run --args="config-get"
```

#### manifest-refresh
```bash
# Normal
./gradlew :composeApp:run --args="manifest-refresh"

# JSON
./gradlew :composeApp:run --args="--json manifest-refresh"

# Verify mod count increases after refresh
```

#### list
```bash
# Normal (with mods)
./gradlew :composeApp:run --args="list"

# JSON (with mods)
./gradlew :composeApp:run --args="--json list"

# Verify mod IDs and names
```

#### search
```bash
# Missing query
./gradlew :composeApp:run --args="search"
# Exit code: 2

# With query
./gradlew :composeApp:run --args="search bepinex"

# JSON
./gradlew :composeApp:run --args="--json search bepinex"
```

#### show
```bash
# Missing mod-id
./gradlew :composeApp:run --args="show"
# Exit code: 2

# Valid mod-id
./gradlew :composeApp:run --args="show <valid-mod-id>"

# Invalid mod-id
./gradlew :composeApp:run --args="show nonexistent-123"
# Verify returns null/no error
```

#### bepinex-install
```bash
# Normal
./gradlew :composeApp:run --args="bepinex-install"

# Verify BepInEx directory created in game folder
```

#### mod-install
```bash
# Missing mod-id
./gradlew :composeApp:run --args="mod-install"
# Exit code: 2

# Valid
./gradlew :composeApp:run --args="mod-install <valid-mod-id>"

# Verify mod installed via list
```

#### update
```bash
# Missing args
./gradlew :composeApp:run --args="update"
# Exit code: 2

# Single mod
./gradlew :composeApp:run --args="update <mod-id>"

# All mods
./gradlew :composeApp:run --args="update --all"

# JSON
./gradlew :composeApp:run --args="--json update <mod-id>"
```

#### enable/disable
```bash
# Missing mod-id
./gradlew :composeApp:run --args="enable"
# Exit code: 2

# Enable
./gradlew :composeApp:run --args="enable <mod-id>"

# Disable
./gradlew :composeApp:run --args="disable <mod-id>"
```

#### uninstall
```bash
# Missing mod-id
./gradlew :composeApp:run --args="uninstall"
# Exit code: 2

# Valid
./gradlew :composeApp:run --args="uninstall <mod-id>"

# Verify removed via list
```

#### add-file
```bash
# Missing path
./gradlew :composeApp:run --args="add-file"
# Exit code: 2

# Valid mod file
./gradlew :composeApp:run --args="add-file /path/to/mod.dll"

# Verify via list
```

#### import/export
```bash
# Export
./gradlew :composeApp:run --args="export /tmp/nomm-export.json"

# Verify JSON file created
cat /tmp/nomm-export.json

# Import
./gradlew :composeApp:run --args="import /tmp/nomm-export.json"
```

### 4. Error Handling Tests

```bash
# Test exit codes
./gradlew :composeApp:run --args="status"; echo "Exit: $?"

# Verify exit code is 0 on success

# Test invalid command
./gradlew :composeApp:run --args="invalid-command"; echo "Exit: $?"
# Should be 2

# Test missing required args
./gradlew :composeApp:run --args="search"; echo "Exit: $?"
# Should be 2
```

### 5. JSON Output Validation

```bash
# For each command with JSON, validate JSON structure
OUTPUT=$(./gradlew :composeApp:run --args="--json status")
echo "$OUTPUT" | jq -e '.ok' > /dev/null
echo "$OUTPUT" | jq -e '.command' > /dev/null
echo "$OUTPUT" | jq -e '.data' > /dev/null

# Test error JSON
./gradlew :composeApp:run --args="invalid" --json
# Should have .ok=false and .error object
```

### 6. File System Lock Tests

```bash
# Test concurrent access (should fail gracefully)
./gradlew :composeApp:run --args="mod-install test-mod" &
./gradlew :composeApp:run --args="mod-install another-mod" &
wait

# Verify one succeeded, one failed with lock error
```

### 7. Integration Tests

```bash
# Full workflow test
1. config-set gamePath <game-path>
2. bepinex-install
3. manifest-refresh
4. search bepinex
5. mod-install <mod-id>
6. list
7. status
8. doctor

# Verify all steps completed with exit code 0
```

## Automated Test Script

```bash
#!/bin/bash
set -e

echo "Running NOMM CLI E2E Tests..."

# Test each command and capture exit codes
TESTS_PASSED=0
TESTS_FAILED=0

run_test() {
    local name=$1
    local args=$2
    echo "Testing: $name"
    if ./gradlew :composeApp:run --args="$args" > /dev/null 2>&1; then
        echo "  ✓ PASS"
        ((TESTS_PASSED++))
    else
        echo "  ✗ FAIL"
        ((TESTS_FAILED++))
    fi
}

run_test "Version" "--version"
run_test "Help" "--help"
run_test "Status" "status"
run_test "Status JSON" "--json status"
run_test "Doctor" "doctor"
run_test "Config Get" "config-get"
run_test "Manifest Refresh" "manifest-refresh"

echo ""
echo "=== Results ==="
echo "Passed: $TESTS_PASSED"
echo "Failed: $TESTS_FAILED"

if [ $TESTS_FAILED -gt 0 ]; then
    exit 1
fi
```

## Known Limitations

1. **No progress reporting** - Long operations don't show progress
2. **Sequential only** - No parallel operations
3. **Basic error messages** - Errors show exception message only
4. **No interactive mode** - All arguments must be provided upfront

## Sign-Off Criteria

- [ ] All 17 commands work without GUI
- [ ] JSON output valid and parseable
- [ ] Exit codes match specification
- [ ] Error cases handled gracefully
- [ ] File locking prevents concurrent corruption
- [ ] Build succeeds on CI
