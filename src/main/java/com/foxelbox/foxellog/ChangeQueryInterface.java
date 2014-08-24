package com.foxelbox.foxellog;

import com.foxelbox.foxellog.actions.BaseAction;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;

import java.util.Collection;

public class ChangeQueryInterface {
    private final FoxelLog plugin;

    public ChangeQueryInterface(FoxelLog plugin) {
        this.plugin = plugin;
    }

    public Collection<BaseAction> queryActions(QueryBuilder query) {
        SearchResponse result = plugin.elasticsearchClient.prepareSearch(plugin.getIndexName())
                .setQuery(query)
                .execute()
                .actionGet();
        //return result;
        return null;
    }
}
