package edu.lehigh.cse.lol;

/**
 * The SplashConfiguration interface describes the key characteristics of the splash screen. In LOL, we assume there is
 * a single image that comprises the background, and that any text or graphics that are needed to instruct the user
 * (i.e., a "play button") are part of the image.
 * 
 * That being the case, the splash screen consists of an image, a sound file, and descriptions of the regions that
 * should be treated as Play, Help, and Quit buttons
 * 
 */
public interface SplashConfiguration
{
    /**
     * Indicate the X coordinate of the bottom left corner of the "PLAY" button
     */
    public int getPlayX();

    /**
     * Indicate the Y coordinate of the bottom left corner of the "PLAY" button
     */
    public int getPlayY();

    /**
     * Indicate the width of the "PLAY" button
     */
    public int getPlayWidth();

    /**
     * Indicate the height of the "PLAY" button
     */
    public int getPlayHeight();

    /**
     * Indicate the X coordinate of the bottom left corner of the "HELP" button
     */
    public int getHelpX();

    /**
     * Indicate the Y coordinate of the bottom left corner of the "HELP" button
     */
    public int getHelpY();

    /**
     * Indicate the width of the "HELP" button
     */
    public int getHelpWidth();

    /**
     * Indicate the height of the "HELP" button
     */
    public int getHelpHeight();

    /**
     * Indicate the X coordinate of the bottom left corner of the "QUIT" button
     */
    public int getQuitX();

    /**
     * Indicate the Y coordinate of the bottom left corner of the "QUIT" button
     */
    public int getQuitY();

    /**
     * Indicate the width of the "QUIT" button
     */
    public int getQuitWidth();

    /**
     * Indicate the height of the "QUIT" button
     */
    public int getQuitHeight();

    /**
     * Indicate the name of the file that serves as the background image for the opening screen
     */
    public String getBackgroundImage();

    /**
     * Indicate the name of the file that serves as the music for the opening screen
     */
    public String getMusic();
}
