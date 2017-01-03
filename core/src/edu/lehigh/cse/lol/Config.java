/**
 * This is free and unencumbered software released into the public domain.
 * <p>
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * <p>
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * <p>
 * For more information, please refer to <http://unlicense.org>
 */

package edu.lehigh.cse.lol;

/**
 * Config stores game-specific configuration values.
 * <p>
 * A programmer should extend Config, and change these values in their class constructor.
 */
public class Config {
    /// The number of pixels on screen that correspond to a meter in the game.
    ///
    /// NB: 'pixels' are relative to <code>mWidth</code> and <code>mHeight</code>
    protected float mPixelMeterRatio;
    /// The default screen width (note: it will be stretched to fill the phone screen)
    public int mWidth;
    /// The default screen height (note: it will be stretched to fill the phone screen)
    public int mHeight;
    /// Should the phone vibrate on certain events?
    protected boolean mEnableVibration;

    /// The game title.  This only matters in Desktop mode.
    public String mGameTitle;
    /// Default text to display when a level is won
    protected String mDefaultWinText;
    /// Default text to display when a level is lost
    protected String mDefaultLoseText;

    /// When this is true, the game will show an outline corresponding to the physics body behind
    /// each WorldActor
    protected boolean mShowDebugBoxes;

    /// Total number of levels. This helps the transition when a level is won
    protected int mNumLevels;
    /// Should the level chooser be activated?
    protected boolean mEnableChooser;
    /// Should all levels be unlocked?
    protected boolean mUnlockAllLevels;

    /// A per-game string, to use for storing information on an Android device
    protected String mStorageKey;

    /// The default font face to use when writing text to the screen
    protected String mDefaultFontFace;
    /// Default font size
    protected int mDefaultFontSize;
    /// Default font color, as #RRGGBB value
    protected String mDefaultFontColor;

    /// The list of image files that will be used by the game
    protected String[] mImageNames;
    /// The list of audio files that will be used as sound effects by the game
    protected String[] mSoundNames;
    /// The list of audio files that will be used as (looping) background music by the game
    protected String[] mMusicNames;

    /// An object to draw the main levels of the game
    protected ScreenManager mLevels;
    /// An object to draw the level chooser
    protected ScreenManager mChooser;
    /// An object to draw the help screens
    protected ScreenManager mHelp;
    /// An object to draw the opening "splash" screen
    protected ScreenManager mSplash;
    /// An object to draw the store screens
    protected ScreenManager mStore;
}
