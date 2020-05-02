package jass.server.util;

import jass.lib.GameMode;
import jass.lib.message.*;
import jass.lib.servicelocator.ServiceLocator;
import jass.server.entity.*;
import jass.server.eventlistener.ChosenGameModeEventListener;
import jass.server.eventlistener.PlayedCardEventListener;
import jass.server.message.*;
import jass.server.repository.GameRepository;
import jass.server.repository.RoundRepository;
import jass.server.repository.TeamRepository;
import jass.server.repository.TurnRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Thomas Weber, Manuele Vaccari
 * @version %I%, %G%
 * @since 0.0.1
 */
public final class GameUtil implements ChosenGameModeEventListener, PlayedCardEventListener {
    /**
     * The logger to print to console and save in a .log file.
     */
    private static final Logger logger = LogManager.getLogger(GameUtil.class);

    /**
     * The client for player one.
     */
    private final ClientUtil clientPlayerOne;

    /**
     * The client for player two.
     */
    private final ClientUtil clientPlayerTwo;

    /**
     * The client for player three.
     */
    private final ClientUtil clientPlayerThree;

    /**
     * The client for player four.
     */
    private final ClientUtil clientPlayerFour;

    /**
     * The game.
     */
    private final GameEntity game;

    /**
     * The current round in the game.
     */
    private RoundEntity currentRound;

    /**
     * The current turn in the game.
     */
    private TurnEntity currentTurn;

    /**
     * @param clientPlayerOne   Player one.
     * @param clientPlayerTwo   Player two.
     * @param clientPlayerThree Player three.
     * @param clientPlayerFour  Player four.
     */
    public GameUtil(final ClientUtil clientPlayerOne, final ClientUtil clientPlayerTwo, final ClientUtil clientPlayerThree, final ClientUtil clientPlayerFour) {
        this.clientPlayerOne = clientPlayerOne;
        UserEntity playerOne = clientPlayerOne.getUser();
        this.clientPlayerTwo = clientPlayerTwo;
        UserEntity playerTwo = clientPlayerTwo.getUser();
        this.clientPlayerThree = clientPlayerThree;
        UserEntity playerThree = clientPlayerThree.getUser();
        this.clientPlayerFour = clientPlayerFour;
        UserEntity playerFour = clientPlayerFour.getUser();

        // Add event listeners
        clientPlayerOne.addChosenGameModeEventListener(this);
        clientPlayerTwo.addChosenGameModeEventListener(this);
        clientPlayerThree.addChosenGameModeEventListener(this);
        clientPlayerFour.addChosenGameModeEventListener(this);

        clientPlayerOne.addPlayedCardEventListener(this);
        clientPlayerTwo.addPlayedCardEventListener(this);
        clientPlayerThree.addPlayedCardEventListener(this);
        clientPlayerFour.addPlayedCardEventListener(this);

        // Assign and create Teams
        TeamEntity teamOne = (new TeamEntity()).setPlayerOne(playerOne).setPlayerTwo(playerThree);
        TeamRepository.getSingleton(null).add(teamOne);
        TeamEntity teamTwo = (new TeamEntity()).setPlayerOne(playerTwo).setPlayerTwo(playerFour);
        TeamRepository.getSingleton(null).add(teamTwo);

        // Initialize new Game
        game = (new GameEntity()).setTeamOne(teamOne).setTeamTwo(teamTwo);
        GameRepository.getSingleton(null).add(game);
        logger.info("Successfully created game with ID: " + game.getId());

        // Broadcast to all Players that a game was created for them
        GameFound gameFound = new GameFound(new GameFoundData(
            game.getId(),
            playerOne.getId(), playerOne.getUsername(), getTeamIdForPlayer(playerOne, teamOne, teamTwo),
            playerTwo.getId(), playerTwo.getUsername(), getTeamIdForPlayer(playerTwo, teamOne, teamTwo),
            playerThree.getId(), playerThree.getUsername(), getTeamIdForPlayer(playerThree, teamOne, teamTwo),
            playerFour.getId(), playerFour.getUsername(), getTeamIdForPlayer(playerFour, teamOne, teamTwo)
        ));
        broadcast(gameFound);

        // Create a round
        CardUtil cardUtil = ServiceLocator.get(CardUtil.class);
        assert cardUtil != null;
        currentRound = (new RoundEntity()).setGameModeChooser(playerOne).setGame(game);
        RoundRepository.getSingleton(null).add(currentRound);

        // Send a deck to each user
        List<DeckEntity> decks = cardUtil.addDecksForPlayers(currentRound, playerOne, playerTwo, playerThree, playerFour);
        cardUtil.broadcastDeck(clientPlayerOne, decks.get(0));
        cardUtil.broadcastDeck(clientPlayerTwo, decks.get(1));
        cardUtil.broadcastDeck(clientPlayerThree, decks.get(2));
        cardUtil.broadcastDeck(clientPlayerFour, decks.get(3));

        // Send a message to player one to choose a game mode
        sendChooseGameMode();
    }

    @Override
    public void onChosenGameMode(final ChosenGameModeData data) {
        // Get the right client to compare to.
        String gameModeChooserUsername = currentRound.getGameModeChooser().getUsername();
        ClientUtil client;
        if (gameModeChooserUsername.equals(clientPlayerOne.getUsername())) {
            client = clientPlayerOne;
        } else if (gameModeChooserUsername.equals(clientPlayerTwo.getUsername())) {
            client = clientPlayerTwo;
        } else if (gameModeChooserUsername.equals(clientPlayerThree.getUsername())) {
            client = clientPlayerThree;
        } else if (gameModeChooserUsername.equals(clientPlayerFour.getUsername())) {
            client = clientPlayerFour;
        } else {
            throw new IllegalStateException("Unexpected value: " + gameModeChooserUsername);
        }

        // Check with token if player one actually is the one who sent the data.
        if (data.getToken().equals(client.getToken())) {
            currentRound.setGameMode(data.getGameMode());
            BroadcastGameMode broadcastGameMode;
            if (data.getGameMode() == GameMode.TRUMPF) {
                currentRound.setTrumpfSuit(data.getTrumpfSuit());
                broadcastGameMode = new BroadcastGameMode(new BroadcastGameModeData(data.getGameMode(), data.getTrumpfSuit()));
            } else {
                broadcastGameMode = new BroadcastGameMode(new BroadcastGameModeData(data.getGameMode()));
            }
            RoundRepository.getSingleton(null).update(currentRound);
            broadcast(broadcastGameMode);

            // send turn information to clients
            TurnEntity turn = addNewTurn(client.getUser(), currentRound);
            BroadcastTurn broadcastTurn = new BroadcastTurn(new BroadcastTurnData(turn.getId(),
                turn.getStartingPlayer().getUsername(), null,
                turn.getCards().stream().map(CardEntity::toCardData).collect(Collectors.toList())
                ));
            broadcast(broadcastTurn);
        }
    }

    /**
     * @param message Message to broadcast to all players.
     */
    private void broadcast(final Message message) {
        clientPlayerOne.send(message);
        clientPlayerTwo.send(message);
        clientPlayerThree.send(message);
        clientPlayerFour.send(message);
    }

    /**
     * @param user    The user.
     * @param teamOne The first team.
     * @param teamTwo The second team.
     *
     * @return Returns the ID of the team the user belongs to.
     */
    private int getTeamIdForPlayer(final UserEntity user, final TeamEntity teamOne, final TeamEntity teamTwo) {
        boolean userIsInTeamOne = teamOne.checkIfPlayerIsInTeam(user);
        if (userIsInTeamOne) {
            return teamOne.getId();
        } else {
            return teamTwo.getId();
        }
    }

    /**
     * Sends a ChooseGameMode message to the user defined in the RoundEntity.
     */
    private void sendChooseGameMode() {
        ChooseGameMode chooseGameMode = new ChooseGameMode(new ChooseGameModeData());
        String gameModeChooserUsername = currentRound.getGameModeChooser().getUsername();
        if (gameModeChooserUsername.equals(clientPlayerOne.getUsername())) {
            clientPlayerOne.send(chooseGameMode);
        } else if (gameModeChooserUsername.equals(clientPlayerTwo.getUsername())) {
            clientPlayerTwo.send(chooseGameMode);
        } else if (gameModeChooserUsername.equals(clientPlayerThree.getUsername())) {
            clientPlayerThree.send(chooseGameMode);
        } else if (gameModeChooserUsername.equals(clientPlayerFour.getUsername())) {
            clientPlayerFour.send(chooseGameMode);
        } else {
            throw new IllegalStateException("Unexpected value: " + currentRound.getGameModeChooser().getUsername());
        }
    }


    public void onPlayedCard(final PlayCardData data) {
        // TODO
        boolean isValid = validateMove(data);
        ClientUtil clientUtil = this.getClientUtilByUsername(data.getUsername());

        // If playedCard is first card in current turn no validations have to be made, just set the playedCard as first card of turn
        PlayedCard result = new PlayedCard(new PlayedCardData(isValid));
        clientUtil.send(result);
    }

    private TurnEntity addNewTurn(UserEntity startingPlayer, RoundEntity round) {
        TurnEntity turn = new TurnEntity(round, startingPlayer);
        TurnRepository.getSingleton(null).add(turn);
        return turn;
    }

    private boolean validateMove(PlayCardData data) {
        double randomNumberBetween0And10 = Math.round(Math.random() * 10);
        if(randomNumberBetween0And10 > 5) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @author: Thomas Weber
     *
     * Validates a move from one of the clients for the game mode "Trump"
     *
     * @param playedCard The card which has been played
     * @param deck The deck of the client to have made the move
     *
     * @return True if the move is valid, false if invalid
     */
    private boolean validateMoveTrump(CardEntity playedCard, DeckEntity deck) {
        CardEntity firstCardOfTurn = currentTurn.getCardOne();

        // In case the playedCard is first card of current turn the move is always valid
        if(firstCardOfTurn.equals(playedCard)) { return true; }

        // If playedCard equals the trump suit, the move is always valid
        if (playedCard.getSuit().equals(currentRound.getTrumpfSuit())) {
            return true;
        }

        boolean isValidMove = true;

        // If the suit of the playedCard does not equal the suit of the firstCardOfTurn, it might be an invalid move - depending on if the client had another card in his hands which he must have played.
        if (!firstCardOfTurn.getSuit().equals(playedCard.getSuit())) {
            // TODO - get only the unplayed cards of deck, not complete deck of player.
            ArrayList<CardEntity> unplayedCards = deck.getCards();

            for (int i = 0; i < unplayedCards.size(); i++) {
                // If a card has the same suite as the firstCardOfTurn this has to be played and thus the move is invalid
                if (unplayedCards.get(i).getSuit().equals(firstCardOfTurn.getSuit())) {

                    // Check exception of trump jack as you are never forced to play this card.
                    if (unplayedCards.get(i).getSuit().equals(currentRound.getTrumpfSuit())) {
                        if (unplayedCards.get(i).getRank().getId() != 6) {
                            isValidMove = false;
                            break;
                        }
                    } else {
                        isValidMove = false;
                        break;
                    }

                }
            }

        }

        return isValidMove;
    }

    private ClientUtil getClientUtilByUsername(String username) {
        if (username.equals(clientPlayerOne.getUsername())) {
            return clientPlayerOne;
        } else if (username.equals(clientPlayerTwo.getUsername())) {
            return clientPlayerTwo;
        } else if (username.equals(clientPlayerThree.getUsername())) {
            return clientPlayerThree;
        } else if (username.equals(clientPlayerFour.getUsername())) {
            return clientPlayerFour;
        }
        return null;
    }
}
