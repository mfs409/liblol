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
     * In levels that have a lose-on-timer feature, we store the timer here, so
     * that we can extend the time left to complete a game
     */
    private static float sCountDownRemaining;

    /**
     * This is the same as sCountDownRemaining, but for levels where the hero
     * wins by lasting until time runs out.
     */
    private static float sWinCountRemaining;

    /**
     * This is for handling everything that gets drawn on the HUD, whether it is
     * pressable or not
     */
    static class HudEntity {
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
         * What image should we display, if this HudEntity has an image
         * associated with it?
         */
        TextureRegion mImage;

        /**
         * Use this constructor for controls that provide pressable images
         * 
         * @param imgName The name of the image to display. If "" is given as
         *            the name, it will not crash.
         * @param x The X coordinate (in pixels) of the bottom left corner.
         * @param y The Y coordinate (in pixels) of the bottom left corner.
         * @param width The width of the Hud Entity
         * @param height The height of the Hud Entity
         */
        HudEntity(String imgName, int x, int y, int width, int height) {
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
         * @param red The red portion of text color (0-255)
         * @param green The green portion of text color (0-255)
         * @param blue The blue portion of text color (0-255)
         */
        HudEntity(int red, int green, int blue) {
            mColor.r = ((float)red) / 256;
            mColor.g = ((float)green) / 256;
            mColor.b = ((float)blue) / 256;
            mIsTouchable = false;
        }

        /**
         * Run this code when this HUD entity is down-pressed
         * 
         * @param vec The coordinates of the touch
         */
        void onDownPress(Vector3 vec) {
        }

        /**
         * Run this code when this HUD entity is still being pressed, after a
         * down press has already been observed.
         * 
         * @param vec The coordinates of the touch
         */
        void onHold(Vector3 vec) {
        }

        /**
         * Run this code when this HUD entity is released
         */
        void onUpPress() {
        }

        /**
         * This is the render method when we've got a valid TR. When we don't,
         * we're displaying text, which probably means we're also dynamically
         * updating the text to display on every render, so it makes sense to
         * overload the render() call for those HUD entities
         * 
         * @param sb The SpriteBatch to use to draw the image
         */
        void render(SpriteBatch sb) {
            if (mImage != null)
                sb.draw(mImage, mRange.x, mRange.y, 0, 0, mRange.width, mRange.height, 1, 1, 0);
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
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param message The text to display
     * @param bf The BitmapFont object to use for the text's font
     * @param sb The SpriteBatch used to render the text
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
     * @param timeout Starting value of the timer
     * @param text The text to display when the timer expires
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     */
    public static void addCountdown(float timeout, String text, int x, int y) {
        addCountdown(timeout, text, x, y, LOL._game._config.getDefaultFontFace(),
                LOL._game._config.getDefaultFontRed(), LOL._game._config.getDefaultFontGreen(),
                LOL._game._config.getDefaultFontBlue(), LOL._game._config.getDefaultFontSize());
    }

    /**
     * Add a countdown timer to the screen, with extra features for describing
     * the appearance of the font. When time is up, the level ends in defeat.
     * 
     * @param timeout Starting value of the timer
     * @param text The text to display when the timer expires
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param fontname The name of the font file to use
     * @param red The red portion of text color (0-255)
     * @param green The green portion of text color (0-255)
     * @param blue The blue portion of text color (0-255)
     * @param size The font size to use (20 is usually a good value)
     */
    public static void addCountdown(final float timeout, final String text, final int x,
            final int y, String fontName, final int red, final int green, final int blue, int size) {
        sCountDownRemaining = timeout;
        final BitmapFont bf = Media.getFont(fontName, size);
        Level._currLevel._controls.add(new HudEntity(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                sCountDownRemaining -= Gdx.graphics.getDeltaTime();
                if (sCountDownRemaining > 0) {
                    drawTextTransposed(x, y, "" + (int)sCountDownRemaining, bf, sb);
                } else {
                    PostScene.setDefaultLoseText(text);
                    Level._currLevel._score.endLevel(false);
                }
            }
        });
    }

    /**
     * Change the amount of time left in a countdown timer
     * 
     * @param delta The amount of time to add before the timer expires
     */
    public static void updateTimerExpiration(float delta) {
        sCountDownRemaining += delta;
    }

    /**
     * Print the frames per second
     * 
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param fontname The name of the font file to use
     * @param red The red portion of text color (0-255)
     * @param green The green portion of text color (0-255)
     * @param blue The blue portion of text color (0-255)
     * @param size The font size to use (20 is usually a good value)
     */
    public static void addFPS(final int x, final int y, String fontName, final int red,
            final int green, final int blue, int size) {
        final BitmapFont bf = Media.getFont(fontName, size);

        Level._currLevel._controls.add(new HudEntity(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, "fps: " + Gdx.graphics.getFramesPerSecond(), bf, sb);
            }
        });
    }

    /**
     * Add a countdown timer to the screen. When time is up, the level ends in
     * victory
     * 
     * @param timeout Starting value of the timer
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     */
    public static void addWinCountdown(float timeout, int x, int y) {
        addWinCountdown(timeout, x, y, LOL._game._config.getDefaultFontFace(),
                LOL._game._config.getDefaultFontRed(), LOL._game._config.getDefaultFontGreen(),
                LOL._game._config.getDefaultFontBlue(), LOL._game._config.getDefaultFontSize());
    }

    /**
     * Add a countdown timer to the screen, with extra features for describing
     * the appearance of the font. When time is up, the level ends in victory
     * 
     * @param timeout Starting value of the timer
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param fontname The name of the font file to use
     * @param red The red portion of text color (0-255)
     * @param green The green portion of text color (0-255)
     * @param blue The blue portion of text color (0-255)
     * @param size The font size to use (20 is usually a good value)
     */
    public static void addWinCountdown(final float timeout, final int x, final int y,
            String fontName, final int red, final int green, final int blue, int size) {
        sWinCountRemaining = timeout;
        final BitmapFont bf = Media.getFont(fontName, size);
        Level._currLevel._controls.add(new HudEntity(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                sWinCountRemaining -= Gdx.graphics.getDeltaTime();
                if (sWinCountRemaining > 0)
                    // get elapsed time for this level
                    drawTextTransposed(x, y, "" + (int)sWinCountRemaining, bf, sb);
                else
                    Level._currLevel._score.endLevel(true);
            }
        });
    }

    /**
     * Add a count of the current number of goodies of type 1
     * 
     * @param max If this is > 0, then the message will be of the form XX/max
     *            instead of just XX
     * @param text The text to display after the number of goodies
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     */
    public static void addGoodieCount1(int max, String text, int x, int y) {
        addGoodieCount(1, max, text, x, y, LOL._game._config.getDefaultFontFace(),
                LOL._game._config.getDefaultFontRed(), LOL._game._config.getDefaultFontGreen(),
                LOL._game._config.getDefaultFontBlue(), LOL._game._config.getDefaultFontSize());
    }

    /**
     * Add a count of the current number of goodies of the specified type, with
     * extra features for describing the appearance of the font
     * 
     * @param type The type of goodie to show (1-4)
     * @param max If this is > 0, then the message wil be of the form XX/max
     *            instead of just XX
     * @param text The text to display after the number of goodies
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param fontname The name of the font file to use
     * @param red The red portion of text color (0-255)
     * @param green The green portion of text color (0-255)
     * @param blue The blue portion of text color (0-255)
     * @param size The font size to use (20 is usually a good value)
     */
    public static void addGoodieCount(final int type, int max, final String text, final int x,
            final int y, String fontName, final int red, final int green, final int blue, int size) {
        // The suffix to display after the goodie count:
        final String suffix = (max > 0) ? "/" + max + " " + text : " " + text;
        final BitmapFont bf = Media.getFont(fontName, size);
        HudEntity he = new HudEntity(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, "" + Level._currLevel._score._goodiesCollected[type - 1]
                        + suffix, bf, sb);
            }
        };
        Level._currLevel._controls.add(he);
    }

    /**
     * Add a count of the number of enemies who have been defeated
     * 
     * @param max If this is > 0, then the message will be of the form XX/max
     *            instead of just XX
     * @param text The text to display after the number of goodies
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     */
    public static void addDefeatedCount(int max, String text, int x, int y) {
        addDefeatedCount(max, text, x, y, LOL._game._config.getDefaultFontFace(),
                LOL._game._config.getDefaultFontRed(), LOL._game._config.getDefaultFontGreen(),
                LOL._game._config.getDefaultFontBlue(), LOL._game._config.getDefaultFontSize());
    }

    /**
     * Add a count of the number of enemies who have been defeated, with extra
     * features for describing the appearance of the font
     * 
     * @param max If this is > 0, then the message wil be of the form XX/max
     *            instead of just XX
     * @param text The text to display after the number of goodies
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param fontname The name of the font file to use
     * @param red The red portion of text color (0-255)
     * @param green The green portion of text color (0-255)
     * @param blue The blue portion of text color (0-255)
     * @param size The font size to use (20 is usually a good value)
     */
    public static void addDefeatedCount(int max, final String text, final int x, final int y,
            String fontName, final int red, final int green, final int blue, int size) {
        // The suffix to display after the goodie count:
        final String suffix = (max > 0) ? "/" + max + " " + text : " " + text;
        final BitmapFont bf = Media.getFont(fontName, size);
        HudEntity he = new HudEntity(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, "" + Level._currLevel._score._enemiesDefeated + suffix,
                        bf, sb);
            }
        };
        Level._currLevel._controls.add(he);
    }

    /**
     * Add a stopwatch for tracking how long a level takes
     * 
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     */
    static public void addStopwatch(int x, int y) {
        addStopwatch(x, y, LOL._game._config.getDefaultFontFace(),
                LOL._game._config.getDefaultFontRed(), LOL._game._config.getDefaultFontGreen(),
                LOL._game._config.getDefaultFontBlue(), LOL._game._config.getDefaultFontSize());
    }

    /**
     * Add a stopwatch for tracking how long a level takes, with extra features
     * for describing the appearance of the font
     * 
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param fontname The name of the font file to use
     * @param red The red portion of text color (0-255)
     * @param green The green portion of text color (0-255)
     * @param blue The blue portion of text color (0-255)
     * @param size The font size to use (20 is usually a good value)
     */
    static public void addStopwatch(final int x, final int y, String fontName, final int red,
            final int green, final int blue, int size) {
        final BitmapFont bf = Media.getFont(fontName, size);
        HudEntity he = new HudEntity(red, green, blue) {
            float _stopWatchProgress;

            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                _stopWatchProgress += Gdx.graphics.getDeltaTime();
                drawTextTransposed(x, y, "" + (int)_stopWatchProgress, bf, sb);
            }
        };
        Level._currLevel._controls.add(he);
    }

    /**
     * Display a strength meter for a specific hero
     * 
     * @param text The text to display after the remaining strength value
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param h The Hero whose strength should be displayed
     */
    static public void addStrengthMeter(String text, int x, int y, Hero h) {
        // forward to the more powerful method...
        addStrengthMeter(text, x, y, LOL._game._config.getDefaultFontFace(),
                LOL._game._config.getDefaultFontRed(), LOL._game._config.getDefaultFontGreen(),
                LOL._game._config.getDefaultFontBlue(), LOL._game._config.getDefaultFontSize(), h);
    }

    /**
     * Display a strength meter for a specific hero, with extra features for
     * describing the appearance of the font
     * 
     * @param text The text to display after the remaining _strength value
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param fontname The name of the font file to use
     * @param red The red portion of text color (0-255)
     * @param green The green portion of text color (0-255)
     * @param blue The blue portion of text color (0-255)
     * @param size The font size to use (20 is usually a good value)
     * @param h The Hero whose strength should be displayed
     */
    static public void addStrengthMeter(final String text, final int x, final int y,
            String fontName, final int red, final int green, final int blue, int size, final Hero h) {
        final BitmapFont bf = Media.getFont(fontName, size);
        HudEntity he = new HudEntity(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, "" + h._strength + " " + text, bf, sb);
            }
        };
        Level._currLevel._controls.add(he);
    }

    /**
     * Display the number of remaining projectiles
     * 
     * @param text The text to display after the number of goodies
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param fontname The name of the font file to use
     * @param red The red portion of text color (0-255)
     * @param green The green portion of text color (0-255)
     * @param blue The blue portion of text color (0-255)
     * @param size The font size to use (20 is usually a good value)
     */
    public static void addProjectileCount(final String text, final int x, final int y,
            String fontName, final int red, final int green, final int blue, int size) {
        final BitmapFont bf = Media.getFont(fontName, size);
        HudEntity he = new HudEntity(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, "" + Projectile._projectilesRemaining + " " + text, bf, sb);
            }
        };
        Level._currLevel._controls.add(he);
    }

    /**
     * Add a button that pauses the game by causing a PauseScene to be
     * displayed. Note that you must configure a PauseScene, or pressing this
     * button will cause your game to crash.
     * 
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param width The width of the image
     * @param height The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *            button
     */
    public static void addPauseButton(int x, int y, int width, int height, String imgName) {
        HudEntity he = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 vv) {
                Level._currLevel._pauseScene._visible = true;
            }
        };
        Level._currLevel._controls.add(he);
    }

    /**
     * Add a button that moves an entity
     * 
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param width The width of the image
     * @param height The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *            button
     * @param entity The entity to move downward
     * @param dx The new X velocity
     * @param dy The new Y velocity
     */
    public static void addMoveButton(int x, int y, int width, int height, String imgName,
            final PhysicsSprite entity, final float dx, final float dy) {
        HudEntity he = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 vv) {
                Vector2 v = entity._physBody.getLinearVelocity();
                if (dx != 0)
                    v.x = dx;
                if (dy != 0)
                    v.y = dy;
                entity.updateVelocity(v.x, v.y);
            }

            @Override
            void onHold(Vector3 vv) {
                onDownPress(vv);
            }

            @Override
            void onUpPress() {
                Vector2 v = entity._physBody.getLinearVelocity();
                if (dx != 0)
                    v.x = 0;
                if (dy != 0)
                    v.y = 0;
                entity.updateVelocity(v.x, v.y);
            }
        };
        Level._currLevel._controls.add(he);
    }

    /**
     * Add a button that moves an entity downward
     * 
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param width The width of the image
     * @param height The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *            button
     * @param rate Rate at which the entity moves
     * @param entity The entity to move downward
     */
    public static void addDownButton(int x, int y, int width, int height, String imgName,
            float rate, PhysicsSprite entity) {
        addMoveButton(x, y, width, height, imgName, entity, 0, -rate);
    }

    /**
     * Add a button that moves an entity upward
     * 
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param width The width of the image
     * @param height The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *            button
     * @param rate Rate at which the entity moves
     * @param entity The entity to move upward
     */
    public static void addUpButton(int x, int y, int width, int height, String imgName, float rate,
            PhysicsSprite entity) {
        addMoveButton(x, y, width, height, imgName, entity, 0, rate);
    }

    /**
     * Add a button that moves the given entity left
     * 
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param width The width of the image
     * @param height The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *            button
     * @param rate Rate at which the entity moves
     * @param entity The entity that should move left when the button is pressed
     */
    public static void addLeftButton(int x, int y, int width, int height, String imgName,
            float rate, PhysicsSprite entity) {
        addMoveButton(x, y, width, height, imgName, entity, -rate, 0);
    }

    /**
     * Add a button that moves the given entity to the right
     * 
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param width The width of the image
     * @param height The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *            button
     * @param rate Rate at which the entity moves
     * @param entity The entity that should move right when the button is
     *            pressed
     */
    public static void addRightButton(int x, int y, int width, int height, String imgName,
            float rate, PhysicsSprite entity) {
        addMoveButton(x, y, width, height, imgName, entity, rate, 0);
    }

    /**
     * Add a button that moves the given entity at one speed when it is
     * depressed, and at another otherwise
     * 
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param width The width of the image
     * @param height The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *            button
     * @param rateDownX Rate (X) at which the entity moves when the button is
     *            pressed
     * @param rateDownY Rate (Y) at which the entity moves when the button is
     *            pressed
     * @param rateUpX Rate (X) at which the entity moves when the button is not
     *            pressed
     * @param rateUpY Rate (Y) at which the entity moves when the button is not
     *            pressed
     * @param entity The entity that the button controls
     */
    public static void addTurboButton(int x, int y, int width, int height, String imgName,
            final int rateDownX, final int rateDownY, final int rateUpX, final int rateUpY,
            final PhysicsSprite entity) {
        HudEntity he = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 vv) {
                Vector2 v = entity._physBody.getLinearVelocity();
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
                Vector2 v = entity._physBody.getLinearVelocity();
                v.x = rateUpX;
                v.y = rateUpY;
                entity.updateVelocity(v.x, v.y);
            }
        };
        Level._currLevel._controls.add(he);
    }

    /**
     * Add a button that moves the given entity at one speed, but doesn't stop
     * the entity when the button is released
     * 
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param width The width of the image
     * @param height The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *            button
     * @param rateX Rate (X) at which the entity moves when the button is
     *            pressed
     * @param rateY Rate (Y) at which the entity moves when the button is
     *            pressed
     * @param entity The entity that the button controls
     */
    public static void addDampenedMotionButton(int x, int y, int width, int height, String imgName,
            final float rateX, final float rateY, final float dampening, final PhysicsSprite entity) {
        HudEntity he = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 vv) {
                Vector2 v = entity._physBody.getLinearVelocity();
                v.x = rateX;
                v.y = rateY;
                entity._physBody.setLinearDamping(0);
                entity.updateVelocity(v.x, v.y);
            }

            @Override
            void onUpPress() {
                entity._physBody.setLinearDamping(dampening);
            }
        };
        Level._currLevel._controls.add(he);
    }

    /**
     * Add a button that puts the hero into crawl mode when depressed, and
     * regular mode when released
     * 
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param width The width of the image
     * @param height The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *            button
     * @param h The hero to control
     */
    public static void addCrawlButton(int x, int y, int width, int height, String imgName,
            final Hero h) {
        HudEntity he = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 vv) {
                h.crawlOn();
            }

            @Override
            void onUpPress() {
                h.crawlOff();
            }
        };
        Level._currLevel._controls.add(he);
    }

    /**
     * Add a button to make the hero jump
     * 
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param width The width of the image
     * @param height The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *            button
     * @param h The hero to control
     */
    public static void addJumpButton(int x, int y, int width, int height, String imgName,
            final Hero h) {
        HudEntity he = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 vv) {
                h.jump();
            }
        };
        Level._currLevel._controls.add(he);
    }

    /**
     * Add a button to make the hero throw a projectile
     * 
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param width The width of the image
     * @param height The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *            button
     * @param h The hero who should throw the projectile
     * @param milliDelay A delay between throws, so that holding doesn't lead to
     *            too many throws at once
     */
    public static void addThrowButton(int x, int y, int width, int height, String imgName,
            final Hero h, final int milliDelay) {
        HudEntity he = new HudEntity(imgName, x, y, width, height) {
            long mLastThrow;

            @Override
            void onDownPress(Vector3 vv) {
                Projectile.throwFixed(h._physBody.getPosition().x, h._physBody.getPosition().y, h);
                mLastThrow = System.nanoTime();
            }

            @Override
            void onHold(Vector3 vv) {
                long now = System.nanoTime();
                if (mLastThrow + milliDelay * 1000000 < now) {
                    mLastThrow = now;
                    Projectile.throwFixed(h._physBody.getPosition().x, h._physBody.getPosition().y,
                            h);
                }
            }
        };
        Level._currLevel._controls.add(he);
    }

    /**
     * Add a button to make the hero throw a projectile, but holding doesn't
     * make it throw more often
     * 
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param width The width of the image
     * @param height The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *            button
     * @param h The hero who should throw the projectile
     */
    public static void addSingleThrowButton(int x, int y, int width, int height, String imgName,
            final Hero h) {
        HudEntity he = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 vv) {
                Projectile.throwFixed(h._physBody.getPosition().x, h._physBody.getPosition().y, h);
            }

            @Override
            void onUpPress() {
            }
        };
        Level._currLevel._controls.add(he);
    }

    /**
     * The default behavior for throwing is to throw in a straight line. If we
     * instead desire that the projectiles have some sort of aiming to them, we
     * need to use this method, which throws toward where the screen was pressed
     * Note: you probably want to use an invisible button that covers the
     * screen...
     * 
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param width The width of the image
     * @param height The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *            button
     * @param h The hero who should throw the projectile
     * @param milliDelay A delay between throws, so that holding doesn't lead to
     *            too many throws at once
     */
    public static void addVectorThrowButton(int x, int y, int width, int height, String imgName,
            final Hero h, final long milliDelay) {
        HudEntity he = new HudEntity(imgName, x, y, width, height) {
            long mLastThrow;

            @Override
            void onDownPress(Vector3 vv) {
                Projectile.throwAt(h._physBody.getPosition().x, h._physBody.getPosition().y, vv.x,
                        vv.y, h);
                mLastThrow = System.nanoTime();
            }

            @Override
            void onHold(Vector3 vv) {
                long now = System.nanoTime();
                if (mLastThrow + milliDelay * 1000000 < now) {
                    mLastThrow = now;
                    Projectile.throwAt(h._physBody.getPosition().x, h._physBody.getPosition().y,
                            vv.x, vv.y, h);
                }
            }

        };
        Level._currLevel._controls.add(he);
    }

    /**
     * This is almost exactly like addVectorThrowButton. The only difference is
     * that holding won't cause the hero to throw more projectiles
     * 
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param width The width of the image
     * @param height The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *            button
     * @param h The hero who should throw the projectile
     */
    public static void addVectorSingleThrowButton(int x, int y, int width, int height,
            String imgName, final Hero h) {
        HudEntity he = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 vv) {
                Projectile.throwAt(h._physBody.getPosition().x, h._physBody.getPosition().y, vv.x,
                        vv.y, h);
            }

            @Override
            void onUpPress() {
            }
        };
        Level._currLevel._controls.add(he);
    }

    /**
     * Display a zoom out button. Note that zooming in and out does not work
     * well with elements that hover on the screen. Use with care.
     * 
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param width The width of the image
     * @param height The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *            button
     * @param maxZoom Maximum zoom. 8 is usually a good default
     */
    public static void addZoomOutButton(int x, int y, int width, int height, String imgName,
            final float maxZoom) {
        HudEntity he = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 v) {
                float curzoom = Level._currLevel._gameCam.zoom;
                if (curzoom < maxZoom) {
                    Level._currLevel._gameCam.zoom *= 2;
                    Level._currLevel._bgCam.zoom *= 2;
                }
            }
        };
        Level._currLevel._controls.add(he);
    }

    /**
     * Display a zoom in button
     * 
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param width The width of the image
     * @param height The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *            button
     * @param minZoom Minimum zoom. 0.25f is usually a good default
     */
    public static void addZoomInButton(int x, int y, int width, int height, String imgName,
            final float minZoom) {
        HudEntity he = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 v) {
                float curzoom = Level._currLevel._gameCam.zoom;
                if (curzoom > minZoom) {
                    Level._currLevel._gameCam.zoom /= 2;
                    Level._currLevel._bgCam.zoom /= 2;
                }
            }
        };
        Level._currLevel._controls.add(he);
    }

    /**
     * Add a button that rotates the hero
     * 
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param width The width of the image
     * @param height The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *            button
     * @param rate Amount of rotation to apply to the hero on each press
     */
    public static void addRotateButton(int x, int y, int width, int height, String imgName,
            final float rate, final Hero h) {
        HudEntity he = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 vv) {
                h.increaseRotation(rate);
            }

            @Override
            void onHold(Vector3 vv) {
                h.increaseRotation(rate);
            }
        };
        Level._currLevel._controls.add(he);
    }

    /**
     * Add an image to the heads-up display. Touching the image has no effect
     * 
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param width The width of the image
     * @param height The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *            button
     */
    public static void addImage(int x, int y, int width, int height, String imgName) {
        HudEntity he = new HudEntity(imgName, x, y, width, height);
        he.mIsTouchable = false;
        Level._currLevel._controls.add(he);
    }

    /**
     * Add a button to the heads-up display that runs custom code via an
     * onControlPress trigger
     * 
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     * @param width The width of the image
     * @param height The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *            button
     * @param id An id to use for the trigger event
     */
    public static void addTriggerControl(int x, int y, int width, int height, String imgName,
            final int id) {
        HudEntity he = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 vv) {
                LOL._game.onControlPressTrigger(id, LOL._game._currLevel);
            }
        };
        Level._currLevel._controls.add(he);
    }
}
