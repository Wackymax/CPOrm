package za.co.cporm.model.map.types;

import android.content.ContentValues;
import android.database.Cursor;
import za.co.cporm.model.map.SqlColumnMapping;

import java.lang.reflect.Type;

/**
 * Created by hennie.brink on 2015-03-19.
 */
public class StringType implements SqlColumnMapping {
    @Override
    public Type getJavaType() {
        return String.class;
    }

    @Override
    public String getSqlColumnTypeName() {
        return "TEXT";
    }

    @Override
    public Object toSqlType(Object source) {
        return (String)source;
    }

    @Override
    public Object getColumnValue(Cursor cursor, int columnIndex) {
        return cursor.getString(columnIndex);
    }

    @Override
    public void setColumnValue(ContentValues contentValues, String key, Object value) {
        contentValues.put(key, (String)value);
    }
}
