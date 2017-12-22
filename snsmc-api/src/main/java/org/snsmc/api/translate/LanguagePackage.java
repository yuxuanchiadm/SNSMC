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
package org.snsmc.api.translate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;

public class LanguagePackage {
	private final Locale locale;
	private Map<String, String> translateMap = new HashMap<String, String>();

	public LanguagePackage(Locale locale) {
		this.locale = locale;
	}

	public boolean loadLanguagePackage(File langFile) {
		if (langFile == null || !langFile.exists() || !langFile.isFile() || !langFile.canRead()) {
			return false;
		}
		Map<String, String> tempTranslateMap = new HashMap<String, String>();
		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(new FileInputStream(langFile), Charset.forName("UTF-8")))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty())
					continue;
				int separator = line.indexOf('=');
				if (separator == -1) {
					Bukkit.getLogger().log(Level.WARNING, "File " + langFile + " is not a valid language file");
					return false;
				}
				tempTranslateMap.put(line.substring(0, separator), line.substring(separator + 1));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		translateMap.clear();
		translateMap.putAll(tempTranslateMap);
		return true;
	}

	public Locale getLocale() {
		return locale;
	}

	public String translateKey(String key) {
		if (translateMap.containsKey(key)) {
			return translateMap.get(key);
		} else {
			return key;
		}
	}
}