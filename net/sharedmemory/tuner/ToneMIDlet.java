/*
 * ToneMidlet.java
 *
 */

package net.sharedmemory.tuner;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.MIDIControl;
import javax.microedition.media.control.ToneControl;

/**
 * A MIDlet to play tones of a specific pitch.
 *
 * @author  David Keen
 */
public class ToneMIDlet extends MIDlet
        implements CommandListener, ItemStateListener {

    // Controls
    private Display display;
    private Form toneForm;
    private Gauge volGauge;
    private ChoiceGroup noteList;
    private String[] notes = {
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    private ChoiceGroup instrumentList;

    private String[] instruments = {
        "Violin", "Trumpet", "Oboe", "Clarinet", "Whistle"};
    private int[] programs = {41, 57, 69, 72, 79};

    // Commands
    private Command exitCommand;
    private Command startStopCommand;

    // Players and controls
    private Player MIDIPlayer;
    private MIDIControl MIDIControl;

    // Note information
    private int currentNote;
    private static final int MIDI_CHANNEL = 10;
    private static final int A = 69;  // A 440Hz
    private boolean isPlaying;

    /**
     * Creates an instance of ToneMIDlet.
     */
    public ToneMIDlet() {
        toneForm = new Form("Tone Generator");

        // Set up Form items
        noteList = new ChoiceGroup("Note", ChoiceGroup.POPUP, notes, null);
        toneForm.append(noteList);

        // Set the default note to A4
        currentNote = A;
        noteList.setSelectedIndex(9, true);

        instrumentList = new ChoiceGroup("Instrument", ChoiceGroup.POPUP, instruments, null);
        toneForm.append(instrumentList);

        // Set the default instrument to whistle.
        instrumentList.setSelectedIndex(4, true);

        // The volume control has 10 levels.
        volGauge = new Gauge("Volume", true, 10, 10);
        toneForm.append(volGauge);

        // Set up Form commands
        exitCommand = new Command("Exit", Command.EXIT, 0);
        toneForm.addCommand(exitCommand);
        startStopCommand = new Command("Start/Stop", Command.SCREEN, 0);
        toneForm.addCommand(startStopCommand);

        toneForm.setItemStateListener(this);
        toneForm.setCommandListener(this);


        isPlaying = false;
    }

    public void startApp() {
        if (MIDIPlayer == null) {
            try {
                MIDIPlayer = Manager.createPlayer(Manager.MIDI_DEVICE_LOCATOR);
                MIDIPlayer.prefetch();
                MIDIControl = (MIDIControl)MIDIPlayer.getControl(
                        "javax.microedition.media.control.MIDIControl");

                // Set the instrument to whistle as it seems to sound the best.
                MIDIControl.setProgram(MIDI_CHANNEL, -1, programs[4]);

                // Set the MIDI volume to 120.
                MIDIControl.setChannelVolume(MIDI_CHANNEL, 120);

                // Give the event time to process.
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // Ignore
            } catch (Exception e) {
                // Cleanup any resources.
                if (MIDIPlayer != null) {
                    MIDIPlayer.close();
                    MIDIControl = null;
                }
                showError(e.getMessage(), toneForm);
            }
        }
        display = Display.getDisplay(this);
        display.setCurrent(toneForm);
    }

    public void pauseApp() {
        // Stop all sound.
        MIDIControl.shortMidiEvent(
                MIDIControl.CONTROL_CHANGE | MIDI_CHANNEL, 0x78, 0);
        isPlaying = false;
    }

    public void destroyApp(boolean unconditional) {
        // Stop all sound.
        MIDIControl.shortMidiEvent(
                MIDIControl.CONTROL_CHANGE | MIDI_CHANNEL, 0x78, 0);
        MIDIPlayer.close();
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == exitCommand) {
            destroyApp(true);
            notifyDestroyed();
        } else if (command == startStopCommand) {
            if (isPlaying) {
                stopPlaying();
                isPlaying = false;
            } else {
                startPlaying(currentNote);
                isPlaying = true;
            }
        }
    }

    public void itemStateChanged(Item item) {
        if (item == noteList) {
            if (isPlaying) {
                stopPlaying();
                currentNote = ToneControl.C4 + noteList.getSelectedIndex();
                startPlaying(currentNote);
            } else {
                currentNote = ToneControl.C4 + noteList.getSelectedIndex();
            }
        } else if (item == instrumentList) {
            MIDIControl.setProgram(
                    MIDI_CHANNEL, -1, programs[instrumentList.getSelectedIndex()]);
            if (isPlaying) {
                stopPlaying();
                currentNote = ToneControl.C4 + noteList.getSelectedIndex();
                startPlaying(currentNote);
            }
        } else if (item == volGauge) {

            // Actual channel volume may be 0 - 127
            // so we multiply value of volGauge by 12 to get a range of 0 - 120
            MIDIControl.setChannelVolume(MIDI_CHANNEL, volGauge.getValue() * 12);
        }
    }

    /**
     * Starts playing the specified note.
     *
     * @param note the note to be played
     */
    public void startPlaying(int note) {
        MIDIControl.shortMidiEvent(MIDIControl.NOTE_ON | MIDI_CHANNEL, note, 127);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}  // Ignore
    }

    /**
     * Stops playing the current note.
     *
     */
    public void stopPlaying() {
         MIDIControl.shortMidiEvent(MIDIControl.NOTE_ON | MIDI_CHANNEL, currentNote, 0);
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {}  // Ignore
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
