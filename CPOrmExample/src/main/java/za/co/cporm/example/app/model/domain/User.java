package za.co.cporm.example.app.model.domain;

import za.co.cporm.example.app.model.domain.view.UserRole;
import za.co.cporm.model.CPDefaultRecord;
import za.co.cporm.model.annotation.ChangeListeners;
import za.co.cporm.model.annotation.Column.Column;
import za.co.cporm.model.annotation.Column.Unique;
import za.co.cporm.model.annotation.Table;

/**
 * Created by hennie.brink on 2015-03-20.
 */
@Table
@ChangeListeners(changeListeners = UserRole.class)
public class User extends CPDefaultRecord<User> {

    @Column
    @Unique
    private String userName;

    @Column
    private String givenName;

    @Column
    private String familyName;

    @Column
    private long roleId;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }
}
