/*
 * In game interactive SNS implementation.
 * Copyright (C) 2017  Yu Xuanchi <https://github.com/yuxuanchiadm>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional terms are added to this program under GPLv3 section 7. You
 * should have received a copy of those additional terms. Contact
 * author of this program if not.
 */
package org.snsmc.holo.entity;

import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.snsmc.packetwrapper.WrapperPlayServerEntityDestroy;
import org.snsmc.packetwrapper.WrapperPlayServerEntityMetadata;
import org.snsmc.packetwrapper.WrapperPlayServerEntityTeleport;
import org.snsmc.packetwrapper.WrapperPlayServerSpawnEntityLiving;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;

public class HoloSlime extends HoloEntity {
	private static final WrappedDataWatcherObject FLAGS //
		= new WrappedDataWatcherObject(0, Registry.get(Byte.class));
	private static final WrappedDataWatcherObject AIR //
		= new WrappedDataWatcherObject(1, Registry.get(Integer.class));
	private static final WrappedDataWatcherObject CUSTOM_NAME //
		= new WrappedDataWatcherObject(2, Registry.get(String.class));
	private static final WrappedDataWatcherObject CUSTOM_NAME_VISIBLE //
		= new WrappedDataWatcherObject(3, Registry.get(Boolean.class));
	private static final WrappedDataWatcherObject SILENT //
		= new WrappedDataWatcherObject(4, Registry.get(Boolean.class));
	private static final WrappedDataWatcherObject NO_GRAVITY //
		= new WrappedDataWatcherObject(5, Registry.get(Boolean.class));
	private static final WrappedDataWatcherObject HAND_STATES //
		= new WrappedDataWatcherObject(6, Registry.get(Byte.class));
	private static final WrappedDataWatcherObject HEALTH //
		= new WrappedDataWatcherObject(7, Registry.get(Float.class));
	private static final WrappedDataWatcherObject POTION_EFFECTS //
		= new WrappedDataWatcherObject(8, Registry.get(Integer.class));
	private static final WrappedDataWatcherObject HIDE_PARTICLES //
		= new WrappedDataWatcherObject(9, Registry.get(Boolean.class));
	private static final WrappedDataWatcherObject ARROW_COUNT_IN_ENTITY //
		= new WrappedDataWatcherObject(10, Registry.get(Integer.class));
	private static final WrappedDataWatcherObject AI_FLAGS //
		= new WrappedDataWatcherObject(11, Registry.get(Byte.class));
	private static final WrappedDataWatcherObject SLIME_SIZE //
		= new WrappedDataWatcherObject(12, Registry.get(Integer.class));

	public HoloSlime(int id, UUID uniqueID, Location location, Predicate<Player> visibilityPredicate) {
		super(id, uniqueID, location, visibilityPredicate);
	}

	@Override
	protected WrappedDataWatcher createDataWatcher(Player player) {
		WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
		dataWatcher.setObject(FLAGS, (byte) 0b100000);
		dataWatcher.setObject(AIR, 0);
		dataWatcher.setObject(CUSTOM_NAME, "");
		dataWatcher.setObject(CUSTOM_NAME_VISIBLE, false);
		dataWatcher.setObject(SILENT, true);
		dataWatcher.setObject(NO_GRAVITY, true);
		dataWatcher.setObject(HAND_STATES, (byte) 0b0);
		dataWatcher.setObject(HEALTH, 1.0F);
		dataWatcher.setObject(POTION_EFFECTS, 0);
		dataWatcher.setObject(HIDE_PARTICLES, false);
		dataWatcher.setObject(ARROW_COUNT_IN_ENTITY, 0);
		dataWatcher.setObject(AI_FLAGS, (byte) 0b0);
		dataWatcher.setObject(SLIME_SIZE, 1);
		return dataWatcher;
	}

	@Override
	protected void sendSpawnPacket(Player player) {
		WrapperPlayServerSpawnEntityLiving packet = new WrapperPlayServerSpawnEntityLiving();
		packet.setEntityID(getId());
		packet.setUniqueId(getUniqueID());
		packet.setX(getLocation().getX());
		packet.setY(getLocation().getY());
		packet.setZ(getLocation().getZ());
		packet.setYaw(getLocation().getYaw());
		packet.setPitch(getLocation().getPitch());
		packet.setType(EntityType.SLIME);
		packet.setMetadata(getDataWatcher(player));
		packet.sendPacket(player);
	}

	@Override
	protected void sendDestoryPacket(Player player) {
		WrapperPlayServerEntityDestroy packet = new WrapperPlayServerEntityDestroy();
		packet.setEntityIds(new int[] { getId() });
		packet.sendPacket(player);
	}

	@Override
	protected void sendTeleportPacket(Player player) {
		WrapperPlayServerEntityTeleport packet = new WrapperPlayServerEntityTeleport();
		packet.setEntityID(getId());
		packet.setX(getLocation().getX());
		packet.setY(getLocation().getY());
		packet.setZ(getLocation().getZ());
		packet.setYaw(getLocation().getYaw());
		packet.setPitch(getLocation().getPitch());
		packet.sendPacket(player);
	}

	@Override
	protected void sendMetadataPacket(Player player) {
		WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata();
		packet.setEntityID(getId());
		packet.setMetadata(getDataWatcher(player).getWatchableObjects().stream()
			.filter(object -> object.getDirtyState()).collect(Collectors.toList()));
		packet.sendPacket(player);
	}
}
