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

import bmach.logic.memory.IMainMemory;
import bmach.logic.memory.IMemoryCell;
import java.awt.Color;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import util.patterns.observer.IObserver;

/**
 *
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public class JMemoryList extends JList  implements IObserver{
    private DefaultListModel model;
    public JMemoryList() {
        super();
        init();
    }

    public JMemoryList(IMainMemory memory){
        super();
        this.model = new DefaultListModel();
        init();
        updateModel(memory);
    }

    public void setMemory(IMainMemory memory){
        updateModel(memory);
    }

    private void init(){

        this.model = new DefaultListModel();
        this.setModel(model);

        this.setCellRenderer(new JByteContainerPanel());
        this.setLayoutOrientation(JList.HORIZONTAL_WRAP);

        this.setVisibleRowCount(128);
        this.setBackground(Color.white);
        this.setSelectionBackground(new Color(126,171,219));
    }

    private void updateModel(IMainMemory memory){
        model.clear();
        for(int i=0; i<memory.getNumberOfCells();i++){
            IMemoryCell cell = memory.get(i);
            if (cell != null) {
                this.model.addElement(cell);
                cell.addObserver(this);
            }
        }
        this.setModel(model);
    }

    public void notifyObserver(Object notificationData) {
        this.repaint();
    }
}
