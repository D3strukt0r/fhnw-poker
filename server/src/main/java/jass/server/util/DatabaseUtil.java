/*
 * fhnw-jass is jass game programmed in java for a school project.
 * Copyright (C) 2020 Manuele Vaccari, Victor Hargrave, Sasa Trajkova, Thomas
 * Weber
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

package jass.server.util;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import jass.lib.servicelocator.Service;
import jass.server.entity.CardEntity;
import jass.server.entity.DeckEntity;
import jass.server.entity.GameEntity;
import jass.server.entity.RankEntity;
import jass.server.entity.RoundEntity;
import jass.server.entity.SuitEntity;
import jass.server.entity.TeamEntity;
import jass.server.entity.UserEntity;
import jass.server.repository.CardRepository;
import jass.server.repository.DeckRepository;
import jass.server.repository.GameRepository;
import jass.server.repository.RankRepository;
import jass.server.repository.RoundRepository;
import jass.server.repository.SuitRepository;
import jass.server.repository.TeamRepository;
import jass.server.repository.UserRepository;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;

/**
 * A class to create a database connection.
 * <p>
 * Also contains a lot of code from following example code:
 * https://github.com/j256/ormlite-jdbc/blob/master/src/test/java/com/j256/ormlite/examples/manytomany/ManyToManyMain.java
 *
 * @author Manuele Vaccari & Victor Hargrave
 * @version %I%, %G%
 * @since 0.0.1
 */
public final class DatabaseUtil implements Service, Closeable {
    /**
     * Default location to store the data.
     */
    public static final String DEFAULT_LOCATION = "sqlite:data/jass_server.sqlite3";

    /**
     * The database connection.
     */
    private final ConnectionSource connectionSource;

    /**
     * The DAO for the users.
     */
    private Dao<UserEntity, Integer> userDao;

    /**
     * The DAO for the teams.
     */
    private Dao<TeamEntity, Integer> teamDao;

    /**
     * The DAO for the games.
     */
    private Dao<GameEntity, Integer> gameDao;

    /**
     * The DAO for the suits.
     */
    private Dao<SuitEntity, Integer> suitDao;

    /**
     * The DAO for the ranks.
     */
    private Dao<RankEntity, Integer> rankDao;

    /**
     * The DAO for the decks.
     */
    private Dao<DeckEntity, Integer> deckDao;

    /**
     * The DAO for the rounds.
     */
    private Dao<RoundEntity, Integer> roundDao;

    /**
     * The DAO for the cards.
     */
    private Dao<CardEntity, Integer> cardDao;

    /**
     * Create a database connection.
     *
     * @param databaseLocation A string containing the location of the file to
     *                         be accessed (and if necessary created).
     *
     * @throws SQLException If an SQL error occurs.
     * @since 0.0.1
     */
    public DatabaseUtil(final String databaseLocation) throws SQLException {
        // This uses h2 but you can change it to match your database
        String databaseUrl = "jdbc:" + databaseLocation;

        // Create our data-source for the database
        connectionSource = new JdbcConnectionSource(databaseUrl);

        // Setup our database and DAOs
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
        userDao = DaoManager.createDao(connectionSource, UserEntity.class);
        UserRepository.getSingleton(userDao);
        teamDao = DaoManager.createDao(connectionSource, TeamEntity.class);
        TeamRepository.getSingleton(teamDao);
        gameDao = DaoManager.createDao(connectionSource, GameEntity.class);
        GameRepository.getSingleton(gameDao);
        suitDao = DaoManager.createDao(connectionSource, SuitEntity.class);
        SuitRepository.getSingleton(suitDao);
        rankDao = DaoManager.createDao(connectionSource, RankEntity.class);
        RankRepository.getSingleton(rankDao);
        cardDao = DaoManager.createDao(connectionSource, CardEntity.class);
        CardRepository.getSingleton(cardDao);
        roundDao = DaoManager.createDao(connectionSource, RoundEntity.class);
        RoundRepository.getSingleton(roundDao);
        deckDao = DaoManager.createDao(connectionSource, DeckEntity.class);
        DeckRepository.getSingleton(deckDao);

        /*
         * Drop and Recreate all the tables except for the user if server
         * restarts.
         */
        TableUtils.createTableIfNotExists(connectionSource, UserEntity.class);
        TableUtils.createTableIfNotExists(connectionSource, RankEntity.class);
        TableUtils.createTableIfNotExists(connectionSource, SuitEntity.class);
        TableUtils.createTableIfNotExists(connectionSource, CardEntity.class);
        TableUtils.createTableIfNotExists(connectionSource, GameEntity.class);
        TableUtils.createTableIfNotExists(connectionSource, TeamEntity.class);
        TableUtils.createTableIfNotExists(connectionSource, RoundEntity.class);
        TableUtils.createTableIfNotExists(connectionSource, DeckEntity.class);

        insertSuitSeedData();
        insertRankSeedData();
        insertCardSeedData();
    }

    private void insertRankSeedData() throws SQLException {
        if (!rankDao.idExists(1)) {
            rankDao.create((new RankEntity().setId(1).setKey("6").setPointsTrumpf(0).setPointsObeAbe(0).setPointsOndeufe(11)));
        }
        if (!rankDao.idExists(2)) {
            rankDao.create((new RankEntity().setId(2).setKey("7").setPointsTrumpf(0).setPointsObeAbe(0).setPointsOndeufe(0)));
        }
        if (!rankDao.idExists(3)) {
            rankDao.create((new RankEntity().setId(3).setKey("8").setPointsTrumpf(0).setPointsObeAbe(8).setPointsOndeufe(8)));
        }
        if (!rankDao.idExists(4)) {
            rankDao.create((new RankEntity().setId(4).setKey("9").setPointsTrumpf(0).setPointsObeAbe(0).setPointsOndeufe(0)));
        }
        if (!rankDao.idExists(5)) {
            rankDao.create((new RankEntity().setId(5).setKey("10").setPointsTrumpf(10).setPointsObeAbe(10).setPointsOndeufe(10)));
        }
        if (!rankDao.idExists(6)) {
            rankDao.create((new RankEntity().setId(6).setKey("jack").setPointsTrumpf(2).setPointsObeAbe(2).setPointsOndeufe(2)));
        }
        if (!rankDao.idExists(7)) {
            rankDao.create((new RankEntity().setId(7).setKey("queen").setPointsTrumpf(3).setPointsObeAbe(3).setPointsOndeufe(3)));
        }
        if (!rankDao.idExists(8)) {
            rankDao.create((new RankEntity().setId(8).setKey("king").setPointsTrumpf(4).setPointsObeAbe(4).setPointsOndeufe(4)));
        }
        if (!rankDao.idExists(9)) {
            rankDao.create((new RankEntity().setId(9).setKey("ace").setPointsTrumpf(11).setPointsObeAbe(11).setPointsOndeufe(0)));
        }
    }

    private void insertSuitSeedData() throws SQLException {
        if (!suitDao.idExists(1)) {
            suitDao.create((new SuitEntity()).setId(1).setKey("hearts"));
        }
        if (!suitDao.idExists(2)) {
            suitDao.create((new SuitEntity()).setId(2).setKey("diamonds"));
        }
        if (!suitDao.idExists(3)) {
            suitDao.create((new SuitEntity()).setId(3).setKey("spades"));
        }
        if (!suitDao.idExists(4)) {
            suitDao.create((new SuitEntity()).setId(4).setKey("clubs"));
        }
    }

    private void insertCardSeedData() throws SQLException {
        int addend = 0;
        for (int i = 1; i <= 4; i++) {
            for (int j = 1; j <= 9; j++) {
                if (!cardDao.idExists(j + addend)) {
                    cardDao.create((new CardEntity())
                        .setId(j + addend)
                        .setRank(rankDao.queryForId(j))
                        .setSuit(suitDao.queryForId(i))
                    );
                }
            }
            addend += 9;
        }
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
            } catch (IOException e) { /* Ignore */ }
        }
    }
}
