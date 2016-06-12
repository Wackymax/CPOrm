package za.co.cporm.model;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.cporm.model.annotation.Column.Column;
import za.co.cporm.model.annotation.References;
import za.co.cporm.model.generate.TableDetails;
import za.co.cporm.model.util.CPOrmException;
import za.co.cporm.model.util.ModelInflater;
import za.co.cporm.provider.util.UriMatcherHelper;

/**
 * Created by hennie.brink on 2016/06/12.
 */
public class TransactionHelper {

    public static ArrayList<ContentProviderOperation> prepareTransaction(Context context, List<? extends CPDefaultRecord> records){

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        Map<Class, Integer> classIndex = new HashMap<>();
        String firstAuthority = null;

        for (int i = 0; i < records.size(); i++) {

            CPDefaultRecord cpDefaultRecord = records.get(i);
            TableDetails tableDetails = CPOrm.findTableDetails(context, cpDefaultRecord.getClass());

            if(firstAuthority == null){
                firstAuthority = tableDetails.getAuthority();
            }

            if(!tableDetails.getAuthority().equals(firstAuthority)){
                throw new CPOrmException(String.format("Cannot mix authorities in the same transaction. First authority is %s, second authority is %s", firstAuthority, tableDetails.getAuthority()));
            }

            ContentProviderOperation.Builder builder;
            if(cpDefaultRecord.getId() == null) {
                builder = newInsert(context, tableDetails, cpDefaultRecord);
                classIndex.put(cpDefaultRecord.getClass(), i);
            }
            else{
                builder = newUpdate(context, tableDetails, cpDefaultRecord);
                classIndex.remove(cpDefaultRecord.getClass());
            }

            applyReferences(tableDetails, builder, classIndex);

            operations.add(builder.build());
        }

        return operations;
    }

    private static ContentProviderOperation.Builder newInsert(Context context, TableDetails tableDetails, CPDefaultRecord cpDefaultRecord) {

        ContentValues contentValues = ModelInflater.deflate(tableDetails, cpDefaultRecord);
        Uri insertUri = UriMatcherHelper.generateItemUri(context, tableDetails).build();
        return ContentProviderOperation.newInsert(insertUri)
                .withValues(contentValues);
    }

    private static ContentProviderOperation.Builder newUpdate(Context context, TableDetails tableDetails, CPDefaultRecord cpDefaultRecord) {

        ContentValues contentValues = ModelInflater.deflate(tableDetails, cpDefaultRecord);
        Object columnValue = ModelInflater.deflateColumn(tableDetails, tableDetails.findPrimaryKeyColumn(), cpDefaultRecord);
        Uri itemUri = UriMatcherHelper.generateItemUri(context, tableDetails, String.valueOf(columnValue)).build();

        return ContentProviderOperation.newUpdate(itemUri)
                .withExpectedCount(1)
                .withValues(contentValues);
    }

    private static void applyReferences(TableDetails tableDetails, ContentProviderOperation.Builder source, Map<Class, Integer> referenceMap){

        for (TableDetails.ColumnDetails columnDetails : tableDetails.getColumns()) {

            if(columnDetails.getColumnField().isAnnotationPresent(References.class)) {
                backReferenceFromReferenceAnnotation(source, referenceMap, columnDetails);
            }
        }
    }

    private static void backReferenceFromReferenceAnnotation(ContentProviderOperation.Builder source, Map<Class, Integer> referenceMap, TableDetails.ColumnDetails columnDetails) {
        References reference = columnDetails.getColumnField().getAnnotation(References.class);

        if(!referenceMap.containsKey(reference.value()))
            return;

        String columnName = columnDetails.getColumnName();

        source.withValueBackReference(columnName, referenceMap.get(reference.value()));
    }
}
