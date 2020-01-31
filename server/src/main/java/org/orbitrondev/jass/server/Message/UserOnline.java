package org.orbitrondev.jass.server.Message;

import org.orbitrondev.jass.server.Client;
import org.orbitrondev.jass.server.Listener;

public class UserOnline extends Message {
	private String token;
	private String username;

	public UserOnline(String[] data) {
		super(data);
		this.token = data[1];
		this.username = data[2];
	}

	/**
	 * Anyone can query a specific user: are they currently logged in?
	 * 
	 * Note that "false" can also mean that the user asking the question is not
	 * logged in, and therefore cannot ask this question.
	 */
	@Override
	public void process(Client client) {
		boolean result = false;
		if (client.getToken() != null && client.getToken().equals(token)) {
		    result = Listener.exists(username);
		}
		client.send(new Result(result));
	}
}
