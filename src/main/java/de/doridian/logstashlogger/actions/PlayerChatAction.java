package de.doridian.logstashlogger.actions;

import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class PlayerChatAction extends PlayerAction {
	private final String message;

	public PlayerChatAction(Player user, String message) {
		super(user, "chat");
		this.message = message;
	}

	@Override
	public JSONObject toJSONObject() {
		final JSONObject thisBlockChange = new JSONObject();
		thisBlockChange.put("message", message);
		return thisBlockChange;
	}
}
