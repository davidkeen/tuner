/*
 * Recorder.java
 *
 */

package net.sharedmemory.tuner;

import java.io.*;
import javax.microedition.media.*;
import javax.microedition.media.control.RecordControl;

/**
 * Records raw PCM samples into a shared buffer.

 * @author David Keen
 */
public class Recorder implements Runnable {
    private int recordingTime;

    private Buffer buffer;
    private TunerMIDlet controller;

    private Player capturePlayer;
    private RecordControl recordControl;
    private ByteArrayOutputStream bos;

    /**
     * Creates a new instance of Recorder
     *
     * @param buffer the shared Buffer.
     * @param controller the controlling TunerMIDlet instance.
     */
    public Recorder(Buffer buffer, TunerMIDlet controller) {
        this.buffer = buffer;
        this.controller = controller;

        // Buffer filling time (s) is FFT length / sample rate (* 1000ms).
        recordingTime = 1000 * controller.getSampleLength() / TunerMIDlet.RATE;

        if (capturePlayer == null) {
            try {
                capturePlayer = Manager.createPlayer(
                        "capture://audio?encoding=pcm&rate=" + TunerMIDlet.RATE);

                capturePlayer.realize();
                recordControl = (RecordControl)capturePlayer.getControl("RecordControl");

                // Create the internal buffer for the recording
                bos = new ByteArrayOutputStream(controller.getSampleLength());
            } catch (Exception e) {
                // No point continuing without a capturePlayer or recordControl so show fatal error.
                controller.showError(e.getMessage(), new FatalForm(controller));
            }
        }
    }

    public void run() {
        while (controller.okToRun) {
            try {
                recordControl.setRecordStream(bos);
                capturePlayer.start();
                recordControl.startRecord();
                Thread.sleep(recordingTime);
                recordControl.stopRecord();
                recordControl.commit();
                bos.flush();

                // Insert the recorded data into the shared buffer.
                buffer.insert(bos.toByteArray());

                // Reset the ByteArrayOutputStream for reuse.
                bos.reset();
            } catch (InterruptedException e) {

                // If Thread was interrupted, we just want to terminate.
                // Close any open Players
                // Do we need to do this if we set the threads to null
                // from TunerMIDlet?
                if (capturePlayer != null) {
                    capturePlayer.close();
                }
            } catch (Exception e) {
                controller.showError(e.getMessage(), new FatalForm(controller));
            }
        }
    }

}
