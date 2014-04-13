package de.doridian.logstashlogger.actions;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.json.simple.JSONObject;

public class PlayerInventoryAction extends PlayerAndLocationAction {
	private final Material material;
	private final Material container;
	private final int amount;

	public PlayerInventoryAction(HumanEntity user, Location location, Material container, Material material, int amount) {
		super(user, "inventory", location);
		this.material = material;
		this.container = container;
		this.amount = amount;
	}

	@Override
	public JSONObject toJSONObject() {
		final JSONObject thisBlockChange = super.toJSONObject();
		thisBlockChange.put("block", material.name());
		thisBlockChange.put("container", container.name());
		thisBlockChange.put("amount", amount);
		return thisBlockChange;
	}
}
