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
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table
public class ReplyBean {
	@Id
	@GeneratedValue
	@Column(unique = true, nullable = false)
	private long id;

	@ManyToOne(cascade = CascadeType.ALL, optional = false)
	@JoinColumn(nullable = false)
	private TopicBean topic;

	@Column(nullable = false)
	private UUID creatorUniqueID;

	@Column(nullable = false)
	private Date createTime;

	@Column(nullable = false)
	private Date updateTime;

	@Column(nullable = false)
	private String content;

	@Column(nullable = false)
	private boolean deleted = false;

	public ReplyBean() {

	}

	public ReplyBean(TopicBean topic, UUID creatorUniqueID, Date createTime, Date updateTime, String content) {
		this.topic = topic;
		this.creatorUniqueID = creatorUniqueID;
		this.createTime = createTime;
		this.updateTime = updateTime;
		this.content = content;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public TopicBean getTopic() {
		return topic;
	}

	public void setTopic(TopicBean topic) {
		this.topic = topic;
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
}
