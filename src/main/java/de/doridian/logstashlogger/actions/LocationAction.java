package de.doridian.logstashlogger.actions;

import org.bukkit.Location;
import org.json.simple.JSONObject;

public class LocationAction extends BaseAction {
	private final Location location;

	public LocationAction(String action, Location location) {
		super(action);
		this.location = location;
	}

	@Override
	public JSONObject toJSONObject() {
		final JSONObject thisBlockChange = new JSONObject();
		thisBlockChange.put("x", location.getX());
		thisBlockChange.put("y", location.getY());
		thisBlockChange.put("z", location.getZ());
		thisBlockChange.put("world", location.getWorld().getName());
		return thisBlockChange;
	}
}