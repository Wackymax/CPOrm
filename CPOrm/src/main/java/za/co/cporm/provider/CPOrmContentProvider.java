package za.co.cporm.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import za.co.cporm.model.CPOrmDatabase;
import za.co.cporm.model.generate.TableDetails;
import za.co.cporm.provider.util.UriMatcherHelper;

/**
 * The base content provided that will expose all of the model objects.
 * Objects are expose in the form of authority/table_name/*
 */
public class CPOrmContentProvider extends ContentProvider {

    private CPOrmDatabase database;
    private UriMatcherHelper uriMatcherHelper;

    @Override
    public boolean onCreate() {
        database = new CPOrmDatabase(getContext());
        uriMatcherHelper = new UriMatcherHelper(getContext());
        uriMatcherHelper.init(getContext(), database.getModelFactory(), database.getTableDetailsCache());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        TableDetails tableDetails = uriMatcherHelper.getTableDetails(uri);
        SQLiteDatabase db = database.getReadableDatabase();

        if(uriMatcherHelper.isSingleItemRequested(uri)){

            String itemId = uri.getLastPathSegment();
            TableDetails.ColumnDetails primaryKeyColumn = tableDetails.findPrimaryKeyColumn();
            return db.query(tableDetails.getTableName(), tableDetails.getColumnNames(), primaryKeyColumn.getColumnName() + " = ?", new String[]{itemId}, null, null, null);
        }
        else return db.query(tableDetails.getTableName(), projection, selection, selectionArgs, null, sortOrder, null);
    }

    @Override
    public String getType(Uri uri) {
        return uriMatcherHelper.getType(uri);
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        TableDetails tableDetails = uriMatcherHelper.getTableDetails(uri);
        SQLiteDatabase db = database.getWritableDatabase();

        long insertId = db.insert(tableDetails.getTableName(), null, contentValues);

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

        if(uriMatcherHelper.isSingleItemRequested(uri)){

            String itemId = uri.getLastPathSegment();
            TableDetails.ColumnDetails primaryKeyColumn = tableDetails.findPrimaryKeyColumn();
            return db.delete(tableDetails.getTableName(), primaryKeyColumn.getColumnName() + " = ?", new String[]{itemId});
        }
        return db.delete(tableDetails.getTableName(), where, args);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String where, String[] args) {

        TableDetails tableDetails = uriMatcherHelper.getTableDetails(uri);
        SQLiteDatabase db = database.getWritableDatabase();

        if(uriMatcherHelper.isSingleItemRequested(uri)){

            String itemId = uri.getLastPathSegment();
            TableDetails.ColumnDetails primaryKeyColumn = tableDetails.findPrimaryKeyColumn();
            return db.update(tableDetails.getTableName(), contentValues, primaryKeyColumn.getColumnName() + " = ?", new String[]{itemId});
        }
        else return db.update(tableDetails.getTableName(), contentValues, where, args);
    }
}
