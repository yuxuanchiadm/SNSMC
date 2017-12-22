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

import org.snsmc.api.util.Initializer;

public interface SorterDef {
	static SorterDef def() {
		return SorterImpl.INITIALIZER.get().def();
	}

	@FunctionalInterface
	interface SorterImpl {
		Initializer<SorterImpl> INITIALIZER = new Initializer<>();

		SorterDef def();
	}

	SorterField field();

	Sorter end();

	abstract class SorterField {
		public final SorterOrder createTime;
		public final SorterOrder updateTime;
		public final SorterOrder topicState;
		public final SorterOrder latestReply;

		protected SorterField() {
			InverseConstruct inverseConstruct = inverseConstruct();
			this.createTime = inverseConstruct.createTime;
			this.updateTime = inverseConstruct.updateTime;
			this.topicState = inverseConstruct.topicState;
			this.latestReply = inverseConstruct.latestReply;
		}

		protected abstract InverseConstruct inverseConstruct();

		protected static class InverseConstruct {
			private final SorterOrder createTime;
			private final SorterOrder updateTime;
			private final SorterOrder topicState;
			private final SorterOrder latestReply;

			public InverseConstruct(SorterOrder createTime, SorterOrder updateTime, SorterOrder topicState,
				SorterOrder latestReply) {
				this.createTime = createTime;
				this.updateTime = updateTime;
				this.topicState = topicState;
				this.latestReply = latestReply;
			}
		}
	}

	interface SorterOrder {
		SorterDef asc();

		SorterDef desc();
	}
}
