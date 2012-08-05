/*
 * Processor.java
 *
 */

package net.sharedmemory.tuner;

/**
 * Processes raw PCM samples.
 *
 * @author David Keen
 */
public class Processor implements Runnable {

    // The frequency range we want to scan.
    public static final double MIN_HZ = 50.0;
    public static final double MAX_HZ = 990.0;

    private double resolution;  // The resolution of the FFT "bins".
    private int minIdx;
    private int maxIdx;

    private Buffer buffer;      // The shared input buffer.
    private TunerMIDlet controller;
    private TunerCanvas tunerCanvas;

    private FFT fft;
    private double[] xr;        // Array of real parts.
    private double[] xi;        // Array of complex parts.

    private double[] freq;      // The output buffer for steady-state filter.

    /**
     * Creates an instance of the Procesor class.
     *
     * @param buffer the shared sample buffer.
     * @param controller the controlling MIDlet.
     * @param tunerCanvas the tuner GUI canvas.
     */
    public Processor(Buffer buffer, TunerMIDlet controller, TunerCanvas tunerCanvas) {
        this.buffer = buffer;
        this.controller = controller;
        this.tunerCanvas = tunerCanvas;

        // Convert the frequency range into FFT array indices to scan.
        resolution = (double)TunerMIDlet.RATE / (double)controller.getSampleLength();
        minIdx = (int)(MIN_HZ / resolution);
        maxIdx = (int)(MAX_HZ / resolution);

        fft = new FFT(controller.getPower());
        xr = new double[controller.getSampleLength()];
        xi = new double[controller.getSampleLength()];

        freq = new double[2];
    }

    public void run() {
        while (controller.okToRun) {

            // Process the first sample
            byte[] samples = buffer.remove();
            fft.populateArrays(xr, xi, samples);
            fft.doFFT(xr, xi, false);

            // Create the original frequency spectrum
            double[] spectrum = fft.createSpectrum(xr, xi);

            // Downsample x2
            double[] times2 = fft.downSample(spectrum, 2);

            // Downsample x3
            double[] times3 = fft.downSample(spectrum, 3);

            // Calculate the Harmonic Product Spectrum
            int max = fft.HPSMax(spectrum, times2, times3, minIdx, maxIdx);

            // Convert the index to frequency.
            freq[0] = fft.indexToFrequency(TunerMIDlet.RATE, spectrum.length, max);

            // Process the second sample
            samples = buffer.remove();
            fft.populateArrays(xr, xi, samples);
            fft.doFFT(xr, xi, false);

            // Create the original frequency spectrum
            spectrum = fft.createSpectrum(xr, xi);

            // Downsample x2
            times2 = fft.downSample(spectrum, 2);

            // Downsample x3
            times3 = fft.downSample(spectrum, 3);

            // Calculate the Harmonic Product Spectrum
            max = fft.HPSMax(spectrum, times2, times3, minIdx, maxIdx);

            // Convert the index to frequency.
            freq[1] = fft.indexToFrequency(TunerMIDlet.RATE, spectrum.length, max);

            // Check if the two samples are close enough (steady-state filter).
            if (Math.abs(freq[1] - freq[0]) < 10.0) {
                double avg = (freq[0] + freq[1]) / 2;

                // Determine the note name.
                String noteName = Note.findNote(avg);
                int tuningDirection = Note.tuningDirection(noteName, avg);

                // Display the results.
                tunerCanvas.setNoteName(noteName);
                tunerCanvas.setFrequency(avg);
                tunerCanvas.setSpectrum(spectrum);
                tunerCanvas.setAccuracy(tuningDirection);
                tunerCanvas.repaint();
            }
        }
    }
}
