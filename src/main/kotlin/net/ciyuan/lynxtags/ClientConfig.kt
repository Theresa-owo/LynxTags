package net.ciyuan.lynxtags

import net.neoforged.neoforge.common.ModConfigSpec

object ClientConfig {
    private val builder=ModConfigSpec.Builder()
    val enabled=builder.comment("Enable LynxTags player formatting. Named mobs remain completely vanilla.").define("enabled",true)
    val showSelf=builder.comment("Show your native overhead nametag in third person; never in first person.").define("show_self_third_person",true)
    val inventory=builder.comment("Show the native nameplate in player inventory previews; no HUD or extra entities.").define("inventory",true)
    val serverFormat=builder.comment("Use optional LynxTools server formatting; otherwise use vanilla display names.").define("server_format",true)
    val inventoryModelScale=builder.comment("Preview model scale multiplier to leave room for the native overhead name. 0.4..1.0.").defineInRange("inventory_model_scale",0.8,0.4,1.0)
    val spec=builder.build()
}
