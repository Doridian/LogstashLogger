package de.doridian.logstashlogger;

import de.doridian.dependencies.config.Configuration;
import de.doridian.dependencies.redis.RedisManager;
import de.doridian.logstashlogger.redis.RedisQueueThread;
import org.bukkit.plugin.java.JavaPlugin;

public class LogstashLogger extends JavaPlugin {
	public static LogstashLogger instance;

	@SuppressWarnings("FieldCanBeLocal")
	private LoggerListener listener;
	@SuppressWarnings("FieldCanBeLocal")
	private RedisQueueThread redisQueueThread;

    public Configuration configuration;

	@Override
	public void onEnable() {
		instance = this;
		super.onEnable();
        configuration = new Configuration(getDataFolder());
		RedisManager.initialize(configuration);

		listener = new LoggerListener();
		redisQueueThread = new RedisQueueThread();
		getServer().getPluginManager().registerEvents(listener, this);
		redisQueueThread.start();
	}
}
