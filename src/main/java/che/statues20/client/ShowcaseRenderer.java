package che.statues20.client;

import che.statues20.Statues20;
import che.statues20.block.ShowcaseBlock;
import che.statues20.blockentity.ShowcaseBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

/** Custom one-item glass display case renderer ported from RenderShowcase. */
public class ShowcaseRenderer implements BlockEntityRenderer<ShowcaseBlockEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Statues20.MODID,"textures/block/showcase.png");
    private final ShowcaseModel model;
    public ShowcaseRenderer(BlockEntityRendererProvider.Context context) { model = new ShowcaseModel(context.bakeLayer(ShowcaseModel.LAYER)); }

    @Override public void render(ShowcaseBlockEntity be,float partialTick,PoseStack ps,MultiBufferSource buffers,int light,int overlay) {
        float yaw = be.getBlockState().getValue(ShowcaseBlock.FACING).toYRot();
        ps.pushPose();
        ps.translate(.5,1.5,.5);
        ps.mulPose(Axis.YP.rotationDegrees(-yaw));
        ps.scale(1,-1,-1);

        float open = Mth.lerp(partialTick, be.prevLidAngle, be.lidAngle);
        float eased = 1.1F - open;
        eased = 1.1F - eased * eased * eased; // exact cubic easing used by the old renderer
        model.lid.xRot = -(eased * (float)Math.PI / 2.0F);
        VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucent(TEXTURE));
        model.render(ps,vc,light,OverlayTexture.NO_OVERLAY);
        ps.popPose();

        renderItem(be,partialTick,ps,buffers,light,overlay,yaw);
    }

    private void renderItem(ShowcaseBlockEntity be,float partialTick,PoseStack ps,MultiBufferSource buffers,int light,int overlay,float yaw) {
        ItemStack stack = be.getItem(0); if(stack.isEmpty()) return;
        ps.pushPose();
        ps.translate(.5,.915,.5);
        ps.mulPose(Axis.YP.rotationDegrees(-yaw));
        if(stack.getItem() instanceof BlockItem) {
            ps.scale(1.2F,1.2F,1.2F); ps.mulPose(Axis.XP.rotationDegrees(-20.33F));
        } else {
            ps.scale(1.5F,1.5F,1.5F); ps.translate(0,-.10,-.22); ps.mulPose(Axis.XP.rotationDegrees(69.67F));
            if(stack.getItem() instanceof SwordItem) { ps.translate(.15,.05,0); ps.mulPose(Axis.ZP.rotationDegrees(45)); }
        }
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, ps, buffers, null, 0);
        ps.popPose();
    }
}
