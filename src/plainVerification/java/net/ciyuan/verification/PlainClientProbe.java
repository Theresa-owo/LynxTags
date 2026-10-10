package net.ciyuan.verification;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import org.slf4j.LoggerFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.Screenshot;

/** Java-only probe: no LynxTags/LynxTools imports or payload registrations. */
@Mod(value="lynxplain_verification", dist=Dist.CLIENT)
public class PlainClientProbe {
    private int age;
    private int connected;
    private boolean success;
    private boolean arrived;
    private ClickEvent.RunCommand accept;
    private int cardAt=-1;
    private final boolean requests=Boolean.getBoolean("lynxplain.requests");
    private abstract static class ClickBridge extends Screen {
        protected ClickBridge() { super(Component.literal("probe")); }
        static void click(ClickEvent e,Minecraft mc) { defaultHandleGameClickEvent(e,mc,mc.screen); }
    }
    private static ClickEvent.RunCommand findAccept(Component c) {
        if(c.getStyle().getClickEvent() instanceof ClickEvent.RunCommand run && run.command().startsWith("/tpa ")) return run;
        for(Component child:c.getSiblings()) { var found=findAccept(child); if(found!=null) return found; }
        return null;
    }
    public PlainClientProbe() {
        if(net.neoforged.fml.ModList.get().isLoaded("lynxtags")) throw new IllegalStateException("Plain probe must run without LynxTags");
        NeoForge.EVENT_BUS.addListener((ClientChatReceivedEvent.System e) -> {
            String text=e.getMessage().getString();
            if(requests && text.contains("希望传送")) { accept=findAccept(e.getMessage()); cardAt=connected; Minecraft.getInstance().setScreen(new ChatScreen("",false)); }
            if(text.contains("已传送到你附近")) { arrived=true; LoggerFactory.getLogger("LynxPlainClient").info("NATIVE_REQUEST_CLICK_PASSED {}",text); }
            if(text.contains("home") && (text.contains("已保存") || text.contains("已更新"))) {
                success=true;
                LoggerFactory.getLogger("LynxPlainClient").info("PLAIN_CLIENT_COMMAND_PASSED {}",text);
            }
        });
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e) -> {
            Minecraft mc=Minecraft.getInstance();
            age++;
            if(mc.player!=null && ++connected==100) mc.player.connection.sendCommand("sethome");
            if(requests && cardAt>=0 && connected==cardAt+20) Screenshot.grab(mc.gameDirectory,"request-card.png",mc.getMainRenderTarget(),1,c -> {});
            if(requests && accept!=null && connected==cardAt+40) { ClickBridge.click(accept,mc); accept=null; }
            if(success && (!requests || arrived) && connected>120) mc.stop();
            if(age>1200) { LoggerFactory.getLogger("LynxPlainClient").error("PLAIN_CLIENT_FAILED timeout"); mc.stop(); }
        });
    }
}
