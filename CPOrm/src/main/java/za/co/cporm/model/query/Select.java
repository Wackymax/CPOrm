package za.co.cporm.model.query;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import za.co.cporm.model.CPHelper;
import za.co.cporm.model.generate.TableDetails;
import za.co.cporm.model.util.CPOrmCursor;
import za.co.cporm.model.util.ContentResolverValues;
import za.co.cporm.model.util.CursorIterator;
import za.co.cporm.provider.util.UriMatcherHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The starting point for select statements.  Contains the basic functions to do a simple select operation
 * and allows you to specify the result type you want for the query.
 */
public class Select<T> implements DataFilterClause{

    private final Class<T> dataObjectClass;
    private DataFilterCriteria filterCriteria;
    private List<String> sortingOrderList;

    private Select(Class<T> dataObjectClass){
        this.dataObjectClass = dataObjectClass;
        this.sortingOrderList = new LinkedList<String>();
        this.filterCriteria = new DataFilterCriteria();
    }

    public static <T> Select<T> from(Class<T> dataObjectClass){
        return new Select(dataObjectClass);
    }

    public Select<T> where(DataFilterClause filterClause){
        this.filterCriteria.addClause(filterClause);
        return this;
    }

    public Select<T> where(DataFilterClause filterClause, DataFilterClause.DataFilterConjunction conjunction){
        this.filterCriteria.addClause(filterClause, conjunction);
        return this;
    }

    public Select<T> whereEquals(String column, Object value){

        addClause(new DataFilterCriterion(column, DataFilterCriterion.DataFilterOperator.EQUAL, value), DataFilterConjunction.AND);
        return this;
    }

    public Select<T> whereLike(String column, Object value){

        addClause(new DataFilterCriterion(column, DataFilterCriterion.DataFilterOperator.LIKE, value), DataFilterConjunction.AND);
        return this;
    }

    public DataFilterCriterion.Builder<Select<T>> and(){
        return new DataFilterCriterion.Builder<Select<T>>(this, DataFilterClause.DataFilterConjunction.AND);
    }

    public DataFilterCriterion.Builder<Select<T>> or(){
        return new DataFilterCriterion.Builder<Select<T>>(this, DataFilterClause.DataFilterConjunction.OR);
    }

    public DataFilterCriteria.Builder<Select<T>> openBracketAnd(){
        return new DataFilterCriteria.Builder<Select<T>>(this, DataFilterClause.DataFilterConjunction.AND);
    }

    public DataFilterCriteria.Builder<Select<T>> openBracketOr(){
        return new DataFilterCriteria.Builder<Select<T>>(this, DataFilterClause.DataFilterConjunction.OR);
    }

    public Select sortDesc(String column){
        sortingOrderList.add(column + " DESC");
        return this;
    }

    public CPOrmCursor<T> queryAsCursor(Context context){
        TableDetails tableDetails = CPHelper.findTableDetails(context, dataObjectClass);

        QueryBuilder where = getWhereClause();
        QueryBuilder sort = buildSort();

        Uri itemUri = UriMatcherHelper.generateItemUri(context, tableDetails);
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(itemUri, null, where.getQueryString(), where.getQueryArgsAsArray(context), sort.getQueryString());

        return new CPOrmCursor<T>(tableDetails, cursor);
    }

    public Iterator<T> queryAsIterator(Context context){
        CPOrmCursor cursor = queryAsCursor(context);
        return new CursorIterator<T>(cursor.getTableDetails(), cursor);
    }

    public List<T> queryAsList(Context context){
        List<T> resultList = new ArrayList<T>();
        CPOrmCursor<T> cursor = queryAsCursor(context);

        try {
            while (cursor.moveToNext()) {
                resultList.add(cursor.inflate());
            }
        }
        finally {
            cursor.close();
        }

        return resultList;
    }

    public int queryAsCount(Context context){
        CPOrmCursor<T> cursor = queryAsCursor(context);

        try {
            return cursor.getCount();
        }
        finally {
            cursor.close();
        }
    }

    public ContentResolverValues asContentResolverValue(Context context){

        TableDetails tableDetails = CPHelper.findTableDetails(context, dataObjectClass);

        QueryBuilder where = getWhereClause();
        QueryBuilder sort = buildSort();

        Uri itemUri = UriMatcherHelper.generateItemUri(context, tableDetails);

        return new ContentResolverValues(itemUri, tableDetails.getColumnNames(), where.getQueryString(), where.getQueryArgsAsArray(context), sort.getQueryString());
    }

    private QueryBuilder buildSort(){

        QueryBuilder builder = new QueryBuilder();
        Iterator<String> sortIterator = sortingOrderList.iterator();

        while(sortIterator.hasNext()){

            builder.append(sortIterator.next());

            if(sortIterator.hasNext()) builder.append(", ");
        }

        return builder;
    }

    /**
     * The where clause for this query
     */
    @Override
    public QueryBuilder getWhereClause() {
        return filterCriteria.getWhereClause();
    }

    @Override
    public void addClause(DataFilterClause clause, DataFilterConjunction conjunction) {

        this.filterCriteria.addClause(clause, conjunction);
    }

    @Override
    public String toString() {
        return getWhereClause().toString();
    }
}
