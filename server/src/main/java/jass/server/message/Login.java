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

import jass.lib.message.LoginData;
import jass.lib.message.MessageData;
import jass.lib.message.ResultData;
import jass.server.entity.UserEntity;
import jass.server.repository.UserRepository;
import jass.server.util.ClientUtil;
import jass.server.util.ServerSocketUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Date;

/**
 * Login to an existing account. If successful, return an authentication token
 * to the client.
 *
 * @author Manuele Vaccari
 * @version %I%, %G%
 * @since 1.0.0
 */
public final class Login extends Message {
    /**
     * The logger to print to console and save in a .log file.
     */
    private static final Logger logger = LogManager.getLogger(Login.class);

    /**
     * The data of the message.
     */
    private final LoginData data;

    /**
     * To generate random numbers.
     */
    private static final SecureRandom rand = new SecureRandom();

    /**
     * @param rawData The data (still not casted).
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    public Login(final MessageData rawData) {
        super(rawData);
        data = (LoginData) rawData;
    }

    /**
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    @Override
    public void process(final ClientUtil client) {
        // Find existing login matching the username.
        UserEntity user;
        if (UserRepository.getSingleton(null).usernameExists(data.getUsername())) {
            logger.info("User " + data.getUsername() + " exists");
            user = UserRepository.getSingleton(null).getByUsername(data.getUsername());
        } else {
            logger.info("User " + data.getUsername() + " does not exist");
            client.send(new Result(new ResultData(data.getId(), false, (new JSONObject()).put("reason", LoginData.Result.USER_DOES_NOT_EXIST))));
            return;
        }

        // Check if the client used the correct password.
        if (user != null && user.checkPassword(data.getPassword())) {
            logger.info("Client used the correct password");

            // Check if already logged in...
            if (ServerSocketUtil.exists(user.getUsername())) {
                client.send(new Result(new ResultData(data.getId(), false, (new JSONObject()).put("reason", LoginData.Result.USER_ALREADY_LOGGED_IN))));
                return;
            }

            // Update last login time
            user.setOnline()
                .setLastLogin(Date.from(Instant.now()));
            String token = createToken();
            user.setToken(token);
            UserRepository.getSingleton(null).update(user);

            // Save the user to this connection.
            client.setUser(user);
            client.setToken(token);

            // Return the token to the client.
            client.send(new Result(new ResultData(data.getId(), true, (new JSONObject()).put("token", token))));
        } else {
            logger.info("Client used the wrong password");
            client.send(new Result(new ResultData(data.getId(), false, (new JSONObject()).put("reason", LoginData.Result.WRONG_PASSWORD))));
        }
    }

    /**
     * @return Returns a generated token.
     *
     * @author Bradley Richards
     * @since 1.0.0
     */
    public static String createToken() {
        byte[] token = new byte[16];
        rand.nextBytes(token);
        return bytesToHex(token);
    }

    /**
     * @param bytes The bytes to convert.
     *
     * @return Returns a converted string.
     *
     * @author https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
     * @since 1.0.0
     */
    public static String bytesToHex(final byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();

        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
