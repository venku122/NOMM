---
name: nomm-cli
description: Manage mods for Nuclear Option (NOMM) via the headless CLI. Use when installing, updating, enabling, disabling, or uninstalling mods from the command line.
---

# NOMM Headless CLI Skill

Use this skill when you need to manage mods via the command line interface (CLI) for NOMM (Nuclear Option Mod Manager).

## Prerequisites

- NOMM must be built first:
  ```bash
  cd /home/tjt/src/llm/mayor/projects/nomm-headless-cli/NOMM
  ./gradlew :composeApp:compileKotlinJvm
  ```

- Use the wrapper script `./nomm-cli.sh` which sets up Java environment

## Usage

```bash
cd /home/tjt/src/llm/mayor/projects/nomm-headless-cli/NOMM
./nomm-cli.sh <command> [options] [args...]
```

## Commands

### Help & Version
```bash
./nomm-cli.sh --help        # Show all commands
./nomm-cli.sh --version     # Show NOMM version (4.8.0)
```

### Status & Diagnostics
```bash
./nomm-cli.sh status              # Show game path, BepInEx, mod counts
./nomm-cli.sh status --json       # JSON output
./nomm-cli.sh doctor              # Run diagnostics
```

### Configuration
```bash
./nomm-cli.sh config-get          # Show all config values
./nomm-cli.sh config-set gamePath /path/to/game
./nomm-cli.sh config-set manifestUrl https://example.com/manifest.json
```

### Manifest Management
```bash
./nomm-cli.sh manifest-refresh    # Refresh the mod manifest from remote
```

### Mod Listing & Search
```bash
./nomm-cli.sh list                # List installed mods
./nomm-cli.sh search bepinex      # Search for mods matching "bepinex"
./nomm-cli.sh show komet          # Show details for mod ID "komet"
```

### BepInEx Installation
```bash
./nomm-cli.sh bepinex-install     # Install BepInEx into game folder
```

### Mod Installation
```bash
./nomm-cli.sh mod-install komet   # Install Komet mod
./nomm-cli.sh mod-install komet 1.0.0  # Install specific version
```

### Mod Updates
```bash
./nomm-cli.sh update komet        # Update specific mod
./nomm-cli.sh update --all        # Update all installed mods
```

### Mod Enable/Disable
```bash
./nomm-cli.sh enable komet        # Enable Komet mod
./nomm-cli.sh disable komet       # Disable Komet mod
```

### Mod Uninstall
```bash
./nomm-cli.sh uninstall komet     # Uninstall Komet mod
```

### Local File Management
```bash
./nomm-cli.sh add-file ~/Downloads/mod.dll  # Add local mod file
```

### Modpack Import/Export
```bash
./nomm-cli.sh export /tmp/my-modpack.nomm.json  # Export enabled mods
./nomm-cli.sh import /tmp/my-modpack.nomm.json  # Import modpack
```

## JSON Output

Add `--json` flag to any command for structured JSON output:

```bash
./nomm-cli.sh --json status
./nomm-cli.sh --json list
./nomm-cli.sh --json search bepinex
```

Example JSON output:
```json
{
    "ok": true,
    "command": "status",
    "data": {
        "gamePath": "/path/to/Nuclear Option",
        "bepInExInstalled": true,
        "installedModCount": 5,
        "enabledModCount": 3,
        "disabledModCount": 2
    },
    "version": "4.8.0"
}
```

## Exit Codes

- `0` - Success
- `1` - General error
- `2` - Invalid arguments

## Environment

The wrapper script automatically:
- Sets up SDKMAN Java environment
- Uses Java 25+ (required for the project)

## Examples

**Setup a new game installation:**
```bash
cd /home/tjt/src/llm/mayor/projects/nomm-headless-cli/NOMM
./nomm-cli.sh config-set gamePath "/home/tjt/.local/share/Steam/steamapps/common/Nuclear Option"
./nomm-cli.sh bepinex-install
./nomm-cli.sh manifest-refresh
```

**Install and enable mods:**
```bash
./nomm-cli.sh mod-install komet
./nomm-cli.sh mod-install bepinex_expanded
./nomm-cli.sh enable komet
./nomm-cli.sh enable bepinex_expanded
```

**Export current setup:**
```bash
./nomm-cli.sh export "/home/tjt/.config/nomm-backup.nomm.json"
```

**List all mods with updates available:**
```bash
./nomm-cli.sh list --json | jq '.data[] | select(.hasUpdate == true) | .id'
```

## Known Limitations

- No progress reporting for long-running operations
- All operations are sequential (no parallel operations)
- Requires manual game path configuration first
