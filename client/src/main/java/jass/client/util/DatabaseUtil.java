/*
 * fhnw-jass is jass game programmed in java for a school project.
 * Copyright (C) 2020 Manuele Vaccari
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

package jass.client.util;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import jass.client.entity.LoginEntity;
import jass.client.entity.ServerEntity;
import jass.client.repository.LoginRepository;
import jass.client.repository.ServerRepository;
import jass.lib.servicelocator.Service;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;

/**
 * A class to create a database connection.
 *
 * @author Manuele Vaccari
 * @version %I%, %G%
 * @since 0.0.1
 */
public final class DatabaseUtil implements Service, Closeable {
    /**
     * The service name.
     */
    public static final String SERVICE_NAME = "db";

    /**
     * The database connection.
     */
    private final ConnectionSource connectionSource;

    /**
     * The DAO for the logins.
     */
    private Dao<LoginEntity, Integer> loginDao;

    /**
     * The DAO for the servers.
     */
    private Dao<ServerEntity, Integer> serverDao;

    /**
     * Create a database connection.
     *
     * @param databaseLocation A string containing the location of the file to
     *                         be accessed (and if necessary created).
     *
     * @since 0.0.1
     */
    public DatabaseUtil(final String databaseLocation) throws SQLException {
        // this uses h2 but you can change it to match your database
        String databaseUrl = "jdbc:sqlite:" + databaseLocation;

        // create our data-source for the database
        connectionSource = new JdbcConnectionSource(databaseUrl);

        // setup our database and DAOs
        setupDatabase();
    }

    /**
     * Setup our database and DAOs, for the created connection.
     *
     * @throws SQLException If an SQL error occurs.
     * @since 0.0.1
     */
    private void setupDatabase() throws SQLException {

        /*
         * Create our DAOs. One for each class and associated table.
         */
        loginDao = DaoManager.createDao(connectionSource, LoginEntity.class);
        LoginRepository.getSingleton(loginDao);
        serverDao = DaoManager.createDao(connectionSource, ServerEntity.class);
        ServerRepository.getSingleton(serverDao);

        /*
         * Create the tables, if they don't exist yet.
         */
        TableUtils.createTableIfNotExists(connectionSource, LoginEntity.class);
        TableUtils.createTableIfNotExists(connectionSource, ServerEntity.class);
    }

    /**
     * Close the database connection.
     *
     * @since 0.0.1
     */
    @Override
    public void close() {
        if (connectionSource != null) {
            try {
                connectionSource.close();
            } catch (IOException e) {
                // we don't care
            }
        }
    }

    /**
     * @return DAO object for the saved logins
     *
     * @since 0.0.1
     */
    public Dao<LoginEntity, Integer> getLoginDao() {
        return loginDao;
    }

    /**
     * @return DAO object for the saved servers
     *
     * @since 0.0.1
     */
    public Dao<ServerEntity, Integer> getServerDao() {
        return serverDao;
    }

    /**
     * For the ServiceLocator.
     */
    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
