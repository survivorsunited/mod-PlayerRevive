package team.creative.playerrevive.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.playerrevive.server.PlayerReviveServer;

public class CancelHelpPacket extends CreativePacket {
    @Override public void executeClient(PlayerEntity player) {}
    @Override public void executeServer(ServerPlayerEntity player) {
        PlayerReviveServer.removePlayerAsHelper(player);
        team.creative.playerrevive.PlayerReviveFabric.NETWORK.sendToClient(new HelperPacket(null, false), player);
    }
}
