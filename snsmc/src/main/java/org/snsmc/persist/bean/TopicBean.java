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
package org.snsmc.persist.bean;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table
public class TopicBean {
	@Id
	@GeneratedValue
	@Column(unique = true, nullable = false)
	private long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private UUID creatorUniqueID;

	@Column(nullable = false)
	private Date createTime;

	@Column(nullable = false)
	private Date updateTime;

	@Column(nullable = false)
	private String topicState;

	@Embedded
	private GlobalLocationBean globalLocation;

	@Column(nullable = false)
	private String content;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "topic")
	private Set<ReplyBean> replies;

	@Column(nullable = false)
	private boolean deleted = false;

	public TopicBean() {

	}

	public TopicBean(String title, UUID creatorUniqueID, Date createTime, Date updateTime, String topicState,
		GlobalLocationBean globalLocation, String content) {
		this.title = title;
		this.creatorUniqueID = creatorUniqueID;
		this.createTime = createTime;
		this.updateTime = updateTime;
		this.topicState = topicState;
		this.globalLocation = globalLocation;
		this.content = content;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public UUID getCreatorUniqueID() {
		return creatorUniqueID;
	}

	public void setCreatorUniqueID(UUID creatorUniqueID) {
		this.creatorUniqueID = creatorUniqueID;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getTopicState() {
		return topicState;
	}

	public void setTopicState(String topicState) {
		this.topicState = topicState;
	}

	public GlobalLocationBean getGlobalLocation() {
		return globalLocation;
	}

	public void setGlobalLocation(GlobalLocationBean globalLocation) {
		this.globalLocation = globalLocation;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Set<ReplyBean> getReplies() {
		return replies;
	}

	public void setReplies(Set<ReplyBean> replies) {
		this.replies = replies;
	}

	public boolean addReply(ReplyBean reply) {
		return replies.add(reply);
	}
	
	public boolean removeReply(ReplyBean reply) {
		return replies.remove(reply);
	}
	
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
}
