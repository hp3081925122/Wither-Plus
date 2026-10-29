# 强化凋零战利品表配置

三个版本（1.20.1 Forge、1.21.1 NeoForge、26.1.2 NeoForge）统一新增 `config/witherplus-common.toml` 的 `[wither] enhancedLootTable`，默认 `minecraft:entities/wither`。填写资源 ID，不填写文件后缀。

仅增强状态的凋零使用此配置。实体通过 `DeathLootTable` 指定的表优先保留。修改配置后重启服务端；数据包内容更新后可使用 `reload`。

原版凋零的战利品表为空，下界之星由 `WitherBoss.dropCustomDeathLoot` 单独生成。此配置切换战利品表生成的物品，保留原版下界之星、经验和其他死亡逻辑。

例如填写 `example:entities/enhanced_wither`：1.20.1 对应数据包 `data/example/loot_tables/entities/enhanced_wither.json`；1.21.1 与 26.1.2 对应 `data/example/loot_table/entities/enhanced_wither.json`。缺失的资源按原版机制生成空战利品；非法资源 ID 回退原版并记录英文警告。

实现保留完整原版战利品上下文、击杀者、幸运值和全局战利品修改器。1.20.1 与 1.21.1 覆写继承的 `getDefaultLootTable`；26.1.2 已移除此方法，改为在 `Mob.getLootTable` 返回时仅对未设置独立表的强化凋零切换资源键。

验证项目：三版本构建；专用服务端默认表击杀、自定义钻石表击杀、切换另一张表击杀、普通凋零不受影响、独立 DeathLootTable 优先。
