package net.ciyuan.lynxtags

import net.ciyuan.lynxtags.protocol.TagUpdate
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
import net.minecraft.network.chat.Component
import net.minecraft.util.TriState
import net.minecraft.world.entity.EntityAttachment
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.client.ClientHooks
import net.neoforged.neoforge.client.event.RenderNameTagEvent
import java.util.UUID
import java.util.WeakHashMap

object ClientTags {
    private val names=mutableMapOf<UUID,Component>()
    private data class PlayerLabel(val text:Component,val attachment:Vec3)
    private val players=WeakHashMap<EntityRenderState,PlayerLabel>()
    private val previews=WeakHashMap<EntityRenderState,Float>()
    fun captureInventory(player:Player,state:EntityRenderState) {
        if(!ClientConfig.enabled.get() || !ClientConfig.inventory.get()) return
        val text=if(ClientConfig.serverFormat.get()) names[player.uuid] ?: player.displayName else player.displayName
        val attachment=player.attachments.getNullable(EntityAttachment.NAME_TAG,0,player.yRot) ?: Vec3(0.0,player.bbHeight.toDouble(),0.0)
        players[state]=PlayerLabel(text,attachment)
    }
    /** Model mods may clear the preview name after extraction. Restore at the native submit boundary. */
    fun beforeSubmit(state:EntityRenderState) {
        if(!ClientConfig.enabled.get()) return
        val label=players[state] ?: return
        if(state in previews && ClientConfig.inventory.get()) {
            state.nameTag=label.text; state.nameTagAttachment=label.attachment; state.scoreText=null
        } else if(state.nameTag!=null && ClientConfig.serverFormat.get()) {
            state.nameTag=label.text
        }
    }
    fun clear() { names.clear(); players.clear(); previews.clear() }
    fun receive(update:TagUpdate) {
        TagUpdate.validate(update.spans)
        if(update.spans.isEmpty()) { names.remove(update.player); return }
        if(update.player !in names && names.size>=4096) return
        val result=Component.empty()
        for(s in update.spans) result.append(Component.literal(s.text).withStyle { style ->
            style.withColor(s.rgb).withBold(s.flags and 1!=0).withItalic(s.flags and 2!=0)
                .withUnderlined(s.flags and 4!=0).withStrikethrough(s.flags and 8!=0)
        })
        names[update.player]=result
    }
    fun name(e:RenderNameTagEvent.CanRender) {
        val p=e.entity as? Player ?: return // Do not interfere with named mobs/mannequins/other entities.
        if(!ClientConfig.enabled.get()) return
        val mc=Minecraft.getInstance()
        val viewer=mc.player ?: return
        val label=if(ClientConfig.serverFormat.get()) names[p.uuid] ?: e.content ?: p.displayName else e.content ?: p.displayName
        val attachment=p.attachments.getNullable(EntityAttachment.NAME_TAG,0,p.yRot) ?: Vec3(0.0,p.bbHeight.toDouble(),0.0)
        players[e.entityRenderState]=PlayerLabel(label,attachment)
        e.setContent(label)
        if(p===viewer) {
            if(mc.options.cameraType.isFirstPerson) {
                e.setCanRender(TriState.FALSE)
            } else if(ClientConfig.showSelf.get() && Minecraft.renderNames() && !p.isInvisibleTo(viewer) && !p.isVehicle
                && e.entityRenderState.distanceToCameraSq<4096.0 && (!p.isDiscrete || ClientHooks.isNameplateInRenderDistance(p,e.entityRenderState.distanceToCameraSq))) {
                e.setCanRender(TriState.TRUE)
            }
        }
        // All other players keep the renderer's original distance/team/invisibility/sneaking decision.
    }
    fun prepareInventory(state:EntityRenderState,scale:Float,width:Int):Float {
        if(!ClientConfig.enabled.get() || !ClientConfig.inventory.get()) return scale
        val screen=Minecraft.getInstance().screen
        if(screen !is InventoryScreen && screen !is CreativeModeInventoryScreen) return scale
        val label=players[state] ?: return scale
        val adjusted=scale*ClientConfig.inventoryModelScale.get().toFloat()
        state.nameTag=label.text
        state.nameTagAttachment=label.attachment
        state.scoreText=null
        state.distanceToCameraSq=0.0
        val pixels=Minecraft.getInstance().font.width(label.text)*0.025f*adjusted
        previews[state]=if(pixels>0) ((width-4).coerceAtLeast(1)/pixels).coerceIn(0.05f,1f) else 1f
        return adjusted
    }
    fun draw(e:RenderNameTagEvent.DoRender) {
        val factor=previews[e.entityRenderState] ?: return
        val point=e.entityRenderState.nameTagAttachment ?: return
        // Still use the vanilla nameplate submit/render pipeline; only fit its scale inside the preview texture.
        e.isCanceled=true
        val pose=e.poseStack
        pose.pushPose()
        pose.scale(factor,factor,factor)
        val anchor=Vec3(point.x/factor,(point.y+0.5)/factor-0.5,point.z/factor)
        val body=e.entityRenderState as? LivingEntityRenderState
        val camera=if(ClientConfig.inventoryFollowRotation.get() && body!=null) CameraRenderState().apply {
            // UI pose already contains the model tilt. Do not cancel it with the billboard camera.
            // LivingEntityRenderer rotates the body by (180 - bodyRot); the native text needs another 180.
            orientation.rotationY(Math.toRadians((360f-body.bodyRot).toDouble()).toFloat())
        } else e.cameraRenderState
        e.submitNodeCollector.submitNameTag(pose,anchor,0,e.content,!e.entityRenderState.isDiscrete,
            e.entityRenderState.lightCoords,0.0,camera)
        pose.popPose()
    }
}
