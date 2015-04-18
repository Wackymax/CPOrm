package za.co.cporm.model.util;

import android.database.Cursor;
import android.database.SQLException;
import za.co.cporm.model.generate.TableDetails;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The iterator will just iterator over a cursor, it does that by checking in the has next method
 * if the cursor is open and not after the last item.  If the cursor is can fetch a next item, that item is returned in next,
 * if the returned item is the last one, the cursor is automatically closed.
 */
public class CursorIterator<T> implements Iterator<T>, Closeable {
    private final TableDetails tableDetails;
    private final Cursor cursor;

    public CursorIterator(TableDetails tableDetails, Cursor cursor) {
        this.tableDetails = tableDetails;
        this.cursor = cursor;
    }

    @Override
    public boolean hasNext() {

        if(cursor != null && cursor.isAfterLast())
            cursor.close();//Close the cursor if we reached the last position

        return cursor != null && !cursor.isClosed() && !cursor.isAfterLast();
    }

    @Override
    public T next() {
        T entity = null;
        if (cursor == null || cursor.isAfterLast()) {
            throw new NoSuchElementException();
        }

        try {
            if (cursor.isBeforeFirst()) {
                cursor.moveToFirst();
            }

            try {

                entity = ModelInflater.inflate(cursor, tableDetails);
            } finally {
                cursor.moveToNext();
            }
        }
        catch (SQLException sqle) {

            cursor.close();
            throw sqle;
        }

        return entity;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {

        if(!cursor.isClosed())
            cursor.close();
    }
}
