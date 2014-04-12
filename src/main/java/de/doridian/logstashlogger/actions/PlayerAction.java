package de.doridian.logstashlogger.actions;

import org.bukkit.entity.Player;

public class PlayerAction extends BaseAction {
	public PlayerAction(Player user, String action) {
		super(user, action);
	}
}
