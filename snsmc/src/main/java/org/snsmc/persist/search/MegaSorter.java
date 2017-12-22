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
package org.snsmc.persist.search;

import java.util.function.UnaryOperator;

import org.snsmc.api.persist.search.Sorter;
import org.snsmc.api.persist.search.SorterDef;
import org.snsmc.api.persist.search.SorterDef.*;
import org.snsmc.persist.bean.query.QTopicBean;
import org.snsmc.util.Functional;

import io.ebean.typequery.TQProperty;

public final class MegaSorter extends SorterField implements Sorter, SorterDef, SorterOrder {
	protected UnaryOperator<QTopicBean> action;

	protected Field currentField;
	protected Order currentOrder;

	private MegaSorter() {
		action = UnaryOperator.identity();
	}

	@Override
	protected InverseConstruct inverseConstruct() {
		return new InverseConstruct(fieldOf(Field.createTime), fieldOf(Field.updateTime), fieldOf(Field.topicState),
			fieldOf(Field.latestReply));
	}

	@Override
	public <IMPL extends Sorter> IMPL cast() {
		@SuppressWarnings("unchecked")
		IMPL impl = (IMPL) this;
		return impl;
	}

	@Override
	public SorterField field() {
		return this;
	}

	@Override
	public Sorter end() {
		return this;
	}

	@Override
	public SorterDef asc() {
		currentOrder = Order.ASC;
		appendFieldOrder();
		return this;
	}

	@Override
	public SorterDef desc() {
		currentOrder = Order.DESC;
		appendFieldOrder();
		return this;
	}

	private void appendFieldOrder() {
		final Order currentOrder = this.currentOrder;
		final Field currentField = this.currentField;
		switch (currentField.ordinal()) {
		case Field.createTimeOrdinal:
			action = Functional.compose(query -> currentOrder.appendOrder(query.createTime), action);
			break;
		case Field.updateTimeOrdinal:
			action = Functional.compose(query -> currentOrder.appendOrder(query.updateTime), action);
			break;
		case Field.topicStateOrdinal:
			action = Functional.compose(query -> currentOrder.appendOrder(query.topicState), action);
			break;
		case Field.latestReplyOrdinal:
			action = Functional.compose(query -> currentOrder.appendOrder(query.replies.updateTime), action);
			break;
		default:
			throw new IllegalStateException();
		}
		this.currentField = null;
		this.currentOrder = null;
	}

	public QTopicBean apply(QTopicBean query) {
		return action.apply(query.order());
	}

	static final class Field {
		public static final int createTimeOrdinal = 0;
		public static final Field createTime = new Field(createTimeOrdinal);
		public static final int updateTimeOrdinal = 1;
		public static final Field updateTime = new Field(updateTimeOrdinal);
		public static final int topicStateOrdinal = 2;
		public static final Field topicState = new Field(topicStateOrdinal);
		public static final int latestReplyOrdinal = 3;
		public static final Field latestReply = new Field(latestReplyOrdinal);

		private final int ordinal;

		private Field(int ordinal) {
			this.ordinal = ordinal;
		}

		public int ordinal() {
			return ordinal;
		}
	}

	static final class MegaSorterOrder implements SorterOrder {
		private final MegaSorter sorter;
		private final Field field;

		public MegaSorterOrder(MegaSorter sorter, Field field) {
			this.sorter = sorter;
			this.field = field;
		}

		@Override
		public SorterDef asc() {
			sorter.currentField = field;
			return sorter.asc();
		}

		@Override
		public SorterDef desc() {
			sorter.currentField = field;
			return sorter.desc();
		}
	}

	private SorterOrder fieldOf(Field field) {
		return new MegaSorterOrder(this, field);
	}

	enum Order {
		ASC, DESC;

		public <R> R appendOrder(TQProperty<R> property) {
			switch (this) {
			case ASC:
				return property.asc();
			case DESC:
				return property.desc();
			default:
				throw new IllegalStateException();
			}
		}
	}

	public static MegaSorter of() {
		MegaSorter megaSorter = new MegaSorter();
		return megaSorter;
	}
}