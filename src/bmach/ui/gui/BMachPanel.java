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

/*
 * BMachPanel.java
 *
 * Created on Nov 21, 2010, 7:16:10 PM
 */
package bmach.ui.gui;

import bmach.logic.machine.IMachine;
import bmach.logic.machine.Machine;
import bmach.logic.machine.parser.MalformedInstructionException;
import bmach.logic.memory.IMainMemory;
import bmach.logic.processor.IProcessor;
import bmach.logic.processor.MalformedProcessorInstructionException;
import bmach.logic.processor.ProcessorNotificationData;
import bmach.logic.processor.ProcessorUtils;
import bmach.logic.registers.IRegister;
import bmach.ui.gui.actions.BMachAction;
import bmach.ui.gui.actions.JDialogSpawningAction;
import bmach.ui.gui.components.JByteContainerPanel;
import bmach.ui.gui.integration.FileDrop;
import documentcontainer.DocumentContainer;
import documentcontainer.DocumentIOManager;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.List;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;
import jsyntaxpane.syntaxkits.BMachSyntaxKit;
import util.binary.bitpattern.BitPatternOverflowException;
import util.binary.bitpattern.BitPatternUtils;
import util.binary.bitpattern.IBitPattern;
import util.patterns.observer.IObserver;

/**
 *
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public class BMachPanel extends javax.swing.JPanel implements IObserver, DocumentContainer, DocumentListener {

    // <editor-fold defaultstate="collapsed" desc="FileFilters">
    private class BMAFileFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            if (f.getAbsolutePath().toLowerCase().endsWith(".bma")) {
                return true;
            }
            return false;
        }

        @Override
        public String getDescription() {
            return "BMach machine language files";
        }
    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Actions">
    private class NewAction extends JDialogSpawningAction {

        public NewAction(JComponent dialogParent) {
            super(dialogParent, "New", "New", new Integer(KeyEvent.VK_N), KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK), "/bmach/ui/gui/resources/page_white_add.png");
        }

        public void actionPerformed(ActionEvent e) {
            try {
                docIOMngr.createNew(getDialogParent(), new BMAFileFilter());
            } catch (IOException ioe) {
                showError("BMach - Error", "I/O error:", "An error occured while trying to save the file.");
            }
        }
    }

    private class OpenAction extends JDialogSpawningAction {

        public OpenAction(JComponent dialogParent) {
            super(dialogParent, "Open", "Open", new Integer(KeyEvent.VK_O), KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK), "/bmach/ui/gui/resources/folder.png");
        }

        public void actionPerformed(ActionEvent e) {
            try {
                docIOMngr.open(getDialogParent(), null);
            } catch (IOException ioe) {
                showError("BMach - Error", "I/O error:", "An error occured while trying to save the file.");
            }
        }
    }

    private class SaveAction extends JDialogSpawningAction {

        public SaveAction(JComponent dialogParent) {
            super(dialogParent, "Save", "Save", new Integer(KeyEvent.VK_S), KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK), "/bmach/ui/gui/resources/disk.png");
        }

        public void actionPerformed(ActionEvent e) {
            try {
                docIOMngr.save(getDialogParent(), new BMAFileFilter());
            } catch (IOException ioe) {
                showError("BMach - Error", "I/O error:", "An error occured while trying to save the file.");
            }
        }
    }

    private class SaveAsAction extends JDialogSpawningAction {

        public SaveAsAction(JComponent dialogParent) {
            super(dialogParent, "Save As..", "Save As..", null, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.SHIFT_MASK + ActionEvent.CTRL_MASK), "/bmach/ui/gui/resources/disk_multiple.png");
        }

        public void actionPerformed(ActionEvent e) {
            try {
                docIOMngr.saveAs(getDialogParent(), new BMAFileFilter());
            } catch (IOException ioe) {
                showError("BMach - Error", "I/O error:", "An error occured while trying to save the file.");
            }
        }
    }

    private class ExecuteAction extends BMachAction {

        public ExecuteAction() {
            super("Execute", "Execute", new Integer(KeyEvent.VK_J), KeyStroke.getKeyStroke(KeyEvent.VK_J, ActionEvent.CTRL_MASK), "/bmach/ui/gui/resources/control_play_blue.png");
        }

        public void actionPerformed(ActionEvent e) {
            if (isEnabled()) {
                loadAndRun(false);
            }
        }
    }

    private class StopAction extends BMachAction {

        public StopAction() {
            super("Stop", "Stop", new Integer(KeyEvent.VK_L), KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK), "/bmach/ui/gui/resources/control_stop_blue.png");
            this.setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            if (isEnabled()) {
                stop();
            }
        }
    }

    private class ExecStepAction extends BMachAction {

        public ExecStepAction() {
            super("Execute step-by-step", "Execute step-by-step", new Integer(KeyEvent.VK_K), KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK), "/bmach/ui/gui/resources/control_end_blue.png");
        }

        public void setNameAndTooltip(String name, String tooltip) {
            this.putValue(Action.NAME, name);
            this.putValue(Action.SHORT_DESCRIPTION, tooltip);
        }

        public void actionPerformed(ActionEvent e) {
            if (isEnabled()) {
                nextStep();
            }
        }
    }

    private class ShowHelpAction extends JDialogSpawningAction {

        public ShowHelpAction(JComponent dialogParent) {
            super(dialogParent, "Help Contents..", "Help", new Integer(KeyEvent.VK_F1), KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "/bmach/ui/gui/resources/help.png");
        }

        public void actionPerformed(ActionEvent e) {
            showHelpDialog();
        }
    }

    private class ShowAboutDialogAction extends JDialogSpawningAction {

        public ShowAboutDialogAction(JComponent dialogParent) {
            super(dialogParent, "About..", "About", null, null, "/bmach/ui/gui/resources/information.png");
        }

        public void actionPerformed(ActionEvent e) {
            showAboutDialog();
        }
    }

    /*private class CopyLogAction extends BMachAction {

        public CopyLogAction() {
            super("Copy", "Copy", null, null, "/bmach/ui/gui/resources/copy-to-clipboard.png");
        }

        public void actionPerformed(ActionEvent e) {
            jTextArea2.copy();
        }
    }*/


    private abstract class AbstractClipBoardAction extends BMachAction {
        protected JTextComponent jTextComponent;

        public AbstractClipBoardAction(JTextComponent jTextComponent, String name, String tooltip, Integer mnemonicKey, KeyStroke accelKey, String smallIconURL) {
            super(name, tooltip, mnemonicKey, accelKey, smallIconURL);
            this.jTextComponent = jTextComponent;
        }


    }
    
    private class CutToClipBoardAction extends AbstractClipBoardAction {

        public CutToClipBoardAction(JTextComponent jTextComponent) {
            super(jTextComponent, "Cut", "Cut", null, null, "/bmach/ui/gui/resources/cut-to-clipboard.png");
        }

        public void actionPerformed(ActionEvent e) {
            jTextComponent.cut();
        }
    }

    private class CopyToClipBoardAction extends AbstractClipBoardAction {

        public CopyToClipBoardAction(JTextComponent jTextComponent) {
            super(jTextComponent, "Copy", "Copy", null, null, "/bmach/ui/gui/resources/copy-to-clipboard.png");
        }

        public void actionPerformed(ActionEvent e) {
            jTextComponent.copy();
        }
    }

    private class PasteFromClipBoardAction extends AbstractClipBoardAction {

        public PasteFromClipBoardAction(JTextComponent jTextComponent) {
            super(jTextComponent, "Paste", "Paste", null, null, "/bmach/ui/gui/resources/paste-from-clipboard.png");
        }

        public void actionPerformed(ActionEvent e) {
            jTextComponent.paste();
        }
    }
    // </editor-fold>

    private IMachine machine;
    private Thread machineThread;
    private final String NEW_DOCUMENT_TITLE = "Untitled";
    private boolean docModified;
    private boolean docNew;
    private String docTitle;
    private File docFile;
    private DocumentIOManager docIOMngr;
    private Action newAction;
    private Action openAction;
    private Action saveAction;
    private Action saveAsAction;
    private Action executeAction;
    private ExecStepAction executeStepByStepAction;
    private Action stopAction;
    private Action showHelpAction;
    private Action showAboutDialogAction;

    /** Creates new form BMachPanel */
    public BMachPanel() {
        init();
        initComponents();
        initUI();
    }

    private void init() {
        machine = new Machine();
        machineThread = new Thread(machine);
        docIOMngr = new DocumentIOManager(this, this.getClass());

        this.docModified = false;
        this.docNew = true;

        this.docFile = null;
        this.docTitle = NEW_DOCUMENT_TITLE;
    }

    private void initUI() {
        initRegistersAndMemoryComponents();
        initEditor();
        initToolbarsAndMenus();

        Border border = BorderFactory.createEmptyBorder( 0, 0, 0, 0 );
        jScrollPane1.setBorder( border );
        jScrollPane1.setViewportBorder(border);
        jScrollPane2.setBorder( border );
        jScrollPane2.setViewportBorder(border);
        jScrollPane4.setBorder( border );
        jScrollPane4.setViewportBorder(border);
    }

    private void initRegistersAndMemoryComponents() {
        IProcessor p = machine.getProcessor();
        p.addObserver(this);

        jByteContainerPanel1.setAddress(" PC ");
        p.getProgramCounter().addObserver(jByteContainerPanel1);

        jByteContainerPanel2.setValue(" 0x0000 ");
        p.getProgramCounter().addObserver(jByteContainerPanel1);

        List<JByteContainerPanel> registerPanels = jRegistersPanel1.getRegisterPanels();
        for (int i = 0; i < registerPanels.size(); i++) {
            IRegister r = p.getRegister(i);
            if (r != null) {
                JByteContainerPanel regPan = registerPanels.get(i);
                regPan.setAddress(r.getAddress().toHexString());
                regPan.setValue(r.getContent().toHexString());
                r.addObserver(regPan);
            }
        }

        IMainMemory memory = machine.getMemory();
        jMemoryList1.setMemory(memory);
    }

    private void initEditor() {
        BMachSyntaxKit.initKit();
        jEditorPane1.setContentType("text/bmach");
        jEditorPane1.getDocument().addDocumentListener(this);
    }

    private void initToolbarsAndMenus() {

        newAction = new NewAction(this);
        openAction = new OpenAction(this);
        saveAction = new SaveAction(this);
        saveAsAction = new SaveAsAction(this);
        executeAction = new ExecuteAction();
        executeStepByStepAction = new ExecStepAction();
        stopAction = new StopAction();
        showHelpAction = new ShowHelpAction(this);
        showAboutDialogAction = new ShowAboutDialogAction(this);

        jButton1.setAction(newAction);
        jButton1.setText("");

        jButton2.setAction(openAction);
        jButton2.setText("");

        jButton3.setAction(saveAction);
        jButton3.setText("");

        jButton5.setAction(executeAction);
        jButton5.setText("");

        jButton6.setAction(executeStepByStepAction);
        jButton6.setText("");

        jButton7.setAction(stopAction);
        jButton7.setText("");

        jButton8.setAction(showHelpAction);
        jButton8.setText("");

        jButton9.setAction(showAboutDialogAction);
        jButton9.setText("");

        BMachSyntaxKit kit = ((BMachSyntaxKit) jEditorPane1.getEditorKit());
        kit.addPopupMenu(jEditorPane1);
        kit.addToolBarActions(jEditorPane1, jToolBar1, 4);

        JPopupMenu logPopUp = new JPopupMenu();
        logPopUp.add(new CopyToClipBoardAction(jTextArea2));
        jTextArea2.setComponentPopupMenu(logPopUp);

        JPopupMenu addressFieldPopUp = new JPopupMenu();
        addressFieldPopUp.add(new CutToClipBoardAction(jAddressTextField1));
        addressFieldPopUp.add(new CopyToClipBoardAction(jAddressTextField1));
        addressFieldPopUp.add(new PasteFromClipBoardAction(jAddressTextField1));
        jAddressTextField1.setComponentPopupMenu(addressFieldPopUp);
    }

    public Action getNewAction() {
        return newAction;
    }

    public Action getOpenAction() {
        return openAction;
    }

    public Action getSaveAction() {
        return saveAction;
    }

    public Action getSaveAsAction() {
        return saveAsAction;
    }
    
    public Action getExecuteAction() {
        return executeAction;
    }
    
    public Action getExecuteStepByStepAction() {
        return executeStepByStepAction;
    }

    public Action getStopAction() {
        return stopAction;
    }

    public Action getShowHelpAction() {
        return showHelpAction;
    }

    public Action getShowAboutDialogAction() {
        return showAboutDialogAction;
    }

    public JMenu getEditMenu(){
        BMachSyntaxKit kit = ((BMachSyntaxKit) jEditorPane1.getEditorKit());
        return kit.getJMenu(jEditorPane1);
    }

    public void setFocusOnEditor(){
        jEditorPane1.requestFocusInWindow();
    }

    // <editor-fold defaultstate="collapsed" desc="Drag n Drop support">
    public void addFileDropSupport() {
        FileDrop fileDrop = new FileDrop(this.getTopLevelAncestor(), new FileDrop.Listener() {

            public void filesDropped(java.io.File[] files) {
                if (files.length > 0) {
                    File myFile = files[0];
                    if (myFile != null) {
                        fileDropped(myFile);
                    }
                }
            }
        });
    }

    private void fileDropped(File f) {
        try{
            docIOMngr.open(this, f, new BMAFileFilter(), Charset.defaultCharset());
        }catch(IOException ioe){
            showError("BMach - Error", "I/O error:", "An error occured while trying to save the file");
        }
    }

    //</editor-fold>

    public void changedUpdate(DocumentEvent e) {
        setCurrentDocModified(true);
    }

    public void removeUpdate(DocumentEvent e) {
        setCurrentDocModified(true);
    }

    public void insertUpdate(DocumentEvent e) {
        setCurrentDocModified(true);
    }

    public boolean isCurrentDocModified() {
        return docModified;
    }

    public boolean isCurrentDocNew() {
        return docNew;
    }

    public void setCurrentDocModified(boolean modified) {
        this.docModified = modified;
        updateTitle();
    }

    public File getCurrentDocFile() {
        return docFile;
    }

    public String getCurrentDocTitle() {
        return docTitle;
    }

    public boolean openDocument(File input) {
        try {
            BufferedReader r = new BufferedReader(new FileReader(input));

            String line;
            StringBuilder sb = new StringBuilder();
            try {
                while ((line = r.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
                r.close();
                jEditorPane1.setText(sb.toString());
                this.docFile = input;
                this.docTitle = input.getName();
                setCurrentDocModified(false);
                this.docNew = false;
                return true;
            } catch (IOException ioe) {
                showError("BMach - Error", "I/O error:", "An error occured while trying to save the file");
                return false;
            }
        } catch (FileNotFoundException fnfe) {
            showError("BMach - Error", "File not found", input.getAbsolutePath());
            return false;
        }
    }

    public boolean saveDocument(File output) {
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(output));
            w.write(jEditorPane1.getText());
            w.flush();
            w.close();
            this.docFile = output;
            this.docTitle = output.getName();
            setCurrentDocModified(false);
            this.docNew = false;
            return true;
        } catch (IOException ioe) {
            showError("BMach - Error", "I/O error:", "An error occured while trying to save the file");
            return false;
        }
    }

    public boolean newDocument() {
        this.jEditorPane1.setText("");
        this.docFile = null;
        this.docTitle = NEW_DOCUMENT_TITLE;
        setCurrentDocModified(false);
        this.docNew = true;
        return true;
    }

    public void recentlyAccessedFilesChanged() {
    }

    public boolean queryExit() {
        if (isCurrentDocModified()) {
            int saveChanges = docIOMngr.showModifiedWarning(this, getCurrentDocTitle());
            if (saveChanges == JOptionPane.YES_OPTION) {
                try{
                    return docIOMngr.save(this, new BMAFileFilter());
                }catch(IOException ioe){
                    showError("BMach - Error", "I/O error:", "An error occured while trying to save the file");
                    return false;
                }
            } else if (saveChanges == JOptionPane.NO_OPTION) {
                return true;
            }else{
                return false;
            }
        } else {
            return true;
        }
    }

    private void updateTitle() {
        Container tla = this.getTopLevelAncestor();
        if (tla instanceof JFrame) {
            String title = "BMach - " + docTitle + (docModified ? "*" : "");
            ((JFrame) tla).setTitle(title);
        }
    }

    private void enableOrDisableControls(boolean machineRunning) {
        stopAction.setEnabled(machineRunning);
        executeAction.setEnabled(!machineRunning);
        executeStepByStepAction.setEnabled(!machineRunning);
        jSlider1.setEnabled(!machineRunning);
        if (machineRunning) {
            executeStepByStepAction.setNameAndTooltip("Next step", "Next step");
        } else {
            executeStepByStepAction.setNameAndTooltip("Execute step-by-step", "Execute step-by-step");
        }
        jButton6.setText("");
    }

    public void notifyObserver(Object notificationData) {
        if (notificationData instanceof ProcessorNotificationData) {
            Object data = ((ProcessorNotificationData) notificationData).getData();
            if (data instanceof IBitPattern) {
                String instBits = ((IBitPattern) data).toBinaryString().substring(8);
                String instAddress = ((IBitPattern) data).toBinaryString().substring(0, 8);

                jByteContainerPanel2.setAddress("0x"+BitPatternUtils.toHexString(Integer.parseInt(instAddress, 2), 2).toUpperCase());
                jByteContainerPanel2.setValue("0x"+BitPatternUtils.toHexString(Integer.parseInt(instBits, 2), 4).toUpperCase());

                String inst = ProcessorUtils.instructionToString(instBits);
                jLabel7.setText("<html>" + inst + "</html>");
                jTextArea2.append(inst + "\n");

                if (machine.getStepByStep()) {
                    executeStepByStepAction.setEnabled(true);
                }
            } else if (data instanceof String) {
                if (((String) data).equals("halt")) {
                    enableOrDisableControls(false);
                }
            } else if (data instanceof MalformedProcessorInstructionException) {
                enableOrDisableControls(false);
            } else if (data instanceof BitPatternOverflowException) {
                enableOrDisableControls(false);
                showError("BMach - Error", "Overflow:", ((BitPatternOverflowException) data).getMessage());
            }
        }
    }

    private void loadAndRun(boolean stepByStep) {
        if (!machineThread.isAlive()) {
            enableOrDisableControls(true);
            machine.reset();
            machine.setSleepTime( (100 - jSlider1.getValue()) * 10 );
            jTextArea2.setText("");
            machine.setStepByStep(stepByStep);
            try {
                machine.loadInstructions(new StringReader(jEditorPane1.getText()));
                machineThread = new Thread(machine);
                machineThread.start();
            } catch (MalformedInstructionException mie) {
                showError("BMach - Error", "Malformed instruction (line " + mie.getLine() + "):", mie.getMessage());
                enableOrDisableControls(false);
            }

        }
    }

    private void nextStep() {
        executeStepByStepAction.setEnabled(false);
        if (!machineThread.isAlive()) {
            loadAndRun(true);
        } else {
            machine.nextStep();
        }
    }

    private void stop() {
        machine.getProcessor().halt();
    }

    private void showError(String title, String error, String msg){
        JOptionPane.showMessageDialog(this, "<html><b>"+error+"</b><br>" + msg + "</html>", title, JOptionPane.ERROR_MESSAGE);
    }

    private void showAboutDialog() {
        JFrame frame = null;
        Container tla = this.getTopLevelAncestor();
        if (tla instanceof JFrame) {
            frame = (JFrame) tla;
        }
        new BMachAboutDialog(frame, true).setVisible(true);
    }

    private void showHelpDialog() {
        JFrame frame = null;
        Container tla = this.getTopLevelAncestor();
        if (tla instanceof JFrame) {
            frame = (JFrame) tla;
        }
        BMachHelpDialog.getDialog(frame, false).setVisible(true);
    }

    private void goToAddress(String address) {
        if(address.matches("0x[0-9a-fA-F]{2}")){
            int index = Integer.parseInt(address.substring(2), 16);
            jMemoryList1.setSelectedIndex(index);
            jMemoryList1.ensureIndexIsVisible(index);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jSeparator11 = new javax.swing.JToolBar.Separator();
        jSlider1 = new javax.swing.JSlider();
        jSeparator8 = new javax.swing.JToolBar.Separator();
        jLabel6 = new javax.swing.JLabel();
        jButton9 = new javax.swing.JButton();
        jSeparator10 = new javax.swing.JToolBar.Separator();
        jButton8 = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jRegistersPanel1 = new bmach.ui.gui.components.JRegistersPanel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel2 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jPanel12 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jMemoryList1 = new bmach.ui.gui.components.JMemoryList();
        jPanel15 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jAddressTextField1 = new bmach.ui.gui.components.JAddressTextField();
        jPanel7 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel4 = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JSeparator();
        jPanel10 = new javax.swing.JPanel();
        jByteContainerPanel1 = new bmach.ui.gui.components.JByteContainerPanel();
        jPanel11 = new javax.swing.JPanel();
        jByteContainerPanel2 = new bmach.ui.gui.components.JByteContainerPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        jPanel4 = new javax.swing.JPanel();
        jSeparator9 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        jPanel14 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        jButton1.setText("New");
        jButton1.setToolTipText("New");
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton1);

        jButton2.setText("Open");
        jButton2.setToolTipText("Open");
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton2);

        jButton3.setText("Save");
        jButton3.setToolTipText("Save");
        jButton3.setFocusable(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton3);
        jToolBar1.add(jSeparator6);
        jToolBar1.add(jSeparator7);

        jButton5.setText("Stop");
        jButton5.setToolTipText("Stop");
        jButton5.setEnabled(false);
        jButton5.setFocusable(false);
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton5);

        jButton6.setText("Execute step-by-step");
        jButton6.setToolTipText("Execute step-by-step");
        jButton6.setFocusable(false);
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton6);

        jButton7.setText("Execute");
        jButton7.setToolTipText("Execute");
        jButton7.setFocusable(false);
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton7);
        jToolBar1.add(jSeparator11);

        jSlider1.setToolTipText("Execution speed");
        jSlider1.setFocusable(false);
        jToolBar1.add(jSlider1);
        jToolBar1.add(jSeparator8);
        jToolBar1.add(jLabel6);

        jButton9.setText("About");
        jButton9.setToolTipText("About...");
        jButton9.setFocusable(false);
        jButton9.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton9.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton9);
        jToolBar1.add(jSeparator10);

        jButton8.setText("Help");
        jButton8.setToolTipText("Help");
        jButton8.setFocusable(false);
        jButton8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton8.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton8);

        jSplitPane1.setDividerLocation(350);
        jSplitPane1.setResizeWeight(1.0);
        jSplitPane1.setContinuousLayout(true);
        jSplitPane1.setDoubleBuffered(true);

        jPanel3.setMinimumSize(new java.awt.Dimension(550, 454));
        jPanel3.setPreferredSize(new java.awt.Dimension(600, 454));

        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel6.setLayout(new javax.swing.BoxLayout(jPanel6, javax.swing.BoxLayout.LINE_AXIS));

        jRegistersPanel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 10, true));
        jPanel6.add(jRegistersPanel1);

        jLabel1.setFont(jLabel1.getFont().deriveFont((jLabel1.getFont().getStyle() & ~java.awt.Font.ITALIC) | java.awt.Font.BOLD, 14));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Registers");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 538, Short.MAX_VALUE)
            .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 538, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel2.setFont(jLabel2.getFont().deriveFont((jLabel2.getFont().getStyle() & ~java.awt.Font.ITALIC) | java.awt.Font.BOLD, 14));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Main Memory");

        jPanel12.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jMemoryList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(jMemoryList1);

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)
        );

        jLabel8.setFont(jLabel8.getFont().deriveFont((jLabel8.getFont().getStyle() & ~java.awt.Font.ITALIC) & ~java.awt.Font.BOLD, 12));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setLabelFor(jAddressTextField1);
        jLabel8.setText("Go to address:");

        jButton4.setText("go");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jAddressTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jAddressTextField1KeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jAddressTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4))
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel8)
                .addComponent(jButton4)
                .addComponent(jAddressTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
            .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
            .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel12, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel3.setFont(jLabel3.getFont().deriveFont((jLabel3.getFont().getStyle() & ~java.awt.Font.ITALIC) | java.awt.Font.BOLD, 14));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Program Counter");

        jLabel4.setFont(jLabel4.getFont().deriveFont((jLabel4.getFont().getStyle() & ~java.awt.Font.ITALIC) | java.awt.Font.BOLD, 14));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Last Executed Instruction");

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));
        jPanel10.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel10.add(jByteContainerPanel1);

        jPanel11.setBackground(new java.awt.Color(255, 255, 255));
        jPanel11.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel11.add(jByteContainerPanel2);

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));
        jPanel8.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel7.setFont(jLabel7.getFont().deriveFont((jLabel7.getFont().getStyle() & ~java.awt.Font.ITALIC) & ~java.awt.Font.BOLD, 12));
        jLabel7.setText("<html>-</html>");
        jLabel7.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
            .addComponent(jSeparator3, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
            .addComponent(jSeparator5, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
            .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
            .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 550, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 502, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane1.setRightComponent(jPanel3);

        jPanel4.setMinimumSize(new java.awt.Dimension(350, 454));
        jPanel4.setPreferredSize(new java.awt.Dimension(200, 454));

        jLabel5.setFont(jLabel5.getFont().deriveFont((jLabel5.getFont().getStyle() & ~java.awt.Font.ITALIC) | java.awt.Font.BOLD, 14));
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Instruction Log");

        jPanel13.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel13.setLayout(new javax.swing.BoxLayout(jPanel13, javax.swing.BoxLayout.LINE_AXIS));

        jEditorPane1.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane2.setViewportView(jEditorPane1);

        jPanel13.add(jScrollPane2);

        jPanel14.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel14.setLayout(new javax.swing.BoxLayout(jPanel14, javax.swing.BoxLayout.LINE_AXIS));

        jScrollPane4.setPreferredSize(new java.awt.Dimension(182, 150));

        jTextArea2.setColumns(20);
        jTextArea2.setEditable(false);
        jTextArea2.setRows(5);
        jScrollPane4.setViewportView(jTextArea2);

        jPanel14.add(jScrollPane4);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel14, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
                    .addComponent(jPanel13, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
                    .addComponent(jSeparator9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator9, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel4);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 906, Short.MAX_VALUE)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 906, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        goToAddress(jAddressTextField1.getText());
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jAddressTextField1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jAddressTextField1KeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            goToAddress(jAddressTextField1.getText());
        }
    }//GEN-LAST:event_jAddressTextField1KeyPressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private bmach.ui.gui.components.JAddressTextField jAddressTextField1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private bmach.ui.gui.components.JByteContainerPanel jByteContainerPanel1;
    private bmach.ui.gui.components.JByteContainerPanel jByteContainerPanel2;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private bmach.ui.gui.components.JMemoryList jMemoryList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private bmach.ui.gui.components.JRegistersPanel jRegistersPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator10;
    private javax.swing.JToolBar.Separator jSeparator11;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JToolBar.Separator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables
}
