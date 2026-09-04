package sekelsta.horse_colors.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.Entity;

import sekelsta.horse_colors.entity.AbstractHorseGenetic;

public class CAutobreedPacket {
    public int entityID;
    public boolean allowed;

    public CAutobreedPacket(int entityID, boolean allowed) {
        this.entityID = entityID;
        this.allowed = allowed;
    }

    public void encode(PacketByteBuf buffer) {
        buffer.writeVarInt(this.entityID);
        buffer.writeBoolean(this.allowed);
    }

    public static CAutobreedPacket decode(PacketByteBuf buffer) {
        int id = buffer.readVarInt();
        boolean allowed = buffer.readBoolean();
        return new CAutobreedPacket(id, allowed);
    }

    public static void handle(CAutobreedPacket packet, MinecraftServer server, ServerPlayerEntity sender) {
        server.execute(() -> {
            Entity entity = sender.getWorld().getEntityById(packet.entityID);
            ((AbstractHorseGenetic)entity).setAutobreedable(packet.allowed);
        });
    }
}
