package dev.justfeli.sulfuricNoteblocks.mixin;

import dev.justfeli.sulfuricNoteblocks.NoteCube;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    /**
     * Runs only when an entity actually collided this tick, before the bounce
     * restitution rewrites the delta movement — so the impact velocity is
     * still intact for the note cube to inspect.
     */
    @Inject(method = "restituteMovementAfterCollisions", at = @At("HEAD"))
    private void sulfuric_noteblocks$onCollide(final BlockState effectState, final boolean xCollision, final boolean zCollision, final Vec3 movement, final CallbackInfo ci) {
        if ((Object) this instanceof NoteCube noteCube) {
            noteCube.sulfuric_noteblocks$onImpact(effectState, xCollision, zCollision);
        }
    }
}
