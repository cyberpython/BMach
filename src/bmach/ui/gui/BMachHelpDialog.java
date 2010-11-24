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
 * BMachHelpDialog.java
 *
 * Created on Nov 23, 2010, 3:48:03 PM
 */
package bmach.ui.gui;

import bmach.ui.gui.actions.BMachAction;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import javax.swing.JPopupMenu;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Style;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

/**
 *
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public class BMachHelpDialog extends javax.swing.JDialog {

    private class CopyLogAction extends BMachAction {

        public CopyLogAction() {
            super("Copy", "Copy", null, null, "/bmach/ui/gui/resources/copy-to-clipboard.png");
        }

        public void actionPerformed(ActionEvent e) {
            jEditorPane1.copy();
        }
    }

    static BMachHelpDialog dlg;
    static{
        dlg = null;
    }

    public static BMachHelpDialog getDialog(Frame parent, boolean modal){
        if(dlg==null){
            dlg = new BMachHelpDialog(parent, modal);
        }
        return dlg;
    }

    /** Creates new form BMachHelpDialog */
    private BMachHelpDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        JPopupMenu logPopUp = new JPopupMenu();
        logPopUp.add(new CopyLogAction());
        jEditorPane1.setComponentPopupMenu(logPopUp);

        StringBuilder sb = new StringBuilder();
        try{
            BufferedReader r = new BufferedReader(new InputStreamReader(BMachHelpDialog.class.getResourceAsStream("/bmach/documentation/help.html")));
            String line;
            while( (line=r.readLine())!=null ){
                sb.append(line);
                sb.append("\n");
            }
            jEditorPane1.setText(sb.toString());
        }catch(IOException ioe){
        }

        jEditorPane1.addHyperlinkListener(new HyperlinkListener() {

            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    if (e.getURL() == null) {
                        jEditorPane1.scrollToReference(e.getDescription().substring(1));
                    } else {
                        try {
                            openUrlInBrowser(e.getURL().toURI());
                        } catch (URISyntaxException use) {
                            use.printStackTrace();
                        }
                    }
                }
            }
        });

        this.setLocationRelativeTo(parent);

        this.addWindowListener(new WindowListener() {

            public void windowOpened(WindowEvent e) {
                jEditorPane1.scrollRectToVisible(new Rectangle(1, 1, 10, 10));
            }

            public void windowClosing(WindowEvent e) {

            }

            public void windowClosed(WindowEvent e) {

            }

            public void windowIconified(WindowEvent e) {

            }

            public void windowDeiconified(WindowEvent e) {

            }

            public void windowActivated(WindowEvent e) {

            }

            public void windowDeactivated(WindowEvent e) {

            }
        });
    }

    private void openUrlInBrowser(URI uri) {
        try {
            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
            desktop.browse(uri);
        } catch (UnsupportedOperationException uoe) {
            this.xdgOpenUrl(uri);
        } catch (Exception e) {
        }
    }

    private void xdgOpenUrl(URI uri) {
        String CROSS_DESKTOP_OPEN_FILE_COMMAND = "/usr/bin/xdg-open";
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.toLowerCase().contains("linux")) {
            try {
                String cmd = "/bin/sh " + CROSS_DESKTOP_OPEN_FILE_COMMAND + " " + uri.toString();
                Runtime rt = Runtime.getRuntime();
                Process p = rt.exec(cmd);
                p.waitFor();
            } catch (Exception e) {
            }
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

        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();

        setTitle("BMach - Help Contents");

        jButton1.setText("Close");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jScrollPane1.setBackground(new java.awt.Color(255, 255, 255));

        jEditorPane1.setBackground(new java.awt.Color(255, 255, 255));
        jEditorPane1.setContentType("text/html");
        jEditorPane1.setEditable(false);
        jScrollPane1.setViewportView(jEditorPane1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 766, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                BMachHelpDialog dialog = new BMachHelpDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
