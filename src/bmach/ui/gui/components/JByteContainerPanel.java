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

import bmach.logic.memory.IMemoryCell;
import bmach.logic.memory.MemoryCellNotificationData;
import bmach.logic.programcounter.IProgramCounter;
import bmach.logic.programcounter.ProgramCounterNotificationData;
import bmach.logic.registers.IRegister;
import bmach.logic.registers.RegisterNotificationData;
import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import util.patterns.observer.IObserver;

/**
 *
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public class JByteContainerPanel extends JPanel implements IObserver, ListCellRenderer {

    private JAddressPanel addressPanel;
    private JValuePanel valuePanel;
    private Color transparentColor;

    public JByteContainerPanel() {
        initComponents();
        this.transparentColor = new Color(0,0,0,0);
        this.setBackground(Color.white);
        this.setOpaque(false);

    }

    private void initComponents() {
        this.transparentColor = new Color(0,0,0,0);
        this.addressPanel = new JAddressPanel();
        this.valuePanel = new JValuePanel();
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(addressPanel);
        this.add(valuePanel);
        this.setBorder(BorderFactory.createLineBorder(transparentColor, 5));
    }

    public void setAddress(String address){
        addressPanel.setAddress(address);
    }
    
    public String getAddress(){
        return addressPanel.getAddress();
    }


    public void setValue(String value){
        valuePanel.setValue(value);
    }

    public String getValue(){
        return valuePanel.getValue();
    }

    public void notifyObserver(Object notificationData) {
        if (notificationData instanceof MemoryCellNotificationData) {
            IMemoryCell cell = ((MemoryCellNotificationData) notificationData).getMemoryCell();
            addressPanel.setAddress(cell.getAddress().toHexString());
            valuePanel.setValue(cell.getContent().toHexString());
        } else if (notificationData instanceof RegisterNotificationData) {
            IRegister register = ((RegisterNotificationData) notificationData).getSender();
            addressPanel.setAddress(register.getAddress().toHexString());
            valuePanel.setValue(register.getContent().toHexString());
        } else if (notificationData instanceof ProgramCounterNotificationData) {
            addressPanel.setAddress(" PC ");
            valuePanel.setValue(((ProgramCounterNotificationData) notificationData).getAddress().toHexString());
        }
        this.repaint();
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof IMemoryCell) {
            IMemoryCell cell = (IMemoryCell)value ;
            addressPanel.setAddress(cell.getAddress().toHexString());
            valuePanel.setValue(cell.getContent().toHexString());

            /*int selIndex = list.getSelectedIndex();
            if(selIndex % 2 == 0){
                int[] indices = {selIndex, selIndex+1};
                list.setSelectedIndices(indices);
            }*/

            if(isSelected){
                this.setBackground(list.getSelectionBackground());
                this.setOpaque(true);
            }else{
                this.setBackground(transparentColor);
                this.setOpaque(false);
            }
        } else if (value instanceof IRegister) {
            IRegister register = (IRegister)value ;
            addressPanel.setAddress(register.getAddress().toHexString());
            valuePanel.setValue(register.getContent().toHexString());
        }
        return this;
    }
}
