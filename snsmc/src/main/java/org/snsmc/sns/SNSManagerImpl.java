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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.snsmc.Main;
import org.snsmc.api.persist.listing.ListingOrder;
import org.snsmc.api.persist.listing.ListingOrderField;
import org.snsmc.api.persist.search.Filter;
import org.snsmc.api.persist.search.Sorter;
import org.snsmc.api.sns.SNSListener;
import org.snsmc.api.sns.SNSManager;
import org.snsmc.api.sns.SNSReplyHandle;
import org.snsmc.api.sns.SNSTopicHandle;
import org.snsmc.api.translate.Locale;
import org.snsmc.config.TopicState;
import org.snsmc.holo.Hologram;
import org.snsmc.holo.HologramListener;
import org.snsmc.holo.HologramManager;
import org.snsmc.persist.TopicManager;
import org.snsmc.persist.TopicRef;
import org.snsmc.translate.I18N;
import org.snsmc.util.LineFormatter;
import org.snsmc.util.LineTruncator;
import org.snsmc.util.Strings;

public class SNSManagerImpl implements SNSManager {
	private final HologramManager hologramManager;
	private final TopicManager topicManager;
	private final Map<TopicRef, Map<Locale, Hologram>> topicsMap;
	private final Map<Player, Set<TopicState>> playerInvisibleTopics;
	private final List<SNSListener> listeners;

	public SNSManagerImpl(HologramManager hologramManager, TopicManager topicManager) {
		this.hologramManager = hologramManager;
		this.topicManager = topicManager;
		this.topicsMap = new HashMap<>();
		this.playerInvisibleTopics = new HashMap<>();
		this.listeners = new ArrayList<>();
		refresh();
	}

	@Override
	public SNSTopicHandle createTopic(OfflinePlayer creator, Location location, String title, String state,
		String content) {
		Date currentTime = getCurrentTime();
		TopicRef topicRef = topicManager.createTopic(creator, location, currentTime, currentTime, title, state,
			content);
		Map<Locale, Hologram> holograms = new HashMap<>();
		I18N.getLocales().stream().forEach(locale -> {
			Hologram hologram = createHologram(topicRef, locale);
			holograms.put(locale, hologram);
		});
		topicsMap.put(topicRef, holograms);
		return new SNSTopicHandleImpl(this, topicRef);
	}

	@Override
	public Stream<SNSTopicHandle> searchTopic(Filter filter, Sorter sorter, int firstTopic, int maxTopic) {
		return topicManager.searchTopic(filter, sorter, firstTopic, maxTopic).stream()
			.map(topicRef -> new SNSTopicHandleImpl(this, topicRef));
	}

	@Override
	public Optional<SNSTopicHandle> getTopic(long id) {
		return topicManager.getTopic(id).map(topicRef -> new SNSTopicHandleImpl(this, topicRef));
	}

	@Override
	public Optional<SNSReplyHandle> getReply(long id) {
		return topicManager.getReply(id)
			.map(replyRef -> new SNSReplyHandleImpl(new SNSTopicHandleImpl(this, replyRef.getTopicRef()), replyRef));
	}

	protected void updateTopic(SNSTopicHandleImpl snsTopic) {
		TopicRef topicRef = snsTopic.getTopicRef();
		topicRef.setUpdateTime(getCurrentTime());
		topicRef.save();
		Map<Locale, Hologram> holograms = topicsMap.get(topicRef);
		Location location = topicRef.getLocation();
		holograms.entrySet().stream().forEach(entry -> {
			Locale locale = entry.getKey();
			Hologram hologram = entry.getValue();
			String text = generateHologramText(topicRef, locale);
			if (!hologram.getLocation().equals(location))
				hologram.setLocation(location);
			if (!hologram.getText().equals(text))
				hologram.setText(text);
		});
	}

	protected void deleteTopic(SNSTopicHandleImpl snsTopic) {
		TopicRef topicRef = snsTopic.getTopicRef();
		Optional.ofNullable(topicsMap.remove(topicRef)).ifPresent(holograms -> {
			topicManager.deleteTopic(topicRef);
			holograms.values().stream().forEach(hologramManager::removeHologram);
		});
	}

	@Override
	public void registerSNSListener(SNSListener snsListener) {
		listeners.add(snsListener);
	}

	@Override
	public void refresh() {
		topicsMap.values().stream()
			.forEach(holograms -> holograms.values().stream().forEach(hologramManager::removeHologram));
		topicsMap.clear();
		topicManager.getTopices().stream().forEach(topicRef -> {
			Map<Locale, Hologram> holograms = new HashMap<>();
			I18N.getLocales().stream().forEach(locale -> {
				Hologram hologram = createHologram(topicRef, locale);
				holograms.put(locale, hologram);
			});
			topicsMap.put(topicRef, holograms);
		});
	}

	private Hologram createHologram(TopicRef topicRef, Locale locale) {
		// @formatter:off
		Hologram hologram = hologramManager.createHologram(topicRef.getLocation(),
			player -> I18N.getLocaleOrDefault(player).equals(locale)
				&& !Optional.ofNullable(playerInvisibleTopics.get(player))
				.map(invisibleTopics -> invisibleTopics.stream()
					.map(TopicState::getName)
					.anyMatch(topicRef.getTopicState()::equals))
				.orElse(false), generateHologramText(topicRef, locale));
		hologram.registerHologramListener(new HologramListener() {
			@Override
			public void onInteract(Player player, Hologram hologram) {
				listeners.stream()
					.forEach(listener -> listener.onClickTopic(player, new SNSTopicHandleImpl(SNSManagerImpl.this, topicRef)));
			}
		});
		return hologram;
		// @formatter:on
	}

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private String generateHologramText(TopicRef topicRef, Locale locale) {
		int labelMaxWidth = Main.getInstance().getConfigManager().getLabelMaxWidth();
		int labelMaxLine = Main.getInstance().getConfigManager().getLabelMaxLine();
		int replyMaxLine = Math.max(labelMaxLine - 4, 0);
		String creatorName = Optional.ofNullable(topicRef.getCreator().getName()).orElse("<UNKNOWN>");
		// @formatter:off
		return
			generateHologramReply(
				new LineTruncator(labelMaxLine)
					.append(new LineFormatter(labelMaxWidth, false, true)
						.append(Long.toString(topicRef.getId()))
						.append(" | ")
						.append(Strings.concateLine(topicRef.getTitle(), " "), 16)
						.append(ChatColor.RESET.toString())
						.append(" | ")
						.append(TopicState.translateTopicState(locale, topicRef.getTopicState()))
						.append(ChatColor.RESET.toString())
					.toString())
					.append(new LineFormatter(labelMaxWidth, false, true)
						.append(creatorName, 8)
						.append(ChatColor.RESET.toString())
						.append(" | ")
						.append(DATE_FORMAT.format(topicRef.getCreateTime()))
						.append(" | ")
						.append(DATE_FORMAT.format(topicRef.getUpdateTime()))
					.toString())
					.append(new LineFormatter(labelMaxWidth, false, true)
						.append(Strings.concateLine(topicRef.getContent(), " "), 16)
						.append(ChatColor.RESET.toString())
					.toString())
					.append(new LineFormatter(labelMaxWidth, false, true)
						.append(Strings.repeat('-', labelMaxWidth), labelMaxWidth)
					.toString()), topicRef, locale, labelMaxWidth, replyMaxLine)
				.toString();
		// @formatter:on
	}
	
	private LineTruncator generateHologramReply(LineTruncator lineTruncator, TopicRef topicRef, Locale locale, int labelMaxWidth, int replyMaxLine) {
		topicRef.listingReply(ListingOrderField.CREATE_TIME, ListingOrder.DESC, 0, replyMaxLine).stream().forEachOrdered(replyRef -> {
			String creatorName = Optional.ofNullable(replyRef.getCreator().getName()).orElse("<UNKNOWN>");
			// @formatter:off
			lineTruncator
				.append(new LineFormatter(labelMaxWidth, false, true)
					.append(Long.toString(replyRef.getId()))
					.append(" | ")
					.append(creatorName, 8)
					.append(ChatColor.RESET.toString())
					.append(" | ")
					.append(Strings.concateLine(replyRef.getContent(), " "), 16)
					.append(ChatColor.RESET.toString())
				.toString());
			// @formatter:on
		});
		return lineTruncator;
	}

	protected Date getCurrentTime() {
		return new Date();
	}

	public void onPlayerJoin(Player player) {
		playerInvisibleTopics.put(player, new HashSet<>());
	}

	public void onPlayerQuit(Player player) {
		playerInvisibleTopics.remove(player);
	}
}
