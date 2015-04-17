package za.co.cporm.model.loader;

import android.content.Context;
import android.content.CursorLoader;
import za.co.cporm.model.generate.TableDetails;
import za.co.cporm.model.query.Select;
import za.co.cporm.model.util.CPOrmCursor;
import za.co.cporm.model.util.ContentResolverValues;

/**
 * A loaded implementation that will create a new Cursor loaded based on the select statement provided.
 */
public class CPOrmLoader<T> extends CursorLoader {

    private TableDetails tableDetails;
    private int cacheSize = -1;

    public CPOrmLoader(Context context, Select<T> select) {
        super(context);

        ContentResolverValues resolverValues = select.asContentResolverValue(context);
        setUri(resolverValues.getItemUri());
        setProjection(resolverValues.getProjection());
        setSelection(resolverValues.getWhere());
        setSelectionArgs(resolverValues.getWhereArgs());
        setSortOrder(resolverValues.getSortOrder());

        tableDetails = resolverValues.getTableDetails();
    }

    public CPOrmLoader(Context context, Select<T> select, int cacheSize) {
        this(context, select);

        enableCursorCache(cacheSize);
    }

    public void enableCursorCache(int size) {

        cacheSize = size;
    }

    @Override
    public CPOrmCursor loadInBackground() {

        return cacheSize == -1 ?
                new CPOrmCursor<T>(tableDetails, super.loadInBackground())
                : new CPOrmCursor<T>(tableDetails, super.loadInBackground(), cacheSize);
    }
}
