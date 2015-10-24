package za.co.cporm.model;

import android.app.Application;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import za.co.cporm.model.generate.TableDetails;
import za.co.cporm.model.query.Select;
import za.co.cporm.model.util.*;
import za.co.cporm.provider.util.UriMatcherHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Contains helper methods that will query contents on the Content Provider
 * and handle all of the Inflation/Deflation of the java object.  This class should mainly be used,
 * to perform Create, Update, Delete operations. To perform
 * Queries the {@link za.co.cporm.model.query.Select} class should be used instead.
 */
public class CPOrm {

    private static Context applicationContext;
    private static TableDetailsCache tableDetailsCache;
    private static boolean allowContentProviderMethodCalling;


    /**
     * This is an optional initialize method that can be used to set the application context.
     * All the context related methods will then use the application context where available.
     *
     * @param application The application that will be using the orm.
     */
    public static void initialize(Application application) {

        initialize(application, false);
    }

    public static void initialize(Application application, boolean allowContentProviderMethodCalling) {

        CPOrm.applicationContext = application;
        CPOrm.allowContentProviderMethodCalling = allowContentProviderMethodCalling;
    }

    /**
     * Gets the inialized application context that can be used to perform querying.
     *
     * @return The application context if available, throws an Illegal Argument Exception if no initialization has been done.
     */
    public static Context getApplicationContext() {

        if (applicationContext == null)
            throw new IllegalArgumentException("You must call initialize() before using this method.");

        return applicationContext;
    }

    public static <T> long countAll(Class<T> dataModel) {

        return countAll(getApplicationContext(), dataModel);
    }

    public static <T> long countAll(Context context, Class<T> dataModel) {

        return Select.from(dataModel).queryAsCount(context);
    }

    public static <T> Iterator<T> findAll(Class<T> dataModel) {

        return findAll(getApplicationContext(), dataModel);
    }

    public static <T> Iterator<T> findAll(Context context, Class<T> dataModel) {

        TableDetails tableDetails = findTableDetails(context, dataModel);
        Uri itemUri = UriMatcherHelper.generateItemUri(context, tableDetails).build();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(itemUri, null, null, null, null);

        return new CursorIterator<T>(tableDetails, cursor);
    }

    public static <T> T findByPrimaryKey(Class<T> dataModel, Object key) {

        return findByPrimaryKey(getApplicationContext(), dataModel, key);
    }

    public static <T> T findByPrimaryKey(Context context, Class<T> dataModel, Object key) {

        TableDetails tableDetails = findTableDetails(context, dataModel);
        TableDetails.ColumnDetails primaryKeyColumn = tableDetails.findPrimaryKeyColumn();
        Object columnValue = primaryKeyColumn.getColumnTypeMapping().toSqlType(key);
        Uri itemUri = UriMatcherHelper.generateItemUri(context, tableDetails, String.valueOf(columnValue)).build();

        return findSingleItem(context, itemUri, tableDetails);
    }

    public static <T> int insertAll(List<T> dataModelObjects) {

        return insertAll(getApplicationContext(), dataModelObjects);
    }

    public static <T> int insertAll(Context context, List<T> dataModelObjects) {

        if (dataModelObjects == null || dataModelObjects.isEmpty())
            return 0;

        TableDetails tableDetails = findTableDetails(context, dataModelObjects.get(0).getClass());
        Uri insertUri = UriMatcherHelper.generateItemUri(context, tableDetails).build();

        ContentValues[] values = new ContentValues[dataModelObjects.size()];
        for (int i = 0; i < dataModelObjects.size(); i++) {

            values[i] = ModelInflater.deflate(tableDetails, dataModelObjects.get(i));
        }

        ContentResolver contentResolver = context.getContentResolver();
        return contentResolver.bulkInsert(insertUri, values);
    }

    public static <T> int insertAll(Context context, ContentProviderClient providerClient, List<T> dataModelObjects) throws RemoteException {

        if (dataModelObjects == null || dataModelObjects.isEmpty())
            return 0;

        TableDetails tableDetails = findTableDetails(context, dataModelObjects.get(0).getClass());
        Uri insertUri = UriMatcherHelper.generateItemUri(context, tableDetails).build();

        ContentValues[] values = new ContentValues[dataModelObjects.size()];
        for (int i = 0; i < dataModelObjects.size(); i++) {

            values[i] = ModelInflater.deflate(tableDetails, dataModelObjects.get(i));
        }

        return providerClient.bulkInsert(insertUri, values);
    }

    public static <T> long insert(T dataModelObject) {

        return insert(getApplicationContext(), dataModelObject);
    }

    public static <T> long insert(Context context, T dataModelObject) {

        TableDetails tableDetails = findTableDetails(context, dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Uri insertUri = UriMatcherHelper.generateItemUri(context, tableDetails).build();

        ContentResolver contentResolver = context.getContentResolver();
        Uri insert = contentResolver.insert(insertUri, contentValues);

        return ContentUris.parseId(insert);
    }

    public static <T> T insertAndReturn(T dataModelObject) {

        return insertAndReturn(getApplicationContext(), dataModelObject);
    }

    public static <T> T insertAndReturn(Context context, T dataModelObject) {

        TableDetails tableDetails = findTableDetails(context, dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Uri insertUri = UriMatcherHelper.generateItemUri(context, tableDetails).build();

        ContentResolver contentResolver = context.getContentResolver();
        Uri itemUri = contentResolver.insert(insertUri, contentValues);

        return findSingleItem(context, itemUri, tableDetails);
    }

    public static <T> ContentProviderOperation prepareInsert(T dataModelObject) {

        return prepareInsert(getApplicationContext(), dataModelObject);
    }

    public static <T> ContentProviderOperation prepareInsert(Context context, T dataModelObject) {

        TableDetails tableDetails = findTableDetails(context, dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Uri insertUri = UriMatcherHelper.generateItemUri(context, tableDetails).build();

        return ContentProviderOperation.newInsert(insertUri)
                .withExpectedCount(1)
                .withValues(contentValues)
                .build();
    }

    public static <T> void update(T dataModelObject) {

        update(getApplicationContext(), dataModelObject);
    }

    public static <T> void update(Context context, T dataModelObject) {

        TableDetails tableDetails = findTableDetails(context, dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Object columnValue = ModelInflater.deflateColumn(tableDetails, tableDetails.findPrimaryKeyColumn(), dataModelObject);
        Uri itemUri = UriMatcherHelper.generateItemUri(context, tableDetails, String.valueOf(columnValue)).build();

        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.update(itemUri, contentValues, null, null);
    }

    public static <T> void updateColumns(T dataModelObject, String... columns) {

        updateColumns(getApplicationContext(), dataModelObject, columns);
    }

    public static <T> void updateColumns(Context context, T dataModelObject, String... columns) {

        TableDetails tableDetails = findTableDetails(context, dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Object columnValue = ModelInflater.deflateColumn(tableDetails, tableDetails.findPrimaryKeyColumn(), dataModelObject);
        Uri itemUri = UriMatcherHelper.generateItemUri(context, tableDetails, String.valueOf(columnValue)).build();

        for (String contentColumn : tableDetails.getColumnNames()) {

            boolean includeColumn = false;
            for (String column : columns) {
                if (contentColumn.equals(column)) {
                    includeColumn = true;
                    break;
                }
            }

            if (!includeColumn)
                contentValues.remove(contentColumn);
        }

        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.update(itemUri, contentValues, null, null);
    }

    public static <T> void updateColumnsExcluding(T dataModelObject, String... columnsToExclude) {

        updateColumnsExcluding(getApplicationContext(), dataModelObject, columnsToExclude);
    }

    public static <T> void updateColumnsExcluding(Context context, T dataModelObject, String... columnsToExclude) {

        TableDetails tableDetails = findTableDetails(context, dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Object columnValue = ModelInflater.deflateColumn(tableDetails, tableDetails.findPrimaryKeyColumn(), dataModelObject);
        Uri itemUri = UriMatcherHelper.generateItemUri(context, tableDetails, String.valueOf(columnValue)).build();

        for (String columnToExclude : columnsToExclude) {

            contentValues.remove(columnToExclude);
        }

        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.update(itemUri, contentValues, null, null);
    }

    public static <T> ContentProviderOperation prepareUpdate(T dataModelObject) {

        return prepareUpdate(getApplicationContext(), dataModelObject);
    }

    public static <T> ContentProviderOperation prepareUpdate(Context context, T dataModelObject) {

        TableDetails tableDetails = findTableDetails(context, dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Object columnValue = ModelInflater.deflateColumn(tableDetails, tableDetails.findPrimaryKeyColumn(), dataModelObject);
        Uri itemUri = UriMatcherHelper.generateItemUri(context, tableDetails, String.valueOf(columnValue)).build();

        return ContentProviderOperation.newUpdate(itemUri)
                .withExpectedCount(1)
                .withValues(contentValues)
                .build();
    }

    public static <T> void delete(T dataModelObject) {

        delete(getApplicationContext(), dataModelObject);
    }

    public static <T> void delete(Context context, T dataModelObject) {

        TableDetails tableDetails = findTableDetails(context, dataModelObject.getClass());
        Object columnValue = ModelInflater.deflateColumn(tableDetails, tableDetails.findPrimaryKeyColumn(), dataModelObject);
        Uri itemUri = UriMatcherHelper.generateItemUri(context, tableDetails, String.valueOf(columnValue)).build();

        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(itemUri, null, null);
    }

    public static <T> void delete(Select<T> select) {

        delete(getApplicationContext(), select);
    }

    public static <T> void delete(Context context, Select<T> select) {

        ContentResolverValues contentResolverValues = select.asContentResolverValue(context);

        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(contentResolverValues.getItemUri(), contentResolverValues.getWhere(), contentResolverValues.getWhereArgs());
    }

    public static <T> ContentProviderOperation prepareDelete(T dataModelObject) {

        return prepareDelete(getApplicationContext(), dataModelObject);
    }

    public static <T> ContentProviderOperation prepareDelete(Context context, T dataModelObject) {

        TableDetails tableDetails = findTableDetails(context, dataModelObject.getClass());
        Object columnValue = ModelInflater.deflateColumn(tableDetails, tableDetails.findPrimaryKeyColumn(), dataModelObject);
        Uri itemUri = UriMatcherHelper.generateItemUri(context, tableDetails, String.valueOf(columnValue)).build();

        return ContentProviderOperation.newDelete(itemUri)
                .withExpectedCount(1)
                .build();
    }

    public static <T> ContentProviderOperation prepareDelete(Select<T> select) {

        return prepareDelete(getApplicationContext(), select);
    }

    public static <T> ContentProviderOperation prepareDelete(Context context, Select<T> select) {

        ContentResolverValues contentResolverValues = select.asContentResolverValue(context);

        return ContentProviderOperation.newDelete(contentResolverValues.getItemUri())
                .withSelection(contentResolverValues.getWhere(), contentResolverValues.getWhereArgs())
                .build();
    }

    public static <T> void deleteAll(Class<T> dataModel) {

        deleteAll(getApplicationContext(), dataModel);
    }

    public static <T> void deleteAll(Context context, Class<T> dataModel) {

        TableDetails tableDetails = findTableDetails(context, dataModel);
        Uri itemUri = UriMatcherHelper.generateItemUri(context, tableDetails).build();

        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(itemUri, null, null);
    }

    public static ContentProviderResult[] applyPreparedOperations(Collection<ContentProviderOperation> operations) throws RemoteException, OperationApplicationException {

        return applyPreparedOperations(getApplicationContext(), operations);
    }

    public static ContentProviderResult[] applyPreparedOperations(Context context, Collection<ContentProviderOperation> operations) throws RemoteException, OperationApplicationException {

        return context
                .getContentResolver()
                .applyBatch(ManifestHelper.getAuthority(context), new ArrayList<ContentProviderOperation>(operations));
    }

    public static <T> Uri getItemUri(Class<T> dataModel) {

        return getItemUri(getApplicationContext(), dataModel);
    }

    public static <T> Uri getItemUri(Context context, Class<T> dataModel) {

        TableDetails tableDetails = findTableDetails(context, dataModel);
        return UriMatcherHelper.generateItemUri(context, tableDetails).build();
    }

    protected static <T> T findSingleItem(Uri itemUri, TableDetails tableDetails) {

        return findSingleItem(getApplicationContext(), itemUri, tableDetails);
    }

    protected static <T> T findSingleItem(Context context, Uri itemUri, TableDetails tableDetails) {

        if (allowContentProviderMethodCalling && tableDetails.isSerializable()) {

            Bundle extras = new Bundle();
            extras.putParcelable("URI", itemUri);

            ContentResolver contentResolver = context.getContentResolver();
            Bundle single = contentResolver.call(itemUri, "FindById", null, extras);

            return single == null ? null : (T) single.getSerializable("ITEM");
        } else {
            ContentResolver contentResolver = context.getContentResolver();

            Cursor cursor = null;
            try {
                cursor = contentResolver.query(itemUri, tableDetails.getColumnNames(), null, null, null);

                if (cursor.moveToFirst()) return ModelInflater.inflate(cursor, tableDetails);
                else throw new IllegalArgumentException("No row found with the key " + itemUri.getLastPathSegment());
            } finally {
                if (cursor != null) cursor.close();
            }
        }
    }

    public static synchronized TableDetails findTableDetails(Context context, Class<?> item) {

        if (tableDetailsCache == null) {
            tableDetailsCache = new TableDetailsCache();
        }
        return tableDetailsCache.findTableDetails(context, item);
    }

    public static boolean allowSingleItemCursorBypass() {

        return allowContentProviderMethodCalling;
    }
}
