package org.orbitrondev.jass.client.Message;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.orbitrondev.jass.client.Entity.LoginEntity;
import org.orbitrondev.jass.client.Utils.BackendUtil;
import org.orbitrondev.jass.client.Utils.DatabaseUtil;
import org.orbitrondev.jass.lib.Message.LoginData;
import org.orbitrondev.jass.lib.Message.MessageData;
import org.orbitrondev.jass.lib.Message.ResultData;
import org.orbitrondev.jass.lib.ServiceLocator.ServiceLocator;

import java.sql.SQLException;

/**
 * Login to the server.
 */
public class Login extends Message {
    private static final Logger logger = LogManager.getLogger(Login.class);
    private LoginData data;

    private String token = null;

	public Login(MessageData rawData) {
		super(rawData);
        data = (LoginData) rawData;
	}

	@Override
	public boolean process(BackendUtil backendUtil) {
        backendUtil.send(this);

        Message result = backendUtil.waitForResultResponse(data.getId());
        ResultData resultData = (ResultData) result.getRawData();

        if (resultData.getResult()) {
            token = resultData.getResultData().getString("token");

            LoginEntity login = new LoginEntity(data.getUsername(), data.getPassword(), token);
            ServiceLocator.remove("login");
            ServiceLocator.add(login);

            DatabaseUtil db = (DatabaseUtil) ServiceLocator.get("db");
            try {
                db.getLoginDao().create(login);
            } catch (SQLException e) {
                logger.error("Couldn't save login data to local database.");
            }
        }
        return resultData.getResult();
	}

    public String getToken() {
        return token;
    }
}