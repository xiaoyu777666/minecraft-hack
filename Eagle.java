/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  org.apache.commons.lang3.RandomUtils
 *  org.lwjgl.input.Keyboard
 */
package alpeetcclient.module.modules.legit;

import alpeetcclient.AlpeetcClient;
import java.util.Objects;
import alpeetcclient.event.EventTarget;
import alpeetcclient.event.types.EventType;
import alpeetcclient.events.MoveInputEvent;
import alpeetcclient.events.TickEvent;
import alpeetcclient.module.Module;
import alpeetcclient.property.properties.BooleanProperty;
import alpeetcclient.property.properties.IntProperty;
import alpeetcclient.util.ItemUtil;
import alpeetcclient.util.MoveUtil;
import alpeetcclient.util.PlayerUtil;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.RandomUtils;
import org.lwjgl.input.Keyboard;

public class Eagle
extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private int sneakDelay = 0;
    public final IntProperty minDelay = new IntProperty("min-delay", 2, 0, 10);
    public final IntProperty maxDelay = new IntProperty("max-delay", 3, 0, 10);
    public final BooleanProperty directionCheck = new BooleanProperty("direction-check", true);
    public final BooleanProperty jumpCheck = new BooleanProperty("jump-check", true);
    public final BooleanProperty pitchCheck = new BooleanProperty("pitch-check", true);
    public final BooleanProperty blocksOnly = new BooleanProperty("blocks-only", true);
    public final BooleanProperty sneakOnly = new BooleanProperty("sneaking-only", false);

    private boolean canMoveSafely() {
        double[] offset = MoveUtil.predictMovement();
        return PlayerUtil.canMove(Eagle.mc.thePlayer.motionX + offset[0], Eagle.mc.thePlayer.motionZ + offset[1]);
    }

    private boolean shouldSneak() {
        if (((Boolean)this.directionCheck.getValue()).booleanValue() && Eagle.mc.gameSettings.keyBindForward.isKeyDown()) {
            return false;
        }
        if (((Boolean)this.jumpCheck.getValue()).booleanValue() && Eagle.mc.gameSettings.keyBindJump.isKeyDown()) {
            return false;
        }
        if (((Boolean)this.pitchCheck.getValue()).booleanValue() && Eagle.mc.thePlayer.rotationPitch < 69.0f) {
            return false;
        }
        if (((Boolean)this.sneakOnly.getValue()).booleanValue() && !Keyboard.isKeyDown((int)Eagle.mc.gameSettings.keyBindSneak.getKeyCode())) {
            return false;
        }
        return ((Boolean)this.blocksOnly.getValue() == false || ItemUtil.isHoldingBlock()) && Eagle.mc.thePlayer.onGround;
    }

    public Eagle() {
        super("Eagle", false);
    }

    @EventTarget(value=4)
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            if (this.sneakDelay > 0) {
                --this.sneakDelay;
            }
            if (this.sneakDelay == 0 && this.canMoveSafely()) {
                this.sneakDelay = RandomUtils.nextInt((int)((Integer)this.minDelay.getValue()), (int)((Integer)this.maxDelay.getValue() + 1));
            }
        }
    }

    @EventTarget(value=4)
    public void onMoveInput(MoveInputEvent event) {
        if (this.isEnabled() && Eagle.mc.currentScreen == null) {
            if (((Boolean)this.sneakOnly.getValue()).booleanValue() && Keyboard.isKeyDown((int)Eagle.mc.gameSettings.keyBindSneak.getKeyCode()) && this.shouldSneak()) {
                Eagle.mc.thePlayer.movementInput.sneak = false;
                Eagle.mc.thePlayer.movementInput.moveForward /= 0.3f;
                Eagle.mc.thePlayer.movementInput.moveStrafe /= 0.3f;
            }
            if (!Eagle.mc.thePlayer.movementInput.sneak && this.shouldSneak() && (this.sneakDelay > 0 || this.canMoveSafely())) {
                Eagle.mc.thePlayer.movementInput.sneak = true;
                Eagle.mc.thePlayer.movementInput.moveStrafe *= 0.3f;
                Eagle.mc.thePlayer.movementInput.moveForward *= 0.3f;
            }
        }
    }

    @Override
    public void onDisabled() {
        this.sneakDelay = 0;
    }

    @Override
    public void verifyValue(String name) {
        switch (name) {
            case "min-delay": {
                if ((Integer)this.minDelay.getValue() <= (Integer)this.maxDelay.getValue()) break;
                this.maxDelay.setValue(this.minDelay.getValue());
                break;
            }
            case "max-delay": {
                if ((Integer)this.minDelay.getValue() <= (Integer)this.maxDelay.getValue()) break;
                this.minDelay.setValue(this.maxDelay.getValue());
            }
        }
    }

    @Override
    public String[] getSuffix() {
        String[] stringArray;
        if (Objects.equals(this.minDelay.getValue(), this.maxDelay.getValue())) {
            String[] stringArray2 = new String[1];
            stringArray = stringArray2;
            stringArray2[0] = ((Integer)this.minDelay.getValue()).toString();
        } else {
            String[] stringArray3 = new String[1];
            stringArray = stringArray3;
            stringArray3[0] = String.format("%d-%d", this.minDelay.getValue(), this.maxDelay.getValue());
        }
        return stringArray;
    }
}

