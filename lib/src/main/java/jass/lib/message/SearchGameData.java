package jass.lib.message;

import org.json.JSONObject;

/**
 * The data model for the Search Game message.
 *
 * @author Thomas Weber
 */

public class SearchGameData extends MessageData {
    private final String token;

    public SearchGameData(String token) {
        super("SearchGame");
        this.token = token;
    }

    public SearchGameData(JSONObject data) {
        super(data);
        token = data.getString("token");
    }

    public String getToken() {
        return token;
    }
}
