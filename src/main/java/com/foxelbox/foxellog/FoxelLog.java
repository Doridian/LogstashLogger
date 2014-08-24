/**
 * This file is part of FoxelLog.
 *
 * FoxelLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoxelLog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FoxelLog.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.foxelbox.foxellog;

import com.foxelbox.dependencies.config.Configuration;
import com.foxelbox.dependencies.redis.RedisManager;
import com.foxelbox.dependencies.threading.SimpleThreadCreator;
import com.foxelbox.foxellog.redis.RedisQueueThread;
import org.bukkit.plugin.java.JavaPlugin;

public class FoxelLog extends JavaPlugin {
	public static FoxelLog instance;

	@SuppressWarnings("FieldCanBeLocal")
	private LoggerListener listener;
	@SuppressWarnings("FieldCanBeLocal")
	private RedisQueueThread redisQueueThread;

    public Configuration configuration;

    public RedisManager redisManager;

	@Override
	public void onEnable() {
		instance = this;
		super.onEnable();
        configuration = new Configuration(getDataFolder());
        redisManager = new RedisManager(new SimpleThreadCreator(), configuration);

		listener = new LoggerListener();
		redisQueueThread = new RedisQueueThread();
		getServer().getPluginManager().registerEvents(listener, this);
		redisQueueThread.start();
	}
}
