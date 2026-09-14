/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.util.MovingObjectPosition$MovingObjectType
 *  net.minecraft.world.WorldSettings$GameType
 */
package alpeetcclient.module.modules.legit;

import alpeetcclient.AlpeetcClient;
import java.util.Objects;
import alpeetcclient.event.EventTarget;
import alpeetcclient.event.types.EventType;
import alpeetcclient.events.LeftClickMouseEvent;
import alpeetcclient.events.TickEvent;
import alpeetcclient.module.Module;
import alpeetcclient.property.properties.BooleanProperty;
import alpeetcclient.property.properties.FloatProperty;
import alpeetcclient.property.properties.IntProperty;
import alpeetcclient.util.ItemUtil;
import alpeetcclient.util.KeyBindUtil;
import alpeetcclient.util.RandomUtil;
import alpeetcclient.util.RotationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.WorldSettings;

public class AutoClicker
extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private boolean clickPending = false;
    private long clickDelay = 0L;
    private boolean blockHitPending = false;
    private long blockHitDelay = 0L;
    public final IntProperty minCPS = new IntProperty("min-cps", 8, 1, 20);
    public final IntProperty maxCPS = new IntProperty("max-cps", 12, 1, 20);
    public final BooleanProperty blockHit = new BooleanProperty("block-hit", false);
    public final FloatProperty blockHitTicks = new FloatProperty("block-hit-ticks", Float.valueOf(1.5f), Float.valueOf(1.0f), Float.valueOf(20.0f), this.blockHit::getValue);
    public final BooleanProperty weaponsOnly = new BooleanProperty("weapons-only", true);
    public final BooleanProperty allowTools = new BooleanProperty("allow-tools", false, this.weaponsOnly::getValue);
    public final BooleanProperty breakBlocks = new BooleanProperty("break-blocks", true);
    public final FloatProperty range = new FloatProperty("range", Float.valueOf(3.0f), Float.valueOf(3.0f), Float.valueOf(8.0f), this.breakBlocks::getValue);
    public final FloatProperty hitBoxVertical = new FloatProperty("hit-box-vertical", Float.valueOf(0.1f), Float.valueOf(0.0f), Float.valueOf(1.0f), this.breakBlocks::getValue);
    public final FloatProperty hitBoxHorizontal = new FloatProperty("hit-box-horizontal", Float.valueOf(0.2f), Float.valueOf(0.0f), Float.valueOf(1.0f), this.breakBlocks::getValue);

    private long getNextClickDelay() {
        return 1000L / RandomUtil.nextLong(((Integer)this.minCPS.getValue()).intValue(), ((Integer)this.maxCPS.getValue()).intValue());
    }

    private long getBlockHitDelay() {
        return (long)(50.0f * ((Float)this.blockHitTicks.getValue()).floatValue());
    }

    private boolean isBreakingBlock() {
        return AutoClicker.mc.objectMouseOver != null && AutoClicker.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK;
    }

    private boolean canClick() {
        if (!((Boolean)this.weaponsOnly.getValue()).booleanValue() || ItemUtil.hasRawUnbreakingEnchant() || ((Boolean)this.allowTools.getValue()).booleanValue() && ItemUtil.isHoldingTool()) {
            if (((Boolean)this.breakBlocks.getValue()).booleanValue() && this.isBreakingBlock() && !this.hasValidTarget()) {
                WorldSettings.GameType gameType12 = AutoClicker.mc.playerController.getCurrentGameType();
                return gameType12 != WorldSettings.GameType.SURVIVAL && gameType12 != WorldSettings.GameType.CREATIVE;
            }
            return true;
        }
        return false;
    }

    private boolean isValidTarget(EntityPlayer entityPlayer) {
        if (entityPlayer != AutoClicker.mc.thePlayer && entityPlayer != AutoClicker.mc.thePlayer.ridingEntity) {
            if (entityPlayer == mc.getRenderViewEntity() || entityPlayer == AutoClicker.mc.getRenderViewEntity().ridingEntity) {
                return false;
            }
            if (entityPlayer.deathTime > 0) {
                return false;
            }
            float borderSize = entityPlayer.getCollisionBorderSize();
            return RotationUtil.rayTrace(entityPlayer.getEntityBoundingBox().expand((double)(borderSize + ((Float)this.hitBoxHorizontal.getValue()).floatValue()), (double)(borderSize + ((Float)this.hitBoxVertical.getValue()).floatValue()), (double)(borderSize + ((Float)this.hitBoxHorizontal.getValue()).floatValue())), AutoClicker.mc.thePlayer.rotationYaw, AutoClicker.mc.thePlayer.rotationPitch, (double)((Float)this.range.getValue()).floatValue()) != null;
        }
        return false;
    }

    private boolean hasValidTarget() {
        return AutoClicker.mc.theWorld.loadedEntityList.stream().filter(e -> e instanceof EntityPlayer).map(e -> (EntityPlayer)e).anyMatch(this::isValidTarget);
    }

    public AutoClicker() {
        super("AutoClicker", false);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (event.getType() == EventType.PRE) {
            if (this.clickDelay > 0L) {
                this.clickDelay -= 50L;
            }
            if (this.blockHitDelay > 0L) {
                this.blockHitDelay -= 50L;
            }
            if (AutoClicker.mc.currentScreen != null) {
                this.clickPending = false;
                this.blockHitPending = false;
            } else {
                if (this.clickPending) {
                    this.clickPending = false;
                    KeyBindUtil.updateKeyState(AutoClicker.mc.gameSettings.keyBindAttack.getKeyCode());
                }
                if (this.blockHitPending) {
                    this.blockHitPending = false;
                    KeyBindUtil.updateKeyState(AutoClicker.mc.gameSettings.keyBindUseItem.getKeyCode());
                }
                if (this.isEnabled() && this.canClick() && AutoClicker.mc.gameSettings.keyBindAttack.isKeyDown()) {
                    if (!AutoClicker.mc.thePlayer.isUsingItem()) {
                        while (this.clickDelay <= 0L) {
                            this.clickPending = true;
                            this.clickDelay += this.getNextClickDelay();
                            KeyBindUtil.setKeyBindState(AutoClicker.mc.gameSettings.keyBindAttack.getKeyCode(), false);
                            KeyBindUtil.pressKeyOnce(AutoClicker.mc.gameSettings.keyBindAttack.getKeyCode());
                        }
                    }
                    if (((Boolean)this.blockHit.getValue()).booleanValue() && this.blockHitDelay <= 0L && AutoClicker.mc.gameSettings.keyBindUseItem.isKeyDown() && ItemUtil.isHoldingSword()) {
                        this.blockHitPending = true;
                        KeyBindUtil.setKeyBindState(AutoClicker.mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                        if (!AutoClicker.mc.thePlayer.isUsingItem()) {
                            this.blockHitDelay += this.getBlockHitDelay();
                            KeyBindUtil.pressKeyOnce(AutoClicker.mc.gameSettings.keyBindUseItem.getKeyCode());
                        }
                    }
                }
            }
        }
    }

    @EventTarget(value=4)
    public void onCLick(LeftClickMouseEvent event) {
        if (this.isEnabled() && !event.isCancelled() && !this.clickPending) {
            this.clickDelay += this.getNextClickDelay();
        }
    }

    @Override
    public void onEnabled() {
        this.clickDelay = 0L;
        this.blockHitDelay = 0L;
    }

    @Override
    public void verifyValue(String mode) {
        if (this.minCPS.getName().equals(mode)) {
            if ((Integer)this.minCPS.getValue() > (Integer)this.maxCPS.getValue()) {
                this.maxCPS.setValue(this.minCPS.getValue());
            }
        } else if (this.maxCPS.getName().equals(mode) && (Integer)this.minCPS.getValue() > (Integer)this.maxCPS.getValue()) {
            this.minCPS.setValue(this.maxCPS.getValue());
        }
    }

    @Override
    public String[] getSuffix() {
        String[] stringArray;
        if (Objects.equals(this.minCPS.getValue(), this.maxCPS.getValue())) {
            String[] stringArray2 = new String[1];
            stringArray = stringArray2;
            stringArray2[0] = ((Integer)this.minCPS.getValue()).toString();
        } else {
            String[] stringArray3 = new String[1];
            stringArray = stringArray3;
            stringArray3[0] = String.format("%d-%d", this.minCPS.getValue(), this.maxCPS.getValue());
        }
        return stringArray;
    }
}

