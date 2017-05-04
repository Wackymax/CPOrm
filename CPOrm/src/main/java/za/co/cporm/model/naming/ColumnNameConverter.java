package za.co.cporm.model.naming;

/**
 * Created by hennie.brink on 2017/05/04.
 */

public interface ColumnNameConverter {

    String convertToSql(String fieldName);
}
