# PlayerHomes

A player homes plugin for AllayMC servers that allows players to set multiple homes and teleport between them.

## Features

- **Set Multiple Homes**: Players can set up to 10 homes with custom names
- **Teleport to Homes**: Quick teleportation to any saved home location
- **Cross-Dimension Support**: Homes work across different dimensions (Overworld, Nether, End)
- **Persistent Storage**: All homes are saved to JSON files and persist across server restarts
- **Home Management**: Easy listing, deletion, and updating of homes
- **Creation Timestamps**: Each home records when it was created

## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/home <name>` | `playerhomes.use` | Teleport to a home |
| `/home set <name>` | `playerhomes.use` | Set a home at current location |
| `/home delete <name>` | `playerhomes.delete` | Delete a home |
| `/home list` | `playerhomes.use` | List all your homes |
| `/home help` | `playerhomes.use` | Show help message |

## Permissions

```
playerhomes.use - Access to basic home commands (set, list, teleport)
playerhomes.delete - Permission to delete homes
```

## Installation

1. Download the latest `PlayerHomes-0.1.0-shaded.jar` from [Releases](https://github.com/atri-0110/PlayerHomes/releases)
2. Place the JAR file in your server's `plugins/` directory
3. Start or restart the server
4. The plugin will create a `homes/` folder in `plugins/PlayerHomes/`

## Building from Source

```bash
./gradlew shadowJar
```

The compiled JAR will be in `build/libs/PlayerHomes-0.1.0-shaded.jar`

## Configuration

Homes are stored in per-player JSON files at `plugins/PlayerHomes/homes/<uuid>.json`

Default settings:
- Maximum 10 homes per player
- Home names must be alphanumeric with underscores (max 16 characters)

## Requirements

- AllayMC Server with API 0.24.0 or higher
- Java 21 or higher

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

- **atri-0110** - [GitHub](https://github.com/atri-0110)

## Support

For support, please open an issue on [GitHub](https://github.com/atri-0110/PlayerHomes/issues).
