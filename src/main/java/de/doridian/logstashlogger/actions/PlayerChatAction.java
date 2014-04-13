package de.doridian.logstashlogger.actions;

import org.bukkit.entity.HumanEntity;
import org.json.simple.JSONObject;

public class PlayerChatAction extends PlayerAction {
	private final String message;

	public PlayerChatAction(HumanEntity user, String message) {
		super(user, "chat");
		this.message = message;
	}

	@Override
	public JSONObject toJSONObject() {
		final JSONObject thisBlockChange = super.toJSONObject();
		thisBlockChange.put("message", message);
		return thisBlockChange;
	}
}
