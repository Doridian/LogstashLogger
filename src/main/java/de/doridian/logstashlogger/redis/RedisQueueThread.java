package de.doridian.logstashlogger.redis;

import de.doridian.dependencies.redis.RedisManager;
import de.doridian.logstashlogger.actions.BaseAction;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RedisQueueThread extends Thread {
	private static final Queue<BaseAction> actionsQueue = new ConcurrentLinkedQueue<>();

	public static void queueAction(BaseAction baseAction) {
		actionsQueue.add(baseAction);
	}

	@Override
	public void run() {
		while(true) {
			if (!actionsQueue.isEmpty()) {
				try {
					while (!actionsQueue.isEmpty()) {
						BaseAction actionObject = actionsQueue.poll();
						if (actionObject == null)
							continue;
                        RedisManager.lpush("logstash", actionObject.toJSONObject().toJSONString());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) { }
		}
	}
}
