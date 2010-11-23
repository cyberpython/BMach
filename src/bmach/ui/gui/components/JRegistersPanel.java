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

package bmach.ui.gui.components;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 *
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public class JRegistersPanel extends JPanel{

    List<JByteContainerPanel> registerPanels;

    public JRegistersPanel() {
        super();
        this.setBackground(Color.white);
        init();
    }

    private void init(){

        this.setLayout( new GridLayout(4, 4, 0, 0));

        this.registerPanels = new ArrayList<JByteContainerPanel>();
        for(int i=0; i<16; i++){
            JByteContainerPanel registerPanel = new JByteContainerPanel();
            this.registerPanels.add(registerPanel);
            this.add(registerPanel);
        }
    }

    public List<JByteContainerPanel> getRegisterPanels() {
        return registerPanels;
    }

}
