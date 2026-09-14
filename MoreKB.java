/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.client.C0BPacketEntityAction
 *  net.minecraft.network.play.client.C0BPacketEntityAction$Action
 *  net.minecraft.util.MathHelper
 *  net.minecraft.util.MovingObjectPosition$MovingObjectType
 */
package alpeetcclient.module.modules.combat;

import alpeetcclient.AlpeetcClient;
import alpeetcclient.event.EventTarget;
import alpeetcclient.events.AttackEvent;
import alpeetcclient.events.TickEvent;
import alpeetcclient.module.Module;
import alpeetcclient.property.properties.BooleanProperty;
import alpeetcclient.property.properties.ModeProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;

public class MoreKB
extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"LEGIT", "LEGIT_FAST", "LESS_PACKET", "PACKET", "DOUBLE_PACKET"});
    public final BooleanProperty intelligent = new BooleanProperty("intelligent", false);
    public final BooleanProperty onlyGround = new BooleanProperty("only-ground", true);
    private boolean shouldSprintReset = false;
    private EntityLivingBase target = null;

    public MoreKB() {
        super("MoreKB", false);
    }

    @EventTarget
    public void onAttack(AttackEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        Entity targetEntity = event.getTarget();
        if (targetEntity != null && targetEntity instanceof EntityLivingBase) {
            this.target = (EntityLivingBase)targetEntity;
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if ((Integer)this.mode.getValue() == 1) {
            if (this.target != null && this.isMoving()) {
                if (((Boolean)this.onlyGround.getValue()).booleanValue() && MoreKB.mc.thePlayer.onGround || !((Boolean)this.onlyGround.getValue()).booleanValue()) {
                    MoreKB.mc.thePlayer.sprintingTicksLeft = 0;
                }
                this.target = null;
            }
            return;
        }
        EntityLivingBase entity = null;
        if (MoreKB.mc.objectMouseOver != null && MoreKB.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && MoreKB.mc.objectMouseOver.entityHit instanceof EntityLivingBase) {
            entity = (EntityLivingBase)MoreKB.mc.objectMouseOver.entityHit;
        }
        if (entity == null) {
            return;
        }
        double x = MoreKB.mc.thePlayer.posX - entity.posX;
        double z = MoreKB.mc.thePlayer.posZ - entity.posZ;
        float calcYaw = (float)(Math.atan2(z, x) * 180.0 / Math.PI - 90.0);
        float diffY = Math.abs(MathHelper.wrapAngleTo180_float((float)(calcYaw - entity.rotationYawHead)));
        if (((Boolean)this.intelligent.getValue()).booleanValue() && diffY > 120.0f) {
            return;
        }
        if (entity.hurtTime == 10) {
            switch ((Integer)this.mode.getValue()) {
                case 0: {
                    this.shouldSprintReset = true;
                    if (MoreKB.mc.thePlayer.isSprinting()) {
                        MoreKB.mc.thePlayer.setSprinting(false);
                        MoreKB.mc.thePlayer.setSprinting(true);
                    }
                    this.shouldSprintReset = false;
                    break;
                }
                case 2: {
                    if (MoreKB.mc.thePlayer.isSprinting()) {
                        MoreKB.mc.thePlayer.setSprinting(false);
                    }
                    mc.getNetHandler().addToSendQueue((Packet)new C0BPacketEntityAction((Entity)MoreKB.mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                    MoreKB.mc.thePlayer.setSprinting(true);
                    break;
                }
                case 3: {
                    MoreKB.mc.thePlayer.sendQueue.addToSendQueue((Packet)new C0BPacketEntityAction((Entity)MoreKB.mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                    MoreKB.mc.thePlayer.sendQueue.addToSendQueue((Packet)new C0BPacketEntityAction((Entity)MoreKB.mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                    MoreKB.mc.thePlayer.setSprinting(true);
                    break;
                }
                case 4: {
                    MoreKB.mc.thePlayer.sendQueue.addToSendQueue((Packet)new C0BPacketEntityAction((Entity)MoreKB.mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                    MoreKB.mc.thePlayer.sendQueue.addToSendQueue((Packet)new C0BPacketEntityAction((Entity)MoreKB.mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                    MoreKB.mc.thePlayer.sendQueue.addToSendQueue((Packet)new C0BPacketEntityAction((Entity)MoreKB.mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                    MoreKB.mc.thePlayer.sendQueue.addToSendQueue((Packet)new C0BPacketEntityAction((Entity)MoreKB.mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                    MoreKB.mc.thePlayer.setSprinting(true);
                }
            }
        }
    }

    private boolean isMoving() {
        return MoreKB.mc.thePlayer.moveForward != 0.0f || MoreKB.mc.thePlayer.moveStrafing != 0.0f;
    }

    @Override
    public String[] getSuffix() {
        return new String[]{((Integer)this.mode.getValue()).toString()};
    }
}

