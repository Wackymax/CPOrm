package za.co.cporm.model.loader.support;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import za.co.cporm.model.util.CPOrmCursor;

/**
 *  A cursor adaptor that will automatically handle view and view holder creation.
 *  Extend this class and implement the abstract methods.
 *  T = Domain Model Object
 *  K = View Holder Class
 */
public abstract class CPOrmCursorAdaptor<T, K> extends CursorAdapter {

    private final int layoutId;

    public CPOrmCursorAdaptor(Context context, Cursor c, int layoutId, int flags) {

        super(context, c, flags);
        this.layoutId = layoutId;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {

        View view = LayoutInflater.from(context).inflate(layoutId, viewGroup, false);
        K viewHolder = createViewHolder(view);
        view.setTag(viewHolder);

        bindView(view, context, cursor);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        K viewHolder = (K) view.getTag();
        setViewInformation(viewHolder, ((CPOrmCursor<T>)cursor).inflate());
    }

    public abstract K createViewHolder(View view);

    public abstract void setViewInformation(K viewHolder, T information);

    @Override
    public void changeCursor(Cursor cursor) {

        if(cursor instanceof CPOrmCursor || cursor == null){
            super.changeCursor(cursor);
        }
        else throw new IllegalArgumentException("The cursor is not of the instance " + CPOrmCursor.class.getSimpleName());
    }
}
