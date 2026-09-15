package che.statues20.client;

import che.statues20.block.StatueBlock;
import che.statues20.blockentity.StatueBlockEntity;
import che.statues20.pose.StatuePose;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 1.20.1 renderer rebuilt from the old RenderPlayerStatue pipeline: exact pose math, materialized
 * player skin, four armor passes, two held items, and the original normalized hand angles.
 */
public class StatueRenderer implements BlockEntityRenderer<StatueBlockEntity> {
    // The original statue occupies a full two-block-high sculpted column.
    // The raw player model spans exactly 32 model pixels (2 blocks) from head top to feet,
    // so a 1.0 scale makes the rendered statue exactly two blocks tall.
    private static final float STATUE_SCALE = 1.0F;
    private final PlayerModel<LivingEntity> classicModel;
    private final PlayerModel<LivingEntity> slimModel;
    private final HumanoidModel<LivingEntity> armorInner;
    private final HumanoidModel<LivingEntity> armorOuter;
    private static final Map<String, ResourceLocation> ARMOR_TEXTURES = new HashMap<>();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public StatueRenderer(BlockEntityRendererProvider.Context context) {
        classicModel = new PlayerModel(context.bakeLayer(ModelLayers.PLAYER), false);
        slimModel = new PlayerModel(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        armorInner = new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR));
        armorOuter = new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));
        preparePlayerModel(classicModel);
        preparePlayerModel(slimModel);
    }

    @Override
    public void render(StatueBlockEntity statue, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        StatuePose pose = statue.getPose();
        PlayerModel<LivingEntity> model = StatueTextureManager.isSlim(statue.getSkinName()) ? slimModel : classicModel;
        preparePlayerModel(model);
        StatuePoseApplier.apply(model, pose);
        float legOffset = StatuePoseApplier.legHeightOffset(model);

        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        float facingYaw = statue.getBlockState().getValue(StatueBlock.FACING).toYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(-facingYaw));

        // Body A/B in the original are whole-statue rotations around Y and X.
        poseStack.mulPose(Axis.YP.rotationDegrees(StatuePoseApplier.bodyYawDegrees(pose)));
        poseStack.mulPose(Axis.XP.rotationDegrees(StatuePoseApplier.bodyPitchDegrees(pose)));

        poseStack.scale(STATUE_SCALE, -STATUE_SCALE, -STATUE_SCALE);
        // Recreates the old bent-leg floor compensation closely in model-space units.
        poseStack.translate(0.0, -1.5 + legOffset * 0.41F, 0.0);

        ResourceLocation skin = StatueTextureManager.texture(statue.getSkinName(), statue.getSourceState());

        // Draw the base skin explicitly so its UVs use the normal PlayerModel parts.
        VertexConsumer baseVertices = buffers.getBuffer(RenderType.entityCutoutNoCull(skin));
        model.head.render(poseStack, baseVertices, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        model.body.render(poseStack, baseVertices, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        model.leftArm.render(poseStack, baseVertices, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        model.rightArm.render(poseStack, baseVertices, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        model.leftLeg.render(poseStack, baseVertices, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        model.rightLeg.render(poseStack, baseVertices, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);

        // The hat is part of the skin, not the armor renderer. Copy the head pose and render
        // the second head layer explicitly. Do the same for the other modern outer layers.
        model.hat.copyFrom(model.head);
        model.jacket.copyFrom(model.body);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftPants.copyFrom(model.leftLeg);
        model.rightPants.copyFrom(model.rightLeg);

        VertexConsumer overlayVertices = buffers.getBuffer(RenderType.entityTranslucent(skin));
        model.hat.render(poseStack, overlayVertices, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        model.jacket.render(poseStack, overlayVertices, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        model.leftSleeve.render(poseStack, overlayVertices, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        model.rightSleeve.render(poseStack, overlayVertices, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        model.leftPants.render(poseStack, overlayVertices, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        model.rightPants.render(poseStack, overlayVertices, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);

        // Restore the complete player model before armor/items copy poses from it.
        preparePlayerModel(model);
        StatuePoseApplier.apply(model, pose);

        renderArmor(statue, model, poseStack, buffers, packedLight);
        renderHeadItem(model, statue.getItem(StatueBlockEntity.HELMET), poseStack, buffers, packedLight);
        renderHeldItem(model, statue.getItem(StatueBlockEntity.MAIN_HAND), true, pose.itemRightA,
                poseStack, buffers, packedLight);
        // Old RenderPlayerStatue passes 1-itemLeftA for the left arm.
        renderHeldItem(model, statue.getItem(StatueBlockEntity.OFF_HAND), false, 1.0F - pose.itemLeftA,
                poseStack, buffers, packedLight);
        poseStack.popPose();
    }



    private static void preparePlayerModel(PlayerModel<?> model) {
        model.setAllVisible(true);
        // PlayerModel outer skin layers are separate model parts. Keep all of them enabled so
        // hats, jackets, sleeves and pants overlays render exactly like a normal player skin.
        model.hat.visible = true;
        model.jacket.visible = true;
        model.leftSleeve.visible = true;
        model.rightSleeve.visible = true;
        model.leftPants.visible = true;
        model.rightPants.visible = true;
    }

    private void renderArmor(StatueBlockEntity statue, PlayerModel<LivingEntity> playerModel, PoseStack ps, MultiBufferSource buffers, int light) {
        renderArmorPiece(playerModel, statue.getItem(StatueBlockEntity.HELMET), EquipmentSlot.HEAD, armorOuter, false, ps, buffers, light);
        renderArmorPiece(playerModel, statue.getItem(StatueBlockEntity.CHEST), EquipmentSlot.CHEST, armorOuter, false, ps, buffers, light);
        renderArmorPiece(playerModel, statue.getItem(StatueBlockEntity.LEGS), EquipmentSlot.LEGS, armorInner, true, ps, buffers, light);
        renderArmorPiece(playerModel, statue.getItem(StatueBlockEntity.BOOTS), EquipmentSlot.FEET, armorOuter, false, ps, buffers, light);
    }

    private void renderArmorPiece(PlayerModel<LivingEntity> playerModel, ItemStack stack, EquipmentSlot slot, HumanoidModel<LivingEntity> baseModel,
                                  boolean inner, PoseStack ps, MultiBufferSource buffers, int light) {
        if (!(stack.getItem() instanceof ArmorItem armor) || armor.getEquipmentSlot() != slot) return;

        StatuePoseApplier.copyToArmor(playerModel, baseModel);
        setArmorVisible(baseModel, slot);
        LivingEntity renderEntity = Minecraft.getInstance().player;
        Model armorModel = renderEntity == null ? baseModel : ForgeHooksClient.getArmorModel(renderEntity, stack, slot, baseModel);

        if (armor instanceof DyeableLeatherItem dyeable) {
            int color = dyeable.getColor(stack);
            float r = ((color >> 16) & 255) / 255.0F;
            float g = ((color >> 8) & 255) / 255.0F;
            float b = (color & 255) / 255.0F;
            renderArmorModel(armorModel, armorTexture(renderEntity, stack, armor, slot, inner, null), stack.hasFoil(), r, g, b, ps, buffers, light);
            renderArmorModel(armorModel, armorTexture(renderEntity, stack, armor, slot, inner, "overlay"), stack.hasFoil(), 1, 1, 1, ps, buffers, light);
        } else {
            renderArmorModel(armorModel, armorTexture(renderEntity, stack, armor, slot, inner, null), stack.hasFoil(), 1, 1, 1, ps, buffers, light);
        }
    }

    private static void setArmorVisible(HumanoidModel<?> armor, EquipmentSlot slot) {
        armor.setAllVisible(false);
        switch (slot) {
            case HEAD -> { armor.head.visible = true; armor.hat.visible = true; }
            case CHEST -> { armor.body.visible = true; armor.rightArm.visible = true; armor.leftArm.visible = true; }
            case LEGS -> { armor.body.visible = true; armor.rightLeg.visible = true; armor.leftLeg.visible = true; }
            case FEET -> { armor.rightLeg.visible = true; armor.leftLeg.visible = true; }
            default -> {}
        }
    }

    private static ResourceLocation armorTexture(LivingEntity entity, ItemStack stack, ArmorItem armor,
                                                 EquipmentSlot slot, boolean inner, String type) {
        String material = armor.getMaterial().getName();
        String domain = "minecraft";
        int colon = material.indexOf(':');
        if (colon >= 0) { domain = material.substring(0, colon); material = material.substring(colon + 1); }
        String path = String.format(Locale.ROOT, "%s:textures/models/armor/%s_layer_%d%s.png",
                domain, material, inner ? 2 : 1, type == null ? "" : "_" + type);
        if (entity != null) path = ForgeHooksClient.getArmorTexture(entity, stack, path, slot, type);
        return ARMOR_TEXTURES.computeIfAbsent(path, ResourceLocation::new);
    }

    private static void renderArmorModel(Model model, ResourceLocation texture, boolean foil,
                                         float r, float g, float b, PoseStack ps,
                                         MultiBufferSource buffers, int light) {
        VertexConsumer vc = ItemRenderer.getArmorFoilBuffer(buffers, RenderType.armorCutoutNoCull(texture), false, foil);
        model.renderToBuffer(ps, vc, light, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
    }

    private void renderHeadItem(PlayerModel<LivingEntity> model, ItemStack stack, PoseStack ps, MultiBufferSource buffers, int light) {
        if (stack.isEmpty() || stack.getItem() instanceof ArmorItem || !(stack.getItem() instanceof BlockItem)) return;
        ps.pushPose();
        model.head.translateAndRotate(ps);
        ps.translate(0.0F, -0.25F, 0.0F);
        ps.mulPose(Axis.YP.rotationDegrees(90));
        ps.scale(0.625F, -0.625F, -0.625F);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, ps, buffers, null, 0);
        ps.popPose();
    }

    private void renderHeldItem(PlayerModel<LivingEntity> model, ItemStack stack, boolean right, float angle, PoseStack ps,
                                MultiBufferSource buffers, int light) {
        if (stack.isEmpty()) return;
        ps.pushPose();
        if (right) model.rightArm.translateAndRotate(ps); else model.leftArm.translateAndRotate(ps);
        ps.translate(right ? -0.0625F : 0.0625F, 0.4375F, 0.0625F);

        // Exact first three transforms from the old renderItemInArm().
        ps.translate(0.0F, 0.0F, -0.35F * (0.5F - Math.abs(0.5F - angle)));
        ps.translate(0.0F, 0.25F * angle, 0.0F);
        ps.mulPose(Axis.XP.rotationDegrees(angle * 180.0F));

        ItemDisplayContext context = right ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        if (stack.getItem() instanceof BlockItem) {
            ps.translate(0.0F, 0.1875F, -0.3125F);
            ps.mulPose(Axis.XP.rotationDegrees(20));
            ps.mulPose(Axis.YP.rotationDegrees(45));
            ps.scale(-0.375F, -0.375F, 0.375F);
            context = ItemDisplayContext.FIXED;
        } else if (stack.getItem() instanceof BowItem) {
            ps.translate(0.0F, 0.125F, 0.3125F);
            ps.mulPose(Axis.YP.rotationDegrees(-20));
            ps.scale(0.625F, -0.625F, 0.625F);
            ps.mulPose(Axis.XP.rotationDegrees(-100));
            ps.mulPose(Axis.YP.rotationDegrees(45));
            context = ItemDisplayContext.FIXED;
        } else {
            ps.translate(0.0F, 0.1875F, 0.0F);
            ps.scale(0.625F, -0.625F, 0.625F);
            ps.mulPose(Axis.XP.rotationDegrees(-100));
            ps.mulPose(Axis.YP.rotationDegrees(45));
        }

        Minecraft.getInstance().getItemRenderer().renderStatic(stack, context, light,
                OverlayTexture.NO_OVERLAY, ps, buffers, null, 0);
        ps.popPose();
    }

    @Override public boolean shouldRenderOffScreen(StatueBlockEntity blockEntity) { return true; }
}
