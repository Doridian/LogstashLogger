package de.doridian.logstashlogger.actions;

import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class PlayerAction extends BaseAction {
	private final Player user;

	public PlayerAction(Player user, String action) {
		super("player_" + action);
		this.user = user;
	}

	@Override
	public JSONObject toJSONObject() {
		final JSONObject thisBlockChange = super.toJSONObject();
		thisBlockChange.put("username", user.getName());
		thisBlockChange.put("useruuid", user.getUniqueId().toString());
		return thisBlockChange;
	}
}
