package de.doridian.logstashlogger.actions;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public abstract class PlayerAndLocationAction extends BaseAction {
	private final Location location;
	private final Player user;

	public PlayerAndLocationAction(Player user, String action, Location location) {
		super(action);
		this.user = user;
		this.location = location;
	}

	@Override
	public JSONObject toJSONObject() {
		final JSONObject thisBlockChange = new JSONObject();

		thisBlockChange.put("username", user.getName());
		thisBlockChange.put("useruuid", user.getUniqueId().toString());

		thisBlockChange.put("x", location.getX());
		thisBlockChange.put("y", location.getY());
		thisBlockChange.put("z", location.getZ());
		thisBlockChange.put("world", location.getWorld().getName());

		return thisBlockChange;
	}
}
