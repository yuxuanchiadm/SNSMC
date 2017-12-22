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

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class GlobalLocationBean {
	@Column(nullable = false)
	private UUID worldUniqueID;

	@Column(nullable = false)
	private double x;

	@Column(nullable = false)
	private double y;

	@Column(nullable = false)
	private double z;

	public GlobalLocationBean() {

	}

	public GlobalLocationBean(UUID worldUniqueID, double x, double y, double z) {
		this.worldUniqueID = worldUniqueID;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public UUID getWorldUniqueID() {
		return worldUniqueID;
	}

	public void setWorldUniqueID(UUID worldUniqueID) {
		this.worldUniqueID = worldUniqueID;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}
}
