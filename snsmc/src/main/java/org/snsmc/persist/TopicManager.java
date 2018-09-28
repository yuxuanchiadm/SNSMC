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
package org.snsmc.persist;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.snsmc.persist.bean.query.QReplyBean;
import org.snsmc.persist.bean.query.QTopicBean;
import org.snsmc.persist.search.MegaFilter;
import org.snsmc.persist.search.MegaSorter;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;

import org.snsmc.api.persist.search.Filter;
import org.snsmc.api.persist.search.Sorter;
import org.snsmc.persist.bean.GlobalLocationBean;
import org.snsmc.persist.bean.TopicBean;

import io.ebean.EbeanServer;

public class TopicManager {
	private final EbeanServer ebeanServer;
	private final TLongObjectMap<TopicRef> topics;

	public TopicManager(EbeanServer ebeanServer) {
		this.ebeanServer = ebeanServer;
		this.topics = new TLongObjectHashMap<>();
		refresh();
	}

	public EbeanServer getEbeanServer() {
		return ebeanServer;
	}

	public TopicRef createTopic(OfflinePlayer creator, Location location, Date createTime, Date updateTime,
		String title, String state, String content) {
		TopicBean topicBean = new TopicBean(title, creator.getUniqueId(), createTime, updateTime, state,
			new GlobalLocationBean(location.getWorld().getUID(), location.getX(), location.getY(), location.getZ()),
			content);
		TopicRef topicRef = new TopicRef(this, topicBean);
		topicRef.save();
		topics.put(topicRef.getId(), topicRef);
		return topicRef;
	}

	public void deleteTopic(TopicRef topicRef) {
		if (!topics.containsKey(topicRef.getId()))
			throw new IllegalStateException();
		topicRef.setDeleted(true);
		topicRef.save();
		topics.remove(topicRef.getId());
	}

	public List<TopicRef> searchTopic(Filter filter, Sorter sorter, int firstTopic, int maxTopic) {
		// @formatter:off
		QTopicBean query = sorter.<MegaSorter> cast().apply(filter.<MegaFilter<?, ?>> cast().apply(
			new QTopicBean(ebeanServer)
				.select(QTopicBean.alias().id)))
				.where()
					.deleted.isFalse()
					.setFirstRow(firstTopic).setMaxRows(maxTopic);
		// @formatter:on
		return query.findList().stream().flatMap(topicBean -> ofNullable(topics.get(topicBean.getId())))
			.collect(Collectors.toList());
	}

	public Optional<TopicRef> getTopic(long id) {
		return Optional.ofNullable(topics.get(id));
	}

	public Optional<ReplyRef> getReply(long id) {
		// @formatter:off
		QReplyBean query = new QReplyBean(ebeanServer)
			.select(QReplyBean.alias().topic.id)
			.where()
				.id.eq(id)
				.deleted.isFalse();
		// @formatter:on
		return query.findOneOrEmpty().flatMap(replyBean -> {
			long topicId = replyBean.getTopic().getId();
			return getTopic(topicId).flatMap(topicRef -> topicRef.getReply(id));
		});
	}

	public Set<TopicRef> getTopices() {
		return Collections.unmodifiableSet(new HashSet<>(topics.valueCollection()));
	}

	private static <T> Stream<T> ofNullable(T t) {
		return t == null ? Stream.empty() : Stream.of(t);
	}

	public void refresh() {
		topics.clear();
		// @formatter:off
		new QTopicBean(ebeanServer)
			.where()
				.deleted.isFalse()
			.findList().stream().forEach(topicBean -> {
				TopicRef topicRef = new TopicRef(this, topicBean);
				topics.put(topicRef.getId(), topicRef);
			});
		// @formatter:on
	}
}
