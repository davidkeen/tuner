/*
 * FatalForm.java
 *
 */

package net.sharedmemory.tuner;

import javax.microedition.lcdui.*;

/**
 * A fatal error screen for when the application must close.
 *
 * @author David Keen
 */
public class FatalForm extends Form {
    private TunerMIDlet controller;

    /** Creates a new instance of FatalForm
     *
     * @param controller the controlling TunerMIDlet instance.
     */
    public FatalForm(TunerMIDlet controller) {
        super("Fatal Error");

        this.controller = controller;

        append("A fatal error has occured and the program needs to close.");

        addCommand(new Command("Exit", Command.EXIT, 0));

        setCommandListener(controller);
    }

}
