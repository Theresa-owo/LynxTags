# LynxTags

Minecraft **26.1.2 / NeoForge 26.1.2.115** 的客户端玩家名牌 Mod，作者 **Theresa-owo**。
与 [LynxTools](https://github.com/Theresa-owo/LynxTools) 的可选协议配套，也可在没有协议的服务器上使用原生显示名。

## 原生名牌

- 使用原生玩家头顶名牌、Component 与字体提交管线。没有盔甲架、Text Display、悬浮实体、HUD 替代物或新字体 shader。
- 第一人称不显示自身世界名牌，F5 前/后第三人称显示自身头顶名牌。
- 其他玩家保留原版距离、潜行、隐身、队伍可见性判断，只替换允许显示时的文字。
- 非玩家实体不接管：命名生物仍按原版显示/隐藏。
- Inventory/创造物品栏的人物预览使用同一份名牌，缩小预览模型为头顶留出空间，并将长标签适配到预览纹理范围。
- 不修改按键、摄像机、队伍、TAB 或聊天显示名。RGB 是逐字颜色插值，不是动画；长串单次渐变效果已由用户确认。

## 安装

Java 25、Minecraft 26.1.2、NeoForge 26.1.2.115；将 `LynxTags-1.0.0.jar` 放入客户端 `mods/`。
Kotlin stdlib 2.3.20 已内嵌，不需要额外 Kotlin 前置。此 Mod 替代 Perspective Nametag，不建议同时安装两个自身名牌控制器。

服务器不安装 LynxTags。需要 LP 格式时安装 LynxTools 与 LuckPerms，并在服务端 Config.toml 启用：

```toml
[nametags]
enabled = true
name_format = "<gradient:#42D9E8:#B58AFF><name></gradient>"
```

执行 `/lynxtools reload`。服务端读取 LuckPerms 当前上下文前后缀并解析颜色/渐变，再发送安全文本样式。
例如 LP 前缀 `<gradient:#42D9E8:#B58AFF>[Admin]</gradient> `；前后缀各自解析，`<name>` 是安全用户名占位符。
没有协议时回退原生 displayName；不伪造 LuckPerms 数据，不要求其他玩家安装客户端扩展。

## 配置

NeoForge 配置文件 `config/lynxtags-client.toml`：

| 键 | 默认 | 说明 |
|---|---|---|
| enabled | true | 总开关 |
| show_self_third_person | true | 自身第三人称名牌 |
| inventory | true | 物品栏人物预览 |
| server_format | true | 使用可选服务端格式 |
| inventory_model_scale | 0.8 | 预览模型倍率，0.4–1.0 |

长标签在小尺寸 Inventory 预览中会缩小，是保留整行文字的取舍。第一人称没有自身 HUD 名牌选项。

## 协议和兼容范围

`lynxtags:name_v1`：NeoForge optional clientbound play payload，版本 1。
内容只有 UUID、UTF-8 文本片段、RGB 和四个装饰位；最多 256 Unicode 码点/256 段。空列表移除样式。
没有点击/悬浮事件、命令或任意 Component 对象树。连接/断开清空缓存，最多缓存 4096 个 UUID。
不修改客户端/服务端 Mod 列表要求。当前仅支持所列 NeoForge/MC 版本；Fabric/Bukkit/Paper 及其它版本尚未实现。

构建：PowerShell 7、JDK 25，执行 `./gradlew.ps1 build sourceArchive --console=plain`。
工具链：Gradle 9.2.1、Kotlin 2.3.20、ModDevGradle 2.0.148。产物 `build/libs/LynxTags-1.0.0.jar`。
独立验证代码不进入生产 JAR。结果与限制见 [docs/VALIDATION.md](docs/VALIDATION.md)。

## 许可

Copyright (C) 2026 Theresa-owo。**GPL-3.0-or-later**，见 [LICENSE.md](LICENSE.md) 与 [NOTICE.md](NOTICE.md)。
允许合规商用，分发受 GPL 覆盖的衍生作品须遵守对应源码等义务。第三方依赖保留各自许可。
