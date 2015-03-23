package za.co.cporm.model.util;

import android.net.Uri;
import za.co.cporm.model.generate.TableDetails;

/**
 * Created by hennie.brink on 2015-03-19.
 */
public class ContentResolverValues {

    private final TableDetails tableDetails;
    private final Uri itemUri;
    private final String[] projection;
    private final String where;
    private final String[] whereArgs;
    private final String sortOrder;

    public ContentResolverValues(TableDetails tableDetails, Uri itemUri, String[] projection, String where, String[] whereArgs, String sortOrder) {
        this.tableDetails = tableDetails;
        this.itemUri = itemUri;
        this.projection = projection;
        this.where = where;
        this.whereArgs = whereArgs;
        this.sortOrder = sortOrder;
    }

    public TableDetails getTableDetails() {

        return tableDetails;
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
