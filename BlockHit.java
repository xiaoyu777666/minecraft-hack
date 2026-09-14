/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.CaseFormat
 *  net.minecraft.client.Minecraft
 *  net.minecraft.entity.EntityLivingBase
 */
package alpeetcclient.module.modules.legit;

import alpeetcclient.AlpeetcClient;
import com.google.common.base.CaseFormat;
import alpeetcclient.event.EventTarget;
import alpeetcclient.event.types.EventType;
import alpeetcclient.events.AttackEvent;
import alpeetcclient.events.TickEvent;
import alpeetcclient.module.Module;
import alpeetcclient.property.properties.BooleanProperty;
import alpeetcclient.property.properties.FloatProperty;
import alpeetcclient.property.properties.IntProperty;
import alpeetcclient.property.properties.ModeProperty;
import alpeetcclient.property.properties.PercentProperty;
import alpeetcclient.util.ItemUtil;
import alpeetcclient.util.KeyBindUtil;
import alpeetcclient.util.RotationUtil;
import alpeetcclient.util.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;

public class BlockHit
extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private final ModeProperty mode = new ModeProperty("Mode", 0, new String[]{"Helper", "Auto"});
    private final IntProperty stopTime = new IntProperty("Stop Ticks", 2, 1, 5, () -> (Integer)this.mode.getValue() == 0);
    private final ModeProperty autoMode = new ModeProperty("Auto Mode", 0, new String[]{"Spam", "Hold"}, () -> (Integer)this.mode.getValue() == 1 && (Integer)this.autoBlockTime.getValue() == 0);
    private final ModeProperty autoBlockTime = new ModeProperty("AutoBlock Time", 0, new String[]{"Delay", "HurtTime", "Sag", "Smart"}, () -> (Integer)this.mode.getValue() == 1);
    private final BooleanProperty onFirstHit = new BooleanProperty("OnFirstHit", true, () -> (Integer)this.mode.getValue() == 1 && (Integer)this.autoBlockTime.getValue() == 3);
    private final IntProperty smartBlockTick = new IntProperty("Smart Block Ticks", 2, 1, 5, () -> (Integer)this.mode.getValue() == 1 && (Integer)this.autoBlockTime.getValue() == 3);
    private final BooleanProperty releaseAfterHit = new BooleanProperty("Release After Hit", true, () -> (Integer)this.mode.getValue() == 1 && (Integer)this.autoBlockTime.getValue() == 3);
    private final IntProperty smartBlockHurtTime = new IntProperty("Smart Block HurtTime", 2, 0, 10, () -> (Integer)this.mode.getValue() == 1 && (Integer)this.autoBlockTime.getValue() == 3);
    private final IntProperty blockDelay = new IntProperty("Block Delay", 100, 0, 1000, () -> (Integer)this.mode.getValue() == 1 && (Integer)this.autoBlockTime.getValue() == 0);
    private final IntProperty holdTick = new IntProperty("Hold Ticks", 2, 2, 5, () -> (Integer)this.mode.getValue() == 1 && (Integer)this.autoMode.getValue() == 1 && (Integer)this.autoBlockTime.getValue() == 0);
    private final IntProperty minHurtTime = new IntProperty("Min HurtTime", 10, 1, 10, () -> (Integer)this.mode.getValue() == 1 && (Integer)this.autoBlockTime.getValue() == 1);
    private final IntProperty maxHurtTime = new IntProperty("Max HurtTime", 10, 1, 10, () -> (Integer)this.mode.getValue() == 1 && (Integer)this.autoBlockTime.getValue() == 1);
    private final PercentProperty chance = new PercentProperty("Block Hit Chance", 50, () -> (Integer)this.mode.getValue() == 1);
    private final BooleanProperty smart = new BooleanProperty("Smart", true, () -> (Integer)this.mode.getValue() == 1);
    private final BooleanProperty autoBlockRange = new BooleanProperty("AutoBlock Range", true, () -> (Integer)this.mode.getValue() == 1);
    private final FloatProperty range = new FloatProperty("Range", Float.valueOf(3.0f), Float.valueOf(1.0f), Float.valueOf(4.0f), () -> (Boolean)this.autoBlockRange.getValue() != false && (Integer)this.mode.getValue() == 1);
    private int holdTicks;
    private int stopTick;
    private boolean startBlocking;
    private boolean attacking;
    private int attackTicks;
    private int sagTicks = 0;
    private boolean canBlock = false;
    private int getBlockTicks = 0;
    private EntityLivingBase target;
    private TimerUtil timer = new TimerUtil();

    public BlockHit() {
        super("BlockHit", false, false);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (!this.isEnabled() || BlockHit.mc.thePlayer == null || BlockHit.mc.theWorld == null) {
            return;
        }
        if (event.getType() == EventType.PRE) {
            if ((Integer)this.mode.getValue() == 0) {
                if (BlockHit.mc.gameSettings.keyBindAttack.isKeyDown() && BlockHit.mc.thePlayer.isBlocking()) {
                    this.startBlocking = true;
                    KeyBindUtil.setKeyBindState(BlockHit.mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                }
                if (this.startBlocking) {
                    ++this.stopTick;
                }
                if (this.stopTick == 2) {
                    KeyBindUtil.pressKeyOnce(BlockHit.mc.gameSettings.keyBindAttack.getKeyCode());
                }
                if (this.stopTick > (Integer)this.stopTime.getValue()) {
                    KeyBindUtil.updateKeyState(BlockHit.mc.gameSettings.keyBindUseItem.getKeyCode());
                    this.startBlocking = false;
                    this.stopTick = 0;
                }
            }
            if ((Integer)this.mode.getValue() == 1) {
                if (this.target == null) {
                    return;
                }
                if (this.attacking) {
                    ++this.attackTicks;
                }
                if (this.attackTicks > 10) {
                    this.reset();
                    this.target = null;
                    return;
                }
                if (Math.random() > (double)((Integer)this.chance.getValue()).intValue()) {
                    this.reset();
                    return;
                }
                if (((Boolean)this.autoBlockRange.getValue()).booleanValue() && RotationUtil.distanceToBox(this.target.getCollisionBoundingBox()) >= (double)((Float)this.range.getValue()).floatValue()) {
                    this.reset();
                    return;
                }
                if (((Boolean)this.smart.getValue()).booleanValue() && this.target.hurtTime == 0) {
                    this.reset();
                    return;
                }
                if (this.attacking && ItemUtil.isHoldingSword()) {
                    if ((Integer)this.autoBlockTime.getValue() == 0 && this.timer.hasTimeElapsed(((Integer)this.blockDelay.getValue()).longValue())) {
                        if ((Integer)this.autoMode.getValue() == 0) {
                            KeyBindUtil.pressKeyOnce(BlockHit.mc.gameSettings.keyBindUseItem.getKeyCode());
                            this.timer.reset();
                            this.reset();
                        }
                        if ((Integer)this.autoMode.getValue() == 1) {
                            this.startBlocking = true;
                        }
                        if (this.startBlocking) {
                            KeyBindUtil.setKeyBindState(BlockHit.mc.gameSettings.keyBindUseItem.getKeyCode(), true);
                            ++this.holdTicks;
                        }
                        if (this.holdTicks > (Integer)this.holdTick.getValue()) {
                            KeyBindUtil.setKeyBindState(BlockHit.mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                            this.startBlocking = false;
                            this.holdTicks = 0;
                            this.timer.reset();
                        }
                    }
                    if ((Integer)this.autoBlockTime.getValue() == 1) {
                        if (BlockHit.mc.thePlayer.hurtTime >= (Integer)this.minHurtTime.getValue() && BlockHit.mc.thePlayer.hurtTime <= (Integer)this.maxHurtTime.getValue()) {
                            KeyBindUtil.setKeyBindState(BlockHit.mc.gameSettings.keyBindUseItem.getKeyCode(), true);
                            this.startBlocking = true;
                        } else if (this.startBlocking) {
                            KeyBindUtil.setKeyBindState(BlockHit.mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                            this.startBlocking = false;
                        }
                    }
                    if ((Integer)this.autoBlockTime.getValue() == 2) {
                        if (this.sagTicks < 10) {
                            KeyBindUtil.setKeyBindState(BlockHit.mc.gameSettings.keyBindUseItem.getKeyCode(), true);
                            ++this.sagTicks;
                        }
                        if (this.sagTicks >= 10) {
                            KeyBindUtil.updateKeyState(BlockHit.mc.gameSettings.keyBindUseItem.getKeyCode());
                            this.sagTicks = 0;
                        }
                    }
                    if ((Integer)this.autoBlockTime.getValue() == 3) {
                        if (BlockHit.mc.thePlayer.hurtTime == (Integer)this.smartBlockHurtTime.getValue()) {
                            this.canBlock = true;
                        }
                        if (this.canBlock) {
                            ++this.getBlockTicks;
                            KeyBindUtil.setKeyBindState(BlockHit.mc.gameSettings.keyBindUseItem.getKeyCode(), true);
                        }
                        if (BlockHit.mc.thePlayer.hurtTime == 9 && ((Boolean)this.releaseAfterHit.getValue()).booleanValue()) {
                            this.canBlock = false;
                            KeyBindUtil.updateKeyState(BlockHit.mc.gameSettings.keyBindUseItem.getKeyCode());
                            this.getBlockTicks = 0;
                        }
                        if (this.getBlockTicks > (Integer)this.smartBlockTick.getValue()) {
                            this.canBlock = false;
                            KeyBindUtil.updateKeyState(BlockHit.mc.gameSettings.keyBindUseItem.getKeyCode());
                            this.getBlockTicks = 0;
                        }
                    }
                }
            }
        }
    }

    private void reset() {
        this.canBlock = false;
        this.attacking = false;
        KeyBindUtil.updateKeyState(BlockHit.mc.gameSettings.keyBindUseItem.getKeyCode());
        this.getBlockTicks = 0;
        this.sagTicks = 0;
        this.holdTicks = 0;
        this.timer.reset();
    }

    @EventTarget
    public void onAttack(AttackEvent event) {
        if (this.isEnabled() && ItemUtil.isHoldingSword()) {
            this.attacking = true;
            this.attackTicks = 0;
            this.target = (EntityLivingBase)event.getTarget();
            if ((Integer)this.autoBlockTime.getValue() == 3 && BlockHit.mc.thePlayer.hurtTime == 0 && ((Boolean)this.onFirstHit.getValue()).booleanValue()) {
                this.canBlock = true;
            }
        }
    }

    @Override
    public String[] getSuffix() {
        return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.mode.getModeString())};
    }
}

