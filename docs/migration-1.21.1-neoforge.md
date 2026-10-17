# Wither Plus 1.21.1 NeoForge 迁移记录

## 目标与基线

源版本：Minecraft 1.20.1 / Forge 47.4.23 / Java 17 / Wither Plus 0.0.1。
目标版本：Minecraft 1.21.1 / NeoForge 21.1.251 / Java 21。
保留原工程，目标分支 `1211neoforge`，目标工作树 `../1211-neoforge-WitherPlus`。
许可证保持 All Rights Reserved。基于本机 `E:/模组/模组模板/1211-neoforge` 迁移构建环境。

## 功能保真清单

- 保留全部 12 个技能、7 类 hazard、阶段调度、百分比生命阈值、冷却与 NBT 持久化。
- 保留基础攻击属性乘技能倍率的伤害和受击退抗性影响的击退。
- 保留全部 17 类 VFX、阶段同步、摄像机震动、闪屏/失色/扭曲和凋零发光层。
- 保留全部纹理、自定义着色器、混合模式、几何细节及配置项；不以原版粒子替代。
- 保留原版 WitherBoss Mixin 的行为入口及客户端 EnergySwirlLayer Mixin。

## API 迁移与验证

1. 按目标模板替换 Gradle、Java 和加载器元数据。
2. 对照本机目标依赖源码替换事件、配置注册、payload 注册与投递。
3. 适配 1.21.1 顶点缓冲、着色器、GUI 层与渲染事件，保留原材质和 shader 源码。
4. 核对 Mixin descriptor 与数据资源格式；当前无 data 目录，无配方或战利品目录需要改名。
5. 构建并比对技能、hazard、VFX、纹理和 shader 清单，审查是否存在删减。
6. 使用专用服务端验证加载、全部技能和阶段、伤害/击退/存档/配置。
7. 渲染验证使用 `E:/模组/发cf的模组/Render Preview`，必须区分静态检查与实际画面证据。

## 验证状态

迁移前记录；实施及测试后补充结果，不将构建成功记作游戏内验证。

## 实施结果（2026-10-17）

- 实际目标加载器为 NeoForge 21.1.251，Java 21，使用本机目标版本依赖源码与已有同版项目确认 API。
- 配置改为 ModConfigSpec/ModContainer；网络改为 CustomPacketPayload + StreamCodec，保留消息编号 0..16、NBT 数据和投递范围。
- 客户端保留原 shader，实现 RegisterGuiLayersEvent、ClientTickEvent.Post、MeshData、新顶点 API 与 Matrix4fStack 的适配。
- 亡灵分类采用 1.21.1 原版 EntityTypeTags.UNDEAD；第三方生物需要正确声明该原版标签，不能继续依赖已移除的 MobType。
- pack_format 改为 34；加载器元数据改为 neoforge.mods.toml；Java 21 Mixin，保留全部注入入口。

### 构建与服务端

`gradlew.bat build --no-daemon -Dfile.encoding=UTF-8` 已成功。

独立专用服务端加载成功，外置验证器的 67 项断言全部通过，结束后正常停服存档：

- 12 个技能逐个启动、运行到结束并检查清理。
- 17 种 payload 的编码解码往返。
- 配置生命/攻击属性、伤害倍率、击退抗性、两个百分比阶段与强制技能、阶段 NBT 写入恢复、配置禁用。

验证器位于 `D:/AI临时文件/witherplus-validation/`；通过 `validation.init.gradle` 注入，只用于本地测试，不进入正式 JAR。
日志证据：`D:/AI临时文件/witherplus-validation/server/logs/latest.log`，结果 `WITHER_VALIDATION_COMPLETE checks=67 skills=12 payloads=17`。
此测试在真实专用服务端中加速调用技能 tick；不等于真人联网完整战斗、网络实际传输或客户端画面验证。测试服初次生成配置有原版 superflat layers 提示，不影响这些断言。

### 保真审查与客户端

独立只读审查确认：12 个具体技能、7 个 hazard、17 个 VFX 均保留；配置表达式一致；5 张纹理哈希一致；4 个 shader JSON 一致；7 个 GLSL 文件统一换行后内容一致。未发现通过删减功能完成迁移的情况。

Render Preview 1.21.1 已加载实际模组着色器，17 个 VFX、发光层、屏幕效果共 19 个场景成功离屏捕获，采样均有非背景像素且洋红缺失纹理比例为 0。统计与拼图分别位于 `D:/AI临时文件/witherplus-preview/results-1211.json`、`contact-1211.png`。骨刺另有近景采样。静态截图不能代替真实战斗的动态镜头与全部时间点测试。

### 跨版本画面对照

使用两个版本各自的 Render Preview，按同一场景参数、时间、镜头、分辨率捕获 19 组图像。
其中 17 组平均 RGB 绝对差异小于 0.0003（每通道范围 0..255）。两组差异单列：

- Scorch：平均差异 1.06675/255；源码的静态 counter 参与火舌位置和噪声 seed，两次运行的实例计数不同，截图不承诺逐像素一致。两边该 VFX 源码语义相同。
- Glow：平均差异 0.09001/255，原模型浮点颜色参数改为目标 API 的 ARGB 整数颜色；几何与发光层保留。

比较数据 `D:/AI临时文件/witherplus-preview/comparison.json`，对照图 `comparison.png`。
原版预览初次因直接使用 SRG 发布 JAR 出现 NoSuchMethodError；改用 Render Preview 已有开发类输出组成的临时开发 JAR 后成功。未修改 Render Preview 工程或原版模组源码。

正式 JAR `build/libs/witherplus-1.21.1-neoforge-0.0.1.jar` 已检查无 `com/witherplus/validation` 或 Render Preview 类。

### 发布状态

GitHub 公开仓库：https://github.com/hp3081925122/Wither-Plus
版本分支：`1201forge` 与 `1211neoforge`，分别对应独立工作树。
CurseForge 项目创建尚未提交：名称、英文摘要、Mods / Mobs 分类已填写，必填图标被 Edge 扩展的文件 URL 访问设置阻止。需开启该能力后继续，不将草稿记为已创建项目。
