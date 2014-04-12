package de.doridian.logstashlogger.actions;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class PlayerBlockAction extends PlayerAndLocationAction {
	private final Material material;

	public PlayerBlockAction(Player user, String action, Block block) {
		super(user, "block_" + action, block.getLocation());
		this.material = block.getType();
	}

	public JSONObject toJSONObject() {
		final JSONObject thisBlockChange = super.toJSONObject();
		thisBlockChange.put("block", material.name());
		return thisBlockChange;
	}
}
