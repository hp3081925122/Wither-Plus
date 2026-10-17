# Wither Plus 1.21.1 NeoForge 迁移记录

## 目标与基线

源版本：Minecraft 1.20.1 / Forge 47.4.23 / Java 17 / Wither Plus 0.0.1。
目标版本：Minecraft 1.21.1 / NeoForge 21.1.233 / Java 21。
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
