/*
 * TunerMIDlet.java
 *
 */

package net.sharedmemory.tuner;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 * An instrument tuner MIDlet. It detects the pitch of the note played
 * and displays whether it is sharp, flat or in tune.
 *
 * @author  David Keen
 */
public class TunerMIDlet extends MIDlet implements CommandListener {

    // Constants
    static final int RATE = 8000; // Encapsulate?

    // The length of the FFT is 2 raised to this power.
    private int power = 12;

    // Threads
    private Thread recorder;
    private Thread processor;
    private Buffer buffer;
    boolean okToRun;  // Flag to control threads.

    // UI
    private Display display;
    private TunerCanvas tunerCanvas;
    private PreferencesForm preferencesForm;
    private Command exitCommand;
    private Command preferencesCommand;

    public TunerMIDlet() {
        power = 12;
        display = Display.getDisplay(this);
        tunerCanvas = new TunerCanvas();
        preferencesForm = new PreferencesForm(this);
        exitCommand = new Command("Exit", Command.EXIT, 0);
        tunerCanvas.addCommand(exitCommand);
        preferencesCommand = new Command("Preferences", Command.SCREEN, 1);
        tunerCanvas.addCommand(preferencesCommand);
        tunerCanvas.setCommandListener(this);
    }

    public void startApp() {
        // Allocate all the memory we will need for objects at the start.
        buffer = new Buffer(getSampleLength());
        recorder = new Thread(new Recorder(buffer, this));
        processor = new Thread(new Processor(buffer, this, tunerCanvas));

        display.setCurrent(tunerCanvas);

        okToRun = true;
        recorder.start();
        processor.start();
    }

    public void pauseApp() {
        stopThreads();
        releaseResources();
    }

    public void destroyApp(boolean unconditional) {
        stopThreads();
        releaseResources();
    }

    public void commandAction(Command command, Displayable displayable) {
        int commandType = command.getCommandType();
        if (commandType == Command.CANCEL) {
            // Returning to main screen.
            startApp();
        } else if (commandType == Command.OK) {
            if (displayable == preferencesForm) {
                ((PreferencesForm)displayable).savePreferences();
                startApp();
            }
        } else if (commandType == Command.EXIT) {
            destroyApp(true);
            notifyDestroyed();
        } else if (commandType == Command.SCREEN) {
            if (command == preferencesCommand) {
                pauseApp();
                display.setCurrent(preferencesForm);
            }
        }
    }

    /**
     * Stops the Recorder and Processor threads.
     */
    private void stopThreads() {
        // Signal threads to stop.
        okToRun = false;

        // Wake up any sleeping threads so they can stop.
        recorder.interrupt();
        processor.interrupt();
    }

    /**
     * Releases references to objects we have created
     * so they can be garbage collected.
     */
    private void releaseResources() {
        buffer = null;
        recorder = null;
        processor = null;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getSampleLength() {
        return 1 << power;
    }

    /**
     * Shows any error alerts.
     *
     * @param message the String to display.
     * @param next the next Displayable to be shown after the alert.
     */
    public void showError(String message, Displayable next) {
        Alert alert = new Alert("Message", message, null, AlertType.ERROR);
        alert.setTimeout(Alert.FOREVER);
        display.setCurrent(alert, next);
    }


}
