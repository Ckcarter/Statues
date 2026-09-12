package che.statues20.network;
import che.statues20.blockentity.StatueBlockEntity;
import che.statues20.pose.StatuePose;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public record UpdatePosePacket(BlockPos pos, StatuePose pose){
 public static void encode(UpdatePosePacket m,FriendlyByteBuf b){b.writeBlockPos(m.pos);m.pose.write(b);} public static UpdatePosePacket decode(FriendlyByteBuf b){return new UpdatePosePacket(b.readBlockPos(),StatuePose.read(b));}
 public static void handle(UpdatePosePacket m,Supplier<NetworkEvent.Context>s){NetworkEvent.Context c=s.get();c.enqueueWork(()->{ServerPlayer p=c.getSender();if(p!=null&&p.distanceToSqr(m.pos.getX()+.5,m.pos.getY()+.5,m.pos.getZ()+.5)<100&&p.level().getBlockEntity(m.pos) instanceof StatueBlockEntity be)be.setPose(m.pose);});c.setPacketHandled(true);}
}
