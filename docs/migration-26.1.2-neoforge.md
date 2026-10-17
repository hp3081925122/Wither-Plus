# Wither Plus 26.1.2 NeoForge 迁移文档

## 基线与边界

- 源：`1211neoforge`，Minecraft 1.21.1 / NeoForge 21.1.251 / Java 21，0.0.1-beta。
- 目标：`2612neoforge`，Minecraft 26.1.2 / NeoForge 26.1.2.95 / Java 25。
- 独立工作树：`../2612-neoforge-WitherPlus`，保留现有两个版本的源码与运行目录。
- 采用本机 2612-neoforge 模板的 Gradle/加载器工具链，保留 ARR 与现有 Beta 版本标记。

## 不可删减的功能清单

保留全部 12 个技能、7 类 hazard、17 类 VFX、全部配置项、百分比阶段阈值、阶段 NBT 持久化、伤害倍率、击退抗性、消息编号与投递范围。
保留原自定义 shader 算法、纹理、几何、透明/加法混合、屏幕失色/闪屏/暗角、摄像机震动和凋零发光层，不使用简化粒子替代。

## 适配清单

1. Java 25、Gradle、NeoForge 与 NeoForm 工具链及元数据。
2. Identifier、网络 payload、NBT/ValueInput/ValueOutput、hurtServer 与实体/音效/标签 API。
3. 提交式生物渲染层、RenderPipeline/GPU uniform/纹理/渲染目标接口，完整迁移各材质与屏幕 shader。
4. 源版本无 data 注册资源，无旧复数数据目录；目标资源包使用 min_format/max_format 84，检查 shader 目录和格式变化。
5. 保留 WitherBoss 与客户端能量层 Mixin 的原有行为，按目标 descriptor 适配。

## 验证计划

- 构建并检查正式 JAR 的版本、UTF-8、元数据、Mixin 与资源。
- 独立审查技能/hazard/VFX/配置/纹理/shader，实现差异逐项说明。
- 外置专用服务端测试全部技能执行、阶段/强制技能、伤害/击退、配置与持久化；测试文件全部位于 D:/AI临时文件。
- 使用 E:/模组/发cf的模组/Render Preview/26.1.2-neoforge-render-preview 捕获同一组场景，并与 1.21.1 基线比较，区别实际截图和静态检查。
- 不将构建或截图作为真人联网完整战斗证明；记录实际通过和待验证范围。

## 当前状态

2026-10-17 已完成目标版适配、专用服务端验证及 Render Preview 实测。正式构建结果与 SHA-256 记录在下方。

### 实现保真审查

- 对比基线 `f99b908`：原 60 个 Java 文件全部保留，新增 4 个渲染适配文件；12 技能和 hazard 的伤害、时序、范围与行为未简化。
- 17 类 VFX 的几何和生命周期保留；全部 35 项配置定义及默认值一致，5 张 PNG 的 SHA-256 完全一致。
- 保留 GLSL 算法主体，改为 RenderPipeline 与 uniform buffer；通过 DynamicTransforms 保留 ColorModulator 乘数。骨刺保留原 position_tex_color 的纹理、顶点色、透明裁剪及深度写入。
- 修复实际客户端捕获发现的首次纹理上传异常：在开启 GPU render pass 前取得并加载骨刺纹理。
- 服务端存档切换 ValueInput/ValueOutput，保留 WpStageTwo/WpStageThree 两个键；伤害切换 hurtServer。未引入新的可选依赖或删减效果。

### 专用服务端

实际运行 NeoForge 26.1.2.95 专服，外置测试器报告 `WITHER_VALIDATION_COMPLETE checks=67 skills=12 payloads=17`，无验证失败，服务器正常保存并结束。

覆盖配置应用、基础伤害倍率、完整击退抗性、阶段 NBT 往返、17 种 payload 编解码、12 技能启动/完成/隐身清理、百分比阶段切换及强制冠冕/安魂曲、增强开关。
测试器手动推进脑逻辑以覆盖各技能周期；这证明专服执行与这些断言通过，不等于真人联网战斗、各地形命中率或长时间负载测试。

- 日志：`D:/AI临时文件/witherplus-2612-server.log`。
- 外置测试器：`D:/AI临时文件/witherplus-2612-validation`，未写入模组源码或正式包。

### Render Preview 实测

使用指定工具 `E:/模组/发cf的模组/Render Preview/26.1.2-neoforge-render-preview`，独立客户端捕获 1280×720 图片，并实际查看汇总与骨刺近景。

- 19 组基线场景：aura、black_sun、bolts、burst、chains、crown、dash、maelstrom、mark、meteor、pillar、pyre、requiem、scorch、shade、spires、tide、glow、screen。
- 5 组补充：第一阶段发光、第二阶段发光、实际推进 Aura 后的狂暴叠层、隐身、近距离骨刺。
- 24 组均捕获成功，紫黑材质比例为 0；隐身场景 foregroundPixels=0。最终两次客户端日志无 ERROR/Exception/GL_INVALID。
- 与已有 1.21.1 捕获保持相同采样 tick、视角、距离、画布尺寸。各技能轮廓、纹理、法阵、屏幕失色/闪光/暗角保持；跨版本并非逐像素一致：19 组平均绝对 RGB 差范围约 0.014–6.025/255，安魂曲最大。
- shader GameTime/随机实例状态未跨进程同步，存在动态纹理相位、像素亮度差异；骨刺透明裁剪已按旧原版 shader 恢复。此对照未覆盖每个技能的全部时间点，也未做实际世界完整战斗录像。
- 基线对照：`D:/AI临时文件/witherplus-preview/comparison-2612.png` 与 `comparison-2612.json`。
- 目标汇总：`D:/AI临时文件/witherplus-preview/contact-2612.png`、`contact-2612-extra.png`。
- 结果元数据：`results-2612.json`、`results-2612-extra.json`，位于同一目录，包含每张原始图片路径。
- 客户端日志：`D:/AI临时文件/witherplus-2612-client-final.log`、`witherplus-2612-client-extra.log`。

### 首次迁移构建与发布边界

正式构建不加载任何外置验证 init 脚本或 Render Preview 依赖。版本保留 `0.0.1-beta`。本轮未上传 26.1.2 到 CurseForge。


- `gradlew clean build`：BUILD SUCCESSFUL（15s）。
- 正式文件：`build/libs/witherplus-26.1.2-neoforge-0.0.1-beta.jar`，183895 字节。
- SHA-256：`aa5f799c3d2e7d7903fe85a0521dbb6079f773e5651e10744c81e468bb799a4a`。
- JAR 审核：全部 class 为 Java 25（major 69）；元数据与文本资源 UTF-8、ARR、Beta、资源包格式 84、JAVA_25 Mixin 正确；5 张纹理齐全；不含验证器或 Render Preview 类。


### 后续 0.0.1 正式发布

用户随后要求将 26.1.2 上传同一 CF 项目并去掉 Beta 标记，已将 `mod_version` 改为 `0.0.1`。
正式 JAR 为 `build/libs/witherplus-26.1.2-neoforge-0.0.1.jar`，183892 字节，SHA-256 为 `3f63d5224701ac9acd50177f087730fa569d1681342447ecebdf3da2d96e4cda`。
重新 `gradlew build` 成功（21s），核对包内版本为 0.0.1，未包含验证器或 Render Preview；本次未改动技能和渲染代码，因此未重复运行已通过的迁移测试。
CF 项目 1749910 已接收文件 9222621，发行类型 Release，最后状态 Processing；详情见 `publishing.md`。初次迁移 Beta 包记录仅作为历史验证证据保留。
