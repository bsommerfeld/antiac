package de.bsommerfeld.antiac.listener;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.google.inject.Inject;
import de.bsommerfeld.antiac.capture.ClickCollector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * PacketEvents-based listener capturing only true attack packets (entity hits)
 * to record click timestamps. This avoids counting swings from block-placing/holding.
 */
public final class ClickListener extends PacketListenerAbstract {

    private final ClickCollector collector;

    @Inject
    public ClickListener(ClickCollector collector) {
        this.collector = collector;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
            if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                UUID id = event.getUser().getUUID();
                long now = System.currentTimeMillis();
                Player p = Bukkit.getPlayer(id);
                if (p != null) {
                    float yaw = p.getLocation().getYaw();
                    float pitch = p.getLocation().getPitch();
                    collector.recordClick(id, now, yaw, pitch);
                } else {
                    collector.recordClick(id, now);
                }
            }
        }
    }
}
