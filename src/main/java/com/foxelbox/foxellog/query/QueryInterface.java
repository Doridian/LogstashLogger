package com.foxelbox.foxellog.query;

import com.foxelbox.foxellog.FoxelLog;
import com.foxelbox.foxellog.actions.BaseAction;
import com.foxelbox.foxellog.actions.PlayerBlockAction;
import com.foxelbox.foxellog.actions.PlayerInventoryAction;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.*;

public class QueryInterface {
    private final FoxelLog plugin;

    public QueryInterface(FoxelLog plugin) {
        this.plugin = plugin;
    }

    private BasicDBObject makeRange(int pos, int range) {
        return new BasicDBObject("$gte", pos - range).append("$lte", pos + range);
    }

    public class QueryException extends Exception {
        public QueryException() {
        }

        public QueryException(String message) {
            super(message);
        }

        public QueryException(String message, Throwable cause) {
            super(message, cause);
        }

        public QueryException(Throwable cause) {
            super(cause);
        }

        public QueryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

    public static final UUID CONSOLE_UUID = UUID.nameUUIDFromBytes("COMMANDSENDER:CONSOLE".getBytes());

    public static class AggregationResults {
        public final String label;
        public final Collection<AggregationResult> results;

        AggregationResults(String label, Collection<AggregationResult> results) {
            this.label = label;
            this.results = results;
        }
    }

    public static class QueryResults {
        public final int count;
        public final Collection<BaseAction> results;

        QueryResults(int count, Collection<BaseAction> results) {
            this.count = count;
            this.results = results;
        }
    }

    public AggregationResults doAggregatedQuery(QueryParams queryParams) throws QueryException {
        DBCollection collection = plugin.getMongoDB().getCollection(BaseAction.getCollection());

        if (queryParams.aggregationMode == null)
            throw new QueryException("This method is for aggregation queries only");

        if (queryParams.performMode != QueryParams.PerformMode.GET)
            throw new QueryException("You can only use the display/default mode while aggregation/sum is turned on!");

        if (queryParams.area >= 0) {
            queryParams.query.put("location",
                    new BasicDBObject("world", queryParams.setLocation.getWorld().getName())
                            .append("x", makeRange(queryParams.setLocation.getBlockX(), queryParams.area))
                            .append("y", makeRange(queryParams.setLocation.getBlockY(), queryParams.area))
                            .append("z", makeRange(queryParams.setLocation.getBlockZ(), queryParams.area))
            );
        } else if (queryParams.worldSet) {
            queryParams.query.put("location", new BasicDBObject("world", queryParams.setLocation.getWorld().getName()));
        }

        queryParams.query.put("type", "player_block_change");

        ArrayList<DBObject> aggregationPipeline = new ArrayList<>();

        aggregationPipeline.add(new BasicDBObject("$match", queryParams.query));

        BasicDBObject project = new BasicDBObject("_id", 0);
        project.put("blockFrom", 1);
        project.put("blockTo", 1);
        aggregationPipeline.add(new BasicDBObject("$project", project));
        BasicDBObject groups = new BasicDBObject();
        aggregationPipeline.add(new BasicDBObject("$group", groups));

        Collection<AggregationResult> results = null;
        String label = null;

        switch (queryParams.aggregationMode) {
            case PLAYERS:
                label = "Player";
                project.put("user_uuid", 1);

                groups.append("_id", "$user_uuid");

                groups.append("placed", new BasicDBObject("$sum", new BasicDBObject("$cond", Arrays.asList(new BasicDBObject("$eq", Arrays.asList("$blockTo", null)), 0, 1))));
                groups.append("destroyed", new BasicDBObject("$sum", new BasicDBObject("$cond", Arrays.asList(new BasicDBObject("$eq", Arrays.asList("$blockFrom", null)), 0, 1))));

                results = new ArrayList<>();
                for (DBObject res : collection.aggregate(aggregationPipeline).results()) {
                    results.add(new AggregationResult(plugin.getServer().getOfflinePlayer((UUID) res.get("_id")).getName(), (int) res.get("placed"), (int) res.get("destroyed")));
                }
                break;
            case BLOCKS:
                label = "Block";

                Map<String, AggregationResult> resultMap = new HashMap<>();

                queryParams.query.put("blockFrom", new BasicDBObject("$ne", null));
                groups.append("_id", "$blockFrom");
                groups.append("value", new BasicDBObject("$sum", 1));
                for (DBObject res : collection.aggregate(aggregationPipeline).results()) {
                    System.out.println(res.toMap());
                    String key = (String) res.get("_id");
                    AggregationResult result = resultMap.get(key);
                    if (result == null) {
                        result = new AggregationResult(key);
                        resultMap.put(key, result);
                    }
                    result.destroyed = (int) res.get("value");
                }

                queryParams.query.remove("blockFrom");
                queryParams.query.put("blockTo", new BasicDBObject("$ne", null));
                groups.put("_id", "$blockTo");
                groups.put("value", new BasicDBObject("$sum", 1));
                for (DBObject res : collection.aggregate(aggregationPipeline).results()) {
                    String key = (String) res.get("_id");
                    AggregationResult result = resultMap.get(key);
                    if (result == null) {
                        result = new AggregationResult(key);
                        resultMap.put(key, result);
                    }
                    result.placed = (int) res.get("value");
                }

                results = resultMap.values();
                break;
        }

        return new AggregationResults(label, results);

    }

    public QueryResults doNormalQuery(QueryParams queryParams) throws QueryException {
        DBCollection collection = plugin.getMongoDB().getCollection(BaseAction.getCollection());

        if (queryParams.aggregationMode != null)
            throw new QueryException("This method is for non-aggregation queries only");

        if (queryParams.area >= 0) {
            queryParams.query.put("location",
                    new BasicDBObject("world", queryParams.setLocation.getWorld().getName())
                            .append("x", makeRange(queryParams.setLocation.getBlockX(), queryParams.area))
                            .append("y", makeRange(queryParams.setLocation.getBlockY(), queryParams.area))
                            .append("z", makeRange(queryParams.setLocation.getBlockZ(), queryParams.area))
            );
        } else if (queryParams.worldSet) {
            queryParams.query.put("location", new BasicDBObject("world", queryParams.setLocation.getWorld().getName()));
        }

        switch (queryParams.performMode) {
            case GET:
                queryParams.query.put("state", 0);
                DBCursor getCursor = collection.find(queryParams.query).sort(queryParams.sort);

                List<BaseAction> getActions = new ArrayList<>();

                for(DBObject dbObject : getCursor)
                    getActions.add(BaseAction.craftActionByTypeAndDBObject(dbObject));

                return new QueryResults(getActions.size(), getActions);
            case ROLLBACK:
                queryParams.query.put("state", 0);
                DBCursor cursor = collection.find(queryParams.query).sort(new BasicDBObject("date", -1));

                List<PlayerBlockAction> blockActions = new ArrayList<>();
                List<PlayerInventoryAction> inventoryActions = new ArrayList<>();

                for(DBObject dbObject : cursor) {
                    BaseAction action = BaseAction.craftActionByTypeAndDBObject(dbObject);
                    if(action instanceof PlayerBlockAction)
                        blockActions.add((PlayerBlockAction)action);
                    else if(action instanceof PlayerInventoryAction)
                        inventoryActions.add((PlayerInventoryAction)action);
                }

                Map<Location, Material> setMaterials = new HashMap<>();

                for(PlayerBlockAction action : blockActions) {
                    Material currentMaterial;
                    Location currentLocation = action.getLocation();
                    if (!setMaterials.containsKey(currentLocation))
                        setMaterials.put(currentLocation, currentLocation.getBlock().getType());
                    currentMaterial = setMaterials.get(currentLocation);

                    if (currentMaterial.equals(action.getBlockTo())) {
                        action.state = 1;
                        collection.update(new BasicDBObject("_id", action.getDbID()), action.toDBObject());
                        setMaterials.put(currentLocation, action.getBlockFrom());
                    }
                }

                int count = 0;

                for(Map.Entry<Location, Material> setMaterial : setMaterials.entrySet()) {
                    setMaterial.getKey().getBlock().setType(setMaterial.getValue());
                    count++;
                }

                return new QueryResults(count, null);
            case REDO:
                queryParams.query.put("state", 1);
                DBCursor cursor2 = collection.find(queryParams.query).sort(new BasicDBObject("date", 1));

                List<PlayerBlockAction> blockActions2 = new ArrayList<>();
                List<PlayerInventoryAction> inventoryActions2 = new ArrayList<>();

                for(DBObject dbObject : cursor2) {
                    BaseAction action = BaseAction.craftActionByTypeAndDBObject(dbObject);
                    if(action instanceof PlayerBlockAction)
                        blockActions2.add((PlayerBlockAction)action);
                    else if(action instanceof PlayerInventoryAction)
                        inventoryActions2.add((PlayerInventoryAction)action);
                }

                Map<Location, Material> setMaterials2 = new HashMap<>();

                for(PlayerBlockAction action : blockActions2) {
                    Material currentMaterial;
                    Location currentLocation = action.getLocation();
                    if (!setMaterials2.containsKey(currentLocation))
                        setMaterials2.put(currentLocation, currentLocation.getBlock().getType());
                    currentMaterial = setMaterials2.get(currentLocation);

                    if (currentMaterial.equals(action.getBlockFrom())) {
                        action.state = 0;
                        collection.update(new BasicDBObject("_id", action.getDbID()), action.toDBObject());
                        setMaterials2.put(currentLocation, action.getBlockTo());
                    }
                }

                int count2 = 0;

                for(Map.Entry<Location, Material> setMaterial : setMaterials2.entrySet()) {
                    setMaterial.getKey().getBlock().setType(setMaterial.getValue());
                    count2++;
                }

                return new QueryResults(count2, null);
        }
        return null;
    }
}
