
package com.me.mylolgame;

import edu.lehigh.cse.lol.SplashConfiguration;

/**
 * The game starts with a splash screen, which has buttons for "PLAY", "HELP",
 * and "QUIT". This file provides configuration information for the splash
 * screen.
 */
public class SplashConfig implements SplashConfiguration {
    /**
     * The name of the image to display as the splash screen. Be sure to
     * register this image in your main java file.
     */
    @Override
    public String getBackgroundImage() {
        return "splash.png";
    }

    /**
     * The name of the music to play while the splash screen is showing. Be sure
     * to register this file in your main java file.
     */
    @Override
    public String getMusic() {
        return "tune.ogg";
    }

    /**
     * The X coordinate of the bottom left corner of the "PLAY" button. If
     * you're not sure, click on the screen, and your 'console' in Eclipse
     * should tell you the coordinate where you clicked.
     */
    public int getPlayX() {
        return 92;
    }

    /**
     * The Y coordinate of the bottom left corner of the "PLAY" button.
     */
    public int getPlayY() {
        return 150;
    }

    /**
     * The width of the "PLAY" button
     */
    public int getPlayWidth() {
        return 75;
    }

    /**
     * The height of the "PLAY" button
     */
    public int getPlayHeight() {
        return 25;
    }

    /**
     * The X coordinate of the bottom left corner of the "HELP" button.
     */
    public int getHelpX() {
        return 284;
    }

    /**
     * The Y coordinate of the bottom left corner of the "HELP" button.
     */
    public int getHelpY() {
        return 140;
    }

    /**
     * The width of the "HELP" button
     */
    public int getHelpWidth() {
        return 70;
    }

    /**
     * The height of the "HELP" button
     */
    public int getHelpHeight() {
        return 25;
    }

    /**
     * The X coordinate of the bottom left corner of the "QUIT" button.
     */
    public int getQuitX() {
        return 163;
    }

    /**
     * The Y coordinate of the bottom left corner of the "QUIT" button.
     */
    public int getQuitY() {
        return 89;
    }

    /**
     * The width of the "QUIT" button
     */
    public int getQuitWidth() {
        return 75;
    }

    /**
     * The height of the "QUIT" button
     */
    public int getQuitHeight() {
        return 25;
    }
}
