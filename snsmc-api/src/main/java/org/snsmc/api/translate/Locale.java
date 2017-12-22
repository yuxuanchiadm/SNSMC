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

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Locale {
	public static final Pattern LANGUAGE_PATTERN = Pattern.compile("(?<language>[a-zA-Z]{2,8})");
	public static final Pattern COUNTRY_PATTERN = Pattern.compile("(?<country>[a-zA-Z]{2}|[0-9]{3})");
	public static final Pattern LOCALE_PATTERN = Pattern
		.compile(LANGUAGE_PATTERN.pattern() + "_" + COUNTRY_PATTERN.pattern());

	private final String language;
	private final String country;

	public Locale(String language, String country) {
		language = language.toLowerCase();
		country = country.toUpperCase();
		if (!LANGUAGE_PATTERN.matcher(language).matches() || !COUNTRY_PATTERN.matcher(country).matches())
			throw new IllegalStateException();
		this.language = language;
		this.country = country;
	}

	public String getLanguage() {
		return language;
	}

	public String getCountry() {
		return country;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result + ((language == null) ? 0 : language.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Locale other = (Locale) obj;
		if (country == null) {
			if (other.country != null)
				return false;
		} else if (!country.equals(other.country))
			return false;
		if (language == null) {
			if (other.language != null)
				return false;
		} else if (!language.equals(other.language))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return language + "_" + country;
	}

	public static Optional<Locale> parse(String locale) {
		Matcher matcher = LOCALE_PATTERN.matcher(locale);
		if (!matcher.matches())
			return Optional.empty();
		String language = matcher.group("language");
		String country = matcher.group("country");
		return Optional.of(new Locale(language, country));
	}
}
