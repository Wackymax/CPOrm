package za.co.cporm.model.util;

import android.net.Uri;

/**
 * Created by hennie.brink on 2015-03-19.
 */
public class ContentResolverValues {

    private final Uri itemUri;
    private final String[] projection;
    private final String where;
    private final String[] whereArgs;
    private final String sortOrder;

    public ContentResolverValues(Uri itemUri, String[] projection, String where, String[] whereArgs, String sortOrder) {
        this.itemUri = itemUri;
        this.projection = projection;
        this.where = where;
        this.whereArgs = whereArgs;
        this.sortOrder = sortOrder;
    }

    public Uri getItemUri() {
        return itemUri;
    }

    public String[] getProjection() {
        return projection;
    }

    public String getWhere() {
        return where;
    }

    public String[] getWhereArgs() {
        return whereArgs;
    }

    public String getSortOrder() {
        return sortOrder;
    }
}
