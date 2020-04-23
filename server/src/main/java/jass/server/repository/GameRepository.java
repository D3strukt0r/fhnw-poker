package jass.server.repository;

import com.j256.ormlite.dao.Dao;
import jass.lib.database.Repository;
import jass.server.entity.GameEntity;

/**
 * A model with all known (and cached) teams.
 *
 * @author Thomas Weber
 * @version %I%, %G%
 * @since 0.0.1
 */
public final class GameRepository extends Repository<Dao<GameEntity, String>, GameEntity> {
    /**
     * The singleton.
     */
    private static GameRepository singleton = null;

    /**
     * Creates a new singleton or returns the existing one.
     *
     * @param dao The DAO to edit inside the database.
     *
     * @return Returns the Repository.
     */
    public static GameRepository getSingleton(final Dao<GameEntity, String> dao) {
        if (singleton == null) {
            singleton = new GameRepository(dao);
        }
        return singleton;
    }

    /**
     * @param dao The DAO to edit inside the database.
     */
    public GameRepository(final Dao<GameEntity, String> dao) {
        super(dao);
    }
}