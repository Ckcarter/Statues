package che.statues20.network;

import che.statues20.blockentity.StatueBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record UpdateStatuePacket(BlockPos pos, String skinName) {
    public static void encode(UpdateStatuePacket msg, FriendlyByteBuf buf) { buf.writeBlockPos(msg.pos); buf.writeUtf(msg.skinName, 64); }
    public static UpdateStatuePacket decode(FriendlyByteBuf buf) { return new UpdateStatuePacket(buf.readBlockPos(), buf.readUtf(64)); }
    public static void handle(UpdateStatuePacket msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null && player.distanceToSqr(msg.pos.getX()+0.5, msg.pos.getY()+0.5, msg.pos.getZ()+0.5) < 64) {
                if (player.level().getBlockEntity(msg.pos) instanceof StatueBlockEntity statue) statue.setSkinName(msg.skinName);
            }
        });
        ctx.setPacketHandled(true);
    }
}
