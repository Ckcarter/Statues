package che.statues20.client;

import che.statues20.menu.ShowcaseMenu;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

/** Large 256x226 screen matching the original showcase GUI footprint. */
public class ShowcaseScreen extends AbstractContainerScreen<ShowcaseMenu> {
    public ShowcaseScreen(ShowcaseMenu m,Inventory i,Component t){super(m,i,t);imageWidth=256;imageHeight=226;inventoryLabelX=48;inventoryLabelY=132;}
    @Override protected void renderBg(GuiGraphics g,float p,int x,int y){
        g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xF01B1B1B);
        g.fill(leftPos+5,topPos+5,leftPos+251,topPos+221,0xE2383838);
        g.fill(leftPos+102,topPos+41,leftPos+154,topPos+89,0xB0101010);
        g.fill(leftPos+42,topPos+139,leftPos+214,topPos+222,0xAA202020);
    }
    @Override public void render(GuiGraphics g,int x,int y,float p){renderBackground(g);super.render(g,x,y,p);renderTooltip(g,x,y);}
    @Override protected void renderLabels(GuiGraphics g,int x,int y){g.drawCenteredString(font,title,imageWidth/2,10,0xFFFFFF);g.drawString(font,playerInventoryTitle,48,132,0xDDDDDD,false);}
    @Override public boolean keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_E||InputConstants.getKey(k,s).getValue()==GLFW.GLFW_KEY_E)return true;return super.keyPressed(k,s,m);}
}
