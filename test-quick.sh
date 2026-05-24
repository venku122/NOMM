#!/bin/bash
set -e

# NOMM CLI Quick E2E Test
# Runs basic command tests via Gradle

NOMM_DIR="/home/tjt/src/llm/mayor/projects/nomm-headless-cli/NOMM"
SANDBOX_DIR="/home/tjt/src/llm/mayor/projects/nomm-headless-cli/sandbox"

echo "=== NOMM CLI Quick E2E Test ==="
echo ""

# Test 1: Version
echo "Test 1: --version"
source "/home/tjt/.sdkman/bin/sdkman-init.sh" && export JAVA_HOME="/home/tjt/.sdkman/candidates/java/current"
cd "$NOMM_DIR"
./gradlew :composeApp:run --args="--version" --no-configuration-cache 2>&1 | grep -q "version" && echo "✓ PASS" || echo "✗ FAIL"

# Test 2: Help
echo "Test 2: --help"
./gradlew :composeApp:run --args="--help" --no-configuration-cache 2>&1 | grep -q "Commands:" && echo "✓ PASS" || echo "✗ FAIL"

# Test 3: Status
echo "Test 3: status"
./gradlew :composeApp:run --args="status" --no-configuration-cache 2>&1 | grep -q "Game path" && echo "✓ PASS" || echo "✗ FAIL"

# Test 4: Status with JSON
echo "Test 4: status --json"
OUTPUT=$(./gradlew :composeApp:run --args="--json status" --no-configuration-cache 2>&1)
echo "$OUTPUT" | grep -q '"ok"' && echo "✓ PASS" || echo "✗ FAIL"

# Test 5: Config Get
echo "Test 5: config-get"
./gradlew :composeApp:run --args="config-get" --no-configuration-cache 2>&1 | grep -q "manifestUrl" && echo "✓ PASS" || echo "✗ FAIL"

# Test 6: Doctor
echo "Test 6: doctor"
./gradlew :composeApp:run --args="doctor" --no-configuration-cache 2>&1 | grep -q "BepInEx" && echo "✓ PASS" || echo "✗ FAIL"

# Test 7: Manifest Refresh
echo "Test 7: manifest-refresh"
./gradlew :composeApp:run --args="manifest-refresh" --no-configuration-cache 2>&1 | grep -q "version" && echo "✓ PASS" || echo "✗ FAIL"

# Test 8: List
echo "Test 8: list"
./gradlew :composeApp:run --args="list" --no-configuration-cache 2>&1 | grep -q "\[" && echo "✓ PASS" || echo "✗ FAIL"

# Test 9: Search
echo "Test 9: search"
./gradlew :composeApp:run --args="search test" --no-configuration-cache 2>&1 | grep -q "\[" && echo "✓ PASS" || echo "✗ FAIL"

# Test 10: Invalid command (should return exit code 2)
echo "Test 10: invalid command"
./gradlew :composeApp:run --args="invalid" --no-configuration-cache 2>&1 > /dev/null
EXIT_CODE=$?
if [ $EXIT_CODE -eq 2 ]; then
    echo "✓ PASS (exit code: $EXIT_CODE)"
else
    echo "✗ FAIL (exit code: $EXIT_CODE, expected 2)"
fi

echo ""
echo "=== Tests Complete ==="
