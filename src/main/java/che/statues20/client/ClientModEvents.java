package che.statues20.client;

import che.statues20.Statues20;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid=Statues20.MODID,bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class ClientModEvents {
    @SubscribeEvent public static void setup(FMLClientSetupEvent e){e.enqueueWork(()->{
        MenuScreens.register(Statues20.STATUE_MENU.get(),StatueScreen::new);
        MenuScreens.register(Statues20.SCULPT_MENU.get(),SculptScreen::new);
        MenuScreens.register(Statues20.SHOWCASE_MENU.get(),ShowcaseScreen::new);
    });}
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){
        e.registerLayerDefinition(ShowcaseModel.LAYER, ShowcaseModel::createLayer);
    }
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){
        e.registerBlockEntityRenderer(Statues20.STATUE_BE.get(),StatueRenderer::new);
        e.registerBlockEntityRenderer(Statues20.SHOWCASE_BE.get(),ShowcaseRenderer::new);
    }
}
