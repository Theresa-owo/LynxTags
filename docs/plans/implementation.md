# 交付检查点

- 独立客户端 LynxTags，配套服务端 LynxTools；仅 NeoForge 26.1.2.115 / MC 26.1.2。
- 用户最终规则：原生头顶名牌，无盔甲架/显示实体/HUD；第一人称自己隐藏，其他玩家/命名生物遵循原版；F5自身和Inventory预览显示。
- 用户已确认逐字RGB长串单次渐变效果，不扩展单字shader方案。
- GPL-3.0-or-later，作者Theresa-owo；用户已授权完成后提交并推送两个指定仓库。
- 客户端 build 成功，真实连接、F5前后、第一人称、生物、Inventory截图与日志已保存在 docs/validation/；无LynxTags客户端连接与/sethome亦通过。
- 测试代码在 src/verification 与 src/plainVerification，不进入生产JAR；运行目录/依赖/世界不推送。
- 下一步最终打包并推送；未验证组合见 docs/VALIDATION.md。未来Fabric/Bukkit/其它版本不在本轮实现范围。
