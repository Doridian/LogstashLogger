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
package com.foxelbox.foxellog.actions;

import com.foxelbox.foxellog.util.ClassUtils;
import org.elasticsearch.common.joda.time.format.DateTimeFormatter;
import org.elasticsearch.common.joda.time.format.ISODateTimeFormat;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.objenesis.ObjenesisStd;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class BaseAction {
	private final Date date;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime();

    public abstract String getType();

    protected BaseAction() {
        date = new Date();
    }

    protected BaseAction(Map<String, Object> fields) {
        date = DATE_TIME_FORMATTER.parseDateTime((String)fields.get("date")).toDate();
    }

    public XContentBuilder toJSONObject(XContentBuilder builder) throws IOException {
        builder.field("date", date);
		return builder;
	}

    static Map<String, Object> builderBasicTypeMapping(String type, String index, String format) throws IOException {
        Map<String, Object> builder = new HashMap<>();

        if(type != null)
            builder.put("type", type);
        if(index != null)
            builder.put("index", index);
        if(format != null)
            builder.put("format", format);

        return builder;
    }

    protected Map<String, Map<String, Object>> getCustomMappings() throws IOException {
        Map<String, Map<String, Object>> retMap = new HashMap<>();

        retMap.put("date", builderBasicTypeMapping("date", null, "dateTime"));

        return retMap;
    }

    private final static ObjenesisStd objenesisStd;
    private final static Map<String, Class<? extends BaseAction>> typeToClassMap = new HashMap<>();
    private final static Map<String, Constructor<? extends BaseAction>> typeToCtorMap = new HashMap<>();
    static {
        objenesisStd = new ObjenesisStd();
        for(Class<? extends BaseAction> clazz : ClassUtils.getSubClasses(BaseAction.class, BaseAction.class.getPackage().getName())) {
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                try {
                    final String classType = objenesisStd.newInstance(clazz).getType();
                    typeToClassMap.put(classType, clazz);
                    typeToCtorMap.put(classType, clazz.getDeclaredConstructor(Map.class));
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Map<String, Map<String, Object>> getCustomMappingsByType(String type) throws IOException {
        return objenesisStd.newInstance(typeToClassMap.get(type)).getCustomMappings();
    }

    public static Set<String> getTypes() {
        return typeToClassMap.keySet();
    }

    public static BaseAction craftActionByTypeAndValues(String type, Map<String, Object> fields) {
        try {
            return typeToCtorMap.get(type).newInstance(fields);
        } catch (IllegalAccessException|InvocationTargetException|InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}
