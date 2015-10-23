package za.co.cporm.model.map.types;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import za.co.cporm.model.map.SqlColumnMapping;

import java.util.UUID;

/**
 * Created by hennie.brink on 2015-03-19.
 */
public class UUIDType implements SqlColumnMapping {
    @Override
    public Class<?> getJavaType() {
        return UUID.class;
    }

    @Override
    public String getSqlColumnTypeName() {
        return "TEXT";
    }

    @Override
    public String toSqlType(Object source) {
        return source.toString();
    }

    @Override
    public Object getColumnValue(Cursor cursor, int columnIndex) {

        return UUID.fromString(cursor.getString(columnIndex));
    }

    @Override
    public void setColumnValue(ContentValues contentValues, String key, Object value) {
        contentValues.put(key, toSqlType(value));
    }

    @Override
    public void setBundleValue(Bundle bundle, String key, Cursor cursor, int columnIndex) {
        bundle.putString(key, cursor.getString(columnIndex));
    }

    @Override
    public Object getColumnValue(Bundle bundle, String columnName) {

        return UUID.fromString(bundle.getString(columnName));
    }
}
