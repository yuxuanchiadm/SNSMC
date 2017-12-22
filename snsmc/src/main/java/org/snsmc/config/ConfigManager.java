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
package org.snsmc.config;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;

public class ConfigManager {
	private final ConfigurationSection config;

	public ConfigManager(ConfigurationSection config) {
		this.config = config;
	}

	public String getDatabaseDriver() {
		return config.getString("snsmc.database.driver", "com.mysql.jdbc.Driver");
	}

	public String getDatabaseURL() {
		return config.getString("snsmc.database.url", "jdbc:mysql://localhost:3306/minecraft");
	}

	public String getDatabaseUser() {
		return config.getString("snsmc.database.user", "root");
	}

	public String getDatabasePassword() {
		return config.getString("snsmc.database.password", "root");
	}

	public String getDatabaseTablePrefix() {
		return config.getString("snsmc.database.tablePrefix", "snsmc");
	}

	public boolean getDatabaseGenerateDDL() {
		return config.getBoolean("snsmc.database.generateDDL", false);
	}

	public int getHoloMaxTrackingRange() {
		return config.getInt("snsmc.holo.maxTrackingRange", 10);
	}

	public int getHoloUpdateFrequency() {
		return config.getInt("snsmc.holo.updateFrequency", 20);
	}

	public int getLabelMaxWidth() {
		return config.getInt("snsmc.label.maxWidth", 64);
	}

	public int getLabelMaxLine() {
		return config.getInt("snsmc.label.maxLine", 8);
	}

	public List<TopicState> getTopicStates() {
		List<Map<?, ?>> states = config.getMapList("snsmc.topic.states");
		return states.stream().map(map -> new TopicState(Objects.requireNonNull((String) map.get("name")),
			Objects.requireNonNull((String) map.get("translateKey")))).collect(Collectors.toList());
	}
}
