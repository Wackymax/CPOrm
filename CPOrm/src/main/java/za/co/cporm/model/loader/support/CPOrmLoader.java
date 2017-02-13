package za.co.cporm.model.loader.support;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import za.co.cporm.model.generate.TableDetails;
import za.co.cporm.model.query.Select;
import za.co.cporm.model.util.CPOrmCursor;
import za.co.cporm.model.util.ContentResolverValues;

/**
 * Created by hennie.brink on 2015-03-31.
 */
public class CPOrmLoader<Model> extends CursorLoader {

    private TableDetails tableDetails;
    private int cacheSize = 0;

    /**
     * Creates a new cursor loader using the select statement provided. The default implementation
     * will enable the cache of the cursor to improve view performance.  To manually specify the
     * cursor cache size, use the overloaded constructor.
     * @param context The context that will be used to create the cursor.
     * @param select The select statement that will be used to retrieve the data.
     */
    public CPOrmLoader(Context context, Select<Model> select) {
        super(context);

        ContentResolverValues resolverValues = select.asContentResolverValue(context);
        setUri(resolverValues.getItemUri());
        setProjection(resolverValues.getProjection());
        setSelection(resolverValues.getWhere());
        setSelectionArgs(resolverValues.getWhereArgs());
        setSortOrder(resolverValues.getSortOrder());

        tableDetails = resolverValues.getTableDetails();
    }

    /**
     * Creates a new cursor loader using the select statement provided. You
     * can specify the cache size to use, or use -1 to disable cursor caching.
     * @param context The context that will be used to create the cursor.
     * @param select The select statement that will be used to retrieve the data.
     * @param cacheSize The cache size for the cursor, or -1 to disable caching
     */
    public CPOrmLoader(Context context, Select<Model> select, int cacheSize) {
        this(context, select);

        enableCursorCache(cacheSize);
    }

    public void enableCursorCache(int size) {

        cacheSize = size;
    }

    @Override
    public CPOrmCursor<Model> loadInBackground() {

        Cursor asyncCursor = super.loadInBackground();
        if(asyncCursor == null)
            return null;

        CPOrmCursor<Model> cursor = new CPOrmCursor<>(tableDetails, asyncCursor);

        if(cacheSize == 0){
            cursor.enableCache();
        } else if(cacheSize > 0) {
            cursor.enableCache(cacheSize);
        }

        //Prefetch at least some items in preparation for the list
        int count = cursor.getCount();
        for (int i = 0; i < count && cursor.isCacheEnabled() && i < 100; i++) {

            cursor.moveToPosition(i);
            Model inflate = cursor.inflate();
        }
        return cursor;
    }
}
