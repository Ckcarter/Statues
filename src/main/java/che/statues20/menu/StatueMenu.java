package che.statues20.menu;

import che.statues20.Statues20;
import che.statues20.blockentity.StatueBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Original ContainerStatue slot layout, horizontally centered inside the modern 256px screen. */
public class StatueMenu extends AbstractContainerMenu {
    public final Container container;
    public final net.minecraft.core.BlockPos pos;
    private static final int OX = 40;

    public StatueMenu(int id,Inventory inv,FriendlyByteBuf buf){this(id,inv,fromNetwork(inv,buf));}
    private static Container fromNetwork(Inventory inv,FriendlyByteBuf buf){
        net.minecraft.core.BlockPos p=buf.readBlockPos(); BlockEntity be=inv.player.level().getBlockEntity(p);
        return be instanceof StatueBlockEntity s?s:new SimpleContainer(6);
    }
    public StatueMenu(int id,Inventory inv,Container container){
        super(Statues20.STATUE_MENU.get(),id); this.container=container;
        this.pos=container instanceof StatueBlockEntity s?s.getBlockPos():net.minecraft.core.BlockPos.ZERO;
        checkContainerSize(container,6); container.startOpen(inv.player);

        addSlot(new ArmorSlot(container,StatueBlockEntity.HELMET,OX+80,33,EquipmentSlot.HEAD));
        addSlot(new ArmorSlot(container,StatueBlockEntity.CHEST, OX+80,51,EquipmentSlot.CHEST));
        addSlot(new ArmorSlot(container,StatueBlockEntity.LEGS,  OX+80,69,EquipmentSlot.LEGS));
        addSlot(new ArmorSlot(container,StatueBlockEntity.BOOTS, OX+80,87,EquipmentSlot.FEET));
        addSlot(new Slot(container,StatueBlockEntity.MAIN_HAND,OX+111,51){@Override public int getMaxStackSize(){return 1;}});
        addSlot(new Slot(container,StatueBlockEntity.OFF_HAND, OX+49, 51){@Override public int getMaxStackSize(){return 1;}});

        for(int r=0;r<3;r++)for(int c=0;c<9;c++)addSlot(new Slot(inv,c+r*9+9,48+c*18,144+r*18));
        for(int c=0;c<9;c++)addSlot(new Slot(inv,c,48+c*18,202));
    }
    @Override public boolean stillValid(Player p){return container.stillValid(p);} @Override public void removed(Player p){super.removed(p);container.stopOpen(p);}
    @Override public ItemStack quickMoveStack(Player player,int index){
        ItemStack result=ItemStack.EMPTY; Slot slot=slots.get(index); if(!slot.hasItem())return result;
        ItemStack stack=slot.getItem(); result=stack.copy();
        if(index<6){if(!moveItemStackTo(stack,6,slots.size(),true))return ItemStack.EMPTY;}
        else {
            int target=preferredStatueSlot(stack);
            if(target>=0){if(!moveItemStackTo(stack,target,target+1,false))return ItemStack.EMPTY;}
            else if(!moveItemStackTo(stack,4,6,false))return ItemStack.EMPTY;
        }
        if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();return result;
    }
    private int preferredStatueSlot(ItemStack stack){
        if(stack.getItem() instanceof ArmorItem a){return switch(a.getEquipmentSlot()){case HEAD->0;case CHEST->1;case LEGS->2;case FEET->3;default->-1;};}
        if(stack.is(Blocks.CARVED_PUMPKIN.asItem()))return 0; return -1;
    }
    private static class ArmorSlot extends Slot{
        private final EquipmentSlot type; ArmorSlot(Container c,int i,int x,int y,EquipmentSlot type){super(c,i,x,y);this.type=type;}
        @Override public boolean mayPlace(ItemStack stack){if(type==EquipmentSlot.HEAD&&stack.is(Blocks.CARVED_PUMPKIN.asItem()))return true;return stack.getItem() instanceof ArmorItem a&&a.getEquipmentSlot()==type;}
        @Override public int getMaxStackSize(){return 1;}
    }
}
