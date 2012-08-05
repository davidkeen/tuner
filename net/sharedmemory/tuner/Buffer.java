/*
 * Buffer.java
 *
 */

package net.sharedmemory.tuner;

/**
 * A thread-safe shared buffer class for holding raw sample data.
 *
 * @author David Keen
 */
public class Buffer {
    private boolean full;
    private byte[] sample;

    /**
     * Creates a new instance of Buffer
     *
     * @param size the initial size of the buffer.
     */
    public Buffer(int size) {
        full = false;
        sample = new byte[size];
    }

    /**
     * A thread-safe method to insert data into the buffer.
     *
     * @param data an array of raw sample data.
     */
    public synchronized void insert(byte[] data) {
        while (full) {
            try {
                wait();
            } catch (InterruptedException e) {
                // If Thread was interrupted, we just want to terminate.
            }
        }

        // Copy the data into the shared buffer.
        System.arraycopy(data, 0, sample, 0, sample.length);
        full = true;
        notifyAll();
    }

    /**
     * A thread-safe method to remove the data from the buffer.
     *
     * @return an array containing the raw data.
     */
    public synchronized byte[] remove() {
        while (!full) {
            try {
                wait();
            } catch (InterruptedException e) {
                // If Thread was interrupted, we just want to terminate.
            }
        }
        full = false;
        notifyAll();
        return sample;
    }
}
