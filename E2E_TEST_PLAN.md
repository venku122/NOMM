# NOMM Headless CLI - E2E Test Plan

## Overview

This document describes the comprehensive end-to-end test plan for the NOMM (Nuclear Option Mod Manager) headless CLI.

**Project:** `/home/tjt/src/llm/mayor/projects/nomm-headless-cli/NOMM`  
**Test Sandbox:** `/home/tjt/src/llm/mayor/projects/nomm-headless-cli/sandbox/`  
**Test Script:** `sandbox/test-cli.sh`

---

## Test Fixture Setup

The sandbox environment creates a minimal but functional setup for testing:

```
sandbox/
├── game/                              # Mock game installation
│   ├── NuclearOption.exe             # Placeholder executable
│   └── BepInEx/                      # BepInEx mod loader
│       ├── plugins/                  # Active plugins directory
│       ├── disabledPlugins/          # Disabled plugins directory
│       └── config.cfg                # BepInEx configuration
├── .nomm/                            # NOMM configuration
│   ├── config.json                   # User settings (gamePath, manifest URL, etc.)
│   ├── manifest.json                 # Mock manifest with test mods
│   └── nomm.lock                     # Lock file for CLI instance
├── test_mods/                        # Test mod files
│   ├── sample_mod.dll                # Sample DLL for testing add-file
│   ├── sample_modpack.nomm.json      # Sample modpack for import/export
│   └── export_test.nomm.json         # Generated during export test
└── test_cli.sh                       # E2E test runner
```

### Key Configuration

The sandbox config (`sandbox/.nomm/config.json`) includes:
- `gamePath`: Points to the mock game directory
- `manifestUrl`: Uses the official NOMNOM manifest repository
- `fakeManifest`: Set to `false` to use real manifest data
- All other theme and UI settings (irrelevant for CLI)

The mock manifest (`sandbox/.nomm/manifest.json`) contains:
- `sample.mod1`: A utility mod (version 1.0.0)
- `sample.mod2`: A feature mod (version 2.0.0) with dependency on sample.mod1

---

## Command Test Coverage

### 1. version (`--version`, `-v`)

**Purpose:** Verify CLI version information

**Tests:**
- `--version` flag returns version number
- `-v` short flag returns version number
- JSON output contains version field
- Exit code: 0 on success

**Normal Output:** `NOMM version 4.8.0`  
**JSON Output:** `{"ok": true, "command": "version", "data": {"version": "4.8.0"}}`

---

### 2. help (`--help`, `-h`)

**Purpose:** Display command usage information

**Tests:**
- `--help` shows usage documentation
- `-h` short flag shows usage
- Output contains all available commands
- Exit code: 0 on success

---

### 3. status

**Purpose:** Show overall installation status

**Tests:**
- Normal output shows game path, BepInEx status, mod counts
- JSON output contains structured status result
- Exit code: 0 on success, 1 on error

**Fields verified:**
- `gamePath`: Configured game path
- `gamePathExists`: Game directory exists
- `gameExeFound`: NuclearOption.exe present
- `bepInExInstalled`: BepInEx directory exists
- `pluginsDirExists`: Plugins directory exists
- `disabledPluginsDirExists`: Disabled plugins directory exists
- `manifestLoaded`: Manifest successfully loaded
- `manifestModCount`: Number of mods in manifest
- `installedModCount`: Number of installed mods
- `enabledModCount`: Number of enabled mods
- `disabledModCount`: Number of disabled mods
- `version`: NOMM version

---

### 4. doctor

**Purpose:** Run diagnostic checks on the installation

**Tests:**
- Normal output shows all diagnostic results
- JSON output contains structured doctor result
- Exit code: 0 on success, 1 if issues found

**Fields verified:**
- `gamePath`: Configured game path
- `gamePathExists`: Game directory exists
- `executableFound`: NuclearOption.exe present
- `bepInExExists`: BepInEx directory exists
- `pluginsDirExists`: Plugins directory exists
- `disabledPluginsDirExists`: Disabled plugins directory exists
- `manifestUrl`: Current manifest URL
- `manifestReachable`: Can reach manifest (network dependent)
- `installedModCount`: Number of installed mods
- `enabledModCount`: Number of enabled mods
- `disabledModCount`: Number of disabled mods
- `issues`: List of any detected problems
- `version`: NOMM version

---

### 5. config-get

**Purpose:** Display all configuration values

**Tests:**
- Normal output shows all config key-value pairs
- JSON output contains structured config data
- Exit code: 0 on success

**Fields verified:**
- `theme`: Theme selection (dark/light/auto)
- `gamePath`: Current game path
- `paletteStyle`: Color palette style
- `contrast`: Contrast level
- `fakeManifest`: Use fake manifest flag
- `manifestUrl`: Current manifest URL
- `manifestVersionUrl`: Manifest version URL
- `ignoreManifestVersion`: Ignore version mismatch flag
- `ignoreHashMismatch`: Ignore hash mismatch flag
- `hueValue`: Custom hue value
- `placement`: Window placement settings

---

### 6. config-set

**Purpose:** Set a configuration value

**Tests:**

**Normal Cases:**
- Set valid config key with valid value
- Verify value persists (config-get confirms)
- Exit code: 0 on success

**Invalid Cases:**
- Set with missing arguments (only key, no value)
- Set with unknown config key
- Exit code: 1 for errors, 2 for invalid args

**Valid Keys Tested:**
- `gamePath`: New game directory path
- `manifestUrl`: New manifest URL
- `manifestVersionUrl`: New version URL
- `ignoreHashMismatch`: true/false
- `ignoreManifestVersion`: true/false
- `fakeManifest`: true/false

---

### 7. manifest-refresh

**Purpose:** Refresh the NOMNOM mod manifest

**Tests:**
- Normal output shows version and mod count
- JSON output contains structured manifest refresh result
- Exit code: 0 on success

**Fields verified:**
- `version`: Manifest version
- `modCount`: Number of mods in manifest
- `cached`: Whether manifest was cached

---

### 8. list

**Purpose:** List all installed mods

**Tests:**
- Normal output shows JSON for each mod
- JSON output is array of mod objects
- Exit code: 0 on success

**Each mod includes:**
- `id`: Mod identifier
- `name`: Mod file name
- `version`: Installed version
- `enabled`: Enabled status (true/false/null)
- `hasUpdate`: Update available flag
- `isUnidentified`: Unknown mod flag
- `problems`: List of any issues
- `isFromRepo`: From official repo flag

---

### 9. search

**Purpose:** Search mods in the manifest

**Tests:**

**Normal Cases:**
- Search with valid query
- JSON output contains array of matching mods
- Exit code: 0 on success

**Invalid Cases:**
- Search without query argument
- Exit code: 2 for invalid args

**Search matches:**
- Mod ID contains query
- Display name contains query
- Description contains query
- Author name contains query

---

### 10. show

**Purpose:** Show detailed information for a specific mod

**Tests:**

**Normal Cases:**
- Show valid mod ID
- JSON output contains full mod details
- Exit code: 0 on success

**Invalid Cases:**
- Show without mod ID
- Show with non-existent mod ID
- Exit code: 2 for invalid args, 1 for not found

**Fields verified:**
- `id`: Mod identifier
- `displayName`: Human-readable name
- `description`: Mod description
- `tags`: List of tags
- `authors`: List of authors
- `downloadCount`: Download statistics
- `latestVersion`: Latest version available
- `versions`: All available versions
- `dependencies`: List of dependencies
- `extends`: Extension (if any)
- `incompatibilities`: List of incompatible mods
- `installed`: Whether installed
- `enabled`: Enabled status
- `installedVersion`: Currently installed version

---

### 11. bepinex-install

**Purpose:** Install BepInEx into the game folder

**Tests:**

**Normal Cases:**
- Install BepInEx when not present
- Verify BepInEx directory structure created
- Exit code: 0 on success

**Edge Cases:**
- Install when BepInEx already exists
- Exit code: 0 or 1 (already installed)

**Directory structure verified:**
- `game/BepInEx/` - Main directory
- `game/BepInEx/plugins/` - Plugins folder
- `game/BepInEx/disabledPlugins/` - Disabled plugins folder
- `game/BepInEx/config.cfg` - Configuration file

---

### 12. mod-install

**Purpose:** Install a mod from the repository

**Tests:**

**Normal Cases:**
- Install a mod that doesn't exist
- Verify mod DLL placed in plugins directory
- Exit code: 0 on success

**Edge Cases:**
- Install mod that's already installed
- Install without mod ID
- Exit code: 0 for already installed, 2 for invalid args

---

### 13. update

**Purpose:** Update a mod or all mods

**Tests:**

**Normal Cases:**
- Update single mod by ID
- Update all mods with `--all` flag
- Exit code: 0 on success

**Invalid Cases:**
- Update without arguments
- Exit code: 2 for invalid args

---

### 14. enable

**Purpose:** Enable a mod

**Tests:**

**Normal Cases:**
- Enable a disabled mod
- Verify mod moves from disabled to active
- Exit code: 0 on success

**Edge Cases:**
- Enable an already enabled mod
- Enable non-existent mod
- Exit code: 0 for already enabled, 1 for not found

---

### 15. disable

**Purpose:** Disable a mod

**Tests:**

**Normal Cases:**
- Disable an enabled mod
- Verify mod moves from active to disabled
- Exit code: 0 on success

**Edge Cases:**
- Disable an already disabled mod
- Disable non-existent mod
- Exit code: 0 for already disabled, 1 for not found

---

### 16. uninstall

**Purpose:** Uninstall a mod

**Tests:**

**Normal Cases:**
- Uninstall a mod
- Verify mod DLL removed from plugins directory
- Exit code: 0 on success

**Invalid Cases:**
- Uninstall without mod ID
- Uninstall non-existent mod
- Exit code: 2 for invalid args, 1 for not found

---

### 17. add-file

**Purpose:** Add a local mod file to the plugins directory

**Tests:**

**Normal Cases:**
- Add existing DLL file (copy mode)
- Add existing DLL file with move flag
- Verify file appears in plugins directory
- Exit code: 0 on success

**Invalid Cases:**
- Add without file path
- Add non-existent file
- Exit code: 2 for invalid args, 1 for file not found

**Fields verified:**
- `fileName`: Name of the file
- `destination`: Destination path
- `action`: "copied" or "moved"

---

### 18. import

**Purpose:** Import a modpack from a .nomm.json file

**Tests:**

**Normal Cases:**
- Import a valid modpack file
- Verify mods installed
- Verify mod states match imported state
- Exit code: 0 on success

**Invalid Cases:**
- Import without file path
- Import non-existent file
- Import invalid JSON file
- Exit code: 2 for invalid args, 1 for file errors

**Fields verified:**
- `succeeded`: List of successfully installed mod IDs
- `failed`: List of failures with error messages

---

### 19. export

**Purpose:** Export currently enabled mods to a .nomm.json file

**Tests:**

**Normal Cases:**
- Export enabled mods to valid path
- Verify file created
- Verify file is valid JSON
- Verify only enabled mods are exported
- Exit code: 0 on success

**Invalid Cases:**
- Export without file path
- Export to invalid path (permission denied, etc.)
- Exit code: 2 for invalid args, 1 for write errors

---

## JSON Output Format

All commands support the `--json` flag. Output format:

```json
{
  "ok": boolean,
  "command": string,
  "data": any,
  "warnings": string[],
  "error": {
    "code": string,
    "message": string,
    "details": object
  }
}
```

- `ok`: true if command succeeded, false otherwise
- `command`: The command that was executed
- `data`: Command-specific data (varies by command)
- `warnings`: Optional list of warning messages
- `error`: Populated when `ok` is false

---

## Exit Codes

| Code | Meaning |
|------|---------|
| 0 | Success |
| 1 | Error (command failed, missing file, network error, etc.) |
| 2 | Invalid arguments (missing required args, unknown command) |

---

## Integration Workflows

### Workflow 1: New Game Setup

1. Set game path: `config-set gamePath /path/to/game`
2. Verify status: `status`
3. Install BepInEx: `bepinex-install`
4. Verify doctor: `doctor`
5. Refresh manifest: `manifest-refresh`
6. Search for mods: `search "quality of life"`
7. Install mods: `mod-install "mod.id"`
8. Enable mods: `enable "mod.id"`
9. Export config: `export /backup/nomm_mods.json`

**Expected:** All commands succeed, mods installed and enabled

---

### Workflow 2: Mod Management Cycle

1. List current mods: `list`
2. Search for updates: `search "mod-name"`
3. Update mod: `update "mod.id"`
4. Disable mod: `disable "mod.id"`
5. Re-enable mod: `enable "mod.id"`
6. Uninstall mod: `uninstall "mod.id"`

**Expected:** All state changes succeed

---

### Workflow 3: Backup and Restore

1. Export enabled mods: `export /backup/current_mods.json`
2. Import backup: `import /backup/current_mods.json`

**Expected:** Mods and states match the export

---

## Edge Case Testing

### 1. Empty Game Directory
- No BepInEx installed
- No mods installed
- Verify doctor reports issues

### 2. Partial Installation
- Game path set but game not present
- BepInEx exists but plugins missing
- Verify doctor catches all issues

### 3. Network Issues
- Invalid manifest URL
- Unreachable manifest
- Verify error messages are helpful

### 4. Permission Errors
- Read-only game directory
- Write-protected plugins folder
- Verify error messages indicate permission issues

### 5. Invalid Data
- Corrupt manifest file
- Invalid JSON in config
- Verify error messages are clear

### 6. Concurrent Access
- Multiple NOMM instances
- Verify lock file prevents conflicts

---

## Test Execution

### Running All Tests

```bash
cd /home/tjt/src/llm/mayor/projects/nomm-headless-cli/sandbox
./test-cli.sh all
```

### Running Specific Tests

```bash
./test-cli.sh status      # Run only status tests
```

### Verbose Output

Tests automatically colorize output:
- **BLUE** `[INFO]` - Informational messages
- **GREEN** `[PASS]` - Passed tests
- **RED** `[FAIL]` - Failed tests
- **YELLOW** `[TEST]` - Test start

---

## Prerequisites

Before running tests:

1. **Build NOMM CLI:**
   ```bash
   cd /home/tjt/src/llm/mayor/projects/nomm-headless-cli/NOMM
   ./gradlew packageDistributionForCurrentOS
   ```

2. **Set up sandbox:**
   ```bash
   cd /home/tjt/src/llm/mayor/projects/nomm-headless-cli/sandbox
   ./setup-sandbox.sh
   ```

3. **Ensure dependencies:**
   - Java 25+ (for JVM target)
   - Bash 4.4+
   - Python 3 (for JSON validation)

---

## Expected Results

All tests should pass with:
- **Exit code:** 0
- **Tests passed:** All test cases
- **No failures:** Zero test failures

A summary is displayed at the end:
```
========================================
Test Summary
========================================
Tests Run:    150
Tests Passed: 150
Tests Failed: 0

All tests passed!
```

---

## Troubleshooting

### Binary Not Found
```
Run: ./gradlew packageDistributionForCurrentOS
```

### Lock File Issues
```
Remove: sandbox/.nomm/nomm.lock
```

### Manifest Refresh Failures
```
Check network connectivity
Verify manifest URL in config
```

### Permission Errors
```
Ensure write permissions on sandbox/game/
Check user permissions
```

---

## Future Enhancements

1. **Performance Tests:** Measure command execution time
2. **Stress Tests:** Large mod counts (100+)
3. **Parallel Tests:** Concurrent command execution
4. **Mock Server:** Simulate manifest server failures
5. **Coverage Reports:** Code coverage for CLI paths
6. **Snapshot Tests:** Compare output to known good snapshots

---

*Test Plan Version: 1.0*  
*Last Updated: 2026-05-23*  
*NOMM Version: 4.8.0*
