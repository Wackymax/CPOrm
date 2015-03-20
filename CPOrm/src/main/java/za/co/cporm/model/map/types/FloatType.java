package za.co.cporm.model.map.types;

import android.content.ContentValues;
import android.database.Cursor;
import za.co.cporm.model.map.SqlColumnMapping;

import java.lang.reflect.Type;

/**
 * Created by hennie.brink on 2015-03-19.
 */
public class FloatType implements SqlColumnMapping {
    @Override
    public Type getJavaType() {
        return Float.class;
    }

    @Override
    public String getSqlColumnTypeName() {
        return "NUMERIC";
    }

    @Override
    public Object toSqlType(Object source) {
        return source;
    }

    @Override
    public Object getColumnValue(Cursor cursor, int columnIndex) {
        return cursor.getFloat(columnIndex);
    }

    @Override
    public void setColumnValue(ContentValues contentValues, String key, Object value) {
        contentValues.put(key, (Float)value);
    }
}
