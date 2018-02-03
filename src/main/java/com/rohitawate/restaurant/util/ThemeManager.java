/*
 * Copyright 2018 Rohit Awate.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rohitawate.restaurant.util;

import javafx.scene.Parent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    private static List<Parent> parentNodes = new ArrayList<>();

    public static void refreshTheme() {
        File themeFile = new File("themes/" + Settings.theme.replaceAll("\"", "") + ".css");
        if (themeFile.exists()) {
            String themePath = themeFile.toURI().toString();

            for (Parent parent : parentNodes) {
                parent.getStylesheets().clear();
                parent.getStylesheets().add(themePath);
            }
        }
    }

    public static void setTheme(Parent parent) {
        /*
            Removes leading and trailing quotation marks since it causes a problem while
            instantiating a file object from this path.
         */
        File themeFile = new File("themes/" + Settings.theme.replaceAll("\"", "") + ".css");
        if (themeFile.exists()) {
            parent.getStylesheets().add(themeFile.toURI().toString());
            parentNodes.add(parent);
        }
    }
}
