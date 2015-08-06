package monakhv.samlib.desk;

import monakhv.samlib.db.SQLController;
import monakhv.samlib.desk.data.Settings;
import monakhv.samlib.desk.gui.MainForm;

import java.sql.SQLException;
import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.Locale;

/*
 * Copyright 2015  Dmitry Monakhov
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
 *
 * 2/13/15.
 */
public class Main {
    public Main() throws SQLException, ClassNotFoundException {

    }
    public static void main(String...args) throws SQLException, ClassNotFoundException {

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainForm(Settings.getInstance()).setVisible(true);
            }
        });
    }
    public static void exit(int i){
        System.exit(i);
    }
}
