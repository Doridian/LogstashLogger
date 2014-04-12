package de.doridian.logstashlogger.actions;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class BlockChangeAction extends LocationAction {
	private final Material material;

	public BlockChangeAction(Player user, String action, Block block) {
		super(user, action, block.getLocation());
		this.material = block.getType();
	}

	public JSONObject toJSONObject() {
		final JSONObject thisBlockChange = super.toJSONObject();
		thisBlockChange.put("block", material.name());
		return thisBlockChange;
	}
}
