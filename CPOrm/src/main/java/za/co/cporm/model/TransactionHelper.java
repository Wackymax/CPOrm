package za.co.cporm.model;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;

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
import za.co.cporm.util.CPOrmLog;

/**
 * Created by hennie.brink on 2016/06/12.
 */
public class TransactionHelper {

    public static void saveInTransaction(Context context, List<? extends CPDefaultRecord> records) throws RemoteException, OperationApplicationException {

        List<ContentProviderOperation> operations = prepareTransaction(context, records);
        ContentProviderResult[] contentProviderResults = CPOrm.applyPreparedOperations(operations);

        Map<Class, Long> referenceIds = new HashMap<>();

        for (int i = 0; i < contentProviderResults.length; i++) {

            ContentProviderResult result = contentProviderResults[i];
            CPDefaultRecord source = records.get(i);

            referenceIds.remove(source.getClass());
            if(result.uri != null && source.getId() == null && ContentUris.parseId(result.uri) != -1){

                source.setId(ContentUris.parseId(result.uri));
                referenceIds.put(source.getClass(), source.getId());
            }

            try {
                applyReferenceResults(source.getClass(), source, referenceIds);
            } catch (IllegalAccessException e) {
                CPOrmLog.e("Failed to apply back reference id's for uri " + result.uri);
            }
        }
    }

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

    private static void applyReferenceResults(Class clazz, CPDefaultRecord source, Map<Class, Long> referenceMap) throws IllegalAccessException {

        for (Field field : clazz.getDeclaredFields()) {

            if(!field.isAnnotationPresent(Column.class))
                continue;

            if(field.isAnnotationPresent(References.class)) {

                Long referenceId = referenceMap.get(field.getAnnotation(References.class).value());
                if(referenceId != null){
                    field.setAccessible(true);
                    field.set(source, referenceId);
                }
            }
        }

        Class superclass = clazz.getSuperclass();
        if(superclass != null && CPDefaultRecord.class.isAssignableFrom(superclass)){

            applyReferenceResults(superclass, source, referenceMap);
        }
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
