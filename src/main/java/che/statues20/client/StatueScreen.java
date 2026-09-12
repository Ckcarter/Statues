package che.statues20.client;

import che.statues20.blockentity.StatueBlockEntity;
import che.statues20.menu.StatueMenu;
import che.statues20.network.ModNetwork;
import che.statues20.network.UpdatePosePacket;
import che.statues20.pose.StatuePose;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

/** Completed-statue GUI: equipment plus the two hand item angles, as in original GuiStatue. */
public class StatueScreen extends AbstractContainerScreen<StatueMenu> {
    private StatuePose pose=new StatuePose();
    public StatueScreen(StatueMenu m,Inventory i,Component t){super(m,i,t);imageWidth=256;imageHeight=226;inventoryLabelX=48;inventoryLabelY=132;}
    @Override protected void init(){
        super.init(); if(minecraft.level.getBlockEntity(menu.pos) instanceof StatueBlockEntity s)pose=s.getPose().copy();
        // Original controls: left at x12 and right at x121 in the old 176px GUI. +40 centers them here.
        addRenderableWidget(new AngleSlider(leftPos+52,topPos+92,43,13,Component.translatable("gui.statues20.left_item"),pose.itemLeftA,v->pose.itemLeftA=v));
        addRenderableWidget(new AngleSlider(leftPos+161,topPos+92,43,13,Component.translatable("gui.statues20.right_item"),pose.itemRightA,v->pose.itemRightA=v));
    }
    @Override protected void renderBg(GuiGraphics g,float p,int x,int y){
        g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xF01B1B1B);
        g.fill(leftPos+5,topPos+5,leftPos+251,topPos+221,0xE2383838);
        g.fill(leftPos+72,topPos+24,leftPos+184,topPos+116,0xA8151515);
        g.fill(leftPos+42,topPos+139,leftPos+214,topPos+222,0xAA202020);
    }
    @Override public void render(GuiGraphics g,int x,int y,float p){renderBackground(g);super.render(g,x,y,p);renderTooltip(g,x,y);}
    @Override protected void renderLabels(GuiGraphics g,int x,int y){g.drawCenteredString(font,title,imageWidth/2,9,0xFFFFFF);g.drawString(font,playerInventoryTitle,48,132,0xDDDDDD,false);}
    @Override public boolean keyPressed(int k,int s,int m){if(k==GLFW.GLFW_KEY_E||InputConstants.getKey(k,s).getValue()==GLFW.GLFW_KEY_E)return true;return super.keyPressed(k,s,m);}
    @Override public void onClose(){ModNetwork.CHANNEL.sendToServer(new UpdatePosePacket(menu.pos,pose.copy()));super.onClose();}
    private interface Set{void set(float v);} private static class AngleSlider extends AbstractSliderButton{
        private final Component label;private final Set set;
        AngleSlider(int x,int y,int w,int h,Component label,float v,Set s){super(x,y,w,h,label,v);this.label=label;set=s;updateMessage();}
        @Override protected void updateMessage(){setMessage(Component.literal(label.getString()+" "+(int)(value*180)+"°"));}
        @Override protected void applyValue(){set.set((float)value);}
    }
}
