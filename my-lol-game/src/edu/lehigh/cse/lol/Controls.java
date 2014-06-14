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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * LOL Games have a heads-up display (hud). The hud is a place for displaying
 * text and drawing touchable buttons, so that as the hero moves through the
 * level, the buttons and text can remain at the same place on the screen
 */
public class Controls {
    /**
     * This is for handling everything that gets drawn on the HUD, whether it is
     * pressable or not
     */
    public static class Control {
        /**
         * Should we run code when this HudEntity is touched?
         */
        boolean mIsTouchable;
        
        /**
         * For touchable HudEntities, this is the rectangle on the screen that
         * is touchable
         */
        Rectangle mRange;

        /**
         * What color should we use to draw text, if this HudEntity is a text
         * entity?
         */
        Color mColor = new Color(0, 0, 0, 1);

        /**
         * For disabling a control and stopping its rendering
         */
        boolean mIsActive = true;

        /**
         * What image should we display, if this HudEntity has an image
         * associated with it?
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
         *            The width of the Hud Entity
         * @param height
         *            The height of the Hud Entity
         */
        Control(String imgName, int x, int y, int width, int height) {
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
        Control(int red, int green, int blue) {
            mColor.r = ((float) red) / 256;
            mColor.g = ((float) green) / 256;
            mColor.b = ((float) blue) / 256;
            mIsTouchable = false;
        }

        // TODO: replace these methods with a GestureAction
        
        /**
         * Code to run when the control is tapped
         * 
         * @param x
         *            X Coordinate of the tap
         * @param y
         *            Y Coordinate of the tap
         */
        boolean onTap(Vector3 worldTouchCoord) {
            return false;
        }

        /**
         * Code to run when the control is tapped
         * 
         * @param x
         *            X Coordinate of the tap
         * @param y
         *            Y Coordinate of the tap
         */
        boolean onPan(Vector3 worldTouchCoord) {
            return false;
        }

        /**
         * Run this when a control is down-pressed or up-pressed
         * 
         * @param isUp
         *            True if it is an up-press
         */
        boolean toggle(boolean isUp, Vector3 touchVec) {
            return false;
        }

        /**
         * Action to perform when the toggle occurs
         */
        void toggleAction() {
        }

        /**
         * Run this code when this HUD entity is down-pressed
         * 
         * @param vec
         *            The coordinates of the touch
         */
        @Deprecated
        void onDownPress(Vector3 vec) {
        }

        /**
         * Run this code when this HUD entity is still being pressed, after a
         * down press has already been observed.
         * 
         * @param vec
         *            The coordinates of the touch
         */
        @Deprecated
        void onHold(Vector3 vec) {
        }

        /**
         * Run this code when this HUD entity is released
         */
        @Deprecated
        void onUpPress() {
        }

        /**
         * This is the render method when we've got a valid TR. When we don't,
         * we're displaying text, which probably means we're also dynamically
         * updating the text to display on every render, so it makes sense to
         * overload the render() call for those HUD entities
         * 
         * @param sb
         *            The SpriteBatch to use to draw the image
         */
        void render(SpriteBatch sb) {
            if (mIsActive && mImage != null)
                sb.draw(mImage, mRange.x, mRange.y, 0, 0, mRange.width,
                        mRange.height, 1, 1, 0);
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
    private Controls() {
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
    static void drawTextTransposed(int x, int y, String message, BitmapFont bf,
            SpriteBatch sb) {
        bf.drawMultiLine(sb, message, x, y
                + bf.getMultiLineBounds(message).height);
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
    public static Control addCountdown(float timeout, String text, int x, int y) {
        return addCountdown(timeout, text, x, y,
                Lol.sGame.mConfig.getDefaultFontFace(),
                Lol.sGame.mConfig.getDefaultFontRed(),
                Lol.sGame.mConfig.getDefaultFontGreen(),
                Lol.sGame.mConfig.getDefaultFontBlue(),
                Lol.sGame.mConfig.getDefaultFontSize());
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
    public static Control addCountdown(final float timeout, final String text,
            final int x, final int y, String fontName, final int red,
            final int green, final int blue, int size) {
        Level.sCurrent.mScore.mCountDownRemaining = timeout;
        final BitmapFont bf = Media.getFont(fontName, size);
        Control c = new Control(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                Level.sCurrent.mScore.mCountDownRemaining -= Gdx.graphics
                        .getDeltaTime();
                if (Level.sCurrent.mScore.mCountDownRemaining > 0) {
                    drawTextTransposed(x, y, ""
                            + (int) Level.sCurrent.mScore.mCountDownRemaining,
                            bf, sb);
                } else {
                    PostScene.setDefaultLoseText(text);
                    Level.sCurrent.mScore.endLevel(false);
                }
            }
        };
        Level.sCurrent.mControls.add(c);
        return c;
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
    public static Control addFPS(final int x, final int y, String fontName,
            final int red, final int green, final int blue, int size) {
        final BitmapFont bf = Media.getFont(fontName, size);

        Control c = new Control(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y,
                        "fps: " + Gdx.graphics.getFramesPerSecond(), bf, sb);
            }
        };
        Level.sCurrent.mControls.add(c);
        return c;
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
    public static Control addWinCountdown(float timeout, int x, int y) {
        return addWinCountdown(timeout, x, y,
                Lol.sGame.mConfig.getDefaultFontFace(),
                Lol.sGame.mConfig.getDefaultFontRed(),
                Lol.sGame.mConfig.getDefaultFontGreen(),
                Lol.sGame.mConfig.getDefaultFontBlue(),
                Lol.sGame.mConfig.getDefaultFontSize());
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
    public static Control addWinCountdown(final float timeout, final int x,
            final int y, String fontName, final int red, final int green,
            final int blue, int size) {
        Level.sCurrent.mScore.mWinCountRemaining = timeout;
        final BitmapFont bf = Media.getFont(fontName, size);
        Control c = new Control(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                Level.sCurrent.mScore.mWinCountRemaining -= Gdx.graphics
                        .getDeltaTime();
                if (Level.sCurrent.mScore.mWinCountRemaining > 0)
                    // get elapsed time for this level
                    drawTextTransposed(x, y, ""
                            + (int) Level.sCurrent.mScore.mWinCountRemaining,
                            bf, sb);
                else
                    Level.sCurrent.mScore.endLevel(true);
            }
        };
        Level.sCurrent.mControls.add(c);
        return c;
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
    public static Control addGoodieCount(final int type, int max,
            final String text, final int x, final int y, String fontName,
            final int red, final int green, final int blue, int size) {
        // The suffix to display after the goodie count:
        final String suffix = (max > 0) ? "/" + max + " " + text : " " + text;
        final BitmapFont bf = Media.getFont(fontName, size);
        Control c = new Control(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, ""
                        + Level.sCurrent.mScore.mGoodiesCollected[type - 1]
                        + suffix, bf, sb);
            }
        };
        Level.sCurrent.mControls.add(c);
        return c;
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
    public static Control addDefeatedCount(int max, String text, int x, int y) {
        return addDefeatedCount(max, text, x, y,
                Lol.sGame.mConfig.getDefaultFontFace(),
                Lol.sGame.mConfig.getDefaultFontRed(),
                Lol.sGame.mConfig.getDefaultFontGreen(),
                Lol.sGame.mConfig.getDefaultFontBlue(),
                Lol.sGame.mConfig.getDefaultFontSize());
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
    public static Control addDefeatedCount(int max, final String text,
            final int x, final int y, String fontName, final int red,
            final int green, final int blue, int size) {
        // The suffix to display after the goodie count:
        final String suffix = (max > 0) ? "/" + max + " " + text : " " + text;
        final BitmapFont bf = Media.getFont(fontName, size);
        Control c = new Control(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, ""
                        + Level.sCurrent.mScore.mEnemiesDefeated + suffix, bf,
                        sb);
            }
        };
        Level.sCurrent.mControls.add(c);
        return c;
    }

    /**
     * Add a stopwatch for tracking how long a level takes
     * 
     * @param x
     *            The X coordinate of the bottom left corner (in pixels)
     * @param y
     *            The Y coordinate of the bottom left corner (in pixels)
     */
    static public Control addStopwatch(int x, int y) {
        return addStopwatch(x, y, Lol.sGame.mConfig.getDefaultFontFace(),
                Lol.sGame.mConfig.getDefaultFontRed(),
                Lol.sGame.mConfig.getDefaultFontGreen(),
                Lol.sGame.mConfig.getDefaultFontBlue(),
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
    static public Control addStopwatch(final int x, final int y,
            String fontName, final int red, final int green, final int blue,
            int size) {
        final BitmapFont bf = Media.getFont(fontName, size);
        Control c = new Control(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                Level.sCurrent.mScore.mStopWatchProgress += Gdx.graphics
                        .getDeltaTime();
                drawTextTransposed(x, y, ""
                        + (int) Level.sCurrent.mScore.mStopWatchProgress, bf,
                        sb);
            }
        };
        Level.sCurrent.mControls.add(c);
        return c;
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
    static public Control addStrengthMeter(String text, int x, int y, Hero h) {
        // forward to the more powerful method...
        return addStrengthMeter(text, x, y,
                Lol.sGame.mConfig.getDefaultFontFace(),
                Lol.sGame.mConfig.getDefaultFontRed(),
                Lol.sGame.mConfig.getDefaultFontGreen(),
                Lol.sGame.mConfig.getDefaultFontBlue(),
                Lol.sGame.mConfig.getDefaultFontSize(), h);
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
    static public Control addStrengthMeter(final String text, final int x,
            final int y, String fontName, final int red, final int green,
            final int blue, int size, final Hero h) {
        final BitmapFont bf = Media.getFont(fontName, size);
        Control c = new Control(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, "" + h.getStrength() + " " + text, bf,
                        sb);
            }
        };
        Level.sCurrent.mControls.add(c);
        return c;
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
    static public Control addDistanceMeter(final String text, final int x,
            final int y, String fontName, final int red, final int green,
            final int blue, int size, final Hero h) {
        final BitmapFont bf = Media.getFont(fontName, size);
        Control c = new Control(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                Level.sCurrent.mScore.mDistance = (int) h.getXPosition();
                drawTextTransposed(x, y, "" + Level.sCurrent.mScore.mDistance
                        + " " + text, bf, sb);
            }
        };
        Level.sCurrent.mControls.add(c);
        return c;
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
    public static Control addProjectileCount(final String text, final int x,
            final int y, String fontName, final int red, final int green,
            final int blue, int size) {
        final BitmapFont bf = Media.getFont(fontName, size);
        Control c = new Control(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, ""
                        + Level.sCurrent.mProjectilePool.mProjectilesRemaining
                        + " " + text, bf, sb);
            }
        };
        Level.sCurrent.mControls.add(c);
        return c;
    }

    /**
     * Add a button that pauses the game by causing a PauseScene to be
     * displayed. Note that you must configure a PauseScene, or pressing this
     * button will cause your game to crash.
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
    public static Control addPauseButton(int x, int y, int width, int height,
            String imgName) {
        Control c = new Control(imgName, x, y, width, height) {
            @Override
            boolean onTap(Vector3 vv) {
                PauseScene.show();
                return true;
            }
        };
        Level.sCurrent.mControls.add(c);
        Level.sCurrent.mTapControls.add(c);
        return c;
    }

    /**
     * Add a button that moves an entity
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
     * @param entity
     *            The entity to move downward
     * @param dx
     *            The new X velocity
     * @param dy
     *            The new Y velocity
     */
    public static Control addMoveButton(int x, int y, int width, int height,
            String imgName, final PhysicsSprite entity, final float dx,
            final float dy) {
        Control c = new Control(imgName, x, y, width, height) {
            boolean active = false;
            /**
             * Run this when a control is down-pressed or up-pressed
             * 
             * @param isUp
             *            True if it is an up-press
             */
            // TODO: might want to have some ability to detect when we slide off
            // of a toggle button... see level 72 for explanation of why
            @Override
            boolean toggle(boolean isUp, Vector3 touchVec) {
                if (isUp) {
                    Vector2 v = entity.mBody.getLinearVelocity();
                    if (dx != 0)
                        v.x = 0;
                    if (dy != 0)
                        v.y = 0;
                    entity.updateVelocity(v.x, v.y);
                    active = false;
                }
                else {
                    active = true;
                }
                return true;
            }

            /**
             * Action to perform when the toggle occurs
             */
            @Override
            void toggleAction() {
                if (active) {
                    Vector2 v = entity.mBody.getLinearVelocity();
                    if (dx != 0)
                        v.x = dx;
                    if (dy != 0)
                        v.y = dy;
                    entity.updateVelocity(v.x, v.y);

                }
            }
        };
        Level.sCurrent.mControls.add(c);
        Level.sCurrent.mToggleControls.add(c);
        return c;
    }

    /**
     * Add a button that moves an entity downward
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
     * @param rate
     *            Rate at which the entity moves
     * @param entity
     *            The entity to move downward
     */
    public static Control addDownButton(int x, int y, int width, int height,
            String imgName, float rate, PhysicsSprite entity) {
        return addMoveButton(x, y, width, height, imgName, entity, 0, -rate);
    }

    /**
     * Add a button that moves an entity upward
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
     * @param rate
     *            Rate at which the entity moves
     * @param entity
     *            The entity to move upward
     */
    public static Control addUpButton(int x, int y, int width, int height,
            String imgName, float rate, PhysicsSprite entity) {
        return addMoveButton(x, y, width, height, imgName, entity, 0, rate);
    }

    /**
     * Add a button that moves the given entity left
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
     * @param rate
     *            Rate at which the entity moves
     * @param entity
     *            The entity that should move left when the button is pressed
     */
    public static Control addLeftButton(int x, int y, int width, int height,
            String imgName, float rate, PhysicsSprite entity) {
        return addMoveButton(x, y, width, height, imgName, entity, -rate, 0);
    }

    /**
     * Add a button that moves the given entity to the right
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
     * @param rate
     *            Rate at which the entity moves
     * @param entity
     *            The entity that should move right when the button is pressed
     */
    public static Control addRightButton(int x, int y, int width, int height,
            String imgName, float rate, PhysicsSprite entity) {
        return addMoveButton(x, y, width, height, imgName, entity, rate, 0);
    }

    /**
     * Add a button that moves the given entity at one speed when it is
     * depressed, and at another otherwise
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
     * @param rateDownX
     *            Rate (X) at which the entity moves when the button is pressed
     * @param rateDownY
     *            Rate (Y) at which the entity moves when the button is pressed
     * @param rateUpX
     *            Rate (X) at which the entity moves when the button is not
     *            pressed
     * @param rateUpY
     *            Rate (Y) at which the entity moves when the button is not
     *            pressed
     * @param entity
     *            The entity that the button controls
     */
    public static Control addTurboButton(int x, int y, int width, int height,
            String imgName, final int rateDownX, final int rateDownY,
            final int rateUpX, final int rateUpY, final PhysicsSprite entity) {
        Control c = new Control(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 vv) {
                Vector2 v = entity.mBody.getLinearVelocity();
                v.x = rateDownX;
                v.y = rateDownY;
                entity.updateVelocity(v.x, v.y);
            }

            @Override
            void onHold(Vector3 vv) {
                onDownPress(vv);
            }

            @Override
            void onUpPress() {
                Vector2 v = entity.mBody.getLinearVelocity();
                v.x = rateUpX;
                v.y = rateUpY;
                entity.updateVelocity(v.x, v.y);
            }
        };
        Level.sCurrent.mControls.add(c);
        return c;
    }

    /**
     * Add a button that moves the given entity at one speed, but doesn't stop
     * the entity when the button is released
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
     * @param rateX
     *            Rate (X) at which the entity moves when the button is pressed
     * @param rateY
     *            Rate (Y) at which the entity moves when the button is pressed
     * @param entity
     *            The entity that the button controls
     */
    public static Control addDampenedMotionButton(int x, int y, int width,
            int height, String imgName, final float rateX, final float rateY,
            final float dampening, final PhysicsSprite entity) {
        Control c = new Control(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 vv) {
                Vector2 v = entity.mBody.getLinearVelocity();
                v.x = rateX;
                v.y = rateY;
                entity.mBody.setLinearDamping(0);
                entity.updateVelocity(v.x, v.y);
            }

            @Override
            void onUpPress() {
                entity.mBody.setLinearDamping(dampening);
            }
        };
        Level.sCurrent.mControls.add(c);
        return c;
    }

    /**
     * Add a button that puts the hero into crawl mode when depressed, and
     * regular mode when released
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
     * @param h
     *            The hero to control
     */
    public static Control addCrawlButton(int x, int y, int width, int height,
            String imgName, final Hero h) {
        Control c = new Control(imgName, x, y, width, height) {
            @Override
            boolean toggle(boolean upPress, Vector3 touchVec) {
                if (upPress)
                    h.crawlOff();
                else 
                    h.crawlOn();
                return true;
            }
        };
        Level.sCurrent.mControls.add(c);
        Level.sCurrent.mToggleControls.add(c);
        return c;
    }

    /**
     * Add a button to make the hero jump
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
     * @param h
     *            The hero to control
     */
    public static Control addJumpButton(int x, int y, int width, int height,
            String imgName, final Hero h) {
        Control c = new Control(imgName, x, y, width, height) {
            @Override
            boolean onTap(Vector3 vv) {
                h.jump();
                return true;
            }
        };
        Level.sCurrent.mControls.add(c);
        Level.sCurrent.mTapControls.add(c);
        return c;
    }

    /**
     * Add a button to make the hero throw a projectile
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
     * @param h
     *            The hero who should throw the projectile
     * @param milliDelay
     *            A delay between throws, so that holding doesn't lead to too
     *            many throws at once
     */
    public static Control addThrowButton(int x, int y, int width, int height,
            String imgName, final Hero h, final int milliDelay) {
        Control c = new Control(imgName, x, y, width, height) {
            long mLastThrow;
            boolean throwing = false;

            @Override
            boolean toggle(boolean isUp, Vector3 touchVec) {
                throwing = !isUp;
                mLastThrow = 0;
                return true;
            }

            /**
             * Action to perform when the toggle occurs
             */
            @Override
            void toggleAction() {
                if (throwing) {
                    long now = System.nanoTime();
                    if (mLastThrow + milliDelay * 1000000 < now) {
                        mLastThrow = now;
                        Level.sCurrent.mProjectilePool.throwFixed(h);
                    }
                }
            }
        };
        Level.sCurrent.mControls.add(c);
        Level.sCurrent.mToggleControls.add(c);
        return c;
    }

    /**
     * Add a button to make the hero throw a projectile, but holding doesn't
     * make it throw more often
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
     * @param h
     *            The hero who should throw the projectile
     */
    public static Control addSingleThrowButton(int x, int y, int width,
            int height, String imgName, final Hero h) {
        Control c = new Control(imgName, x, y, width, height) {
            @Override
            boolean onTap(Vector3 vv) {
                Level.sCurrent.mProjectilePool.throwFixed(h);
                return true;
            }
        };
        Level.sCurrent.mControls.add(c);
        Level.sCurrent.mTapControls.add(c);
        return c;
    }

    /**
     * The default behavior for throwing is to throw in a straight line. If we
     * instead desire that the projectiles have some sort of aiming to them, we
     * need to use this method, which throws toward where the screen was pressed
     * Note: you probably want to use an invisible button that covers the
     * screen...
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
     * @param h
     *            The hero who should throw the projectile
     * @param milliDelay
     *            A delay between throws, so that holding doesn't lead to too
     *            many throws at once
     */
    public static Control addVectorThrowButton(int x, int y, int width,
            int height, String imgName, final Hero h, final long milliDelay) {
        Control c = new Control(imgName, x, y, width, height) {
            long mLastThrow;
            Vector3 v = new Vector3();
            boolean active = false;
            
            @Override
            boolean toggle(boolean isUp, Vector3 touchVec) {
                if (isUp) {
                    active = false;
                }
                else {
                    active = true;
                    mLastThrow = 0;
                    v.x = touchVec.x;
                    v.y = touchVec.y;
                    v.z = touchVec.z;
                }
                return true;
            }
            
            @Override
            void toggleAction() {
                if (active) {
                    long now = System.nanoTime();
                    if (mLastThrow + milliDelay * 1000000 < now) {
                        mLastThrow = now;
                        Level.sCurrent.mProjectilePool.throwAt(
                                h.mBody.getPosition().x, h.mBody.getPosition().y,
                                v.x, v.y, h);
                    }
                }
            }
            
            @Override
            boolean onPan(Vector3 touchVec) {
                v.x = touchVec.x;
                v.y = touchVec.y;
                v.z = touchVec.z;
                return true;
            }
        };
        Level.sCurrent.mControls.add(c);
        // on toggle, we start or stop throwing; on pan, we change throw direction
        Level.sCurrent.mToggleControls.add(c);
        Level.sCurrent.mPanControls.add(c);
        return c;
    }

    /**
     * This is almost exactly like addVectorThrowButton. The only difference is
     * that holding won't cause the hero to throw more projectiles
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
     * @param h
     *            The hero who should throw the projectile
     */
    public static Control addVectorSingleThrowButton(int x, int y, int width,
            int height, String imgName, final Hero h) {
        Control c = new Control(imgName, x, y, width, height) {
            @Override
            boolean onTap(Vector3 touchVec) {
                Level.sCurrent.mProjectilePool.throwAt(h.mBody.getPosition().x,
                        h.mBody.getPosition().y, touchVec.x, touchVec.y, h);
                return true;
            }
        };
        Level.sCurrent.mControls.add(c);
        Level.sCurrent.mTapControls.add(c);
        return c;
    }

    /**
     * Display a zoom out button. Note that zooming in and out does not work
     * well with elements that hover on the screen. Use with care.
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
     * @param maxZoom
     *            Maximum zoom. 8 is usually a good default
     */
    public static Control addZoomOutButton(int x, int y, int width, int height,
            String imgName, final float maxZoom) {
        Control c = new Control(imgName, x, y, width, height) {
            @Override
            boolean onTap(Vector3 worldTouchCoord) {
                float curzoom = Level.sCurrent.mGameCam.zoom;
                if (curzoom < maxZoom) {
                    Level.sCurrent.mGameCam.zoom *= 2;
                    Level.sCurrent.mBgCam.zoom *= 2;
                }
                return true;
            }
        };
        Level.sCurrent.mControls.add(c);
        Level.sCurrent.mTapControls.add(c);
        return c;
    }

    /**
     * Display a zoom in button
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
     * @param minZoom
     *            Minimum zoom. 0.25f is usually a good default
     */
    public static Control addZoomInButton(int x, int y, int width, int height,
            String imgName, final float minZoom) {
        Control c = new Control(imgName, x, y, width, height) {
            @Override
            boolean onTap(Vector3 worldTouchCoord) {
                float curzoom = Level.sCurrent.mGameCam.zoom;
                if (curzoom > minZoom) {
                    Level.sCurrent.mGameCam.zoom /= 2;
                    Level.sCurrent.mBgCam.zoom /= 2;
                }
                return true;
            }
        };
        Level.sCurrent.mControls.add(c);
        Level.sCurrent.mTapControls.add(c);
        return c;
    }

    /**
     * Add a button that rotates the hero
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
     * @param rate
     *            Amount of rotation to apply to the hero on each press
     */
    public static Control addRotateButton(int x, int y, int width, int height,
            String imgName, final float rate, final Hero h) {
        Control c = new Control(imgName, x, y, width, height) {
            boolean active = false;
            @Override
            boolean toggle(boolean isUp, Vector3 touchVec) {
                active = !isUp;
                return true;
            }
            @Override
            void toggleAction() {
                if (active)
                    h.increaseRotation(rate);
            }
        };
        Level.sCurrent.mControls.add(c);
        Level.sCurrent.mToggleControls.add(c);
        return c;
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
    public static Control addImage(int x, int y, int width, int height,
            String imgName) {
        Control c = new Control(imgName, x, y, width, height);
        c.mIsTouchable = false;
        Level.sCurrent.mControls.add(c);
        return c;
    }

    /**
     * Add a button to the heads-up display that runs custom code via an
     * onControlPress trigger
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
     * @param id
     *            An id to use for the trigger event
     */
    public static Control addTriggerControl(int x, int y, int width,
            int height, String imgName, final int id) {
        Control c = new Control(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 vv) {
                Lol.sGame.onControlPressTrigger(id, Lol.sGame.mCurrLevelNum);
            }
        };
        Level.sCurrent.mControls.add(c);
        return c;
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
    static public Control addLevelFact(final String key, final int x,
            final int y, String fontName, final int red, final int green,
            final int blue, int size, final String prefix, final String suffix) {
        final BitmapFont bf = Media.getFont(fontName, size);
        Control c = new Control(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, prefix + "" + Facts.getLevelFact(key)
                        + " " + suffix, bf, sb);
            }
        };
        Level.sCurrent.mControls.add(c);
        return c;
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
    static public Control addSessionFact(final String key, final int x,
            final int y, String fontName, final int red, final int green,
            final int blue, int size, final String prefix, final String suffix) {
        final BitmapFont bf = Media.getFont(fontName, size);
        Control c = new Control(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y,
                        prefix + "" + Facts.getSessionFact(key) + " " + suffix,
                        bf, sb);
            }
        };
        Level.sCurrent.mControls.add(c);
        return c;
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
    static public Control addGameFact(final String key, final int x,
            final int y, String fontName, final int red, final int green,
            final int blue, int size, final String prefix, final String suffix) {
        final BitmapFont bf = Media.getFont(fontName, size);
        Control c = new Control(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, prefix + "" + Facts.getGameFact(key)
                        + " " + suffix, bf, sb);
            }
        };
        Level.sCurrent.mControls.add(c);
        return c;
    }
}