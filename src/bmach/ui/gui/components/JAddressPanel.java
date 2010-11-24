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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public class JAddressPanel extends JPanel {

    private float arcw;
    private float arch;
    private JLabel addressLabel;
    private Paint borderPaint;
    private Paint separatorPaint;
    private Color bgGradientColor1;
    private Color bgGradientColor2;
    private Stroke borderStroke;
    private Font addressFont;

    public JAddressPanel() {


        this.borderPaint = new Color(106, 106, 106);
        this.separatorPaint = new Color(155, 155, 155);
        this.bgGradientColor1 = new Color(255, 255, 255);
        this.bgGradientColor2 = new Color(98, 98, 98);

        this.borderStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

        this.addressFont = new Font(null, Font.BOLD, 14);

        this.arcw = 30;
        this.arch = 30;

        initComponents();

    }

    public void setAddress(String address) {
        this.addressLabel.setText(address);
    }

    public String getAddress(){
        return this.addressLabel.getText();
    }

    private void initComponents() {
        this.addressLabel = new JLabel("0x00");
        this.addressLabel.setFont(addressFont);
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(Box.createHorizontalStrut(10));
        this.add(addressLabel);
        this.add(Box.createHorizontalStrut(5));
        this.setPreferredSize(new Dimension(50,30));
    }

    @Override
    protected void paintComponent(Graphics g) {
        int width = this.getWidth();
        int height = this.getHeight();
        RoundRectangle2D boundingRect = new RoundRectangle2D.Float(0, 0, width + arcw, height - 1, arcw, arch);
        initGraphics((Graphics2D) g);
        paintBg((Graphics2D) g, boundingRect);
        paintBorder((Graphics2D) g, boundingRect);
    }

    private void initGraphics(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    private void paintBg(Graphics2D g2d, RoundRectangle2D boundingRect) {
        GradientPaint bgPaint = new GradientPaint(0, 0, bgGradientColor1, 0, (int) boundingRect.getHeight(), bgGradientColor2);
        g2d.setPaint(bgPaint);
        g2d.fill(boundingRect);
    }

    private void paintBorder(Graphics2D g2d, RoundRectangle2D boundingRect) {
        g2d.setPaint(borderPaint);
        g2d.setStroke(borderStroke);
        g2d.draw(boundingRect);
        float x = (float) this.getWidth() - 1;
        float y = (float) boundingRect.getHeight() - 2;
        g2d.setPaint(separatorPaint);
        g2d.draw(new Line2D.Float(x, 0, x, y));
    }
}
