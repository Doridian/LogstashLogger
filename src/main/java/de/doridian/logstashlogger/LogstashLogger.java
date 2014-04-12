package de.doridian.logstashlogger;

import de.doridian.logstashlogger.redis.RedisManager;
import de.doridian.logstashlogger.redis.RedisQueueThread;
import org.bukkit.plugin.java.JavaPlugin;

public class LogstashLogger extends JavaPlugin {
	public static LogstashLogger instance;

	@SuppressWarnings("FieldCanBeLocal")
	private LoggerListener listener;
	@SuppressWarnings("FieldCanBeLocal")
	private RedisQueueThread redisQueueThread;

	@Override
	public void onEnable() {
		instance = this;
		super.onEnable();
		RedisManager.initialize();

		listener = new LoggerListener();
		redisQueueThread = new RedisQueueThread();
		getServer().getPluginManager().registerEvents(listener, this);
		redisQueueThread.start();
	}
}
