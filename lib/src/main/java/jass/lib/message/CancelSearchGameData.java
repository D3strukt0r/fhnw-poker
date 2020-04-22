package jass.lib.message;

import org.json.JSONObject;

/**
 * The data model for the Search Game message.
 *
 * @author Thomas Weber
 */
public final class CancelSearchGameData extends MessageData {
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
     */
    public CancelSearchGameData(final String token, final String username) {
        super("CancelSearchGame");
        this.token = token;
        this.username = username;
    }

    /**
     * @param data The message containing all the data.
     */
    public CancelSearchGameData(final JSONObject data) {
        super(data);
        token = data.getString("token");
        username = data.getString("username");
    }

    /**
     * @return Returns the token.
     */
    public String getToken() {
        return token;
    }

    /**
     * @return Returns the username.
     */
    public String getUsername() {
        return username;
    }

}
