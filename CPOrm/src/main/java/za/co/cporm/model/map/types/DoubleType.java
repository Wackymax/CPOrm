package za.co.cporm.model.map.types;

import android.content.ContentValues;
import android.database.Cursor;
import za.co.cporm.model.map.SqlColumnMapping;

/**
 * Created by hennie.brink on 2015-03-19.
 */
public class DoubleType implements SqlColumnMapping {
    @Override
    public Class<?> getJavaType() {
        return Double.class;
    }

    @Override
    public String getSqlColumnTypeName() {
        return "REAL";
    }

    @Override
    public Object toSqlType(Object source) {
        return source;
    }

    @Override
    public Object getColumnValue(Cursor cursor, int columnIndex) {
        return cursor.getDouble(columnIndex);
    }

    @Override
    public void setColumnValue(ContentValues contentValues, String key, Object value) {
        contentValues.put(key, (Double)value);
    }
}
