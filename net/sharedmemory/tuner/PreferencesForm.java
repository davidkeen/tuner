/*
 * PreferencesForm.java
 *
 */

package net.sharedmemory.tuner;

import javax.microedition.lcdui.*;

/**
 * A class to handle user preferences for TunerMIDlet.
 *
 * @author David Keen
 */
public class PreferencesForm extends Form {
    private TunerMIDlet controller;

    // Items
    private static ChoiceGroup resolution;
    private static final String[] resolutionList = {
        "Low (Faster)", "High (Slower)"};

    // Commands
    private Command cancelCommand;
    private Command okCommand;

    /**
     * Creates a new instance of PreferencesForm
     *
     * @param controller the TunerMIDlet controlling instance.
     */
    public PreferencesForm(TunerMIDlet controller) {
        super("Preferences");

        this.controller = controller;

        // Set up items
        resolution = new ChoiceGroup("Resolution", ChoiceGroup.EXCLUSIVE, resolutionList, null);
        append(resolution);

        // Set up commands
        cancelCommand = new Command("Cancel", Command.CANCEL, 0);
        addCommand(cancelCommand);
        okCommand = new Command("OK", Command.OK, 0);
        addCommand(okCommand);

        initForm();

        setCommandListener(controller);
    }

    /**
     * Saves the user preferences for this session.
     */
    public void savePreferences() {
        if (resolution.getSelectedIndex() == 0) {
            controller.setPower(12);
        } else {
            controller.setPower(13);
        }
    }

    /**
     * Sets the form to reflect the current user preference values.
     */
    private void initForm() {
       if (controller.getPower() == 12) {
           resolution.setSelectedIndex(0, true);
       } else {
           resolution.setSelectedIndex(1, true);
       }
    }
}
