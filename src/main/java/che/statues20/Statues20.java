package che.statues20;

import che.statues20.block.*;
import che.statues20.blockentity.*;
import che.statues20.item.*;
import che.statues20.menu.*;
import che.statues20.network.ModNetwork;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;

@Mod(Statues20.MODID)
public class Statues20 {
 public static final String MODID="statues20";
 public static final DeferredRegister<Block> BLOCKS=DeferredRegister.create(ForgeRegistries.BLOCKS,MODID); public static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,MODID); public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES=DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES,MODID); public static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(ForgeRegistries.MENU_TYPES,MODID); public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,MODID); public static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(ForgeRegistries.SOUND_EVENTS,MODID);
 public static final RegistryObject<Block> STATUE=BLOCKS.register("statue",()->new StatueBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1f).noOcclusion()));
 public static final RegistryObject<Block> SHOWCASE=BLOCKS.register("showcase",()->new ShowcaseBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1f).noOcclusion()));
 public static final RegistryObject<Item> SHOWCASE_ITEM=ITEMS.register("showcase",()->new BlockItem(SHOWCASE.get(),new Item.Properties()));
 public static final RegistryObject<Item> HAMMER=ITEMS.register("sculpting_hammer",()->new SculptingHammerItem(new Item.Properties())); public static final RegistryObject<Item> PALETTE=ITEMS.register("palette",()->new PaletteItem(new Item.Properties()));
 public static final RegistryObject<SoundEvent> COPY_SOUND=SOUNDS.register("copy",()->SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID,"copy"))); public static final RegistryObject<SoundEvent> PAINT_SOUND=SOUNDS.register("paint",()->SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID,"paint")));
 public static final RegistryObject<BlockEntityType<StatueBlockEntity>> STATUE_BE=BLOCK_ENTITIES.register("statue",()->BlockEntityType.Builder.of(StatueBlockEntity::new,STATUE.get()).build(null));
 public static final RegistryObject<BlockEntityType<ShowcaseBlockEntity>> SHOWCASE_BE=BLOCK_ENTITIES.register("showcase",()->BlockEntityType.Builder.of(ShowcaseBlockEntity::new,SHOWCASE.get()).build(null));
 public static final RegistryObject<MenuType<StatueMenu>> STATUE_MENU=MENUS.register("statue",()->IForgeMenuType.create(StatueMenu::new)); public static final RegistryObject<MenuType<SculptMenu>> SCULPT_MENU=MENUS.register("sculpt",()->IForgeMenuType.create(SculptMenu::new)); public static final RegistryObject<MenuType<ShowcaseMenu>> SHOWCASE_MENU=MENUS.register("showcase",()->IForgeMenuType.create(ShowcaseMenu::new));
 public static final RegistryObject<CreativeModeTab> STATUES20_TAB=CREATIVE_TABS.register("statues20",()->CreativeModeTab.builder().title(Component.translatable("creativetab.statues20")).icon(()->new ItemStack(HAMMER.get())).displayItems((parameters,output)->{output.accept(HAMMER.get());output.accept(PALETTE.get());output.accept(SHOWCASE_ITEM.get());}).build());
 public Statues20(){IEventBus b=FMLJavaModLoadingContext.get().getModEventBus();BLOCKS.register(b);ITEMS.register(b);BLOCK_ENTITIES.register(b);MENUS.register(b);CREATIVE_TABS.register(b);SOUNDS.register(b);ModNetwork.register();}
 public static boolean canSculpt(BlockState s){
  if(s.isAir()||s.is(Blocks.BEDROCK)||!s.getFluidState().isEmpty()||s.hasBlockEntity())return false;
  Block b=s.getBlock();
  if(b instanceof RedStoneWireBlock||b instanceof DiodeBlock||b instanceof RedstoneTorchBlock||b instanceof LeverBlock||b instanceof ButtonBlock||b instanceof BasePressurePlateBlock||b instanceof TripWireBlock||b instanceof TripWireHookBlock||b instanceof BaseFireBlock)return false;
  return true;
 }
}
