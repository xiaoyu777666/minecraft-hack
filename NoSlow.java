/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.CaseFormat
 *  net.minecraft.client.Minecraft
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.passive.EntityVillager
 *  net.minecraft.network.play.client.C07PacketPlayerDigging
 *  net.minecraft.network.play.client.C07PacketPlayerDigging$Action
 *  net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
 *  net.minecraft.network.play.client.C09PacketHeldItemChange
 *  net.minecraft.util.BlockPos
 *  net.minecraft.util.EnumFacing
 *  xyz.wagyourtail.jvmdg.j11.NestMembers
 */
package alpeetcclient.module.modules.movement;

import com.google.common.base.CaseFormat;
import alpeetcclient.AlpeetcClient;
import alpeetcclient.enums.BlinkModules;
import alpeetcclient.enums.FloatModules;
import alpeetcclient.event.EventTarget;
import alpeetcclient.event.types.EventType;
import alpeetcclient.events.LivingUpdateEvent;
import alpeetcclient.events.PlayerUpdateEvent;
import alpeetcclient.events.RightClickMouseEvent;
import alpeetcclient.events.UpdateEvent;
import alpeetcclient.mixin.IAccessorPlayerControllerMP;
import alpeetcclient.module.Module;
import alpeetcclient.module.modules.combat.KillAura;
import alpeetcclient.module.modules.misc.Disabler;
import alpeetcclient.property.properties.BooleanProperty;
import alpeetcclient.property.properties.IntProperty;
import alpeetcclient.property.properties.ModeProperty;
import alpeetcclient.property.properties.PercentProperty;
import alpeetcclient.util.BlockUtil;
import alpeetcclient.util.ItemUtil;
import alpeetcclient.util.PacketUtil;
import alpeetcclient.util.PlayerUtil;
import alpeetcclient.util.TeamUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
public class NoSlow
extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public final ModeProperty swordMode = new ModeProperty("Sword Mode", 1, new String[]{"None", "Vanilla", "BlinkSemi", "Prediction", "PredictionSemi", "Test"});
    public final BooleanProperty tick0 = new BooleanProperty("Tick 0", true, () -> (Integer)this.swordMode.getValue() == 4);
    public final BooleanProperty tick1 = new BooleanProperty("Tick 1", true, () -> (Integer)this.swordMode.getValue() == 4);
    public final BooleanProperty tick2 = new BooleanProperty("Tick 2", false, () -> (Integer)this.swordMode.getValue() == 4);
    public final BooleanProperty tick3 = new BooleanProperty("Tick 3", false, () -> (Integer)this.swordMode.getValue() == 4);
    public final BooleanProperty tick4 = new BooleanProperty("Tick 4", false, () -> (Integer)this.swordMode.getValue() == 4);
    public final BooleanProperty slowOnRelease = new BooleanProperty("SlowOnRelease", true, () -> (Integer)this.swordMode.getValue() == 3);
    public final IntProperty swapDelay = new IntProperty("Slow Delay", 0, 0, 3, () -> (Integer)this.swordMode.getValue() == 3);
    public final PercentProperty swordMotion = new PercentProperty("Sword Motion", 100, () -> (Integer)this.swordMode.getValue() != 0);
    public final BooleanProperty swordSprint = new BooleanProperty("Sword Sprint", true, () -> (Integer)this.swordMode.getValue() != 0);
    public final BooleanProperty onlyKillAuraAutoBlock = new BooleanProperty("Only Kill Aura Auto Block", false, () -> (Integer)this.swordMode.getValue() != 0);
    public final ModeProperty foodMode = new ModeProperty("Food Mode", 0, new String[]{"None", "Vanilla", "Float"});
    public final PercentProperty foodMotion = new PercentProperty("Food Motion", 100, () -> (Integer)this.foodMode.getValue() != 0);
    public final BooleanProperty foodSprint = new BooleanProperty("Food Sprint", true, () -> (Integer)this.foodMode.getValue() != 0);
    public final ModeProperty bowMode = new ModeProperty("Bow Mode", 0, new String[]{"None", "Vanilla", "Float"});
    public final PercentProperty bowMotion = new PercentProperty("Bow Motion", 100, () -> (Integer)this.bowMode.getValue() != 0);
    public final BooleanProperty bowSprint = new BooleanProperty("Bow Sprint", true, () -> (Integer)this.bowMode.getValue() != 0);
    private int lastSlot = -1;
    private int delay = 0;
    private int blinkDelay = 0;
    private boolean swapped = false;

    public NoSlow() {
        super("NoSlow", false);
    }

    public boolean isSwordActive() {
        return (Integer)this.swordMode.getValue() != 0 && ItemUtil.isHoldingSword() && ((Boolean)this.onlyKillAuraAutoBlock.getValue() == false || this.isKillAuraAutoBlocking());
    }

    public boolean isFoodActive() {
        return (Integer)this.foodMode.getValue() != 0 && ItemUtil.isEating();
    }

    public boolean isBowActive() {
        return (Integer)this.bowMode.getValue() != 0 && ItemUtil.isUsingBow();
    }

    public boolean isFloatMode() {
        return (Integer)this.foodMode.getValue() == 2 && ItemUtil.isEating() || (Integer)this.bowMode.getValue() == 2 && ItemUtil.isUsingBow();
    }

    private boolean isKillAuraAutoBlocking() {
        KillAura aura = (KillAura)AlpeetcClient.moduleManager.modules.get(KillAura.class);
        if (!aura.shouldAutoBlock() || !aura.isEnabled()) {
            return false;
        }
        return aura.isBlocking;
    }

    public boolean isAnyActive() {
        if ((Integer)this.swordMode.getValue() != 2 && (Integer)this.swordMode.getValue() != 3 && (Integer)this.swordMode.getValue() != 4 && (Integer)this.swordMode.getValue() != 5) {
            return NoSlow.mc.thePlayer.isUsingItem() && (this.isSwordActive() || this.isFoodActive() || this.isBowActive());
        }
        if ((Integer)this.swordMode.getValue() == 2 && this.isSwordActive()) {
            return this.blinkDelay == 2;
        }
        if ((Integer)this.swordMode.getValue() == 3 && this.isSwordActive()) {
            KillAura killAura = (KillAura)AlpeetcClient.moduleManager.getModule(KillAura.class);
            if (!((Boolean)this.slowOnRelease.getValue()).booleanValue() || killAura.blockTick != 0) {
                return this.delay == 0;
            }
        } else {
            if ((Integer)this.swordMode.getValue() == 4 && this.isSwordActive()) {
                KillAura killAura = (KillAura)AlpeetcClient.moduleManager.getModule(KillAura.class);
                return killAura.isEnabled() && killAura.shouldAutoBlock() && ((Boolean)this.tick0.getValue() != false && killAura.blockTick == 0 || (Boolean)this.tick1.getValue() != false && killAura.blockTick == 1 || (Boolean)this.tick2.getValue() != false && killAura.blockTick == 2 || (Boolean)this.tick3.getValue() != false && killAura.blockTick == 3 || (Boolean)this.tick4.getValue() != false && killAura.blockTick == 4);
            }
            if ((Integer)this.swordMode.getValue() == 5 && this.isSwordActive()) {
                return !this.swapped;
            }
        }
        return false;
    }

    public boolean canSprint() {
        return this.isSwordActive() && (Boolean)this.swordSprint.getValue() != false || this.isFoodActive() && (Boolean)this.foodSprint.getValue() != false || this.isBowActive() && (Boolean)this.bowSprint.getValue() != false;
    }

    public int getMotionMultiplier() {
        if (ItemUtil.isHoldingSword()) {
            return (Integer)this.swordMotion.getValue();
        }
        if (ItemUtil.isEating()) {
            return (Integer)this.foodMotion.getValue();
        }
        return ItemUtil.isUsingBow() ? (Integer)this.bowMotion.getValue() : 100;
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        KillAura killAura = (KillAura)AlpeetcClient.moduleManager.getModule(KillAura.class);
        if (this.isSwordActive() && PlayerUtil.isUsingItem()) {
            int handle;
            if ((Integer)this.swordMode.getValue() == 3 && event.getType() == EventType.PRE) {
                --this.delay;
                if (this.delay < 0) {
                    if (!((Boolean)this.slowOnRelease.getValue()).booleanValue() || killAura.blockTick != 0) {
                        handle = NoSlow.mc.thePlayer.inventory.currentItem;
                        PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getAltSlot(handle)));
                        PacketUtil.sendPacket(new C09PacketHeldItemChange(handle % 7 + 2));
                        PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                    }
                    this.delay = (Integer)this.swapDelay.getValue();
                }
            }
            if ((Integer)this.swordMode.getValue() == 2 && event.getType() == EventType.PRE) {
                if (this.blinkDelay == 2) {
                    int randomSlot = Disabler.getSwapSlot();
                    PacketUtil.sendPacket(new C09PacketHeldItemChange(randomSlot));
                    PacketUtil.sendPacket(new C09PacketHeldItemChange(NoSlow.mc.thePlayer.inventory.currentItem));
                    PacketUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    NoSlow.mc.thePlayer.stopUsingItem();
                    this.blinkDelay = 0;
                } else {
                    if (!this.isKillAuraAutoBlocking() && this.blinkDelay == 0) {
                        AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                        ((IAccessorPlayerControllerMP)NoSlow.mc.playerController).callSyncCurrentPlayItem();
                        PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(NoSlow.mc.thePlayer.getHeldItem()));
                        NoSlow.mc.thePlayer.setItemInUse(NoSlow.mc.thePlayer.getHeldItem(), NoSlow.mc.thePlayer.getHeldItem().getMaxItemUseDuration());
                        AlpeetcClient.blinkManager.setBlinkState(true, BlinkModules.AUTO_BLOCK);
                    }
                    ++this.blinkDelay;
                }
            }
            if ((Integer)this.swordMode.getValue() == 5) {
                handle = NoSlow.mc.thePlayer.inventory.currentItem;
                if (this.swapped) {
                    this.swapped = false;
                    PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                    return;
                }
                PacketUtil.sendPacket(new C09PacketHeldItemChange(Disabler.getAltSlot(handle)));
                this.swapped = true;
            }
        } else {
            if ((Integer)this.swordMode.getValue() == 5) {
                int handle = NoSlow.mc.thePlayer.inventory.currentItem;
                if (this.swapped) {
                    this.swapped = false;
                    PacketUtil.sendPacket(new C09PacketHeldItemChange(handle));
                    return;
                }
            }
            if (this.blinkDelay >= 0 && (Integer)this.swordMode.getValue() == 2) {
                AlpeetcClient.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                this.blinkDelay = -1;
            }
        }
    }

    @EventTarget
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (this.isEnabled() && this.isAnyActive()) {
            float multiplier = (float)this.getMotionMultiplier() / 100.0f;
            NoSlow.mc.thePlayer.movementInput.moveForward *= multiplier;
            NoSlow.mc.thePlayer.movementInput.moveStrafe *= multiplier;
            if (!this.canSprint()) {
                NoSlow.mc.thePlayer.setSprinting(false);
            }
        }
    }

    @EventTarget(value=3)
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (this.isEnabled() && this.isFloatMode()) {
            int item = NoSlow.mc.thePlayer.inventory.currentItem;
            if (this.lastSlot != item && PlayerUtil.isUsingItem()) {
                this.lastSlot = item;
                AlpeetcClient.floatManager.setFloatState(true, FloatModules.NO_SLOW);
            }
        } else {
            this.lastSlot = -1;
            AlpeetcClient.floatManager.setFloatState(false, FloatModules.NO_SLOW);
        }
    }

    @EventTarget
    public void onRightClick(RightClickMouseEvent event) {
        if (this.isEnabled()) {
            if (NoSlow.mc.objectMouseOver != null) {
                switch (NoSlow.mc.objectMouseOver.typeOfHit) {
                    case BLOCK: {
                        BlockPos blockPos = NoSlow.mc.objectMouseOver.getBlockPos();
                        if (!BlockUtil.isInteractable(blockPos) || PlayerUtil.isSneaking()) break;
                        return;
                    }
                    case ENTITY: {
                        Entity entityHit = NoSlow.mc.objectMouseOver.entityHit;
                        if (entityHit instanceof EntityVillager) {
                            return;
                        }
                        if (!(entityHit instanceof EntityLivingBase) || !TeamUtil.isShop((EntityLivingBase)entityHit)) break;
                        return;
                    }
                }
            }
            if (this.isFloatMode() && !AlpeetcClient.floatManager.isPredicted() && NoSlow.mc.thePlayer.onGround) {
                event.setCancelled(true);
                NoSlow.mc.thePlayer.motionY = 0.42f;
            }
        }
    }

    @Override
    public String[] getSuffix() {
        return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.swordMode.getModeString())};
    }
}

