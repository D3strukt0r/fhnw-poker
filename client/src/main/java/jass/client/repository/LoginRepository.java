/*
 * fhnw-jass is jass game programmed in java for a school project.
 * Copyright (C) 2020 Manuele Vaccari & Victor Hargrave & Thomas Weber & Sasa
 * Trajkova
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package jass.client.repository;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import jass.client.entity.LoginEntity;
import jass.lib.database.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Helper functions concerning the LoginEntity class.
 *
 * @author Manuele Vaccari
 * @version %I%, %G%
 * @since 1.0.0
 */
public final class LoginRepository extends Repository<Dao<LoginEntity, Integer>, LoginEntity> {
    /**
     * The singleton.
     */
    private static LoginRepository singleton = null;

    /**
     * Creates a new singleton or returns the existing one.
     *
     * @param dao The DAO to edit inside the database.
     *
     * @return Returns the Repository.
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    public static LoginRepository getSingleton(final Dao<LoginEntity, Integer> dao) {
        if (singleton == null) {
            singleton = new LoginRepository(dao);
        }
        return singleton;
    }

    /**
     * @param dao The DAO to edit inside the database.
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    public LoginRepository(final Dao<LoginEntity, Integer> dao) {
        super(dao);
    }

    /**
     * Sets the given login to connect automatically and disables all other
     * logins.
     *
     * @param login A LoginEntity object
     *
     * @return "true" if everything went alright, "false" if something failed.
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    public boolean setToRememberMe(final LoginEntity login) {
        boolean result;
        for (LoginEntity l : getDao()) {
            // Disable the other entry (should be only one)
            if (l.isRememberMe()) {
                l.setRememberMe(false);
                result = update(l);

                if (!result) {
                    return false;
                }
            }
            // Set the new entry to connect automatically (if it is already in
            // the db)
            if (l.getId() == login.getId()) {
                login.setRememberMe(true);
                result = update(login);

                if (!result) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @return Returns the login which is set as connect automatically.
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    public LoginEntity findRememberMe() throws SQLException {
        QueryBuilder<LoginEntity, Integer> findRememberMeLoginStmt = getDao().queryBuilder();
        findRememberMeLoginStmt.where()
            .like(LoginEntity.REMEMBER_ME_FIELD_NAME, true);
        List<LoginEntity> findRememberMeLoginResult = getDao().query(findRememberMeLoginStmt.prepare());

        if (findRememberMeLoginResult.size() > 0) {
            return findRememberMeLoginResult.get(0);
        }
        return null;
    }
}
