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

package jass.server.message;

import jass.lib.message.MessageData;
import jass.lib.message.ResultData;
import jass.server.entity.UserEntity;
import jass.server.repository.UserRepository;
import jass.server.util.ClientUtil;

/**
 * Logs out a user from the current connection.
 *
 * @author Manuele Vaccari
 * @version %I%, %G%
 * @since 1.0.0
 */
public final class Logout extends Message {
    /**
     * @param rawData The data (still not casted).
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    public Logout(final MessageData rawData) {
        super(rawData);
    }

    /**
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    @Override
    public void process(final ClientUtil client) {
        UserEntity user = UserRepository.getSingleton(null).getByUsername(client.getUsername());
        // User could be null if we just deleted it
        if (user != null) {
            user.setOffline();
            UserRepository.getSingleton(null).update(user);
        }

        client.setToken(null); // Destroy authentication token
        client.setUser(null); // Destroy account information
        client.send(new Result(new ResultData(getRawData().getId(), true)));
    }
}
