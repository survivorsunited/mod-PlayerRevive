package team.creative.playerrevive;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.util.Identifier;
import team.creative.playerrevive.packet.GiveUpPacket;
import team.creative.playerrevive.server.PlayerReviveServer;

public class ReviveClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            world.getClientWorld().waitForChunksRender();
            world.getServer().runCommand("gamemode survival @a");
            world.getServer().runOnServer(server -> {
                PlayerReviveFabric.CONFIG.bleedInSingleplayer = true;
                var player = server.getPlayerManager().getPlayerList().getFirst();
                player.damage(player.getEntityWorld(), player.getDamageSources().outOfWorld(), 100);
            });
            context.waitFor(client -> client.player != null && PlayerReviveServer.isBleeding(client.player));
            context.waitTicks(10);
            context.runOnClient(client -> {
                if (!Identifier.of(PlayerReviveFabric.MODID, "blur").equals(client.gameRenderer.getPostProcessorId()))
                    throw new AssertionError("Downed player must have the blur effect");
                if (!client.player.isAlive()) throw new AssertionError("Client must remain alive while downed");
            });
            context.takeScreenshot("playerrevive-downed");
            world.getServer().runCommand("revive @a[bleeding=true]");
            context.waitFor(client -> !PlayerReviveServer.isBleeding(client.player));
            context.waitTicks(5);
            context.runOnClient(client -> {
                if (client.gameRenderer.getPostProcessorId() != null) throw new AssertionError("Revive must clear the blur effect");
            });
            world.getServer().runOnServer(server -> {
                var player = server.getPlayerManager().getPlayerList().getFirst();
                PlayerReviveServer.startBleeding(player, player.getDamageSources().generic());
            });
            context.waitFor(client -> PlayerReviveServer.isBleeding(client.player));
            context.runOnClient(client -> PlayerReviveFabric.NETWORK.sendToServer(new GiveUpPacket()));
            context.waitFor(client -> !client.player.isAlive());
            System.out.println("PLAYERREVIVE_CLIENT_TEST_PASS");
        }
    }
}
