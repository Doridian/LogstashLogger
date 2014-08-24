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
package com.foxelbox.foxellog;

import com.foxelbox.dependencies.config.Configuration;
import com.foxelbox.foxellog.commands.FLCommand;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class FoxelLog extends JavaPlugin {
	public static FoxelLog instance;

	private LoggerListener listener;

    public Configuration configuration;

    private MongoClient mongoClient;
    private DB mongoDB;

    private ChangeQueryInterface changeQueryInterface;

    public DB getMongoDB() {
        return mongoDB;
    }

	@Override
	public void onEnable() {
		instance = this;
		super.onEnable();
        configuration = new Configuration(getDataFolder());

        try {
            mongoClient = new MongoClient(configuration.getValue("mongodb-host", "localhost"), Integer.parseInt(configuration.getValue("mongodb-port", "27017")));
            mongoDB = mongoClient.getDB(configuration.getValue("mongodb-db", "foxellog_unnamed"));
            String authUser = configuration.getValue("mongodb-auth-user", "");
            String authPassword = configuration.getValue("mongodb-auth-password", "");
            if(authUser != null && !authUser.isEmpty() && authPassword != null && !authPassword.isEmpty())
                mongoDB.authenticate(authUser, authPassword.toCharArray());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        listener = new LoggerListener(this);
		getServer().getPluginManager().registerEvents(listener, this);

        getServer().getPluginCommand("fl").setExecutor(new FLCommand(this));

        changeQueryInterface = new ChangeQueryInterface(this);

        listener.enable();
	}

    public ChangeQueryInterface getChangeQueryInterface() {
        return changeQueryInterface;
    }

    @Override
    public void onDisable() {
        listener.disable();
        mongoClient.close();
    }
}
