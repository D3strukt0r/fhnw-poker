/*
 * fhnw-jass is jass game programmed in java for a school project.
 * Copyright (C) 2020 Manuele Vaccari & Victor Hargrave & Thomas Weber & Sasa Trajkova
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

package jass.server.repository;

import com.j256.ormlite.dao.Dao;
import jass.lib.database.Repository;
import jass.server.entity.RoundEntity;

/**
 * A model with all known rounds.
 *
 * @author Victor Hargrave
 * @version %I%, %G%
 * @since 0.0.1
 */
public final class RoundRepository extends Repository<Dao<RoundEntity, Integer>, RoundEntity> {
    /**
     * The singleton.
     */
    private static RoundRepository singleton = null;

    /**
     * Creates a new singleton or returns the existing one.
     *
     * @param dao The DAO to edit inside the database.
     *
     * @return Returns the Repository.
     */
    public static RoundRepository getSingleton(final Dao<RoundEntity, Integer> dao) {
        if (singleton == null) {
            singleton = new RoundRepository(dao);
        }
        return singleton;
    }

    /**
     * @param dao The DAO to edit inside the database.
     */
    public RoundRepository(final Dao<RoundEntity, Integer> dao) {
        super(dao);
    }
}
