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

import edu.lehigh.cse.lol.ChooserConfiguration;

public class ChooserConfig implements ChooserConfiguration {

    /**
     * Each chooser screen will have a bunch of buttons for playing specific
     * levels. This defines the number of rows of buttons on each screen.
     */
    @Override
    public int getRows() {
        return 1;
    }

    /**
     * Each chooser screen will have a bunch of buttons for playing specific
     * levels. This defines the number of columns of buttons on each screen.
     */
    @Override
    public int getColumns() {
        return 1;
    }

    /**
     * This is the space, in pixels, between the top of the screen and the first
     * row of buttons
     *
     * @return
     */
    @Override
    public int getTopMargin() {
        return 1;
    }

    /**
     * This is the space, in pixels, between the left of the screen and the
     * first column of buttons
     */
    @Override
    public int getLeftMargin() {
        return 1;
    }

    /**
     * This is the horizontal space between buttons
     */
    @Override
    public int getHPadding() {
        return 1;
    }

    /**
     * This is the vertical space between buttons
     */
    @Override
    public int getBPadding() {
        return 1;
    }

    /**
     * This is the name of the image that is displayed for each "level" button,
     * behind the text for that button
     */
    @Override
    public String getLevelButtonName() {
        return "";
    }

    /**
     * This is the width of each level button's image
     */
    @Override
    public int getLevelButtonWidth() {
        return 1;
    }

    /**
     * This is the height of each level button's image
     */
    @Override
    public int getLevelButtonHeight() {
        return 1;
    }

    /**
     * This is the name of the font to use when making the "level" buttons
     */
    @Override
    public String getLevelFont() {
        return "arial.ttf";
    }

    /**
     * This is the font size for the "level" buttons
     */
    @Override
    public int getLevelFontSize() {
        return 30;
    }

    /**
     * This is the name of the music file to play for the Chooser scenes
     */
    @Override
    public String getMusicName() {
        return "";
    }

    /**
     * This is the name of the background image to display on the Chooser scenes
     *
     * @return
     */
    @Override
    public String getBackgroundName() {
        return "";
    }

    /**
     * The image to display as the "back to splash" button
     */
    @Override
    public String getBackButtonName() {
        return "";
    }

    /**
     * The X coordinate of the bottom left corner of the "back to splash" button
     */
    @Override
    public int getBackButtonX() {
        return 1;
    }

    /**
     * The Y coordinate of the bottom left corner of the "back to splash" button
     */
    @Override
    public int getBackButtonY() {
        return 1;
    }

    /**
     * The width of the "back to splash" button
     */
    @Override
    public int getBackButtonWidth() {
        return 1;
    }

    /**
     * The height of the "back to splash" button
     */
    @Override
    public int getBackButtonHeight() {
        return 1;
    }

    /**
     * The image to display as the "previous chooser screen" button
     */
    @Override
    public String getPrevButtonName() {
        return "";
    }

    /**
     * The X coordinate of bottom left corner of the "previous chooser screen"
     * button
     */
    @Override
    public int getPrevButtonX() {
        return 1;
    }

    /**
     * The Y coordinate of bottom left corner of the "previous chooser screen"
     * button
     */
    @Override
    public int getPrevButtonY() {
        return 1;
    }

    /**
     * The width of the "previous chooser screen" button
     */
    @Override
    public int getPrevButtonWidth() {
        return 1;
    }

    /**
     * The width of the "previous chooser screen" button
     */
    @Override
    public int getPrevButtonHeight() {
        return 1;
    }

    /**
     * The image to display as the "next chooser screen" button
     */
    @Override
    public String getNextButtonName() {
        return "";
    }

    /**
     * The X coordinate of the bottom left corner of the "next chooser screen"
     * button
     */
    @Override
    public int getNextButtonX() {
        return 1;
    }

    /**
     * The Y coordinate of the bottom left corner of the "next chooser screen"
     * button
     */
    @Override
    public int getNextButtonY() {
        return 1;
    }

    /**
     * The width of the "next chooser screen" button
     */
    @Override
    public int getNextButtonWidth() {
        return 1;
    }

    /**
     * The height of the "next chooser screen" button
     */
    @Override
    public int getNextButtonHeight() {
        return 1;
    }
}
