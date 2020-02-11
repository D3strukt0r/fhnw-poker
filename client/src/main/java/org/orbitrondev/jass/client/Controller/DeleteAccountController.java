/*
 * fhnw-jass is jass game programmed in java for a school project.
 * Copyright (C) 2020 Manuele Vaccari
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

package org.orbitrondev.jass.client.Controller;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.orbitrondev.jass.client.Entity.LoginEntity;
import org.orbitrondev.jass.client.Message.DeleteLogin;
import org.orbitrondev.jass.client.Message.Logout;
import org.orbitrondev.jass.client.Model.DeleteAccountModel;
import org.orbitrondev.jass.client.Utils.BackendUtil;
import org.orbitrondev.jass.client.View.DeleteAccountView;
import org.orbitrondev.jass.client.View.ViewHelper;
import org.orbitrondev.jass.lib.MVC.Controller;
import org.orbitrondev.jass.lib.Message.DeleteLoginData;
import org.orbitrondev.jass.lib.Message.LogoutData;
import org.orbitrondev.jass.lib.ServiceLocator.ServiceLocator;

/**
 * The controller for the delete account view.
 *
 * @author Manuele Vaccari
 * @version %I%, %G%
 * @since 0.0.1
 */
public class DeleteAccountController extends Controller<DeleteAccountModel, DeleteAccountView> {
    /**
     * Initializes all event listeners for the view.
     *
     * @since 0.0.1
     */
    protected DeleteAccountController(DeleteAccountModel model, DeleteAccountView view) {
        super(model, view);

        // Register ourselves to listen for button clicks
        view.getBtnDelete().setOnAction(event -> clickOnDelete());

        // Register ourselves to listen for button clicks
        view.getBtnCancel().setOnAction(event -> ControllerHelper.switchToDashboardWindow(view));

        // Register ourselves to handle window-closing event
        view.getStage().setOnCloseRequest(event -> Platform.exit());
    }

    /**
     * Disables all the form fields in the view.
     *
     * @since 0.0.1
     */
    public void disableAll() {
        view.getBtnDelete().setDisable(true);
        view.getBtnCancel().setDisable(true);
    }

    /**
     * Enables all the form fields in the view.
     *
     * @since 0.0.1
     */
    public void enableAll() {
        view.getBtnDelete().setDisable(false);
        view.getBtnCancel().setDisable(false);
    }

    /**
     * As the view contains an error message field, this updates the text and the window appropriately.
     *
     * @since 0.0.1
     */
    public void setErrorMessage(String translatorKey) {
        Platform.runLater(() -> {
            if (view.getErrorMessage().getChildren().size() == 0) {
                // Make window larger, so it doesn't become crammed, only if we haven't done so yet
                view.getStage().setHeight(view.getStage().getHeight() + 30);
            }
            Text text = ViewHelper.useText(translatorKey);
            text.setFill(Color.RED);
            view.getErrorMessage().getChildren().clear();
            view.getErrorMessage().getChildren().addAll(text, ViewHelper.useSpacer(20));
        });
    }

    /**
     * Handles the click on the delete button. This will send it to the server, and logout if successful.
     *
     * @since 0.0.1
     */
    public void clickOnDelete() {
        // Disable everything to prevent something while working on the data
        disableAll();

        // Connection would freeze window (and the animations) so do it in a different thread.
        new Thread(() -> {
            BackendUtil backend = (BackendUtil) ServiceLocator.get("backend");
            LoginEntity login = (LoginEntity) ServiceLocator.get("login");
            DeleteLogin deleteLoginMsg = new DeleteLogin(new DeleteLoginData(login.getToken()));

            // Try to delete the account
            if (deleteLoginMsg.process(backend)) {
                Logout logoutMsg = new Logout(new LogoutData());

                // If deleted, try logging out now.
                if (logoutMsg.process(backend)) {
                    ServiceLocator.remove("login");
                    ControllerHelper.switchToLoginWindow(view);
                } else {
                    enableAll();
                    setErrorMessage("gui.deleteAccount.logoutFailed");
                }
            } else {
                enableAll();
                setErrorMessage("gui.deleteAccount.deleteFailed");
            }
        }).start();
    }
}
