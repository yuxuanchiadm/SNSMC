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

import org.snsmc.Main;
import org.snsmc.api.translate.Locale;
import org.snsmc.translate.I18N;

public class TopicState {
	private final String name;
	private final String translateKey;

	public TopicState(String name, String translateKey) {
		this.name = name;
		this.translateKey = translateKey;
	}

	public String getName() {
		return name;
	}

	public String getTranslateKey() {
		return translateKey;
	}

	public static String translateTopicState(Locale locale, String topicState) {
		return Main.getInstance().getConfigManager().getTopicStates().stream()
			.filter(state -> state.getName().equals(topicState)).findFirst().map(TopicState::getTranslateKey)
			.map(translateKey -> I18N.translateKey(locale, translateKey)).orElse(topicState);
	}
}
