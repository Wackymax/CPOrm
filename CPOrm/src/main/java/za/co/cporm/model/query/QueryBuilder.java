package za.co.cporm.model.query;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class will keep track of the query and its supplied arguments , so that when multiple
 * queries are appended, the query and arguments will always match up.
 */
public class QueryBuilder implements Serializable{

    private final StringBuilder queryBuffer;
    private final List<Object> argsStore;

    public QueryBuilder() {

        queryBuffer = new StringBuilder();
        argsStore = new LinkedList<Object>();
    }

    public QueryBuilder(String init){

        queryBuffer = new StringBuilder(init);
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

    private Collection<Object> getQueryArgs(){

        List<Object> queryArgs = new LinkedList<Object>();
        queryArgs.addAll(argsStore);

        return Collections.unmodifiableCollection(queryArgs);
    }

    public String[] getQueryArgsAsArray(){

        return getQueryArgs().toArray(new String[]{});
    }

    @Override
    public String toString() {
        return getQueryString();
    }
}
