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
package org.snsmc.translate;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.snsmc.Main;
import org.snsmc.api.translate.Locale;

public class I18N {
	public static String translateKey(CommandSender sender, String key) {
		return Main.getInstance().getTranslateManager().translateKey(sender, key);
	}

	public static String translateKey(Locale locale, String key) {
		return Main.getInstance().getTranslateManager().translateKey(locale, key);
	}

	public static Locale getLocale(Player player) {
		return Main.getInstance().getTranslateManager().getLocale(player);
	}

	public static Locale getLocaleOrDefault(Player player) {
		return Main.getInstance().getTranslateManager().getLocaleOrDefault(player);
	}

	public static Set<Locale> getLocales() {
		return Main.getInstance().getTranslateManager().getLocales();
	}
}
