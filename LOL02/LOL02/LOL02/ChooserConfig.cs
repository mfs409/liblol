using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using LOL;

namespace LOL02
{
    public class ChooserConfig: ChooserConfiguration
    {

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
            return Util.ah(130);
        }

        /**
         * This is the space, in pixels, between the left of the screen and the
         * first column of buttons
         */
    
        public int getLeftMargin() {
            return Util.ax(85);
        }

        /**
         * This is the horizontal space between buttons
         */
    
        public int getHPadding() {
            return Util.ax(15);
        }

        /**
         * This is the vertical space between buttons
         */
    
        public int getBPadding() {
            return Util.ah(15);
        }

        /**
         * This is the name of the image that is displayed for each "level" button,
         * behind the text for that button
         */
    
        public String getLevelButtonName() {
            return "leveltile";
        }

        /**
         * This is the width of each level button's image
         */
    
        public int getLevelButtonWidth() {
            return Util.ax(50);
        }

        /**
         * This is the height of each level button's image
         */
    
        public int getLevelButtonHeight() {
            return Util.ah(50);
        }

        /**
         * This is the name of the font to use when making the "level" buttons
         */
    
        public String getLevelFont() {
            return "Default";
        }

        /**
         * This is the font size for the "level" buttons
         */
    
        public int getLevelFontSize() {
            return 28;
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
            return "tune";
        }

        /**
         * This is the name of the background image to display on the Chooser scenes
         */
    
        public String getBackgroundName() {
            return "chooser";
        }

        /**
         * The image to display as the "back to splash" button
         */
    
        public String getBackButtonName() {
            return "backarrow";
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
            return Util.ax(25);
        }

        /**
         * The height of the "back to splash" button
         */
    
        public int getBackButtonHeight() {
            return Util.ah(25);
        }

        /**
         * The image to display as the "previous chooser screen" button
         */
    
        public String getPrevButtonName() {
            return "leftarrow";
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
            return Util.ay(110, 40);
        }

        /**
         * The width of the "previous chooser screen" button
         */
    
        public int getPrevButtonWidth() {
            return Util.ax(40);
        }

        /**
         * The width of the "previous chooser screen" button
         */
    
        public int getPrevButtonHeight() {
            return Util.ah(40);
        }

        /**
         * The image to display as the "next chooser screen" button
         */
    
        public String getNextButtonName() {
            return "rightarrow";
        }

        /**
         * The X coordinate of the bottom left corner of the "next chooser screen"
         * button
         */
    
        public int getNextButtonX() {
            return Util.ax(440);
        }

        /**
         * The Y coordinate of the bottom left corner of the "next chooser screen"
         * button
         */
    
        public int getNextButtonY() {
            return Util.ay(110, 40);
        }

        /**
         * The width of the "next chooser screen" button
         */
    
        public int getNextButtonWidth() {
            return Util.ax(40);
        }

        /**
         * The height of the "next chooser screen" button
         */
    
        public int getNextButtonHeight() {
            return Util.ah(40);
        }
    }
}
