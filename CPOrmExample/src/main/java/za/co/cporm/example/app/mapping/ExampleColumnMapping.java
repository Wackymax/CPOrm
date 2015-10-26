package za.co.cporm.example.app.mapping;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import za.co.cporm.model.map.SqlColumnMapping;

import java.io.*;
import java.util.List;

/**
 * Created by hennie.brink on 2015-05-16.
 */
public class ExampleColumnMapping implements SqlColumnMapping {

    @Override
    public Class<?> getJavaType() {

        return List.class;
    }

    @Override
    public String getSqlColumnTypeName() {

        return "BLOB";
    }

    @Override
    public Object toSqlType(Object source) {

        ByteArrayOutputStream outputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(source);
            objectOutputStream.flush();
            return outputStream.toByteArray();
        } catch (IOException io) {
            Log.e(getClass().getSimpleName(), "Failed to serialize object for storage", io);
            return null;
        } finally {
            closeStreams(outputStream, objectOutputStream);
        }
    }

    @Override
    public Object getColumnValue(Cursor cursor, int columnIndex) {

        byte[] columnValue = cursor.getBlob(columnIndex);
        InputStream inputStream = null;
        ObjectInputStream objectInputStream = null;

        try{

            inputStream = new ByteArrayInputStream(columnValue);
            objectInputStream = new ObjectInputStream(inputStream);

            return objectInputStream.readObject();
        }
        catch (Exception ex) {
            Log.e(getClass().getSimpleName(), "Failed to deserialize object", ex);
        }
        finally {
            closeStreams(inputStream, objectInputStream);
        }
        return null;
    }

    @Override
    public void setColumnValue(ContentValues contentValues, String key, Object value) {

        contentValues.put(key, (byte[]) toSqlType(value));
    }

    @Override
    public void setBundleValue(Bundle bundle, String key, Cursor cursor, int columnIndex) {

        bundle.putSerializable(key, (Serializable) getColumnValue(cursor, columnIndex));
    }

    @Override
    public Object getColumnValue(Bundle bundle, String columnName) {

        return bundle.getSerializable(columnName);
    }

    private void closeStreams(Closeable... streams) {


        try {
            for (Closeable stream : streams) {
                if(stream != null) stream.close();
            }
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), "Failed to close streams");
        }
    }
}
