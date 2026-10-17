# Wither Plus 发布记录

更新日期：2026-10-17。

## CurseForge

- 项目 ID：1749910。
- CF 展示名：Wither Plus: Soul, Bone and Black Sun。
- Slug：wither-plus-soul-bone-and-black-sun。
- 作者后台：https://authors.curseforge.com/#/projects/1749910/files
- 项目预览：https://www.curseforge.com/minecraft/mc-mods/wither-plus-soul-bone-and-black-sun/preview
- 内置浏览器明确返回 Project created successfully；项目尚待审核，当前对其他用户不可见。
- 已上传项目图标，填写英文摘要与中英文介绍，主分类 Mods / Mobs，开启评论。
- 许可证 All Rights Reserved；禁止第三方分发。
- 已提交以下两个发布文件；文件 ID 表示提交已接收，不表示审核通过。
- 简称 Wither Plus 对应的 slug 已存在，因此 CF 使用上述扩展展示名。模组内名称和 GitHub 名称保持不变。

## GitHub

公开仓库：https://github.com/hp3081925122/Wither-Plus

- 1201forge：原版 Minecraft 1.20.1 Forge。
- 1211neoforge：迁移版 Minecraft 1.21.1 NeoForge。
- 2612neoforge：迁移版 Minecraft 26.1.2 / NeoForge 26.1.2.95 / Java 25。
- 三个分支对应独立本地工作树。
## 2026-10-17 首次文件提交

两个版本分别使用 Java 17、Java 21 构建成功，并通过 JAR 元数据、UTF-8 资源和临时验证器排除检查。

| Minecraft / 加载器 | 模组版本 | 发行类型 | 文件 ID | 已核对标签 |
| --- | --- | --- | --- | --- |
| 1.20.1 Forge | 0.0.1 | Release | 9221895 | 1.20.1, Forge, Java 17, Client, Server |
| 1.21.1 NeoForge | 0.0.1-beta | Beta | 9221930 | 1.21.1, NeoForge, Java 21, Client, Server |

- Forge 文件后台：https://authors.curseforge.com/#/projects/1749910/files/9221895
- NeoForge Beta 文件后台：https://authors.curseforge.com/#/projects/1749910/files/9221930
- 均设置为审核通过后自动发布，更新说明为中英文，无额外模组依赖。
- 构建日志位于 `D:/AI临时文件/witherplus-release/build-1201.log` 与 `build-1211.log`。
- Forge JAR SHA-256：`7e340407a4458e9fd6cb4972672ee8fad84b28c412f977f8ad4fcbacc74a16e3`。
- NeoForge Beta JAR SHA-256：`efa6146550b7fd82d0e5c2d5f5dc8cdf6f578ab392d7e2508e6773fffbe8a2eb`。
最后一次后台核对：两个文件均为 Under Review（审核中）；项目仍提示待审核，不承诺审核或公开时间。


## 26.1.2 独立迁移分支

`2612neoforge` 基于 `1211neoforge` 的 `f99b908` 建立，模组版本保留 `0.0.1-beta`。
本次只构建、验证和推送源码，未为 26.1.2 上传 CurseForge 文件。现有两个文件的审核状态沿用上次核对记录。
目标版验证详见 `migration-26.1.2-neoforge.md`。
