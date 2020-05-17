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

package jass.client.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import jass.client.entity.ServerEntity;
import jass.client.view.AboutView;
import jass.client.view.LobbyView;
import jass.client.view.LoginView;
import jass.client.view.RegisterView;
import jass.client.view.ServerConnectionView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import jass.client.entity.LoginEntity;
import jass.client.repository.LoginRepository;
import jass.client.eventlistener.DisconnectEventListener;
import jass.client.mvc.Controller;
import jass.client.message.Login;
import jass.client.util.I18nUtil;
import jass.client.util.SocketUtil;
import jass.client.util.WindowUtil;
import jass.client.util.ViewUtil;
import jass.lib.message.LoginData;
import jass.lib.servicelocator.ServiceLocator;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The controller for the server connection view.
 *
 * @author Manuele Vaccari
 * @version %I%, %G%
 * @since 1.0.0
 */
public final class LoginController extends Controller implements DisconnectEventListener {
    /**
     * The logger to print to console and save in a .log file.
     */
    private static final Logger logger = LogManager.getLogger(LoginController.class);

    /**
     * The view.
     */
    private LoginView view;

    /**
     * The "File" element.
     */
    @FXML
    private Menu mFile;

    /**
     * The "File -> Change Language" element.
     */
    @FXML
    private Menu mFileChangeLanguage;

    /**
     * The "File -> Disconnect" element.
     */
    @FXML
    private MenuItem mFileDisconnect;

    /**
     * The "File -> Exit" element.
     */
    @FXML
    private MenuItem mFileExit;

    /**
     * The "Edit" element.
     */
    @FXML
    private Menu mEdit;

    /**
     * The "Edit -> Delete" element.
     */
    @FXML
    private MenuItem mEditDelete;

    /**
     * The "Help" element.
     */
    @FXML
    private Menu mHelp;

    /**
     * The "Help -> About" element.
     */
    @FXML
    private MenuItem mHelpAbout;

    /**
     * The navbar.
     */
    @FXML
    private Text navbar;

    /**
     * The error message.
     */
    @FXML
    private VBox errorMessage;

    /**
     * The username text field.
     */
    @FXML
    private JFXTextField username;

    /**
     * The password field.
     */
    @FXML
    private JFXPasswordField password;

    /**
     * The "remember me" checkbox.
     */
    @FXML
    private JFXCheckBox connectAutomatically;

    /**
     * The login button.
     */
    @FXML
    private JFXButton login;

    /**
     * The register button.
     */
    @FXML
    private JFXButton register;

    /**
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        /*
         * Register oneself for disconnect events
         */
        SocketUtil socket = ServiceLocator.get(SocketUtil.class);
        assert socket != null;
        socket.addDisconnectListener(this);

        /*
         * Bind all texts
         */
        mFile.textProperty().bind(I18nUtil.createStringBinding(mFile.getText()));
        mFileChangeLanguage.textProperty().bind(I18nUtil.createStringBinding(mFileChangeLanguage.getText()));
        ViewUtil.useLanguageMenuContent(mFileChangeLanguage);
        mFileDisconnect.textProperty().bind(I18nUtil.createStringBinding(mFileDisconnect.getText()));
        mFileExit.textProperty().bind(I18nUtil.createStringBinding(mFileExit.getText()));
        mFileExit.setAccelerator(KeyCombination.keyCombination("Alt+F4"));

        mEdit.textProperty().bind(I18nUtil.createStringBinding(mEdit.getText()));
        mEditDelete.textProperty().bind(I18nUtil.createStringBinding(mEditDelete.getText()));

        mHelp.textProperty().bind(I18nUtil.createStringBinding(mHelp.getText()));
        mHelpAbout.textProperty().bind(I18nUtil.createStringBinding(mHelpAbout.getText()));

        navbar.textProperty().bind(I18nUtil.createStringBinding(navbar.getText()));

        username.promptTextProperty().bind(I18nUtil.createStringBinding(username.getPromptText()));
        password.promptTextProperty().bind(I18nUtil.createStringBinding(password.getPromptText()));

        connectAutomatically.textProperty().bind(I18nUtil.createStringBinding(connectAutomatically.getText()));

        login.textProperty().bind(I18nUtil.createStringBinding(login.getText()));
        register.textProperty().bind(I18nUtil.createStringBinding(register.getText()));

        /*
         * Disable/Enable the "Connect"-button depending on if the inputs are valid
         */
        AtomicBoolean usernameValid = new AtomicBoolean(false);
        AtomicBoolean passwordValid = new AtomicBoolean(false);
        Runnable updateButtonClickable = () -> login.setDisable(!usernameValid.get() || !passwordValid.get());
        username.textProperty().addListener((o, oldVal, newVal) -> {
            if (!oldVal.equals(newVal)) {
                usernameValid.set(username.validate());
                updateButtonClickable.run();
            }
        });
        password.textProperty().addListener((o, oldVal, newVal) -> {
            if (!oldVal.equals(newVal)) {
                passwordValid.set(password.validate());
                updateButtonClickable.run();
            }
        });

        /*
         * Validate input fields
         */
        username.getValidators().addAll(
            ViewUtil.useRequiredValidator("gui.login.username.empty")
        );
        password.getValidators().addAll(
            ViewUtil.useRequiredValidator("gui.login.password.empty")
        );
    }

    /**
     * Disables all the input fields in the view.
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    public void disableInputs() {
        username.setDisable(true);
        password.setDisable(true);
        connectAutomatically.setDisable(true);
    }

    /**
     * Disables all the form fields in the view.
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    public void disableAll() {
        disableInputs();
        login.setDisable(true);
        register.setDisable(true);
    }

    /**
     * Enables all the input fields in the view.
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    public void enableInputs() {
        username.setDisable(false);
        password.setDisable(false);
        connectAutomatically.setDisable(false);
    }

    /**
     * Enables all the form fields in the view.
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    public void enableAll() {
        enableInputs();
        login.setDisable(false);
        register.setDisable(false);
    }

    /**
     * As the view contains an error message field, this updates the text and
     * the window appropriately.
     *
     * @param translatorKey The key of the translation.
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    public void setErrorMessage(final String translatorKey) {
        Platform.runLater(() -> {
            if (errorMessage.getChildren().size() == 0) {
                // Make window larger, so it doesn't become crammed, only if we
                // haven't done so yet
                // TODO: This keeps the window size even after switching to e.g.
                //  login
                //view.getStage().setHeight(view.getStage().getHeight() + 30);
                errorMessage.setPrefHeight(50);
            }
            Text text = ViewUtil.useText(translatorKey);
            text.setFill(Color.RED);
            errorMessage.getChildren().clear();
            errorMessage.getChildren().addAll(text, ViewUtil.useSpacer(20));
        });
    }

    /**
     * Disconnect from the server and returns to the server connection window.
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    @FXML
    private void clickOnDisconnect() {
        SocketUtil socket = ServiceLocator.get(SocketUtil.class);
        if (socket != null) { // Not necessary but keeps IDE happy
            socket.close();
        }
        ServiceLocator.remove(SocketUtil.class);
        WindowUtil.switchTo(view, ServerConnectionView.class);
    }

    /**
     * Shuts down the application.
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    @FXML
    private void clickOnExit() {
        Platform.exit();
    }

    /**
     * Opens the about window.
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    @FXML
    public void clickOnAbout() {
        WindowUtil.openInNewWindow(AboutView.class);
    }

    /**
     * Handles the click on the login button. Inputs should already be checked.
     * This will send it to the server, and update local values if successful.
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    @FXML
    private void clickOnLogin() {
        // Disable everything to prevent something while working on the data
        disableAll();

        // Connection would freeze window (and the animations) so do it in a
        // different thread.
        new Thread(() -> {
            SocketUtil backend = ServiceLocator.get(SocketUtil.class);
            assert backend != null;
            ServerEntity server = ServiceLocator.get(ServerEntity.class);
            assert server != null;

            LoginEntity login = (new LoginEntity())
                .setServer(server)
                .setUsername(username.getText())
                .setPassword(password.getText())
                .setConnectAutomatically(connectAutomatically.isSelected());
            Login loginMsg = new Login(new LoginData(login.getUsername(), login.getPassword()));

            // Send the login request to the server. Update locally if
            // successful.
            if (loginMsg.process(backend)) {
                login.setToken(loginMsg.getToken());

                // Save the login in the db
                // TODO This keeps adding the same entity, check before adding
                if (!LoginRepository.getSingleton(null).add(login)) {
                    logger.error("Couldn't save login data to local database.");
                }

                if (login.isConnectAutomatically()) {
                    // Make sure it's the only entry
                    LoginRepository.getSingleton(null).setToConnectAutomatically(login);
                }

                WindowUtil.switchTo(view, LobbyView.class);
            } else {
                LoginData.Result reason = loginMsg.getResultData().getResultData().optEnum(LoginData.Result.class, "reason");
                if (reason == null) {
                    setErrorMessage("gui.login.login.failed");
                } else {
                    switch (reason) {
                        case USER_DOES_NOT_EXIST:
                            setErrorMessage("gui.login.login.failed.user_does_not_exist");
                            break;
                        case WRONG_PASSWORD:
                            setErrorMessage("gui.login.login.failed.wrong_password");
                            break;
                        case USER_ALREADY_LOGGED_IN:
                            setErrorMessage("gui.login.login.failed.already_logged_in");
                            break;
                        default:
                            setErrorMessage("gui.login.login.failed");
                            break;
                    }
                }
                enableAll();
            }
        }).start();
    }

    /**
     * @return Returns the login button
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    public JFXButton getLogin() {
        return login;
    }

    /**
     * After clicking on register, switch to the register window.
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    @FXML
    private void clickOnRegister() {
        WindowUtil.switchTo(view, RegisterView.class);
    }

    /**
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    @Override
    public void onDisconnectEvent() {
        SocketUtil socket = ServiceLocator.get(SocketUtil.class);
        if (socket != null) { // Not necessary but keeps IDE happy
            socket.close();
        }
        ServiceLocator.remove(SocketUtil.class);
        WindowUtil.switchTo(view, ServerConnectionView.class);
    }

    /**
     * @param view The view.
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    public void setView(final LoginView view) {
        this.view = view;
    }
}
