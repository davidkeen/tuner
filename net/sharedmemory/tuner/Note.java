/*
 * Note.java
 *
 */

package net.sharedmemory.tuner;

/**
 * A utility class for handling note names.
 *
 * @author David Keen
 */
public class Note {

    // I used parallel arrays for speed and memory optimisation rather than
    // creating an array of some kind of note objects.

    // List of note names
    private static final String[] NOTE_NAMES = {
        "C3", "C#3", "D3", "D#3", "E3", "F3", "F#3", "G3", "G#3", "A3", "A#3", "B3",
        "C4", "C#4", "D4", "D#4", "E4", "F4", "F#4", "G4", "G#4", "A4", "A#4", "B4",
        "C5", "C#5", "D5", "D#5", "E5", "F5", "F#5", "G5", "G#5", "A5", "A#5", "B5"
    };

    // Parallel array of note frequencies
    private static final double[] NOTE_FREQS = {
        130.81, 138.59, 146.83, 155.56, 164.81, 174.61, 185.0, 196.0, 207.65, 220.0, 233.08, 246.94,
        261.63, 277.18, 293.66, 311.13, 329.63, 349.23, 369.99, 392.0, 415.3, 440.0, 466.16, 493.88,
        523.25, 554.37, 587.33, 622.25, 659.26, 698.46, 739.99, 783.99, 830.61, 880.0, 932.33, 987.77
    };

    /**
     * Converts note frequency to nearest note name.
     *
     * @param frequency the frequency to be converted.
     * @return the note name and octave.
     */
    public static String findNote(double frequency) {
        if (frequency < 0) {
            throw new IllegalArgumentException("Frequency must be > 0");
        }

        // Perform binary search to find closest note.
        int first = 0;
        int last = NOTE_FREQS.length - 1;
        int mid;

        // If frequency is out of our range then just return the top or bottom note.
        if (frequency > NOTE_FREQS[last]) {
            return NOTE_NAMES[last];
        } else if (frequency < NOTE_FREQS[first]) {
            return NOTE_NAMES[first];
        }

        // Otherwise, find the position of the frequency in the array.
        while (first <= last) {
            mid = (first + last) / 2;
            if (frequency < NOTE_FREQS[mid]) {
                last = mid - 1;
            } else {
                first = mid + 1;
            }
        }

        // Find which note is the closest.
        if (Math.abs(NOTE_FREQS[first] - frequency) < Math.abs(NOTE_FREQS[last] - frequency)) {
            return NOTE_NAMES[first];
        } else {
            return NOTE_NAMES[last];
        }
    }

    /**
     * Determines whether a note is sharp or flat.
     *
     * @param noteName the name of the note to test, including octave (Eg, F#4).
     * This must be in the range of C3 - B5 (case insensitive).
     * @param frequency the frequency of the note to test in Hz.
     * @return -1 if flat, 0 if equal, 1 if sharp
     */
    public static int tuningDirection(String noteName, double frequency) {
        if (frequency < 0) {
            throw new IllegalArgumentException("frequency must be > 0");
        }

        for (int i = 0; i < NOTE_NAMES.length; i++) {
            if (NOTE_NAMES[i].equalsIgnoreCase(noteName)) {
                if (Math.abs(frequency - NOTE_FREQS[i]) < 1.0) {
                    return 0;
                } else if (frequency < NOTE_FREQS[i]) {
                    return -1;
                } else if (frequency > NOTE_FREQS[i]) {
                    return 1;
                }
            }
        }

        // The note name isn't in our list.
        throw new IllegalArgumentException("noteName must be in range C3 - B5");
    }
}
