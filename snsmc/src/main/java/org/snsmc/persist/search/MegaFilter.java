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

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.bukkit.OfflinePlayer;
import org.snsmc.api.persist.search.Filter;
import org.snsmc.api.persist.search.FilterDef;
import org.snsmc.api.persist.search.FilterDef.*;
import org.snsmc.persist.bean.query.QTopicBean;
import org.snsmc.util.Functional;

import io.ebean.typequery.PBaseDate;
import io.ebean.typequery.PBaseNumber;
import io.ebean.typequery.PBaseValueEqual;
import io.ebean.typequery.PBoolean;
import io.ebean.typequery.PEnum;
import io.ebean.typequery.PString;

public final class MegaFilter<T, P> extends FilterCondition<P>
	implements Filter, FilterDef<P>, FilterInitial<P>, FilterTerminate<P>, FilterRelation<T, P>,
	FilterOrderedRelation<T, P>, FilterFulltextRelation<T, P>, FilterUnaryJuntion<P>, FilterBinaryJuntion<P> {
	protected UnaryOperator<QTopicBean> action;

	protected Relation<T, P, ? extends FilterRelation<T, P>> currentRelation;
	protected RelationType currentRelationType;
	protected T currentRelationValue;
	protected OperatorType currentOperatorType;
	protected Queue<UnaryOperator<QTopicBean>> outputQueue;
	protected Queue<OperatorType> operatorQueue;

	private MegaFilter() {
		action = UnaryOperator.identity();

		outputQueue = Collections.asLifoQueue(new ArrayDeque<>());
		operatorQueue = Collections.asLifoQueue(new ArrayDeque<>());
	}

	@Override
	protected InverseConstruct<P> inverseConstruct() {
		return new InverseConstruct<>(
			this.<OfflinePlayer, P, MegaFilter<OfflinePlayer, P>> castDiscriminatory()
				.relationOf(Relation.creatorPoly()),
			this.<Date, P, MegaFilter<Date, P>> castDiscriminatory().relationOf(Relation.createTimePoly()),
			this.<Date, P, MegaFilter<Date, P>> castDiscriminatory().relationOf(Relation.updateTimePoly()),
			this.<String, P, MegaFilter<String, P>> castDiscriminatory().relationOf(Relation.topicStatePoly()),
			this.<String, P, MegaFilter<String, P>> castDiscriminatory().relationOf(Relation.titlePoly()),
			this.<String, P, MegaFilter<String, P>> castDiscriminatory().relationOf(Relation.contentPoly()));
	}

	@Override
	public <IMPL extends Filter> IMPL cast() {
		@SuppressWarnings("unchecked")
		IMPL impl =  (IMPL) this;
		return impl;
	}

	@Override
	public FilterCondition<P> cond() {
		return castDiscriminatory();
	}

	@Override
	public <G extends FilterTerminate<P> & FilterBinaryJuntion<P>, R extends FilterInitial<G> & FilterTerminate<G>> R begin() {
		currentOperatorType = OperatorType.BEGIN;
		appendOperator();
		return castRespect();
	}

	@Override
	public P end() {
		currentOperatorType = OperatorType.END;
		appendOperator();
		return castRespect();
	}

	@Override
	public <R extends FilterTerminate<P> & FilterBinaryJuntion<P>> R gt(T value) {
		currentRelationType = RelationType.GT;
		currentRelationValue = value;
		appendRelation();
		return castRespect();
	}

	@Override
	public <R extends FilterTerminate<P> & FilterBinaryJuntion<P>> R ge(T value) {
		currentRelationType = RelationType.GE;
		currentRelationValue = value;
		appendRelation();
		return castRespect();
	}

	@Override
	public <R extends FilterTerminate<P> & FilterBinaryJuntion<P>> R eq(T value) {
		currentRelationType = RelationType.EQ;
		currentRelationValue = value;
		appendRelation();
		return castRespect();
	}

	@Override
	public <R extends FilterTerminate<P> & FilterBinaryJuntion<P>> R le(T value) {
		currentRelationType = RelationType.LE;
		currentRelationValue = value;
		appendRelation();
		return castRespect();
	}

	@Override
	public <R extends FilterTerminate<P> & FilterBinaryJuntion<P>> R lt(T value) {
		currentRelationType = RelationType.LT;
		currentRelationValue = value;
		appendRelation();
		return castRespect();
	}

	@Override
	public <R extends FilterTerminate<P> & FilterBinaryJuntion<P>> R contains(T value) {
		currentRelationType = RelationType.CONTAINS;
		currentRelationValue = value;
		appendRelation();
		return castRespect();
	}

	@Override
	public <R extends FilterInitial<P>> R not() {
		currentOperatorType = OperatorType.NOT;
		appendOperator();
		return castRespect();
	}

	@Override
	public <R extends FilterInitial<P> & FilterUnaryJuntion<P>> R and() {
		currentOperatorType = OperatorType.AND;
		appendOperator();
		return castRespect();
	}

	@Override
	public <R extends FilterInitial<P> & FilterUnaryJuntion<P>> R or() {
		currentOperatorType = OperatorType.OR;
		appendOperator();
		return castRespect();
	}

	private void appendRelation() {
		final Relation<T, P, ? extends FilterRelation<T, P>> currentRelation = this.currentRelation;
		final RelationType currentRelationType = this.currentRelationType;
		final T currentRelationValue = this.currentRelationValue;
		switch (currentRelation.ordinal()) {
		case Relation.creatorOrdinal:
			outputQueue.add(query -> currentRelation.appendRelation(query, currentRelationType, currentRelationValue));
			break;
		case Relation.createTimeOrdinal:
			outputQueue.add(query -> currentRelation.appendRelation(query, currentRelationType, currentRelationValue));
			break;
		case Relation.updateTimeOrdinal:
			outputQueue.add(query -> currentRelation.appendRelation(query, currentRelationType, currentRelationValue));
			break;
		case Relation.topicStateOrdinal:
			outputQueue.add(query -> currentRelation.appendRelation(query, currentRelationType, currentRelationValue));
			break;
		case Relation.titleOrdinal:
			outputQueue.add(query -> currentRelation.appendRelation(query, currentRelationType, currentRelationValue));
			break;
		case Relation.contentOrdinal:
			outputQueue.add(query -> currentRelation.appendRelation(query, currentRelationType, currentRelationValue));
			break;
		default:
			throw new IllegalStateException();
		}
		this.currentRelation = null;
		this.currentRelationType = null;
		this.currentRelationValue = null;
	}

	private void appendOperator() {
		final OperatorType currentOperatorType = this.currentOperatorType;
		switch (currentOperatorType) {
		case BEGIN:
			operatorQueue.add(OperatorType.BEGIN);
			break;
		case END:
			for (OperatorType operatorType; !Optional.ofNullable((operatorType = operatorQueue.poll()))
				.map(OperatorType.BEGIN::equals).orElse(true);)
				appendOutput(operatorType);
			if (operatorQueue.isEmpty())
				if (outputQueue.size() == 1)
					action = Functional.compose(outputQueue.element(), action);
				else
					throw new IllegalStateException();
			break;
		case NOT:
			operatorQueue.add(OperatorType.NOT);
			break;
		case AND:
			while (Optional.ofNullable(operatorQueue.peek())
				.map(operatorType -> operatorType.getPrecedence() >= OperatorType.AND.getPrecedence()).orElse(false))
				appendOutput(operatorQueue.remove());
			operatorQueue.add(OperatorType.AND);
			break;
		case OR:
			while (Optional.ofNullable(operatorQueue.peek())
				.map(operatorType -> operatorType.getPrecedence() >= OperatorType.OR.getPrecedence()).orElse(false))
				appendOutput(operatorQueue.remove());
			operatorQueue.add(OperatorType.OR);
			break;
		default:
			throw new IllegalStateException();
		}
		this.currentOperatorType = null;
	}

	private void appendOutput(OperatorType operatorType) {
		final UnaryOperator<QTopicBean> operator1;
		final UnaryOperator<QTopicBean> operator2;
		switch (operatorType) {
		case NOT:
			operator1 = outputQueue.remove();
			outputQueue.add(query -> operator1.apply(query.not()).endJunction());
			break;
		case AND:
			operator2 = outputQueue.remove();
			operator1 = outputQueue.remove();
			outputQueue.add(query -> operator2.apply(operator1.apply(query.and())).endJunction());
			break;
		case OR:
			operator2 = outputQueue.remove();
			operator1 = outputQueue.remove();
			outputQueue.add(query -> operator2.apply(operator1.apply(query.or())).endJunction());
			break;
		default:
			throw new IllegalStateException();
		}
	}

	public QTopicBean apply(QTopicBean query) {
		return action.apply(query.where());
	}

	static final class Relation<T, P, R extends FilterRelation<T, P>> {
		// @formatter:off
		public static final int creatorOrdinal = 0;
		public static final Relation<OfflinePlayer, Void, FilterRelation<OfflinePlayer, Void>> creator = new Relation<>(
			creatorOrdinal,
			(query, relationType) -> value -> relationType.appendRelation(query.creatorUniqueID, value.getUniqueId()));
		public static final int createTimeOrdinal = 1;
		public static final Relation<Date, Void, FilterOrderedRelation<Date, Void>> createTime = new Relation<>(
			createTimeOrdinal,
			(query, relationType) -> value -> relationType.appendOrderedRelation(query.createTime, value));
		public static final int updateTimeOrdinal = 2;
		public static final Relation<Date, Void, FilterOrderedRelation<Date, Void>> updateTime = new Relation<>(
			updateTimeOrdinal,
			(query, relationType) -> value -> relationType.appendOrderedRelation(query.updateTime, value));
		public static final int topicStateOrdinal = 3;
		public static final Relation<String, Void, FilterRelation<String, Void>> topicState = new Relation<>(
			topicStateOrdinal,
			(query, relationType) -> value -> relationType.appendRelation(query.topicState, value));
		public static final int titleOrdinal = 4;
		public static final Relation<String, Void, FilterFulltextRelation<String, Void>> title = new Relation<>(
			titleOrdinal,
			(query, relationType) -> value -> relationType.appendFulltextRelation(query.title, value));
		public static final int contentOrdinal = 5;
		public static final Relation<String, Void, FilterFulltextRelation<String, Void>> content = new Relation<>(
			contentOrdinal,
			(query, relationType) -> value -> relationType.appendFulltextRelation(query.content, value));
		// @formatter:on

		private final int ordinal;
		private final BiFunction<QTopicBean, RelationType, Function<T, QTopicBean>> relationAppender;

		private Relation(int ordinal, BiFunction<QTopicBean, RelationType, Function<T, QTopicBean>> relationAppender) {
			this.ordinal = ordinal;
			this.relationAppender = relationAppender;
		}

		public int ordinal() {
			return ordinal;
		}

		public QTopicBean appendRelation(QTopicBean query, RelationType relationType, T value) {
			return relationAppender.apply(query, relationType).apply(value);
		}

		@SuppressWarnings("unchecked")
		public static final <P> Relation<OfflinePlayer, P, FilterRelation<OfflinePlayer, P>> creatorPoly() {
			return (Relation<OfflinePlayer, P, FilterRelation<OfflinePlayer, P>>) (Relation<?, ?, ?>) creator;
		}

		@SuppressWarnings("unchecked")
		public static final <P> Relation<Date, P, FilterOrderedRelation<Date, P>> createTimePoly() {
			return (Relation<Date, P, FilterOrderedRelation<Date, P>>) (Relation<?, ?, ?>) createTime;
		}

		@SuppressWarnings("unchecked")
		public static final <P> Relation<Date, P, FilterOrderedRelation<Date, P>> updateTimePoly() {
			return (Relation<Date, P, FilterOrderedRelation<Date, P>>) (Relation<?, ?, ?>) updateTime;
		}

		@SuppressWarnings("unchecked")
		public static final <P> Relation<String, P, FilterRelation<String, P>> topicStatePoly() {
			return (Relation<String, P, FilterRelation<String, P>>) (Relation<?, ?, ?>) topicState;
		}

		@SuppressWarnings("unchecked")
		public static final <P> Relation<String, P, FilterFulltextRelation<String, P>> titlePoly() {
			return (Relation<String, P, FilterFulltextRelation<String, P>>) (Relation<?, ?, ?>) title;
		}

		@SuppressWarnings("unchecked")
		public static final <P> Relation<String, P, FilterFulltextRelation<String, P>> contentPoly() {
			return (Relation<String, P, FilterFulltextRelation<String, P>>) (Relation<?, ?, ?>) content;
		}
	}

	static final class MegaFilterRelation<T, P>
		implements FilterRelation<T, P>, FilterOrderedRelation<T, P>, FilterFulltextRelation<T, P> {
		private final MegaFilter<T, P> filter;
		private final Relation<T, P, ? extends FilterRelation<T, P>> relation;

		public MegaFilterRelation(MegaFilter<T, P> filter, Relation<T, P, ? extends FilterRelation<T, P>> relation) {
			this.filter = filter;
			this.relation = relation;
		}

		@Override
		public <R extends FilterTerminate<P> & FilterBinaryJuntion<P>> R gt(T value) {
			filter.currentRelation = relation;
			return filter.gt(value);
		}

		@Override
		public <R extends FilterTerminate<P> & FilterBinaryJuntion<P>> R ge(T value) {
			filter.currentRelation = relation;
			return filter.ge(value);
		}

		@Override
		public <R extends FilterTerminate<P> & FilterBinaryJuntion<P>> R eq(T value) {
			filter.currentRelation = relation;
			return filter.eq(value);
		}

		@Override
		public <R extends FilterTerminate<P> & FilterBinaryJuntion<P>> R le(T value) {
			filter.currentRelation = relation;
			return filter.le(value);
		}

		@Override
		public <R extends FilterTerminate<P> & FilterBinaryJuntion<P>> R lt(T value) {
			filter.currentRelation = relation;
			return filter.lt(value);
		}

		@Override
		public <R extends FilterTerminate<P> & FilterBinaryJuntion<P>> R contains(T value) {
			filter.currentRelation = relation;
			return filter.contains(value);
		}

	}

	enum RelationType {
		GT, GE, EQ, LE, LT, CONTAINS;

		public <R, T> R appendRelation(PBaseValueEqual<R, T> property, T value) {
			switch (this) {
			case EQ:
				return property.eq(value);
			default:
				throw new IllegalStateException();
			}
		}

		public <R> R appendRelation(PBoolean<R> property, boolean value) {
			switch (this) {
			case EQ:
				return property.eq(value);
			default:
				throw new IllegalStateException();
			}
		}

		public <R, E> R appendRelation(PEnum<R, E> property, E value) {
			switch (this) {
			case EQ:
				return property.eq(value);
			default:
				throw new IllegalStateException();
			}
		}

		public <R, T> R appendRelation(PBaseNumber<R, T> property, T value) {
			switch (this) {
			case EQ:
				return property.eq(value);
			default:
				throw new IllegalStateException();
			}
		}

		public <R, T> R appendRelation(PBaseDate<R, T> property, T value) {
			switch (this) {
			case EQ:
				return property.eq(value);
			default:
				throw new IllegalStateException();
			}
		}

		public <R> R appendRelation(PString<R> property, String value) {
			switch (this) {
			case EQ:
				return property.eq(value);
			default:
				throw new IllegalStateException();
			}
		}

		public <R, T> R appendOrderedRelation(PBaseNumber<R, T> property, T value) {
			switch (this) {
			case GT:
				return property.gt(value);
			case GE:
				return property.ge(value);
			case EQ:
				return property.eq(value);
			case LE:
				return property.le(value);
			case LT:
				return property.lt(value);
			default:
				throw new IllegalStateException();
			}
		}

		public <R, T> R appendOrderedRelation(PBaseDate<R, T> property, T value) {
			switch (this) {
			case GT:
				return property.gt(value);
			case GE:
				return property.ge(value);
			case EQ:
				return property.eq(value);
			case LE:
				return property.le(value);
			case LT:
				return property.lt(value);
			default:
				throw new IllegalStateException();
			}
		}

		public <R> R appendFulltextRelation(PString<R> property, String value) {
			switch (this) {
			case EQ:
				return property.eq(value);
			case CONTAINS:
				return property.contains(value);
			default:
				throw new IllegalStateException();
			}
		}
	}

	enum OperatorType {
		BEGIN(0), END(0), NOT(3), AND(2), OR(1);

		private final int precedence;

		private OperatorType(int precedence) {
			this.precedence = precedence;
		}

		public int getPrecedence() {
			return precedence;
		}
	}

	private <R extends FilterRelation<T, P>> R relationOf(Relation<T, P, R> relation) {
		@SuppressWarnings("unchecked")
		R filterRelation = (R) new MegaFilterRelation<T, P>(this, relation);
		return filterRelation;
	}

	private <NEW_T, NEW_P, R extends FilterCondition<NEW_P> & Filter & FilterDef<NEW_P> & FilterInitial<NEW_P> & FilterTerminate<NEW_P> & FilterRelation<NEW_T, NEW_P> & FilterOrderedRelation<NEW_T, NEW_P> & FilterFulltextRelation<NEW_T, NEW_P> & FilterUnaryJuntion<NEW_P> & FilterBinaryJuntion<NEW_P>> R castDiscriminatory() {
		@SuppressWarnings("unchecked")
		R megaFilter = (R) this;
		return megaFilter;
	}

	private <NEW_T, NEW_P, R extends Filter & FilterDef<NEW_P> & FilterInitial<NEW_P> & FilterTerminate<NEW_P> & FilterRelation<NEW_T, NEW_P> & FilterOrderedRelation<NEW_T, NEW_P> & FilterFulltextRelation<NEW_T, NEW_P> & FilterUnaryJuntion<NEW_P> & FilterBinaryJuntion<NEW_P>> R castRespect() {
		@SuppressWarnings("unchecked")
		R megaFilter = (R) this;
		return megaFilter;
	}

	public static <R extends FilterInitial<Filter> & FilterTerminate<Filter>> R of() {
		return new MegaFilter<>().castRespect();
	}
}
