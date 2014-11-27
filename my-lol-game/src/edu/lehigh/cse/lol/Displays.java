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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import edu.lehigh.cse.lol.Level.GestureAction;

public class Displays {
    /**
     * This is for handling everything that gets drawn on the HUD, whether it is
     * pressable or not
     */
    public static class Display {
        /**
         * A custom value that the control can store
         */
        float mVal;

        /**
         * Should we run code when this Control is touched?
         */
        boolean mIsTouchable;

        /**
         * Code to run when this Control is touched
         */
        GestureAction mGestureAction;

        /**
         * For touchable Controls, this is the rectangle on the screen that is
         * touchable
         */
        Rectangle mRange;

        /**
         * What color should we use to draw text, if this Control is a text
         * entity?
         */
        Color mColor = new Color(0, 0, 0, 1);

        /**
         * For disabling a control and stopping its rendering
         */
        boolean mIsActive = true;

        /**
         * What image should we display, if this Control has an image associated
         * with it?
         */
        TextureRegion mImage;

        /**
         * Use this constructor for controls that provide pressable images
         * 
         * @param imgName
         *            The name of the image to display. If "" is given as the
         *            name, it will not crash.
         * @param x
         *            The X coordinate (in pixels) of the bottom left corner.
         * @param y
         *            The Y coordinate (in pixels) of the bottom left corner.
         * @param width
         *            The width of the Control
         * @param height
         *            The height of the Control
         */
        Display(String imgName, int x, int y, int width, int height) {
            // set up the image to display
            //
            // NB: this will fail gracefully (no crash) for invalid file names
            TextureRegion[] trs = Media.getImage(imgName);
            if (trs != null)
                mImage = trs[0];

            // set up the touchable range for the image
            mRange = new Rectangle(x, y, width, height);
            mIsTouchable = true;
        }

        /**
         * Use this constructor for controls that are simply for displaying text
         * 
         * @param red
         *            The red portion of text color (0-255)
         * @param green
         *            The green portion of text color (0-255)
         * @param blue
         *            The blue portion of text color (0-255)
         */
        Display(int red, int green, int blue) {
            mColor.r = ((float) red) / 256;
            mColor.g = ((float) green) / 256;
            mColor.b = ((float) blue) / 256;
            mIsTouchable = false;
        }

        /**
         * This is the render method when we've got a valid TR. When we don't,
         * we're displaying text, which probably means we're also dynamically
         * updating the text to display on every render, so it makes sense to
         * overload the render() call for those Controls
         * 
         * @param sb
         *            The SpriteBatch to use to draw the image
         */
        void render(SpriteBatch sb) {
            if (mIsActive && mImage != null)
                sb.draw(mImage, mRange.x, mRange.y, 0, 0, mRange.width, mRange.height, 1, 1, 0);
        }

        /**
         * Disable a control
         */
        public void setInactive() {
            mIsActive = false;
        }

        /**
         * Enable a control
         */
        public void setActive() {
            mIsActive = true;
        }

        /**
         * Disable touch
         */
        public void disableTouch() {
            mIsTouchable = false;
        }

        /**
         * Enable touch
         */
        public void enableTouch() {
            mIsTouchable = true;
        }
    }

    /**
     * Controls is a pure static class, and should never be constructed
     * explicitly
     */
    private Displays() {
    }

    /**
     * A helper method to draw text nicely. In GDX, we draw everything by giving
     * the bottom left corner, except text, which takes the top left corner.
     * This function handles the conversion, so that we can use bottom-left.
     * 
     * @param x
     *            The X coordinate of the bottom left corner (in pixels)
     * @param y
     *            The Y coordinate of the bottom left corner (in pixels)
     * @param message
     *            The text to display
     * @param bf
     *            The BitmapFont object to use for the text's font
     * @param sb
     *            The SpriteBatch used to render the text
     */
    static void drawTextTransposed(int x, int y, String message, BitmapFont bf, SpriteBatch sb) {
        bf.drawMultiLine(sb, message, x, y + bf.getMultiLineBounds(message).height);
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Add a countdown timer to the screen. When time is up, the level ends in
     * defeat
     * 
     * @param timeout
     *            Starting value of the timer
     * @param text
     *            The text to display when the timer expires
     * @param x
     *            The X coordinate of the bottom left corner (in pixels)
     * @param y
     *            The Y coordinate of the bottom left corner (in pixels)
     */
    public static Display addCountdown(float timeout, String text, int x, int y) {
        return addCountdown(timeout, text, x, y, Lol.sGame.mConfig.getDefaultFontFace(),
                Lol.sGame.mConfig.getDefaultFontRed(), Lol.sGame.mConfig.getDefaultFontGreen(),
                Lol.sGame.mConfig.getDefaultFontBlue(), Lol.sGame.mConfig.getDefaultFontSize());
    }

    /**
     * Add a countdown timer to the screen, with extra features for describing
     * the appearance of the font. When time is up, the level ends in defeat.
     * 
     * @param timeout
     *            Starting value of the timer
     * @param text
     *            The text to display when the timer expires
     * @param x
     *            The X coordinate of the bottom left corner (in pixels)
     * @param y
     *            The Y coordinate of the bottom left corner (in pixels)
     * @param fontname
     *            The name of the font file to use
     * @param red
     *            The red portion of text color (0-255)
     * @param green
     *            The green portion of text color (0-255)
     * @param blue
     *            The blue portion of text color (0-255)
     * @param size
     *            The font size to use (20 is usually a good value)
     */
    public static Display addCountdown(final float timeout, final String text, final int x, final int y,
            String fontName, final int red, final int green, final int blue, int size) {
        Level.sCurrent.mScore.mCountDownRemaining = timeout;
        final BitmapFont bf = Media.getFont(fontName, size);
        Display d = new Display(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                Level.sCurrent.mScore.mCountDownRemaining -= Gdx.graphics.getDeltaTime();
                if (Level.sCurrent.mScore.mCountDownRemaining > 0) {
                    drawTextTransposed(x, y, "" + (int) Level.sCurrent.mScore.mCountDownRemaining, bf, sb);
                } else {
                    PostScene.setDefaultLoseText(text);
                    Level.sCurrent.mScore.endLevel(false);
                }
            }
        };
        Level.sCurrent.mDisplays.add(d);
        return d;
    }

    /**
     * Print the frames per second
     * 
     * @param x
     *            The X coordinate of the bottom left corner (in pixels)
     * @param y
     *            The Y coordinate of the bottom left corner (in pixels)
     * @param fontname
     *            The name of the font file to use
     * @param red
     *            The red portion of text color (0-255)
     * @param green
     *            The green portion of text color (0-255)
     * @param blue
     *            The blue portion of text color (0-255)
     * @param size
     *            The font size to use (20 is usually a good value)
     */
    public static Display addFPS(final int x, final int y, String fontName, final int red, final int green,
            final int blue, int size) {
        final BitmapFont bf = Media.getFont(fontName, size);

        Display d = new Display(red, green, blue) {
            // TODO: we could have render() take a 'if text' route, and then
            // just override a "maketext" method
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, "fps: " + Gdx.graphics.getFramesPerSecond(), bf, sb);
            }
        };
        Level.sCurrent.mDisplays.add(d);
        return d;
    }

    /**
     * Add a countdown timer to the screen. When time is up, the level ends in
     * victory
     * 
     * @param timeout
     *            Starting value of the timer
     * @param x
     *            The X coordinate of the bottom left corner (in pixels)
     * @param y
     *            The Y coordinate of the bottom left corner (in pixels)
     */
    public static Display addWinCountdown(float timeout, int x, int y) {
        return addWinCountdown(timeout, x, y, Lol.sGame.mConfig.getDefaultFontFace(),
                Lol.sGame.mConfig.getDefaultFontRed(), Lol.sGame.mConfig.getDefaultFontGreen(),
                Lol.sGame.mConfig.getDefaultFontBlue(), Lol.sGame.mConfig.getDefaultFontSize());
    }

    /**
     * Add a countdown timer to the screen, with extra features for describing
     * the appearance of the font. When time is up, the level ends in victory
     * 
     * @param timeout
     *            Starting value of the timer
     * @param x
     *            The X coordinate of the bottom left corner (in pixels)
     * @param y
     *            The Y coordinate of the bottom left corner (in pixels)
     * @param fontname
     *            The name of the font file to use
     * @param red
     *            The red portion of text color (0-255)
     * @param green
     *            The green portion of text color (0-255)
     * @param blue
     *            The blue portion of text color (0-255)
     * @param size
     *            The font size to use (20 is usually a good value)
     */
    public static Display addWinCountdown(final float timeout, final int x, final int y, String fontName,
            final int red, final int green, final int blue, int size) {
        Level.sCurrent.mScore.mWinCountRemaining = timeout;
        final BitmapFont bf = Media.getFont(fontName, size);
        Display d = new Display(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                Level.sCurrent.mScore.mWinCountRemaining -= Gdx.graphics.getDeltaTime();
                if (Level.sCurrent.mScore.mWinCountRemaining > 0)
                    // get elapsed time for this level
                    drawTextTransposed(x, y, "" + (int) Level.sCurrent.mScore.mWinCountRemaining, bf, sb);
                else
                    Level.sCurrent.mScore.endLevel(true);
            }
        };
        Level.sCurrent.mDisplays.add(d);
        return d;
    }

    /**
     * Add a count of the current number of goodies of the specified type, with
     * extra features for describing the appearance of the font
     * 
     * @param type
     *            The type of goodie to show (1-4)
     * @param max
     *            If this is > 0, then the message wil be of the form XX/max
     *            instead of just XX
     * @param text
     *            The text to display after the number of goodies
     * @param x
     *            The X coordinate of the bottom left corner (in pixels)
     * @param y
     *            The Y coordinate of the bottom left corner (in pixels)
     * @param fontname
     *            The name of the font file to use
     * @param red
     *            The red portion of text color (0-255)
     * @param green
     *            The green portion of text color (0-255)
     * @param blue
     *            The blue portion of text color (0-255)
     * @param size
     *            The font size to use (20 is usually a good value)
     */
    public static Display addGoodieCount(final int type, int max, final String text, final int x, final int y,
            String fontName, final int red, final int green, final int blue, int size) {
        // The suffix to display after the goodie count:
        final String suffix = (max > 0) ? "/" + max + " " + text : " " + text;
        final BitmapFont bf = Media.getFont(fontName, size);
        Display d = new Display(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, "" + Level.sCurrent.mScore.mGoodiesCollected[type - 1] + suffix, bf, sb);
            }
        };
        Level.sCurrent.mDisplays.add(d);
        return d;
    }

    /**
     * Add a count of the number of enemies who have been defeated
     * 
     * @param max
     *            If this is > 0, then the message will be of the form XX/max
     *            instead of just XX
     * @param text
     *            The text to display after the number of goodies
     * @param x
     *            The X coordinate of the bottom left corner (in pixels)
     * @param y
     *            The Y coordinate of the bottom left corner (in pixels)
     */
    public static Display addDefeatedCount(int max, String text, int x, int y) {
        return addDefeatedCount(max, text, x, y, Lol.sGame.mConfig.getDefaultFontFace(),
                Lol.sGame.mConfig.getDefaultFontRed(), Lol.sGame.mConfig.getDefaultFontGreen(),
                Lol.sGame.mConfig.getDefaultFontBlue(), Lol.sGame.mConfig.getDefaultFontSize());
    }

    /**
     * Add a count of the number of enemies who have been defeated, with extra
     * features for describing the appearance of the font
     * 
     * @param max
     *            If this is > 0, then the message wil be of the form XX/max
     *            instead of just XX
     * @param text
     *            The text to display after the number of goodies
     * @param x
     *            The X coordinate of the bottom left corner (in pixels)
     * @param y
     *            The Y coordinate of the bottom left corner (in pixels)
     * @param fontname
     *            The name of the font file to use
     * @param red
     *            The red portion of text color (0-255)
     * @param green
     *            The green portion of text color (0-255)
     * @param blue
     *            The blue portion of text color (0-255)
     * @param size
     *            The font size to use (20 is usually a good value)
     */
    public static Display addDefeatedCount(int max, final String text, final int x, final int y, String fontName,
            final int red, final int green, final int blue, int size) {
        // The suffix to display after the goodie count:
        final String suffix = (max > 0) ? "/" + max + " " + text : " " + text;
        final BitmapFont bf = Media.getFont(fontName, size);
        Display d = new Display(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, "" + Level.sCurrent.mScore.mEnemiesDefeated + suffix, bf, sb);
            }
        };
        Level.sCurrent.mDisplays.add(d);
        return d;
    }

    /**
     * Add a stopwatch for tracking how long a level takes
     * 
     * @param x
     *            The X coordinate of the bottom left corner (in pixels)
     * @param y
     *            The Y coordinate of the bottom left corner (in pixels)
     */
    static public Display addStopwatch(int x, int y) {
        return addStopwatch(x, y, Lol.sGame.mConfig.getDefaultFontFace(), Lol.sGame.mConfig.getDefaultFontRed(),
                Lol.sGame.mConfig.getDefaultFontGreen(), Lol.sGame.mConfig.getDefaultFontBlue(),
                Lol.sGame.mConfig.getDefaultFontSize());
    }

    /**
     * Add a stopwatch for tracking how long a level takes, with extra features
     * for describing the appearance of the font
     * 
     * @param x
     *            The X coordinate of the bottom left corner (in pixels)
     * @param y
     *            The Y coordinate of the bottom left corner (in pixels)
     * @param fontname
     *            The name of the font file to use
     * @param red
     *            The red portion of text color (0-255)
     * @param green
     *            The green portion of text color (0-255)
     * @param blue
     *            The blue portion of text color (0-255)
     * @param size
     *            The font size to use (20 is usually a good value)
     */
    static public Display addStopwatch(final int x, final int y, String fontName, final int red, final int green,
            final int blue, int size) {
        final BitmapFont bf = Media.getFont(fontName, size);
        Display d = new Display(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                Level.sCurrent.mScore.mStopWatchProgress += Gdx.graphics.getDeltaTime();
                drawTextTransposed(x, y, "" + (int) Level.sCurrent.mScore.mStopWatchProgress, bf, sb);
            }
        };
        Level.sCurrent.mDisplays.add(d);
        return d;
    }

    /**
     * Display a strength meter for a specific hero
     * 
     * @param text
     *            The text to display after the remaining strength value
     * @param x
     *            The X coordinate of the bottom left corner (in pixels)
     * @param y
     *            The Y coordinate of the bottom left corner (in pixels)
     * @param h
     *            The Hero whose strength should be displayed
     */
    static public Display addStrengthMeter(String text, int x, int y, Hero h) {
        // forward to the more powerful method...
        return addStrengthMeter(text, x, y, Lol.sGame.mConfig.getDefaultFontFace(),
                Lol.sGame.mConfig.getDefaultFontRed(), Lol.sGame.mConfig.getDefaultFontGreen(),
                Lol.sGame.mConfig.getDefaultFontBlue(), Lol.sGame.mConfig.getDefaultFontSize(), h);
    }

    /**
     * Display a strength meter for a specific hero, with extra features for
     * describing the appearance of the font
     * 
     * @param text
     *            The text to display after the remaining strength value
     * @param x
     *            The X coordinate of the bottom left corner (in pixels)
     * @param y
     *            The Y coordinate of the bottom left corner (in pixels)
     * @param fontname
     *            The name of the font file to use
     * @param red
     *            The red portion of text color (0-255)
     * @param green
     *            The green portion of text color (0-255)
     * @param blue
     *            The blue portion of text color (0-255)
     * @param size
     *            The font size to use (20 is usually a good value)
     * @param h
     *            The Hero whose strength should be displayed
     */
    static public Display addStrengthMeter(final String text, final int x, final int y, String fontName, final int red,
            final int green, final int blue, int size, final Hero h) {
        final BitmapFont bf = Media.getFont(fontName, size);
        Display d = new Display(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, "" + h.getStrength() + " " + text, bf, sb);
            }
        };
        Level.sCurrent.mDisplays.add(d);
        return d;
    }

    /**
     * Display a meter showing how far a hero has traveled
     * 
     * @param text
     *            The text to display after the remaining strength value
     * @param x
     *            The X coordinate of the bottom left corner (in pixels)
     * @param y
     *            The Y coordinate of the bottom left corner (in pixels)
     * @param fontname
     *            The name of the font file to use
     * @param red
     *            The red portion of text color (0-255)
     * @param green
     *            The green portion of text color (0-255)
     * @param blue
     *            The blue portion of text color (0-255)
     * @param size
     *            The font size to use (20 is usually a good value)
     * @param h
     *            The Hero whose distance should be displayed
     */
    static public Display addDistanceMeter(final String text, final int x, final int y, String fontName, final int red,
            final int green, final int blue, int size, final Hero h) {
        final BitmapFont bf = Media.getFont(fontName, size);
        Display d = new Display(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                Level.sCurrent.mScore.mDistance = (int) h.getXPosition();
                drawTextTransposed(x, y, "" + Level.sCurrent.mScore.mDistance + " " + text, bf, sb);
            }
        };
        Level.sCurrent.mDisplays.add(d);
        return d;
    }

    /**
     * Display the number of remaining projectiles
     * 
     * @param text
     *            The text to display after the number of goodies
     * @param x
     *            The X coordinate of the bottom left corner (in pixels)
     * @param y
     *            The Y coordinate of the bottom left corner (in pixels)
     * @param fontname
     *            The name of the font file to use
     * @param red
     *            The red portion of text color (0-255)
     * @param green
     *            The green portion of text color (0-255)
     * @param blue
     *            The blue portion of text color (0-255)
     * @param size
     *            The font size to use (20 is usually a good value)
     */
    public static Display addProjectileCount(final String text, final int x, final int y, String fontName,
            final int red, final int green, final int blue, int size) {
        final BitmapFont bf = Media.getFont(fontName, size);
        Display d = new Display(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, "" + Level.sCurrent.mProjectilePool.mProjectilesRemaining + " " + text, bf, sb);
            }
        };
        Level.sCurrent.mDisplays.add(d);
        return d;
    }

    /**
     * Add an image to the heads-up display. Touching the image has no effect
     * 
     * @param x
     *            The X coordinate of the bottom left corner (in pixels)
     * @param y
     *            The Y coordinate of the bottom left corner (in pixels)
     * @param width
     *            The width of the image
     * @param height
     *            The height of the image
     * @param imgName
     *            The name of the image to display. Use "" for an invisible
     *            button
     */
    public static Display addImage(int x, int y, int width, int height, String imgName) {
        Display d = new Display(imgName, x, y, width, height);
        d.mIsTouchable = false;
        Level.sCurrent.mDisplays.add(d);
        return d;
    }

    /**
     * Display text corresponding to a fact saved for the current level
     * 
     * @param key
     *            The key to look up in the Fact store
     * @param x
     *            The X coordinate of the bottom left corner (in pixels)
     * @param y
     *            The Y coordinate of the bottom left corner (in pixels)
     * @param fontname
     *            The name of the font file to use
     * @param red
     *            The red portion of text color (0-255)
     * @param green
     *            The green portion of text color (0-255)
     * @param blue
     *            The blue portion of text color (0-255)
     * @param size
     *            The font size to use (20 is usually a good value)
     * @param prefix
     *            Text to put before the fact
     * @param suffix
     *            Text to put after the fact
     */
    static public Display addLevelFact(final String key, final int x, final int y, String fontName, final int red,
            final int green, final int blue, int size, final String prefix, final String suffix) {
        final BitmapFont bf = Media.getFont(fontName, size);
        Display d = new Display(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, prefix + "" + Facts.getLevelFact(key) + suffix, bf, sb);
            }
        };
        Level.sCurrent.mDisplays.add(d);
        return d;
    }

    /**
     * Display text corresponding to a fact saved for the current game session
     * 
     * @param key
     *            The key to look up in the Fact store
     * @param x
     *            The X coordinate of the bottom left corner (in pixels)
     * @param y
     *            The Y coordinate of the bottom left corner (in pixels)
     * @param fontname
     *            The name of the font file to use
     * @param red
     *            The red portion of text color (0-255)
     * @param green
     *            The green portion of text color (0-255)
     * @param blue
     *            The blue portion of text color (0-255)
     * @param size
     *            The font size to use (20 is usually a good value)
     * @param prefix
     *            Text to put before the fact
     * @param suffix
     *            Text to put after the fact
     */
    static public Display addSessionFact(final String key, final int x, final int y, String fontName, final int red,
            final int green, final int blue, int size, final String prefix, final String suffix) {
        final BitmapFont bf = Media.getFont(fontName, size);
        Display d = new Display(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, prefix + "" + Facts.getSessionFact(key) + suffix, bf, sb);
            }
        };
        Level.sCurrent.mDisplays.add(d);
        return d;
    }

    /**
     * Display text corresponding to a fact saved for the game
     * 
     * @param key
     *            The key to look up in the Fact store
     * @param x
     *            The X coordinate of the bottom left corner (in pixels)
     * @param y
     *            The Y coordinate of the bottom left corner (in pixels)
     * @param fontname
     *            The name of the font file to use
     * @param red
     *            The red portion of text color (0-255)
     * @param green
     *            The green portion of text color (0-255)
     * @param blue
     *            The blue portion of text color (0-255)
     * @param size
     *            The font size to use (20 is usually a good value)
     * @param prefix
     *            Text to put before the fact
     * @param suffix
     *            Text to put after the fact
     */
    static public Display addGameFact(final String key, final int x, final int y, String fontName, final int red,
            final int green, final int blue, int size, final String prefix, final String suffix) {
        final BitmapFont bf = Media.getFont(fontName, size);
        Display d = new Display(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, prefix + "" + Facts.getGameFact(key) + suffix, bf, sb);
            }
        };
        Level.sCurrent.mDisplays.add(d);
        return d;
    }

}
