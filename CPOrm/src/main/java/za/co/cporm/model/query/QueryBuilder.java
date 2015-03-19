package za.co.cporm.model.query;

import android.content.Context;
import za.co.cporm.model.map.SqlColumnMappingFactory;
import za.co.cporm.model.util.ManifestHelper;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class will keep track of the query and its supplied arguments , so that when multiple
 * queries are appended, the query and arguments will always match up.
 */
public class QueryBuilder {

    private final StringBuilder queryBuffer;
    private final List<Object> argsStore;

    public QueryBuilder() {

        queryBuffer = new StringBuilder();
        argsStore = new LinkedList<Object>();
    }

    public void append(String query, Object... args) {

        queryBuffer.append(query);

        for (Object arg : args) {

            argsStore.add(String.valueOf(arg));
        }
    }

    public void append(QueryBuilder queryBuilder){

        queryBuffer.append(queryBuilder.getQueryString());
        argsStore.addAll(queryBuilder.getQueryArgs());
    }

    public String getQueryString(){

        StringBuilder queryString = new StringBuilder();
        queryString.append(queryBuffer);

        return queryString.toString();
    }

    public Collection<String> getQueryArgs(Context context){

        SqlColumnMappingFactory columnMappingFactory = ManifestHelper.getMappingFactory(context);
        List<String> queryArgs = new LinkedList<String>();

        for (Object arg : argsStore) {

            Object argConverted = columnMappingFactory.findColumnMapping(arg.getClass()).toSqlType(arg);
            queryArgs.add(String.valueOf(argConverted));
        }

        return Collections.unmodifiableCollection(queryArgs);
    }

    private Collection<Object> getQueryArgs(){

        List<Object> queryArgs = new LinkedList<Object>();
        queryArgs.addAll(argsStore);

        return Collections.unmodifiableCollection(queryArgs);
    }

    public String[] getQueryArgsAsArray(Context context){

        return getQueryArgs(context).toArray(new String[]{});
    }

    @Override
    public String toString() {
        return getQueryString();
    }
}
