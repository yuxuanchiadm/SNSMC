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

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.snsmc.Main;
import org.snsmc.api.persist.listing.ListingOrder;
import org.snsmc.api.persist.listing.ListingOrderField;
import org.snsmc.persist.bean.query.QReplyBean;
import org.snsmc.persist.listing.MegaListing;
import org.snsmc.util.Functional;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;

import org.snsmc.persist.bean.GlobalLocationBean;
import org.snsmc.persist.bean.ReplyBean;
import org.snsmc.persist.bean.TopicBean;

import io.ebean.EbeanServer;

public class TopicRef {
	private final TopicManager topicManager;
	private final TopicBean topicBean;
	private final TLongObjectMap<ReplyRef> replies;

	public TopicRef(TopicManager topicManager, TopicBean topicBean) {
		this.topicManager = topicManager;
		this.topicBean = topicBean;
		this.replies = new TLongObjectHashMap<>();
		refresh();
	}

	public TopicManager getTopicManager() {
		return topicManager;
	}

	protected TopicBean getTopicBean() {
		return topicBean;
	}

	private EbeanServer getEbeanServer() {
		return topicManager.getEbeanServer();
	}

	public long getId() {
		return topicBean.getId();
	}

	protected void setId(long id) {
		topicBean.setId(id);
	}

	public String getTitle() {
		return topicBean.getTitle();
	}

	public void setTitle(String title) {
		topicBean.setTitle(title);
	}

	public OfflinePlayer getCreator() {
		return Main.getInstance().getServer().getOfflinePlayer(topicBean.getCreatorUniqueID());
	}

	public void setCreator(OfflinePlayer player) {
		topicBean.setCreatorUniqueID(player.getUniqueId());
	}

	public Date getCreateTime() {
		return topicBean.getCreateTime();
	}

	public void setCreateTime(Date createTime) {
		topicBean.setCreateTime(createTime);
	}

	public Date getUpdateTime() {
		return topicBean.getUpdateTime();
	}

	public void setUpdateTime(Date updateTime) {
		topicBean.setUpdateTime(updateTime);
	}

	public String getTopicState() {
		return topicBean.getTopicState();
	}

	public void setTopicState(String topicState) {
		topicBean.setTopicState(topicState);
	}

	public Location getLocation() {
		GlobalLocationBean globalLocationBean = topicBean.getGlobalLocation();
		World world = Main.getInstance().getServer().getWorld(globalLocationBean.getWorldUniqueID());
		if (world == null)
			return null;
		return new Location(world, globalLocationBean.getX(), globalLocationBean.getY(), globalLocationBean.getZ());
	}

	public void setLocation(Location location) {
		topicBean.setGlobalLocation(
			new GlobalLocationBean(location.getWorld().getUID(), location.getX(), location.getY(), location.getZ()));
	}

	public String getContent() {
		return topicBean.getContent();
	}

	public void setContent(String content) {
		topicBean.setContent(content);
	}

	public boolean isDeleted() {
		return topicBean.isDeleted();
	}

	protected void setDeleted(boolean deleted) {
		topicBean.setDeleted(deleted);
	}

	public Optional<ReplyRef> getReply(long id) {
		return Optional.ofNullable(replies.get(id));
	}

	public Set<ReplyRef> getReplies() {
		return Collections.unmodifiableSet(new HashSet<>(replies.valueCollection()));
	}

	public ReplyRef addReply(OfflinePlayer player, String content, Date createTime, Date updateTime) {
		ReplyBean replyBean = new ReplyBean(topicBean, player.getUniqueId(), createTime, updateTime, content);
		topicBean.addReply(replyBean);
		ReplyRef replyRef = new ReplyRef(this, replyBean);
		replyRef.save();
		replies.put(replyRef.getId(), replyRef);
		return replyRef;
	}

	public void deleteReply(ReplyRef replyRef) {
		if (!replies.containsKey(replyRef.getId()))
			throw new IllegalStateException();
		topicBean.removeReply(replyRef.getReplyBean());
		replyRef.setDeleted(true);
		replyRef.save();
		replies.remove(replyRef.getId());
	}

	public List<ReplyRef> listingReply(ListingOrderField listingOrderField, ListingOrder listingOrder, int firstReply,
		int maxReply) {
		// @formatter:off
		return MegaListing.appendOrderField(listingOrderField, listingOrder, new QReplyBean(getEbeanServer())
			.select(QReplyBean.alias().id)
			.where()
				.topic.equalTo(topicBean)
				.deleted.isFalse())
			.setFirstRow(firstReply)
			.setMaxRows(maxReply)
			.findList().stream().flatMap(replyBean -> {
				long replyID = replyBean.getId();
				return Functional.asStream(getReply(replyID));
			}).collect(Collectors.toList());
		// @formatter:on
	}

	public void save() {
		getEbeanServer().save(topicBean);
	}

	public void refresh() {
		replies.clear();
		// @formatter:off
		new QReplyBean(getEbeanServer())
			.where()
				.topic.equalTo(topicBean)
				.deleted.isFalse()
			.findList().stream().forEach(replyBean -> {
				ReplyRef replyRef = new ReplyRef(this, replyBean);
				replies.put(replyRef.getId(), replyRef);
			});
		// @formatter:on
	}
}
