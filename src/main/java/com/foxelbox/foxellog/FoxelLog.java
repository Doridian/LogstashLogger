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
import org.bukkit.plugin.java.JavaPlugin;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import java.io.File;

public class FoxelLog extends JavaPlugin {
	public static FoxelLog instance;

	@SuppressWarnings("FieldCanBeLocal")
	private LoggerListener listener;

    public Configuration configuration;

    public Client elasticsearchClient;
    private Node elasticsearchNode;

	@Override
	public void onEnable() {
		instance = this;
		super.onEnable();
        configuration = new Configuration(getDataFolder());

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

        listener = new LoggerListener();
		getServer().getPluginManager().registerEvents(listener, this);
	}

    @Override
    public void onDisable() {
        elasticsearchNode.close();
    }
}
