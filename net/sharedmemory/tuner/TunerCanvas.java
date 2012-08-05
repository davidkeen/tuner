/*
 * TunerCanvas.java
 *
 */

package net.sharedmemory.tuner;

import javax.microedition.lcdui.*;

/**
 * GUI class for TunerMIDlet.
 *
 * @author David Keen
 */
public class TunerCanvas extends Canvas {

    private double[] spectrum;             // Array of frequencies.

    private String noteName;
    private Font bigFont;
    private double frequency;
    private int accuracy;

    private final int CANVAS_WIDTH = getWidth();
    private final int CANVAS_HEIGHT = getHeight();
    private final int SPECT_HEIGHT = 100;  // Height of the spectrograph display.


    /** Creates a new instance of TunerCanvas */
    public TunerCanvas() {
        bigFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);
        noteName = "-";
        frequency = 0.0;
        accuracy = 0;
        spectrum = new double[4096];  // Default spectrum length.
    }

    protected void paint(Graphics g) {
        // Paint the background white.
        g.setColor(0xffffff);
        g.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // Draw the note name.
        g.setColor(0x000000);
        g.setFont(bigFont);
        g.drawString(noteName, 0, 0, Graphics.TOP | Graphics.LEFT);

        // Draw the tuning direction.
        String s;
        if (accuracy < 0) {
            s = "Flat";
        } else if (accuracy > 0) {
            s = "Sharp";
        } else {
            s = "In tune";
        }
        g.drawString(s, CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2, Graphics.BASELINE | Graphics.HCENTER);

        // Draw the frequency detected.
        g.drawString(frequency + "", CANVAS_WIDTH, 0, Graphics.TOP | Graphics.RIGHT);

        // Draw the spectrogram.
        drawSpectrum(g);
    }

    public void setNoteName(String noteName) {
        this.noteName = noteName;
    }

    public void setSpectrum(double[] spectrum) {
        this.spectrum = spectrum;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    /**
     * Paints the frequency spectrum on the screen.
     *
     * @param g the Graphics object used for painting on the Canvas.
     */
    private void drawSpectrum(Graphics g) {
        // Draw a black rectangle for spectrogram background.
        // Leave a 1 pixel gap on each side.
        g.setColor(0x000000);
        g.fillRect(1, CANVAS_HEIGHT - SPECT_HEIGHT, CANVAS_WIDTH - 2, SPECT_HEIGHT);

        // We need to scale the spectrum to the displayable screen width but
        // we only need to graph from min - max frequencies.
        // Get the xFactor for averaging the values to scale the spectrogram.
        int xFactor = (int)(Processor.MAX_HZ - Processor.MIN_HZ) / (CANVAS_WIDTH - 2);

        // For each value of the spectrum draw a vertical line.
        // Make the colour red.
        g.setColor(255, 0, 0);
        for (int i = 1, j = (int)Processor.MIN_HZ; i < (CANVAS_WIDTH - 1); i++, j += xFactor) {

            // We scale the magnitude of the spectrum array by 40 to give
            // lines a decent height.
            int lineLength = CANVAS_HEIGHT - ((int)(average(spectrum, j, xFactor) * 40));

            // If the magnitude is off the scale, make it the max height.
            if (lineLength < CANVAS_HEIGHT - SPECT_HEIGHT) {
                lineLength = CANVAS_HEIGHT - SPECT_HEIGHT;
            }
            g.drawLine(i, CANVAS_HEIGHT, i, lineLength);
        }
    }

    /**
     * Returns the average of a number of consecutive array indices.
     *
     * @param array the array to use.
     * @param startIndex the array index to start averaging from.
     * @param factor the number of consectutive indices to average.
     * @return the average.
     */
    private double average(double[] array, int startIndex, int factor) {
        // Prevent divide by zero.
        if (factor <= 0) {
            throw new IllegalArgumentException("Factor must be > 0");
        }

        double d = 0.0;
        for (int i = 0; i < factor; i++) {
            d += array[i + startIndex];
        }
        return d / factor;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }
}
