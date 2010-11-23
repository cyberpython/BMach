/*
 *  Copyright 2010 Georgios Migdos <cyberpython@gmail.com>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package bmach.ui.gui.actions;

import bmach.ui.gui.BMachPanel;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

/**
 *
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public abstract class BMachAction extends AbstractAction {

        public BMachAction(String name, String tooltip, Integer mnemonicKey, KeyStroke accelKey, String smallIconURL) {
            super();
            this.putValue(Action.NAME, name);
            this.putValue(Action.SHORT_DESCRIPTION, tooltip);
            if (mnemonicKey != null) {
                this.putValue(Action.MNEMONIC_KEY, mnemonicKey);
            }
            if(accelKey!=null){
                this.putValue(Action.ACCELERATOR_KEY, accelKey);
            }
            this.putValue(Action.SMALL_ICON, new ImageIcon(BMachPanel.class.getResource(smallIconURL)));
        }
    }
