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
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JComponent;

/**
 *
 * @author Toni Spets <toni.spets@iki.fi>
 */
public class SimpleProgress extends JComponent {

    private int min;
    private int max;
    private volatile int value;

    private Color shadowColor;
    private Color borderColor;

    public SimpleProgress() {
        this(0, 100);
    }

    public SimpleProgress(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public void setValue(int value) {
        boolean needRepaint = false;

        if (this.value != value)
            needRepaint = true;

        this.value = value;

        if (needRepaint)
            repaint();
    }

    public void setShadow(Color color) {
        this.shadowColor = color;
    }

    public void setBorder(Color color) {
        this.borderColor = color;
    }

    public double getPercentComplete() {
        return (double)value / max;
    }

    @Override
    public void paint(Graphics g) {
        Rectangle b = g.getClipBounds();

        if (shadowColor != null) {
            g.setColor(shadowColor);
            g.drawLine(1, b.height - 1, b.width - 2, b.height - 1);
            g.drawLine(b.width - 1, 1, b.width - 1, b.height - 1);

            b.width -= 1;
            b.height -= 1;
        }

        if (isOpaque()) {
            g.setColor(getBackground());
            g.fillRect(1, 1, b.width - 2, b.height - 2);
        }

        g.setColor(borderColor != null ? borderColor : getForeground());
        g.drawRect(0, 0, b.width - 1, b.height - 1);
        g.setColor(getForeground());
        g.fillRect(1, 1, (int)(b.width * getPercentComplete()) - 2, b.height - 2);
    }
}
