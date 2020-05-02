package jass.client.eventlistener;

import jass.lib.message.PlayedCardData;

/**
 * @author Manuele Vaccari
 * @version %I%, %G%
 * @since 0.0.1
 */
public interface PlayCardEventListener {
    /**
     * Executes when a user is requested to play a card.
     *
     * @param data The data (basically the ID).
     */
    void onPlayCard(PlayedCardData data);
}
