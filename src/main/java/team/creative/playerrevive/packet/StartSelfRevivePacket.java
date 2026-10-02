package team.creative.playerrevive.packet;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.entity.player.PlayerEntity;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.playerrevive.PlayerReviveFabric;
import team.creative.playerrevive.api.IBleeding;
import team.creative.playerrevive.server.PlayerReviveServer;

public class StartSelfRevivePacket extends CreativePacket {

    @Override
    public void executeClient(PlayerEntity player) {}

    @Override
    public void executeServer(ServerPlayerEntity player) {
        if (!PlayerReviveFabric.CONFIG.revive.selfRevive.enabled)
            return;

        IBleeding bleeding = PlayerReviveServer.getBleeding(player);
        if (!player.isAlive() || !bleeding.isBleeding() || bleeding.isSelfReviving())
            return;

        boolean consumed = false;
        if (PlayerReviveFabric.CONFIG.revive.selfRevive.item.is(player.getEntityWorld(), player.getMainHandStack()) && player.getMainHandStack()
                .getCount() >= PlayerReviveFabric.CONFIG.revive.selfRevive.itemCount) {
            if (PlayerReviveFabric.CONFIG.revive.selfRevive.consumeItem) {
                player.getMainHandStack().decrement(PlayerReviveFabric.CONFIG.revive.selfRevive.itemCount);
                player.getInventory().markDirty();
            }
            consumed = true;
        }

        if (!consumed && PlayerReviveFabric.CONFIG.revive.selfRevive.item.is(player.getEntityWorld(), player.getOffHandStack()) && player.getOffHandStack()
                .getCount() >= PlayerReviveFabric.CONFIG.revive.selfRevive.itemCount) {
            if (PlayerReviveFabric.CONFIG.revive.selfRevive.consumeItem) {
                player.getOffHandStack().decrement(PlayerReviveFabric.CONFIG.revive.selfRevive.itemCount);
                player.getInventory().markDirty();
            }
            consumed = true;
        }

        if (!consumed)
            return;

        bleeding.startSelfRevive();
        PlayerReviveFabric.CONFIG.revive.selfRevive.sound.play(player, SoundCategory.PLAYERS);
    }

}
