package za.co.cporm.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import za.co.cporm.model.CPOrmConfiguration;
import za.co.cporm.model.CPOrmDatabase;
import za.co.cporm.model.generate.TableDetails;
import za.co.cporm.model.util.ManifestHelper;
import za.co.cporm.provider.util.UriMatcherHelper;
import za.co.cporm.util.CPOrmLog;

import java.util.Arrays;
import java.util.List;

/**
 * The base content provided that will expose all of the model objects.
 * Objects are expose in the form of authority/table_name/*
 */
public class CPOrmContentProvider extends ContentProvider {

    public static final String PARAMETER_OFFSET = "OFFSET";
    public static final String PARAMETER_LIMIT = "LIMIT";
    public static final String PARAMETER_SYNC = "IS_SYNC";

    private CPOrmConfiguration cPOrmConfiguration;
    private CPOrmDatabase database;
    private UriMatcherHelper uriMatcherHelper;
    private boolean debugEnabled;

    @Override
    public boolean onCreate() {

        cPOrmConfiguration = ManifestHelper.getConfiguration(getContext());
        database = new CPOrmDatabase(getContext(), cPOrmConfiguration);
        uriMatcherHelper = new UriMatcherHelper(getContext());
        uriMatcherHelper.init(getContext(), database.getcPOrmConfiguration(), database.getTableDetailsCache());

        debugEnabled = cPOrmConfiguration.isQueryLoggingEnabled();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        TableDetails tableDetails = uriMatcherHelper.getTableDetails(uri);
        SQLiteDatabase db = database.getReadableDatabase();
        String limit = constructLimit(uri);

        if (debugEnabled) {
            CPOrmLog.d("********* Query **********");
            CPOrmLog.d("Uri: " + uri);
            CPOrmLog.d("Projection: " + Arrays.toString(projection));
            CPOrmLog.d("Selection: " + selection);
            CPOrmLog.d("Args: " + Arrays.toString(selectionArgs));
            CPOrmLog.d("Sort: " + sortOrder);
            CPOrmLog.d("Limit: " + limit);
        }

        Cursor cursor;

        if (uriMatcherHelper.isSingleItemRequested(uri)) {

            String itemId = uri.getLastPathSegment();
            TableDetails.ColumnDetails primaryKeyColumn = tableDetails.findPrimaryKeyColumn();
            cursor = db.query(tableDetails.getTableName(), projection, primaryKeyColumn.getColumnName() + " = ?", new String[]{itemId}, null, sortOrder, limit);
        } else
            cursor = db.query(tableDetails.getTableName(), projection, selection, selectionArgs, null, null, sortOrder, limit);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {

        return uriMatcherHelper.getType(uri);
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        TableDetails tableDetails = uriMatcherHelper.getTableDetails(uri);
        SQLiteDatabase db = database.getWritableDatabase();

        if (debugEnabled) {
            CPOrmLog.d("********* Insert **********");
            CPOrmLog.d("Uri: " + uri);
            CPOrmLog.d("Content Values: " + contentValues);
        }

        long insertId = db.insertOrThrow(tableDetails.getTableName(), null, contentValues);

        if (insertId == -1)
            throw new IllegalArgumentException("Failed to insert row for into table " + tableDetails.getTableName() + " using values " + contentValues);

        notifyChanges(uri, tableDetails);

        TableDetails.ColumnDetails primaryKeyColumn = tableDetails.findPrimaryKeyColumn();
        if (primaryKeyColumn.isAutoIncrement()) return uriMatcherHelper.generateSingleItemUri(tableDetails, insertId);
        else {

            String primaryKeyValue = contentValues.getAsString(primaryKeyColumn.getColumnName());
            return uriMatcherHelper.generateSingleItemUri(tableDetails, primaryKeyValue);
        }
    }

    @Override
    public int delete(Uri uri, String where, String[] args) {

        TableDetails tableDetails = uriMatcherHelper.getTableDetails(uri);
        SQLiteDatabase db = database.getWritableDatabase();

        if (debugEnabled) {
            CPOrmLog.d("********* Delete **********");
            CPOrmLog.d("Uri: " + uri);
            CPOrmLog.d("Where: " + where);
            CPOrmLog.d("Args: " + Arrays.toString(args));
        }

        int deleteCount;

        if (uriMatcherHelper.isSingleItemRequested(uri)) {

            String itemId = uri.getLastPathSegment();
            TableDetails.ColumnDetails primaryKeyColumn = tableDetails.findPrimaryKeyColumn();
            deleteCount = db.delete(tableDetails.getTableName(), primaryKeyColumn.getColumnName() + " = ?", new String[]{itemId});
        } else deleteCount = db.delete(tableDetails.getTableName(), where, args);

        if (deleteCount == 0)
            return deleteCount;

        notifyChanges(uri, tableDetails);


        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String where, String[] args) {

        TableDetails tableDetails = uriMatcherHelper.getTableDetails(uri);
        SQLiteDatabase db = database.getWritableDatabase();

        if (debugEnabled) {
            CPOrmLog.d("********* Update **********");
            CPOrmLog.d("Uri: " + uri);
            CPOrmLog.d("Content Values: " + contentValues);
            CPOrmLog.d("Where: " + where);
            CPOrmLog.d("Args: " + Arrays.toString(args));
        }

        int updateCount;

        if (uriMatcherHelper.isSingleItemRequested(uri)) {

            String itemId = uri.getLastPathSegment();
            TableDetails.ColumnDetails primaryKeyColumn = tableDetails.findPrimaryKeyColumn();
            updateCount = db.update(tableDetails.getTableName(), contentValues, primaryKeyColumn.getColumnName() + " = ?", new String[]{itemId});
        } else updateCount = db.update(tableDetails.getTableName(), contentValues, where, args);

        if (updateCount > 0 && shouldChangesBeNotified(tableDetails, contentValues)) {
            notifyChanges(uri, tableDetails);
        }

        return updateCount;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        TableDetails tableDetails = uriMatcherHelper.getTableDetails(uri);
        SQLiteDatabase db = database.getWritableDatabase();
        db.beginTransactionNonExclusive();

        if (debugEnabled) {
            CPOrmLog.d("********* Bulk Insert **********");
            CPOrmLog.d("Uri: " + uri);
        }

        int count = 0;

        try {
            for (int i = 0; i < values.length; i++) {
                ContentValues contentValues = values[i];
                db.insertOrThrow(tableDetails.getTableName(), null, contentValues);
                count++;
            }

            db.setTransactionSuccessful();

            notifyChanges(uri, tableDetails);
        } finally {
            db.endTransaction();
        }

        return count;
    }

    private String constructLimit(Uri uri) {

        String offsetParam = uri.getQueryParameter(PARAMETER_OFFSET);
        String limitParam = uri.getQueryParameter(PARAMETER_LIMIT);

        Integer offset = null;
        Integer limit = null;

        if (!TextUtils.isEmpty(offsetParam) && TextUtils.isDigitsOnly(offsetParam)) {
            offset = Integer.valueOf(offsetParam);
        }
        if (!TextUtils.isEmpty(limitParam) && TextUtils.isDigitsOnly(limitParam)) {
            limit = Integer.valueOf(limitParam);
        }

        if (limit == null && offset == null)
            return null;

        StringBuilder limitStatement = new StringBuilder();

        if (limit != null && offset != null) {
            limitStatement.append(offset);
            limitStatement.append(",");
            limitStatement.append(limit);
        } else if (limit != null) {
            limitStatement.append(limit);
        } else throw new IllegalArgumentException("A limit must also be provided when setting an offset");

        return limitStatement.toString();
    }

    private boolean shouldChangesBeNotified(TableDetails tableDetails, ContentValues contentValues) {

        boolean notify = false;

        for (String columnName : contentValues.keySet()) {

            TableDetails.ColumnDetails column = tableDetails.findColumn(columnName);
            if (column != null)
                notify = notify || column.notifyChanges();
        }

        return notify;
    }

    private void notifyChanges(Uri uri, TableDetails tableDetails) {

        Boolean sync = uri.getBooleanQueryParameter(PARAMETER_SYNC, true);
        getContext().getContentResolver().notifyChange(uri, null, sync);

        List<Class<?>> changeListeners = tableDetails.getChangeListeners();
        if (!changeListeners.isEmpty()) {

            for (int i = 0; i < changeListeners.size(); i++) {
                Class<?> changeListener = changeListeners.get(i);
                TableDetails changeListenerDetails = database.getTableDetailsCache().findTableDetails(getContext(), changeListener);

                if (changeListenerDetails == null)
                    continue;

                //Change listeners are registered on views, so the entire view needs to be updated if changes to its data occurs
                Uri changeUri = uriMatcherHelper.generateItemUri(changeListenerDetails);
                getContext().getContentResolver().notifyChange(changeUri, null, sync);
            }
        }
    }
}
