package roboterroridentifier;

/**
 * A client that can be run at the end of matches to parse through .dsevents
 * files and output a ".txt" file that only contains important information about
 * robot malfunctions. It can also parse further through the use of commands to
 * find specific errors. Also included are graphical representations of robot
 * errors, and much more. Helpful for post-match diagnostics. <Also, if you can
 * see this, thanks for checking out our code! :) >
 * 
 * @version 7.1.3
 * @author Team 2976!
 * @see Github: https://github.com/SkylineSpartabots/RobotErrorIdentifier
 */
public class RobotErrorIdentifier {

    /**
     * Main class to run: Needs to be decomposed this way so the .jar and .exe can
     * run properly.
     * 
     * @param args -> The command line arguments (unused).
     */
    public static void main(final String[] args) {
        LoggerGUI.executeGUI();
    }
}