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

package edu.lehigh.cse.lol;

/**
 * The ChooserConfiguration interface describes the key characteristics of the
 * level chooser screens. The chooser consists of a bunch of screens, each
 * containing a bunch of Level buttons, which are used to choose specific levels
 * to play. There are also buttons for going back to the splash screen, and for
 * moving among the screens of the chooser.
 */
public interface ChooserConfiguration {
    /**
     * Each chooser screen will have a bunch of buttons for playing specific
     * levels. This defines the number of rows of buttons on each screen.
     */
    int getRows();

    /**
     * Each chooser screen will have a bunch of buttons for playing specific
     * levels. This defines the number of columns of buttons on each screen.
     */
    int getColumns();

    /**
     * This is the space, in pixels, between the top of the screen and the first
     * row of buttons
     * 
     * @return
     */
    int getTopMargin();

    /**
     * This is the space, in pixels, between the left of the screen and the
     * first column of buttons
     */
    int getLeftMargin();

    /**
     * This is the horizontal space between buttons
     */
    int getHPadding();

    /**
     * This is the vertical space between buttons
     */
    int getBPadding();

    /**
     * This is the name of the image that is displayed for each "level" button,
     * behind the text for that button
     */
    String getLevelButtonName();

    /**
     * This is the width of each level button's image
     */
    int getLevelButtonWidth();

    /**
     * This is the height of each level button's image
     */
    int getLevelButtonHeight();

    /**
     * This is the name of the font to use when making the "level" buttons
     */
    String getLevelFont();

    /**
     * This is the font size for the "level" buttons
     */
    int getLevelFontSize();

    /**
     * This is the red component of the font for level buttons
     */
    int getLevelFontRed();

    /**
     * This is the green component of the font for level buttons
     */
    int getLevelFontGreen();

    /**
     * This is the blue component of the font for level buttons
     */
    int getLevelFontBlue();

    /**
     * This is the text to display on locked levels... it's usually "X"
     */
    String getLevelLockText();

    /**
     * This is the name of the music file to play for the Chooser scenes
     */
    String getMusicName();

    /**
     * This is the name of the background image to display on the Chooser scenes
     */
    String getBackgroundName();

    /**
     * The image to display as the "back to splash" button
     */
    String getBackButtonName();

    /**
     * The X coordinate of the bottom left corner of the "back to splash" button
     */
    int getBackButtonX();

    /**
     * The Y coordinate of the bottom left corner of the "back to splash" button
     */
    int getBackButtonY();

    /**
     * The width of the "back to splash" button
     */
    int getBackButtonWidth();

    /**
     * The height of the "back to splash" button
     */
    int getBackButtonHeight();

    /**
     * The image to display as the "previous chooser screen" button
     */
    String getPrevButtonName();

    /**
     * The X coordinate of bottom left corner of the "previous chooser screen"
     * button
     */
    int getPrevButtonX();

    /**
     * The Y coordinate of bottom left corner of the "previous chooser screen"
     * button
     */
    int getPrevButtonY();

    /**
     * The width of the "previous chooser screen" button
     */
    int getPrevButtonWidth();

    /**
     * The width of the "previous chooser screen" button
     */
    int getPrevButtonHeight();

    /**
     * The image to display as the "next chooser screen" button
     */
    String getNextButtonName();

    /**
     * The X coordinate of the bottom left corner of the "next chooser screen"
     * button
     */
    int getNextButtonX();

    /**
     * The Y coordinate of the bottom left corner of the "next chooser screen"
     * button
     */
    int getNextButtonY();

    /**
     * The width of the "next chooser screen" button
     */
    int getNextButtonWidth();

    /**
     * The height of the "next chooser screen" button
     */
    int getNextButtonHeight();
    
    /**
     * Return true if pressing 'play' should result in the level-chooser screen
     * showing; return false if pressing 'play' should immediately start level
     * one.
     */
    boolean showChooser();
}
