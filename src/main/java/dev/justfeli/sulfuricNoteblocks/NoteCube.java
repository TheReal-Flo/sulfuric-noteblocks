package dev.justfeli.sulfuricNoteblocks;

import net.minecraft.world.level.block.state.BlockState;

/**
 * Duck interface implemented on {@code SulfurCube} via mixin. Lets the generic
 * collision hook in {@code EntityMixin} hand impacts over to the note logic.
 */
public interface NoteCube {

    /**
     * Called just before collision restitution, while the entity's delta
     * movement still holds the pre-bounce impact velocity.
     *
     * @param effectState the block state the entity is standing on / landed on
     * @param xCollision  whether the entity collided along the X axis
     * @param zCollision  whether the entity collided along the Z axis
     */
    void sulfuric_noteblocks$onImpact(BlockState effectState, boolean xCollision, boolean zCollision);
}
