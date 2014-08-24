package com.foxelbox.foxellog.commands;

import com.foxelbox.foxellog.FoxelLog;
import com.foxelbox.foxellog.actions.BaseAction;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.Collection;

public class FLCommand implements CommandExecutor {
    private final FoxelLog plugin;

    public FLCommand(FoxelLog plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Collection<BaseAction> actions = plugin.getChangeQueryInterface().queryActions(
                QueryBuilders.termQuery("user_uuid", ((Player)commandSender).getUniqueId().toString())
        );

        System.out.println(actions);

        return true;
    }
}
