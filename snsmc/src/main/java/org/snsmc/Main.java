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
package org.snsmc;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.jar.JarFile;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.snsmc.api.persist.search.FilterDef;
import org.snsmc.api.persist.search.SorterDef;
import org.snsmc.api.sns.SNSManager;
import org.snsmc.api.translate.TranslateManager;
import org.snsmc.commnad.CommandReceiver;
import org.snsmc.config.ConfigManager;
import org.snsmc.holo.HologramManager;
import org.snsmc.holo.entity.EntityManager;
import org.snsmc.listener.PlayerListener;
import org.snsmc.listener.TickListener;
import org.snsmc.listener.protocol.PlayClientUseEntityListener;
import org.snsmc.persist.TopicManager;
import org.snsmc.persist.search.MegaFilter;
import org.snsmc.persist.search.MegaSorter;
import org.snsmc.sns.SNSManagerImpl;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.config.ServerConfig;
import io.ebean.config.TableName;
import io.ebean.config.UnderscoreNamingConvention;

public class Main extends JavaPlugin {
	private static Main INSTANCE;

	private JarFile jarFile;
	private ConfigManager configManager;
	private TranslateManager translateManager;
	private ProtocolManager protocolManager;
	private PluginCommand pluginCommand;
	private CommandReceiver commandReceiver;
	private EntityManager entityManager;
	private HologramManager hologramManager;
	private EbeanServer ebeanServer;
	private TopicManager topicManager;
	private SNSManagerImpl snsManager;

	@Override
	public void onEnable() {
		INSTANCE = this;

		saveDefaultConfig();

		try {
			jarFile = new JarFile(getFile());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		configManager = new ConfigManager(getConfig());
		translateManager = new TranslateManager(getJarFile(), getDataFolder());
		protocolManager = ProtocolLibrary.getProtocolManager();
		pluginCommand = getCommand("snsmc");
		commandReceiver = new CommandReceiver();
		entityManager = new EntityManager(configManager.getHoloMaxTrackingRange(),
			configManager.getHoloUpdateFrequency());
		hologramManager = new HologramManager();
		ebeanServer = initEbeanServer();
		topicManager = new TopicManager(ebeanServer);
		snsManager = new SNSManagerImpl(hologramManager, topicManager);
		pluginCommand.setExecutor(commandReceiver);

		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		getServer().getScheduler().runTaskTimer(this, new TickListener(), 0L, 1L);
		protocolManager.addPacketListener(new PlayClientUseEntityListener());

		getServer().getServicesManager().register(SNSManager.class, snsManager, this, ServicePriority.Highest);
	}

	@Override
	public void onDisable() {
		ebeanServer.shutdown(true, true);
	}

	static {
		FilterDef.FilterImpl.INITIALIZER.initialize(MegaFilter::of);
		SorterDef.SorterImpl.INITIALIZER.initialize(MegaSorter::of);
	}

	private EbeanServer initEbeanServer() {
		Thread currentThread = Thread.currentThread();
		ClassLoader prev = currentThread.getContextClassLoader();
		currentThread.setContextClassLoader(getClassLoader());
		try {
			ServerConfig config = new ServerConfig();
			config.setName("db");
			config.getDataSourceConfig().setDriver(configManager.getDatabaseDriver());
			config.getDataSourceConfig().setUrl(configManager.getDatabaseURL());
			config.getDataSourceConfig().setUsername(configManager.getDatabaseUser());
			config.getDataSourceConfig().setPassword(configManager.getDatabasePassword());
			config
				.setNamingConvention(new TablePrefixUnderscoreNamingConvention(configManager.getDatabaseTablePrefix()));
			config.loadFromProperties();
			config.setDdlGenerate(configManager.getDatabaseGenerateDDL());
			return EbeanServerFactory.create(config);
		} finally {
			currentThread.setContextClassLoader(prev);
		}
	}

	private final static class TablePrefixUnderscoreNamingConvention extends UnderscoreNamingConvention {
		private final String tablePrefix;

		public TablePrefixUnderscoreNamingConvention(String tablePrefix) {
			this.tablePrefix = tablePrefix;
		}

		@Override
		public TableName getTableNameByConvention(Class<?> beanClass) {
			return new TableName(getCatalog(), getSchema(),
				tablePrefix + "_" + toUnderscoreFromCamel(beanClass.getSimpleName()));
		}
	}

	public JarFile getJarFile() {
		return jarFile;
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}

	public TranslateManager getTranslateManager() {
		return translateManager;
	}

	public ProtocolManager getProtocolManager() {
		return protocolManager;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public HologramManager getHologramManager() {
		return hologramManager;
	}

	public TopicManager getTopicManager() {
		return topicManager;
	}

	public SNSManagerImpl getSNSManager() {
		return snsManager;
	}

	public static Main getInstance() {
		return INSTANCE;
	}
}
