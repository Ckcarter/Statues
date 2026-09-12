package che.statues20.blockentity;

import che.statues20.Statues20;
import che.statues20.menu.ShowcaseMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** One-slot showcase inventory with the original chest-like lid timing and viewer count. */
public class ShowcaseBlockEntity extends BlockEntity implements Container, MenuProvider {
    private NonNullList<ItemStack> items=NonNullList.withSize(1,ItemStack.EMPTY);
    public float lidAngle, prevLidAngle;
    private int users;

    public ShowcaseBlockEntity(BlockPos p,BlockState s){super(Statues20.SHOWCASE_BE.get(),p,s);}

    public static void tick(Level level,BlockPos pos,BlockState state,ShowcaseBlockEntity be){
        be.prevLidAngle=be.lidAngle;
        float previous=be.lidAngle;
        if(be.users>0 && be.lidAngle==0 && !level.isClientSide)
            level.playSound(null,pos,SoundEvents.CHEST_OPEN,SoundSource.BLOCKS,.5F,.9F+level.random.nextFloat()*.1F);
        if((be.users==0&&be.lidAngle>0)||(be.users>0&&be.lidAngle<1)) {
            be.lidAngle += be.users>0 ? .1F : -.1F;
            be.lidAngle=Math.max(0,Math.min(1,be.lidAngle));
            if(be.lidAngle<.5F && previous>=.5F && !level.isClientSide)
                level.playSound(null,pos,SoundEvents.CHEST_CLOSE,SoundSource.BLOCKS,.5F,.9F+level.random.nextFloat()*.1F);
        }
    }

    private void sync(){setChanged();if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    @Override protected void saveAdditional(CompoundTag t){super.saveAdditional(t);ContainerHelper.saveAllItems(t,items);t.putInt("Users",users);}
    @Override public void load(CompoundTag t){super.load(t);items=NonNullList.withSize(1,ItemStack.EMPTY);ContainerHelper.loadAllItems(t,items);users=t.getInt("Users");}
    @Override public CompoundTag getUpdateTag(){CompoundTag t=new CompoundTag();saveAdditional(t);return t;}
    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    @Override public void onDataPacket(Connection c,ClientboundBlockEntityDataPacket p){if(p.getTag()!=null)load(p.getTag());}
    @Override public int getContainerSize(){return 1;} @Override public boolean isEmpty(){return items.get(0).isEmpty();}
    @Override public ItemStack getItem(int i){return items.get(i);}
    @Override public ItemStack removeItem(int i,int a){ItemStack s=ContainerHelper.removeItem(items,i,a);sync();return s;}
    @Override public ItemStack removeItemNoUpdate(int i){return ContainerHelper.takeItem(items,i);}
    @Override public void setItem(int i,ItemStack s){items.set(i,s.copyWithCount(Math.min(1,s.getCount())));sync();}
    @Override public boolean stillValid(Player p){return Container.stillValidBlockEntity(this,p);} @Override public void clearContent(){items.clear();sync();}
    @Override public void startOpen(Player p){if(!p.isSpectator()){users++;sync();}} @Override public void stopOpen(Player p){if(!p.isSpectator()){users=Math.max(0,users-1);sync();}}
    @Override public Component getDisplayName(){return Component.translatable("container.statues20.showcase");}
    @Nullable @Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new ShowcaseMenu(id,inv,this);}
}
