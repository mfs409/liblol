using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

// NOTE: PLACEHOLDER
namespace LOL
{
    public class Physics
    {
        /**
         * This ratio means that every 10 pixels on the screen will correspond to a
         * meter. Note that 'pixels' are defined in terms of what a programmer's
         * Config says, not the actual screen size, because the programmer's Config
         * gets scaled to screen dimensions.
         */
        public static float PIXEL_METER_RATIO = 10;

        /**
         * Configure physics for the current level
         * 
         * @param defaultXGravity The default force moving entities to the left
         *            (negative) or right (positive)... Usually zero
         * @param defaultYGravity The default force pushing the hero down (negative)
         *            or up (positive)... Usually zero or -10
         */
        public static void configure(float defaultXGravity, float defaultYGravity) {
            
        }
    }
}
