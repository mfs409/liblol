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
        public int getRows();

        /**
         * Each chooser screen will have a bunch of buttons for playing specific
         * levels. This defines the number of columns of buttons on each screen.
         */
        public int getColumns();

        /**
         * This is the space, in pixels, between the top of the screen and the first
         * row of buttons
         * 
         * @return
         */
        public int getTopMargin();

        /**
         * This is the space, in pixels, between the left of the screen and the
         * first column of buttons
         */
        public int getLeftMargin();

        /**
         * This is the horizontal space between buttons
         */
        public int getHPadding();

        /**
         * This is the vertical space between buttons
         */
        public int getBPadding();

        /**
         * This is the name of the image that is displayed for each "level" button,
         * behind the text for that button
         */
        public String getLevelButtonName();

        /**
         * This is the width of each level button's image
         */
        public int getLevelButtonWidth();

        /**
         * This is the height of each level button's image
         */
        public int getLevelButtonHeight();

        /**
         * This is the name of the font to use when making the "level" buttons
         */
        public String getLevelFont();

        /**
         * This is the font size for the "level" buttons
         */
        public int getLevelFontSize();

        /**
         * This is the red component of the font for level buttons
         */
        public int getLevelFontRed();

        /**
         * This is the green component of the font for level buttons
         */
        public int getLevelFontGreen();

        /**
         * This is the blue component of the font for level buttons
         */
        public int getLevelFontBlue();

        /**
         * This is the text to display on locked levels... it's usually "X"
         */
        public String getLevelLockText();

        /**
         * This is the name of the music file to play for the Chooser scenes
         */
        public String getMusicName();

        /**
         * This is the name of the background image to display on the Chooser scenes
         */
        public String getBackgroundName();

        /**
         * The image to display as the "back to splash" button
         */
        public String getBackButtonName();

        /**
         * The X coordinate of the bottom left corner of the "back to splash" button
         */
        public int getBackButtonX();

        /**
         * The Y coordinate of the bottom left corner of the "back to splash" button
         */
        public int getBackButtonY();

        /**
         * The width of the "back to splash" button
         */
        public int getBackButtonWidth();

        /**
         * The height of the "back to splash" button
         */
        public int getBackButtonHeight();

        /**
         * The image to display as the "previous chooser screen" button
         */
        public String getPrevButtonName();

        /**
         * The X coordinate of bottom left corner of the "previous chooser screen"
         * button
         */
        public int getPrevButtonX();

        /**
         * The Y coordinate of bottom left corner of the "previous chooser screen"
         * button
         */
        public int getPrevButtonY();

        /**
         * The width of the "previous chooser screen" button
         */
        public int getPrevButtonWidth();

        /**
         * The width of the "previous chooser screen" button
         */
        public int getPrevButtonHeight();

        /**
         * The image to display as the "next chooser screen" button
         */
        public String getNextButtonName();

        /**
         * The X coordinate of the bottom left corner of the "next chooser screen"
         * button
         */
        public int getNextButtonX();

        /**
         * The Y coordinate of the bottom left corner of the "next chooser screen"
         * button
         */
        public int getNextButtonY();

        /**
         * The width of the "next chooser screen" button
         */
        public int getNextButtonWidth();

        /**
         * The height of the "next chooser screen" button
         */
        public int getNextButtonHeight();
    }
}
