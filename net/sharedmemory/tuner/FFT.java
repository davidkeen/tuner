/*
 * FFT.java
 *
 * Adapted by David Keen from the Java implementation by Craig A. Lindley
 */

package net.sharedmemory.tuner;

/* libfft.c - fast Fourier transform library
 **
 ** Copyright (C) 1989 by Jef Poskanzer.
 **
 ** Permission to use, copy, modify, and distribute this software and its
 ** documentation for any purpose and without fee is hereby granted, provided
 ** that the above copyright notice appear in all copies and that both that
 ** copyright notice and this permission notice appear in supporting
 ** documentation.  This software is provided "as is" without express or
 ** implied warranty.
 */

public class FFT {
    private int bits;
    private int [] bitreverse = new int[MAXFFTSIZE];
    private static final double TWOPI = 2.0 * Math.PI;

    // Limits on the number of bits this algorithm can utilize
    private static final int LOG2_MAXFFTSIZE = 15;
    private static final int MAXFFTSIZE = 1 << LOG2_MAXFFTSIZE;

    /**
     * FFT class constructor
     * Initializes code for doing a fast Fourier transform
     *
     * @param int bits is a power of two such that 2^b is the number
     * of samples.
     */
    public FFT(int bits) {

        this.bits = bits;

        if (bits > LOG2_MAXFFTSIZE) {
            throw new IllegalArgumentException("" + bits + " is too big");
        }
        for (int i = (1 << bits) - 1; i >= 0; --i) {
            int k = 0;
            for (int j = 0; j < bits; ++j) {
                k *= 2;
                if ((i & (1 << j)) != 0)
                    k++;
            }
            bitreverse[i] = k;
        }
    }

    /**
     * A fast Fourier transform routine
     *
     * @param double [] xr is the real part of the data to be transformed
     * @param double [] xi is the imaginary part of the data to be transformed
     * (normally zero unless inverse transoform is effect).
     * @param boolean invFlag which is true if inverse transform is being
     * applied. false for a forward transform.
     */
    public void doFFT(double [] xr, double [] xi, boolean invFlag) {
        int n, n2, i, k, kn2, l, p;
        double ang, s, c, tr, ti;

        n2 = (n = (1 << bits)) / 2;

        for (l = 0; l < bits; ++l) {
            for (k = 0; k < n; k += n2) {
                for (i = 0; i < n2; ++i, ++k) {
                    p = bitreverse[k / n2];
                    ang = TWOPI * p / n;
                    c = Math.cos(ang);
                    s = Math.sin(ang);
                    kn2 = k + n2;

                    if (invFlag)
                        s = -s;

                    tr = xr[kn2] * c + xi[kn2] * s;
                    ti = xi[kn2] * c - xr[kn2] * s;

                    xr[kn2] = xr[k] - tr;
                    xi[kn2] = xi[k] - ti;
                    xr[k] += tr;
                    xi[k] += ti;
                }
            }
            n2 /= 2;
        }

        for (k = 0; k < n; k++) {
            if ((i = bitreverse[k]) <= k)
                continue;

            tr = xr[k];
            ti = xi[k];
            xr[k] = xr[i];
            xi[k] = xi[i];
            xr[i] = tr;
            xi[i] = ti;
        }

        // Finally, multiply each value by 1/n, if this is the forward
        // transform.
        if (!invFlag) {
            double f = 1.0 / n;

            for (i = 0; i < n ; i++) {
                xr[i] *= f;
                xi[i] *= f;
            }
        }
    }

    /**
     * Creates a frequency spectrum from the raw FFT output.
     * The maximum frequency detectable is equal to half the length of
     * the sample array (Nyquist frequency).
     *
     * @param xr array of real parts.
     * @param xi array of imaginary parts.
     * @return array containing magnitudes of each FFT bin.
     *
     *@author David Keen
     */
    public double[] createSpectrum(double [] xr, double [] xi) {
        // Only need to scan from 0 > xr.length / 2 (Nyquist frequency).
        double[] spectrum = new double[xr.length / 2];
        for (int i = 0; i < spectrum.length; i++) {
            spectrum[i] = Math.sqrt((xr[i] * xr[i]) + (xi[i] * xi[i]));
        }
        return spectrum;
    }

    /**
     * Converts the index of the FFT "bin" to the corresponding frequency.
     *
     * @param sampleRate the sample rate in Hz
     * @param numSamples the length of the spectrum (sampleLength/2)
     * @param the index of the array to be converted
     * @return the calculated frequency in Hz
     *
     * @author David Keen
     */
    public double indexToFrequency(int sampleRate, int numSamples, int index) {
        return (double)index * (double)sampleRate / (double)numSamples;
    }

    /**
     * Loads the given arrays with the sample data.  The arrays are a
     * parallel array representation of complex numbers.
     *
     * @param xr the array holding the real parts of the complex numbers.
     * @param xi the array holding the imaginary parts of the complex numbers.
     *
     * @author David Keen
     */
    public void populateArrays(double[] xr, double[] xi, byte[] samples) {
        for (int i = 0; i < samples.length; i++) {
            xr[i] = samples[i];
            xi[i] = 0.0;
        }
    }

    /**
     * Downsamples a spectrum.
     *
     * @param spectrum original spectrum array to downsample.
     * @param factor the downsampling factor.
     * @return the downsampled spectrum.
     *
     * @author David Keen
     */
    public double[] downSample(double[] spectrum, int factor) {
        int downsampleLength = spectrum.length / factor;
        double[] d = new double[spectrum.length];

        // Load the new array with the average of factor consecutive samples
        // of the original.  Remainder should be set to 1.
        for (int i = 0; i < spectrum.length; i ++) {
            if (i < downsampleLength) {
                for (int j = 0; j < factor; j++) {      // Add the consecutive samples
                    d[i] += spectrum[(i * factor) + j];
                }
                d[i] = d[i] / factor;                   // and average them.
            } else {
                d[i] = 1;
            }
        }
        return d;
    }

    /**
     * Finds the index of the array that contains the fundamental.
     * We use the Harmonic Product Spectrum method.
     * Narrowing the scanning range can help accuracy and improve speed.
     *
     * @param original the original frequency spectrum.
     * @param times2 the original downsampled by 2.
     * @param times3 the original downsampled by 3.
     * @param min the minimum index to scan.
     * @param max the maximum index to scan.
     * @return the index containing the max value (the fundamental).
     *
     * @author David Keen
     */
    public int HPSMax(
            double[] original, double[] times2, double[] times3, int min, int max) {
        double[] d = new double[original.length];

        // Calculate the product of the spectrums.
        for (int i = min; i < max; i++) {
            d[i] = original[i] * times2[i] * times3[i];
        }

        // Find the index with the highest value.
        int maxIdx = 0;
        for (int i = 0; i < original.length; i++) {
            if (d[i] > d[maxIdx]) {
                maxIdx = i;
            }
        }
        return maxIdx;
    }
}
