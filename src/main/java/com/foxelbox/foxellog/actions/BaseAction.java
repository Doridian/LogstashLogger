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
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.SearchHitField;
import org.objenesis.ObjenesisStd;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseAction {
	private final Date timestamp;

    public abstract String getType();

    protected BaseAction() {
        timestamp = new Date();
    }

    protected BaseAction(Map<String, SearchHitField> fields) {
        timestamp = fields.get("date").value();
    }

    public XContentBuilder toJSONObject(XContentBuilder builder) throws IOException {
        builder.field("date", timestamp);
		return builder;
	}

    private final static ObjenesisStd objenesisStd;
    private final static Map<String, Constructor<? extends BaseAction>> typeToClassMap = new HashMap<>();
    static {
        objenesisStd = new ObjenesisStd();
        for(Class<? extends BaseAction> clazz : ClassUtils.getSubClasses(BaseAction.class, BaseAction.class.getPackage().getName())) {
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                try {
                    typeToClassMap.put(objenesisStd.newInstance(clazz).getType(), clazz.getConstructor(Map.class));
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static BaseAction craftActionByTypeAndValues(String type, Map<String, SearchHitField> fields) {
        try {
            return typeToClassMap.get(type).newInstance(fields);
        } catch (IllegalAccessException|InvocationTargetException|InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}
