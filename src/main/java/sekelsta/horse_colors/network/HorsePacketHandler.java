package sekelsta.horse_colors.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import sekelsta.horse_colors.HorseColors;

public class HorsePacketHandler {
    public static final Identifier AUTOBREED_CHANNEL = new Identifier(HorseColors.MOD_ID, "autobreed");

    public static void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(AUTOBREED_CHANNEL, (server, player, handler, buf, responseSender) ->
                CAutobreedPacket.handle(CAutobreedPacket.decode(buf), server, player));
    }

    public static void sendToServer(CAutobreedPacket packet) {
        PacketByteBuf buf = PacketByteBufs.create();
        packet.encode(buf);
        ClientPlayNetworking.send(AUTOBREED_CHANNEL, buf);
    }
}
