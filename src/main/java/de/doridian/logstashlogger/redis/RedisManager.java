package de.doridian.logstashlogger.redis;

import de.doridian.logstashlogger.config.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisManager {
	public static JedisPool jedisPool;

	private static String REDIS_PASSWORD;
	private static int REDIS_DB;

	public static void initialize() {
		REDIS_PASSWORD = Configuration.getValue("redis-pw", "");
		REDIS_DB = Integer.parseInt(Configuration.getValue("redis-db", "1"));
		jedisPool = createPool(Configuration.getValue("redis-host", ""));
	}

	private static JedisPool createPool(String host) {
		return new JedisPool(new JedisPoolConfig(), host, 6379, 20000, REDIS_PASSWORD, REDIS_DB);
	}
}
