package che.statues20.network;
import che.statues20.Statues20;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
public final class ModNetwork {
 private static final String PROTOCOL="3"; public static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation(Statues20.MODID,"main"),()->PROTOCOL,PROTOCOL::equals,PROTOCOL::equals); private ModNetwork(){}
 public static void register(){int id=0;CHANNEL.registerMessage(id++,UpdateStatuePacket.class,UpdateStatuePacket::encode,UpdateStatuePacket::decode,UpdateStatuePacket::handle);CHANNEL.registerMessage(id++,CreateStatuePacket.class,CreateStatuePacket::encode,CreateStatuePacket::decode,CreateStatuePacket::handle);CHANNEL.registerMessage(id,UpdatePosePacket.class,UpdatePosePacket::encode,UpdatePosePacket::decode,UpdatePosePacket::handle);}
}
