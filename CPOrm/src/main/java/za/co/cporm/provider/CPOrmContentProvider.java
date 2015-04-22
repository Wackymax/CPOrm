package za.co.cporm.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import za.co.cporm.model.CPOrmConfiguration;
import za.co.cporm.model.CPOrmDatabase;
import za.co.cporm.model.generate.TableDetails;
import za.co.cporm.model.util.ManifestHelper;
import za.co.cporm.provider.util.UriMatcherHelper;

import java.util.Arrays;

/**
 * The base content provided that will expose all of the model objects.
 * Objects are expose in the form of authority/table_name/*
 */
public class CPOrmContentProvider extends ContentProvider {

    private static final String TAG = "CPOrmContentProvider";

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
            Log.d(TAG, "********* Query **********");
            Log.d(TAG, "Uri: " + uri);
            Log.d(TAG, "Projection: " + Arrays.toString(projection));
            Log.d(TAG, "Selection: " + selection);
            Log.d(TAG, "Args: " + Arrays.toString(selectionArgs));
            Log.d(TAG, "Sort: " + sortOrder);
            Log.d(TAG, "Limit: " + limit);
        }

        Cursor cursor;

        if (uriMatcherHelper.isSingleItemRequested(uri)) {

            String itemId = uri.getLastPathSegment();
            TableDetails.ColumnDetails primaryKeyColumn = tableDetails.findPrimaryKeyColumn();
            cursor = db.query(tableDetails.getTableName(), tableDetails.getColumnNames(), primaryKeyColumn.getColumnName() + " = ?", new String[]{itemId}, null, null, null);
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
        Boolean sync = uri.getBooleanQueryParameter(PARAMETER_SYNC, true);

        if (debugEnabled) {
            Log.d(TAG, "********* Insert **********");
            Log.d(TAG, "Uri: " + uri);
            Log.d(TAG, "Content Values: " + contentValues);
        }

        long insertId = db.insertOrThrow(tableDetails.getTableName(), null, contentValues);

        if (insertId == -1)
            throw new IllegalArgumentException("Failed to insert row for into table " + tableDetails.getTableName() + " using values " + contentValues);

        getContext().getContentResolver().notifyChange(uri, null, sync);

        if (!tableDetails.getChangeListeners().isEmpty()) {

            for (Class<?> changeListener : tableDetails.getChangeListeners()) {

                TableDetails changeListenerDetails = database.getTableDetailsCache().findTableDetails(getContext(), changeListener);

                if (changeListenerDetails == null)
                    continue;

                Uri changeUri = uriMatcherHelper.generateItemUri(changeListenerDetails);
                getContext().getContentResolver().notifyChange(changeUri, null, sync);
            }
        }

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
        Boolean sync = uri.getBooleanQueryParameter(PARAMETER_SYNC, true);

        if (debugEnabled) {
            Log.d(TAG, "********* Delete **********");
            Log.d(TAG, "Uri: " + uri);
            Log.d(TAG, "Where: " + where);
            Log.d(TAG, "Args: " + Arrays.toString(args));
        }

        int deleteCount;

        if (uriMatcherHelper.isSingleItemRequested(uri)) {

            String itemId = uri.getLastPathSegment();
            TableDetails.ColumnDetails primaryKeyColumn = tableDetails.findPrimaryKeyColumn();
            deleteCount = db.delete(tableDetails.getTableName(), primaryKeyColumn.getColumnName() + " = ?", new String[]{itemId});
        } else deleteCount = db.delete(tableDetails.getTableName(), where, args);

        if (deleteCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null, sync);

            if (!tableDetails.getChangeListeners().isEmpty()) {

                for (Class<?> changeListener : tableDetails.getChangeListeners()) {

                    TableDetails changeListenerDetails = database.getTableDetailsCache().findTableDetails(getContext(), changeListener);

                    if (changeListenerDetails == null)
                        continue;

                    Uri changeUri = uriMatcherHelper.generateItemUri(changeListenerDetails);
                    getContext().getContentResolver().notifyChange(changeUri, null, sync);
                }
            }
        }


        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String where, String[] args) {

        TableDetails tableDetails = uriMatcherHelper.getTableDetails(uri);
        SQLiteDatabase db = database.getWritableDatabase();
        Boolean sync = uri.getBooleanQueryParameter(PARAMETER_SYNC, true);

        if (debugEnabled) {
            Log.d(TAG, "********* Update **********");
            Log.d(TAG, "Uri: " + uri);
            Log.d(TAG, "Content Values: " + contentValues);
            Log.d(TAG, "Where: " + where);
            Log.d(TAG, "Args: " + Arrays.toString(args));
        }

        int updateCount;

        if (uriMatcherHelper.isSingleItemRequested(uri)) {

            String itemId = uri.getLastPathSegment();
            TableDetails.ColumnDetails primaryKeyColumn = tableDetails.findPrimaryKeyColumn();
            updateCount = db.update(tableDetails.getTableName(), contentValues, primaryKeyColumn.getColumnName() + " = ?", new String[]{itemId});
        } else updateCount = db.update(tableDetails.getTableName(), contentValues, where, args);

        if (updateCount > 0 && shouldChangesBeNotified(tableDetails, contentValues)) {

            getContext().getContentResolver().notifyChange(uri, null, sync);

            if (!tableDetails.getChangeListeners().isEmpty()) {

                String itemId = uri.getLastPathSegment();
                for (Class<?> changeListener : tableDetails.getChangeListeners()) {

                    TableDetails changeListenerDetails = database.getTableDetailsCache().findTableDetails(getContext(), changeListener);

                    if (changeListenerDetails == null)
                        continue;

                    Uri changeUri;
                    if (TextUtils.isEmpty(itemId))
                        changeUri = uriMatcherHelper.generateItemUri(changeListenerDetails);
                    else changeUri = uriMatcherHelper.generateSingleItemUri(changeListenerDetails, itemId);

                    getContext().getContentResolver().notifyChange(changeUri, null, sync);
                }
            }
        }


        return updateCount;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        TableDetails tableDetails = uriMatcherHelper.getTableDetails(uri);
        SQLiteDatabase db = database.getWritableDatabase();
        db.beginTransactionNonExclusive();
        Boolean sync = uri.getBooleanQueryParameter(PARAMETER_SYNC, true);

        if (debugEnabled) {
            Log.d(TAG, "********* Bulk Insert **********");
            Log.d(TAG, "Uri: " + uri);
        }

        int count = 0;

        try {
            for (ContentValues contentValues : values) {

                db.insertOrThrow(tableDetails.getTableName(), null, contentValues);
                count++;
            }

            db.setTransactionSuccessful();

            getContext().getContentResolver().notifyChange(uri, null, sync);

            if (!tableDetails.getChangeListeners().isEmpty()) {

                for (Class<?> changeListener : tableDetails.getChangeListeners()) {

                    TableDetails changeListenerDetails = database.getTableDetailsCache().findTableDetails(getContext(), changeListener);

                    if (changeListenerDetails == null)
                        continue;

                    Uri changeUri = uriMatcherHelper.generateItemUri(changeListenerDetails);
                    getContext().getContentResolver().notifyChange(changeUri, null, sync);
                }
            }
        }
        finally {
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
            if(column != null)
                notify = notify || column.notifyChanges();
        }

        return notify;
    }
}
