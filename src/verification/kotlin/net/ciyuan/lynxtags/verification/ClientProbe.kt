package net.ciyuan.lynxtags.verification

import net.ciyuan.lynxtags.ClientTags
import net.minecraft.client.Minecraft
import net.minecraft.client.CameraType
import net.minecraft.client.Screenshot
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.network.chat.Component
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.common.Mod
import net.neoforged.bus.api.EventPriority
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.RenderNameTagEvent
import org.slf4j.LoggerFactory
import java.util.UUID

/** A real game client, restricted to loopback test server. Not shipped in the release JAR. */
@Mod(value="lynxtags_verification",dist=[Dist.CLIENT])
class ClientProbe {
    private val log=LoggerFactory.getLogger("LynxTagsClientProbe")
    private var ticks=0
    private var phase=-1
    private var age=0
    private var own=0
    private var mob=0
    private var done=false
    private var lastOwn=""
    init {
        NeoForge.EVENT_BUS.addListener<ClientTickEvent.Post> { tick() }
        NeoForge.EVENT_BUS.addListener<RenderNameTagEvent.DoRender>(EventPriority.LOWEST,true) { e ->
            val p=Minecraft.getInstance().player
            if(p!=null && e.content.string.contains(p.gameProfile.name())) {
                own++
                val mc=Minecraft.getInstance()
                lastOwn="camera=${mc.options.cameraType}, screen=${mc.screen?.javaClass?.simpleName}, content=${e.content.string}"
            }
            if(e.content.string.contains("NamedCow")) mob++
        }
    }
    @Suppress("UNCHECKED_CAST") private fun synced(id:UUID):Component? =
        (ClientTags::class.java.getDeclaredField("names").apply { isAccessible=true }.get(ClientTags) as Map<UUID,Component>)[id]
    private fun tick() {
        if(done) return
        val mc=Minecraft.getInstance()
        try {
            check(++ticks<2400) { "Timed out waiting for loopback server/protocol" }
            val p=mc.player ?: return
            val label=synced(p.uuid) ?: return
            mc.options.pauseOnLostFocus=false
            if(phase<0) {
                check(label.string.contains("Lynx")) { "Missing live server format: ${label.string}" }
                log.info("LIVE_PROTOCOL_RECEIVED uuid={} text={}",p.uuid,label.string)
                phase=0; age=0; mc.setScreen(null)
                mc.options.cameraType=CameraType.THIRD_PERSON_BACK
            }
            age++
            p.yRot=0f; p.xRot=0f
            mc.options.cameraType=when(phase) { 0 -> CameraType.THIRD_PERSON_BACK; 1 -> CameraType.THIRD_PERSON_FRONT; else -> CameraType.FIRST_PERSON }
            if(System.getProperty("lynxtags.visual")=="true") {
                mc.options.cameraType=CameraType.THIRD_PERSON_FRONT
                if(age==100) { capture("long-single-gradient.png"); log.info("LONG_GRADIENT_READY {}",label.string) }
                if(age>=100) return
            }
            if(age==20) { own=0; mob=0 }
            if(age<100) return
            when(phase) {
                0 -> { check(own>0) { "No self native tag in third person back" }; capture("01-third-person-back.png"); mc.options.cameraType=CameraType.THIRD_PERSON_FRONT }
                1 -> { check(own>0) { "No self native tag in third person front" }; capture("02-third-person-front.png"); mc.options.cameraType=CameraType.FIRST_PERSON }
                2 -> { capture("03-first-person.png"); check(own==0) { "Self tag rendered in first person: $lastOwn" }; check(mob>0) { "Vanilla named mob missing" }; mc.setScreen(InventoryScreen(p)) }
                3 -> { check(own>0) { "No native inventory tag" }; capture("04-inventory.png"); log.info("CLIENT_NATIVE_TAGS_PASSED: live protocol, F5 back/front, first-person self hidden, named mob retained, native inventory tag") }
                4 -> { done=true; mc.stop(); return }
            }
            log.info("CLIENT_PHASE_PASS phase={} nativeSelfSubmits={} namedMobSubmits={}",phase,own,mob)
            phase++; age=0; own=0; mob=0
        } catch(e:Throwable) { log.error("CLIENT_NATIVE_TAGS_FAILED",e); done=true; mc.stop() }
    }
    private fun capture(name:String) {
        val mc=Minecraft.getInstance()
        Screenshot.grab(mc.gameDirectory,name,mc.mainRenderTarget,1) { log.info("Screenshot {}: {}",name,it.string) }
    }
}
