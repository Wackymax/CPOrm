package za.co.cporm.example.app.model.domain.view;

import za.co.cporm.model.annotation.Column.Column;
import za.co.cporm.model.annotation.Column.PrimaryKey;
import za.co.cporm.model.annotation.Table;
import za.co.cporm.model.generate.TableView;

/**
 * Created by hennie.brink on 2015-05-16.
 */
@Table
public class UserRole implements TableView {

    @Override
    public String getTableViewSql() {

        return "SELECT U._ID, U.USER_NAME, R.ROLE_NAME FROM USER U INNER JOIN ROLE R ON U.ROLE_ID = R._ID";
    }

    @Column
    @PrimaryKey
    private int _id;
    @Column
    private String userName;
    @Column
    private String roleName;


}
