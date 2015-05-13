package za.co.cporm.model;

import android.content.ContentProviderOperation;
import android.content.Context;

import java.util.Iterator;

/**
 * This class is just a wrapper for {@link CPOrm}, sub classes can extend this
 * to invoke the basic crud operations on the class itself.
 */
public abstract class CPRecord<T> {

    public Iterator<T> findAll() {

        return findAll(CPOrm.getApplicationContext());
    }

    public Iterator<T> findAll(Context context){
        return (Iterator<T>) CPOrm.findAll(context, getClass());
    }

    public T findByPrimaryKey(Object key){

        return findByPrimaryKey(CPOrm.getApplicationContext(), key);
    }

    public T findByPrimaryKey(Context context, Object key){
        return (T) CPOrm.findByPrimaryKey(context, getClass(), key);
    }

    public void insert() {

        insert(CPOrm.getApplicationContext());
    }

    public void insert(Context context){
        CPOrm.insert(context, this);
    }

    public ContentProviderOperation prepareInsert(){

        return prepareInsert(CPOrm.getApplicationContext());
    }

    public ContentProviderOperation prepareInsert(Context context) {
        return CPOrm.prepareInsert(context, this);
    }

    public T insertAndReturn(){

        return insertAndReturn(CPOrm.getApplicationContext());
    }

    public T insertAndReturn(Context context){
        return (T) CPOrm.insertAndReturn(context, this);
    }

    public void update() {

        update(CPOrm.getApplicationContext());
    }

    public void update(Context context){
        CPOrm.update(context, this);
    }

    public ContentProviderOperation prepareUpdate() {

        return prepareUpdate(CPOrm.getApplicationContext());
    }

    public ContentProviderOperation prepareUpdate(Context context) {
        return CPOrm.prepareUpdate(context, this);
    }

    public void delete() {

        delete(CPOrm.getApplicationContext());
    }

    public void delete(Context context){
        CPOrm.delete(context, this);
    }

    public ContentProviderOperation prepareDelete() {

        return prepareDelete(CPOrm.getApplicationContext());
    }

    public ContentProviderOperation prepareDelete(Context context) {
        return CPOrm.prepareDelete(context, this);
    }
}
