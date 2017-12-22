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

import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public interface Functional {
	static <T> UnaryOperator<T> compose(UnaryOperator<T> f, UnaryOperator<T> g) {
		return t -> f.apply(g.apply(t));
	}

	static <T> Stream<T> asStream(Optional<T> optional) {
		return optional.map(Stream::of).orElseGet(Stream::empty);
	}
}
