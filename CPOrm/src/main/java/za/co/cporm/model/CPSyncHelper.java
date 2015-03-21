package za.co.cporm.model;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;
import za.co.cporm.model.generate.TableDetails;
import za.co.cporm.model.util.ModelInflater;
import za.co.cporm.provider.CPOrmContentProvider;
import za.co.cporm.provider.util.UriMatcherHelper;

/**
 * This class contains basic helper functions to assist with data synchronization.  What
 * differentiates this class from {@link za.co.cporm.model.CPHelper} is that all
 * operations will be passed with a special parameter telling the content provider
 * that a network sync should not be scheduled when notifying content observers.
 */
public class CPSyncHelper {

    public static <T> void insert(Context context, ContentProviderClient provider, T dataModelObject) throws RemoteException {
        TableDetails tableDetails = CPHelper.findTableDetails(context, dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Uri insertUri = UriMatcherHelper.generateItemUri(context, tableDetails)
                .appendQueryParameter(CPOrmContentProvider.PARAMETER_SYNC, "false").build();

        Uri itemUri = provider.insert(insertUri, contentValues);
    }

    public static <T> T insertAndReturn(Context context, ContentProviderClient provider, T dataModelObject) throws RemoteException {
        TableDetails tableDetails = CPHelper.findTableDetails(context, dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Uri insertUri = UriMatcherHelper.generateItemUri(context, tableDetails)
                .appendQueryParameter(CPOrmContentProvider.PARAMETER_SYNC, "false").build();

        Uri itemUri = provider.insert(insertUri, contentValues);

        return CPHelper.findSingleItem(context, itemUri, tableDetails);
    }

    public static <T> void update(Context context, ContentProviderClient provider, T dataModelObject) throws RemoteException {
        TableDetails tableDetails = CPHelper.findTableDetails(context, dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Object columnValue = contentValues.get(tableDetails.findPrimaryKeyColumn().getColumnName());
        Uri itemUri = UriMatcherHelper.generateItemUri(context, tableDetails, String.valueOf(columnValue))
                .appendQueryParameter(CPOrmContentProvider.PARAMETER_SYNC, "false").build();

        provider.update(itemUri, contentValues, null, null);
    }

    public static <T> void delete(Context context, ContentProviderClient provider, T dataModelObject) throws RemoteException {
        TableDetails tableDetails = CPHelper.findTableDetails(context, dataModelObject.getClass());
        ContentValues contentValues = ModelInflater.deflate(tableDetails, dataModelObject);
        Object columnValue = contentValues.get(tableDetails.findPrimaryKeyColumn().getColumnName());
        Uri itemUri = UriMatcherHelper.generateItemUri(context, tableDetails, String.valueOf(columnValue))
                .appendQueryParameter(CPOrmContentProvider.PARAMETER_SYNC, "false").build();

        provider.delete(itemUri, null, null);
    }
}
