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
public class Role extends CPDefaultRecord<Role> {

    @Column
    @Unique
    private String roleName;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public String toString() {

        return roleName;
    }
}
