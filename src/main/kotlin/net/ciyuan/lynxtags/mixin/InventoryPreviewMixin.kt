package net.ciyuan.lynxtags.mixin

import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
import net.ciyuan.lynxtags.ClientTags
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState
import org.joml.Quaternionf
import org.joml.Vector3f
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At

@Mixin(GuiGraphicsExtractor::class)
abstract class InventoryPreviewMixin {
    @WrapOperation(method=["entity"],at=[At(value="NEW",target="net/minecraft/client/renderer/state/gui/pip/GuiEntityRenderState")])
    private fun lynxPreview(state:EntityRenderState,translation:Vector3f,rotation:Quaternionf,camera:Quaternionf?,
        x0:Int,y0:Int,x1:Int,y1:Int,scale:Float,scissor:ScreenRectangle?,original:Operation<GuiEntityRenderState>):GuiEntityRenderState {
        val fitted=ClientTags.prepareInventory(state,scale,x1-x0)
        return original.call(state,translation,rotation,camera,x0,y0,x1,y1,fitted,scissor)
    }
}
