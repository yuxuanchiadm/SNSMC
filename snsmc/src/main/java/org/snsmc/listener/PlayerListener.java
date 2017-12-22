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
package org.snsmc.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.snsmc.Main;

public class PlayerListener implements Listener {
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Main.getInstance().getEntityManager().onPlayerJoin(event.getPlayer());
		Main.getInstance().getSNSManager().onPlayerJoin(event.getPlayer());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Main.getInstance().getEntityManager().onPlayerQuit(event.getPlayer());
		Main.getInstance().getSNSManager().onPlayerQuit(event.getPlayer());
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Main.getInstance().getEntityManager().onPlayerTeleport(event.getPlayer());
	}
}
