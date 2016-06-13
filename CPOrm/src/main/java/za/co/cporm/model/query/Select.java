package za.co.cporm.model.query;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import za.co.cporm.model.CPOrm;
import za.co.cporm.model.generate.TableDetails;
import za.co.cporm.model.map.SqlColumnMappingFactory;
import za.co.cporm.model.util.CPOrmCursor;
import za.co.cporm.model.util.ContentResolverValues;
import za.co.cporm.model.util.CursorIterator;
import za.co.cporm.model.util.ManifestHelper;
import za.co.cporm.provider.CPOrmContentProvider;
import za.co.cporm.provider.util.UriMatcherHelper;

import java.util.*;

/**
 * The starting point for select statements.  Contains the basic functions to do a simple select operation
 * and allows you to specify the result type you want for the query.
 */
public class Select<Model> implements DataFilterClause<Select<Model>> {

    private final Class<Model> dataObjectClass;
    private DataFilterCriteria filterCriteria;
    private List<String> sortingOrderList;
    private List<String> includedColumns;
    private List<String> excludedColumns;
    private Integer offset;
    private Integer limit;
    private boolean distinct = false;
    private String groupBy;
    private String having;

    private Select(Class<Model> dataObjectClass) {

        this.dataObjectClass = dataObjectClass;
        this.sortingOrderList = new LinkedList<String>();
        this.filterCriteria = new DataFilterCriteria();
        this.includedColumns = new ArrayList<String>();
        this.excludedColumns = new ArrayList<String>();
    }

    /**
     * The data model object that will be selected from
     *
     * @param dataObjectClass The class object
     * @param <T>             The generic type telling java the type of class
     * @return The current Select instance
     */
    public static <T> Select<T> from(Class<T> dataObjectClass) {

        return new Select<T>(dataObjectClass);
    }

    public Class<Model> getTableModel() {
        return this.dataObjectClass;
    }

    public TableDetails getTableDetails() {
        return CPOrm.findTableDetails(CPOrm.getApplicationContext(), this.dataObjectClass);
    }

    public TableDetails getTableDetails(Context context) {
        return CPOrm.findTableDetails(context, this.dataObjectClass);
    }

    /**
     * Calling this method will execute the query with a distinct clause
     */
    public Select<Model> distinct() {
        this.distinct = true;

        return this;
    }

    /**
     * The filter clause that will be used to apply filtering, each clause will be added with ann AND conjunction
     *
     * @param filterClause the filter clause to add
     * @return The current select instance
     */
    public Select<Model> where(DataFilterClause filterClause) {

        this.filterCriteria.addClause(filterClause);
        return this;
    }

    /**
     * The filter clause that will be used to apply filtering, adds the clause with the specified conjunction
     *
     * @param filterClause The filter clause to add
     * @param conjunction  The conjunction used to add the filter clause
     * @return The current select instance
     */
    public Select<Model> where(DataFilterClause filterClause, DataFilterClause.DataFilterConjunction conjunction) {

        this.filterCriteria.addClause(filterClause, conjunction);
        return this;
    }

    /**
     * Convenience method that will add a equals criterion with an AND conjunction
     *
     * @param column The column to compare
     * @param value  The value to compare
     * @return The current select instance
     */
    public Select<Model> whereEquals(String column, Object value) {

        addClause(new DataFilterCriterion(column, DataFilterCriterion.DataFilterOperator.EQUAL, value), DataFilterConjunction.AND);
        return this;
    }

    /**
     * Convenience method that will add a like criterion with an AND conjunction
     *
     * @param column The column to compare
     * @param value  The value to compare
     * @return The current select instance
     */
    public Select<Model> whereLike(String column, Object value) {

        addClause(new DataFilterCriterion(column, DataFilterCriterion.DataFilterOperator.LIKE, value), DataFilterConjunction.AND);
        return this;
    }

    /**
     * Starts a new Criterion builder with and AND conjunction
     *
     * @return The current select instance
     */
    public DataFilterCriterion.Builder<Select<Model>> and() {

        return new DataFilterCriterion.Builder<Select<Model>>(this, DataFilterClause.DataFilterConjunction.AND);
    }

    /**
     * Adds the specified clause with and AND conjunction
     *
     * @param clause The clause to add
     * @return The current select instance
     */
    public Select<Model> and(DataFilterClause clause) {

        filterCriteria.addClause(clause, DataFilterConjunction.AND);
        return this;
    }

    /**
     * Starts a new Criterion builder with an OR conjunction
     *
     * @return The current select instance
     */
    public DataFilterCriterion.Builder<Select<Model>> or() {

        return new DataFilterCriterion.Builder<Select<Model>>(this, DataFilterClause.DataFilterConjunction.OR);
    }

    /**
     * Adds the specified clause with an OR conjunction
     *
     * @param filterClause The clause to add
     * @return The current select instance
     */
    public Select<Model> or(DataFilterClause filterClause) {

        filterCriteria.addClause(filterClause, DataFilterConjunction.OR);
        return this;
    }

    /**
     * Starts a new Criteria builder with AND conjunction
     *
     * @return The current select instance
     */
    public DataFilterCriteria.Builder<Select<Model>> openBracketAnd() {

        return new DataFilterCriteria.Builder<Select<Model>>(this, DataFilterClause.DataFilterConjunction.AND);
    }

    /**
     * Starts a new Criteria builder with OR conjunction
     *
     * @return The current select instance
     */
    public DataFilterCriteria.Builder<Select<Model>> openBracketOr() {

        return new DataFilterCriteria.Builder<Select<Model>>(this, DataFilterClause.DataFilterConjunction.OR);
    }

    /**
     * Sorts the specified columns in DESC order.  The order here is important, as the sorting will be done in the same order the columns are added.
     *
     * @param columns the columns to sort
     * @return The current select instance
     */
    public Select<Model> sortDesc(String... columns) {

        for (String column : columns) {
            sortingOrderList.add(column + " DESC");
        }
        return this;
    }

    /**
     * Sorts the specified columns in ASC order.  The order here is important, as the sorting will be done in the same order the columns are added.
     *
     * @param columns the columns to sort
     * @return The current select instance
     */
    public Select<Model> sortAsc(String... columns) {

        for (String column : columns) {
            sortingOrderList.add(column + " ASC");
        }
        return this;
    }

    /**
     * Sets the offset of rows from which the select will start executing
     *
     * @param offset the row offset
     * @return The current select instance
     */
    public Select<Model> offset(int offset) {

        if (offset < 0)
            throw new IllegalArgumentException("Offset must be larger than 0");
        this.offset = offset;
        return this;
    }

    /**
     * Sets the limit of how many rows will be selected
     *
     * @param limit Amount of rows
     * @return The current select statement
     */
    public Select<Model> limit(int limit) {

        if (limit < 1)
            throw new IllegalArgumentException("Limit must be larger than 0");
        this.limit = limit;
        return this;
    }

    /**
     * Columns to retrieve, if not specified all columns will be retrieved.  Remember, the inflated object will only contain valid values for the selected columns.
     * If this is specified the excluded columns will be ignored
     *
     * @param columns The columns to retrieve
     * @return The current select instance
     */
    public Select<Model> include(String... columns) {

        Collections.addAll(includedColumns, columns);

        return this;
    }

    /**
     * Columns not to retrieve.  If not specified all columns will be retrieved.  Remember, the inflated object will not contain valid values for the specified columns.
     * If the include was specified as part of the select, excluded columns are ignored.
     *
     * @param columns The columns to exclude
     * @return The current select instance
     */
    public Select<Model> exclude(String... columns) {

        Collections.addAll(excludedColumns, columns);

        return this;
    }

    /**
     * The group by clause to use
     */
    public Select<Model> groupBy(String groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    /**
     * The group by clause to use
     */
    public Select<Model> having(String having) {
        this.having = having;
        return this;
    }

    /**
     * @see #queryAsCursor(Context)
     */
    public CPOrmCursor<Model> queryAsCursor() {

        return queryAsCursor(CPOrm.getApplicationContext());
    }

    /**
     * Executes the query and returns the results as a cursor. The {@link za.co.cporm.model.util.CPOrmCursor} is a wrapper for the normal cursor,
     * and in addition to providing the normal cursor functionality, it also has methods to manipulate model objects, such as inflating the current cursor
     * values to a model object.
     *
     * @return The {@link za.co.cporm.model.util.CPOrmCursor} containing the results
     */
    public CPOrmCursor<Model> queryAsCursor(Context context) {

        ContentResolverValues contentResolverValues = asContentResolverValue(context);
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(contentResolverValues.getItemUri(),
                contentResolverValues.getProjection(),
                contentResolverValues.getWhere(),
                contentResolverValues.getWhereArgs(),
                contentResolverValues.getSortOrder());

        return cursor == null ? null : new CPOrmCursor<Model>(contentResolverValues.getTableDetails(), cursor);
    }

    /**
     * @see #queryAsIterator(Context)
     */
    public CursorIterator<Model> queryAsIterator() {

        return queryAsIterator(CPOrm.getApplicationContext());
    }

    /**
     * Does the same as query cursor, but wraps the cursor in an iterator, and inflates the objects automatically. This iterator will close the cursor
     * once all of the objects have been retrieved, so it is important to always iterate over all the objects so the cursor can close. If an exception
     * is thrown that will prevent this iterator from completing, then please close it manually.
     *
     * @return The iterator containing the results
     */
    public CursorIterator<Model> queryAsIterator(Context context) {

        CPOrmCursor<Model> cursor = queryAsCursor(context);
        return cursor == null ? null : new CursorIterator<Model>(cursor.getTableDetails(), cursor);
    }

    /**
     * @see #queryAsList(Context)
     */
    public List<Model> queryAsList() {

        return queryAsList(CPOrm.getApplicationContext());
    }

    /**
     * Does the same as the query cursor, but packs all of the cursor items into a list, once the list is populated, the cursor will be closed.
     *
     * @return The list containing the results
     */
    public List<Model> queryAsList(Context context) {

        CPOrmCursor<Model> cursor = queryAsCursor(context);

        if(cursor == null)
            return new ArrayList<>();
        try {
            List<Model> resultList = new ArrayList<Model>(cursor.getCount());

            while (cursor.moveToNext()) {
                resultList.add(cursor.inflate());
            }

            return resultList;
        } finally {
            cursor.close();
        }
    }

    /**
     * @see #queryAsCount(Context)
     */
    public int queryAsCount() {

        return queryAsCount(CPOrm.getApplicationContext());
    }

    /**
     * Executes the query as a cursor, and then retrieves the row count from the cursor, the cursor is then closed and the count returned.
     *
     * @return The count indicating the amount of results for this select
     */
    public int queryAsCount(Context context) {

        boolean customInclude = false;

        if(includedColumns.isEmpty() && excludedColumns.isEmpty()) {
            String columnName = CPOrm.findTableDetails(context, dataObjectClass).findPrimaryKeyColumn().getColumnName();
            includedColumns.add(columnName);
            customInclude = true;
        }

        CPOrmCursor<Model> cursor = queryAsCursor(context);
        try {
            if(cursor == null)
                return 0;
            return cursor.getCount();
        } finally {

            if(customInclude)
            {
                includedColumns.clear();
            }

            if(cursor != null)
                cursor.close();
        }

    }

    /**
     * @see #asContentResolverValue(Context)
     */
    public ContentResolverValues asContentResolverValue() {

        return asContentResolverValue(CPOrm.getApplicationContext());
    }

    /**
     * Packages this select into a {@link za.co.cporm.model.util.ContentResolverValues} package, this will contain all of the required arguments to run this query on
     * a content resolver, it is used internally by all of the as* methods.
     *
     * @return The {@link za.co.cporm.model.util.ContentResolverValues} containing the arguments needed by the content resolver query method
     */
    public ContentResolverValues asContentResolverValue(Context context) {

        if (context == null)
            throw new IllegalArgumentException("Attempt to query with a null context");

        TableDetails tableDetails = CPOrm.findTableDetails(context, dataObjectClass);

        QueryBuilder where = buildWhereClause(context, ManifestHelper.getMappingFactory(context));
        QueryBuilder sort = buildSort();

        Uri.Builder itemUri = UriMatcherHelper.generateItemUri(context, tableDetails);

        if (offset != null) itemUri.appendQueryParameter(CPOrmContentProvider.PARAMETER_OFFSET, offset.toString());
        if (limit != null) itemUri.appendQueryParameter(CPOrmContentProvider.PARAMETER_LIMIT, limit.toString());
        if(distinct) itemUri.appendQueryParameter("DISTINCT", Boolean.TRUE.toString());
        if(!TextUtils.isEmpty(groupBy)) {
            itemUri.appendQueryParameter("GROUP_BY", groupBy);
            if (!TextUtils.isEmpty(having)) itemUri.appendQueryParameter("HAVING", having);
        }

        return new ContentResolverValues(tableDetails, itemUri.build(), getProjection(tableDetails), where.getQueryString(), where.getQueryArgsAsArray(), sort.getQueryString());
    }

    /**
     * @see #first(Context)
     */
    public Model first() {

        return first(CPOrm.getApplicationContext());
    }

    /**
     * Gets the results as a cursor, and returns the first item it finds.  The cursor is closed before the item is returned.  If now item is found mathing the query
     * null is returned instead.
     *
     * @return The first result if found, null otherwise.
     */
    public Model first(Context context) {

        Integer currentLimit = limit;
        limit(1); //Add a default limit for the user

        CPOrmCursor<Model> cursor = queryAsCursor(context);
        if(cursor == null)
            return null;
        try {

            if (cursor.moveToFirst()) {

                Model inflate = cursor.inflate();
                return inflate;
            } else return null;
        } finally {
            cursor.close();
            //Restore the previous limit
            limit = currentLimit;
        }
    }

    /**
     * @see #last(Context)
     */
    public Model last() {

        return last(CPOrm.getApplicationContext());
    }

    /**
     * Same as first, but this queries the last item in the cursor.
     *
     * @return Tha last item found in the cursor, null otherwise.
     */
    public Model last(Context context) {

        ContentResolverValues values = asContentResolverValue(context);
        CPOrmCursor<Model> cursor = queryAsCursor(context);
        if(cursor == null)
            return null;
        try {

            if (cursor.moveToLast()) {
                return cursor.inflate();
            } else return null;
        } finally {
            cursor.close();
        }

    }

    /**
     * Creates the projection based on the users inclusion, exclusion criteria.  If none is specified, all columns will be returned.
     *
     * @param tableDetails The table details object containing the column information
     * @return String[] containing the columns values to be queried
     */
    private String[] getProjection(TableDetails tableDetails) {

        if (!includedColumns.isEmpty()) {

            return includedColumns.toArray(new String[includedColumns.size()]);
        } else if (!excludedColumns.isEmpty()) {

            List<String> columns = new ArrayList<String>();

            for (String column : tableDetails.getColumnNames()) {

                if (!excludedColumns.contains(column))
                    columns.add(column);
            }

            return columns.toArray(new String[columns.size()]);
        } else return tableDetails.getColumnNames();
    }

    /**
     * Builds the sort query
     *
     * @return {@link za.co.cporm.model.query.QueryBuilder} containing the sort information
     */
    private QueryBuilder buildSort() {

        QueryBuilder builder = new QueryBuilder();
        Iterator<String> sortIterator = sortingOrderList.iterator();

        while (sortIterator.hasNext()) {

            builder.append(sortIterator.next());

            if (sortIterator.hasNext()) builder.append(", ");
        }

        return builder;
    }

    protected QueryBuilder getSelectQuery(Context context) {

        if (context == null)
            throw new IllegalArgumentException("Attempt to query with a null context");

        TableDetails tableDetails = CPOrm.findTableDetails(context, dataObjectClass);

        QueryBuilder select = new QueryBuilder();
        QueryBuilder where = buildWhereClause(context, ManifestHelper.getMappingFactory(context));

        select.append("SELECT ");

        if(distinct) {
            select.append("DISTINCT ");
        }

        Iterator<String> columnIterator = Arrays.asList(getProjection(tableDetails)).iterator();
        while (columnIterator.hasNext()) {

            select.append(columnIterator.next());

            if (columnIterator.hasNext()) select.append(", ");
        }
        select.append(" FROM ");
        select.append(tableDetails.getTableName());

        if (hasFilterValue()) {
            select.append(" WHERE ");
            select.append(where);
        }

        if(!TextUtils.isEmpty(groupBy)) {

            select.append(" GROUP BY ");
            select.append(groupBy);

            if(!TextUtils.isEmpty(having)) {

                select.append(" HAVING ");
                select.append(having);
            }
        }

        return select;
    }

    protected boolean isSingleColumnProjection() {

        return includedColumns.size() == 1;
    }

    /**
     * The where clause for this query
     */
    @Override
    public QueryBuilder buildWhereClause(Context context, SqlColumnMappingFactory columnMappingFactory) {

        return filterCriteria.buildWhereClause(context, columnMappingFactory);
    }

    @Override
    public QueryBuilder getWhereClause() {

        return filterCriteria.getWhereClause();
    }

    @Override
    public Select<Model> addClause(DataFilterClause clause, DataFilterConjunction conjunction) {

        this.filterCriteria.addClause(clause, conjunction);
        return this;
    }

    @Override
    public boolean hasFilterValue() {

        return filterCriteria.hasFilterValue();
    }

    @Override
    public String toString() {

        Context applicationContext = null;

        try {
            applicationContext = CPOrm.getApplicationContext();
        } catch (IllegalArgumentException ignore) {
        }

        if (applicationContext == null) return getWhereClause().toString();
        return getSelectQuery(applicationContext).toString();
    }
}
