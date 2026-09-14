/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.CaseFormat
 *  io.netty.buffer.Unpooled
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.entity.EntityOtherPlayerMP
 *  net.minecraft.client.gui.inventory.GuiContainer
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.boss.EntityDragon
 *  net.minecraft.entity.boss.EntityWither
 *  net.minecraft.entity.monster.EntityIronGolem
 *  net.minecraft.entity.monster.EntityMob
 *  net.minecraft.entity.monster.EntitySilverfish
 *  net.minecraft.entity.monster.EntitySlime
 *  net.minecraft.entity.passive.EntityAnimal
 *  net.minecraft.entity.passive.EntityBat
 *  net.minecraft.entity.passive.EntitySquid
 *  net.minecraft.entity.passive.EntityVillager
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.Packet
 *  net.minecraft.network.PacketBuffer
 *  net.minecraft.network.play.client.C02PacketUseEntity
 *  net.minecraft.network.play.client.C02PacketUseEntity$Action
 *  net.minecraft.network.play.client.C07PacketPlayerDigging
 *  net.minecraft.network.play.client.C07PacketPlayerDigging$Action
 *  net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
 *  net.minecraft.network.play.client.C09PacketHeldItemChange
 *  net.minecraft.network.play.client.C17PacketCustomPayload
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.util.BlockPos
 *  net.minecraft.util.EnumFacing
 *  net.minecraft.util.MathHelper
 *  net.minecraft.util.MovingObjectPosition
 *  net.minecraft.util.MovingObjectPosition$MovingObjectType
 *  net.minecraft.util.Vec3
 *  net.minecraft.world.WorldSettings$GameType
 *  xyz.wagyourtail.jvmdg.j11.NestHost
 *  xyz.wagyourtail.jvmdg.j11.NestMembers
 */
package alpeetcclient.module.modules.combat;

import alpeetcclient.AlpeetcClient;
import com.google.common.base.CaseFormat;
import io.netty.buffer.Unpooled;
import java.awt.AWTException;
import java.util.ArrayList;
import alpeetcclient.enums.BlinkModules;
import alpeetcclient.event.EventManager;
import alpeetcclient.event.EventTarget;
import alpeetcclient.event.types.EventType;
import alpeetcclient.events.AttackEvent;
import alpeetcclient.events.CancelUseEvent;
import alpeetcclient.events.HitBlockEvent;
import alpeetcclient.events.LeftClickMouseEvent;
import alpeetcclient.events.MoveInputEvent;
import alpeetcclient.events.PacketEvent;
import alpeetcclient.events.Render3DEvent;
import alpeetcclient.events.RightClickMouseEvent;
import alpeetcclient.events.TickEvent;
import alpeetcclient.events.UpdateEvent;
import alpeetcclient.management.RotationState;
import alpeetcclient.mixin.IAccessorMinecraft;
import alpeetcclient.mixin.IAccessorPlayerControllerMP;
import alpeetcclient.module.Module;
import alpeetcclient.module.modules.combat.SmartAttack;
import alpeetcclient.module.modules.combat.Velocity;
import alpeetcclient.module.modules.misc.AutoHeal;
import alpeetcclient.module.modules.misc.Disabler;
import alpeetcclient.module.modules.movement.NoSlow;
import alpeetcclient.module.modules.player.AutoBlockIn;
import alpeetcclient.module.modules.player.BedNuker;
import alpeetcclient.module.modules.player.KeepSprint;
import alpeetcclient.module.modules.player.Scaffold;
import alpeetcclient.property.properties.BooleanProperty;
import alpeetcclient.property.properties.FloatProperty;
import alpeetcclient.property.properties.IntProperty;
import alpeetcclient.property.properties.ModeProperty;
import alpeetcclient.property.properties.PercentProperty;
import alpeetcclient.util.ChatUtil;
import alpeetcclient.util.ItemUtil;
import alpeetcclient.util.KeyBindUtil;
import alpeetcclient.util.MoveUtil;
import alpeetcclient.util.PacketUtil;
import alpeetcclient.util.PlayerUtil;
import alpeetcclient.util.RandomUtil;
import alpeetcclient.util.RotationUtil;
import alpeetcclient.util.TeamUtil;
import alpeetcclient.util.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.AxisAlignedBB;
import alpeetcclient.module.modules.misc.Bypass;
import alpeetcclient.util.BypassManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldSettings;
public class KillAura
extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public final ModeProperty mode;
    public final ModeProperty sort;
    public ModeProperty autoBlock;
    public ModeProperty hypixelMode;
    public ModeProperty lagMode;
    private final BooleanProperty noStop = new BooleanProperty("NoSwap", true, this::isOldHypixel);
    private final BooleanProperty test = new BooleanProperty("MoreAttack", false, this::isOldHypixel);
    private final IntProperty moreAttackDelay = new IntProperty("MoreAttackDelay", 1, 0, 3, () -> this.isOldHypixel() && (Boolean)this.test.getValue() != false);
    public final IntProperty maxTick = new IntProperty("MaxTick", 3, 1, 5, this::isHypixelCustom);
    private final IntProperty startBlinkTick = new IntProperty("StartBlinkTick", 0, 1, 5, this::isHypixelCustom);
    private final IntProperty stopBlinkTick = new IntProperty("StopBlinkTick", 2, 1, 5, this::isHypixelCustom);
    private final IntProperty swapTick = new IntProperty("SwapTick", 2, 1, 5, this::isHypixelCustom);
    private final IntProperty switchBackTick = new IntProperty("SwitchBackTick", 2, 1, 5, this::isHypixelCustom);
    private final IntProperty stopBlockTick = new IntProperty("StopBlockTick", 2, 1, 5, this::isHypixelCustom);
    public final IntProperty attackTick = new IntProperty("AttackTick", 0, 1, 5, this::isHypixelCustom);
    private final IntProperty startBlockTick = new IntProperty("StartBlockTick", 0, 1, 5, this::isHypixelCustom);
    private final BooleanProperty postStartBlock = new BooleanProperty("PostBlock", false, this::isHypixelCustom);
    private final BooleanProperty alwaysRenderBlocking = new BooleanProperty("AlwaysRenderBlocking", true, this::isLag);
    private final BooleanProperty c09Instead = new BooleanProperty("C09Instead", true, this::isLag3Tick);
    private final BooleanProperty fullC09 = new BooleanProperty("FullC09(Will Cause Damage Less)", false, () -> this.isLag4Tick() | this.isLag5Tick());
    public final BooleanProperty autoBlockRequirePress;
    public final IntProperty autoBlockCPS;
    public final FloatProperty autoBlockRange;
    public final FloatProperty swingRange;
    public final FloatProperty attackRange;
    public final IntProperty fov;
    public final IntProperty minCPS;
    public final IntProperty maxCPS;
    public final IntProperty switchDelay;
    public final ModeProperty rotations;
    public final ModeProperty moveFix;
    public ModeProperty rotationMode;
    public final PercentProperty smoothing;
    public final IntProperty angleStep;
    public final BooleanProperty throughWalls;
    public final BooleanProperty requirePress;
    public final BooleanProperty allowMining;
    public final BooleanProperty allowPlayerBlocking;
    public final BooleanProperty weaponsOnly;
    public final BooleanProperty allowTools;
    public final BooleanProperty inventoryCheck;
    public final BooleanProperty lowTimerCheck;
    public final BooleanProperty botCheck;
    public final BooleanProperty players;
    public final BooleanProperty bosses;
    public final BooleanProperty mobs;
    public final BooleanProperty animals;
    public final BooleanProperty golems;
    public final BooleanProperty silverfish;
    public final BooleanProperty teams;
    private final TimerUtil timer = new TimerUtil();
    private AttackData target = null;
    private int switchTick = 0;
    private boolean hitRegistered = false;
    public boolean blockingState = false;
    public boolean isBlocking = false;
    private boolean fakeBlockState = false;
    private long attackDelayMS = 0L;
    public int blockTick = 0;
    private boolean swapped = false;
    private boolean postBlock = false;
    private boolean postSwap = false;
    private boolean postBlinkReset = false;
    private int testAttackTick = 0;
    private boolean bufferPending = false;
    public final ModeProperty bypassMode = new ModeProperty("Bypass", 0, new String[]{"None", "GrimAC", "Intave", "Karhu", "NCP", "Artemis", "FairFight", "WatchNeko", "Medusa", "Lon", "MXProject", "ACA"});
    public final BooleanProperty gcdBypass = new BooleanProperty("GCD Bypass", true);
    public final BooleanProperty rotationNoise = new BooleanProperty("Rotation Noise", true);
    public final BooleanProperty attackRandomizer = new BooleanProperty("Attack Randomizer", true);
    public final BooleanProperty extraArmAnimation = new BooleanProperty("Extra ArmAnimation", true);
    public final BooleanProperty alwaysSwing = new BooleanProperty("Always Swing", true);

    public KillAura() {
        super("KillAura", false);
        this.mode = new ModeProperty("Mode", 0, new String[]{"Single", "Switch"});
        this.sort = new ModeProperty("Sort", 0, new String[]{"Distance", "Health", "Hurt Time", "FOV"});
        this.autoBlock = new ModeProperty("AutoBlock", 0, new String[]{"None", "Vanilla", "Hypixel", "Legit", "Fake"});
        this.hypixelMode = new ModeProperty("HypixelMode", 0, new String[]{"OldHypixel", "Without NoSlow", "Custom", "Lag"}, () -> (Integer)this.autoBlock.getValue() == 2);
        this.lagMode = new ModeProperty("LagMode", 1, new String[]{"Hypixel1", "Hypixel2", "Hypixel3", "Hypixel2+3", "Hypixel4", "Hypixel5", "Hypixel2Full", "Hypixel4Full", "Swap", "TestPostSwap"}, () -> (Integer)this.autoBlock.getValue() == 2 && (Integer)this.hypixelMode.getValue() == 3);
        this.autoBlockRequirePress = new BooleanProperty("AutoBlock Require Press", false);
        this.autoBlockCPS = new IntProperty("AutoBlock Aps", 10, 1, 20);
        this.autoBlockRange = new FloatProperty("AutoBlock Range", Float.valueOf(6.0f), Float.valueOf(3.0f), Float.valueOf(8.0f));
        this.swingRange = new FloatProperty("Swing Range", Float.valueOf(3.5f), Float.valueOf(3.0f), Float.valueOf(6.0f));
        this.attackRange = new FloatProperty("Attack Range", Float.valueOf(3.0f), Float.valueOf(3.0f), Float.valueOf(6.0f));
        this.fov = new IntProperty("Fov", 360, 30, 360);
        this.minCPS = new IntProperty("Min Aps", 14, 1, 20);
        this.maxCPS = new IntProperty("Max Aps", 14, 1, 20);
        this.switchDelay = new IntProperty("Switch Delay", 150, 0, 1000);
        this.rotations = new ModeProperty("Rotations", 2, new String[]{"None", "Legit", "Silent", "Lock View"});
        this.moveFix = new ModeProperty("Move Fix", 1, new String[]{"None", "Silent", "Strict"});
        this.rotationMode = new ModeProperty("RotationMode", 2, new String[]{"Normal", "Nearest", "Smart"});
        this.smoothing = new PercentProperty("Smoothing", 0);
        this.angleStep = new IntProperty("Angle Step", 90, 30, 180);
        this.throughWalls = new BooleanProperty("Through Walls", true);
        this.requirePress = new BooleanProperty("Require Press", false);
        this.allowPlayerBlocking = new BooleanProperty("Allow Player Blocking", true);
        this.allowMining = new BooleanProperty("Allow Mining", false);
        this.weaponsOnly = new BooleanProperty("Weapons Only", false);
        this.allowTools = new BooleanProperty("Allow Tools", false, this.weaponsOnly::getValue);
        this.inventoryCheck = new BooleanProperty("Inventory Check", true);
        this.lowTimerCheck = new BooleanProperty("Low Timer Check", true);
        this.botCheck = new BooleanProperty("Bot Check", true);
        this.players = new BooleanProperty("Players", true);
        this.bosses = new BooleanProperty("Bosses", false);
        this.mobs = new BooleanProperty("Mobs", false);
        this.animals = new BooleanProperty("Animals", false);
        this.golems = new BooleanProperty("Golems", false);
        this.silverfish = new BooleanProperty("Silverfish", false);
        this.teams = new BooleanProperty("Teams", true);
    }

    private long getAttackDelay() {
        return this.isBlocking ? (long)(1000.0f / (float)((Integer)this.autoBlockCPS.getValue()).intValue()) : 1000L / RandomUtil.nextLong(((Integer)this.minCPS.getValue()).intValue(), ((Integer)this.maxCPS.getValue()).intValue());
    }

    private boolean performAttack(float yaw, float pitch) {
        if (!AlpeetcClient.playerStateManager.digging && !AlpeetcClient.playerStateManager.placing) {
            if (this.bufferPending) {
                this.bufferPending = false;
                if (this.target != null && this.isValidTarget(this.target.getEntity()) && (!this.isPlayerBlocking() || (Integer)this.autoBlock.getValue() == 1) && ((Integer)this.rotations.getValue() == 0 && this.isBoxInAttackRange(this.target.getBox()) || RotationUtil.rayTrace(this.target.getBox(), yaw, pitch, (double)((Float)this.attackRange.getValue()).floatValue()) != null)) {
                    return this.sendAttackPacket();
                }
                return false;
            }
            if (Velocity.stoppedBlock) {
                return false;
            }
            if (this.isPlayerBlocking() && (Integer)this.autoBlock.getValue() != 1) {
                return false;
            }
            if (this.attackDelayMS > 0L) {
                return false;
            }
            if (((IAccessorMinecraft)KillAura.mc).getTimer().timerSpeed < 1.0f && ((Boolean)this.lowTimerCheck.getValue()).booleanValue()) {
                return false;
            }
            if (AlpeetcClient.moduleManager.getModule(SmartAttack.class).isEnabled() && SmartAttack.shouldCancel && ((Boolean)SmartAttack.onKillAura.getValue()).booleanValue()) {
                return false;
            }
            if (Velocity.extraAttacked && ((Integer)this.autoBlock.getValue() == 2 || (Integer)this.autoBlock.getValue() == 3)) {
                ChatUtil.sendFormatted("StoppedAttack");
                Velocity.extraAttacked = false;
                Velocity velocity = (Velocity)AlpeetcClient.moduleManager.getModule(Velocity.class);
                if ((Integer)velocity.reduceMode.getValue() == 2) {
                    if (this.isOldHypixel() || this.isHypixelWithoutNoSlow()) {
                        this.blockTick = 0;
                    } else if (this.isHypixelCustom()) {
                        this.blockTick = (Integer)this.attackTick.getValue();
                    } else if (this.isLag()) {
                        this.blockTick = 0;
                    } else if ((Integer)this.autoBlock.getValue() == 3) {
                        this.blockTick = 0;
                    }
                } else if ((Integer)velocity.reduceMode.getValue() == 1) {
                    if (this.isOldHypixel() || this.isHypixelWithoutNoSlow()) {
                        this.blockTick = 2;
                    } else if (this.isHypixelCustom()) {
                        this.blockTick = (Integer)this.attackTick.getValue();
                    } else if (this.isLag()) {
                        this.blockTick = (Integer)this.lagMode.getValue() == 0 ? 1 : ((Integer)this.lagMode.getValue() == 1 ? 2 : ((Integer)this.lagMode.getValue() == 2 ? 3 : ((Integer)this.lagMode.getValue() == 5 ? 5 : ((Integer)this.lagMode.getValue() == 8 ? 3 : ((Integer)this.lagMode.getValue() == 9 ? 2 : 4)))));
                    } else if ((Integer)this.autoBlock.getValue() == 3) {
                        this.blockTick = 1;
                    }
                }
                return false;
            }
            this.attackDelayMS += this.getAttackDelay();
            if (!this.isBufferEnabled()) {
                KillAura.mc.thePlayer.swingItem();
            }
            if (!((Integer)this.rotations.getValue() == 0 && this.isBoxInAttackRange(this.target.getBox()) || RotationUtil.rayTrace(this.target.getBox(), yaw, pitch, (double)((Float)this.attackRange.getValue()).floatValue()) != null)) {
                return false;
            }
            if (this.isBufferEnabled()) {
                KillAura.mc.thePlayer.setSprinting(false);
                KeyBindUtil.setKeyBindState(KillAura.mc.gameSettings.keyBindSprint.getKeyCode(), false);
                if (this.shouldDelayAttack()) {
                    this.bufferPending = true;
                    return false;
                }
            }
            return this.sendAttackPacket();
        }
        return false;
    }

    private void sendUseItem() {
        ((IAccessorPlayerControllerMP)KillAura.mc.playerController).callSyncCurrentPlayItem();
        this.startBlock(KillAura.mc.thePlayer.getHeldItem());
    }

    private boolean sendAttackPacket() {
        if (this.isBufferEnabled()) {
            KillAura.mc.thePlayer.swingItem();
        }
        AttackEvent event = new AttackEvent((Entity)this.target.getEntity());
        EventManager.call(event);
        ((IAccessorPlayerControllerMP)KillAura.mc.playerController).callSyncCurrentPlayItem();
        PacketUtil.sendPacket(new C02PacketUseEntity((Entity)this.target.getEntity(), C02PacketUseEntity.Action.ATTACK));
        if (KillAura.mc.playerController.getCurrentGameType() != WorldSettings.GameType.SPECTATOR) {
            PlayerUtil.attackEntity((Entity)this.target.getEntity());
        }
        this.hitRegistered = true;
        return true;
    }

    private boolean isBufferEnabled() {
        KeepSprint keepSprint = (KeepSprint)AlpeetcClient.moduleManager.getModule(KeepSprint.class);
        return keepSprint != null && keepSprint.isEnabled() && keepSprint.isBufferMode();
    }

    private boolean shouldDelayAttack() {
        KeepSprint keepSprint = (KeepSprint)AlpeetcClient.moduleManager.getModule(KeepSprint.class);
        if (keepSprint == null || !keepSprint.isEnabled() || !keepSprint.isBufferMode()) {
            return false;
        }
        return KillAura.mc.thePlayer.hurtTime <= 0 || (Boolean)keepSprint.onHurt.getValue() != false;
    }

    private void startBlock(ItemStack itemStack) {
        PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(itemStack));
        KillAura.mc.thePlayer.setItemInUse(itemStack, itemStack.getMaxItemUseDuration());
        this.blockingState = true;
    }

    private void stopBlock() {
        PacketUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
        KillAura.mc.thePlayer.stopUsingItem();
        this.blockingState = false;
    }

    private void interactAttack(float yaw, float pitch) {
        MovingObjectPosition mop;
        if (this.target != null && (mop = RotationUtil.rayTrace(this.target.getBox(), yaw, pitch, 8.0)) != null) {
            ((IAccessorPlayerControllerMP)KillAura.mc.playerController).callSyncCurrentPlayItem();
            PacketUtil.sendPacket(new C02PacketUseEntity((Entity)this.target.getEntity(), new Vec3(mop.hitVec.xCoord - this.target.getX(), mop.hitVec.yCoord - this.target.getY(), mop.hitVec.zCoord - this.target.getZ())));
            PacketUtil.sendPacket(new C02PacketUseEntity((Entity)this.target.getEntity(), C02PacketUseEntity.Action.INTERACT));
            PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(KillAura.mc.thePlayer.getHeldItem()));
            KillAura.mc.thePlayer.setItemInUse(KillAura.mc.thePlayer.getHeldItem(), KillAura.mc.thePlayer.getHeldItem().getMaxItemUseDuration());
            this.blockingState = true;
        }
    }

    private boolean isNormalTargetVisible(AxisAlignedBB box) {
        double targetZ;
        double maxTargetY;
        double minTargetY;
        double targetY;
        double targetX;
        Vec3 targetPoint;
        if (KillAura.mc.thePlayer == null || KillAura.mc.theWorld == null) {
            return false;
        }
        Vec3 eyePos = KillAura.mc.thePlayer.getPositionEyes(1.0f);
        MovingObjectPosition mop = KillAura.mc.theWorld.rayTraceBlocks(eyePos, targetPoint = new Vec3(targetX = (box.minX + box.maxX) / 2.0, targetY = MathHelper.clamp_double((double)eyePos.yCoord, (double)(minTargetY = box.minY + 0.05 * (box.maxY - box.minY)), (double)(maxTargetY = box.minY + 0.75 * (box.maxY - box.minY))), targetZ = (box.minZ + box.maxZ) / 2.0), false, true, false);
        return mop == null;
    }

    private boolean canAttack() {
        if (((Boolean)this.inventoryCheck.getValue()).booleanValue() && KillAura.mc.currentScreen instanceof GuiContainer) {
            return false;
        }
        if (!((Boolean)this.weaponsOnly.getValue()).booleanValue() || ItemUtil.hasRawUnbreakingEnchant() || ((Boolean)this.allowTools.getValue()).booleanValue() && ItemUtil.isHoldingTool()) {
            if (((IAccessorPlayerControllerMP)KillAura.mc.playerController).getIsHittingBlock()) {
                return false;
            }
            if ((ItemUtil.isEating() || ItemUtil.isUsingBow()) && PlayerUtil.isUsingItem()) {
                return false;
            }
            AutoHeal autoHeal = (AutoHeal)AlpeetcClient.moduleManager.modules.get(AutoHeal.class);
            if (autoHeal.isEnabled() && autoHeal.isSwitching()) {
                return false;
            }
            BedNuker bedNuker = (BedNuker)AlpeetcClient.moduleManager.modules.get(BedNuker.class);
            AutoBlockIn autoBlockIn = (AutoBlockIn)AlpeetcClient.moduleManager.modules.get(AutoBlockIn.class);
            if (bedNuker.isEnabled() && bedNuker.isReady()) {
                return false;
            }
            if (AlpeetcClient.moduleManager.modules.get(Scaffold.class).isEnabled()) {
                return false;
            }
            if (autoBlockIn.isEnabled()) {
                return false;
            }
            if (((Boolean)this.requirePress.getValue()).booleanValue()) {
                return PlayerUtil.isAttacking();
            }
            return (Boolean)this.allowMining.getValue() == false || !KillAura.mc.objectMouseOver.typeOfHit.equals((Object)MovingObjectPosition.MovingObjectType.BLOCK) || !PlayerUtil.isAttacking();
        }
        return false;
    }

    private boolean canAutoBlock() {
        if (Velocity.stoppedBlock) {
            return false;
        }
        if (AlpeetcClient.moduleManager.getModule(SmartAttack.class).isEnabled() && SmartAttack.shouldCancel && ((Boolean)SmartAttack.cancelAuraBlocking.getValue()).booleanValue() && ((Boolean)SmartAttack.onKillAura.getValue()).booleanValue()) {
            return false;
        }
        if (!ItemUtil.isHoldingSword()) {
            return false;
        }
        return (Boolean)this.autoBlockRequirePress.getValue() == false || PlayerUtil.isUsingItem();
    }

    private boolean hasValidTarget() {
        return KillAura.mc.theWorld.loadedEntityList.stream().anyMatch(entity -> entity instanceof EntityLivingBase && this.isValidTarget((EntityLivingBase)entity) && this.isInBlockRange((EntityLivingBase)entity));
    }

    private boolean isValidTarget(EntityLivingBase entityLivingBase) {
        if (!KillAura.mc.theWorld.loadedEntityList.contains(entityLivingBase)) {
            return false;
        }
        if (entityLivingBase != KillAura.mc.thePlayer && entityLivingBase != KillAura.mc.thePlayer.ridingEntity) {
            if (entityLivingBase == mc.getRenderViewEntity() || entityLivingBase == KillAura.mc.getRenderViewEntity().ridingEntity) {
                return false;
            }
            if (entityLivingBase.deathTime > 0) {
                return false;
            }
            if (RotationUtil.angleToEntity((Entity)entityLivingBase) > ((Integer)this.fov.getValue()).floatValue()) {
                return false;
            }
            if (!((Boolean)this.throughWalls.getValue()).booleanValue() && !RotationUtil.hasVisiblePoint(entityLivingBase.getEntityBoundingBox())) {
                return false;
            }
            if (entityLivingBase instanceof EntityOtherPlayerMP) {
                if (!((Boolean)this.players.getValue()).booleanValue()) {
                    return false;
                }
                if (TeamUtil.isFriend((EntityPlayer)entityLivingBase)) {
                    return false;
                }
                return !((Boolean)this.teams.getValue() != false && TeamUtil.isSameTeam((EntityPlayer)entityLivingBase) || (Boolean)this.botCheck.getValue() != false && TeamUtil.isBot((EntityPlayer)entityLivingBase));
            }
            if (entityLivingBase instanceof EntityDragon || entityLivingBase instanceof EntityWither) {
                return (Boolean)this.bosses.getValue();
            }
            if (!(entityLivingBase instanceof EntityMob) && !(entityLivingBase instanceof EntitySlime)) {
                if (entityLivingBase instanceof EntityAnimal || entityLivingBase instanceof EntityBat || entityLivingBase instanceof EntitySquid || entityLivingBase instanceof EntityVillager) {
                    return (Boolean)this.animals.getValue();
                }
                if (!(entityLivingBase instanceof EntityIronGolem)) {
                    return false;
                }
                return (Boolean)this.golems.getValue() != false && ((Boolean)this.teams.getValue() == false || !TeamUtil.hasTeamColor(entityLivingBase));
            }
            if (!(entityLivingBase instanceof EntitySilverfish)) {
                return (Boolean)this.mobs.getValue();
            }
            return (Boolean)this.silverfish.getValue() != false && ((Boolean)this.teams.getValue() == false || !TeamUtil.hasTeamColor(entityLivingBase));
        }
        return false;
    }

    private boolean isInRange(EntityLivingBase entityLivingBase) {
        return this.isInBlockRange(entityLivingBase) || this.isInSwingRange(entityLivingBase) || this.isInAttackRange(entityLivingBase);
    }

    private boolean isInBlockRange(EntityLivingBase entityLivingBase) {
        return RotationUtil.distanceToEntity((Entity)entityLivingBase) <= (double)((Float)this.autoBlockRange.getValue()).floatValue();
    }

    private boolean isInSwingRange(EntityLivingBase entityLivingBase) {
        return RotationUtil.distanceToEntity((Entity)entityLivingBase) <= (double)((Float)this.swingRange.getValue()).floatValue();
    }

    private boolean isBoxInSwingRange(AxisAlignedBB axisAlignedBB) {
        return RotationUtil.distanceToBox(axisAlignedBB) <= (double)((Float)this.swingRange.getValue()).floatValue();
    }

    private boolean isInAttackRange(EntityLivingBase entityLivingBase) {
        return RotationUtil.distanceToEntity((Entity)entityLivingBase) <= (double)((Float)this.attackRange.getValue()).floatValue();
    }

    private boolean isBoxInAttackRange(AxisAlignedBB axisAlignedBB) {
        return RotationUtil.distanceToBox(axisAlignedBB) <= (double)((Float)this.attackRange.getValue()).floatValue();
    }

    private boolean isPlayerTarget(EntityLivingBase entityLivingBase) {
        return entityLivingBase instanceof EntityPlayer && TeamUtil.isTarget((EntityPlayer)entityLivingBase);
    }

    public EntityLivingBase getTarget() {
        return this.target != null ? this.target.getEntity() : null;
    }

    public boolean isAttackAllowed() {
        Scaffold scaffold = (Scaffold)AlpeetcClient.moduleManager.modules.get(Scaffold.class);
        if (scaffold.isEnabled()) {
            return false;
        }
        if (!((Boolean)this.weaponsOnly.getValue()).booleanValue() || ItemUtil.hasRawUnbreakingEnchant() || ((Boolean)this.allowTools.getValue()).booleanValue() && ItemUtil.isHoldingTool()) {
            return (Boolean)this.requirePress.getValue() == false || KeyBindUtil.isKeyDown(KillAura.mc.gameSettings.keyBindAttack.getKeyCode());
        }
        return false;
    }

    public boolean isOldHypixel() {
        return (Integer)this.autoBlock.getValue() == 2 && (Integer)this.hypixelMode.getValue() == 0;
    }

    public boolean isHypixelWithoutNoSlow() {
        return (Integer)this.autoBlock.getValue() == 2 && (Integer)this.hypixelMode.getValue() == 1;
    }

    public boolean isHypixelCustom() {
        return (Integer)this.autoBlock.getValue() == 2 && (Integer)this.hypixelMode.getValue() == 2;
    }

    public boolean isLag() {
        return (Integer)this.autoBlock.getValue() == 2 && (Integer)this.hypixelMode.getValue() == 3;
    }

    public boolean isLag3Tick() {
        return this.isLag() && (Integer)this.lagMode.getValue() == 1;
    }

    public boolean isLag4Tick() {
        return this.isLag() && (Integer)this.lagMode.getValue() == 2;
    }

    public boolean isLag5Tick() {
        return this.isLag() && ((Integer)this.lagMode.getValue() == 3 || (Integer)this.lagMode.getValue() == 4 || (Integer)this.lagMode.getValue() == 5 || (Integer)this.lagMode.getValue() == 6 || (Integer)this.lagMode.getValue() == 7 || (Integer)this.lagMode.getValue() == 8);
    }

    public boolean shouldAutoBlock() {
        if ((Integer)this.autoBlock.getValue() <= 1 || (Integer)this.autoBlock.getValue() == 4) {
            return this.hasValidTarget();
        }
        if (this.isPlayerBlocking() && this.isBlocking) {
            return !KillAura.mc.thePlayer.isInWater() && !KillAura.mc.thePlayer.isInLava() && ((Integer)this.autoBlock.getValue() == 2 || (Integer)this.autoBlock.getValue() == 3);
        }
        return false;
    }

    public boolean velocityCanReduce(int phase, int tick) {
        switch ((Integer)this.autoBlock.getValue()) {
            case 0: 
            case 1: 
            case 4: {
                return true;
            }
            case 2: {
                switch ((Integer)this.hypixelMode.getValue()) {
                    case 0: 
                    case 1: {
                        return phase == 2 ? tick == 2 : tick == 0;
                    }
                    case 2: {
                        int maxT = Math.max(1, (Integer)this.maxTick.getValue() - 1);
                        switch (phase) {
                            case 0: {
                                return tick == (Integer)this.attackTick.getValue();
                            }
                            case 1: {
                                return tick == (Integer)this.attackTick.getValue() % maxT;
                            }
                        }
                        return tick == ((Integer)this.attackTick.getValue() - 2 + maxT) % maxT;
                    }
                    case 3: {
                        switch ((Integer)this.lagMode.getValue()) {
                            case 0: {
                                return phase == 2 ? tick == 1 : tick == 0;
                            }
                            case 1: {
                                return phase == 2 ? tick == 2 : (phase == 1 ? tick == 0 : tick == 0 || tick == 2);
                            }
                            case 2: {
                                return phase == 2 ? tick == 3 : (phase == 1 ? tick == 0 : tick == 0 || tick == 3);
                            }
                            case 3: {
                                return phase == 2 ? tick == 4 : (phase == 1 ? tick == 0 : tick == 0 || tick == 2 || tick == 4);
                            }
                            case 4: {
                                return phase == 2 ? tick == 4 : (phase == 1 ? tick == 0 : tick == 0 || tick == 4);
                            }
                            case 5: {
                                return phase == 2 ? tick == 5 : (phase == 1 ? tick == 0 : tick == 0 || tick == 5);
                            }
                            case 8: {
                                return phase == 2 ? tick == 3 : (phase == 1 ? tick == 0 : tick == 0 || tick == 3);
                            }
                            case 9: {
                                return phase == 2 ? tick == 2 : tick == 0;
                            }
                        }
                        return false;
                    }
                }
                return false;
            }
            case 3: {
                return phase == 2 ? tick == 1 : tick == 0;
            }
        }
        return true;
    }

    public boolean isBlocking() {
        return this.fakeBlockState && ItemUtil.isHoldingSword();
    }

    public boolean isPlayerBlocking() {
        return (KillAura.mc.thePlayer.isUsingItem() || this.blockingState) && ItemUtil.isHoldingSword();
    }

    @EventTarget(value=3)
    public void onUpdate(UpdateEvent event) throws AWTException {
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            boolean block;
            if (this.attackDelayMS > 0L) {
                this.attackDelayMS -= 50L;
            }
            boolean attack = this.target != null && this.canAttack();
            boolean bl = block = attack && this.canAutoBlock();
            if (!block) {
                AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                if (this.isOldHypixel() && this.isBlocking && AlpeetcClient.moduleManager.getModule(NoSlow.class).isEnabled()) {
                    this.isBlocking = false;
                    this.stopBlock();
                }
                if (this.swapped) {
                    int handle = KillAura.mc.thePlayer.inventory.currentItem;
                    PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                    this.swapped = false;
                } else {
                    this.isBlocking = false;
                }
                this.fakeBlockState = false;
                this.blockTick = 0;
            }
            if (attack) {
                boolean swap = false;
                boolean postBlink = false;
                boolean blocked = false;
                if (block) {
                    block0 : switch ((Integer)this.autoBlock.getValue()) {
                        case 0: {
                            if (PlayerUtil.isUsingItem()) {
                                this.isBlocking = true;
                                if (!(this.isPlayerBlocking() || AlpeetcClient.playerStateManager.digging || AlpeetcClient.playerStateManager.placing)) {
                                    swap = true;
                                }
                            } else {
                                this.isBlocking = false;
                                if (this.isPlayerBlocking() && !AlpeetcClient.playerStateManager.digging && !AlpeetcClient.playerStateManager.placing) {
                                    this.stopBlock();
                                }
                            }
                            AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                            this.fakeBlockState = false;
                            break;
                        }
                        case 1: {
                            if (this.hasValidTarget()) {
                                if (!(this.isPlayerBlocking() || AlpeetcClient.playerStateManager.digging || AlpeetcClient.playerStateManager.placing)) {
                                    swap = true;
                                }
                                AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                this.isBlocking = true;
                                this.fakeBlockState = false;
                                break;
                            }
                            AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                            this.isBlocking = false;
                            this.fakeBlockState = false;
                            break;
                        }
                        case 2: {
                            switch ((Integer)this.hypixelMode.getValue()) {
                                case 0: {
                                    int handle;
                                    if (this.hasValidTarget()) {
                                        if (!AlpeetcClient.playerStateManager.digging && !AlpeetcClient.playerStateManager.placing) {
                                            switch (this.blockTick) {
                                                case 0: {
                                                    if (!this.isPlayerBlocking()) {
                                                        swap = true;
                                                    }
                                                    blocked = true;
                                                    this.blockTick = 1;
                                                    break;
                                                }
                                                case 1: {
                                                    attack = false;
                                                    this.blockTick = 2;
                                                    break;
                                                }
                                                case 2: {
                                                    if (this.isPlayerBlocking()) {
                                                        if (!((Boolean)this.noStop.getValue()).booleanValue()) {
                                                            handle = KillAura.mc.thePlayer.inventory.currentItem;
                                                            PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getAltSlot(handle)));
                                                            PacketUtil.sendPacket(new C09PacketHeldItemChange(handle % 7 + 2));
                                                            PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                                                        }
                                                        this.stopBlock();
                                                    }
                                                    if (((Boolean)this.test.getValue()).booleanValue()) {
                                                        if (this.testAttackTick >= (Integer)this.moreAttackDelay.getValue()) {
                                                            this.testAttackTick = 0;
                                                        } else {
                                                            ++this.testAttackTick;
                                                            attack = false;
                                                        }
                                                    } else {
                                                        attack = false;
                                                    }
                                                    this.blockTick = 0;
                                                    break;
                                                }
                                                default: {
                                                    this.blockTick = 0;
                                                }
                                            }
                                        }
                                        this.isBlocking = true;
                                        this.fakeBlockState = true;
                                        break block0;
                                    }
                                    AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                    this.isBlocking = false;
                                    this.fakeBlockState = false;
                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getSwapSlot()));
                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(KillAura.mc.thePlayer.inventory.currentItem));
                                    Velocity.extraAttacked = false;
                                    break block0;
                                }
                                case 1: {
                                    if (this.hasValidTarget()) {
                                        if (!AlpeetcClient.playerStateManager.digging && !AlpeetcClient.playerStateManager.placing) {
                                            switch (this.blockTick) {
                                                case 0: {
                                                    AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                                    if (!this.isPlayerBlocking()) {
                                                        swap = true;
                                                    }
                                                    this.blockTick = 1;
                                                    break;
                                                }
                                                case 1: {
                                                    attack = false;
                                                    this.blockTick = 2;
                                                    break;
                                                }
                                                case 2: {
                                                    AlpeetcClient.blinkManager.setBlinkState(true, BlinkModules.AUTO_BLOCK);
                                                    if (this.isPlayerBlocking()) {
                                                        this.stopBlock();
                                                    }
                                                    if (((Boolean)this.test.getValue()).booleanValue()) {
                                                        if (this.testAttackTick >= (Integer)this.moreAttackDelay.getValue()) {
                                                            this.testAttackTick = 0;
                                                        } else {
                                                            ++this.testAttackTick;
                                                            attack = false;
                                                        }
                                                    }
                                                    this.blockTick = 0;
                                                    break;
                                                }
                                                default: {
                                                    this.blockTick = 0;
                                                }
                                            }
                                        }
                                        this.isBlocking = true;
                                        this.fakeBlockState = true;
                                        break block0;
                                    }
                                    AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                    this.isBlocking = false;
                                    this.fakeBlockState = false;
                                    Velocity.extraAttacked = false;
                                    break block0;
                                }
                                case 2: {
                                    if (this.hasValidTarget()) {
                                        if (!AlpeetcClient.playerStateManager.digging && !AlpeetcClient.playerStateManager.placing) {
                                            if (this.blockTick + 1 == (Integer)this.startBlinkTick.getValue()) {
                                                blocked = true;
                                            }
                                            if (this.blockTick + 1 != (Integer)this.attackTick.getValue()) {
                                                attack = false;
                                            }
                                            if (this.blockTick + 1 == (Integer)this.startBlockTick.getValue() && !this.isPlayerBlocking()) {
                                                swap = true;
                                                if (((Boolean)this.postStartBlock.getValue()).booleanValue()) {
                                                    this.postBlock = true;
                                                }
                                            }
                                            if (this.blockTick + 1 == (Integer)this.stopBlinkTick.getValue()) {
                                                AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                            }
                                            if (this.blockTick + 1 == (Integer)this.swapTick.getValue()) {
                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getSwapSlot()));
                                                this.swapped = true;
                                            }
                                            if (this.blockTick + 1 == (Integer)this.switchBackTick.getValue() && this.swapped) {
                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(KillAura.mc.thePlayer.inventory.currentItem));
                                                this.swapped = false;
                                            }
                                            if (this.blockTick + 1 == (Integer)this.stopBlockTick.getValue() && this.isPlayerBlocking()) {
                                                this.stopBlock();
                                            }
                                            ++this.blockTick;
                                            if (this.blockTick >= (Integer)this.maxTick.getValue() - 1) {
                                                this.blockTick = 0;
                                            }
                                        }
                                        this.isBlocking = true;
                                        this.fakeBlockState = true;
                                        break block0;
                                    }
                                    if (this.swapped) {
                                        PacketUtil.sendPacket(new C09PacketHeldItemChange(KillAura.mc.thePlayer.inventory.currentItem));
                                        this.swapped = false;
                                    }
                                    AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                    this.isBlocking = false;
                                    this.fakeBlockState = false;
                                    Velocity.extraAttacked = false;
                                    break block0;
                                }
                                case 3: {
                                    int handle;
                                    switch ((Integer)this.lagMode.getValue()) {
                                        case 0: {
                                            if (this.hasValidTarget()) {
                                                if (!AlpeetcClient.playerStateManager.digging && !AlpeetcClient.playerStateManager.placing) {
                                                    switch (this.blockTick) {
                                                        case 0: {
                                                            AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                                            if (!this.isPlayerBlocking()) {
                                                                swap = true;
                                                            }
                                                            this.blockTick = 1;
                                                            break;
                                                        }
                                                        case 1: {
                                                            AlpeetcClient.blinkManager.setBlinkState(true, BlinkModules.AUTO_BLOCK);
                                                            if (this.isPlayerBlocking()) {
                                                                this.stopBlock();
                                                            }
                                                            attack = false;
                                                            if (this.attackDelayMS > 50L) break;
                                                            this.blockTick = 0;
                                                            break;
                                                        }
                                                        default: {
                                                            this.blockTick = 0;
                                                        }
                                                    }
                                                }
                                                this.isBlocking = true;
                                                this.fakeBlockState = (Boolean)this.alwaysRenderBlocking.getValue();
                                                break block0;
                                            }
                                            AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                            this.isBlocking = false;
                                            this.fakeBlockState = false;
                                            Velocity.extraAttacked = false;
                                            break block0;
                                        }
                                        case 1: {
                                            if (this.hasValidTarget()) {
                                                if (!AlpeetcClient.playerStateManager.digging && !AlpeetcClient.playerStateManager.placing) {
                                                    switch (this.blockTick) {
                                                        case 0: {
                                                            blocked = true;
                                                            if (!this.isPlayerBlocking()) {
                                                                swap = true;
                                                            }
                                                            this.blockTick = 1;
                                                            break;
                                                        }
                                                        case 1: {
                                                            if (this.isPlayerBlocking()) {
                                                                if (((Boolean)this.c09Instead.getValue()).booleanValue()) {
                                                                    handle = KillAura.mc.thePlayer.inventory.currentItem;
                                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getAltSlot(handle)));
                                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(handle % 7 + 2));
                                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                                                                } else {
                                                                    this.stopBlock();
                                                                }
                                                            }
                                                            attack = false;
                                                            this.blockTick = 2;
                                                            break;
                                                        }
                                                        case 2: {
                                                            AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                                            if (this.attackDelayMS > 50L) break;
                                                            this.blockTick = 0;
                                                            break;
                                                        }
                                                        default: {
                                                            this.blockTick = 0;
                                                        }
                                                    }
                                                }
                                                this.isBlocking = true;
                                                this.fakeBlockState = (Boolean)this.alwaysRenderBlocking.getValue();
                                                break block0;
                                            }
                                            AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                            this.isBlocking = false;
                                            this.fakeBlockState = false;
                                            Velocity.extraAttacked = false;
                                            break block0;
                                        }
                                        case 2: {
                                            if (this.hasValidTarget()) {
                                                if (!AlpeetcClient.playerStateManager.digging && !AlpeetcClient.playerStateManager.placing) {
                                                    switch (this.blockTick) {
                                                        case 0: {
                                                            blocked = true;
                                                            if (!this.isPlayerBlocking()) {
                                                                swap = true;
                                                            }
                                                            this.blockTick = 1;
                                                            break;
                                                        }
                                                        case 1: {
                                                            if (this.isPlayerBlocking() && ((Boolean)this.fullC09.getValue()).booleanValue()) {
                                                                handle = KillAura.mc.thePlayer.inventory.currentItem;
                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getAltSlot(handle)));
                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(handle % 7 + 2));
                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                                                                this.stopBlock();
                                                            }
                                                            attack = false;
                                                            this.blockTick = 2;
                                                            break;
                                                        }
                                                        case 2: {
                                                            handle = KillAura.mc.thePlayer.inventory.currentItem;
                                                            PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getAltSlot(handle)));
                                                            PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                                                            this.stopBlock();
                                                            this.blockTick = 3;
                                                            break;
                                                        }
                                                        case 3: {
                                                            AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                                            if (this.attackDelayMS > 50L) break;
                                                            this.blockTick = 0;
                                                            break;
                                                        }
                                                        default: {
                                                            this.blockTick = 0;
                                                        }
                                                    }
                                                }
                                                this.isBlocking = true;
                                                this.fakeBlockState = (Boolean)this.alwaysRenderBlocking.getValue();
                                                break block0;
                                            }
                                            AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                            this.isBlocking = false;
                                            this.fakeBlockState = false;
                                            Velocity.extraAttacked = false;
                                            break block0;
                                        }
                                        case 3: {
                                            if (this.hasValidTarget()) {
                                                if (!AlpeetcClient.playerStateManager.digging && !AlpeetcClient.playerStateManager.placing) {
                                                    switch (this.blockTick) {
                                                        case 0: {
                                                            blocked = true;
                                                            if (!this.isPlayerBlocking()) {
                                                                swap = true;
                                                            }
                                                            this.blockTick = 1;
                                                            break;
                                                        }
                                                        case 1: {
                                                            if (this.isPlayerBlocking()) {
                                                                if (((Boolean)this.fullC09.getValue()).booleanValue()) {
                                                                    handle = KillAura.mc.thePlayer.inventory.currentItem;
                                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getAltSlot(handle)));
                                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(handle % 7 + 2));
                                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                                                                }
                                                                this.stopBlock();
                                                            }
                                                            attack = false;
                                                            this.blockTick = 2;
                                                            break;
                                                        }
                                                        case 2: {
                                                            blocked = true;
                                                            if (!this.isPlayerBlocking()) {
                                                                swap = true;
                                                            }
                                                            this.blockTick = 3;
                                                            break;
                                                        }
                                                        case 3: {
                                                            if (this.isPlayerBlocking()) {
                                                                if (((Boolean)this.fullC09.getValue()).booleanValue()) {
                                                                    handle = KillAura.mc.thePlayer.inventory.currentItem;
                                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getAltSlot(handle)));
                                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(handle % 7 + 2));
                                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                                                                }
                                                                this.stopBlock();
                                                            }
                                                            this.blockTick = 4;
                                                            break;
                                                        }
                                                        case 4: {
                                                            AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                                            if (this.attackDelayMS > 50L) break;
                                                            this.blockTick = 0;
                                                            break;
                                                        }
                                                        default: {
                                                            this.blockTick = 0;
                                                        }
                                                    }
                                                }
                                                this.isBlocking = true;
                                                this.fakeBlockState = (Boolean)this.alwaysRenderBlocking.getValue();
                                                break block0;
                                            }
                                            AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                            this.isBlocking = false;
                                            this.fakeBlockState = false;
                                            Velocity.extraAttacked = false;
                                            break block0;
                                        }
                                        case 4: {
                                            if (this.hasValidTarget()) {
                                                if (!AlpeetcClient.playerStateManager.digging && !AlpeetcClient.playerStateManager.placing) {
                                                    switch (this.blockTick) {
                                                        case 0: {
                                                            if (!this.isPlayerBlocking()) {
                                                                swap = true;
                                                            }
                                                            this.blockTick = 1;
                                                            break;
                                                        }
                                                        case 1: {
                                                            AlpeetcClient.blinkManager.setBlinkState(true, BlinkModules.AUTO_BLOCK);
                                                            if (this.isPlayerBlocking()) {
                                                                if (((Boolean)this.fullC09.getValue()).booleanValue()) {
                                                                    handle = KillAura.mc.thePlayer.inventory.currentItem;
                                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getAltSlot(handle)));
                                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(handle % 7 + 2));
                                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                                                                }
                                                                this.stopBlock();
                                                            }
                                                            attack = false;
                                                            this.blockTick = 2;
                                                            break;
                                                        }
                                                        case 2: {
                                                            if (((Boolean)this.fullC09.getValue()).booleanValue()) {
                                                                handle = KillAura.mc.thePlayer.inventory.currentItem;
                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getAltSlot(handle)));
                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(handle % 7 + 2));
                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                                                                this.stopBlock();
                                                            }
                                                            attack = false;
                                                            this.blockTick = 3;
                                                            break;
                                                        }
                                                        case 3: {
                                                            if (((Boolean)this.fullC09.getValue()).booleanValue()) {
                                                                handle = KillAura.mc.thePlayer.inventory.currentItem;
                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getAltSlot(handle)));
                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(handle % 7 + 2));
                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                                                                this.stopBlock();
                                                            }
                                                            attack = false;
                                                            this.blockTick = 4;
                                                            break;
                                                        }
                                                        case 4: {
                                                            AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                                            if (this.attackDelayMS > 50L) break;
                                                            this.blockTick = 0;
                                                            break;
                                                        }
                                                        case 5: {
                                                            if (this.hasValidTarget()) {
                                                                if (!AlpeetcClient.playerStateManager.digging && !AlpeetcClient.playerStateManager.placing) {
                                                                    switch (this.blockTick) {
                                                                        case 0: {
                                                                            if (!this.isPlayerBlocking()) {
                                                                                swap = true;
                                                                            }
                                                                            this.blockTick = 1;
                                                                            break;
                                                                        }
                                                                        case 1: {
                                                                            AlpeetcClient.blinkManager.setBlinkState(true, BlinkModules.AUTO_BLOCK);
                                                                            if (this.isPlayerBlocking()) {
                                                                                if (((Boolean)this.fullC09.getValue()).booleanValue()) {
                                                                                    handle = KillAura.mc.thePlayer.inventory.currentItem;
                                                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getAltSlot(handle)));
                                                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(handle % 7 + 2));
                                                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                                                                                    this.stopBlock();
                                                                                }
                                                                            }
                                                                            attack = false;
                                                                            this.blockTick = 2;
                                                                            break;
                                                                        }
                                                                        case 2: {
                                                                            if (((Boolean)this.fullC09.getValue()).booleanValue()) {
                                                                                handle = KillAura.mc.thePlayer.inventory.currentItem;
                                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getAltSlot(handle)));
                                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(handle % 7 + 2));
                                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                                                                                this.stopBlock();
                                                                            }
                                                                            attack = false;
                                                                            this.blockTick = 3;
                                                                            break;
                                                                        }
                                                                        case 3: {
                                                                            if (((Boolean)this.fullC09.getValue()).booleanValue()) {
                                                                                handle = KillAura.mc.thePlayer.inventory.currentItem;
                                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getAltSlot(handle)));
                                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(handle % 7 + 2));
                                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                                                                                this.stopBlock();
                                                                            }
                                                                            attack = false;
                                                                            this.blockTick = 4;
                                                                            break;
                                                                        }
                                                                        case 4: {
                                                                            if (((Boolean)this.fullC09.getValue()).booleanValue()) {
                                                                                handle = KillAura.mc.thePlayer.inventory.currentItem;
                                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getAltSlot(handle)));
                                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(handle % 7 + 2));
                                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                                                                                this.stopBlock();
                                                                            }
                                                                            attack = false;
                                                                            this.blockTick = 5;
                                                                            break;
                                                                        }
                                                                        case 5: {
                                                                            AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                                                            if (this.attackDelayMS > 50L) break;
                                                                            this.blockTick = 0;
                                                                            break;
                                                                        }
                                                                        default: {
                                                                            this.blockTick = 0;
                                                                        }
                                                                    }
                                                                }
                                                                this.isBlocking = true;
                                                                this.fakeBlockState = (Boolean)this.alwaysRenderBlocking.getValue();
                                                                break block0;
                                                            }
                                                            AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                                            this.isBlocking = false;
                                                            this.fakeBlockState = false;
                                                            Velocity.extraAttacked = false;
                                                            break block0;
                                                        }
                                                        case 6: {
                                                            if (this.hasValidTarget()) {
                                                                if (!AlpeetcClient.playerStateManager.digging && !AlpeetcClient.playerStateManager.placing) {
                                                                    switch (this.blockTick) {
                                                                        case 0: {
                                                                            if (!this.isPlayerBlocking()) {
                                                                                swap = true;
                                                                            }
                                                                            postBlink = true;
                                                                            this.blockTick = 1;
                                                                            break;
                                                                        }
                                                                        case 1: {
                                                                            AlpeetcClient.blinkManager.setBlinkState(true, BlinkModules.AUTO_BLOCK);
                                                                            if (this.isPlayerBlocking()) {
                                                                                if (((Boolean)this.fullC09.getValue()).booleanValue()) {
                                                                                    handle = KillAura.mc.thePlayer.inventory.currentItem;
                                                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getAltSlot(handle)));
                                                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(handle % 7 + 2));
                                                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                                                                                }
                                                                                this.stopBlock();
                                                                            }
                                                                            attack = false;
                                                                            this.blockTick = 2;
                                                                            break;
                                                                        }
                                                                        case 2: {
                                                                            if (((Boolean)this.fullC09.getValue()).booleanValue()) {
                                                                                handle = KillAura.mc.thePlayer.inventory.currentItem;
                                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getAltSlot(handle)));
                                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(handle % 7 + 2));
                                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                                                                                this.stopBlock();
                                                                            }
                                                                            attack = false;
                                                                            if (this.attackDelayMS > 50L) break;
                                                                            this.blockTick = 0;
                                                                            break;
                                                                        }
                                                                        default: {
                                                                            this.blockTick = 0;
                                                                        }
                                                                    }
                                                                }
                                                                this.isBlocking = true;
                                                                this.fakeBlockState = (Boolean)this.alwaysRenderBlocking.getValue();
                                                                break block0;
                                                            }
                                                            AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                                            this.isBlocking = false;
                                                            this.fakeBlockState = false;
                                                            Velocity.extraAttacked = false;
                                                            break block0;
                                                        }
                                                        case 7: {
                                                            if (this.hasValidTarget()) {
                                                                if (!AlpeetcClient.playerStateManager.digging && !AlpeetcClient.playerStateManager.placing) {
                                                                    switch (this.blockTick) {
                                                                        case 0: {
                                                                            if (!this.isPlayerBlocking()) {
                                                                                swap = true;
                                                                            }
                                                                            postBlink = true;
                                                                            this.blockTick = 1;
                                                                            break;
                                                                        }
                                                                        case 1: {
                                                                            AlpeetcClient.blinkManager.setBlinkState(true, BlinkModules.AUTO_BLOCK);
                                                                            if (this.isPlayerBlocking()) {
                                                                                if (((Boolean)this.fullC09.getValue()).booleanValue()) {
                                                                                    handle = KillAura.mc.thePlayer.inventory.currentItem;
                                                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getAltSlot(handle)));
                                                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(handle % 7 + 2));
                                                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                                                                                }
                                                                                this.stopBlock();
                                                                            }
                                                                            attack = false;
                                                                            this.blockTick = 2;
                                                                            break;
                                                                        }
                                                                        case 2: {
                                                                            if (((Boolean)this.fullC09.getValue()).booleanValue()) {
                                                                                handle = KillAura.mc.thePlayer.inventory.currentItem;
                                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getAltSlot(handle)));
                                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(handle % 7 + 2));
                                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                                                                                this.stopBlock();
                                                                            }
                                                                            attack = false;
                                                                            this.blockTick = 3;
                                                                            break;
                                                                        }
                                                                        case 3: {
                                                                            if (((Boolean)this.fullC09.getValue()).booleanValue()) {
                                                                                handle = KillAura.mc.thePlayer.inventory.currentItem;
                                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getAltSlot(handle)));
                                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(handle % 7 + 2));
                                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                                                                                this.stopBlock();
                                                                            }
                                                                            attack = false;
                                                                            if (this.attackDelayMS > 50L) break;
                                                                            this.blockTick = 0;
                                                                            break;
                                                                        }
                                                                        default: {
                                                                            this.blockTick = 0;
                                                                        }
                                                                    }
                                                                }
                                                                this.isBlocking = true;
                                                                this.fakeBlockState = (Boolean)this.alwaysRenderBlocking.getValue();
                                                                break block0;
                                                            }
                                                            AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                                            this.isBlocking = false;
                                                            this.fakeBlockState = false;
                                                            Velocity.extraAttacked = false;
                                                            break block0;
                                                        }
                                                        case 8: {
                                                            if (this.hasValidTarget()) {
                                                                if (!AlpeetcClient.playerStateManager.digging && !AlpeetcClient.playerStateManager.placing) {
                                                                    switch (this.blockTick) {
                                                                        case 0: {
                                                                            if (!this.isPlayerBlocking()) {
                                                                                swap = true;
                                                                            }
                                                                            this.blockTick = 1;
                                                                            break;
                                                                        }
                                                                        case 1: {
                                                                            AlpeetcClient.blinkManager.setBlinkState(true, BlinkModules.AUTO_BLOCK);
                                                                            if (this.isPlayerBlocking()) {
                                                                                if (((Boolean)this.fullC09.getValue()).booleanValue()) {
                                                                                    handle = KillAura.mc.thePlayer.inventory.currentItem;
                                                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getAltSlot(handle)));
                                                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(handle % 7 + 2));
                                                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                                                                                }
                                                                                this.stopBlock();
                                                                            }
                                                                            attack = false;
                                                                            this.blockTick = 2;
                                                                            break;
                                                                        }
                                                                        case 2: {
                                                                            if (((Boolean)this.fullC09.getValue()).booleanValue()) {
                                                                                handle = KillAura.mc.thePlayer.inventory.currentItem;
                                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getAltSlot(handle)));
                                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(handle % 7 + 2));
                                                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                                                                                this.stopBlock();
                                                                            }
                                                                            attack = false;
                                                                            this.blockTick = 3;
                                                                            break;
                                                                        }
                                                                        case 3: {
                                                                            postBlink = true;
                                                                            if (this.attackDelayMS > 50L) break;
                                                                            this.blockTick = 0;
                                                                            break;
                                                                        }
                                                                        default: {
                                                                            this.blockTick = 0;
                                                                        }
                                                                    }
                                                                }
                                                                this.isBlocking = true;
                                                                this.fakeBlockState = (Boolean)this.alwaysRenderBlocking.getValue();
                                                                break block0;
                                                            }
                                                            AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                                            this.isBlocking = false;
                                                            this.fakeBlockState = false;
                                                            Velocity.extraAttacked = false;
                                                            break block0;
                                                        }
                                                        case 9: {
                                                            if (this.hasValidTarget()) {
                                                                if (!AlpeetcClient.playerStateManager.digging && !AlpeetcClient.playerStateManager.placing) {
                                                                    switch (this.blockTick) {
                                                                        case 0: {
                                                                            if (!this.isPlayerBlocking()) {
                                                                                swap = true;
                                                                            }
                                                                            postBlinkReset = true;
                                                                            this.blockTick = 1;
                                                                            break;
                                                                        }
                                                                        case 1: {
                                                                            AlpeetcClient.blinkManager.setBlinkState(true, BlinkModules.AUTO_BLOCK);
                                                                            if (this.isPlayerBlocking()) {
                                                                                this.postSwap = true;
                                                                            }
                                                                            attack = false;
                                                                            this.blockTick = 2;
                                                                            break;
                                                                        }
                                                                        case 2: {
                                                                            handle = KillAura.mc.thePlayer.inventory.currentItem;
                                                                            PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getAltSlot(handle)));
                                                                            PacketUtil.sendPacket(new C09PacketHeldItemChange(handle % 7 + 2));
                                                                            PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                                                                            attack = false;
                                                                            if (this.attackDelayMS > 50L) break;
                                                                            this.blockTick = 0;
                                                                            break;
                                                                        }
                                                                        default: {
                                                                            this.blockTick = 0;
                                                                        }
                                                                    }
                                                                }
                                                                this.isBlocking = true;
                                                                this.fakeBlockState = (Boolean)this.alwaysRenderBlocking.getValue();
                                                                break block0;
                                                            }
                                                            AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                                            this.isBlocking = false;
                                                            this.fakeBlockState = false;
                                                            Velocity.extraAttacked = false;
                                                            break block0;
                                                        }
                                                        default: {
                                                            this.blockTick = 0;
                                                        }
                                                    }
                                                }
                                                this.isBlocking = true;
                                                this.fakeBlockState = (Boolean)this.alwaysRenderBlocking.getValue();
                                                break block0;
                                            }
                                            AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                            this.isBlocking = false;
                                            this.fakeBlockState = false;
                                            Velocity.extraAttacked = false;
                                            break block0;
                                        }
                                    }
                                    break block0;
                                }
                            }
                            break;
                        }
                        case 3: {
                            if (this.hasValidTarget()) {
                                if (!AlpeetcClient.playerStateManager.digging && !AlpeetcClient.playerStateManager.placing) {
                                    switch (this.blockTick) {
                                        case 0: {
                                            if (!this.isPlayerBlocking()) {
                                                swap = true;
                                            }
                                            this.blockTick = 1;
                                            break;
                                        }
                                        case 1: {
                                            if (this.isPlayerBlocking()) {
                                                this.stopBlock();
                                                attack = false;
                                            }
                                            if (this.attackDelayMS > 50L) break;
                                            this.blockTick = 0;
                                            break;
                                        }
                                        default: {
                                            this.blockTick = 0;
                                        }
                                    }
                                }
                                AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                this.isBlocking = true;
                                this.fakeBlockState = false;
                                break;
                            }
                            AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                            this.isBlocking = false;
                            this.fakeBlockState = false;
                            Velocity.extraAttacked = false;
                            break;
                        }
                        case 4: {
                            AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                            this.isBlocking = false;
                            this.fakeBlockState = this.hasValidTarget();
                            if (!PlayerUtil.isUsingItem() || this.isPlayerBlocking() || AlpeetcClient.playerStateManager.digging || AlpeetcClient.playerStateManager.placing) break;
                            swap = true;
                        }
                    }
                }
                boolean attacked = false;
                if (this.isBoxInSwingRange(this.target.getBox())) {
                    if ((Integer)this.rotations.getValue() == 2 || (Integer)this.rotations.getValue() == 3) {
                        AxisAlignedBB box = this.target.getBox();
                        float currentYaw = event.getYaw();
                        float currentPitch = event.getPitch();
                        float angleStep = (float)((Integer)this.angleStep.getValue()).intValue() + RandomUtil.nextFloat(-5.0f, 5.0f);
                        float smooth = (float)((Integer)this.smoothing.getValue()).intValue() / 100.0f;
                        int mode = (Integer)this.rotationMode.getValue();
                        float[] rotations = mode == 1 ? RotationUtil.nearestRotation(box, currentYaw, currentPitch, angleStep, smooth) : (mode == 2 ? (this.isNormalTargetVisible(box) ? RotationUtil.getRotationsToBox(box, currentYaw, currentPitch, angleStep, smooth) : RotationUtil.nearestRotation(box, currentYaw, currentPitch, angleStep, smooth)) : RotationUtil.getRotationsToBox(box, currentYaw, currentPitch, angleStep, smooth));
                        if (rotations != null) {
                            event.setRotation(rotations[0], rotations[1], 1);
                        }
                        if ((Integer)this.rotations.getValue() == 3 && rotations != null) {
                            AlpeetcClient.rotationManager.setRotation(rotations[0], rotations[1], 1, true);
                        }
                        if (((Integer)this.moveFix.getValue() != 0 || (Integer)this.rotations.getValue() == 3) && rotations != null) {
                            event.setPervRotation(rotations[0], 1);
                        }
                    }
                    if (!(!attack || Velocity.cancellingKillAuraAttack && AlpeetcClient.moduleManager.getModule(Velocity.class).isEnabled())) {
                        attacked = this.performAttack(event.getNewYaw(), event.getNewPitch());
                    }
                }
                if (postBlink) {
                    AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                }
                if (swap) {
                    if (attacked) {
                        this.interactAttack(event.getNewYaw(), event.getNewPitch());
                    } else if (!this.postBlock) {
                        this.sendUseItem();
                    }
                }
                if (blocked) {
                    AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                    AlpeetcClient.blinkManager.setBlinkState(true, BlinkModules.AUTO_BLOCK);
                }
            }
        }
        if (event.getType() == EventType.POST && this.isEnabled()) {
            if (this.postBlinkReset) {
                AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                AlpeetcClient.blinkManager.setBlinkState(true, BlinkModules.AUTO_BLOCK);
                this.postBlinkReset = false;
            }
            if (this.postSwap) {
                PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getSwapSlot()));
                mc.getNetHandler().addToSendQueue((Packet)new C17PacketCustomPayload("send", new PacketBuffer(Unpooled.buffer())));
                PacketUtil.sendPacket(new C09PacketHeldItemChange(KillAura.mc.thePlayer.inventory.currentItem));
                this.stopBlock();
                this.postSwap = false;
            }
            if (this.postBlock) {
                this.sendUseItem();
                this.postBlock = false;
            }
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled()) {
            switch (event.getType()) {
                case PRE: {
                    if (this.target == null || !this.isValidTarget(this.target.getEntity()) || !this.isBoxInAttackRange(this.target.getBox()) || !this.isBoxInSwingRange(this.target.getBox()) || this.timer.hasTimeElapsed(((Integer)this.switchDelay.getValue()).longValue())) {
                        this.timer.reset();
                        ArrayList<EntityLivingBase> targets = new ArrayList<EntityLivingBase>();
                        for (Entity entity : KillAura.mc.theWorld.loadedEntityList) {
                            if (!(entity instanceof EntityLivingBase) || !this.isValidTarget((EntityLivingBase)entity) || !this.isInRange((EntityLivingBase)entity)) continue;
                            targets.add((EntityLivingBase)entity);
                        }
                        if (targets.isEmpty()) {
                            this.target = null;
                        } else {
                            if (targets.stream().anyMatch(this::isInSwingRange)) {
                                targets.removeIf(entityLivingBase -> !this.isInSwingRange((EntityLivingBase)entityLivingBase));
                            }
                            if (targets.stream().anyMatch(this::isInAttackRange)) {
                                targets.removeIf(entityLivingBase -> !this.isInAttackRange((EntityLivingBase)entityLivingBase));
                            }
                            if (targets.stream().anyMatch(this::isPlayerTarget)) {
                                targets.removeIf(entityLivingBase -> !this.isPlayerTarget((EntityLivingBase)entityLivingBase));
                            }
                            targets.sort((entityLivingBase1, entityLivingBase2) -> {
                                int sortBase = 0;
                                switch ((Integer)this.sort.getValue()) {
                                    case 1: {
                                        sortBase = Float.compare(TeamUtil.getHealthScore(entityLivingBase1), TeamUtil.getHealthScore(entityLivingBase2));
                                        break;
                                    }
                                    case 2: {
                                        sortBase = Integer.compare(entityLivingBase1.hurtResistantTime, entityLivingBase2.hurtResistantTime);
                                        break;
                                    }
                                    case 3: {
                                        sortBase = Float.compare(RotationUtil.angleToEntity((Entity)entityLivingBase1), RotationUtil.angleToEntity((Entity)entityLivingBase2));
                                    }
                                }
                                return sortBase != 0 ? sortBase : Double.compare(RotationUtil.distanceToEntity((Entity)entityLivingBase1), RotationUtil.distanceToEntity((Entity)entityLivingBase2));
                            });
                            if ((Integer)this.mode.getValue() == 1 && this.hitRegistered) {
                                this.hitRegistered = false;
                                ++this.switchTick;
                            }
                            if ((Integer)this.mode.getValue() == 0 || this.switchTick >= targets.size()) {
                                this.switchTick = 0;
                            }
                            this.target = new AttackData((EntityLivingBase)targets.get(this.switchTick));
                        }
                    }
                    if (this.target == null) break;
                    this.target = new AttackData(this.target.getEntity());
                    break;
                }
                case POST: {
                    if (!this.isPlayerBlocking() || KillAura.mc.thePlayer.isBlocking()) break;
                    KillAura.mc.thePlayer.setItemInUse(KillAura.mc.thePlayer.getHeldItem(), KillAura.mc.thePlayer.getHeldItem().getMaxItemUseDuration());
                }
            }
        }
    }

    @EventTarget(value=4)
    public void onPacket(PacketEvent event) {
        if (this.isEnabled() && !event.isCancelled()) {
            C07PacketPlayerDigging packet;
            if (event.getPacket() instanceof C07PacketPlayerDigging && (packet = (C07PacketPlayerDigging)event.getPacket()).getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                this.blockingState = false;
            }
            if (event.getPacket() instanceof C09PacketHeldItemChange) {
                this.blockingState = false;
                if (this.isBlocking) {
                    KillAura.mc.thePlayer.stopUsingItem();
                }
            }
        }
    }

    @EventTarget
    public void onMove(MoveInputEvent event) {
        if (this.isEnabled() && (Integer)this.moveFix.getValue() == 1 && (Integer)this.rotations.getValue() != 3 && RotationState.isActived() && RotationState.getPriority() == 1.0f && MoveUtil.isForwardPressed()) {
            MoveUtil.fixStrafe(RotationState.getSmoothedYaw());
        }
    }

    @EventTarget
    public void onRender(Render3DEvent event) {
    }

    @EventTarget
    public void onLeftClick(LeftClickMouseEvent event) {
        if (this.isBlocking) {
            event.setCancelled(true);
        } else if (this.isEnabled() && this.target != null && this.canAttack()) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onRightClick(RightClickMouseEvent event) {
        if (this.isBlocking) {
            event.setCancelled(true);
        } else if (this.isEnabled() && this.target != null && this.canAttack() && !((Boolean)this.allowPlayerBlocking.getValue()).booleanValue()) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onHitBlock(HitBlockEvent event) {
        if (this.isBlocking) {
            event.setCancelled(true);
        } else if (this.isEnabled() && this.target != null && this.canAttack()) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onCancelUse(CancelUseEvent event) {
        if (this.isBlocking) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onEnabled() {
        this.target = null;
        this.switchTick = 0;
        this.hitRegistered = false;
        this.attackDelayMS = 0L;
        this.blockTick = 0;
        this.bufferPending = false;
    }

    @Override
    public void onDisabled() {
        AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
        Velocity.extraAttacked = false;
        this.blockingState = false;
        this.fakeBlockState = false;
        this.bufferPending = false;
        if (this.swapped) {
            int handle = KillAura.mc.thePlayer.inventory.currentItem;
            PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
            this.swapped = false;
        }
        if (this.isOldHypixel() && this.isBlocking && AlpeetcClient.moduleManager.getModule(NoSlow.class).isEnabled()) {
            this.isBlocking = false;
            this.stopBlock();
        } else {
            this.isBlocking = false;
        }
    }

    @Override
    public void verifyValue(String mode) {
        if (!this.autoBlock.getName().equals(mode) && !this.autoBlockCPS.getName().equals(mode)) {
            if (this.swingRange.getName().equals(mode)) {
                if (((Float)this.swingRange.getValue()).floatValue() < ((Float)this.attackRange.getValue()).floatValue()) {
                    this.attackRange.setValue(this.swingRange.getValue());
                }
            } else if (this.attackRange.getName().equals(mode)) {
                if (((Float)this.swingRange.getValue()).floatValue() < ((Float)this.attackRange.getValue()).floatValue()) {
                    this.swingRange.setValue(this.attackRange.getValue());
                }
            } else if (this.minCPS.getName().equals(mode)) {
                if ((Integer)this.minCPS.getValue() > (Integer)this.maxCPS.getValue()) {
                    this.maxCPS.setValue(this.minCPS.getValue());
                }
            } else if (this.maxCPS.getName().equals(mode) && (Integer)this.minCPS.getValue() > (Integer)this.maxCPS.getValue()) {
                this.minCPS.setValue(this.maxCPS.getValue());
            }
        }
    }

    @Override
    public String[] getSuffix() {
        return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.mode.getModeString())};
    }

    private void applyBypass(EntityLivingBase target) {
        if (AlpeetcClient.moduleManager.getModule(Bypass.class).isEnabled()) {
            int acIndex = (Integer)this.bypassMode.getValue();
            if (((Boolean)this.gcdBypass.getValue()).booleanValue() && BypassManager.needsGCD(BypassManager.currentAC)) {
            }
            if (((Boolean)this.rotationNoise.getValue()).booleanValue()) {
            }
            if (((Boolean)this.extraArmAnimation.getValue()).booleanValue() && BypassManager.needsArmAnimation(BypassManager.currentAC)) {
                mc.getNetHandler().addToSendQueue((Packet)new C0APacketAnimation());
            }
        }
    }

    public static class AttackData {
        private final EntityLivingBase entity;
        private final AxisAlignedBB box;
        private final double x;
        private final double y;
        private final double z;

        public AttackData(EntityLivingBase entityLivingBase) {
            this.entity = entityLivingBase;
            double collisionBorderSize = entityLivingBase.getCollisionBorderSize();
            this.box = entityLivingBase.getEntityBoundingBox().expand(collisionBorderSize, collisionBorderSize, collisionBorderSize);
            this.x = entityLivingBase.posX;
            this.y = entityLivingBase.posY;
            this.z = entityLivingBase.posZ;
        }

        public EntityLivingBase getEntity() {
            return this.entity;
        }

        public AxisAlignedBB getBox() {
            return this.box;
        }

        public double getX() {
            return this.x;
        }

        public double getY() {
            return this.y;
        }

        public double getZ() {
            return this.z;
        }
    }
}

