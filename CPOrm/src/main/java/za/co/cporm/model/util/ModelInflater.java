package za.co.cporm.model.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
import za.co.cporm.model.generate.TableDetails;

import java.lang.reflect.Field;

/**
 * Handles the inflation and deflation of Java objects to and from content values/cursors
 */
public class ModelInflater {

    public static ContentValues deflate(TableDetails tableDetails, Object dataModelObject){

        long time = System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();

        for (TableDetails.ColumnDetails columnDetails : tableDetails.getColumns()) {

            if(columnDetails.isAutoIncrement()) continue;

            try {

                String key = columnDetails.getColumnName();
                Object value = columnDetails.getColumnField().get(dataModelObject);

                if(value == null) continue;

                columnDetails.getColumnTypeMapping().setColumnValue(contentValues, key, value);
            }
            catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Unable to access protected field, change the access level: " + columnDetails.getColumnName());
            }
        }

        Log.d("ModelInflater", "Deflated item in " + (System.currentTimeMillis() - time) + "ms");
        return contentValues;
    }

    public static <T> T inflate(Cursor cursor, TableDetails tableDetails){

        T dataModelObject;

        try
        {
            dataModelObject = (T)tableDetails.getTableClass().getConstructor().newInstance();
        } catch (Exception ex){
            throw new IllegalArgumentException("Could not create a new instance of data model object: " + tableDetails.getTableName());
        }

        for (TableDetails.ColumnDetails columnDetails : tableDetails.getColumns()) {
            inflateColumn(cursor, dataModelObject, columnDetails);
        }

        return dataModelObject;
    }

    private static <T> void inflateColumn(Cursor cursor,T dataModelObject, TableDetails.ColumnDetails columnDetails){

        int columnIndex = cursor.getColumnIndexOrThrow(columnDetails.getColumnName());

        if(cursor.isNull(columnIndex)){
            if(columnDetails.isNullable()) return;
            else throw new IllegalStateException("Found a null value on a non nullable column: " + columnDetails.getColumnName());
        }

        Field columnField = columnDetails.getColumnField();
        try {


            columnField.set(dataModelObject, columnDetails.getColumnTypeMapping().getColumnValue(cursor,columnIndex));
        }
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Not allowed to alter the value of the field, please change the access level: " + columnDetails.getColumnName());
        }
    }
}
