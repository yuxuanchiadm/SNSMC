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
package org.snsmc.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.utility.MinecraftReflection;

// <<-- HEURISTIC "EVERYTHING AS INTENDED" PROTECTION ENGINE INITIALIZED -->>
public class Voodoo {
	private static final MethodHandle ENTITY_COUNT_GETTER_MH;
	private static final MethodHandle ENTITY_COUNT_SETTER_MH;
	private static final MethodHandle GET_PLAYER_CHUNK_MAP_MH;
	private static final MethodHandle IS_PLAYER_WATCHING_CHUNK_MH;

	static {
		try {
			Field entityCountField = MinecraftReflection.getEntityClass().getDeclaredField("entityCount");
			entityCountField.setAccessible(true);
			ENTITY_COUNT_GETTER_MH = MethodHandles.lookup().unreflectGetter(entityCountField);
			ENTITY_COUNT_SETTER_MH = MethodHandles.lookup().unreflectSetter(entityCountField);

			Method getPlayerChunkMapMethod = MinecraftReflection.getWorldServerClass()
				.getDeclaredMethod("getPlayerChunkMap");
			getPlayerChunkMapMethod.setAccessible(true);
			GET_PLAYER_CHUNK_MAP_MH = MethodHandles.lookup().unreflect(getPlayerChunkMapMethod);

			Method isPlayerWatchingChunkMethod = MinecraftReflection.getMinecraftClass("PlayerChunkMap")
				.getDeclaredMethod("a", MinecraftReflection.getEntityPlayerClass(), int.class, int.class);
			isPlayerWatchingChunkMethod.setAccessible(true);
			IS_PLAYER_WATCHING_CHUNK_MH = MethodHandles.lookup().unreflect(isPlayerWatchingChunkMethod);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public static int aquireEntityID() {
		try {
			int entityID;
			ENTITY_COUNT_SETTER_MH.invokeExact((entityID = (int) ENTITY_COUNT_GETTER_MH.invokeExact()) + 1);
			return entityID;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean isPlayerWatchingChunk(Player player, Chunk chunk) {
		try {
			World world = player.getWorld();
			if (!chunk.getWorld().equals(world))
				return false;
			Object playerChunkMap = GET_PLAYER_CHUNK_MAP_MH
				.invokeWithArguments(BukkitUnwrapper.getInstance().unwrapItem(world));
			boolean isPlayerWatchingChunk = (boolean) IS_PLAYER_WATCHING_CHUNK_MH.invokeWithArguments(playerChunkMap,
				BukkitUnwrapper.getInstance().unwrapItem(player), chunk.getX(), chunk.getZ());
			return isPlayerWatchingChunk;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
