package com.foxelbox.foxellog;

import com.foxelbox.foxellog.actions.BaseAction;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.util.ArrayList;
import java.util.Collection;

public class ChangeQueryInterface {
    private final FoxelLog plugin;

    public ChangeQueryInterface(FoxelLog plugin) {
        this.plugin = plugin;
    }

    public Collection<BaseAction> queryActions(QueryBuilder query) {
        SearchResponse result = plugin.elasticsearchClient.prepareSearch(plugin.getIndexName())
                .setQuery(query)
                .setScroll(new TimeValue(60000))
                .setSize(100)
                .execute()
                .actionGet();

        ArrayList<BaseAction> ret = new ArrayList<>();

        while (true) {
            result = plugin.elasticsearchClient.prepareSearchScroll(result.getScrollId()).setScroll(new TimeValue(600000)).execute().actionGet();
            SearchHits hits = result.getHits();
            for (SearchHit hit : hits)
                ret.add(BaseAction.craftActionByTypeAndValues(hit.getType(), hit.getFields()));
            if (hits.getHits().length == 0)
                break;
        }

        return ret;
    }
}
