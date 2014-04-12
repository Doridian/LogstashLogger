package de.doridian.logstashlogger;

import org.bukkit.plugin.java.JavaPlugin;

public class LogstashLoggerPlugin extends JavaPlugin {
	public static LogstashLoggerPlugin instance;

	@Override
	public void onEnable() {
		instance = this;
		super.onEnable();
		RedisManager.initialize();

		LogstashLoggerListener listener = new LogstashLoggerListener();
		getServer().getPluginManager().registerEvents(listener, this);
		listener.start();
	}
}
