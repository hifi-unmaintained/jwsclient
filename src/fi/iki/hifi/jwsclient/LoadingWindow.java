/*
 * Copyright (c) 2013 Toni Spets <toni.spets@iki.fi>
 * 
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package fi.iki.hifi.jwsclient;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

/**
 *
 * @author Toni Spets <toni.spets@iki.fi>
 */
final public class LoadingWindow extends JWindow { 

    private SimpleProgress progress;
    private JLabel status;
    private JLabel statusShadow;
    private JLabel background;
    private ImageIcon image;

    public LoadingWindow() {
        setLayout(null);

        progress = new SimpleProgress(0, 1000);
        progress.setForeground(Color.white);
        progress.setBackground(Color.black);
        progress.setOpaque(true);
        //progress.setBorder(Color.black);
        add(progress);

        status = new JLabel();
        status.setVerticalAlignment(JLabel.BOTTOM);
        status.setFont(new Font("SansSerif", Font.PLAIN, 10));
        status.setForeground(Color.white);
        add(status);

        statusShadow = new JLabel();
        statusShadow.setVerticalAlignment(JLabel.BOTTOM);
        statusShadow.setFont(new Font("SansSerif", Font.PLAIN, 10));
        statusShadow.setForeground(Color.black);
        add(statusShadow);

        background = new JLabel();
        add(background);

        setAlwaysOnTop(true);
    }

    public void setImage(ImageIcon image) {
        background.setIcon(image);
        background.setBounds(0, 0, image.getIconWidth(), image.getIconHeight());

        setSize(new Dimension(image.getIconWidth(), image.getIconHeight()));
        setLocationRelativeTo(null);
        this.image = image;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(image == null ? false : visible);
    }

    public void enableProgress(boolean enable) {
        progress.setBounds(3, image.getIconHeight() - 7, image.getIconWidth() - 6, 4 );

        if (enable) {
            status.setBounds(3, -3, image.getIconWidth(), image.getIconHeight() - progress.getHeight());
            statusShadow.setBounds(4, -2, image.getIconWidth(), image.getIconHeight() - progress.getHeight());
        } else {
            status.setBounds(3, -3, image.getIconWidth(), image.getIconHeight());
            statusShadow.setBounds(4, -2, image.getIconWidth(), image.getIconHeight());
        }

        progress.setVisible(enable);
    }

    public void setStatus(String text) {
        final String finalText = text;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                status.setText(finalText);
                statusShadow.setText(finalText);
            }
        });
    }

    public void setProgress(int value) {
        final int finalValue = value;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progress.setValue(finalValue);
            }
        });
    }
}
