package com.kabryxis.thevoid.round.utility;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.kabryxis.kabutils.spigot.plugin.SpoofPlugin;

public class DismountPacketAdapter extends PacketAdapter {

	public DismountPacketAdapter() {
		super(SpoofPlugin.get(), PacketType.Play.Client.STEER_VEHICLE);
	}
	
	@Override
	public void onPacketReceiving(PacketEvent event) {
		event.getPacket().getBooleans().write(1, false);
	}
	
}
