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
import com.foxelbox.foxellog.actions.BaseAction;
import com.foxelbox.foxellog.commands.FLCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class FoxelLog extends JavaPlugin {
	public static FoxelLog instance;

	@SuppressWarnings("FieldCanBeLocal")
	private LoggerListener listener;

    public Configuration configuration;

    Client elasticsearchClient;
    private Node elasticsearchNode;

    private ChangeQueryInterface changeQueryInterface;

    private String INDEX_NAME;
    public String getIndexName() {
        return INDEX_NAME;
    }

	@Override
	public void onEnable() {
		instance = this;
		super.onEnable();
        configuration = new Configuration(getDataFolder());

        INDEX_NAME = "foxellog_" + configuration.getValue("server-name", "N/A").toLowerCase();

        ImmutableSettings.Builder settings;
        try {
            settings = ImmutableSettings.settingsBuilder()
                    .classLoader(this.getClassLoader())
                    .loadFromUrl(new File(getDataFolder(), "elasticsearch.yml").toURI().toURL());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        elasticsearchNode = NodeBuilder.nodeBuilder().settings(settings).client(true).node();
        elasticsearchClient = elasticsearchNode.client();

        listener = new LoggerListener(this);
		getServer().getPluginManager().registerEvents(listener, this);

        getServer().getPluginCommand("fl").setExecutor(new FLCommand(this));

        changeQueryInterface = new ChangeQueryInterface(this);

        try {
            registerIndices();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
	}

    private void registerIndices() throws IOException {
        elasticsearchClient.admin().indices().prepareCreate(getIndexName()).execute().actionGet();

        for(String actionType : BaseAction.getTypes()) {
            Map<String, Map<String, Object>> fieldMappings = BaseAction.getCustomMappingsByType(actionType);
            if(fieldMappings == null)
                continue;

            elasticsearchClient.admin().indices()
                    .preparePutMapping(getIndexName())
                    .setType(actionType)
                    .setSource(
                            XContentFactory.jsonBuilder()
                                    .startObject()
                                    .field("properties", fieldMappings)
                                    .endObject()
                    )
                    .execute()
                    .actionGet();
        }
    }

    public ChangeQueryInterface getChangeQueryInterface() {
        return changeQueryInterface;
    }

    @Override
    public void onDisable() {
        elasticsearchNode.close();
        elasticsearchClient.close();
    }
}
