package za.co.cporm.model;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import za.co.cporm.model.generate.TableDetails;
import za.co.cporm.model.query.Select;
import za.co.cporm.model.util.CursorIterator;
import za.co.cporm.model.util.ModelInflater;
import za.co.cporm.model.util.TableDetailsCache;
import za.co.cporm.provider.util.UriMatcherHelper;

import java.util.Iterator;

/**
 * Contains helper methods that will query contents on the Content Provider
 * and handle all of the Inflation/Deflation of the java object.  This class should mainly be used,
 * to perform Create, Update, Delete operations. To perform
 * Queries the {@link za.co.cporm.model.query.Select} class should be used instead.
 */
public class CPHelper {

    private static TableDetailsCache tableDetailsCache;

    public static <T> long countAll(Context context, Class<T> dataModel){
        return Select.from(dataModel).queryAsCount(context);
    }

    public static <T> Iterator<T> findAll(Context context, Class<T> dataModel){

        TableDetails tableDetails = findTableDetails(context, dataModel);
        Uri itemUri = UriMatcherHelper.generateItemUri(context, tableDetails).build();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(itemUri, null, null, null, null);

        return new CursorIterator<T>(tableDetails, cursor);
    }

    public static <T> T findByPrimaryKey(Context context, Class<T> dataModel, Object key){

        TableDetails tableDetails = findTableDetails(context, dataModel);
        TableDetails.ColumnDetails primaryKeyColumn = tableDetails.findPrimaryKeyColumn();
        Object columnValue = primaryKeyColumn.getColumnTypeMapping().toSqlType(key);
        Uri itemUri = UriMatcherHelper.generateItemUri(context, tableDetails, String.valueOf(columnValue)).build();

        return findSingleItem(context, itemUri, tableDetails);
    }

    public static <T> void insert(Context context, T dataModelObject){
        TableDetails tableDetails = findTableDetails(context, dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Uri insertUri = UriMatcherHelper.generateItemUri(context, tableDetails).build();

        ContentResolver contentResolver = context.getContentResolver();
        Uri itemUri = contentResolver.insert(insertUri, contentValues);
    }

    public static <T> T insertAndReturn(Context context, T dataModelObject){
        TableDetails tableDetails = findTableDetails(context, dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Uri insertUri = UriMatcherHelper.generateItemUri(context, tableDetails).build();

        ContentResolver contentResolver = context.getContentResolver();
        Uri itemUri = contentResolver.insert(insertUri, contentValues);

        return findSingleItem(context, itemUri, tableDetails);
    }

    public static <T> void update(Context context, T dataModelObject){
        TableDetails tableDetails = findTableDetails(context, dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Object columnValue = contentValues.get(tableDetails.findPrimaryKeyColumn().getColumnName());
        Uri itemUri = UriMatcherHelper.generateItemUri(context, tableDetails, String.valueOf(columnValue)).build();

        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.update(itemUri, contentValues, null, null);
    }

    public static <T> void delete(Context context, T dataModelObject){
        TableDetails tableDetails = findTableDetails(context, dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Object columnValue = contentValues.get(tableDetails.findPrimaryKeyColumn().getColumnName());
        Uri itemUri = UriMatcherHelper.generateItemUri(context, tableDetails, String.valueOf(columnValue)).build();

        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(itemUri, null, null);
    }

    public static <T> void deleteAll(Context context, Class<T> dataModel){
        TableDetails tableDetails = findTableDetails(context, dataModel);
        Uri itemUri = UriMatcherHelper.generateItemUri(context, tableDetails).build();

        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(itemUri, null, null);
    }

    protected static <T> T findSingleItem(Context context, Uri itemUri, TableDetails tableDetails){
        ContentResolver contentResolver = context.getContentResolver();

        Cursor cursor = null;
        try{
            cursor = contentResolver.query(itemUri, null, null, null, null);

            if(cursor.moveToFirst()) return ModelInflater.inflate(cursor, tableDetails);
            else throw new IllegalArgumentException("No row found with the key " + itemUri.getLastPathSegment());
        }
        finally {
            if(cursor != null) cursor.close();
        }
    }

    public static synchronized TableDetails findTableDetails(Context context, Class<?> item){

        if(tableDetailsCache == null){
            tableDetailsCache = new TableDetailsCache();
        }
        return tableDetailsCache.findTableDetails(context, item);
    }
}
