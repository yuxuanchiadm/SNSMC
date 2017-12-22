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
package org.snsmc.persist.listing;

import org.snsmc.api.persist.listing.ListingOrder;
import org.snsmc.api.persist.listing.ListingOrderField;
import org.snsmc.persist.bean.query.QReplyBean;

import io.ebean.typequery.TQProperty;

public final class MegaListing {
	public static QReplyBean appendOrderField(ListingOrderField listingOrderField, ListingOrder listingOrder,
		QReplyBean query) {
		switch (listingOrderField) {
		case CREATE_TIME:
			return appendOrder(listingOrder, query.createTime);
		case UPDATE_TIME:
			return appendOrder(listingOrder, query.updateTime);
		default:
			throw new IllegalStateException();
		}
	}

	public static QReplyBean appendOrder(ListingOrder listingOrder, TQProperty<QReplyBean> property) {
		switch (listingOrder) {
		case ASC:
			return property.asc();
		case DESC:
			return property.desc();
		default:
			throw new IllegalStateException();
		}
	}
}
