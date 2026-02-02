# DeathChest

A death chest system for AllayMC that stores player items when they die, preventing item loss.

## Features

- **Automatic Item Storage**: When a player dies, all their inventory items are stored in a virtual death chest
- **Chest Recovery**: Players can recover their items using the `/deathchest` command
- **Expiration System**: Death chests expire after 24 hours
- **Cross-Dimension Support**: Works across Overworld, Nether, and End dimensions
- **Persistent Storage**: All chests are saved to JSON files

## Commands

| Command | Description |
|---------|-------------|
| `/deathchest` or `/deathchest list` | List all your active death chests |
| `/deathchest recover <id>` | Recover items from a specific death chest |
| `/deathchest help` | Show command help |

## Installation

1. Download the latest `DeathChest-0.1.0-shaded.jar` from releases
2. Place the JAR file in your server's `plugins/` directory
3. Restart the server
4. The plugin will create a `chests/` folder in `plugins/DeathChest/`

## Building from Source

```bash
./gradlew shadowJar
```

The compiled JAR will be in `build/libs/DeathChest-0.1.0-shaded.jar`

## Requirements

- AllayMC Server with API 0.24.0 or higher
- Java 21 or higher

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

- **atri-0110** - [GitHub](https://github.com/atri-0110)

## Support

For support, please open an issue on [GitHub](https://github.com/atri-0110/DeathChest/issues).
