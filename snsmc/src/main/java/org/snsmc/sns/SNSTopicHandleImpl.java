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
package org.snsmc.sns;

import java.util.Date;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.snsmc.api.persist.listing.ListingOrder;
import org.snsmc.api.persist.listing.ListingOrderField;
import org.snsmc.api.sns.SNSManager;
import org.snsmc.api.sns.SNSReplyHandle;
import org.snsmc.api.sns.SNSTopicHandle;
import org.snsmc.persist.ReplyRef;
import org.snsmc.persist.TopicRef;

public class SNSTopicHandleImpl implements SNSTopicHandle {
	private final SNSManagerImpl snsManager;
	private final TopicRef topicRef;

	protected SNSTopicHandleImpl(SNSManagerImpl snsManager, TopicRef topicRef) {
		this.snsManager = snsManager;
		this.topicRef = topicRef;
	}

	@Override
	public SNSManager getSNSManager() {
		return snsManager;
	}

	protected TopicRef getTopicRef() {
		return topicRef;
	}

	@Override
	public long getId() {
		return topicRef.getId();
	}

	@Override
	public String getTitle() {
		return topicRef.getTitle();
	}

	@Override
	public OfflinePlayer getCreator() {
		return topicRef.getCreator();
	}

	@Override
	public Date getCreateTime() {
		return topicRef.getCreateTime();
	}

	@Override
	public Date getUpdateTime() {
		return topicRef.getUpdateTime();
	}

	@Override
	public String getTopicState() {
		return topicRef.getTopicState();
	}

	@Override
	public Location getLocation() {
		return topicRef.getLocation();
	}

	@Override
	public String getContent() {
		return topicRef.getContent();
	}

	@Override
	public void updateTopic(Consumer<TopicUpdater> updater) {
		if (topicRef.isDeleted())
			throw new IllegalStateException();
		TopicUpdaterImpl topicUpdater = new TopicUpdaterImpl();
		updater.accept(topicUpdater);
		if (topicUpdater.isDirty())
			snsManager.updateTopic(this);
	}

	public class TopicUpdaterImpl implements TopicUpdater {
		private boolean isDirty;

		TopicUpdaterImpl() {
			isDirty = false;
		}

		@Override
		public void setTitle(String title) {
			isDirty = true;
			topicRef.setTitle(title);
		}

		@Override
		public void setTopicState(String topicState) {
			isDirty = true;
			topicRef.setTopicState(topicState);
		}

		@Override
		public void setLocation(Location location) {
			isDirty = true;
			topicRef.setLocation(location);
		}

		@Override
		public void setContent(String content) {
			isDirty = true;
			topicRef.setContent(content);
		}

		boolean isDirty() {
			return isDirty;
		}
	}

	@Override
	public Stream<SNSReplyHandle> getReplies() {
		return topicRef.getReplies().stream().map(replyRef -> new SNSReplyHandleImpl(this, replyRef));
	}

	@Override
	public Stream<SNSReplyHandle> listingReply(ListingOrderField listingOrderField, ListingOrder listingOrder,
		int firstReply, int maxReply) {
		return topicRef.listingReply(listingOrderField, listingOrder, firstReply, maxReply).stream()
			.map(replyRef -> new SNSReplyHandleImpl(this, replyRef));
	}

	@Override
	public SNSReplyHandle addReply(OfflinePlayer player, String content) {
		Date currentTime = snsManager.getCurrentTime();
		ReplyRef replyRef = topicRef.addReply(player, content, currentTime, currentTime);
		snsManager.updateTopic(this);
		return new SNSReplyHandleImpl(this, replyRef);
	}

	protected void updateReply(SNSReplyHandleImpl snsReply) {
		if (snsReply.getSNSTopicHandle().equals(this))
			throw new IllegalStateException();
		ReplyRef replyRef = snsReply.getReplyRef();
		replyRef.setUpdateTime(snsManager.getCurrentTime());
		replyRef.save();
		snsManager.updateTopic(this);
	}

	protected void deleteReply(SNSReplyHandleImpl snsReply) {
		if (snsReply.getSNSTopicHandle().equals(this))
			throw new IllegalStateException();
		ReplyRef replyRef = snsReply.getReplyRef();
		topicRef.deleteReply(replyRef);
		snsManager.updateTopic(this);
	}

	@Override
	public void delete() {
		if (topicRef.isDeleted())
			throw new IllegalStateException();
		snsManager.deleteTopic(this);
	}
}
