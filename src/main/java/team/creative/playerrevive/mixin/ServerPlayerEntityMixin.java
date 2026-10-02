package team.creative.playerrevive.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import team.creative.playerrevive.PlayerReviveFabric;
import team.creative.playerrevive.api.BleedingHolder;
import team.creative.playerrevive.api.PlayerExtender;
import team.creative.playerrevive.cap.Bleeding;
import team.creative.playerrevive.server.PlayerReviveServer;
import team.creative.playerrevive.server.ReviveEventServer;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements PlayerExtender {

    @Unique
    private float overkill;

    @Inject(method = "getPermissions", at = @At("HEAD"), cancellable = true)
    public void getPermissions(CallbackInfoReturnable<net.minecraft.command.permission.PermissionPredicate> callback) {
        if (PlayerReviveFabric.CONFIG.bleeding.changePermissionLevel && PlayerReviveServer.isBleeding((ServerPlayerEntity) (Object) this))
            callback.setReturnValue(net.minecraft.command.permission.LeveledPermissionPredicate.fromLevel(
                    net.minecraft.command.permission.PermissionLevel.fromLevel(PlayerReviveFabric.CONFIG.bleeding.permissionLevel)));
    }

    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    private void onDie(DamageSource source, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (ReviveEventServer.onPlayerDeath(player, source)) {
            ci.cancel();
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamageHead(net.minecraft.server.world.ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (ReviveEventServer.onPlayerDamaged(player, source)) {
            cir.setReturnValue(false);
        }
    }

    // Save bleeding state to NBT (like NeoForge AttachmentType.serializable)
    @Inject(method = "writeCustomData", at = @At("TAIL"))
    private void onSave(net.minecraft.storage.WriteView view, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        Bleeding bleeding = ((BleedingHolder) player).playerrevive$getBleeding();
        if (bleeding != null && bleeding.isBleeding()) {
            view.put("playerrevive:bleeding", NbtCompound.CODEC, bleeding.serializeNBT());
        }
    }

    // Restore bleeding state from NBT (like NeoForge AttachmentType.serializable)
    @Inject(method = "readCustomData", at = @At("TAIL"))
    private void onLoad(net.minecraft.storage.ReadView view, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        view.read("playerrevive:bleeding", NbtCompound.CODEC).ifPresent(nbt -> {
            Bleeding bleeding = new Bleeding();
            bleeding.deserializeNBT(nbt);
            ((BleedingHolder) player).playerrevive$setBleeding(bleeding);
            bleeding.restoreEffects(player);
            player.getDataTracker().set(PlayerReviveServer.BLEEDING_TRACKER, bleeding.isBleeding());
        });
    }

    @Override
    public float getOverkill() {
        return overkill;
    }

    @Override
    public void setOverkill(float overkill) {
        this.overkill = overkill;
    }

}
