using System;

namespace LibLOL
{
    public interface ChooserConfiguration
    {
        /*
         * Each chooser screen will have a bunch of buttons for playing specific
         * levels. This defines the number of rows of buttons on each screen.
         */
        int GetRows();

        /*
         * Each chooser screen will have a bunch of buttons for playing specific
         * levels. This defines the number of columns of buttons on each screen.
         */
        int GetColumns();

        /*
         * This is the space, in pixels, between the top of the screen and the first
         * row of buttons
         */
        int GetTopMargin();

        /*
         * This is the space, in pixels, between the left of the screen and the
         * first column of buttons
         */
        int GetLeftMargin();

        /*
         * This is the horizontal space between buttons
         */
        int GetHPadding();

        /*
         * This is the vertical space between buttons
         */
        int GetBPadding();

        /*
         * This is the name of the image that is displayed for each "level" button,
         * behind the text for that button
         */
        string GetLevelButtonName();

        /*
         * This is the width of each level button's image
         */
        int GetLevelButtonWidth();

        /*
         * This is the height of each level button's image
         */
        int GetLevelButtonHeight();

        /*
         * This is the name of the font to use when making the "level" buttons
         */
        string GetLevelFont();

        /*
         * This is the font size for the "level" buttons
         */
        int GetLevelFontSize();

        /*
         * This is the red component of the font for level buttons
         */
        int GetLevelFontRed();

        /*
         * This is the green component of the font for level buttons
         */
        int GetLevelFontGreen();

        /**
         * This is the blue component of the font for level buttons
         */
        int GetLevelFontBlue();

        /*
         * This is the text to display on locked levels... it's usually "X"
         */
        string GetLevelLockText();

        /*
         * This is the name of the music file to play for the Chooser scenes
         */
        string GetMusicName();

        /*
         * This is the name of the background image to display on the Chooser scenes
         */
        string GetBackgroundName();

        /*
         * The image to display as the "back to splash" button
         */
        string GetBackButtonName();

        /*
         * The X coordinate of the bottom left corner of the "back to splash" button
         */
        int GetBackButtonX();

        /*
         * The Y coordinate of the bottom left corner of the "back to splash" button
         */
        int GetBackButtonY();

        /*
         * The width of the "back to splash" button
         */
        int GetBackButtonWidth();

        /*
         * The height of the "back to splash" button
         */
        int GetBackButtonHeight();

        /*
         * The image to display as the "previous chooser screen" button
         */
        string GetPrevButtonName();

        /**
         * The X coordinate of bottom left corner of the "previous chooser screen"
         * button
         */
        int GetPrevButtonX();

        /**
         * The Y coordinate of bottom left corner of the "previous chooser screen"
         * button
         */
        int GetPrevButtonY();

        /*
         * The width of the "previous chooser screen" button
         */
        int GetPrevButtonWidth();

        /*
         * The width of the "previous chooser screen" button
         */
        int GetPrevButtonHeight();

        /*
         * The image to display as the "next chooser screen" button
         */
        string GetNextButtonName();

        /*
         * The X coordinate of the bottom left corner of the "next chooser screen"
         * button
         */
        int GetNextButtonX();

        /*
         * The Y coordinate of the bottom left corner of the "next chooser screen"
         * button
         */
        int GetNextButtonY();

        /*
         * The width of the "next chooser screen" button
         */
        int GetNextButtonWidth();

        /*
         * The height of the "next chooser screen" button
         */
        int GetNextButtonHeight();
    }
}
