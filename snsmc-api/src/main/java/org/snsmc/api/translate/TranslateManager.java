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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;

public class TranslateManager {
	private final Locale defaultLocale;
	private final Map<Locale, LanguagePackage> langPakMap = new HashMap<Locale, LanguagePackage>();

	public TranslateManager() {
		this(new Locale("en", "US"));
	}

	public TranslateManager(ZipFile zipFile, File dataFolder) {
		this();
		saveDefaultLangPak(zipFile, dataFolder);
		refreshLangPak(dataFolder);
	}

	public TranslateManager(Locale defaultLocale) {
		this.defaultLocale = defaultLocale;
	}

	public String translateKey(CommandSender sender, String key) {
		if (sender instanceof Player) {
			return translateKey(getLocale((Player) sender), key);
		} else if (sender instanceof ConsoleCommandSender) {
			return translateKey(defaultLocale, key);
		} else if (sender instanceof BlockCommandSender) {
			return translateKey(defaultLocale, key);
		} else if (sender instanceof RemoteConsoleCommandSender) {
			return translateKey(defaultLocale, key);
		} else {
			return translateKey(defaultLocale, key);
		}
	}

	public String translateKey(Locale locale, String key) {
		if (langPakMap.containsKey(locale)) {
			return langPakMap.get(locale).translateKey(key);
		} else {
			return key;
		}
	}

	public Locale getLocale(Player player) {
		String playerLocale = player.spigot().getLocale();
		Locale locale = Locale.parse(playerLocale).orElse(defaultLocale);
		return locale;
	}

	public Locale getLocaleOrDefault(Player player) {
		String playerLocale = player.spigot().getLocale();
		Locale locale = Locale.parse(playerLocale).orElse(defaultLocale);
		if (!langPakMap.containsKey(locale))
			return defaultLocale;
		return locale;
	}

	public Set<Locale> getLocales() {
		return Collections.unmodifiableSet(new HashSet<>(langPakMap.keySet()));
	}

	public void saveDefaultLangPak(ZipFile zipFile, File dataFolder) {
		Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
		while (enumeration.hasMoreElements()) {
			ZipEntry zipEntry = enumeration.nextElement();
			String name = zipEntry.getName();
			if (name.matches("lang/" + Locale.LOCALE_PATTERN.pattern() + "\\.lang")) {
				File langFile = new File(dataFolder, name);
				if (!langFile.exists()) {
					if (!langFile.getParentFile().exists()) {
						langFile.getParentFile().mkdirs();
					}
					try {
						langFile.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
					try (FileOutputStream out = new FileOutputStream(langFile);
						InputStream in = zipFile.getInputStream(zipEntry);) {
						byte[] buffer = new byte[1024];
						int len;
						while ((len = in.read(buffer)) > 0) {
							out.write(buffer, 0, len);
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void refreshLangPak(File dataFolder) {
		langPakMap.clear();
		File langFolder = new File(dataFolder, "lang");
		for (File file : langFolder.listFiles()) {
			String fileName;
			if (file.isFile() && (fileName = file.getName()).endsWith(".lang")) {
				Locale.parse(fileName.substring(0, fileName.length() - 5)).ifPresent(locale -> {
					LanguagePackage langPak = new LanguagePackage(locale);
					langPak.loadLanguagePackage(new File(dataFolder, "lang/" + locale + ".lang"));
					langPakMap.put(locale, langPak);
				});
			}
		}
	}
}