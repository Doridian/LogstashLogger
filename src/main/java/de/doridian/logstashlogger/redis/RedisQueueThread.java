package de.doridian.logstashlogger.redis;

import de.doridian.logstashlogger.actions.BaseJSONAction;
import redis.clients.jedis.Jedis;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RedisQueueThread extends Thread {
	private static final Queue<BaseJSONAction> actionsQueue = new ConcurrentLinkedQueue<>();

	public static void queueAction(BaseJSONAction baseJSONAction) {
		actionsQueue.add(baseJSONAction);
	}

	@Override
	public void run() {
		while(true) {
			if (!actionsQueue.isEmpty()) {
				final Jedis jedis = RedisManager.jedisPool.getResource();
				try {
					while (!actionsQueue.isEmpty()) {
						BaseJSONAction actionObject = actionsQueue.poll();
						if (actionObject == null)
							continue;
						jedis.lpush("logstash", actionObject.toJSONObject().toJSONString());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				RedisManager.jedisPool.returnResource(jedis);
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) { }
		}
	}
}
