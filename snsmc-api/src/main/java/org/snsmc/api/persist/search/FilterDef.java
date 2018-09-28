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
package org.snsmc.api.persist.search;

import java.util.Date;

import org.bukkit.OfflinePlayer;
import org.snsmc.api.util.Initializer;

public interface FilterDef<P> {
	static <R extends FilterInitial<Filter> & FilterUnaryJuntion<Filter>> R def() {
		return FilterImpl.INITIALIZER.get().def();
	}

	@FunctionalInterface
	interface FilterImpl {
		Initializer<FilterImpl> INITIALIZER = new Initializer<>();

		<R extends FilterInitial<Filter> & FilterUnaryJuntion<Filter>> R def();
	}

	FilterCondition<P> cond();

	<R extends FilterTerminate<P> & FilterBinaryJuntion<P>> R constant(boolean b);

	abstract class FilterCondition<P> {
		public final FilterRelation<OfflinePlayer, P> creator;
		public final FilterOrderedRelation<Date, P> createTime;
		public final FilterOrderedRelation<Date, P> updateTime;
		public final FilterRelation<String, P> topicState;
		public final FilterFulltextRelation<String, P> title;
		public final FilterFulltextRelation<String, P> content;

		protected FilterCondition() {
			InverseConstruct<P> inverseConstruct = inverseConstruct();
			this.creator = inverseConstruct.creator;
			this.createTime = inverseConstruct.createTime;
			this.updateTime = inverseConstruct.updateTime;
			this.topicState = inverseConstruct.topicState;
			this.title = inverseConstruct.title;
			this.content = inverseConstruct.content;
		}

		protected abstract InverseConstruct<P> inverseConstruct();

		protected static class InverseConstruct<P> {
			private final FilterRelation<OfflinePlayer, P> creator;
			private final FilterOrderedRelation<Date, P> createTime;
			private final FilterOrderedRelation<Date, P> updateTime;
			private final FilterRelation<String, P> topicState;
			private final FilterFulltextRelation<String, P> title;
			private final FilterFulltextRelation<String, P> content;

			public InverseConstruct(FilterRelation<OfflinePlayer, P> creator, FilterOrderedRelation<Date, P> createTime,
				FilterOrderedRelation<Date, P> updateTime, FilterRelation<String, P> topicState,
				FilterFulltextRelation<String, P> title, FilterFulltextRelation<String, P> content) {
				this.creator = creator;
				this.createTime = createTime;
				this.updateTime = updateTime;
				this.topicState = topicState;
				this.title = title;
				this.content = content;
			}
		}
	}

	interface FilterInitial<P> extends FilterDef<P> {
		<G extends FilterTerminate<P> & FilterBinaryJuntion<P>, R extends FilterInitial<G> & FilterUnaryJuntion<G>> R begin();
	}

	interface FilterTerminate<P> {
		P end();
	}

	interface FilterRelation<T, P> {
		<R extends FilterTerminate<P> & FilterBinaryJuntion<P>> R eq(T t);
	}

	interface FilterOrderedRelation<T, P> extends FilterRelation<T, P> {
		<R extends FilterTerminate<P> & FilterBinaryJuntion<P>> R gt(T t);

		<R extends FilterTerminate<P> & FilterBinaryJuntion<P>> R ge(T t);

		@Override
		<R extends FilterTerminate<P> & FilterBinaryJuntion<P>> R eq(T t);

		<R extends FilterTerminate<P> & FilterBinaryJuntion<P>> R le(T t);

		<R extends FilterTerminate<P> & FilterBinaryJuntion<P>> R lt(T t);
	}

	interface FilterFulltextRelation<T, P> extends FilterRelation<T, P> {
		<R extends FilterTerminate<P> & FilterBinaryJuntion<P>> R contains(T t);
	}

	interface FilterUnaryJuntion<P> {
		<R extends FilterInitial<P> & FilterUnaryJuntion<P>> R not();
	}

	interface FilterBinaryJuntion<P> {
		<R extends FilterInitial<P> & FilterUnaryJuntion<P>> R and();

		<R extends FilterInitial<P> & FilterUnaryJuntion<P>> R or();
	}
}
