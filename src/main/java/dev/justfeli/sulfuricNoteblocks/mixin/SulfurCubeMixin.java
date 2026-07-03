package dev.justfeli.sulfuricNoteblocks.mixin;

import dev.justfeli.sulfuricNoteblocks.NoteCube;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.cubemob.SulfurCube;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SulfurCube.class)
public abstract class SulfurCubeMixin extends AgeableMob implements NoteCube {

    @Unique
    private static final int NOTE_RANGE = 25;
    @Unique
    private static final double MIN_IMPACT_SPEED = 0.12;
    @Unique
    private static final int IMPACT_COOLDOWN_TICKS = 4;

    @Unique
    private int sulfuric_noteblocks$note = 0;
    @Unique
    private int sulfuric_noteblocks$impactCooldown = 0;
    @Unique
    private boolean sulfuric_noteblocks$wasPowered = false;

    protected SulfurCubeMixin(final EntityType<? extends AgeableMob> entityType, final Level level) {
        super(entityType, level);
    }

    /**
     * Right-clicking a note-block-filled cube with an empty hand cycles its
     * note and previews it, exactly like tuning a placed note block.
     */
    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void sulfuric_noteblocks$tune(final Player player, final InteractionHand hand, final CallbackInfoReturnable<InteractionResult> cir) {
        SulfurCube self = (SulfurCube) (Object) this;
        if (this.isBaby() || self.isPrimed() || !this.sulfuric_noteblocks$hasNoteBlock()) {
            return;
        }

        if (!player.getItemInHand(hand).isEmpty()) {
            return;
        }

        if (!this.level().isClientSide()) {
            this.sulfuric_noteblocks$note = (this.sulfuric_noteblocks$note + 1) % NOTE_RANGE;
            this.sulfuric_noteblocks$playNote(this.sulfuric_noteblocks$baseInstrument());
            player.awardStat(Stats.TUNE_NOTEBLOCK);
        }

        cir.setReturnValue(InteractionResult.SUCCESS);
    }

    /**
     * Being hit plays the note too, mirroring a note block being punched.
     * The archetype knockback still applies, so the cube also flies off.
     */
    @Inject(method = "knockback", at = @At("TAIL"))
    private void sulfuric_noteblocks$noteOnHit(final double power, final double xd, final double zd, final DamageSource source, final float damage, final boolean comesFromEffect, final CallbackInfo ci) {
        if (!this.level().isClientSide() && source.getEntity() != null && this.sulfuric_noteblocks$hasNoteBlock()) {
            this.sulfuric_noteblocks$playNote(this.sulfuric_noteblocks$baseInstrument());
        }
    }

    /**
     * Redstone rising edge plays the note, like a powered note block.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void sulfuric_noteblocks$tickNote(final CallbackInfo ci) {
        if (this.sulfuric_noteblocks$impactCooldown > 0) {
            this.sulfuric_noteblocks$impactCooldown--;
        }

        if (this.level() instanceof ServerLevel serverLevel && this.sulfuric_noteblocks$hasNoteBlock()) {
            boolean powered = serverLevel.getBestOwnOrNeighbourSignal(this.blockPosition()) != 0;
            if (powered && !this.sulfuric_noteblocks$wasPowered) {
                this.sulfuric_noteblocks$playNote(this.sulfuric_noteblocks$baseInstrument());
            }

            this.sulfuric_noteblocks$wasPowered = powered;
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void sulfuric_noteblocks$save(final ValueOutput output, final CallbackInfo ci) {
        output.putInt("sulfuric_noteblocks:note", this.sulfuric_noteblocks$note);
        output.putBoolean("sulfuric_noteblocks:was_powered", this.sulfuric_noteblocks$wasPowered);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void sulfuric_noteblocks$load(final ValueInput input, final CallbackInfo ci) {
        this.sulfuric_noteblocks$note = input.getIntOr("sulfuric_noteblocks:note", 0) % NOTE_RANGE;
        this.sulfuric_noteblocks$wasPowered = input.getBooleanOr("sulfuric_noteblocks:was_powered", false);
    }

    @Override
    public void sulfuric_noteblocks$onImpact(final BlockState effectState, final boolean xCollision, final boolean zCollision) {
        if (this.level().isClientSide() || this.sulfuric_noteblocks$impactCooldown > 0 || !this.sulfuric_noteblocks$hasNoteBlock()) {
            return;
        }

        Vec3 velocity = this.getDeltaMovement();
        BlockState hitState = null;
        double impact = 0.0;

        if (xCollision || zCollision) {
            double impactX = xCollision ? Math.abs(velocity.x) : 0.0;
            double impactZ = zCollision ? Math.abs(velocity.z) : 0.0;
            Direction direction;
            if (impactX >= impactZ) {
                direction = velocity.x > 0.0 ? Direction.EAST : Direction.WEST;
                impact = impactX;
            } else {
                direction = velocity.z > 0.0 ? Direction.SOUTH : Direction.NORTH;
                impact = impactZ;
            }

            BlockPos wallPos = BlockPos.containing(this.getBoundingBox().getCenter()).relative(direction);
            hitState = this.level().getBlockState(wallPos);
            if (hitState.isAir()) {
                hitState = this.level().getBlockState(this.blockPosition().relative(direction));
            }
        }

        if (this.verticalCollision) {
            double impactY = Math.abs(velocity.y);
            if (impactY > impact) {
                impact = impactY;
                if (velocity.y <= 0.0) {
                    hitState = effectState;
                } else {
                    BlockPos ceilingPos = BlockPos.containing(this.getX(), this.getBoundingBox().maxY + 0.1, this.getZ());
                    hitState = this.level().getBlockState(ceilingPos);
                }
            }
        }

        if (hitState == null || hitState.isAir() || impact < MIN_IMPACT_SPEED) {
            return;
        }

        this.sulfuric_noteblocks$impactCooldown = IMPACT_COOLDOWN_TICKS;
        this.sulfuric_noteblocks$playNote(this.sulfuric_noteblocks$instrumentOf(hitState));
    }

    @Unique
    private boolean sulfuric_noteblocks$hasNoteBlock() {
        return this.getItemBySlot(EquipmentSlot.BODY).is(Items.NOTE_BLOCK);
    }

    /**
     * Instrument used for tuning, punching, and redstone: the block below the
     * cube, with the same harp fallback a placed note block uses.
     */
    @Unique
    private NoteBlockInstrument sulfuric_noteblocks$baseInstrument() {
        return this.sulfuric_noteblocks$instrumentOf(this.level().getBlockState(this.blockPosition().below()));
    }

    @Unique
    private NoteBlockInstrument sulfuric_noteblocks$instrumentOf(final BlockState state) {
        NoteBlockInstrument instrument = state.instrument();
        return instrument.hasCustomSound() ? NoteBlockInstrument.HARP : instrument;
    }

    @Unique
    private void sulfuric_noteblocks$playNote(final NoteBlockInstrument instrument) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        float pitch;
        if (instrument.isTunable()) {
            pitch = NoteBlock.getPitchFromNote(this.sulfuric_noteblocks$note);
            serverLevel.sendParticles(
                ParticleTypes.NOTE,
                this.getX(),
                this.getY() + this.getBbHeight() + 0.3,
                this.getZ(),
                0,
                this.sulfuric_noteblocks$note / 24.0,
                0.0,
                0.0,
                1.0
            );
        } else {
            pitch = 1.0F;
        }

        serverLevel.playSeededSound(
            null,
            this.getX(),
            this.getY() + this.getBbHeight() / 2.0,
            this.getZ(),
            instrument.getSoundEvent(),
            SoundSource.RECORDS,
            NoteBlock.NOTE_VOLUME,
            pitch,
            serverLevel.getRandom().nextLong()
        );
        this.gameEvent(GameEvent.NOTE_BLOCK_PLAY);
    }
}
