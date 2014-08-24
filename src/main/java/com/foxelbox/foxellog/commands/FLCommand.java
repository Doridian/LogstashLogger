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
import com.mongodb.DBObject;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class FLCommand implements CommandExecutor {
    private final FoxelLog plugin;

    public FLCommand(FoxelLog plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        final DBObject query = new BasicDBObject().append("user_uuid", ((Player) commandSender).getUniqueId());

        final long timeStart = System.nanoTime();
        final List<BaseAction> actions = plugin.getChangeQueryInterface().queryActions(query);
        final long timeEnd = System.nanoTime();

        commandSender.sendMessage("Time taken: " + (((double)(timeEnd - timeStart)) / 1000000000D) + " seconds");

        System.out.println(actions);

        return true;
    }
}
