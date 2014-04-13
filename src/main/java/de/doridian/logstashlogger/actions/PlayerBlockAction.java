package de.doridian.logstashlogger.actions;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class PlayerBlockAction extends PlayerAndLocationAction {
	private final Material materialBefore;
	private final Material materialAfter;

	public PlayerBlockAction(Player user, Location location, Material materialBefore, Material materialAfter) {
		super(user, "block_change", location);
		this.materialBefore = materialBefore;
		this.materialAfter = materialAfter;
	}

	public JSONObject toJSONObject() {
		final JSONObject thisBlockChange = super.toJSONObject();
		thisBlockChange.put("block_from", materialBefore.name());
		thisBlockChange.put("block_to", materialAfter.name());
		return thisBlockChange;
	}
}
