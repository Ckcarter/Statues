package che.statues20.menu;

import che.statues20.Statues20;
import che.statues20.blockentity.ShowcaseBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Slot coordinates match the original 256x226 showcase GUI. */
public class ShowcaseMenu extends AbstractContainerMenu {
    public final Container container;
    public ShowcaseMenu(int id,Inventory inv,FriendlyByteBuf b){this(id,inv,from(inv,b));}
    private static Container from(Inventory i,FriendlyByteBuf b){BlockEntity e=i.player.level().getBlockEntity(b.readBlockPos());return e instanceof ShowcaseBlockEntity s?s:new SimpleContainer(1);}
    public ShowcaseMenu(int id,Inventory inv,Container c){
        super(Statues20.SHOWCASE_MENU.get(),id); container=c; checkContainerSize(c,1); c.startOpen(inv.player);
        addSlot(new Slot(c,0,120,59){@Override public int getMaxStackSize(){return 1;}});
        for(int r=0;r<3;r++)for(int x=0;x<9;x++)addSlot(new Slot(inv,x+r*9+9,48+x*18,144+r*18));
        for(int x=0;x<9;x++)addSlot(new Slot(inv,x,48+x*18,202));
    }
    @Override public boolean stillValid(Player p){return container.stillValid(p);}
    @Override public void removed(Player p){super.removed(p);container.stopOpen(p);}
    @Override public ItemStack quickMoveStack(Player p,int i){ItemStack out=ItemStack.EMPTY;Slot s=slots.get(i);if(s.hasItem()){ItemStack st=s.getItem();out=st.copy();if(i==0){if(!moveItemStackTo(st,1,slots.size(),true))return ItemStack.EMPTY;}else if(!moveItemStackTo(st,0,1,false))return ItemStack.EMPTY;if(st.isEmpty())s.set(ItemStack.EMPTY);else s.setChanged();}return out;}
}
