package team.creative.playerrevive;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.world.GameMode;
import team.creative.playerrevive.cap.Bleeding;
import team.creative.playerrevive.server.PlayerReviveServer;

public class ReviveGameTests {
    private ServerPlayerEntity player(TestContext context) {
        var profile = new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "revive-test");
        var data = net.minecraft.server.network.ConnectedClientData.createDefault(profile, false);
        ServerPlayerEntity player = new ServerPlayerEntity(context.getWorld().getServer(), context.getWorld(), profile, data.syncedOptions());
        var connection = new net.minecraft.network.ClientConnection(net.minecraft.network.NetworkSide.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        context.getWorld().getServer().getPlayerManager().onPlayerConnect(connection, player, data);
        player.networkHandler.onPlayerLoaded(new net.minecraft.network.packet.c2s.play.PlayerLoadedC2SPacket());
        player.changeGameMode(GameMode.SURVIVAL);
        player.setHealth(20);
        // GameTest uses a local test server. Enable the same mechanics as a
        // multiplayer server without waiting for the login damage grace period.
        PlayerReviveFabric.CONFIG.bleedInSingleplayer = true;
        return player;
    }

    @GameTest
    public void lethalDamageStartsBleedingAndReviveRestoresHealth(TestContext context) {
        ServerPlayerEntity player = player(context);
        boolean accepted = player.damage(context.getWorld(), player.getDamageSources().outOfWorld(), 100);
        context.assertTrue(player.isAlive() && PlayerReviveServer.isBleeding(player), "Lethal damage must down the player without killing them: health=" + player.getHealth()
                + " alive=" + player.isAlive() + " bleeding=" + PlayerReviveServer.isBleeding(player) + " accepted=" + accepted);
        context.assertTrue(player.getDataTracker().get(PlayerReviveServer.BLEEDING_TRACKER), "Downed state must be tracked");
        PlayerReviveServer.revive(player);
        context.assertFalse(PlayerReviveServer.isBleeding(player), "Revive must clear bleeding");
        context.assertEquals(player.getHealth(), (float)PlayerReviveFabric.CONFIG.revive.healthAfter, "Revive health");
        context.assertFalse(player.getDataTracker().get(PlayerReviveServer.BLEEDING_TRACKER), "Revive must clear tracked state");
        context.complete();
    }

    @GameTest
    public void bleedOutReallyKillsPlayer(TestContext context) {
        ServerPlayerEntity player = player(context);
        PlayerReviveServer.startBleeding(player, player.getDamageSources().generic());
        PlayerReviveServer.kill(player);
        context.assertFalse(player.isAlive(), "Bleeding expiry must cause real death");
        context.assertFalse(player.getDataTracker().get(PlayerReviveServer.BLEEDING_TRACKER), "Death must clear tracked state");
        context.complete();
    }

    @GameTest
    public void savedStatePreservesCountdownAndConsumedItem(TestContext context) {
        ServerPlayerEntity player = player(context);
        Bleeding bleeding = (Bleeding)PlayerReviveServer.getBleeding(player);
        bleeding.knockOut(player, player.getDamageSources().generic());
        bleeding.setItemConsumed();
        bleeding.tick(player);
        Bleeding restored = new Bleeding();
        var write = net.minecraft.storage.NbtWriteView.create(net.minecraft.util.ErrorReporter.EMPTY, player.getRegistryManager());
        player.writeData(write);
        bleeding.revive(player);
        player.readData(net.minecraft.storage.NbtReadView.create(net.minecraft.util.ErrorReporter.EMPTY, player.getRegistryManager(), write.getNbt()));
        restored = (Bleeding)PlayerReviveServer.getBleeding(player);
        context.assertTrue(restored.isBleeding() && restored.isItemConsumed(), "Reload must preserve bleeding and item consumption");
        context.assertEquals(restored.timeLeft(), PlayerReviveFabric.CONFIG.bleeding.bleedTime - 1, "Countdown must survive reload");
        context.assertEquals(restored.downedTime(), 1, "Damage grace period must survive reload");
        context.assertTrue(player.getDataTracker().get(PlayerReviveServer.BLEEDING_TRACKER), "Reload must restore tracked state");
        context.assertEquals(player.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.JUMP_STRENGTH), 0.0,
                "Downed players must remain unable to jump after reloading");
        restored.getSource(player.getRegistryManager());
        context.complete();
    }

    @GameTest
    public void anotherPlayerCanHelpAndCancel(TestContext context) {
        ServerPlayerEntity target = player(context);
        ServerPlayerEntity helper = player(context);
        PlayerReviveServer.startBleeding(target, target.getDamageSources().generic());
        net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.invoker().interact(helper, context.getWorld(),
                net.minecraft.util.Hand.MAIN_HAND, target, null);
        var bleeding = PlayerReviveServer.getBleeding(target);
        context.assertTrue(bleeding.revivingPlayers().contains(helper), "Right-click must begin helping");
        bleeding.tick(target);
        context.assertTrue(bleeding.getProgress() > 0, "Helper must advance revival");
        new team.creative.playerrevive.packet.CancelHelpPacket().executeServer(helper);
        context.assertTrue(bleeding.revivingPlayers().isEmpty(), "Release must cancel helping");
        context.complete();
    }
}

