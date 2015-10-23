package za.co.cporm.model.map.types;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import za.co.cporm.model.map.SqlColumnMapping;

/**
 * Created by hennie.brink on 2015-03-19.
 */
public class FloatType implements SqlColumnMapping {
    @Override
    public Class<?> getJavaType() {
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

    @Override
    public void setBundleValue(Bundle bundle, String key, Cursor cursor, int columnIndex) {
        bundle.putFloat(key, cursor.getFloat(columnIndex));
    }

    @Override
    public Object getColumnValue(Bundle bundle, String columnName) {

        return bundle.getFloat(columnName);
    }
}
