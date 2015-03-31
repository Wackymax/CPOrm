package za.co.cporm.model.loader.support;

import android.content.Context;
import android.support.v4.content.CursorLoader;
import za.co.cporm.model.generate.TableDetails;
import za.co.cporm.model.query.Select;
import za.co.cporm.model.util.CPOrmCursor;
import za.co.cporm.model.util.ContentResolverValues;

/**
 * Created by hennie.brink on 2015-03-31.
 */
public class CPOrmLoader<T> extends CursorLoader {

    private TableDetails tableDetails;

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

    @Override
    public CPOrmCursor loadInBackground() {

        return new CPOrmCursor<T>(tableDetails, super.loadInBackground());
    }
}
