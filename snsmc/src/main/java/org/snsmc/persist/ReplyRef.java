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

import java.util.Date;

import org.bukkit.OfflinePlayer;
import org.snsmc.Main;
import org.snsmc.persist.bean.ReplyBean;

import io.ebean.EbeanServer;

public class ReplyRef {
	private final TopicRef topicRef;
	private final ReplyBean replyBean;

	public ReplyRef(TopicRef topicRef, ReplyBean replyBean) {
		this.topicRef = topicRef;
		this.replyBean = replyBean;
	}

	public TopicRef getTopicRef() {
		return topicRef;
	}

	protected ReplyBean getReplyBean() {
		return replyBean;
	}

	private EbeanServer getEbeanServer() {
		return topicRef.getTopicManager().getEbeanServer();
	}

	public long getId() {
		return replyBean.getId();
	}

	protected void setId(long id) {
		replyBean.setId(id);
	}

	public OfflinePlayer getCreator() {
		return Main.getInstance().getServer().getOfflinePlayer(replyBean.getCreatorUniqueID());
	}

	public void setCreator(OfflinePlayer player) {
		replyBean.setCreatorUniqueID(player.getUniqueId());
	}

	public Date getCreateTime() {
		return replyBean.getCreateTime();
	}

	public void setCreateTime(Date createTime) {
		replyBean.setCreateTime(createTime);
	}

	public Date getUpdateTime() {
		return replyBean.getUpdateTime();
	}

	public void setUpdateTime(Date updateTime) {
		replyBean.setUpdateTime(updateTime);
	}

	public String getContent() {
		return replyBean.getContent();
	}

	public void setContent(String content) {
		replyBean.setContent(content);
	}

	public boolean isDeleted() {
		return replyBean.isDeleted();
	}

	protected void setDeleted(boolean deleted) {
		replyBean.setDeleted(deleted);
	}

	public void save() {
		getEbeanServer().save(replyBean);
	}
}
