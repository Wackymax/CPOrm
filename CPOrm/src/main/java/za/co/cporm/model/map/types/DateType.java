package za.co.cporm.model.map.types;

import android.content.ContentValues;
import android.database.Cursor;
import za.co.cporm.model.map.SqlColumnMapping;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * Created by hennie.brink on 2015-03-19.
 */
public class DateType implements SqlColumnMapping {
    @Override
    public Type getJavaType() {
        return Date.class;
    }

    @Override
    public String getSqlColumnTypeName() {
        return "INTEGER";
    }

    @Override
    public Object toSqlType(Object source) {
        return source;
    }

    @Override
    public Object getColumnValue(Cursor cursor, int columnIndex) {
        return new Date(cursor.getLong(columnIndex));
    }

    @Override
    public void setColumnValue(ContentValues contentValues, String key, Object value) {
        contentValues.put(key, ((Date)value).getTime());
    }
}
