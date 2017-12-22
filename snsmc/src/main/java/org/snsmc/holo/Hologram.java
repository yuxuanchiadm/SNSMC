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
package org.snsmc.holo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.snsmc.Main;
import org.snsmc.holo.entity.ActionListener;
import org.snsmc.holo.entity.HoloArmorStand;
import org.snsmc.holo.entity.HoloEntity;
import org.snsmc.holo.entity.HoloSlime;
import org.snsmc.util.Voodoo;

import com.comphenix.protocol.wrappers.EnumWrappers.Hand;

public class Hologram {
	private static final double TEXT_OFFSET = -0.29D;
	private static final double SLIME_OFFSET = 0.0D;
	private static final double LINE_HEIGTH = 0.23D;
	private static final double SLIME_HEIGTH = 0.5D;
	private static final double SPACE_BETWEEN_LINES = 0.02D;

	private Location location;
	private String text;
	private final List<Line> lines;
	private final Predicate<Player> visibilityPredicate;
	private final Set<HologramListener> listeners;

	protected Hologram(Location location, Predicate<Player> visibilityPredicate, String text) {
		this.location = location.clone();
		this.visibilityPredicate = visibilityPredicate;
		this.text = text;
		this.lines = new ArrayList<>();
		this.listeners = new HashSet<>();
		updateLines();
	}

	public Location getLocation() {
		return location.clone();
	}

	public void setLocation(Location location) {
		this.location = location.clone();
		updateLines();
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		updateLines();
	}

	public void registerHologramListener(HologramListener hologramListener) {
		listeners.add(hologramListener);
	}

	protected void remove() {
		lines.stream().forEach(Line::remove);
	}

	private List<String> splitLine() {
		List<String> result = new ArrayList<>();
		Matcher matcher = Pattern.compile("^.*$", Pattern.MULTILINE).matcher(text);
		while (matcher.find())
			result.add(matcher.group());
		return result;
	}

	private void updateLines() {
		List<String> lineList = splitLine();
		for (int i = lineList.size(); i < lines.size(); i++)
			lines.get(i).remove();
		double yOffset = 0.0D;
		for (int i = 0; i < lineList.size(); i++) {
			yOffset -= LINE_HEIGTH;
			yOffset -= i == 0 ? 0.0D : SPACE_BETWEEN_LINES;

			if (i < lines.size()) {
				Line line = lines.get(i);
				line.setLocation(getLocation().add(0.0D, yOffset, 0.0D));
				line.setText(lineList.get(i));
			} else {
				Line line = new Line(getLocation().add(0.0D, yOffset, 0.0D), lineList.get(i));
				lines.add(line);
			}
		}
	}

	class Line implements ActionListener {
		private Location location;
		private String text;
		private final HoloArmorStand armorStand;
		private final HoloSlime slime;

		Line(Location location, String text) {
			this.location = location.clone();
			this.text = text;
			this.armorStand = new HoloArmorStand(Voodoo.aquireEntityID(), UUID.randomUUID(), getArmorStandLocation(),
				visibilityPredicate, text);
			this.slime = new HoloSlime(Voodoo.aquireEntityID(), UUID.randomUUID(), getSlimeLocation(),
				visibilityPredicate);
			Main.getInstance().getEntityManager().addEntity(armorStand);
			Main.getInstance().getEntityManager().addEntity(slime);
			slime.registerActionListener(this);
		}

		Location getLocation() {
			return location.clone();
		}

		void setLocation(Location location) {
			this.location = location.clone();
			this.armorStand.setLocation(getArmorStandLocation());
			this.slime.setLocation(getSlimeLocation());
		}

		String getText() {
			return text;
		}

		void setText(String text) {
			this.text = text;
			this.armorStand.setText(text);
		}

		void remove() {
			Main.getInstance().getEntityManager().removeEntity(armorStand);
			Main.getInstance().getEntityManager().removeEntity(slime);
		}

		Location getArmorStandLocation() {
			return getLocation().add(0.0D, TEXT_OFFSET, 0.0D);
		}

		Location getSlimeLocation() {
			return getLocation().add(0.0D, SLIME_OFFSET, 0.0D).add(0.0D, LINE_HEIGTH / 2.0D - SLIME_HEIGTH / 2.0D,
				0.0D);
		}

		public void onAttack(Player player, HoloEntity entity) {
			listeners.stream().forEach(listener -> listener.onAttack(player, Hologram.this));
		}

		public void onInteract(Player player, HoloEntity entity, Hand hand) {
			if (hand.equals(Hand.MAIN_HAND))
				listeners.stream().forEach(listener -> listener.onInteract(player, Hologram.this));
		}
	}
}
