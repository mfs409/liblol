/**
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

/**
 * LOL Games have a heads-up display (hud). The hud is a place for displaying
 * text and drawing touchable buttons, so that as the hero moves through the
 * level, the buttons and text can remain at the same place on the screen. This
 * class encapsulates all of the displayable text.
 */
public class Display {
    /**
     * What color should we use to draw text
     */
    Color mColor = new Color(0, 0, 0, 1);

    /**
     * The font object to use
     */
    BitmapFont mFont;

    /**
     * The constructor keeps track of the text color, but that's it.
     *
     * @param red      The red portion of text color (0-255)
     * @param green    The green portion of text color (0-255)
     * @param blue     The blue portion of text color (0-255)
     * @param fontName The name of the .ttf font file to use
     * @param fontSize The point size of the font
     */
    Display(int red, int green, int blue, String fontName, int fontSize) {
        mColor.r = ((float) red) / 256;
        mColor.g = ((float) green) / 256;
        mColor.b = ((float) blue) / 256;
        mFont = Media.getFont(fontName, fontSize);
    }

    /**
     * A helper method to draw text nicely. In GDX, we draw everything by giving
     * the bottom left corner, except text, which takes the top left corner.
     * This function handles the conversion, so that we can use bottom-left.
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param message The text to display
     * @param bf      The BitmapFont object to use for the text's font
     * @param sb      The SpriteBatch used to render the text
     */
    static void drawTextTransposed(int x, int y, String message, BitmapFont bf, SpriteBatch sb) {
        bf.drawMultiLine(sb, message, x, y + bf.getMultiLineBounds(message).height);
    }

    /**
     * Add a countdown timer to the screen. When time is up, the level ends in
     * defeat
     *
     * @param timeout Starting value of the timer
     * @param text    The text to display when the timer expires
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     */
    public static Display addCountdown(float timeout, String text, int x, int y) {
        return addCountdown(timeout, text, x, y, Lol.sGame.mDefaultFontFace, Lol.sGame.mDefaultFontRed,
                Lol.sGame.mDefaultFontGreen, Lol.sGame.mDefaultFontBlue, Lol.sGame.mDefaultFontSize);
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Add a countdown timer to the screen, with extra features for describing
     * the appearance of the font. When time is up, the level ends in defeat.
     *
     * @param timeout  Starting value of the timer
     * @param text     The text to display when the timer expires
     * @param x        The X coordinate of the bottom left corner (in pixels)
     * @param y        The Y coordinate of the bottom left corner (in pixels)
     * @param fontName The name of the font file to use
     * @param red      The red portion of text color (0-255)
     * @param green    The green portion of text color (0-255)
     * @param blue     The blue portion of text color (0-255)
     * @param size     The font size to use (20 is usually a good value)
     */
    public static Display addCountdown(final float timeout, final String text, final int x, final int y,
                                       String fontName, final int red, final int green, final int blue, int size) {
        Lol.sGame.mCurrentLevel.mScore.mCountDownRemaining = timeout;
        Display d = new Display(red, green, blue, fontName, size) {
            @Override
            void render(SpriteBatch sb) {
                mFont.setColor(mColor.r, mColor.g, mColor.b, 1);
                Lol.sGame.mCurrentLevel.mScore.mCountDownRemaining -= Gdx.graphics.getDeltaTime();
                if (Lol.sGame.mCurrentLevel.mScore.mCountDownRemaining > 0) {
                    drawTextTransposed(x, y, "" + (int) Lol.sGame.mCurrentLevel.mScore.mCountDownRemaining, mFont, sb);
                } else {
                    LoseScene.get().setDefaultText(text);
                    Lol.sGame.mCurrentLevel.mScore.endLevel(false);
                }
            }
        };
        Lol.sGame.mCurrentLevel.mDisplays.add(d);
        return d;
    }

    /**
     * Print the frames per second. We use this in debug mode, but some people
     * like to show it all the time.
     *
     * @param x        The X coordinate of the bottom left corner (in pixels)
     * @param y        The Y coordinate of the bottom left corner (in pixels)
     * @param fontName The name of the font file to use
     * @param red      The red portion of text color (0-255)
     * @param green    The green portion of text color (0-255)
     * @param blue     The blue portion of text color (0-255)
     * @param size     The font size to use (20 is usually a good value)
     */
    public static Display addFPS(final int x, final int y, String fontName, final int red, final int green,
                                 final int blue, int size) {
        Display d = new Display(red, green, blue, fontName, size) {
            @Override
            void render(SpriteBatch sb) {
                mFont.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, "fps: " + Gdx.graphics.getFramesPerSecond(), mFont, sb);
            }
        };
        Lol.sGame.mCurrentLevel.mDisplays.add(d);
        return d;
    }

    /**
     * Add a countdown timer to the screen. When time is up, the level ends in
     * victory
     *
     * @param timeout Starting value of the timer
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     */
    public static Display addWinCountdown(float timeout, int x, int y) {
        return addWinCountdown(timeout, x, y, Lol.sGame.mDefaultFontFace, Lol.sGame.mDefaultFontRed,
                Lol.sGame.mDefaultFontGreen, Lol.sGame.mDefaultFontBlue, Lol.sGame.mDefaultFontSize);
    }

    /**
     * Add a countdown timer to the screen, with extra features for describing
     * the appearance of the font. When time is up, the level ends in victory
     *
     * @param timeout  Starting value of the timer
     * @param x        The X coordinate of the bottom left corner (in pixels)
     * @param y        The Y coordinate of the bottom left corner (in pixels)
     * @param fontName The name of the font file to use
     * @param red      The red portion of text color (0-255)
     * @param green    The green portion of text color (0-255)
     * @param blue     The blue portion of text color (0-255)
     * @param size     The font size to use (20 is usually a good value)
     */
    public static Display addWinCountdown(final float timeout, final int x, final int y, String fontName,
                                          final int red, final int green, final int blue, int size) {
        Lol.sGame.mCurrentLevel.mScore.mWinCountRemaining = timeout;
        Display d = new Display(red, green, blue, fontName, size) {
            @Override
            void render(SpriteBatch sb) {
                mFont.setColor(mColor.r, mColor.g, mColor.b, 1);
                Lol.sGame.mCurrentLevel.mScore.mWinCountRemaining -= Gdx.graphics.getDeltaTime();
                if (Lol.sGame.mCurrentLevel.mScore.mWinCountRemaining > 0)
                    // get elapsed time for this level
                    drawTextTransposed(x, y, "" + (int) Lol.sGame.mCurrentLevel.mScore.mWinCountRemaining, mFont, sb);
                else
                    Lol.sGame.mCurrentLevel.mScore.endLevel(true);
            }
        };
        Lol.sGame.mCurrentLevel.mDisplays.add(d);
        return d;
    }

    /**
     * Add a count of the current number of goodies of the specified type, with
     * extra features for describing the appearance of the font
     *
     * @param type     The type of goodie to show (1-4)
     * @param max      If this is &gt; 0, then the message wil be of the form XX/max
     *                 instead of just XX
     * @param text     The text to display after the number of goodies
     * @param x        The X coordinate of the bottom left corner (in pixels)
     * @param y        The Y coordinate of the bottom left corner (in pixels)
     * @param fontName The name of the font file to use
     * @param red      The red portion of text color (0-255)
     * @param green    The green portion of text color (0-255)
     * @param blue     The blue portion of text color (0-255)
     * @param size     The font size to use (20 is usually a good value)
     */
    public static Display addGoodieCount(final int type, int max, final String text, final int x, final int y,
                                         String fontName, final int red, final int green, final int blue, int size) {
        // The suffix to display after the goodie count:
        final String suffix = (max > 0) ? "/" + max + text : text;
        Display d = new Display(red, green, blue, fontName, size) {
            @Override
            void render(SpriteBatch sb) {
                mFont.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, "" + Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[type - 1] + suffix,
                        mFont, sb);
            }
        };
        Lol.sGame.mCurrentLevel.mDisplays.add(d);
        return d;
    }

    /**
     * Add a count of the number of enemies who have been defeated
     *
     * @param max  If this is &gt; 0, then the message will be of the form XX/max
     *             instead of just XX
     * @param text The text to display after the number of goodies
     * @param x    The X coordinate of the bottom left corner (in pixels)
     * @param y    The Y coordinate of the bottom left corner (in pixels)
     */
    public static Display addDefeatedCount(int max, String text, int x, int y) {
        return addDefeatedCount(max, text, x, y, Lol.sGame.mDefaultFontFace, Lol.sGame.mDefaultFontRed,
                Lol.sGame.mDefaultFontGreen, Lol.sGame.mDefaultFontBlue, Lol.sGame.mDefaultFontSize);
    }

    /**
     * Add a count of the number of enemies who have been defeated, with extra
     * features for describing the appearance of the font
     *
     * @param max      If this is &gt; 0, then the message wil be of the form XX/max
     *                 instead of just XX
     * @param text     The text to display after the number of goodies
     * @param x        The X coordinate of the bottom left corner (in pixels)
     * @param y        The Y coordinate of the bottom left corner (in pixels)
     * @param fontName The name of the font file to use
     * @param red      The red portion of text color (0-255)
     * @param green    The green portion of text color (0-255)
     * @param blue     The blue portion of text color (0-255)
     * @param size     The font size to use (20 is usually a good value)
     */
    public static Display addDefeatedCount(int max, final String text, final int x, final int y, String fontName,
                                           final int red, final int green, final int blue, int size) {
        // The suffix to display after the goodie count:
        final String suffix = (max > 0) ? "/" + max + text : text;
        Display d = new Display(red, green, blue, fontName, size) {
            @Override
            void render(SpriteBatch sb) {
                mFont.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, "" + Lol.sGame.mCurrentLevel.mScore.mEnemiesDefeated + suffix, mFont, sb);
            }
        };
        Lol.sGame.mCurrentLevel.mDisplays.add(d);
        return d;
    }

    /**
     * Add a stopwatch for tracking how long a level takes
     *
     * @param x The X coordinate of the bottom left corner (in pixels)
     * @param y The Y coordinate of the bottom left corner (in pixels)
     */
    static public Display addStopwatch(int x, int y) {
        return addStopwatch(x, y, Lol.sGame.mDefaultFontFace, Lol.sGame.mDefaultFontRed, Lol.sGame.mDefaultFontGreen,
                Lol.sGame.mDefaultFontBlue, Lol.sGame.mDefaultFontSize);
    }

    /**
     * Add a stopwatch for tracking how long a level takes, with extra features
     * for describing the appearance of the font
     *
     * @param x        The X coordinate of the bottom left corner (in pixels)
     * @param y        The Y coordinate of the bottom left corner (in pixels)
     * @param fontName The name of the font file to use
     * @param red      The red portion of text color (0-255)
     * @param green    The green portion of text color (0-255)
     * @param blue     The blue portion of text color (0-255)
     * @param size     The font size to use (20 is usually a good value)
     */
    static public Display addStopwatch(final int x, final int y, String fontName, final int red, final int green,
                                       final int blue, int size) {
        Display d = new Display(red, green, blue, fontName, size) {
            @Override
            void render(SpriteBatch sb) {
                mFont.setColor(mColor.r, mColor.g, mColor.b, 1);
                Lol.sGame.mCurrentLevel.mScore.mStopWatchProgress += Gdx.graphics.getDeltaTime();
                drawTextTransposed(x, y, "" + (int) Lol.sGame.mCurrentLevel.mScore.mStopWatchProgress, mFont, sb);
            }
        };
        Lol.sGame.mCurrentLevel.mDisplays.add(d);
        return d;
    }

    /**
     * Display a strength meter for a specific hero
     *
     * @param text The text to display after the remaining strength value
     * @param x    The X coordinate of the bottom left corner (in pixels)
     * @param y    The Y coordinate of the bottom left corner (in pixels)
     * @param h    The Hero whose strength should be displayed
     */
    static public Display addStrengthMeter(String text, int x, int y, Hero h) {
        // forward to the more powerful method...
        return addStrengthMeter(text, x, y, Lol.sGame.mDefaultFontFace, Lol.sGame.mDefaultFontRed,
                Lol.sGame.mDefaultFontGreen, Lol.sGame.mDefaultFontBlue, Lol.sGame.mDefaultFontSize, h);
    }

    /**
     * Display a strength meter for a specific hero, with extra features for
     * describing the appearance of the font
     *
     * @param text     The text to display after the remaining strength value
     * @param x        The X coordinate of the bottom left corner (in pixels)
     * @param y        The Y coordinate of the bottom left corner (in pixels)
     * @param fontName The name of the font file to use
     * @param red      The red portion of text color (0-255)
     * @param green    The green portion of text color (0-255)
     * @param blue     The blue portion of text color (0-255)
     * @param size     The font size to use (20 is usually a good value)
     * @param h        The Hero whose strength should be displayed
     */
    static public Display addStrengthMeter(final String text, final int x, final int y, String fontName, final int red,
                                           final int green, final int blue, int size, final Hero h) {
        Display d = new Display(red, green, blue, fontName, size) {
            @Override
            void render(SpriteBatch sb) {
                mFont.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, "" + h.getStrength() + text, mFont, sb);
            }
        };
        Lol.sGame.mCurrentLevel.mDisplays.add(d);
        return d;
    }

    /**
     * Display a meter showing how far an actor has traveled
     *
     * @param text     The text to display after the remaining strength value
     * @param x        The X coordinate of the bottom left corner (in pixels)
     * @param y        The Y coordinate of the bottom left corner (in pixels)
     * @param fontName The name of the font file to use
     * @param red      The red portion of text color (0-255)
     * @param green    The green portion of text color (0-255)
     * @param blue     The blue portion of text color (0-255)
     * @param size     The font size to use (20 is usually a good value)
     * @param actor    The Actor whose distance should be displayed
     */
    static public Display addDistanceMeter(final String text, final int x, final int y, String fontName, final int red,
                                           final int green, final int blue, int size, final Actor actor) {
        Display d = new Display(red, green, blue, fontName, size) {
            @Override
            void render(SpriteBatch sb) {
                mFont.setColor(mColor.r, mColor.g, mColor.b, 1);
                Lol.sGame.mCurrentLevel.mScore.mDistance = (int) actor.getXPosition();
                drawTextTransposed(x, y, "" + Lol.sGame.mCurrentLevel.mScore.mDistance + text, mFont, sb);
            }
        };
        Lol.sGame.mCurrentLevel.mDisplays.add(d);
        return d;
    }

    /**
     * Display the number of remaining projectiles
     *
     * @param text     The text to display after the number of goodies
     * @param x        The X coordinate of the bottom left corner (in pixels)
     * @param y        The Y coordinate of the bottom left corner (in pixels)
     * @param fontName The name of the font file to use
     * @param red      The red portion of text color (0-255)
     * @param green    The green portion of text color (0-255)
     * @param blue     The blue portion of text color (0-255)
     * @param size     The font size to use (20 is usually a good value)
     */
    public static Display addProjectileCount(final String text, final int x, final int y, String fontName,
                                             final int red, final int green, final int blue, int size) {
        Display d = new Display(red, green, blue, fontName, size) {
            @Override
            void render(SpriteBatch sb) {
                mFont.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, "" + Lol.sGame.mCurrentLevel.mProjectilePool.mProjectilesRemaining + text,
                        mFont, sb);
            }
        };
        Lol.sGame.mCurrentLevel.mDisplays.add(d);
        return d;
    }

    /**
     * Display text corresponding to a fact saved for the current level
     *
     * @param key      The key to look up in the Fact store
     * @param x        The X coordinate of the bottom left corner (in pixels)
     * @param y        The Y coordinate of the bottom left corner (in pixels)
     * @param fontName The name of the font file to use
     * @param red      The red portion of text color (0-255)
     * @param green    The green portion of text color (0-255)
     * @param blue     The blue portion of text color (0-255)
     * @param size     The font size to use (20 is usually a good value)
     * @param prefix   Text to put before the fact
     * @param suffix   Text to put after the fact
     */
    static public Display addLevelFact(final String key, final int x, final int y, String fontName, final int red,
                                       final int green, final int blue, int size, final String prefix, final String suffix) {
        Display d = new Display(red, green, blue, fontName, size) {
            @Override
            void render(SpriteBatch sb) {
                mFont.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, prefix + "" + Facts.getLevelFact(key, -1) + suffix, mFont, sb);
            }
        };
        Lol.sGame.mCurrentLevel.mDisplays.add(d);
        return d;
    }

    /**
     * Display text corresponding to a fact saved for the current game session
     *
     * @param key      The key to look up in the Fact store
     * @param x        The X coordinate of the bottom left corner (in pixels)
     * @param y        The Y coordinate of the bottom left corner (in pixels)
     * @param fontName The name of the font file to use
     * @param red      The red portion of text color (0-255)
     * @param green    The green portion of text color (0-255)
     * @param blue     The blue portion of text color (0-255)
     * @param size     The font size to use (20 is usually a good value)
     * @param prefix   Text to put before the fact
     * @param suffix   Text to put after the fact
     */
    static public Display addSessionFact(final String key, final int x, final int y, String fontName, final int red,
                                         final int green, final int blue, int size, final String prefix, final String suffix) {
        Display d = new Display(red, green, blue, fontName, size) {
            @Override
            void render(SpriteBatch sb) {
                mFont.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, prefix + "" + Facts.getSessionFact(key, -1) + suffix, mFont, sb);
            }
        };
        Lol.sGame.mCurrentLevel.mDisplays.add(d);
        return d;
    }

    /**
     * Display text corresponding to a fact saved for the game
     *
     * @param key      The key to look up in the Fact store
     * @param x        The X coordinate of the bottom left corner (in pixels)
     * @param y        The Y coordinate of the bottom left corner (in pixels)
     * @param fontName The name of the font file to use
     * @param red      The red portion of text color (0-255)
     * @param green    The green portion of text color (0-255)
     * @param blue     The blue portion of text color (0-255)
     * @param size     The font size to use (20 is usually a good value)
     * @param prefix   Text to put before the fact
     * @param suffix   Text to put after the fact
     */
    static public Display addGameFact(final String key, final int x, final int y, String fontName, final int red,
                                      final int green, final int blue, int size, final String prefix, final String suffix) {
        Display d = new Display(red, green, blue, fontName, size) {
            @Override
            void render(SpriteBatch sb) {
                mFont.setColor(mColor.r, mColor.g, mColor.b, 1);
                drawTextTransposed(x, y, prefix + "" + Facts.getGameFact(key, -1) + suffix, mFont, sb);
            }
        };
        Lol.sGame.mCurrentLevel.mDisplays.add(d);
        return d;
    }

    /**
     * Render the text. Since each control needs to get its text at the time it
     * is rendered, we don't provide a default implementation.
     *
     * @param sb The SpriteBatch to use to draw the image
     */
    void render(SpriteBatch sb) {
    }
}
