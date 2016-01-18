package sypan.utility;

import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import javax.imageio.ImageIO;

import sypan.draughts.game.Game;
import sypan.draughts.game.piece.Piece;
import sypan.draughts.game.piece.Tile;

/**
 * {@code Utility} is an abstract class containing numerous static convenience methods used in the project.
 * 
 * @author Carl Linley
 **/
public abstract class Utility {

    /**
     * If the specified piece <b>toCheck</b> can jump over an enemy piece from
     * tile <b>checkFrom</b>, the landing tile is returned.<br>
     * If not, {@code null} is returned.
     *
     * @param game - the current game.
     * @param toCheck - the piece to check.
     * @param checkFrom - the tile to check from. Due to the way this method is
     * used, toCheck.getTile() would not suffice.
     *
     * @return the tile <b>toCheck</b> lands on if it can jump from
     * <b>checkFrom</b>, or {@code null} if not.
     **/
    public static Tile getJumpDestination(Game game, Piece toCheck, Tile checkFrom) {
        Tile newDestination;

        if (game.canJump(toCheck.getType(), checkFrom, (newDestination = toCheck.getTile().add(2, 2)))
         || game.canJump(toCheck.getType(), checkFrom, (newDestination = toCheck.getTile().add(-2, 2)))
         || game.canJump(toCheck.getType(), checkFrom, (newDestination = toCheck.getTile().add(2, -2)))
         || game.canJump(toCheck.getType(), checkFrom, (newDestination = toCheck.getTile().add(-2, -2)))) {
            return newDestination;
        }
        return null;
    }

    /**
     * A method used to return the current date as a string.
     *
     * @param fileSafe - <i>true</i> if the string must be applicable to file
     * names. This replaces the default splitter ('/') with '-'.
     * @return the current date as a string.
     **/
    public static String getDate(boolean fileSafe) {
        Calendar c = Calendar.getInstance();
        char s = (fileSafe ? '-' : '/');

        return doubleDigit(c.get(Calendar.DATE)) + s + Utility.doubleDigit(c.get(Calendar.MONTH) + 1) + s + c.get(Calendar.YEAR);
    }

    /**
     * A method used to return the current time as a string.
     *
     * @param fileSafe - <i>true</i> if the string must be applicable to file
     * names. This replaces the default splitter (':') with '.'.
     * @return the current date as a string.
     **/
    public static String getTime(boolean fileSafe) {
        Calendar c = Calendar.getInstance();
        char s = (fileSafe ? '.' : ':');

        return doubleDigit(c.get(Calendar.HOUR_OF_DAY)) + s + doubleDigit(c.get(Calendar.MINUTE)) + s + doubleDigit(c.get(Calendar.SECOND));
    }

    /**
     * Converts a single-digit number to a double-digit number, if
     * applicable.<p>
     *
     * For example:<br>
     * - Passing 1 returns "01"<br>
     * - Passing 4 returns "04"<br>
     * - Passing 42 returns "42"<br>
     *
     * @param toConvert - the integer to convert to a double digit format.
     * @return a string containing the specified integer converted to a double
     * digit format if applicable.
     **/
    public static String doubleDigit(int toConvert) {
        if (toConvert < 10) {
            return "0" + toConvert;
        }
        return toConvert + "";
    }

    /**
     * Based on example code found on StackOverflow, posted by a user named <i>Marcel</i>.
     * I had absolutely no idea how to go about doing this, so thanks Marcel!
     * <p>
     *
     * Link:
     * <a href="http://stackoverflow.com/questions/38955/is-it-possible-to-get-the-maximum-supported-resolution-of-a-connected-display-in">
     * http://stackoverflow.com/questions/38955/is-it-possible-to-get-the-maximum-supported-resolution-of-a-connected-display-in
     * </a>
     *
     * @return an {@code ArrayList} of type String containing every resolution
     * supported by the main display in the format "WIDTH x HEIGHT".
     * @author Carl Linley
     * @author Marcel
     **/
    public static ArrayList<String> getSupportedResolutions() {
        DisplayMode[] supportedModes = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayModes();
        ArrayList<String> resolutionList = new ArrayList<>(supportedModes.length);
        String resolutionType;

        for (int i = 0; i < supportedModes.length; i++) {
            resolutionType = supportedModes[i].getWidth() + " x " + supportedModes[i].getHeight();

            if (!resolutionList.contains(resolutionType)) {
                resolutionList.add(resolutionType);
            }
        }
        return resolutionList;
    }

    public static Dimension parseDimension(String asString) {
        String[] values = asString.split(" x ");

        return new Dimension(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
    }

    /**
     * Formats the specified string to a reasonable standard for names. Spaces
     * and dashes constitute a following upper-case letter.
     *
     * @param toFormat - the string to format.
     * @return the formatted string.
     **/
    public static String formatName(String toFormat) {
        toFormat = toFormat.toLowerCase().replaceAll("_", " ");

        char buf[] = toFormat.toCharArray();

        boolean endMarker = true;

        for (int i = 0; i < buf.length; i++) {
            char c = buf[i];

            if (endMarker && c >= 'a' && c <= 'z') {
                buf[i] -= 0x20;
            }
            endMarker = (c == ' ' || c == '-');
        }
        return new String(buf, 0, buf.length);
    }

    /**
     * @param toCheck - the resolution to check, in the format "123x123" (e.g "800x600", "1024x768", "1920x1080").
     * @return true if the specified resolution is supported.
     **/
    public static boolean validResolution(String toCheck) {
        return getSupportedResolutions().contains(toCheck);
    }

    /**
     * @param filePath - the relative path to the file.
     * @return a BufferedReader to the specified file. Works both in the file
     * system and packed in a JAR.
     **/
    public static BufferedReader getReader(String filePath) {
        InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(filePath);

        if (inputStream != null) {
            return new BufferedReader(new InputStreamReader(inputStream)); // Load from inside the JAR
        }
        else {
            try {
                return new BufferedReader(new FileReader(new File(filePath))); // Load from outside the JAR
            }
            catch (FileNotFoundException e) {
                return null;
            }
        }
    }

    /**
     * @param filePath - the relative path to the file.
     * 
     * @return a BufferedImage of the specified file. Works both in the file
     * system and packed in a JAR.
     * @throws java.io.IOException
     **/
    public static BufferedImage getImage(String filePath) throws IOException {
        InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(filePath);

        if (inputStream == null) {
            throw new IOException("Failed to open input stream.");
        }
        return ImageIO.read(inputStream);
    }
}