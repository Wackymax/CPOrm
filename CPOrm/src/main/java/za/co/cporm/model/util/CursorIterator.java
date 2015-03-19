package za.co.cporm.model.util;

import android.database.Cursor;
import za.co.cporm.model.generate.TableDetails;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The iterator will just iterator over a cursor, it does that by checking in the has next method
 * if the cursor is open and not after the last item.  If the cursor is can fetch a next item, that item is returned in next,
 * if the returned item is the last one, the cursor is automatically closed.
 */
public class CursorIterator<T> implements Iterator<T> {
    private final TableDetails tableDetails;
    private final Cursor cursor;

    public CursorIterator(TableDetails tableDetails, Cursor cursor) {
        this.tableDetails = tableDetails;
        this.cursor = cursor;
    }

    @Override
    public boolean hasNext() {
        return cursor != null && !cursor.isClosed() && !cursor.isAfterLast();
    }

    @Override
    public T next() {
        T entity = null;
        if (cursor == null || cursor.isAfterLast()) {
            throw new NoSuchElementException();
        }

        if (cursor.isBeforeFirst()) {
            cursor.moveToFirst();
        }

        try {

            entity = ModelInflater.inflate(cursor, tableDetails);
        }
        finally {
            cursor.moveToNext();
            if (cursor.isAfterLast()) {
                cursor.close();
            }
        }

        return entity;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
