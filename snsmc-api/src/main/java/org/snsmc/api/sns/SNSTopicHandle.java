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
package org.snsmc.api.sns;

import java.util.Date;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.snsmc.api.persist.listing.ListingOrder;
import org.snsmc.api.persist.listing.ListingOrderField;

public interface SNSTopicHandle {
	SNSManager getSNSManager();

	long getId();

	String getTitle();

	OfflinePlayer getCreator();

	Date getCreateTime();

	Date getUpdateTime();

	String getTopicState();

	Location getLocation();

	String getContent();

	public interface TopicUpdater {
		void setTitle(String title);

		void setTopicState(String topicState);

		void setLocation(Location location);

		void setContent(String content);
	}

	void updateTopic(Consumer<TopicUpdater> updater);

	Stream<SNSReplyHandle> getReplies();

	Stream<SNSReplyHandle> listingReply(ListingOrderField listingOrderField, ListingOrder listingOrder, int firstReply,
		int maxReply);

	SNSReplyHandle addReply(OfflinePlayer player, String content);

	void delete();
}