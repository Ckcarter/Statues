package che.statues20.client;

import che.statues20.Statues20;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/** Exact box dimensions transcribed from the old Techne ModelShowcase (128x64). */
public class ShowcaseModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(Statues20.MODID, "showcase"), "main");
    private static final float BASE_ANGLE = 0.3548836F;
    private final ModelPart root;
    public final ModelPart lid;

    public ShowcaseModel(ModelPart root) { this.root = root; this.lid = root.getChild("lid"); }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape1", CubeListBuilder.create().texOffs(0,42).addBox(-1,0,0,1,12,2), PartPose.offset(16,12,-8));
        root.addOrReplaceChild("shape2", CubeListBuilder.create().texOffs(0,42).addBox(0,0,0,1,12,2), PartPose.offset(-16,12,-8));
        root.addOrReplaceChild("shape3", CubeListBuilder.create().texOffs(6,42).addBox(0,0,-2,1,16,2), PartPose.offset(-16,8,8));
        root.addOrReplaceChild("shape4", CubeListBuilder.create().texOffs(6,42).addBox(-1,0,-2,1,16,2), PartPose.offset(16,8,8));
        root.addOrReplaceChild("below", CubeListBuilder.create().texOffs(0,0).addBox(0,-6,0,32,6,17), PartPose.offsetAndRotation(-16,14,-8,BASE_ANGLE,0,0));
        root.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0,23).addBox(0,-2,-17,32,2,17), PartPose.offsetAndRotation(-16,2.5F,6,BASE_ANGLE,0,0));
        root.addOrReplaceChild("cradle", CubeListBuilder.create().texOffs(12,42).addBox(-15,-2,0,30,1,15), PartPose.offsetAndRotation(0,14.5F,-6.666667F,BASE_ANGLE,0,0));
        return LayerDefinition.create(mesh,128,64);
    }

    public void render(PoseStack ps, VertexConsumer vc, int light, int overlay) {
        root.render(ps,vc,light,overlay,1,1,1,1);
    }
}
