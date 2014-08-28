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
import com.foxelbox.foxellog.query.AggregationResult;
import com.foxelbox.foxellog.query.QueryInterface;
import com.foxelbox.foxellog.query.QueryParams;
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

    private BasicDBObject makeRange(int pos, int range) {
        return new BasicDBObject("$gte", pos - range).append("$lte", pos + range);
    }

    private final HashMap<UUID, QueryParams> lastQueryParams = new HashMap<>();

    public static final UUID CONSOLE_UUID = UUID.nameUUIDFromBytes("COMMANDSENDER:CONSOLE".getBytes());

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandName, String[] argsRaw) {
        QueryParams queryParams = new QueryParams();
        final UUID myUUID;
        if(commandSender instanceof Player)
            myUUID = ((Player)commandSender).getUniqueId();
        else
            myUUID = CONSOLE_UUID;

        for(String arg : argsRaw)
            if(arg.equalsIgnoreCase("last"))
                queryParams = lastQueryParams.get(myUUID);

        queryParams.aggregationMode = null;
        queryParams.performMode = QueryParams.PerformMode.GET;

        lastQueryParams.put(myUUID, queryParams);

        if(queryParams.setLocation == null)
            queryParams.setLocation = (commandSender instanceof Player) ? ((Player)commandSender).getLocation() : new Location(plugin.getServer().getWorlds().get(0), 0, 0, 0);

        for(int i = 0; i < argsRaw.length; i += 2) {
            String arg = argsRaw[i];
            String param = (i < argsRaw.length - 1) ? argsRaw[i + 1] : "";

            switch(arg.toLowerCase()) {
                case "self":
                case "me":
                case "myself":
                    param = "me";
                    i--;
                case "player":
                    final Set<UUID> playersToMatch = new HashSet<>();
                    for(final String ply : param.split(",")) {
                        if (ply.equals("self") || ply.equals("myself") || ply.equals("me"))
                            playersToMatch.add(myUUID);
                        else
                            playersToMatch.add(plugin.getServer().getPlayer(param).getUniqueId());
                    }

                    final int size = playersToMatch.size();
                    if(size == 1)
                        queryParams.query.put("user_uuid", playersToMatch.iterator().next());
                    else if(size > 1)
                        queryParams.query.put("user_uuid", new BasicDBObject("$in", playersToMatch.toArray(new UUID[size])));
                    break;
                case "world":
                    queryParams.worldSet = true;
                    queryParams.setLocation.setWorld(plugin.getServer().getWorld(param));
                    break;
                case "loc":
                case "location":
                    String[] locs = param.split("[,;]+");
                    if(locs.length == 2) {
                        queryParams.setLocation.setX(Integer.parseInt(locs[0]));
                        queryParams.setLocation.setZ(Integer.parseInt(locs[1]));
                    } else if(locs.length == 3) {
                        queryParams.setLocation.setX(Integer.parseInt(locs[0]));
                        queryParams.setLocation.setY(Integer.parseInt(locs[1]));
                        queryParams.setLocation.setZ(Integer.parseInt(locs[2]));
                    }
                    break;
                case "area":
                    queryParams.area = Integer.parseInt(param);
                    break;
                case "since":
                    //All newer than X time
                    break;
                case "before":
                    //All older than X time
                    break;
                case "last":
                    i--; //Ignore!
                    break;
                case "rollback":
                    queryParams.performMode = QueryParams.PerformMode.ROLLBACK;
                    i--;
                    break;
                case "redo":
                    queryParams.performMode = QueryParams.PerformMode.REDO;
                    i--;
                    break;
                case "sum":
                    switch(param.toLowerCase()) {
                        case "player":
                        case "players":
                            queryParams.aggregationMode = QueryParams.AggregationMode.PLAYERS;
                            break;
                        case "block":
                        case "blocks":
                            queryParams.aggregationMode = QueryParams.AggregationMode.BLOCKS;
                            break;
                    }
                    break;
            }
        }

        try {
            if (queryParams.aggregationMode == null) {
                plugin.getQueryInterface().doNormalQuery(queryParams);
            } else {
                plugin.getQueryInterface().doAggregatedQuery(queryParams);
            }
        } catch (QueryInterface.QueryException e) {
            commandSender.sendMessage(e.getMessage());
        }

        return true;
    }
}
