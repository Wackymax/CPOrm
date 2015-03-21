package za.co.cporm.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
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
    public static final String PARAMETER_SYNC = "SYNC";

    private CPOrmDatabase database;
    private UriMatcherHelper uriMatcherHelper;
    private boolean debugEnabled;

    @Override
    public boolean onCreate() {
        debugEnabled = ManifestHelper.getDebugEnabled(getContext());
        database = new CPOrmDatabase(getContext(), debugEnabled);
        uriMatcherHelper = new UriMatcherHelper(getContext());
        uriMatcherHelper.init(getContext(), database.getModelFactory(), database.getTableDetailsCache());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        TableDetails tableDetails = uriMatcherHelper.getTableDetails(uri);
        SQLiteDatabase db = database.getReadableDatabase();
        String limit = constructLimit(uri);

        if(debugEnabled){
            Log.d(TAG, "********* Query **********");
            Log.d(TAG, "Uri: " + uri);
            Log.d(TAG, "Projection: " + Arrays.toString(projection));
            Log.d(TAG, "Selection: " + selection);
            Log.d(TAG, "Args: " + Arrays.toString(selectionArgs));
            Log.d(TAG, "Sort: " + sortOrder);
            Log.d(TAG, "Limit: " + limit);
        }

        if(uriMatcherHelper.isSingleItemRequested(uri)){

            String itemId = uri.getLastPathSegment();
            TableDetails.ColumnDetails primaryKeyColumn = tableDetails.findPrimaryKeyColumn();
            return db.query(tableDetails.getTableName(), tableDetails.getColumnNames(), primaryKeyColumn.getColumnName() + " = ?", new String[]{itemId}, null, null, null);
        }
        else return db.query(tableDetails.getTableName(), projection, selection, selectionArgs, null, null, sortOrder, limit);
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

        if(debugEnabled){
            Log.d(TAG, "********* Insert **********");
            Log.d(TAG, "Uri: " + uri);
            Log.d(TAG, "Content Values: " + contentValues);
        }

        long insertId = db.insert(tableDetails.getTableName(), null, contentValues);

        getContext().getContentResolver().notifyChange(uri, null, sync);

        TableDetails.ColumnDetails primaryKeyColumn = tableDetails.findPrimaryKeyColumn();
        if(primaryKeyColumn.isAutoIncrement()) return uriMatcherHelper.generateSingleItemUri(tableDetails, String.valueOf(insertId));
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

        if(debugEnabled){
            Log.d(TAG, "********* Delete **********");
            Log.d(TAG, "Uri: " + uri);
            Log.d(TAG, "Where: " + where);
            Log.d(TAG, "Args: " + Arrays.toString(args));
        }

        int deleteCount = 0;
        if(uriMatcherHelper.isSingleItemRequested(uri)){

            String itemId = uri.getLastPathSegment();
            TableDetails.ColumnDetails primaryKeyColumn = tableDetails.findPrimaryKeyColumn();
            deleteCount = db.delete(tableDetails.getTableName(), primaryKeyColumn.getColumnName() + " = ?", new String[]{itemId});
        }
        deleteCount = db.delete(tableDetails.getTableName(), where, args);

        if(deleteCount > 0) getContext().getContentResolver().notifyChange(uri, null, sync);

        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String where, String[] args) {

        TableDetails tableDetails = uriMatcherHelper.getTableDetails(uri);
        SQLiteDatabase db = database.getWritableDatabase();
        Boolean sync = uri.getBooleanQueryParameter(PARAMETER_SYNC, true);

        if(debugEnabled){
            Log.d(TAG, "********* Update **********");
            Log.d(TAG, "Uri: " + uri);
            Log.d(TAG, "Content Values: " + contentValues);
            Log.d(TAG, "Where: " + where);
            Log.d(TAG, "Args: " + Arrays.toString(args));
        }

        int updateCount = 0;
        if(uriMatcherHelper.isSingleItemRequested(uri)){

            String itemId = uri.getLastPathSegment();
            TableDetails.ColumnDetails primaryKeyColumn = tableDetails.findPrimaryKeyColumn();
            updateCount = db.update(tableDetails.getTableName(), contentValues, primaryKeyColumn.getColumnName() + " = ?", new String[]{itemId});
        }
        else updateCount = db.update(tableDetails.getTableName(), contentValues, where, args);

        if(updateCount > 0) getContext().getContentResolver().notifyChange(uri, null, sync);

        return updateCount;
    }

    private String constructLimit(Uri uri){

        String offsetParam = uri.getQueryParameter(PARAMETER_OFFSET);
        String limitParam = uri.getQueryParameter(PARAMETER_LIMIT);

        Integer offset = null;
        Integer limit = null;

        if(!TextUtils.isEmpty(offsetParam) && TextUtils.isDigitsOnly(offsetParam)){
            offset = Integer.valueOf(offsetParam);
        }
        if(!TextUtils.isEmpty(limitParam) && TextUtils.isDigitsOnly(limitParam)){
            limit = Integer.valueOf(limitParam);
        }

        if(limit == null && offset == null)
            return null;

        StringBuilder limitStatement = new StringBuilder();

        if(limit != null && offset != null)
        {
            limitStatement.append(" LIMIT ");
            limitStatement.append(offset);
            limitStatement.append(", ");
            limitStatement.append(limit);
        }
        else if(limit != null)
        {
            limitStatement.append(" LIMIT ");
            limitStatement.append(limit);
        }
        else
        {
            limitStatement.append(" OFFSET ");
            limitStatement.append(offset);
        }

        return limitStatement.toString();
    }
}
