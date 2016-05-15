package za.co.cporm.model.map.types;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;

import za.co.cporm.model.map.SqlColumnMapping;

/**
 * Created by hennie.brink on 2016/05/14.
 */
public class ByteArray implements SqlColumnMapping {
    @Override
    public Class<?> getJavaType() {
        return Byte[].class;
    }

    @Override
    public String getSqlColumnTypeName() {
        return "BLOB";
    }

    @Override
    public byte[] toSqlType(Object source) {

        return (byte[]) source;
    }

    @Override
    public byte[] getColumnValue(Cursor cursor, int columnIndex) {
        return cursor.getBlob(columnIndex);
    }

    @Override
    public void setColumnValue(ContentValues contentValues, String key, Object value) {
        contentValues.put(key, toSqlType(value));
    }

    @Override
    public void setBundleValue(Bundle bundle, String key, Cursor cursor, int columnIndex) {
        bundle.putByteArray(key, getColumnValue(cursor, columnIndex));
    }

    @Override
    public byte[] getColumnValue(Bundle bundle, String columnName) {

        return bundle.getByteArray(columnName);
    }
}
