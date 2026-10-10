package net.ciyuan.lynxtags.mixin

import net.ciyuan.lynxtags.ClientTags
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.state.EntityRenderState
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(EntityRenderer::class)
abstract class NativeNameSubmitMixin {
    @Inject(method=["extractRenderState"],at=[At("RETURN")])
    private fun lynxCapture(entity:Entity,state:EntityRenderState,partialTick:Float,ci:CallbackInfo) {
        if(entity is Player) ClientTags.captureInventory(entity,state)
    }
    @Inject(method=["submit"],at=[At("HEAD")])
    private fun lynxName(state:EntityRenderState,pose:PoseStack,collector:SubmitNodeCollector,camera:CameraRenderState,ci:CallbackInfo) {
        ClientTags.beforeSubmit(state)
    }
}
