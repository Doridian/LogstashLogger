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
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
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

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandName, String[] args) {
        DBCollection collection = plugin.getMongoDB().getCollection(BaseAction.getCollection());
        BasicDBObject query = new BasicDBObject();
        BasicDBObject sort = new BasicDBObject();

        ArrayList<DBObject> aggregationPipeline = null;
        AggregationMode aggregationMode = null;

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

        if(aggregationMode != null) {
            aggregationPipeline = new ArrayList<>();

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

            switch(aggregationMode) {
                case PLAYERS:
                    project.put("user_uuid", 1);

                    groups.append("_id", "$user_uuid");

                    groups.append("placed", new BasicDBObject("$sum", new BasicDBObject("$cond", Arrays.asList(new BasicDBObject("$eq", Arrays.asList("$blockTo", null)), 0, 1))));
                    groups.append("destroyed", new BasicDBObject("$sum", new BasicDBObject("$cond", Arrays.asList(new BasicDBObject("$eq", Arrays.asList("$blockFrom", null)), 0, 1))));

                    results = new ArrayList<>();
                    for(DBObject res : collection.aggregate(aggregationPipeline).results()) {
                        results.add(new AggregationResult(plugin.getServer().getPlayer((UUID)res.get("_id")).getName(), (int)res.get("placed"), (int)res.get("destroyed")));
                    }
                    break;
                case BLOCKS:
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
            DBCursor cursor = collection.find(query).sort(sort);

            final long timeStart = System.nanoTime();
            //final List<BaseAction> actions = plugin.getChangeQueryInterface().queryActions(query);
            final long timeEnd = System.nanoTime();

            commandSender.sendMessage("Time taken: " + (((double) (timeEnd - timeStart)) / 1000000000D) + " seconds");

            //System.out.println(actions);
        }

        return true;
    }
}
