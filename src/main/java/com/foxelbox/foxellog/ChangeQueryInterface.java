package com.foxelbox.foxellog;

import com.foxelbox.foxellog.actions.BaseAction;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class ChangeQueryInterface {
    private final FoxelLog plugin;

    public ChangeQueryInterface(FoxelLog plugin) {
        this.plugin = plugin;
    }

    public Collection<BaseAction> queryActions(final QueryBuilder query) {
        return queryActions(query, true);
    }

    public Collection<BaseAction> queryActions(final QueryBuilder query, final boolean sortByDateDesc) {
        final TimeValue SCROLL_TIME = new TimeValue(60000);

        SearchResponse result = plugin.elasticsearchClient.prepareSearch(plugin.getIndexName())
                .setSearchType(SearchType.SCAN)
                .setScroll(SCROLL_TIME)
                .setQuery(query)
                .setSize(100)
                .execute()
                .actionGet();

        final ArrayList<BaseAction> ret = new ArrayList<>();

        while (true) {
            result = plugin.elasticsearchClient.prepareSearchScroll(result.getScrollId())
                    .setScroll(SCROLL_TIME)
                    .execute()
                    .actionGet();
            final SearchHits hits = result.getHits();
            for (final SearchHit hit : hits)
                ret.add(BaseAction.craftActionByTypeAndValues(hit.getType(), hit.getSource()));
            if (hits.getHits().length == 0)
                break;
        }

        if(sortByDateDesc) {
            Collections.sort(ret, new Comparator<BaseAction>() {
                @Override
                public int compare(BaseAction o1, BaseAction o2) {
                    return o1.getDate().compareTo(o2.getDate());
                }
            });
        }

        return ret;
    }
}
