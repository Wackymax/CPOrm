package za.co.cporm.model.query;

import android.content.Context;
import android.text.TextUtils;
import za.co.cporm.model.map.SqlColumnMappingFactory;

/**
 * Created by hennie.brink on 2015-03-20.
 */
public class SQLSegment implements DataFilterClause {

    private final String sqlSegment;
    private final Object[] args;

    public SQLSegment(String sqlSegment, Object... args) {

        this.sqlSegment = sqlSegment;

        this.args = args;
    }

    @Override
    public QueryBuilder buildWhereClause(Context context, SqlColumnMappingFactory columnMappingFactory) {

        for (int i = 0; i < args.length; i++) {
            Object argObject = args[i];
            args[i] = columnMappingFactory.findColumnMapping(argObject.getClass()).toSqlType(argObject);
        }

        return new QueryBuilder(sqlSegment, args);
    }


    @Override
    public String getWhereClause() {

        return sqlSegment;
    }

    @Override
    public SQLSegment addClause(DataFilterClause clause, DataFilterConjunction conjunction) {
        throw new UnsupportedOperationException("Clauses cannot be added to a data filter criterion");
    }

    @Override
    public boolean hasFilterValue() {

        return !TextUtils.isEmpty(sqlSegment);
    }
}
