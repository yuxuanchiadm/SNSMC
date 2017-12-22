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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import com.comphenix.protocol.wrappers.EnumWrappers.Hand;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

public abstract class HoloEntity {
	private final int id;
	private final UUID uniqueID;
	private Location location;
	private final Predicate<Player> visibilityPredicate;
	private final Map<Player, WrappedDataWatcher> trackedPlayers;
	private Optional<EntityManager> entityManager;
	private final Set<ActionListener> actionListeners;

	public HoloEntity(int id, UUID uniqueID, Location location, Predicate<Player> visibilityPredicate) {
		this.id = id;
		this.uniqueID = uniqueID;
		this.location = location.clone();
		this.visibilityPredicate = visibilityPredicate;
		this.trackedPlayers = new HashMap<>();
		this.entityManager = Optional.empty();
		this.actionListeners = new HashSet<>();
	}

	public int getId() {
		return id;
	}

	public UUID getUniqueID() {
		return uniqueID;
	}

	public Predicate<Player> getVisibilityPredicate() {
		return visibilityPredicate;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location.clone();
		getEntityManager().ifPresent(EntityManager::setDirty);
		getTrackedPlayers().stream().forEach(this::sendTeleportPacket);
	}

	protected void setEntityManager(Optional<EntityManager> entityManager) {
		this.entityManager = entityManager;
	}

	public Optional<EntityManager> getEntityManager() {
		return entityManager;
	}

	protected void onPlayerUseEntity(Player player, EntityUseAction action, Vector hitVec, Hand hand) {
		actionListeners.stream().forEach(actionListener -> {
			switch (action) {
			case ATTACK:
				actionListener.onAttack(player, this);
				break;
			case INTERACT:
				actionListener.onInteract(player, this, hand);
				break;
			case INTERACT_AT:
				actionListener.onInteractAt(player, this, hitVec, hand);
				break;
			}
		});
	}

	public void registerActionListener(ActionListener actionListener) {
		actionListeners.add(actionListener);
	}

	protected void track(Player player) {
		if (trackedPlayers.containsKey(player))
			throw new IllegalStateException();
		WrappedDataWatcher dataWatcher = createDataWatcher(player);
		trackedPlayers.put(player, dataWatcher);
		sendSpawnPacket(player);
	}

	protected void untrack(Player player) {
		if (!trackedPlayers.containsKey(player))
			throw new IllegalStateException();
		trackedPlayers.remove(player);
		sendDestoryPacket(player);
	}

	protected boolean isTracked(Player player) {
		return trackedPlayers.containsKey(player);
	}

	protected Set<Player> getTrackedPlayers() {
		return Collections.unmodifiableSet(new HashSet<>(trackedPlayers.keySet()));
	}

	protected abstract WrappedDataWatcher createDataWatcher(Player player);
	
	protected WrappedDataWatcher getDataWatcher(Player player) {
		if (!trackedPlayers.containsKey(player))
			throw new IllegalStateException();
		return trackedPlayers.get(player);
	}

	protected abstract void sendSpawnPacket(Player player);

	protected abstract void sendDestoryPacket(Player player);

	protected abstract void sendTeleportPacket(Player player);

	protected abstract void sendMetadataPacket(Player player);
}
