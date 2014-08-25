/**
 * This file is part of FoxelLog.
 *
 * FoxelLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoxelLog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FoxelLog.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.foxelbox.foxellog.commands;

import com.foxelbox.foxellog.FoxelLog;
import com.foxelbox.foxellog.actions.BaseAction;
import com.foxelbox.foxellog.actions.PlayerBlockAction;
import com.foxelbox.foxellog.actions.PlayerInventoryAction;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class FLCommand implements CommandExecutor {
    private final FoxelLog plugin;

    public FLCommand(FoxelLog plugin) {
        this.plugin = plugin;
    }

    private class AggregationResult {
        public final String label;
        public int placed = 0;
        public int destroyed = 0;

        private AggregationResult(String label) {
            this(label, 0, 0);
        }

        private AggregationResult(String label, int placed, int destroyed) {
            this.label = label;
            this.placed = placed;
            this.destroyed = destroyed;
        }

        @Override
        public String toString() {
            return "{" + label + "+" + placed + "-" + destroyed + "}";
        }
    }

    enum AggregationMode {
        PLAYERS,
        BLOCKS
    }

    enum PerformMode {
        ROLLBACK,
        REDO,
        GET
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandName, String[] args) {
        DBCollection collection = plugin.getMongoDB().getCollection(BaseAction.getCollection());
        BasicDBObject query = new BasicDBObject();
        BasicDBObject sort = new BasicDBObject("date", -1);

        AggregationMode aggregationMode = null;
        PerformMode performMode = PerformMode.GET;

        for(int i = 0; i < args.length; i += 2) {
            final String arg = args[i];

            final String param;
            if(i < args.length - 1)
                param = args[i + 1];
            else
                param = "";

            switch(arg.toLowerCase()) {
                case "self":
                    query = query.append("user_uuid", ((Player)commandSender).getUniqueId());
                    i--;
                    break;
                case "rollback":
                    performMode = PerformMode.ROLLBACK;
                    i--;
                    break;
                case "redo":
                    performMode = PerformMode.REDO;
                    i--;
                    break;
                case "player":
                    query = query.append("user_uuid", plugin.getServer().getPlayer(param).getUniqueId());
                    break;
                case "sum":
                    query = query.append("type", "player_block_change");
                    switch(param.toLowerCase()) {
                        case "player":
                        case "players":
                            aggregationMode = AggregationMode.PLAYERS;
                            break;
                        case "block":
                        case "blocks":
                            aggregationMode = AggregationMode.BLOCKS;
                            break;
                    }
                    break;
            }
        }

        if(performMode != PerformMode.GET && aggregationMode != null) {
            commandSender.sendMessage("You can only use the display/default mode while aggregation/sum is turned on!");
            return false;
        }

        final long timeStart = System.nanoTime();

        if(aggregationMode != null) {
            ArrayList<DBObject> aggregationPipeline = new ArrayList<>();

            aggregationPipeline.add(new BasicDBObject("$match", query));

            BasicDBObject project = new BasicDBObject("_id", 0);
            project.put("blockFrom", 1);
            project.put("blockTo", 1);
            //exception: The top-level _id field is the only field currently supported for exclusion
            /*project.put("location", 0);
            project.put("date", 0);
            project.put("type", 0);*/
            aggregationPipeline.add(new BasicDBObject("$project", project));
            BasicDBObject groups = new BasicDBObject();
            aggregationPipeline.add(new BasicDBObject("$group", groups));

            Collection<AggregationResult> results = null;
            String label = null;

            switch(aggregationMode) {
                case PLAYERS:
                    label = "Player";

                    project.put("user_uuid", 1);

                    groups.append("_id", "$user_uuid");

                    groups.append("placed", new BasicDBObject("$sum", new BasicDBObject("$cond", Arrays.asList(new BasicDBObject("$eq", Arrays.asList("$blockTo", null)), 0, 1))));
                    groups.append("destroyed", new BasicDBObject("$sum", new BasicDBObject("$cond", Arrays.asList(new BasicDBObject("$eq", Arrays.asList("$blockFrom", null)), 0, 1))));

                    results = new ArrayList<>();
                    for(DBObject res : collection.aggregate(aggregationPipeline).results()) {
                        results.add(new AggregationResult(plugin.getServer().getOfflinePlayer((UUID)res.get("_id")).getName(), (int)res.get("placed"), (int)res.get("destroyed")));
                    }
                    break;
                case BLOCKS:
                    label = "Block";

                    //project.put("user_uuid", 0);

                    Map<String, AggregationResult> resultMap = new HashMap<>();

                    query.append("blockFrom", new BasicDBObject("$ne", null));
                    groups.append("_id", "$blockFrom");
                    groups.append("value", new BasicDBObject("$sum", 1));
                    for(DBObject res : collection.aggregate(aggregationPipeline).results()) {
                        System.out.println(res.toMap());
                        String key = (String)res.get("_id");
                        AggregationResult result = resultMap.get(key);
                        if(result == null) {
                            result = new AggregationResult(key);
                            resultMap.put(key, result);
                        }
                        result.destroyed = (int)res.get("value");
                     }

                    query.remove("blockFrom");
                    query.append("blockTo", new BasicDBObject("$ne", null));
                    groups.put("_id", "$blockTo");
                    groups.put("value", new BasicDBObject("$sum", 1));
                    for(DBObject res : collection.aggregate(aggregationPipeline).results()) {
                        String key = (String)res.get("_id");
                        AggregationResult result = resultMap.get(key);
                        if(result == null) {
                            result = new AggregationResult(key);
                            resultMap.put(key, result);
                        }
                        result.placed = (int)res.get("value");
                    }

                    results = resultMap.values();
                    break;
            }

            System.out.println(results);
        } else {
            switch (performMode) {
                case GET:
                    DBCursor getCursor = collection.find(query.append("state", 0)).sort(sort);

                    List<BaseAction> getActions = new ArrayList<>();

                    for(DBObject dbObject : getCursor)
                        getActions.add(BaseAction.craftActionByTypeAndDBObject(dbObject));

                    System.out.println(getActions);
                    break;
                case ROLLBACK:
                    query = query.append("state", 0);
                    DBCursor cursor = collection.find(query).sort(new BasicDBObject("date", -1));
                    //collection.updateMulti(query, new BasicDBObject("state", 1));

                    List<PlayerBlockAction> blockActions = new ArrayList<>();
                    List<PlayerInventoryAction> inventoryActions = new ArrayList<>();

                    for(DBObject dbObject : cursor) {
                        BaseAction action = BaseAction.craftActionByTypeAndDBObject(dbObject);
                        if(action instanceof PlayerBlockAction)
                            blockActions.add((PlayerBlockAction)action);
                        else if(action instanceof PlayerInventoryAction)
                            inventoryActions.add((PlayerInventoryAction)action);
                    }

                    Map<Location, Material> setMaterials = new HashMap<>();

                    for(PlayerBlockAction action : blockActions) {
                        Material currentMaterial;
                        Location currentLocation = action.getLocation();
                        if (!setMaterials.containsKey(currentLocation))
                            setMaterials.put(currentLocation, currentLocation.getBlock().getType());
                        currentMaterial = setMaterials.get(currentLocation);

                        if (currentMaterial.equals(action.getBlockTo())) {
                            action.state = 1;
                            collection.update(new BasicDBObject("_id", action.getDbID()), action.toDBObject());
                            setMaterials.put(currentLocation, action.getBlockFrom());
                        }
                    }

                    for(Map.Entry<Location, Material> setMaterial : setMaterials.entrySet()) {
                        setMaterial.getKey().getBlock().setType(setMaterial.getValue());
                    }
                    break;
                case REDO:
                    query = query.append("state", 1);
                    DBCursor cursor2 = collection.find(query).sort(new BasicDBObject("date", 1));
                    //collection.updateMulti(query, new BasicDBObject("state", 0));

                    List<PlayerBlockAction> blockActions2 = new ArrayList<>();
                    List<PlayerInventoryAction> inventoryActions2 = new ArrayList<>();

                    for(DBObject dbObject : cursor2) {
                        BaseAction action = BaseAction.craftActionByTypeAndDBObject(dbObject);
                        if(action instanceof PlayerBlockAction)
                            blockActions2.add((PlayerBlockAction)action);
                        else if(action instanceof PlayerInventoryAction)
                            inventoryActions2.add((PlayerInventoryAction)action);
                    }

                    Map<Location, Material> setMaterials2 = new HashMap<>();

                    for(PlayerBlockAction action : blockActions2) {
                        Material currentMaterial;
                        Location currentLocation = action.getLocation();
                        if (!setMaterials2.containsKey(currentLocation))
                            setMaterials2.put(currentLocation, currentLocation.getBlock().getType());
                        currentMaterial = setMaterials2.get(currentLocation);

                        if (currentMaterial.equals(action.getBlockFrom())) {
                            action.state = 0;
                            collection.update(new BasicDBObject("_id", action.getDbID()), action.toDBObject());
                            setMaterials2.put(currentLocation, action.getBlockTo());
                        }
                    }

                    for(Map.Entry<Location, Material> setMaterial : setMaterials2.entrySet()) {
                        setMaterial.getKey().getBlock().setType(setMaterial.getValue());
                    }
                    break;
            }
        }

        final long timeEnd = System.nanoTime();

        commandSender.sendMessage("Time taken: " + (((double) (timeEnd - timeStart)) / 1000000000D) + " seconds");

        return true;
    }
}
