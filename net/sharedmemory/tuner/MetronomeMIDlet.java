/*
 * MetronomeMIDlet.java
 *
 */

package net.sharedmemory.tuner;

import javax.microedition.media.*;
import javax.microedition.media.control.ToneControl;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 * A MIDlet that implements a metronome funtion.
 *
 * @author  David Keen
 */
public class MetronomeMIDlet extends MIDlet
        implements CommandListener, ItemStateListener {

    // Tone pitch constants.
    private static final byte HIGH_NOTE = 100;
    private static final byte LOW_NOTE = 90;

    // Min and max beats per minute (BPM).
    private static final int MIN_TEMPO = 40;
    private static final int MAX_TEMPO = 300;

    // These are the time signatures we will support.
    private static final String[] timeSignatures = {
        "1", "2", "3", "4", "5", "6", "7"
    };

    // Tone sequence variables.
    private byte timeSignature;
    private byte tempo;
    private byte duration;

    private boolean isPlaying;

    // GUI
    private Display display;
    private Form metroForm;
    private Gauge tempoGauge;

    // We need to store the current value of the tempo gauge so
    // we can change it when the user adjusts the gauge.
    private int tempoGaugeVal;
    private ChoiceGroup timeSigSelect;

    private StringItem tempoName;

    // Commands
    private Command exitCommand;
    private Command startStopCommand;
    private Command stopCommand;

    // Player and controls.
    private Player tonePlayer;
    private ToneControl toneControl;

    public MetronomeMIDlet() {
        isPlaying = false;

        // BPM = tempo * 4, so set default tempo to 120 BPM (30).
        tempo = (byte)30;
        setDuration(tempo);

        // Set the default number of beats to 4.
        timeSignature = (byte)4;

        // Build GUI
        metroForm = new Form("Metronome");

        // The tempo is adjustable from 40-300 BPM. (Default to 120).
//#if S60Emulator
//#         tempoGauge = new Gauge("Tempo", true, MAX_TEMPO, 120);
//#else
        tempoGauge = new Gauge("Tempo: 120", true, MAX_TEMPO, 120);
//#endif

        // Record the current value of the gauge.
        tempoGaugeVal = 120;
        metroForm.append(tempoGauge);

        timeSigSelect = new ChoiceGroup(
                "Time Signature", ChoiceGroup.POPUP, timeSignatures, null);

        // Default to 4 beats.
        timeSigSelect.setSelectedIndex(3, true);
        metroForm.append(timeSigSelect);

        tempoName = new StringItem(null, getTempoName(tempo));
        metroForm.append(tempoName);

        exitCommand = new Command("Exit", Command.EXIT, 0);
        metroForm.addCommand(exitCommand);

        startStopCommand = new Command("Start/Stop", Command.SCREEN, 0);
        metroForm.addCommand(startStopCommand);

        metroForm.setCommandListener(this);
        metroForm.setItemStateListener(this);
    }

    public void startApp() {
        if (tonePlayer == null) {
            try {
                tonePlayer = Manager.createPlayer(Manager.TONE_DEVICE_LOCATOR);
                tonePlayer.realize();

                toneControl = (ToneControl)tonePlayer.getControl("ToneControl");
            } catch (Exception e) {
                // Release any resources.
                if (tonePlayer != null) {
                    tonePlayer.close();
                }
                toneControl = null;

                // Display the error.
                showError(e.getMessage(), metroForm);
            }
        }

        display = Display.getDisplay(this);
        display.setCurrent(metroForm);
    }

    public void pauseApp() {
        if (isPlaying) {
            stopPlayer();
        }
        tonePlayer.deallocate();
    }

    public void destroyApp(boolean unconditional) {
        if (isPlaying) {
            stopPlayer();
        }
        tonePlayer.close();
    }

    /**
     * Starts the metronome playing.
     *
     * @param numBeats the number of beats in a bar.
     * @param tempo the tempo in BPM/4
     */
    private void startPlayer(byte numBeats, byte tempo) {

        // Set the tone sequence depending on the number of beats required.
        byte[] sequence;
        switch (numBeats) {
            case 1:
            default:
                sequence = new byte[] {
                    ToneControl.VERSION, 1,
                    ToneControl.TEMPO, tempo,
                    ToneControl.BLOCK_START, 0,
                    HIGH_NOTE, duration,
                    ToneControl.SILENCE, (byte)(16 - duration),
                    ToneControl.BLOCK_END, 0,
                    ToneControl.PLAY_BLOCK, 0,
                };
                break;
            case 2:
                sequence = new byte[] {
                    ToneControl.VERSION, 1,
                    ToneControl.TEMPO, tempo,
                    ToneControl.BLOCK_START, 0,
                    HIGH_NOTE, duration,
                    ToneControl.SILENCE, (byte)(16 - duration),
                    ToneControl.BLOCK_END, 0,
                    ToneControl.BLOCK_START, 1,
                    LOW_NOTE, duration,
                    ToneControl.SILENCE, (byte)(16 - duration),
                    ToneControl.BLOCK_END, 1,
                    ToneControl.PLAY_BLOCK, 0,
                    ToneControl.PLAY_BLOCK, 1
                };
                break;
            case 3:
                sequence = new byte[] {
                    ToneControl.VERSION, 1,
                    ToneControl.TEMPO, tempo,
                    ToneControl.BLOCK_START, 0,
                    HIGH_NOTE, duration,
                    ToneControl.SILENCE, (byte)(16 - duration),
                    ToneControl.BLOCK_END, 0,
                    ToneControl.BLOCK_START, 1,
                    LOW_NOTE, duration,
                    ToneControl.SILENCE, (byte)(16 - duration),
                    ToneControl.BLOCK_END, 1,
                    ToneControl.PLAY_BLOCK, 0,
                    ToneControl.PLAY_BLOCK, 1,
                    ToneControl.PLAY_BLOCK, 1
                };
                break;
            case 4:
                sequence = new byte[] {
                    ToneControl.VERSION, 1,
                    ToneControl.TEMPO, tempo,
                    ToneControl.BLOCK_START, 0,
                    HIGH_NOTE, duration,
                    ToneControl.SILENCE, (byte)(16 - duration),
                    ToneControl.BLOCK_END, 0,
                    ToneControl.BLOCK_START, 1,
                    LOW_NOTE, duration,
                    ToneControl.SILENCE, (byte)(16 - duration),
                    ToneControl.BLOCK_END, 1,
                    ToneControl.PLAY_BLOCK, 0,
                    ToneControl.PLAY_BLOCK, 1,
                    ToneControl.PLAY_BLOCK, 1,
                    ToneControl.PLAY_BLOCK, 1
                };
                break;
            case 5:
                sequence = new byte[] {
                    ToneControl.VERSION, 1,
                    ToneControl.TEMPO, tempo,
                    ToneControl.BLOCK_START, 0,
                    HIGH_NOTE, duration,
                    ToneControl.SILENCE, (byte)(16 - duration),
                    ToneControl.BLOCK_END, 0,
                    ToneControl.BLOCK_START, 1,
                    LOW_NOTE, duration,
                    ToneControl.SILENCE, (byte)(16 - duration),
                    ToneControl.BLOCK_END, 1,
                    ToneControl.PLAY_BLOCK, 0,
                    ToneControl.PLAY_BLOCK, 1,
                    ToneControl.PLAY_BLOCK, 1,
                    ToneControl.PLAY_BLOCK, 1,
                    ToneControl.PLAY_BLOCK, 1
                };
                break;
            case 6:
                sequence = new byte[] {
                    ToneControl.VERSION, 1,
                    ToneControl.TEMPO, tempo,
                    ToneControl.BLOCK_START, 0,
                    HIGH_NOTE, duration,
                    ToneControl.SILENCE, (byte)(16 - duration),
                    ToneControl.BLOCK_END, 0,
                    ToneControl.BLOCK_START, 1,
                    LOW_NOTE, duration,
                    ToneControl.SILENCE, (byte)(16 - duration),
                    ToneControl.BLOCK_END, 1,
                    ToneControl.PLAY_BLOCK, 0,
                    ToneControl.PLAY_BLOCK, 1,
                    ToneControl.PLAY_BLOCK, 1,
                    ToneControl.PLAY_BLOCK, 1,
                    ToneControl.PLAY_BLOCK, 1,
                    ToneControl.PLAY_BLOCK, 1
                };
                break;
            case 7:
                sequence = new byte[] {
                    ToneControl.VERSION, 1,
                    ToneControl.TEMPO, tempo,
                    ToneControl.BLOCK_START, 0,
                    HIGH_NOTE, duration,
                    ToneControl.SILENCE, (byte)(16 - duration),
                    ToneControl.BLOCK_END, 0,
                    ToneControl.BLOCK_START, 1,
                    LOW_NOTE, duration,
                    ToneControl.SILENCE, (byte)(16 - duration),
                    ToneControl.BLOCK_END, 1,
                    ToneControl.PLAY_BLOCK, 0,
                    ToneControl.PLAY_BLOCK, 1,
                    ToneControl.PLAY_BLOCK, 1,
                    ToneControl.PLAY_BLOCK, 1,
                    ToneControl.PLAY_BLOCK, 1,
                    ToneControl.PLAY_BLOCK, 1,
                    ToneControl.PLAY_BLOCK, 1
                };
                break;
        }

        toneControl.setSequence(sequence);
        tonePlayer.setLoopCount(-1); // Indefinite.
        try {
            tonePlayer.start();
        } catch (MediaException e) {
            showError(e.getMessage(), metroForm);
        }
    }

    /**
     * Stops the metronome playing.
     */
    private void stopPlayer() {
        try {
            tonePlayer.stop();
            tonePlayer.deallocate();
        } catch (MediaException e) {
            showError(e.getMessage(), metroForm);
        }
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == exitCommand) {
            destroyApp(true);
            notifyDestroyed();
        } else if (command == startStopCommand) {
            if (isPlaying) {
                stopPlayer();
                isPlaying = false;
            } else {
                startPlayer(timeSignature, tempo);
                isPlaying = true;
            }
        }
    }

    public void itemStateChanged(Item item) {
        if (item == timeSigSelect) {
            // Change the time signature.
            timeSignature = (byte)(timeSigSelect.getSelectedIndex() + 1);

            // Restart the player if it was playing.
            if (isPlaying) {
                stopPlayer();
                startPlayer(timeSignature, tempo);
            }
        } else if (item == tempoGauge) {
            // We always need to increment or decrement the value of the gauge
            // by 4 because actual tempo = gauge value / 4. The minimum value should be 40.
            int val = tempoGauge.getValue();
            if (val > tempoGaugeVal) {        // User increased gauge.
                tempoGauge.setValue(tempoGaugeVal + 4);
                tempoGaugeVal += 4;
            } else if (val < tempoGaugeVal) { // User decreased gauge.
                if (val < MIN_TEMPO) {
                    tempoGauge.setValue(MIN_TEMPO);
                } else {
                    tempoGauge.setValue(tempoGaugeVal - 4);
                    tempoGaugeVal -= 4;
                }
            }
            tempo = (byte)(tempoGaugeVal / 4);
            setDuration(tempo);
//#if !S60Emulator
            tempoGauge.setLabel("Tempo: " + tempoGaugeVal);
//#endif
            tempoName.setText(getTempoName(tempo));
        }
    }

    /**
     * Sets the duration of the tone.  This changes because as we increase or
     * decrease the tempo value, the duration also needs to increase or decrease
     * so the absolute length of note remains the same.
     *
     * @param tempo the tempo to set. This is the BPM / 4.  Must be >= 8.
     * @exception IllegalArgumentException
     */
    private void setDuration(byte tempo) throws IllegalArgumentException {

        // A note duration of 500ms is 500 * resolution * tempo / 240000.
        // Default resolution is 64.  Our tempo is never below 40 so duration is
        // never < 1.
        if (tempo < 8) {
            throw new IllegalArgumentException("Tempo must be >= 8");
        }
        duration = (byte)(32000 * tempo / 240000);
    }

    /**
     * Converts a tempo value into a tempo name.
     *
     * @param tempo the ToneSequence tempo. This is actual tempo / 4.
     * @return String representing the tempo in words
     */
    private String getTempoName(byte tempo) {
        if (tempo < 15) {
            return "Largo";
        } else if (tempo < 17) {
            return "Larghetto";
        } else if (tempo < 19) {
            return "Adagio";
        } else if (tempo < 27) {
            return "Andante";
        } else if (tempo < 30) {
            return "Moderato";
        } else if (tempo < 42) {
            return "Allegro";
        } else if (tempo < 50) {
            return "Presto";
        } else return "Prestissimo";
    }

    /**
     * Shows any error alerts.
     * @param message the String to display.
     * @param next the next Displayable to be shown after the alert.
     */
    public void showError(String message, Displayable next) {
        Alert alert = new Alert("Message", message, null, AlertType.ERROR);
        alert.setTimeout(Alert.FOREVER);
        display.setCurrent(alert, next);
    }
}
