/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.CaseFormat
 *  net.minecraft.client.Minecraft
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.INetHandlerPlayClient
 *  net.minecraft.network.play.client.C02PacketUseEntity
 *  net.minecraft.network.play.client.C02PacketUseEntity$Action
 *  net.minecraft.network.play.client.C0APacketAnimation
 *  net.minecraft.network.play.client.C0BPacketEntityAction
 *  net.minecraft.network.play.client.C0BPacketEntityAction$Action
 *  net.minecraft.network.play.server.S12PacketEntityVelocity
 *  net.minecraft.network.play.server.S19PacketEntityStatus
 *  net.minecraft.network.play.server.S27PacketExplosion
 *  net.minecraft.potion.Potion
 *  net.minecraft.world.World
 *  net.minecraftforge.fml.common.gameevent.TickEvent
 */
package alpeetcclient.module.modules.combat;

import alpeetcclient.AlpeetcClient;
import com.google.common.base.CaseFormat;
import java.util.Objects;
import alpeetcclient.enums.BlinkModules;
import alpeetcclient.enums.DelayModules;
import alpeetcclient.event.EventManager;
import alpeetcclient.event.EventTarget;
import alpeetcclient.event.types.EventType;
import alpeetcclient.events.AttackEvent;
import alpeetcclient.events.HitSlowDownEvent;
import alpeetcclient.events.KnockbackEvent;
import alpeetcclient.events.LivingUpdateEvent;
import alpeetcclient.events.LoadWorldEvent;
import alpeetcclient.events.MoveInputEvent;
import alpeetcclient.events.PacketEvent;
import alpeetcclient.events.UpdateEvent;
import alpeetcclient.mixin.IAccessorEntity;
import alpeetcclient.module.Module;
import alpeetcclient.module.modules.combat.KillAura;
import alpeetcclient.module.modules.movement.LongJump;
import alpeetcclient.module.modules.movement.Stuck;
import alpeetcclient.module.modules.player.KeepSprint;
import alpeetcclient.module.modules.player.Scaffold;
import alpeetcclient.property.properties.BooleanProperty;
import alpeetcclient.property.properties.IntProperty;
import alpeetcclient.property.properties.ModeProperty;
import alpeetcclient.property.properties.PercentProperty;
import alpeetcclient.util.BypassManager;
import alpeetcclient.util.ChatUtil;
import alpeetcclient.util.MoveUtil;
import alpeetcclient.util.PacketUtil;
import alpeetcclient.util.RayCastUtil;
import alpeetcclient.util.RotationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Velocity
extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public final ModeProperty mode = new ModeProperty("Mode", 0, new String[]{"Vanilla", "Prediction", "GrimReduce"});
    public final BooleanProperty reduce = new BooleanProperty("Reduce", true, () -> (Integer)this.mode.getValue() == 1);
    public final ModeProperty reduceMode = new ModeProperty("ReduceMode", 0, new String[]{"Attack", "ReleaseWhenCanAttack", "ReleaseBeforeCanAttack", "Blink"}, () -> (Integer)this.mode.getValue() == 1 && (Boolean)this.reduce.getValue() != false);
    public final IntProperty startBlinkHurtTime = new IntProperty("StartBlinkHurtTime", 1, 0, 10, () -> (Integer)this.mode.getValue() == 1 && (Boolean)this.reduce.getValue() != false && (Integer)this.reduceMode.getValue() == 3);
    public final IntProperty startReleaseTicks = new IntProperty("StartReleaseTicks", 1, 0, 5, () -> (Integer)this.mode.getValue() == 1 && (Boolean)this.reduce.getValue() != false && (Integer)this.reduceMode.getValue() == 3);
    public final BooleanProperty forceBlocking = new BooleanProperty("ForceBlocking", true, () -> (Integer)this.mode.getValue() == 1 && (Boolean)this.reduce.getValue() != false && (Integer)this.reduceMode.getValue() == 3);
    public final IntProperty grimReduceTicks = new IntProperty("Grim Reduce Ticks", 4, 1, 10, () -> (Integer)this.mode.getValue() == 2);
    private final BooleanProperty extraAttack = new BooleanProperty("ExtraAttack", false, () -> (Integer)this.mode.getValue() == 1 && (Boolean)this.reduce.getValue() != false && (Integer)this.reduceMode.getValue() != 0);
    private final BooleanProperty reduceWhenCanAttack = new BooleanProperty("Reduce When Can Attack", true, () -> (Integer)this.mode.getValue() == 1 && (Boolean)this.reduce.getValue() != false && (Integer)this.reduceMode.getValue() == 0);
    public final BooleanProperty cancelKillAuraAttack = new BooleanProperty("CancelKillAuraAttack", false, () -> (Integer)this.mode.getValue() == 1 && (Boolean)this.reduce.getValue() != false && (Integer)this.reduceMode.getValue() == 0);
    private final BooleanProperty onlySprinting = new BooleanProperty("Only Sprinting", true, () -> (Integer)this.mode.getValue() == 1 && (Integer)this.reduceMode.getValue() == 0 && (Boolean)this.reduce.getValue() != false);
    public final BooleanProperty smartTimes = new BooleanProperty("SmartTimes", true, () -> (Integer)this.mode.getValue() == 1 && (Boolean)this.reduce.getValue() != false && (Integer)this.reduceMode.getValue() == 0);
    public final IntProperty attackTimes = new IntProperty("Attack Times", 1, 1, 5, () -> (Integer)this.mode.getValue() == 1 && (Boolean)this.reduce.getValue() != false && (Integer)this.reduceMode.getValue() == 0 && (Boolean)this.smartTimes.getValue() == false);
    public final BooleanProperty keepSprint = new BooleanProperty("KeepSprint", false, () -> (Integer)this.mode.getValue() == 1 && (Boolean)this.reduce.getValue() != false && (Integer)this.reduceMode.getValue() == 0);
    public final BooleanProperty testMode = new BooleanProperty("TestMode", false, () -> (Integer)this.mode.getValue() == 1 && (Boolean)this.reduce.getValue() != false && (Integer)this.reduceMode.getValue() == 0);
    private final IntProperty stopBlockHurtTime = new IntProperty("StopBlockHurtTime", 2, 0, 10, () -> (Integer)this.mode.getValue() == 1 && (Boolean)this.reduce.getValue() != false && (Integer)this.reduceMode.getValue() == 0 && (Boolean)this.testMode.getValue() != false);
    public final BooleanProperty jump = new BooleanProperty("Jump", true, () -> (Integer)this.mode.getValue() == 1 || (Integer)this.mode.getValue() == 2);
    public final BooleanProperty delay = new BooleanProperty("Delay", false, () -> (Integer)this.mode.getValue() == 1 || (Integer)this.mode.getValue() == 2);
    public final IntProperty delayTicks = new IntProperty("Delay Ticks", 1, 1, 5, () -> ((Integer)this.mode.getValue() == 1 || (Integer)this.mode.getValue() == 2) && (Boolean)this.delay.getValue() != false && (Boolean)this.airBuffer.getValue() == false);
    public final BooleanProperty forceDelayRisingToFalling = new BooleanProperty("Force Delay Rising To Falling", false, () -> ((Integer)this.mode.getValue() == 1 || (Integer)this.mode.getValue() == 2) && (Boolean)this.delay.getValue() != false && (Boolean)this.airBuffer.getValue() == false);
    public final BooleanProperty airBuffer = new BooleanProperty("Delay Till On Ground", true, () -> ((Integer)this.mode.getValue() == 1 || (Integer)this.mode.getValue() == 2) && (Boolean)this.delay.getValue() != false);
    public final BooleanProperty groundDelay = new BooleanProperty("Ground Delay", false, () -> ((Integer)this.mode.getValue() == 1 || (Integer)this.mode.getValue() == 2) && (Boolean)this.delay.getValue() != false && (Boolean)this.airBuffer.getValue() == false);
    public final BooleanProperty rotate = new BooleanProperty("Rotate", false, () -> (Integer)this.mode.getValue() == 1);
    public final IntProperty rotateTick = new IntProperty("Rotate Ticks", 3, 1, 12, () -> (Integer)this.mode.getValue() == 1 && (Boolean)this.rotate.getValue() != false);
    public final BooleanProperty autoMove = new BooleanProperty("Auto Move", false, () -> (Integer)this.mode.getValue() == 1 && (Boolean)this.rotate.getValue() != false);
    public final PercentProperty chance = new PercentProperty("Chance", 100, () -> (Integer)this.mode.getValue() == 0);
    public final PercentProperty horizontal = new PercentProperty("Horizontal", 100, () -> (Integer)this.mode.getValue() == 0);
    public final PercentProperty vertical = new PercentProperty("Vertical", 100, () -> (Integer)this.mode.getValue() == 0);
    public final PercentProperty explosionHorizontal = new PercentProperty("Explosions Horizontal", 100, () -> (Integer)this.mode.getValue() == 0);
    public final PercentProperty explosionVertical = new PercentProperty("Explosions Vertical", 100, () -> (Integer)this.mode.getValue() == 0);
    public final BooleanProperty fakeCheck = new BooleanProperty("Fake Check", true);
    public final BooleanProperty debug = new BooleanProperty("Debug", false);
    public final ModeProperty bypassMode = new ModeProperty("BypassMode", 0, new String[]{"None", "GrimAC", "Intave", "Karhu", "NCP", "Artemis", "FairFight", "WatchNeko"});
    public final BooleanProperty grimTransaction = new BooleanProperty("Grim Transaction", false, () -> (Integer)this.bypassMode.getValue() == 1 || (Integer)this.bypassMode.getValue() == 7);
    public final BooleanProperty intavePrediction = new BooleanProperty("Intave Prediction", false, () -> (Integer)this.bypassMode.getValue() == 2);
    public boolean knockback = false;
    private int chanceCounter = 0;
    private int rotateTickCounter = 0;
    private boolean pendingExplosion = false;
    private boolean allowNext = true;
    private boolean delayFlag = false;
    private boolean jumpFlag = false;
    private boolean grimActive = false;
    private int grimTick = 0;
    public static boolean hasReceivedVelocity;
    private int ticksSinceVelocity = -1;
    private double knockbackX = 0.0;
    private float[] targetRotation = null;
    private double knockbackZ = 0.0;
    public int reduceTick = -1;
    public int hitCount;
    public static boolean extraAttacked;
    public static boolean velocityAttacked;
    public static boolean stoppedBlock;
    public static boolean cancellingKillAuraAttack;
    public static boolean blinkActive;
    private boolean blinkingVelocity = false;
    private boolean blinkScheduled = false;
    private int knockbackTimer = -1;

    public Velocity() {
        super("Velocity", false, false);
    }

    private boolean isInLiquidOrWeb() {
        return Velocity.mc.thePlayer.isInWater() || Velocity.mc.thePlayer.isInLava() || ((IAccessorEntity)Velocity.mc.thePlayer).getIsInWeb();
    }

    private int computeReduceTicks(int motionX, int motionZ) {
        double kb = Math.hypot(motionX, motionZ);
        double ticksExact = 6.43153527E-4 * kb + 2.9419087136;
        int ticks = (int)Math.round(ticksExact);
        if (ticks < 1) {
            ticks = 1;
        }
        if (ticks > 10) {
            ticks = 10;
        }
        return ticks;
    }

    @EventTarget
    public void onKnockback(KnockbackEvent event) {
        if (!this.allowNext || !((Boolean)this.fakeCheck.getValue()).booleanValue()) {
            this.allowNext = true;
            if (this.pendingExplosion) {
                if ((Integer)this.mode.getValue() == 0) {
                    this.pendingExplosion = false;
                    if ((Integer)this.explosionHorizontal.getValue() > 0) {
                        event.setX(event.getX() * (double)((Integer)this.explosionHorizontal.getValue()).intValue() / 100.0);
                        event.setZ(event.getZ() * (double)((Integer)this.explosionHorizontal.getValue()).intValue() / 100.0);
                    } else {
                        event.setX(Velocity.mc.thePlayer.motionX);
                        event.setZ(Velocity.mc.thePlayer.motionZ);
                    }
                    if ((Integer)this.explosionVertical.getValue() > 0) {
                        event.setY(event.getY() * (double)((Integer)this.explosionVertical.getValue()).intValue() / 100.0);
                    } else {
                        event.setY(Velocity.mc.thePlayer.motionY);
                    }
                }
            } else {
                if (!this.isEnabled() || event.isCancelled()) {
                    this.pendingExplosion = false;
                    this.allowNext = true;
                    return;
                }
                if ((Integer)this.mode.getValue() == 1 && ((Boolean)this.rotate.getValue()).booleanValue() && event.getY() > 0.0) {
                    this.knockbackX = event.getX();
                    this.knockbackZ = event.getZ();
                    if (Math.abs(this.knockbackX) > 0.01 || Math.abs(this.knockbackZ) > 0.01) {
                        this.rotateTickCounter = 1;
                    }
                }
                if ((Integer)this.mode.getValue() == 1 && ((Boolean)this.smartTimes.getValue()).booleanValue()) {
                    this.hitCount = this.computeReduceTicks((int)event.getX(), (int)event.getZ());
                }
                if (((Boolean)this.delay.getValue()).booleanValue() && !((Boolean)this.groundDelay.getValue()).booleanValue() && Velocity.mc.thePlayer.onGround && event.getY() > 0.0) {
                    if (((Boolean)this.jump.getValue()).booleanValue() && ((Integer)this.mode.getValue() == 1 || (Integer)this.mode.getValue() == 2) && !Velocity.mc.thePlayer.isBurning()) {
                        this.jumpFlag = true;
                    }
                    this.ticksSinceVelocity = 0;
                }
                if (!((Boolean)this.delay.getValue()).booleanValue()) {
                    this.ticksSinceVelocity = 0;
                }
                this.chanceCounter = this.chanceCounter % 100 + (Integer)this.chance.getValue();
                if (this.chanceCounter >= 100 && (Integer)this.mode.getValue() == 0) {
                    if ((Integer)this.horizontal.getValue() > 0) {
                        event.setX(event.getX() * (double)((Integer)this.horizontal.getValue()).intValue() / 100.0);
                        event.setZ(event.getZ() * (double)((Integer)this.horizontal.getValue()).intValue() / 100.0);
                    } else {
                        event.setX(Velocity.mc.thePlayer.motionX);
                        event.setZ(Velocity.mc.thePlayer.motionZ);
                    }
                    if ((Integer)this.vertical.getValue() > 0) {
                        event.setY(event.getY() * (double)((Integer)this.vertical.getValue()).intValue() / 100.0);
                    } else {
                        event.setY(Velocity.mc.thePlayer.motionY);
                    }
                }
                // Bypass mode handling
                int bypassIdx = (Integer)this.bypassMode.getValue();
                if (bypassIdx > 0) {
                    BypassManager.AntiCheat ac = BypassManager.ANTI_CHEATS[bypassIdx];
                    double reducePercent = BypassManager.getVelocityReducePercent(ac);
                    if (reducePercent > 0.0) {
                        event.setX(event.getX() * (1.0 - reducePercent));
                        event.setZ(event.getZ() * (1.0 - reducePercent));
                        event.setY(event.getY() * (1.0 - reducePercent));
                    }
                    if (BypassManager.isTransactionCheckUsed(ac) && (Boolean)this.grimTransaction.getValue()) {
                        BypassManager.currentAC = ac;
                    }
                    if (BypassManager.isPositionSimulationRequired(ac) && (Boolean)this.intavePrediction.getValue()) {
                        BypassManager.currentAC = ac;
                    }
                }
            }
        }
    }

    @EventTarget
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (this.isEnabled() && this.jumpFlag) {
            if (Velocity.mc.thePlayer.onGround && MoveUtil.isForwardPressed() && !Velocity.mc.thePlayer.isPotionActive(Potion.jump) && !this.isInLiquidOrWeb() && Velocity.mc.thePlayer.isSprinting()) {
                Velocity.mc.thePlayer.movementInput.jump = true;
            }
            this.jumpFlag = false;
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled()) {
            if (((Boolean)this.testMode.getValue()).booleanValue() && (Integer)this.mode.getValue() == 1 && ((Boolean)this.reduce.getValue()).booleanValue() && (Integer)this.reduceMode.getValue() == 0 && this.ticksSinceVelocity >= (Integer)this.stopBlockHurtTime.getValue()) {
                hasReceivedVelocity = true;
                stoppedBlock = true;
            }
            if (this.ticksSinceVelocity >= 0) {
                ++this.ticksSinceVelocity;
            }
            if (this.ticksSinceVelocity >= 10) {
                this.ticksSinceVelocity = -1;
            }
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        int maxTick;
        if (!this.isEnabled()) {
            return;
        }
        if (event.getType() == EventType.PRE) {
            cancellingKillAuraAttack = false;
            maxTick = (Integer)this.rotateTick.getValue();
            if (this.rotateTickCounter > 0 && this.rotateTickCounter <= maxTick) {
                if (this.rotateTickCounter == 1) {
                    double deltaX = -this.knockbackX;
                    double deltaZ = -this.knockbackZ;
                    this.targetRotation = RotationUtil.getRotationsTo(deltaX, 0.0, deltaZ, event.getYaw(), event.getPitch());
                }
                if (this.targetRotation != null && !AlpeetcClient.moduleManager.getModule(Scaffold.class).isEnabled()) {
                    event.setRotation(this.targetRotation[0], this.targetRotation[1], 2);
                    event.setPervRotation(this.targetRotation[0], 2);
                }
            }
        }
        if (event.getType() == EventType.PRE) {
            maxTick = (Integer)this.rotateTick.getValue();
            if (this.rotateTickCounter > 0 && this.rotateTickCounter <= maxTick) {
                ++this.rotateTickCounter;
                if (this.rotateTickCounter > maxTick) {
                    this.rotateTickCounter = 0;
                    this.targetRotation = null;
                    this.knockbackX = 0.0;
                    this.knockbackZ = 0.0;
                }
            }
        }
        if ((Integer)this.mode.getValue() == 1) {
            if (((Boolean)this.reduce.getValue()).booleanValue() && (Integer)this.reduceMode.getValue() == 3 && event.getType() == EventType.PRE) {
                if (this.knockbackTimer >= 0) {
                    ++this.knockbackTimer;
                }
                if (this.blinkingVelocity) {
                    if (this.knockbackTimer >= (Integer)this.startReleaseTicks.getValue()) {
                        this.releaseVelocityBlink();
                    }
                } else if (this.knockback && Velocity.mc.thePlayer.hurtTime == (Integer)this.startBlinkHurtTime.getValue()) {
                    if (((Boolean)this.forceBlocking.getValue()).booleanValue()) {
                        KillAura killAura = (KillAura)AlpeetcClient.moduleManager.getModule(KillAura.class);
                        if (killAura != null && killAura.isEnabled() && killAura.isPlayerBlocking()) {
                            this.startVelocityBlink();
                        } else {
                            this.blinkScheduled = true;
                        }
                    } else {
                        this.startVelocityBlink();
                    }
                } else if (this.blinkScheduled) {
                    if (this.knockbackTimer >= (Integer)this.startReleaseTicks.getValue()) {
                        this.blinkScheduled = false;
                        this.knockback = false;
                        this.knockbackTimer = -1;
                    } else {
                        KillAura killAura = (KillAura)AlpeetcClient.moduleManager.getModule(KillAura.class);
                        if (killAura != null && killAura.isEnabled() && killAura.isPlayerBlocking()) {
                            this.startVelocityBlink();
                        }
                    }
                }
            }
            if (((Boolean)this.reduce.getValue()).booleanValue() && (Integer)this.reduceMode.getValue() == 0 && event.getType() == EventType.PRE) {
                if (velocityAttacked) {
                    KillAura killAura = (KillAura)AlpeetcClient.moduleManager.getModule(KillAura.class);
                    if (killAura.getTarget() != null && killAura.isEnabled() && Velocity.mc.thePlayer.isSprinting()) {
                        ChatUtil.sendFormatted("Attack");
                        EventManager.call(new AttackEvent((Entity)killAura.getTarget()));
                        mc.getNetHandler().addToSendQueue((Packet)new C0APacketAnimation());
                        if (killAura.getTarget() != Velocity.mc.thePlayer) {
                            mc.getNetHandler().addToSendQueue((Packet)new C02PacketUseEntity((Entity)killAura.getTarget(), C02PacketUseEntity.Action.ATTACK));
                        } else {
                            mc.getNetHandler().addToSendQueue((Packet)new C02PacketUseEntity((Entity)Objects.requireNonNull(killAura.getTarget()), C02PacketUseEntity.Action.ATTACK));
                        }
                        this.applyHitSlowDown(false);
                    } else {
                        extraAttacked = false;
                    }
                    velocityAttacked = false;
                }
                if (hasReceivedVelocity) {
                    RayCastUtil.RayCastResult targetA;
                    if (((Boolean)this.smartTimes.getValue()).booleanValue()) {
                        if (this.reduceTick >= this.hitCount) {
                            this.reduceTick = 0;
                            hasReceivedVelocity = false;
                            stoppedBlock = false;
                        }
                    } else if (this.reduceTick >= (Integer)this.attackTimes.getValue()) {
                        this.reduceTick = 0;
                        hasReceivedVelocity = false;
                        stoppedBlock = false;
                    }
                    if ((targetA = RayCastUtil.rayCast(new RotationUtil.RotationVec(event.getYaw(), event.getPitch()), 3.0)) != null && (Integer)this.reduceMode.getValue() == 0 && targetA.entityHit instanceof EntityPlayer && targetA.entityHit != Velocity.mc.thePlayer && (Velocity.mc.thePlayer.isSprinting() || !((Boolean)this.onlySprinting.getValue()).booleanValue())) {
                        KillAura killAura = (KillAura)AlpeetcClient.moduleManager.getModule(KillAura.class);
                        if (killAura.getTarget() != null) {
                            if (!((Boolean)this.reduceWhenCanAttack.getValue()).booleanValue() || killAura.velocityCanReduce(0, killAura.blockTick)) {
                                if (((Boolean)this.cancelKillAuraAttack.getValue()).booleanValue()) {
                                    cancellingKillAuraAttack = true;
                                }
                                EventManager.call(new AttackEvent((Entity)killAura.getTarget()));
                                mc.getNetHandler().addToSendQueue((Packet)new C0APacketAnimation());
                                if (killAura.getTarget() != Velocity.mc.thePlayer) {
                                    mc.getNetHandler().addToSendQueue((Packet)new C02PacketUseEntity((Entity)killAura.getTarget(), C02PacketUseEntity.Action.ATTACK));
                                } else {
                                    mc.getNetHandler().addToSendQueue((Packet)new C02PacketUseEntity((Entity)Objects.requireNonNull(killAura.getTarget()), C02PacketUseEntity.Action.ATTACK));
                                }
                                this.applyHitSlowDown(true);
                            }
                        } else {
                            if (((Boolean)this.cancelKillAuraAttack.getValue()).booleanValue()) {
                                cancellingKillAuraAttack = true;
                            }
                            EventManager.call(new AttackEvent(targetA.entityHit));
                            mc.getNetHandler().addToSendQueue((Packet)new C0APacketAnimation());
                            if (targetA.entityHit != Velocity.mc.thePlayer) {
                                mc.getNetHandler().addToSendQueue((Packet)new C02PacketUseEntity(targetA.entityHit, C02PacketUseEntity.Action.ATTACK));
                            } else {
                                mc.getNetHandler().addToSendQueue((Packet)new C02PacketUseEntity(Objects.requireNonNull(targetA.entityHit), C02PacketUseEntity.Action.ATTACK));
                            }
                            this.applyHitSlowDown(true);
                        }
                    }
                    ++this.reduceTick;
                }
            }
        }
        if (((Integer)this.mode.getValue() == 1 || (Integer)this.mode.getValue() == 2) && event.getType() == EventType.POST) {
            KillAura killAura = (KillAura)AlpeetcClient.moduleManager.getModule(KillAura.class);
            if (this.delayFlag && ((Boolean)this.forceDelayRisingToFalling.getValue() == false || Velocity.mc.thePlayer.motionY <= 0.0) && ((Boolean)this.delay.getValue() != false && (this.isInLiquidOrWeb() || AlpeetcClient.delayManager.getDelay() >= (long)((Integer)this.delayTicks.getValue()).intValue() && (Boolean)this.airBuffer.getValue() == false) || Velocity.mc.thePlayer.onGround && (Boolean)this.groundDelay.getValue() == false && (Boolean)this.airBuffer.getValue() == false || (Boolean)this.airBuffer.getValue() != false && Velocity.mc.thePlayer.onGround && this.delayFlag) || (Integer)this.reduceMode.getValue() == 1 && killAura.velocityCanReduce(1, killAura.blockTick) && killAura.shouldAutoBlock() && (Boolean)this.reduce.getValue() != false && this.delayFlag || (Integer)this.reduceMode.getValue() == 2 && killAura.velocityCanReduce(2, killAura.blockTick) && killAura.shouldAutoBlock() && ((Boolean)this.reduce.getValue()).booleanValue() && this.delayFlag) {
                this.ticksSinceVelocity = 0;
                if (killAura.getTarget() != null && ((Boolean)this.extraAttack.getValue()).booleanValue() && ((Boolean)this.reduce.getValue()).booleanValue() && (Integer)this.reduceMode.getValue() != 0 && !extraAttacked) {
                    extraAttacked = true;
                    velocityAttacked = true;
                }
                if (!((Boolean)this.testMode.getValue()).booleanValue()) {
                    hasReceivedVelocity = true;
                }
                this.dbg(Velocity.jvmdowngrader$concat$onUpdate$1(AlpeetcClient.clientName, AlpeetcClient.delayManager.getDelay()));
                AlpeetcClient.delayManager.setDelayState(false, DelayModules.VELOCITY);
                this.delayFlag = false;
                if (((Boolean)this.jump.getValue()).booleanValue() && ((Integer)this.mode.getValue() == 1 || (Integer)this.mode.getValue() == 2)) {
                    this.jumpFlag = true;
                }
                if ((Integer)this.mode.getValue() == 2) {
                    this.grimActive = true;
                    this.grimTick = 0;
                }
            }
        }
        if ((Integer)this.mode.getValue() == 2 && event.getType() == EventType.PRE && this.grimActive) {
            EntityLivingBase target;
            cancellingKillAuraAttack = true;
            KillAura killAura = (KillAura)AlpeetcClient.moduleManager.getModule(KillAura.class);
            EntityLivingBase entityLivingBase = target = killAura != null ? killAura.getTarget() : null;
            if (target != null && target != Velocity.mc.thePlayer && Velocity.mc.thePlayer.isSprinting() && killAura.isEnabled()) {
                EventManager.call(new AttackEvent((Entity)target));
                mc.getNetHandler().addToSendQueue((Packet)new C0APacketAnimation());
                mc.getNetHandler().addToSendQueue((Packet)new C02PacketUseEntity((Entity)target, C02PacketUseEntity.Action.ATTACK));
                this.applyHitSlowDown(false);
            }
            ++this.grimTick;
            if (this.grimTick >= (Integer)this.grimReduceTicks.getValue()) {
                this.grimActive = false;
                cancellingKillAuraAttack = false;
            }
        }
    }

    private void startVelocityBlink() {
        if (AlpeetcClient.blinkManager.setBlinkState(true, BlinkModules.VELOCITY)) {
            this.blinkingVelocity = true;
            blinkActive = true;
            this.blinkScheduled = false;
        }
    }

    private void releaseVelocityBlink() {
        if (!this.blinkingVelocity) {
            return;
        }
        boolean wasActive = blinkActive;
        blinkActive = false;
        KeepSprint keepSprint = (KeepSprint)AlpeetcClient.moduleManager.getModule(KeepSprint.class);
        double factor = keepSprint != null && keepSprint.isEnabled() ? keepSprint.getSlowFactor() : 0.6;
        blinkActive = wasActive;
        boolean wasBlinking = AlpeetcClient.blinkManager.isBlinking();
        AlpeetcClient.blinkManager.blinking = false;
        int i = 0;
        boolean serverSprinting = Velocity.mc.thePlayer.isSprinting();
        boolean slowed = false;
        for (Packet<?> p : AlpeetcClient.blinkManager.blinkedPackets) {
            if (p instanceof C0BPacketEntityAction) {
                if (((C0BPacketEntityAction)p).getAction() == C0BPacketEntityAction.Action.START_SPRINTING) {
                    serverSprinting = true;
                }
                if (((C0BPacketEntityAction)p).getAction() == C0BPacketEntityAction.Action.STOP_SPRINTING) {
                    serverSprinting = false;
                }
            }
            if (p instanceof C02PacketUseEntity) {
                ++i;
                if (serverSprinting && !slowed) {
                    Velocity.mc.thePlayer.motionX *= factor;
                    Velocity.mc.thePlayer.motionZ *= factor;
                    Velocity.mc.thePlayer.setSprinting(false);
                    slowed = true;
                }
            }
            PacketUtil.sendPacketNoEvent(p);
        }
        AlpeetcClient.blinkManager.blinkedPackets.clear();
        if (!wasBlinking) {
            AlpeetcClient.blinkManager.blinkModule = BlinkModules.NONE;
        }
        this.blinkingVelocity = false;
        blinkActive = false;
        this.blinkScheduled = false;
        this.knockback = false;
        this.knockbackTimer = -1;
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        S12PacketEntityVelocity velocityPacket;
        if (this.isEnabled() && event.getType() == EventType.RECEIVE && !event.isCancelled()) {
            S12PacketEntityVelocity packet;
            if (event.getPacket() instanceof S12PacketEntityVelocity) {
                packet = (S12PacketEntityVelocity)event.getPacket();
                if (packet.getEntityID() == Velocity.mc.thePlayer.getEntityId()) {
                    if (!((Boolean)this.testMode.getValue()).booleanValue()) {
                        if (!((Boolean)this.delay.getValue()).booleanValue()) {
                            hasReceivedVelocity = true;
                        }
                        if (((Boolean)this.delay.getValue()).booleanValue() && !((Boolean)this.groundDelay.getValue()).booleanValue() && Velocity.mc.thePlayer.onGround) {
                            hasReceivedVelocity = true;
                        }
                    }
                    LongJump longJump = (LongJump)AlpeetcClient.moduleManager.modules.get(LongJump.class);
                    if (!((Integer)this.mode.getValue() != 1 && (Integer)this.mode.getValue() != 2 || this.delayFlag || this.isInLiquidOrWeb() || this.pendingExplosion || AlpeetcClient.moduleManager.getModule(Stuck.class).isEnabled() || this.allowNext && ((Boolean)this.fakeCheck.getValue()).booleanValue() || longJump.isEnabled() && longJump.canStartJump() || !((Boolean)this.airBuffer.getValue() != false && !Velocity.mc.thePlayer.onGround || (Boolean)this.delay.getValue() != false && !Velocity.mc.thePlayer.onGround) && (!((Boolean)this.delay.getValue()).booleanValue() || !((Boolean)this.groundDelay.getValue()).booleanValue() || ((Boolean)this.airBuffer.getValue()).booleanValue()))) {
                        AlpeetcClient.delayManager.setDelayState(true, DelayModules.VELOCITY);
                        this.dbg(Velocity.jvmdowngrader$concat$onPacket$1(AlpeetcClient.clientName));
                        AlpeetcClient.delayManager.delayedPacket.offer((Packet<INetHandlerPlayClient>)packet);
                        event.setCancelled(true);
                        this.delayFlag = true;
                    }
                }
            } else if (!(event.getPacket() instanceof S27PacketExplosion)) {
                Entity entity;
                if (event.getPacket() instanceof S19PacketEntityStatus && (entity = ((S19PacketEntityStatus)event.getPacket()).getEntity((World)Velocity.mc.theWorld)) != null && entity.equals((Object)Velocity.mc.thePlayer) && ((S19PacketEntityStatus)event.getPacket()).getOpCode() == 2) {
                    this.allowNext = false;
                }
            } else if ((Integer)this.mode.getValue() == 0 && (((S27PacketExplosion)event.getPacket()).func_149149_c() != 0.0f || ((S27PacketExplosion)event.getPacket()).func_149144_d() != 0.0f || ((S27PacketExplosion)event.getPacket()).func_149147_e() != 0.0f)) {
                this.pendingExplosion = true;
                if ((Integer)this.explosionHorizontal.getValue() == 0 || (Integer)this.explosionVertical.getValue() == 0) {
                    event.setCancelled(true);
                }
            }
        }
        if (event.getType() == EventType.RECEIVE && !event.isCancelled() && event.getPacket() instanceof S12PacketEntityVelocity && (velocityPacket = (S12PacketEntityVelocity)event.getPacket()).getEntityID() == Velocity.mc.thePlayer.getEntityId()) {
            this.knockback = true;
            this.knockbackTimer = 0;
        }
    }

    @EventTarget
    public void onMove(MoveInputEvent event) {
        if (this.isEnabled() && this.rotateTickCounter > 0 && this.rotateTickCounter <= (Integer)this.rotateTick.getValue() && ((Boolean)this.autoMove.getValue()).booleanValue()) {
            Velocity.mc.thePlayer.movementInput.moveForward = 1.0f;
        }
    }

    @EventTarget
    public void onLoadWorld(LoadWorldEvent event) {
        this.onDisabled();
    }

    public void dbg(String msg) {
        if (((Boolean)this.debug.getValue()).booleanValue()) {
            ChatUtil.sendFormatted(msg);
        }
    }

    private void applyHitSlowDown(boolean respectKeepSprint) {
        HitSlowDownEvent hitSlowDownEvent = (HitSlowDownEvent)EventManager.call(new HitSlowDownEvent());
        Velocity.mc.thePlayer.motionX *= hitSlowDownEvent.getSlowDown();
        Velocity.mc.thePlayer.motionZ *= hitSlowDownEvent.getSlowDown();
        if (!(hitSlowDownEvent.getSprint() || respectKeepSprint && ((Boolean)this.keepSprint.getValue()).booleanValue())) {
            Velocity.mc.thePlayer.setSprinting(false);
        }
    }

    @Override
    public void onEnabled() {
        this.knockback = false;
        hasReceivedVelocity = false;
        this.grimActive = false;
        this.grimTick = 0;
        this.rotateTickCounter = 0;
        this.targetRotation = null;
        this.knockbackX = 0.0;
        this.knockbackZ = 0.0;
    }

    @Override
    public void onDisabled() {
        this.pendingExplosion = false;
        stoppedBlock = false;
        this.allowNext = true;
        hasReceivedVelocity = false;
        this.knockback = false;
        cancellingKillAuraAttack = false;
        this.grimActive = false;
        this.grimTick = 0;
        AlpeetcClient.delayManager.setDelayState(false, DelayModules.VELOCITY);
        if (this.blinkingVelocity) {
            this.blinkingVelocity = false;
            blinkActive = false;
            this.blinkScheduled = false;
            this.knockbackTimer = -1;
            AlpeetcClient.blinkManager.blinking = false;
            AlpeetcClient.blinkManager.blinkedPackets.clear();
            AlpeetcClient.blinkManager.blinkModule = BlinkModules.NONE;
        }
    }

    @Override
    public String[] getSuffix() {
        if ((Integer)this.mode.getValue() == 0) {
            return new String[]{String.format("%d%%", this.horizontal.getValue()), String.format("%d%%", this.vertical.getValue())};
        }
        return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.mode.getModeString())};
    }

    static {
        velocityAttacked = false;
        stoppedBlock = false;
        cancellingKillAuraAttack = false;
        blinkActive = false;
    }

    private static /* synthetic */ String jvmdowngrader$concat$onUpdate$1(String string, long l) {
        return string + "Delay/Buffer " + l + " Ticks";
    }

    private static /* synthetic */ String jvmdowngrader$concat$onPacket$1(String string) {
        return string + "Delay/Buffer Active";
    }
}

