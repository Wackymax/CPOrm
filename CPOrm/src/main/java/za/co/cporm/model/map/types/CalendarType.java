package za.co.cporm.model.map.types;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import za.co.cporm.model.map.SqlColumnMapping;

import java.util.Calendar;

/**
 * Created by hennie.brink on 2015-03-19.
 */
public class CalendarType implements SqlColumnMapping {
    @Override
    public Class<?> getJavaType() {
        return Calendar.class;
    }

    @Override
    public String getSqlColumnTypeName() {
        return "INTEGER";
    }

    @Override
    public Long toSqlType(Object source) {
        return ((Calendar)source).getTimeInMillis();
    }

    @Override
    public Object getColumnValue(Cursor cursor, int columnIndex) {
        long time = cursor.getLong(columnIndex);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar;
    }

    @Override
    public void setColumnValue(ContentValues contentValues, String key, Object value) {
        contentValues.put(key, toSqlType(value));
    }

    @Override
    public void setBundleValue(Bundle bundle, String key, Cursor cursor, int columnIndex) {
        bundle.putLong(key, cursor.getLong(columnIndex));
    }

    @Override
    public Object getColumnValue(Bundle bundle, String columnName) {

        long time = bundle.getLong(columnName);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar;
    }
}
