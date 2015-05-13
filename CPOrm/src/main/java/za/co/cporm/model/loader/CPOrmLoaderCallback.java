package za.co.cporm.model.loader;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.CursorAdapter;
import za.co.cporm.model.query.Select;

/**
 * Created by hennie.brink on 2015-05-11.
 */
public class CPOrmLoaderCallback<T> implements LoaderManager.LoaderCallbacks<Cursor> {

    private final Context context;
    private final CursorAdapter listAdapter;
    private final Select<T> select;

    public CPOrmLoaderCallback(Context context, CursorAdapter listAdapter, Select<T> select) {

        this.context = context;
        this.listAdapter = listAdapter;
        this.select = select;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CPOrmLoader<T>(context, select);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        listAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        listAdapter.changeCursor(null);
    }
}
