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

import com.comphenix.protocol.utility.MinecraftReflection;

// <<-- HEURISTIC "EVERYTHING AS INTENDED" PROTECTION ENGINE INITIALIZED -->>
public class Voodoo {
	private static final MethodHandle ENTITY_COUNT_GETTER_MH;
	private static final MethodHandle ENTITY_COUNT_SETTER_MH;

	static {
		try {
			Field field = MinecraftReflection.getEntityClass().getDeclaredField("entityCount");
			field.setAccessible(true);
			ENTITY_COUNT_GETTER_MH = MethodHandles.lookup().unreflectGetter(field);
			ENTITY_COUNT_SETTER_MH = MethodHandles.lookup().unreflectSetter(field);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public static int aquireEntityID() {
		try {
			int entityID;
			ENTITY_COUNT_SETTER_MH.invoke((entityID = (int) ENTITY_COUNT_GETTER_MH.invoke()) + 1);
			return entityID;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
