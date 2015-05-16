package za.co.cporm.model.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;
import za.co.cporm.util.CPOrmLog;

/**
 * Created by hennie.brink on 2015-03-18.
 */
public class CPOrmCursorFactory implements SQLiteDatabase.CursorFactory {

    private final boolean debugEnabled;

    public CPOrmCursorFactory(TableDetailsCache tableDetailCache) {
        this(false);
    }

    public CPOrmCursorFactory(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    @Override
    public Cursor newCursor(SQLiteDatabase sqLiteDatabase, SQLiteCursorDriver sqLiteCursorDriver, String tableName, SQLiteQuery sqLiteQuery) {

        if (debugEnabled) {
            CPOrmLog.d(sqLiteQuery.toString());
        }

        return new SQLiteCursor(sqLiteCursorDriver, tableName, sqLiteQuery);
    }
}
