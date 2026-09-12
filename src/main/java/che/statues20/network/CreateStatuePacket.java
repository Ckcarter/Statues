package che.statues20.network;

import che.statues20.Statues20;
import che.statues20.block.StatueBlock;
import che.statues20.blockentity.StatueBlockEntity;
import che.statues20.pose.StatuePose;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record CreateStatuePacket(BlockPos pos, String skin, Direction facing, StatuePose pose) {
    public static void encode(CreateStatuePacket m, FriendlyByteBuf b){ b.writeBlockPos(m.pos); b.writeUtf(m.skin,64); b.writeEnum(m.facing); m.pose.write(b); }
    public static CreateStatuePacket decode(FriendlyByteBuf b){ return new CreateStatuePacket(b.readBlockPos(), b.readUtf(64), b.readEnum(Direction.class), StatuePose.read(b)); }
    public static void handle(CreateStatuePacket m, Supplier<NetworkEvent.Context> sup){
        NetworkEvent.Context c=sup.get(); c.enqueueWork(()->{
            ServerPlayer p=c.getSender(); if(p==null || p.distanceToSqr(m.pos.getX()+.5,m.pos.getY()+.5,m.pos.getZ()+.5)>100) return;
            BlockState source=p.level().getBlockState(m.pos); BlockState above=p.level().getBlockState(m.pos.above());
            if(!source.equals(above) || !Statues20.canSculpt(source)) return;
            
            BlockState lower=Statues20.STATUE.get().defaultBlockState().setValue(StatueBlock.FACING,m.facing).setValue(StatueBlock.HALF, DoubleBlockHalf.LOWER);
            p.level().setBlock(m.pos, lower, 3);
            p.level().setBlock(m.pos.above(), lower.setValue(StatueBlock.HALF,DoubleBlockHalf.UPPER), 3);
            if(p.level().getBlockEntity(m.pos) instanceof StatueBlockEntity be){ be.setSkinName(m.skin); be.setPose(m.pose); be.setSourceState(source); }
            p.level().levelEvent(2001,m.pos,Block.getId(source)); p.level().levelEvent(2001,m.pos.above(),Block.getId(source));
        }); c.setPacketHandled(true);
    }
}
