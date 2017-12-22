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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.snsmc.Main;
import org.snsmc.packetwrapper.WrapperPlayClientUseEntity;

import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import com.comphenix.protocol.wrappers.EnumWrappers.Hand;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class EntityManager {
	private final Set<HoloEntity> entities;
	private final ReadWriteLock idEntityMapLock;
	private final TIntObjectMap<HoloEntity> idEntitiyMap;
	private final int maxTrackingRange;
	private final int updateFrequency;
	private int updateCounter;
	private boolean isDirty;

	public EntityManager(int maxTrackingRange, int updateFrequency) {
		this.entities = new HashSet<>();
		this.idEntityMapLock = new ReentrantReadWriteLock();
		this.idEntitiyMap = new TIntObjectHashMap<>();
		this.maxTrackingRange = maxTrackingRange;
		this.updateFrequency = updateFrequency;
		this.updateCounter = 0;
		this.isDirty = false;
	}

	public void addEntity(HoloEntity entity) {
		if (!entities.add(entity))
			throw new IllegalStateException();
		idEntityMapLock.writeLock().lock();
		try {
			idEntitiyMap.put(entity.getId(), entity);
		} finally {
			idEntityMapLock.writeLock().unlock();
		}
		entity.setEntityManager(Optional.of(this));
		getPlayers().stream().filter(player -> isVisible(entity, player)).forEach(entity::track);
	}

	public void removeEntity(HoloEntity entity) {
		if (!entities.remove(entity))
			throw new IllegalStateException();
		idEntityMapLock.writeLock().lock();
		try {
			idEntitiyMap.remove(entity.getId());
		} finally {
			idEntityMapLock.writeLock().unlock();
		}
		entity.setEntityManager(Optional.empty());
		entity.getTrackedPlayers().stream().forEach(entity::untrack);
	}

	public Set<HoloEntity> getEntities() {
		return Collections.unmodifiableSet(new HashSet<>(entities));
	}

	public void removeAllEntities() {
		getEntities().stream().forEach(this::removeEntity);
	}

	public Optional<HoloEntity> getEntity(int entityID) {
		return Optional.ofNullable(idEntitiyMap.get(entityID));
	}

	public void onTick() {
		if (isDirty || updateCounter % updateFrequency == 0) {
			entities.stream().forEach(entity -> {
				getPlayers().stream().forEach(player -> {
					if (entity.isTracked(player)) {
						if (!isVisible(entity, player))
							entity.untrack(player);
					} else {
						if (isVisible(entity, player))
							entity.track(player);
					}
				});
			});
			updateCounter = 0;
			isDirty = false;
		} else
			updateCounter++;
	}

	public void onPlayerJoin(Player player) {
		entities.stream().filter(entity -> isVisible(entity, player)).forEach(entity -> entity.track(player));
	}

	public void onPlayerQuit(Player player) {
		entities.stream().filter(entity -> entity.isTracked(player)).forEach(entity -> entity.untrack(player));
	}

	public void onPlayerTeleport(Player player) {
		Main.getInstance().getServer().getScheduler().runTaskLater(Main.getInstance(), () -> entities.stream()
			.forEach(entity -> entity.getTrackedPlayers().stream().forEach(entity::sendSpawnPacket)), 20);
	}

	public boolean onPlayerUseEntity(Player player, WrapperPlayClientUseEntity packet) {
		int entityID = packet.getTargetID();
		EntityUseAction action = packet.getType();
		Vector hitVec = packet.getTargetVector();
		Hand hand = packet.getHand();
		idEntityMapLock.readLock().lock();
		try {
			if (!idEntitiyMap.containsKey(entityID))
				return false;
		} finally {
			idEntityMapLock.readLock().unlock();
		}
		Main.getInstance().getServer().getScheduler().runTask(Main.getInstance(),
			() -> getEntity(entityID).ifPresent(entity -> entity.onPlayerUseEntity(player, action, hitVec, hand)));
		return true;
	}

	protected void setDirty() {
		this.isDirty = true;
	}

	protected boolean isVisible(HoloEntity entity, Player player) {
		if (!entity.getLocation().getWorld().equals(player.getLocation().getWorld()))
			return false;
		if (!entity.getVisibilityPredicate().test(player))
			return false;
		double x = player.getLocation().getX() - entity.getLocation().getX();
		double z = player.getLocation().getZ() - entity.getLocation().getZ();
		double range = maxTrackingRange * 16;
		return x >= -range && x <= range && z >= -range && z <= range;
	}

	private Collection<? extends Player> getPlayers() {
		return Main.getInstance().getServer().getOnlinePlayers();
	}
}
