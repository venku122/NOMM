# NOMM - Nuclear Option Mod Manager

A Mod Manager for the Game Nuclear Option

# Features
- Automatic BepInEx installation
- Mod searching
- Mod installing and updating
- Mod conflict warnings
- Mod automatic dependency resolution
- Adding Mods from Files
- Mod toggling
- Mod Uninstalling
- Customizable Theme
- **Headless CLI** for scripting and automation


## Installation

Download the appropriate file for your platform from the [Latest Release](https://github.com/Combat787/NOMM/releases/latest).

### Windows
* **Portable:** `portable.exe`
* **Installer:** `.msi`

### Linux
* **Debian:** `.deb`
* **Fedora:** `.rpm`
* **Standalone:** `.AppImage`
* **Flatpak:** `.flatpak` *(Flatpak may or may not work)*

To work NOMM retrieves a manifest from [NOMNOM](https://github.com/KopterBuzz/NOMNOM) to get the list of mods. To add your own mods go there.

App Icon made by Shumatsu

This is a Kotlin Multiplatform project targeting Desktop (JVM).

### Build and Run Desktop (JVM) Application

To build and run the development version of the desktop app, use the run configuration from the run widget
in your IDE's toolbar or run it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:run
  ```

---

## Headless CLI

NOMM includes a headless CLI interface for scripting, automation, and troubleshooting. Pass any argument to the application to enter CLI mode instead of launching the GUI.

### Usage

```shell
# Run via Gradle (args after --)
./gradlew :composeApp:run -- --help
./gradlew :composeApp:run -- --version

# Or if using a packaged binary
nomm --help
nomm --version
```

### Commands

| Command | Description |
|---------|-------------|
| `status` | Show overall status summary |
| `doctor` | Run diagnostics on the installation |
| `config get` | Show all configuration values |
| `config set <key> <value>` | Set a configuration value |
| `manifest refresh` | Refresh the NOMNOM mod manifest |
| `list --installed` | List installed mods |
| `search <query>` | Search mods in the manifest |
| `show <mod-id>` | Show detailed mod information |
| `bepinex install` | Install BepInEx into the game folder |
| `install <mod-id>` | Install a mod (add `--version <ver>`, `--no-enable`) |
| `update <mod-id>` | Update a specific mod |
| `update --all` | Update all installed mods |
| `enable <mod-id>` | Enable a mod |
| `disable <mod-id>` | Disable a mod |
| `uninstall <mod-id>` | Uninstall a mod |
| `add-file <path>` | Add a local mod file (add `--move` to move instead of copy) |
| `import <path>` | Import modpack from `.nomm.json` |
| `export <path>` | Export enabled mods to `.nomm.json` |

---



Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
