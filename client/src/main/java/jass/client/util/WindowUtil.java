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

package jass.client.util;

import jass.client.mvc.View;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * A helper class for the controllers to switch between windows easily.
 *
 * @author Manuele Vaccari & Victor Hargrave
 * @version %I%, %G%
 * @since 1.0.0
 */
public final class WindowUtil {
    /**
     * Utility classes, which are collections of static members, are not meant
     * to be instantiated.
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    private WindowUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Uses the existing stage and replaces it's contents with the new view.
     *
     * @param oldView   The old view.
     * @param viewClass The new view.
     *
     * @author Manuele Vaccari & Victor Hargrave
     * @since 1.0.0
     */
    public static void switchTo(final View oldView, final Class<? extends View> viewClass) {
        Platform.runLater(() -> {
            try {
                Constructor<?> constructor = viewClass.getConstructor(Stage.class);
                Stage existingStage = oldView.getStage();
                View view = (View) constructor.newInstance(existingStage);
                view.start();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Opens a new window for the new view and closes the previous window.
     *
     * @param oldView   The old view.
     * @param viewClass The new view.
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    public static void switchToNewWindow(final View oldView, final Class<? extends View> viewClass) {
        openInNewWindow(viewClass);
        Platform.runLater(oldView::stop);
    }

    /**
     * Opens the view in a new window.
     *
     * @param viewClass The new view.
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    public static void openInNewWindow(final Class<? extends View> viewClass) {
        Platform.runLater(() -> {
            try {
                Constructor<?> constructor = viewClass.getConstructor(Stage.class);
                Stage newStage = new Stage();
                View view = (View) constructor.newInstance(newStage);
                view.start();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        });
    }
}
