/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.entity.ai.attributes.AttributeModifier
 *  net.minecraft.entity.ai.attributes.IAttributeInstance
 *  xyz.wagyourtail.jvmdg.j11.NestMembers
 */
package alpeetcclient.module.modules.movement;

import alpeetcclient.AlpeetcClient;
import alpeetcclient.event.EventTarget;
import alpeetcclient.events.TickEvent;
import alpeetcclient.mixin.IAccessorEntityLivingBase;
import alpeetcclient.module.Module;
import alpeetcclient.property.properties.BooleanProperty;
import alpeetcclient.util.KeyBindUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
public class Sprint
extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private boolean wasSprinting = false;
    public final BooleanProperty foxFix = new BooleanProperty("fov-fix", true);

    public Sprint() {
        super("Sprint", true, true);
    }

    public boolean shouldApplyFovFix(IAttributeInstance attribute) {
        if (!((Boolean)this.foxFix.getValue()).booleanValue()) {
            return false;
        }
        AttributeModifier attributeModifier = ((IAccessorEntityLivingBase)Sprint.mc.thePlayer).getSprintingSpeedBoostModifier();
        return attribute.getModifier(attributeModifier.getID()) == null && this.wasSprinting;
    }

    public boolean shouldKeepFov(boolean boolean2) {
        return (Boolean)this.foxFix.getValue() != false && !boolean2 && this.wasSprinting;
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled()) {
            switch (event.getType()) {
                case PRE: {
                    KeyBindUtil.setKeyBindState(Sprint.mc.gameSettings.keyBindSprint.getKeyCode(), true);
                    break;
                }
                case POST: {
                    this.wasSprinting = Sprint.mc.thePlayer.isSprinting();
                }
            }
        }
    }

    @Override
    public void onDisabled() {
        this.wasSprinting = false;
        KeyBindUtil.updateKeyState(Sprint.mc.gameSettings.keyBindSprint.getKeyCode());
    }
}

