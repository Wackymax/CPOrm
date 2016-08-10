package za.co.cporm.model.query;

import android.content.Context;
import android.text.TextUtils;

import java.util.Arrays;

import za.co.cporm.model.map.SqlColumnMappingFactory;

/**
 * Created by hennie.brink on 2015-03-20.
 */
public class SQLSegment implements DataFilterClause {

    private String sqlSegment;
    private Object[] args;

    private SQLSegment(){
    }

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
    public QueryBuilder getWhereClause() {

        return new QueryBuilder(sqlSegment);
    }

    @Override
    public SQLSegment addClause(DataFilterClause clause, DataFilterConjunction conjunction) {
        throw new UnsupportedOperationException("Clauses cannot be added to a data filter criterion");
    }

    @Override
    public SQLSegment cloneFrom() {

        SQLSegment clone = new SQLSegment();
        clone.sqlSegment = this.sqlSegment;

        if(this.args != null)
            clone.args = Arrays.copyOf(this.args, this.args.length);

        return clone;
    }

    @Override
    public boolean hasFilterValue() {

        return !TextUtils.isEmpty(sqlSegment);
    }
}
