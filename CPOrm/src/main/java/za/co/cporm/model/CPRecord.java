package za.co.cporm.model;

import android.content.ContentProviderOperation;
import android.content.Context;

import java.util.Iterator;

/**
 * This class is just a wrapper for {@link za.co.cporm.model.CPHelper}, sub classes can extend this
 * to invoke the basic crud operations on the class itself.
 */
public abstract class CPRecord<T> {

    public long countAll(){

        return countAll(CPHelper.getApplicationContext());
    }

    public long countAll(Context context){
        return CPHelper.countAll(context, getClass());
    }

    public Iterator<T> findAll() {

        return findAll(CPHelper.getApplicationContext());
    }

    public Iterator<T> findAll(Context context){
        return (Iterator<T>)CPHelper.findAll(context, getClass());
    }

    public T findByPrimaryKey(Object key){

        return findByPrimaryKey(CPHelper.getApplicationContext(), key);
    }

    public T findByPrimaryKey(Context context, Object key){
        return (T)CPHelper.findByPrimaryKey(context, getClass(), key);
    }

    public void insert() {

        insert(CPHelper.getApplicationContext());
    }

    public void insert(Context context){
        CPHelper.insert(context, this);
    }

    public ContentProviderOperation prepareInsert(){

        return prepareInsert(CPHelper.getApplicationContext());
    }

    public ContentProviderOperation prepareInsert(Context context) {
        return CPHelper.prepareInsert(context, this);
    }

    public T insertAndReturn(){

        return insertAndReturn(CPHelper.getApplicationContext());
    }

    public T insertAndReturn(Context context){
        return (T)CPHelper.insertAndReturn(context, this);
    }

    public void update() {

        update(CPHelper.getApplicationContext());
    }

    public void update(Context context){
        CPHelper.update(context, this);
    }

    public ContentProviderOperation prepareUpdate() {

        return prepareUpdate(CPHelper.getApplicationContext());
    }

    public ContentProviderOperation prepareUpdate(Context context) {
        return CPHelper.prepareUpdate(context, this);
    }

    public void delete() {

        delete(CPHelper.getApplicationContext());
    }

    public void delete(Context context){
        CPHelper.delete(context, this);
    }

    public ContentProviderOperation prepareDelete() {

        return prepareDelete(CPHelper.getApplicationContext());
    }

    public ContentProviderOperation prepareDelete(Context context) {
        return CPHelper.prepareDelete(context, this);
    }
}
