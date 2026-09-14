package che.statues20.client;

import che.statues20.menu.SculptMenu;
import che.statues20.network.CreateStatuePacket;
import che.statues20.network.ModNetwork;
import che.statues20.pose.StatuePose;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

/** Modernized recreation of the original 227x228 GuiSculpt, enlarged but preserving its six-pad workflow. */
public class SculptScreen extends AbstractContainerScreen<SculptMenu> {
    private static StatuePose LAST_POSE = new StatuePose();
    private static String LAST_SKIN = "";

    private EditBox skin;
    private StatuePose pose;
    private final Random random = new Random();
    private PosePad ar, al, rr, ll, head, body;
    private PlayerModel<LivingEntity> previewClassic;
    private PlayerModel<LivingEntity> previewSlim;

    public SculptScreen(SculptMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 300;
        imageHeight = 270;
        inventoryLabelY = 999;
        pose = menu.hasCopiedPose ? menu.copiedPose.copy() : LAST_POSE.copy();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override protected void init() {
        super.init();
        previewClassic = new PlayerModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER), false);
        previewSlim = new PlayerModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_SLIM), true);
        preparePreviewModel(previewClassic);
        preparePreviewModel(previewSlim);
        int x = leftPos, y = topPos;

        // Old GuiSculpt pad order/semantics, including left-side axis inversions.
        ar = addRenderableWidget(new PosePad(x+12,y+28,58,58,"Right Arm", pose.armRightA, 1-pose.armRightB,
                (u,v)->{pose.armRightA=u; pose.armRightB=1-v;}));
        al = addRenderableWidget(new PosePad(x+230,y+28,58,58,"Left Arm", 1-pose.armLeftA, 1-pose.armLeftB,
                (u,v)->{pose.armLeftA=1-u; pose.armLeftB=1-v;}));
        rr = addRenderableWidget(new PosePad(x+12,y+98,58,58,"Right Leg", pose.legRightA, 1-pose.legRightB,
                (u,v)->{pose.legRightA=u; pose.legRightB=1-v;}));
        ll = addRenderableWidget(new PosePad(x+230,y+98,58,58,"Left Leg", 1-pose.legLeftA, 1-pose.legLeftB,
                (u,v)->{pose.legLeftA=1-u; pose.legLeftB=1-v;}));
        head = addRenderableWidget(new PosePad(x+12,y+168,58,58,"Head", pose.headA, pose.headB,
                (u,v)->{pose.headA=u; pose.headB=v;}));
        body = addRenderableWidget(new PosePad(x+230,y+168,58,58,"Body", pose.bodyA, pose.bodyB,
                (u,v)->{pose.bodyA=u; pose.bodyB=v;}));

        skin = new EditBox(font, x+92, y+167, 116, 20, Component.translatable("gui.statues20.player_name"));
        skin.setMaxLength(64); skin.setValue(LAST_SKIN); addRenderableWidget(skin);

        addRenderableWidget(Button.builder(Component.translatable("gui.statues20.randomize"), b -> {
            pose.randomize(random); syncPads();
        }).bounds(x+84,y+200,132,20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.statues20.sculpt_button"), b -> {
            LAST_POSE = pose.copy(); LAST_SKIN = skin.getValue();
            Direction facing = minecraft.player.getDirection().getOpposite();
            ModNetwork.CHANNEL.sendToServer(new CreateStatuePacket(menu.sourcePos, skin.getValue(), facing, pose.copy()));
            onClose();
        }).bounds(x+84,y+228,132,20).build());
    }

    private void syncPads() {
        ar.setSilently(pose.armRightA, 1-pose.armRightB);
        al.setSilently(1-pose.armLeftA, 1-pose.armLeftB);
        rr.setSilently(pose.legRightA, 1-pose.legRightB);
        ll.setSilently(1-pose.legLeftA, 1-pose.legLeftB);
        head.setSilently(pose.headA, pose.headB);
        body.setSilently(pose.bodyA, pose.bodyB);
    }

    @Override protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        // Original-inspired dark stone panel; kept procedural so resource packs can still style widgets.
        g.fill(leftPos, topPos, leftPos+imageWidth, topPos+imageHeight, 0xF01D1D1D);
        g.fill(leftPos+5, topPos+5, leftPos+imageWidth-5, topPos+imageHeight-5, 0xEE3A3A3A);
        g.fill(leftPos+78, topPos+24, leftPos+222, topPos+158, 0xBB151515);
        renderPreview(g);
    }

    private static void preparePreviewModel(PlayerModel<?> model) {
        model.setAllVisible(true);
        model.hat.visible = true;
        model.jacket.visible = true;
        model.leftSleeve.visible = true;
        model.rightSleeve.visible = true;
        model.leftPants.visible = true;
        model.rightPants.visible = true;
    }

    private void renderPreview(GuiGraphics graphics) {
        if (previewClassic == null || previewSlim == null || minecraft.level == null) return;
        BlockState source = minecraft.level.getBlockState(menu.sourcePos);
        String skinName = skin == null ? LAST_SKIN : skin.getValue();
        PlayerModel<LivingEntity> previewModel = StatueTextureManager.isSlim(skinName) ? previewSlim : previewClassic;
        preparePreviewModel(previewModel);
        StatuePoseApplier.apply(previewModel, pose);
        ResourceLocation texture = StatueTextureManager.texture(skinName, source);

        PoseStack ps = graphics.pose();
        ps.pushPose();
        ps.translate(leftPos + imageWidth/2.0F, topPos + 145.0F, 100.0F);
        // GUI coordinates already grow downward on Y. A negative Y scale here turns the
        // preview upside down; keep Y positive for an upright player preview.
        ps.scale(42.0F, 42.0F, 42.0F);
        ps.mulPose(Axis.YP.rotationDegrees(180.0F + StatuePoseApplier.bodyYawDegrees(pose)));
        ps.mulPose(Axis.XP.rotationDegrees(StatuePoseApplier.bodyPitchDegrees(pose)));
        ps.translate(0, -1.5 + StatuePoseApplier.legHeightOffset(previewModel) * 0.41F, 0);
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucent(texture));
        previewModel.renderToBuffer(ps, vc, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1,1,1,1);
        buffers.endBatch();
        ps.popPose();
    }

    @Override public void render(GuiGraphics g, int mx, int my, float partialTick) {
        renderBackground(g); super.render(g,mx,my,partialTick); renderTooltip(g,mx,my);
    }

    @Override protected void renderLabels(GuiGraphics g, int mx, int my) {
        g.drawCenteredString(font, title, imageWidth/2, 9, 0xFFFFFF);
        g.drawCenteredString(font, Component.translatable("gui.statues20.player_name"), imageWidth/2, 155, 0xDDDDDD);
        g.drawCenteredString(font, Component.translatable("gui.statues20.pose_help"), imageWidth/2, 254, 0xAAAAAA);
    }

    // User-requested modernization: unlike the old GUI, inventory/E never closes this screen.
    @Override public boolean keyPressed(int key, int scan, int mods) {
        if (key == GLFW.GLFW_KEY_E || InputConstants.getKey(key,scan).getValue() == GLFW.GLFW_KEY_E) {
            if (skin != null && skin.isFocused()) return skin.keyPressed(key,scan,mods);
            return true;
        }
        return super.keyPressed(key,scan,mods);
    }
    @Override public boolean charTyped(char c, int mods) {
        if (skin != null && skin.isFocused() && skin.charTyped(c,mods)) return true;
        return super.charTyped(c,mods);
    }

    private interface Setter { void set(float u, float v); }
    private static class PosePad extends AbstractWidget {
        private float u,v; private final Setter setter; private final String label;
        PosePad(int x,int y,int w,int h,String label,float u,float v,Setter setter) {
            super(x,y,w,h,Component.literal(label)); this.label=label; this.u=u; this.v=v; this.setter=setter;
        }
        void setSilently(float u,float v){this.u=u;this.v=v;}
        private void update(double mx,double my){u=(float)Math.max(0,Math.min(1,(mx-getX())/(double)width));v=(float)Math.max(0,Math.min(1,(my-getY())/(double)height));setter.set(u,v);}
        @Override public void onClick(double mx,double my){update(mx,my);} @Override protected void onDrag(double mx,double my,double dx,double dy){update(mx,my);}
        @Override protected void renderWidget(GuiGraphics g,int mx,int my,float pt){
            g.fill(getX(),getY(),getX()+width,getY()+height,0xFF101010);
            g.hLine(getX(),getX()+width,getY()+height/2,0xFF626262); g.vLine(getX()+width/2,getY(),getY()+height,0xFF626262);
            int px=getX()+(int)(u*(width-1)),py=getY()+(int)(v*(height-1)); g.fill(px-3,py-3,px+4,py+4,0xFFFFFFFF);
            g.drawCenteredString(Minecraft.getInstance().font,label,getX()+width/2,getY()-11,0xEEEEEE);
        }
        @Override protected void updateWidgetNarration(NarrationElementOutput out){out.add(NarratedElementType.TITLE,getMessage());}
    }
}
