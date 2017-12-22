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

import org.bukkit.OfflinePlayer;
import org.snsmc.api.sns.SNSReplyHandle;
import org.snsmc.api.sns.SNSTopicHandle;
import org.snsmc.persist.ReplyRef;

public class SNSReplyHandleImpl implements SNSReplyHandle {
	private final SNSTopicHandleImpl snsTopicHandle;
	private final ReplyRef replyRef;

	public SNSReplyHandleImpl(SNSTopicHandleImpl snsTopicHandle, ReplyRef replyRef) {
		this.snsTopicHandle = snsTopicHandle;
		this.replyRef = replyRef;
	}

	@Override
	public SNSTopicHandle getSNSTopicHandle() {
		return snsTopicHandle;
	}

	protected ReplyRef getReplyRef() {
		return replyRef;
	}

	@Override
	public long getId() {
		return replyRef.getId();
	}

	@Override
	public OfflinePlayer getCreator() {
		return replyRef.getCreator();
	}

	@Override
	public Date getCreateTime() {
		return replyRef.getCreateTime();
	}

	@Override
	public Date getUpdateTime() {
		return replyRef.getUpdateTime();
	}

	@Override
	public String getContent() {
		return replyRef.getContent();
	}

	@Override
	public void updateReply(Consumer<ReplyUpdater> updater) {
		if (replyRef.isDeleted())
			throw new IllegalStateException();
		ReplyUpdaterImpl replyUpdater = new ReplyUpdaterImpl();
		updater.accept(replyUpdater);
		if (replyUpdater.isDirty())
			snsTopicHandle.updateReply(this);
	}

	public class ReplyUpdaterImpl implements ReplyUpdater {
		private boolean isDirty;

		ReplyUpdaterImpl() {
			isDirty = false;
		}

		@Override
		public void setContent(String content) {
			isDirty = true;
			replyRef.setContent(content);
		}

		boolean isDirty() {
			return isDirty;
		}
	}

	@Override
	public void delete() {
		if (replyRef.isDeleted())
			throw new IllegalStateException();
		snsTopicHandle.deleteReply(this);
	}
}
