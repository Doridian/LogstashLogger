/**
 * This file is part of LogstashLogger.
 *
 * LogstashLogger is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LogstashLogger is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LogstashLogger.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.doridian.logstashlogger.redis;

import de.doridian.dependencies.redis.RedisManager;
import de.doridian.logstashlogger.LogstashLogger;
import de.doridian.logstashlogger.actions.BaseAction;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RedisQueueThread extends Thread {
    public RedisQueueThread() {
        setName("LogstashLogger-RedisQueueThread");
        setDaemon(true);
    }

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
                        LogstashLogger.instance.redisManager.lpush("logstash", actionObject.toJSONObject().toJSONString());
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
