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

import java.util.ArrayList;
import java.util.List;

public class LineFormatter {
	private final int maxWidthCp;
	private final boolean forceTruncate;
	private final boolean fillEmpty;
	private final List<Entry> entries;
	private int totalMinWidthCp;

	public LineFormatter(int maxWidthCp) {
		this(maxWidthCp, false, false);
	}

	public LineFormatter(int maxWidthCp, boolean forceTruncate, boolean fillEmpty) {
		this.maxWidthCp = maxWidthCp;
		this.forceTruncate = forceTruncate;
		this.fillEmpty = fillEmpty;
		this.entries = new ArrayList<Entry>();
		this.totalMinWidthCp = 0;
	}

	public LineFormatter append(String string) {
		Entry entry = new Entry(string, cpLength(string), "");
		append(entry);
		return this;
	}

	public LineFormatter append(String string, int minWidthCp) {
		Entry entry = new Entry(string, Math.min(minWidthCp, cpLength(string)), "...");
		append(entry);
		return this;
	}

	public LineFormatter append(String string, int minWidthCp, String omissionSuffix) {
		Entry entry = new Entry(string, Math.min(minWidthCp, cpLength(string)), omissionSuffix);
		append(entry);
		return this;
	}

	public int getMaxWidth() {
		return maxWidthCp;
	}

	public boolean isForceTruncate() {
		return forceTruncate;
	}

	public int getTotalMinWidth() {
		return totalMinWidthCp;
	}

	public boolean isFillEmpty() {
		return fillEmpty;
	}

	private void append(Entry entry) {
		totalMinWidthCp += entry.getFormatMinWidth();
		entries.add(entry);
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		int available = Math.max(maxWidthCp - totalMinWidthCp, 0);
		for (Entry entry : entries) {
			int required = cpLength(entry.string) - entry.getFormatMinWidth();
			if (available >= required) {
				available -= required;
				stringBuilder.append(entry.string);
			} else {
				stringBuilder.append(cpSubstring(entry.string, 0, available + entry.minWidthCp));
				stringBuilder.append(entry.omissionSuffix);
				available = 0;
			}
		}
		String string = stringBuilder.toString();
		string = cpSubstring(string, 0, Math.min(forceTruncate ? maxWidthCp : Integer.MAX_VALUE, cpLength(string)));
		if (!fillEmpty || cpLength(string) >= maxWidthCp)
			return string;
		return string.concat(Strings.repeat(' ', maxWidthCp - cpLength(string)));
	}

	private static int cpLength(String string) {
		return string.codePointCount(0, string.length());
	}

	private static String cpSubstring(String string, int beginCp, int endCp) {
		return string.substring(string.offsetByCodePoints(0, beginCp), string.offsetByCodePoints(0, endCp));
	}

	static class Entry {
		final String string;
		final int minWidthCp;
		final String omissionSuffix;

		Entry(String string, int minWidthCp, String omissionSuffix) {
			this.string = string;
			this.minWidthCp = minWidthCp;
			this.omissionSuffix = omissionSuffix;
		}

		int getFormatMinWidth() {
			return Math.min(minWidthCp + cpLength(omissionSuffix), cpLength(string));
		}
	}
}
