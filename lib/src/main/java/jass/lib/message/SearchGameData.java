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

package jass.lib.message;

import org.json.JSONObject;

/**
 * The data model for the Search Game message.
 *
 * @author Thomas Weber
 * @version %I%, %G%
 * @since 1.0.0
 */
public final class SearchGameData extends MessageData {
    /**
     * The token of the current session.
     */
    private final String token;

    /**
     * The username of the player searching.
     */
    private final String username;

    /**
     * @param token    The token of the current session.
     * @param username The username of the player searching.
     *
     * @author Thomas Weber
     * @since 1.0.0
     */
    public SearchGameData(final String token, final String username) {
        super("SearchGame");
        this.token = token;
        this.username = username;
    }

    /**
     * @param data The message containing all the data.
     *
     * @author Thomas Weber
     * @since 1.0.0
     */
    public SearchGameData(final JSONObject data) {
        super(data);
        token = data.getString("token");
        username = data.getString("username");
    }

    /**
     * @return Returns the token.
     *
     * @author Thomas Weber
     * @since 1.0.0
     */
    public String getToken() {
        return token;
    }

    /**
     * @return Returns the username.
     *
     * @author Thomas Weber
     * @since 1.0.0
     */
    public String getUsername() {
        return username;
    }
}
