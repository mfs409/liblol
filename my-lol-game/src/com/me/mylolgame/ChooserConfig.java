/**
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org>
 */

package com.me.mylolgame;

public class ChooserConfig {

    /**
     * Each chooser screen will have a bunch of buttons for playing specific
     * levels. This defines the number of rows of buttons on each screen.
     */
    public int getRows() {
        return 2;
    }

    /**
     * Each chooser screen will have a bunch of buttons for playing specific
     * levels. This defines the number of columns of buttons on each screen.
     */
    public int getColumns() {
        return 5;
    }

    /**
     * This is the space, in pixels, between the top of the screen and the first
     * row of buttons
     * 
     * @return
     */
    
    public int getTopMargin() {
        return 260;
    }

    /**
     * This is the space, in pixels, between the left of the screen and the
     * first column of buttons
     */
    
    public int getLeftMargin() {
        return 170;
    }

    /**
     * This is the horizontal space between buttons
     */
    
    public int getHPadding() {
        return 30;
    }

    /**
     * This is the vertical space between buttons
     */
    
    public int getBPadding() {
        return 30;
    }

    /**
     * This is the name of the image that is displayed for each "level" button,
     * behind the text for that button
     */
    
    public String getLevelButtonName() {
        return "leveltile.png";
    }

    /**
     * This is the width of each level button's image
     */
    
    public int getLevelButtonWidth() {
        return 100;
    }

    /**
     * This is the height of each level button's image
     */
    
    public int getLevelButtonHeight() {
        return 100;
    }

    /**
     * This is the name of the font to use when making the "level" buttons
     */
    
    public String getLevelFont() {
        return "arial.ttf";
    }

    /**
     * This is the font size for the "level" buttons
     */
    
    public int getLevelFontSize() {
        return 56;
    }

    /**
     * This is the red component of the font for level buttons
     */
    
    public int getLevelFontRed() {
        return 255;
    }

    /**
     * This is the green component of the font for level buttons
     */
    
    public int getLevelFontGreen() {
        return 255;
    }

    /**
     * This is the blue component of the font for level buttons
     */
    
    public int getLevelFontBlue() {
        return 255;
    }

    /**
     * This is the text to display on locked levels
     */
    
    public String getLevelLockText() {
        return "X";
    }

    /**
     * This is the name of the music file to play for the Chooser scenes
     */
    
    public String getMusicName() {
        return "tune.ogg";
    }

    /**
     * This is the name of the background image to display on the Chooser scenes
     */
    
    public String getBackgroundName() {
        return "chooser.png";
    }

    /**
     * The image to display as the "back to splash" button
     */
    
    public String getBackButtonName() {
        return "backarrow.png";
    }

    /**
     * The X coordinate of the bottom left corner of the "back to splash" button
     */
    
    public int getBackButtonX() {
        return 0;
    }

    /**
     * The Y coordinate of the bottom left corner of the "back to splash" button
     */
    
    public int getBackButtonY() {
        return 0;
    }

    /**
     * The width of the "back to splash" button
     */
    
    public int getBackButtonWidth() {
        return 50;
    }

    /**
     * The height of the "back to splash" button
     */
    
    public int getBackButtonHeight() {
        return 50;
    }

    /**
     * The image to display as the "previous chooser screen" button
     */
    
    public String getPrevButtonName() {
        return "leftarrow.png";
    }

    /**
     * The X coordinate of bottom left corner of the "previous chooser screen"
     * button
     */
    
    public int getPrevButtonX() {
        return 0;
    }

    /**
     * The Y coordinate of bottom left corner of the "previous chooser screen"
     * button
     */
    
    public int getPrevButtonY() {
        return 220;
    }

    /**
     * The width of the "previous chooser screen" button
     */
    
    public int getPrevButtonWidth() {
        return 80;
    }

    /**
     * The width of the "previous chooser screen" button
     */
    
    public int getPrevButtonHeight() {
        return 80;
    }

    /**
     * The image to display as the "next chooser screen" button
     */
    
    public String getNextButtonName() {
        return "rightarrow.png";
    }

    /**
     * The X coordinate of the bottom left corner of the "next chooser screen"
     * button
     */
    
    public int getNextButtonX() {
        return 880;
    }

    /**
     * The Y coordinate of the bottom left corner of the "next chooser screen"
     * button
     */
    
    public int getNextButtonY() {
        return 220;
    }

    /**
     * The width of the "next chooser screen" button
     */
    
    public int getNextButtonWidth() {
        return 80;
    }

    /**
     * The height of the "next chooser screen" button
     */
    
    public int getNextButtonHeight() {
        return 80;
    }

    /**
     * Return true if pressing 'play' should result in the level-chooser screen
     * showing; return false if pressing 'play' should immediately start level
     * one.
     */
    
    public boolean showChooser() {
        return true;
    }
}
