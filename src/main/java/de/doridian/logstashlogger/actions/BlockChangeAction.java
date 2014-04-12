package de.doridian.logstashlogger.actions;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class BlockChangeAction extends LocationJSONAction {
	private final Material material;

	public BlockChangeAction(Player user, String action, Location location, Material material) {
		super(user, action, location);
		this.material = material;
	}

	public JSONObject toJSONObject() {
		final JSONObject thisBlockChange = super.toJSONObject();
		thisBlockChange.put("block", material.name());
		return thisBlockChange;
	}
}
