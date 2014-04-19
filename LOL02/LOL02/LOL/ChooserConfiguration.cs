using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace LOL
{
    public interface ChooserConfiguration
    {
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
    }
}
