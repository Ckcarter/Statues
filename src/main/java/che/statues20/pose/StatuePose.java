package che.statues20.pose;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import java.util.Random;

/** 1.20.1 equivalent of the original StatueParameters class. Values are normalized 0..1. */
public class StatuePose {
    public float armLeftA = 2f/3f, armLeftB = 0f;
    public float armRightA = 2f/3f, armRightB = 0f;
    public float legLeftA = 1f, legLeftB = .5f;
    public float legRightA = 1f, legRightB = .5f;
    public float headA = .5f, headB = .5f;
    public float bodyA = .5f, bodyB = .5f;
    public float itemLeftA = 1f, itemRightA = 0f;

    public StatuePose copy() { StatuePose p = new StatuePose(); p.copyFrom(this); return p; }
    public void copyFrom(StatuePose s) {
        armLeftA=s.armLeftA; armLeftB=s.armLeftB; armRightA=s.armRightA; armRightB=s.armRightB;
        legLeftA=s.legLeftA; legLeftB=s.legLeftB; legRightA=s.legRightA; legRightB=s.legRightB;
        headA=s.headA; headB=s.headB; bodyA=s.bodyA; bodyB=s.bodyB;
        itemLeftA=s.itemLeftA; itemRightA=s.itemRightA;
    }
    public void randomize(Random r) {
        armLeftA=r.nextFloat(); armLeftB=r.nextFloat(); armRightA=r.nextFloat(); armRightB=r.nextFloat();
        legLeftA=r.nextFloat(); legLeftB=r.nextFloat(); legRightA=r.nextFloat(); legRightB=r.nextFloat();
        headA=r.nextFloat(); headB=r.nextFloat(); bodyA=r.nextFloat(); bodyB=r.nextFloat();
    }
    public void save(CompoundTag t) {
        t.putFloat("ala",armLeftA); t.putFloat("alb",armLeftB); t.putFloat("ara",armRightA); t.putFloat("arb",armRightB);
        t.putFloat("lla",legLeftA); t.putFloat("llb",legLeftB); t.putFloat("lra",legRightA); t.putFloat("lrb",legRightB);
        t.putFloat("ha",headA); t.putFloat("hb",headB); t.putFloat("ba",bodyA); t.putFloat("bb",bodyB);
        t.putFloat("ila",itemLeftA); t.putFloat("ira",itemRightA);
    }
    public void load(CompoundTag t) {
        if(!t.contains("ala")) return;
        armLeftA=t.getFloat("ala"); armLeftB=t.getFloat("alb"); armRightA=t.getFloat("ara"); armRightB=t.getFloat("arb");
        legLeftA=t.getFloat("lla"); legLeftB=t.getFloat("llb"); legRightA=t.getFloat("lra"); legRightB=t.getFloat("lrb");
        headA=t.getFloat("ha"); headB=t.getFloat("hb"); bodyA=t.getFloat("ba"); bodyB=t.getFloat("bb");
        itemLeftA=t.getFloat("ila"); itemRightA=t.getFloat("ira");
    }
    public void write(FriendlyByteBuf b) {
        b.writeFloat(armLeftA); b.writeFloat(armLeftB); b.writeFloat(armRightA); b.writeFloat(armRightB);
        b.writeFloat(legLeftA); b.writeFloat(legLeftB); b.writeFloat(legRightA); b.writeFloat(legRightB);
        b.writeFloat(headA); b.writeFloat(headB); b.writeFloat(bodyA); b.writeFloat(bodyB);
        b.writeFloat(itemLeftA); b.writeFloat(itemRightA);
    }
    public static StatuePose read(FriendlyByteBuf b) {
        StatuePose p=new StatuePose();
        p.armLeftA=b.readFloat(); p.armLeftB=b.readFloat(); p.armRightA=b.readFloat(); p.armRightB=b.readFloat();
        p.legLeftA=b.readFloat(); p.legLeftB=b.readFloat(); p.legRightA=b.readFloat(); p.legRightB=b.readFloat();
        p.headA=b.readFloat(); p.headB=b.readFloat(); p.bodyA=b.readFloat(); p.bodyB=b.readFloat();
        p.itemLeftA=b.readFloat(); p.itemRightA=b.readFloat(); return p;
    }
}
