package za.co.cporm.model.util;

import android.content.ContentValues;
import android.database.Cursor;
import za.co.cporm.model.generate.TableDetails;

import java.lang.reflect.Field;

/**
 * Handles the inflation and deflation of Java objects to and from content values/cursors
 */
public class ModelInflater {

    public static Object deflateColumn(TableDetails tableDetails, TableDetails.ColumnDetails columnDetails, Object dataModelObject){

        try {

            Object value = columnDetails.getColumnField().get(dataModelObject);

            if(value == null) return null;
            else return columnDetails.getColumnTypeMapping().toSqlType(value);
        }
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Unable to access protected field, change the access level: " + columnDetails.getColumnName());
            }
    }

    public static ContentValues deflate(TableDetails tableDetails, Object dataModelObject){

        ContentValues contentValues = new ContentValues(tableDetails.getColumns().size());

        for (TableDetails.ColumnDetails columnDetails : tableDetails.getColumns()) {

            if(columnDetails.isAutoIncrement()) continue;

            try {

                String key = columnDetails.getColumnName();
                Object value = columnDetails.getColumnField().get(dataModelObject);

                if(value == null) contentValues.putNull(key);
                else columnDetails.getColumnTypeMapping().setColumnValue(contentValues, key, value);
            }
            catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Unable to access protected field, change the access level: " + columnDetails.getColumnName());
            }
        }

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

    private static <T> void inflateColumn(Cursor cursor, T dataModelObject, TableDetails.ColumnDetails columnDetails){

        int columnIndex = cursor.getColumnIndex(columnDetails.getColumnName());

        if(columnIndex == -1 || cursor.isNull(columnIndex)){
            return;
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
