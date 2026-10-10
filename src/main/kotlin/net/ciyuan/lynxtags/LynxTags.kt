package net.ciyuan.lynxtags

import net.ciyuan.lynxtags.protocol.TagUpdate
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent
import net.neoforged.neoforge.client.event.RenderNameTagEvent

@Mod(value="lynxtags",dist=[Dist.CLIENT])
class LynxTags(bus:IEventBus,container:ModContainer) {
    init {
        container.registerConfig(ModConfig.Type.CLIENT,ClientConfig.spec,"lynxtags-client.toml")
        bus.addListener<RegisterPayloadHandlersEvent> { e -> e.registrar("1").optional().playToClient(TagUpdate.TYPE,TagUpdate.CODEC) }
        bus.addListener<RegisterClientPayloadHandlersEvent> { e -> e.register(TagUpdate.TYPE) { payload,_ -> ClientTags.receive(payload) } }
        NeoForge.EVENT_BUS.addListener<RenderNameTagEvent.CanRender>(ClientTags::name)
        NeoForge.EVENT_BUS.addListener<RenderNameTagEvent.DoRender>(ClientTags::draw)
        NeoForge.EVENT_BUS.addListener<ClientPlayerNetworkEvent.LoggingIn> { ClientTags.clear() }
        NeoForge.EVENT_BUS.addListener<ClientPlayerNetworkEvent.LoggingOut> { ClientTags.clear() }
    }
}
