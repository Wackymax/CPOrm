package za.co.cporm.model.query;

import android.content.Context;
import android.text.TextUtils;
import za.co.cporm.model.util.ManifestHelper;

import java.util.Collection;
import java.util.Iterator;

/**
 * A filter criterion is single SQL condition.  The filter column, operator, and filter value must be supplied
 * for this criterion to be valid. Filter values are automatically converted to the correct sql format, and
 * the sql % are automatically added in the correct locations based on the operator used.
 */
public class DataFilterCriterion implements DataFilterClause {

    public String filterColumn;
    public DataFilterOperator filterOperator;
    public Object filterValue;

    /**
     * Creates a new instance, using the context to determine the conversion for arguments to sql friendly format
     */
    private DataFilterCriterion(){

    }

    /**
     * Creates a new instance, using the context to determine the conversion for arguments to sql friendly format.
     * The additional arguments can be used to set the values for the criterion.
     * @param filterColumn The column to query compare.
     * @param operator The operator used for comparison.
     */
    public DataFilterCriterion(String filterColumn, DataFilterOperator operator, Object filterValue){
        this.filterColumn = filterColumn;
        this.filterOperator = operator;
        this.filterValue = filterValue;
    }

    @Override
    public QueryBuilder buildWhereClause(Context context) {

        QueryBuilder builder = new QueryBuilder();
        builder.append(filterColumn);
        builder.append(" ");
        builder.append(filterOperator.getSqlRepresentation());

        if(filterValue != null){

            if(filterValue instanceof Collection){

                Iterator collectionIterator = ((Collection)filterValue).iterator();
                builder.append(" (");

                while(collectionIterator.hasNext()){

                    builder.append("?", convertToSQLFormat(context, collectionIterator.next()));

                    if(collectionIterator.hasNext()) builder.append(", ");
                }
                builder.append(")");

            }
            else if(filterValue instanceof Select){

                Select innerSelect = (Select)filterValue;

                if(!innerSelect.isSingleColumnProjection())
                    throw new IllegalArgumentException("Inner select can only contain a single column selection");

                builder.append(" (");
                builder.append((innerSelect).getSelectQuery(context));
                builder.append(")");
            }
            else builder.append(" ?", convertToSQLFormat(context, filterValue));
        }

        return builder;
    }

    @Override
    public String getWhereClause() {

        QueryBuilder builder = new QueryBuilder();
        builder.append(filterColumn);
        builder.append(" ");
        builder.append(filterOperator.getSqlRepresentation());

        if(filterValue != null){

            if(filterValue instanceof Collection){

                Iterator collectionIterator = ((Collection)filterValue).iterator();
                builder.append(" (");

                while(collectionIterator.hasNext()){

                    builder.append("?");

                    if(collectionIterator.hasNext()) builder.append(", ");
                }
                builder.append(")");

            }
            else if(filterValue instanceof Select){

                Select innerSelect = (Select)filterValue;

                if(!innerSelect.isSingleColumnProjection())
                    throw new IllegalArgumentException("Inner select can only contain a single column selection");

                builder.append(" (");
                builder.append((innerSelect).getSelectQuery(null));
                builder.append(")");
            }
            else builder.append(" ?");
        }

        return builder.toString();
    }

    @Override
    public void addClause(DataFilterClause clause, DataFilterConjunction conjunction) {
        throw new UnsupportedOperationException("Clauses cannot be added to a data filter criterion");
    }

    private Object convertToSQLFormat(Context context, Object object){

        if(filterOperator == DataFilterOperator.LIKE || filterOperator == DataFilterOperator.NOT_LIKE) return "%" + object + "%";
        else if(filterOperator == DataFilterOperator.BEGINS_WITH) return object + "%";
        else if(filterOperator == DataFilterOperator.ENDS_WITH) return "%" + object;
        else return ManifestHelper.getMappingFactory(context).findColumnMapping(filterValue.getClass()).toSqlType(filterValue);
    }

    private void validate()
    {
        if(TextUtils.isEmpty(filterColumn)) throw new IllegalStateException("Filter column is empty");
        if(filterOperator == null) throw new IllegalStateException("Filter operator not specified");
        if(filterValue == null && filterOperator != DataFilterOperator.IS_NULL && filterOperator != DataFilterOperator.IS_NOT_NULL)
            throw new IllegalStateException("Filter value must be supplied with this operator");
    }

    private void setFilterColumn(String filterColumn) {
        this.filterColumn = filterColumn;
    }

    private void setFilterOperator(DataFilterOperator filterOperator) {
        this.filterOperator = filterOperator;
    }

    private void setFilterValue(Object filterValue) {
        this.filterValue = filterValue;
    }

    public static class Builder<T extends DataFilterClause>{

        private final T originator;
        private final DataFilterConjunction conjunction;
        private final DataFilterCriterion criterion;

        protected Builder(T originator, DataFilterConjunction conjunction){

            this.originator = originator;
            this.conjunction = conjunction;
            criterion = new DataFilterCriterion();
        }

        public T equal(String column, Object value) {

            column(column);
            criterion.setFilterOperator(DataFilterOperator.EQUAL);
            return value(value);
        }

        public T notEqual(String column, Object value){

            column(column);
            criterion.setFilterOperator(DataFilterOperator.NOT_EQUAL);
            return value(value);
        }

        public T greaterOrEqual(String column, Object value){

            column(column);
            criterion.setFilterOperator(DataFilterOperator.GREATER_OR_EQUAL);
            return value(value);
        }

        public T smallerOrEqual(String column, Object value){

            column(column);
            criterion.setFilterOperator(DataFilterOperator.SMALLER_OR_EQUAL);
            return value(value);
        }

        public T greaterThan(String column, Object value){

            column(column);
            criterion.setFilterOperator(DataFilterOperator.GREATER_THAN);
            return value(value);
        }

        public T smallerThan(String column, Object value){

            column(column);
            criterion.setFilterOperator(DataFilterOperator.SMALLER_THAN);
            return value(value);
        }

        public T like(String column, Object value){

            column(column);
            criterion.setFilterOperator(DataFilterOperator.LIKE);
            return value(value);
        }

        public T notLike(String column, Object value){

            column(column);
            criterion.setFilterOperator(DataFilterOperator.NOT_LIKE);
            return value(value);
        }

        public T in(String column, Collection value){

            column(column);
            criterion.setFilterOperator(DataFilterOperator.IN);
            return value(value);
        }

        public T in(String column, Select value){

            column(column);
            criterion.setFilterOperator(DataFilterOperator.IN);
            return value(value);
        }

        public T notIn(String column, Collection value){

            column(column);
            criterion.setFilterOperator(DataFilterOperator.NOT_IN);
            return value(value);
        }

        public T notIn(String column, Select value){

            column(column);
            criterion.setFilterOperator(DataFilterOperator.NOT_IN);
            return value(value);
        }

        public T isNull(String column){

            column(column);
            criterion.setFilterOperator(DataFilterOperator.IS_NULL);
            return build();
        }

        public T isNotNull(String column){

            column(column);
            criterion.setFilterOperator(DataFilterOperator.IS_NOT_NULL);
            return build();
        }

        private Builder<T> column(String column){

            criterion.setFilterColumn(column);
            return this;
        }

        private T value(Object value){

            criterion.setFilterValue(value);
            return build();
        }

        private T build(){

            criterion.validate();
            originator.addClause(criterion, conjunction);
            return originator;
        }
    }

    public enum DataFilterOperator{
        EQUAL("="),
        NOT_EQUAL("<>"),
        GREATER_OR_EQUAL(">="),
        SMALLER_OR_EQUAL("<="),
        GREATER_THAN(">"),
        SMALLER_THAN("<"),
        LIKE("LIKE"),
        NOT_LIKE("NOT LIKE"),
        IN("IN"),
        NOT_IN("NOT IN"),
        IS_NULL("IS NULL"),
        IS_NOT_NULL("IS NOT NULL"),
        BEGINS_WITH("LIKE"),
        ENDS_WITH("LIKE");

        private final String sqlRepresentation;

        DataFilterOperator(String sqlRepresentation) {
            this.sqlRepresentation = sqlRepresentation;
        }

        public String getSqlRepresentation() {
            return sqlRepresentation;
        }
    }
}
