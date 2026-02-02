# DeathChest

AllayMC 的死亡宝箱系统，当玩家死亡时自动存储物品，防止物品丢失。

## 功能特性

- **自动物品存储**: 玩家死亡时，所有背包物品都会存储到虚拟死亡宝箱中
- **宝箱找回**: 玩家可以使用 `/deathchest` 命令找回物品
- **过期系统**: 死亡宝箱24小时后过期
- **跨维度支持**: 适用于主世界、下界和末地
- **持久化存储**: 所有宝箱都保存到JSON文件

## 命令

| 命令 | 描述 |
|---------|-------------|
| `/deathchest` 或 `/deathchest list` | 列出你所有活跃的死亡宝箱 |
| `/deathchest recover <id>` | 从指定的死亡宝箱中找回物品 |
| `/deathchest help` | 显示命令帮助 |

## 安装

1. 从发布页面下载最新的 `DeathChest-0.1.0-shaded.jar`
2. 将 JAR 文件放入服务器的 `plugins/` 目录
3. 重启服务器
4. 插件会在 `plugins/DeathChest/` 目录下创建 `chests/` 文件夹

## 从源码构建

```bash
./gradlew shadowJar
```

编译后的 JAR 文件位于 `build/libs/DeathChest-0.1.0-shaded.jar`

## 系统要求

- AllayMC 服务器 API 0.24.0 或更高版本
- Java 21 或更高版本

## 许可证

本项目采用 MIT 许可证 - 详情请查看 [LICENSE](LICENSE) 文件

## 作者

- **atri-0110** - [GitHub](https://github.com/atri-0110)

## 支持

如需支持，请在 [GitHub](https://github.com/atri-0110/DeathChest/issues) 上提交 issue
