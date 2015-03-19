package za.co.cporm.model.util;

import android.database.Cursor;
import android.database.CursorWrapper;
import za.co.cporm.model.generate.TableDetails;

/**
 * Created by hennie.brink on 2015-03-18.
 */
public class CPOrmCursor<T> extends CursorWrapper {

    private final TableDetails tableDetails;

    public CPOrmCursor(TableDetails tableDetails, Cursor cursor) {
        super(cursor);
        this.tableDetails = tableDetails;
    }

    public T inflate(){
        return ModelInflater.inflate(this, tableDetails);
    }

    public TableDetails getTableDetails() {
        return tableDetails;
    }
}
