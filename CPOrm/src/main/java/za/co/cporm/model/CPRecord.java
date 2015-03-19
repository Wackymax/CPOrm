package za.co.cporm.model;

import android.content.Context;

import java.util.Iterator;

/**
 * This class is just a wrapper for {@link za.co.cporm.model.CPHelper}, sub classes can extend this
 * to invoke the basic crud operations on the class itself.
 */
public abstract class CPRecord<T> {

    public long countAll(Context context){
        return CPHelper.countAll(context, getClass());
    }

    public Iterator<T> findAll(Context context){
        return (Iterator<T>)CPHelper.findAll(context, getClass());
    }

    public T findByPrimaryKey(Context context,  Object key){
        return (T)CPHelper.findByPrimaryKey(context, getClass(), key);
    }

    public void insert(Context context){
        CPHelper.insert(context, this);
    }

    public T insertAndReturn(Context context, T dataModelObject){
        return (T)CPHelper.insertAndReturn(context, this);
    }

    public void update(Context context){
        CPHelper.update(context, this);
    }

    public void delete(Context context){
        CPHelper.delete(context, this);
    }
}
