package com.foxelbox.foxellog.commands;

import com.foxelbox.foxellog.FoxelLog;
import com.foxelbox.foxellog.actions.BaseAction;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.List;

public class FLCommand implements CommandExecutor {
    private final FoxelLog plugin;

    public FLCommand(FoxelLog plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        final QueryBuilder queryBuilder = QueryBuilders.termQuery("user_uuid", ((Player) commandSender).getUniqueId().toString());

        final long timeStart = System.nanoTime();
        final List<BaseAction> actions = plugin.getChangeQueryInterface().queryActions(queryBuilder);
        final long timeEnd = System.nanoTime();

        commandSender.sendMessage("Time taken: " + (((double)(timeEnd - timeStart)) / 1000000000D) + " seconds");

        System.out.println(actions);

        return true;
    }
}
