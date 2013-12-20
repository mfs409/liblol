
package edu.lehigh.cse.lol;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Controls {
    /**
     * This is for handling everything that gets drawn presses to the buttons
     * that are drawn on the hudCam
     */
    static class HudEntity {
        /**
         * Use this constructor for controls that provide pressable images
         * 
         * @param imgName The name of the image to display. If "" is given as
         *            the name, it will not crash.
         */
        HudEntity(String imgName, int x, int y, int width, int height) {
            // set up the image to display
            //
            // NB: this will fail gracefully (no crash) for invalid file names
            TextureRegion[] trs = Media.getImage(imgName);
            if (trs != null)
                _tr = trs[0];

            // set up the touchable range for the image
            _range = new Rectangle(x, y, width, height);
        }

        Color _c = new Color(0, 0, 0, 1);

        /**
         * Use this constructor for controls that are simply for displaying text
         * 
         * @param red red portion of text color
         * @param green green portion of text color
         * @param blue blue portion of text color
         */
        HudEntity(int red, int green, int blue) {
            _c.r = ((float)red) / 256;
            _c.g = ((float)green) / 256;
            _c.b = ((float)blue) / 256;
        }

        void onDownPress(Vector3 vec) {
        }

        void onHold(Vector3 vec) {
        }

        void onUpPress() {
        }

        Rectangle _range;

        TextureRegion _tr;

        /**
         * This is the render method when we've got a valid TR. When we don't,
         * we're displaying text, which probably means we're also dynamically
         * updating the text to display on every render, so it makes sense to
         * overload the render() call for those methods
         * 
         * @param sb
         */
        void render(SpriteBatch sb) {
            if (_tr != null)
                sb.draw(_tr, _range.x, _range.y, 0, 0, _range.width, _range.height, 1, 1, 0);
        }
    }

    /**
     * Store the duration between when the program started and when the _current
     * level started, so that we can reuse the timer from one level to the next
     */
    private static float _countDownRemaining;

    /**
     * Store the amount of stopwatch that has transpired
     */
    private static float _stopWatchProgress;

    private static float _winCountRemaining;

    /**
     * Controls is a pure static class, and should never be constructed
     * explicitly
     */
    private Controls() {
    }

    /*
     * TEXT-ONLY CONTROLS
     */

    /**
     * Change the amount of time left in a countdown
     * 
     * @param delta The amount of time to add before the timer expires
     */
    public static void updateTimerExpiration(float delta) {
        _countDownRemaining += delta;
    }

    static void drawTextTransposed(int x, int y, String message, BitmapFont bf, SpriteBatch sb) {
        bf.drawMultiLine(sb, message, x, y + bf.getMultiLineBounds(message).height);
    }

    /**
     * Add a countdown timer to the screen. When time is up, the level ends in
     * defeat
     * 
     * @param timeout Starting value of the timer
     * @param text The text to display when the timer expires
     * @param x The x coordinate where the timer should be drawn
     * @param y The y coordinate where the timer should be drawn
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
     * @param x The x coordinate where the timer should be drawn
     * @param y The y coordinate where the timer should be drawn
     * @param red A value between 0 and 255, indicating the red portion of the
     *            font color
     * @param green A value between 0 and 255, indicating the green portion of
     *            the font color
     * @param blue A value between 0 and 255, indicating the blue portion of the
     *            font color
     * @param size The font size, typically 32 but can be varied depending on
     *            the amount of text being drawn to the screen
     */
    public static void addCountdown(final float timeout, final String text, final int x,
            final int y, String fontName, final int red, final int green, final int blue, int size) {
        _countDownRemaining = timeout;
        final BitmapFont bf = Media.getFont(fontName, size);
        Level._currLevel._controls.add(new HudEntity(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(_c.r, _c.g, _c.b, 1);
                _countDownRemaining -= Gdx.graphics.getDeltaTime();
                if (_countDownRemaining > 0) {
                    drawTextTransposed(x, y, "" + (int)_countDownRemaining, bf, sb);
                } else {
                    PostScene.setDefaultLoseText(text);
                    Level._currLevel._score.endLevel(false);
                }
            }
        });
    }

    /**
     * Print the frames per second
     * 
     * @param x
     * @param y
     * @param red
     * @param green
     * @param blue
     * @param size
     */
    public static void addFPS(final int x, final int y, String fontName, final int red,
            final int green, final int blue, int size) {
        final BitmapFont bf = Media.getFont(fontName, size);

        Level._currLevel._controls.add(new HudEntity(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(_c.r, _c.g, _c.b, 1);
                drawTextTransposed(x, y, "fps: " + Gdx.graphics.getFramesPerSecond(), bf, sb);
            }
        });
    }

    /**
     * Add a countdown timer to the screen. When time is up, the level ends in
     * victory
     * 
     * @param timeout Starting value of the timer
     * @param x The x coordinate where the timer should be drawn
     * @param y The y coordinate where the timer should be drawn
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
     * @param x The x coordinate where the timer should be drawn
     * @param y The y coordinate where the timer should be drawn
     * @param red A value between 0 and 255, indicating the red portion of the
     *            font color
     * @param green A value between 0 and 255, indicating the green portion of
     *            the font color
     * @param blue A value between 0 and 255, indicating the blue portion of the
     *            font color
     * @param size The font size, typically 32 but can be varied depending on
     *            the amount of text being drawn to the screen
     */
    public static void addWinCountdown(final float timeout, final int x, final int y,
            String fontName, final int red, final int green, final int blue, int size) {
        _winCountRemaining = timeout;
        final BitmapFont bf = Media.getFont(fontName, size);
        Level._currLevel._controls.add(new HudEntity(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(_c.r, _c.g, _c.b, 1);
                _winCountRemaining -= Gdx.graphics.getDeltaTime();
                if (_winCountRemaining > 0)
                    // get elapsed time for this level
                    drawTextTransposed(x, y, "" + (int)_winCountRemaining, bf, sb);
                else
                    Level._currLevel._score.endLevel(true);
            }
        });
    }

    /**
     * Add a count of the current number of goodies of type 1
     * 
     * @param max If this is > 0, then the message wil be of the form XX/max
     *            instead of just XX
     * @param text The text to display after the number of goodies
     * @param x The x coordinate where the text should be drawn
     * @param y The y coordinate where the text should be drawn
     */
    public static void addGoodieCount1(int max, String text, int x, int y) {
        addGoodieCount1(max, text, x, y, LOL._game._config.getDefaultFontFace(),
                LOL._game._config.getDefaultFontRed(), LOL._game._config.getDefaultFontGreen(),
                LOL._game._config.getDefaultFontBlue(), LOL._game._config.getDefaultFontSize());
    }

    /**
     * Add a count of the current number of goodies of type 1, with extra
     * features for describing the appearance of the font
     * 
     * @param max If this is > 0, then the message wil be of the form XX/max
     *            instead of just XX
     * @param text The text to display after the number of goodies
     * @param x The x coordinate where the text should be drawn
     * @param y The y coordinate where the text should be drawn
     * @param red A value between 0 and 255, indicating the red portion of the
     *            font color
     * @param green A value between 0 and 255, indicating the green portion of
     *            the font color
     * @param blue A value between 0 and 255, indicating the blue portion of the
     *            font color
     * @param size The font size, typically 32 but can be varied depending on
     *            the amount of text being drawn to the screen
     */
    public static void addGoodieCount1(int max, final String text, final int x, final int y,
            String fontName, final int red, final int green, final int blue, int size) {
        // The suffix to display after the goodie count:
        final String suffix = (max > 0) ? "/" + max + " " + text : " " + text;
        final BitmapFont bf = Media.getFont(fontName, size);
        HudEntity he = new HudEntity(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(_c.r, _c.g, _c.b, 1);
                drawTextTransposed(x, y,
                        "" + Level._currLevel._score._goodiesCollected[0] + suffix, bf, sb);
            }
        };
        Level._currLevel._controls.add(he);
    }

    /**
     * Add a count of the current number of goodies of type 2, with extra
     * features for describing the appearance of the font
     * 
     * @param max If this is > 0, then the message wil be of the form XX/max
     *            instead of just XX
     * @param text The text to display after the number of goodies
     * @param x The x coordinate where the text should be drawn
     * @param y The y coordinate where the text should be drawn
     * @param red A value between 0 and 255, indicating the red portion of the
     *            font color
     * @param green A value between 0 and 255, indicating the green portion of
     *            the font color
     * @param blue A value between 0 and 255, indicating the blue portion of the
     *            font color
     * @param size The font size, typically 32 but can be varied depending on
     *            the amount of text being drawn to the screen
     */
    public static void addGoodieCount2(int max, final String text, final int x, final int y,
            String fontName, final int red, final int green, final int blue, int size) {
        // The suffix to display after the goodie count:
        final String suffix = (max > 0) ? "/" + max + " " + text : " " + text;
        final BitmapFont bf = Media.getFont(fontName, size);
        HudEntity he = new HudEntity(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(_c.r, _c.g, _c.b, 1);
                drawTextTransposed(x, y,
                        "" + Level._currLevel._score._goodiesCollected[1] + suffix, bf, sb);
            }
        };
        Level._currLevel._controls.add(he);
    }

    /**
     * Add a count of the current number of goodies of type 3, with extra
     * features for describing the appearance of the font
     * 
     * @param max If this is > 0, then the message wil be of the form XX/max
     *            instead of just XX
     * @param text The text to display after the number of goodies
     * @param x The x coordinate where the text should be drawn
     * @param y The y coordinate where the text should be drawn
     * @param red A value between 0 and 255, indicating the red portion of the
     *            font color
     * @param green A value between 0 and 255, indicating the green portion of
     *            the font color
     * @param blue A value between 0 and 255, indicating the blue portion of the
     *            font color
     * @param size The font size, typically 32 but can be varied depending on
     *            the amount of text being drawn to the screen
     */
    public static void addGoodieCount3(int max, final String text, final int x, final int y,
            String fontName, final int red, final int green, final int blue, int size) {
        // The suffix to display after the goodie count:
        final String suffix = (max > 0) ? "/" + max + " " + text : " " + text;
        final BitmapFont bf = Media.getFont(fontName, size);
        HudEntity he = new HudEntity(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(_c.r, _c.g, _c.b, 1);
                drawTextTransposed(x, y,
                        "" + Level._currLevel._score._goodiesCollected[2] + suffix, bf, sb);
            }
        };
        Level._currLevel._controls.add(he);

    }

    /**
     * Add a count of the current number of goodies of type 4, with extra
     * features for describing the appearance of the font
     * 
     * @param max If this is > 0, then the message wil be of the form XX/max
     *            instead of just XX
     * @param text The text to display after the number of goodies
     * @param x The x coordinate where the text should be drawn
     * @param y The y coordinate where the text should be drawn
     * @param red A value between 0 and 255, indicating the red portion of the
     *            font color
     * @param green A value between 0 and 255, indicating the green portion of
     *            the font color
     * @param blue A value between 0 and 255, indicating the blue portion of the
     *            font color
     * @param size The font size, typically 32 but can be varied depending on
     *            the amount of text being drawn to the screen
     */
    public static void addGoodieCount4(int max, final String text, final int x, final int y,
            String fontName, final int red, final int green, final int blue, int size) {
        // The suffix to display after the goodie count:
        final String suffix = (max > 0) ? "/" + max + " " + text : " " + text;
        final BitmapFont bf = Media.getFont(fontName, size);
        HudEntity he = new HudEntity(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(_c.r, _c.g, _c.b, 1);
                drawTextTransposed(x, y,
                        "" + Level._currLevel._score._goodiesCollected[3] + suffix, bf, sb);
            }
        };
        Level._currLevel._controls.add(he);
    }

    /**
     * Add a count of the _current number of enemies who have been defeated
     * 
     * @param max If this is > 0, then the message wil be of the form XX/max
     *            instead of just XX
     * @param text The text to display after the number of goodies
     * @param x The x coordinate where the text should be drawn
     * @param y The y coordinate where the text should be drawn
     */
    public static void addDefeatedCount(int max, String text, int x, int y) {
        addDefeatedCount(max, text, x, y, LOL._game._config.getDefaultFontFace(),
                LOL._game._config.getDefaultFontRed(), LOL._game._config.getDefaultFontGreen(),
                LOL._game._config.getDefaultFontBlue(), LOL._game._config.getDefaultFontSize());
    }

    /**
     * Add a count of the _current number of enemies who have been defeated,
     * with extra features for describing the appearance of the font
     * 
     * @param max If this is > 0, then the message wil be of the form XX/max
     *            instead of just XX
     * @param text The text to display after the number of goodies
     * @param x The x coordinate where the text should be drawn
     * @param y The y coordinate where the text should be drawn
     * @param red A value between 0 and 255, indicating the red portion of the
     *            font color
     * @param green A value between 0 and 255, indicating the green portion of
     *            the font color
     * @param blue A value between 0 and 255, indicating the blue portion of the
     *            font color
     * @param size The font size, typically 32 but can be varied depending on
     *            the amount of text being drawn to the screen
     */
    public static void addDefeatedCount(int max, final String text, final int x, final int y,
            String fontName, final int red, final int green, final int blue, int size) {
        // The suffix to display after the goodie count:
        final String suffix = (max > 0) ? "/" + max + " " + text : " " + text;
        final BitmapFont bf = Media.getFont(fontName, size);
        HudEntity he = new HudEntity(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(_c.r, _c.g, _c.b, 1);
                drawTextTransposed(x, y, "" + Level._currLevel._score._enemiesDefeated + suffix,
                        bf, sb);
            }
        };
        Level._currLevel._controls.add(he);
    }

    /**
     * Add a stopwatch for tracking how long a level takes
     * 
     * @param x The x coordinate where the stopwatch should be drawn
     * @param y The y coordinate where the stopwatch should be drawn
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
     * @param x The x coordinate where the stopwatch should be drawn
     * @param y The y coordinate where the stopwatch should be drawn
     * @param red A value between 0 and 255, indicating the red portion of the
     *            font color
     * @param green A value between 0 and 255, indicating the green portion of
     *            the font color
     * @param blue A value between 0 and 255, indicating the blue portion of the
     *            font color
     * @param size The font size, typically 32 but can be varied depending on
     *            the amount of text being drawn to the screen
     */
    static public void addStopwatch(final int x, final int y, String fontName, final int red,
            final int green, final int blue, int size) {
        _stopWatchProgress = 0;
        final BitmapFont bf = Media.getFont(fontName, size);
        HudEntity he = new HudEntity(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(_c.r, _c.g, _c.b, 1);
                _stopWatchProgress += Gdx.graphics.getDeltaTime();
                drawTextTransposed(x, y, "" + (int)_stopWatchProgress, bf, sb);
            }
        };
        Level._currLevel._controls.add(he);
    }

    /**
     * Display a strength meter
     * 
     * @param text The text to display after the remaining _strength value
     * @param x The x coordinate where the text should be drawn
     * @param y The y coordinate where the text should be drawn
     */
    static public void addStrengthMeter(String text, int x, int y, Hero h) {
        // forward to the more powerful method...
        addStrengthMeter(text, x, y, LOL._game._config.getDefaultFontFace(),
                LOL._game._config.getDefaultFontRed(), LOL._game._config.getDefaultFontGreen(),
                LOL._game._config.getDefaultFontBlue(), LOL._game._config.getDefaultFontSize(), h);
    }

    /**
     * Display a _strength meter, with extra features for describing the
     * appearance of the font
     * 
     * @param text The text to display after the remaining _strength value
     * @param x The x coordinate where the text should be drawn
     * @param y The y coordinate where the text should be drawn
     * @param red A value between 0 and 255, indicating the red portion of the
     *            font color
     * @param green A value between 0 and 255, indicating the green portion of
     *            the font color
     * @param blue A value between 0 and 255, indicating the blue portion of the
     *            font color
     * @param size The font size, typically 32 but can be varied depending on
     *            the amount of text being drawn to the screen
     */
    static public void addStrengthMeter(final String text, final int x, final int y,
            String fontName, final int red, final int green, final int blue, int size, final Hero h) {
        final BitmapFont bf = Media.getFont(fontName, size);
        HudEntity he = new HudEntity(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(_c.r, _c.g, _c.b, 1);
                drawTextTransposed(x, y, "" + h._strength + " " + text, bf, sb);
            }
        };
        Level._currLevel._controls.add(he);
    }

    /**
     * Display the number of remaining projectiles
     * 
     * @param text The text to display after the number of goodies
     * @param x The x coordinate where the text should be drawn
     * @param y The y coordinate where the text should be drawn
     * @param red The red dimension of the font color
     * @param green The green dimension of the font color
     * @param blue The blue dimension of the font color
     * @param size The size of the font
     */
    public static void addProjectileCount(final String text, final int x, final int y,
            String fontName, final int red, final int green, final int blue, int size) {
        final BitmapFont bf = Media.getFont(fontName, size);
        HudEntity he = new HudEntity(red, green, blue) {
            @Override
            void render(SpriteBatch sb) {
                bf.setColor(_c.r, _c.g, _c.b, 1);
                drawTextTransposed(x, y, "" + Projectile._projectilesRemaining + " " + text, bf, sb);
            }
        };
        Level._currLevel._controls.add(he);
    }

    /*
     * GRAPHICAL BUTTON CONTROLS
     */

    /**
     * Add a button that moves an entity downward
     * 
     * @param x X coordinate of top left corner of the button
     * @param y Y coordinate of top left corner of the button
     * @param width Width of the button
     * @param height Height of the button
     * @param imgName Name of the image to use for this button
     * @param rate Rate at which the entity moves
     * @param entity The entity to move downward
     */
    public static void addDownButton(int x, int y, int width, int height, String imgName,
            final float rate, final PhysicsSprite entity) {
        HudEntity pe = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 vv) {
                Vector2 v = entity._physBody.getLinearVelocity();
                v.y = -rate;
                entity.updateVelocity(v.x, v.y);
            }

            @Override
            void onHold(Vector3 vv) {
                onDownPress(vv);
            }

            @Override
            void onUpPress() {
                Vector2 v = entity._physBody.getLinearVelocity();
                v.y = 0;
                entity.updateVelocity(v.x, v.y);
            }
        };
        Level._currLevel._controls.add(pe);
    }

    public static void addPauseButton(int x, int y, int width, int height, String imgName) {
        HudEntity pe = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 vv) {
                Level._currLevel._pauseScene._visible = true;
            }
        };
        Level._currLevel._controls.add(pe);
    }

    /**
     * Add a button that moves an entity upward
     * 
     * @param x X coordinate of top left corner of the button
     * @param y Y coordinate of top left corner of the button
     * @param width Width of the button
     * @param height Height of the button
     * @param imgName Name of the image to use for this button
     * @param rate Rate at which the entity moves
     * @param entity The entity to move
     */
    public static void addUpButton(int x, int y, int width, int height, String imgName,
            final float rate, final PhysicsSprite entity) {
        HudEntity pe = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 vv) {
                Vector2 v = entity._physBody.getLinearVelocity();
                v.y = rate;
                entity.updateVelocity(v.x, v.y);
            }

            @Override
            void onHold(Vector3 vv) {
                onDownPress(vv);
            }

            @Override
            void onUpPress() {
                Vector2 v = entity._physBody.getLinearVelocity();
                v.y = 0;
                entity.updateVelocity(v.x, v.y);
            }
        };
        Level._currLevel._controls.add(pe);
    }

    /**
     * Add a button that moves the given entity left
     * 
     * @param x X coordinate of top left corner of the button
     * @param y Y coordinate of top left corner of the button
     * @param width Width of the button
     * @param height Height of the button
     * @param imgName Name of the image to use for this button
     * @param rate Rate at which the entity moves
     * @param entity The entity that should move left when the button is pressed
     */
    public static void addLeftButton(int x, int y, int width, int height, String imgName,
            final float rate, final PhysicsSprite entity) {
        HudEntity pe = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 vv) {
                Vector2 v = entity._physBody.getLinearVelocity();
                v.x = -rate;
                entity.updateVelocity(v.x, v.y);
            }

            @Override
            void onHold(Vector3 vv) {
                onDownPress(vv);
            }

            @Override
            void onUpPress() {
                Vector2 v = entity._physBody.getLinearVelocity();
                v.x = 0;
                entity.updateVelocity(v.x, v.y);
            }
        };
        Level._currLevel._controls.add(pe);
    }

    /**
     * Add a button that moves the given entity to the right
     * 
     * @param x X coordinate of top left corner of the button
     * @param y Y coordinate of top left corner of the button
     * @param width Width of the button
     * @param height Height of the button
     * @param imgName Name of the image to use for this button
     * @param rate Rate at which the entity moves
     * @param entity The entity that should move right when the button is
     *            pressed
     */
    public static void addRightButton(int x, int y, int width, int height, String imgName,
            final float rate, final PhysicsSprite entity) {
        HudEntity pe = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 vv) {
                Vector2 v = entity._physBody.getLinearVelocity();
                v.x = rate;
                entity.updateVelocity(v.x, v.y);
            }

            @Override
            void onHold(Vector3 vv) {
                onDownPress(vv);
            }

            @Override
            void onUpPress() {
                Vector2 v = entity._physBody.getLinearVelocity();
                v.x = 0;
                entity.updateVelocity(v.x, v.y);
            }
        };
        Level._currLevel._controls.add(pe);
    }

    /**
     * Add a button that moves the given entity at one speed when it is
     * depressed, and at another otherwise
     * 
     * @param x X coordinate of top left corner of the button
     * @param y Y coordinate of top left corner of the button
     * @param width Width of the button
     * @param height Height of the button
     * @param imgName Name of the image to use for this button
     * @param rateDownX Rate (X) at which the entity moves when the button is
     *            pressed
     * @param rateDownY Rate (Y) at which the entity moves when the button is
     *            pressed
     * @param rateUpX Rate (X) at which the entity moves when the button is not
     *            pressed
     * @param rateUpY Rate (Y) at which the entity moves when the button is not
     *            pressed
     * @param entity The entity that should move left when the button is pressed
     */
    public static void addTurboButton(int x, int y, int width, int height, String imgName,
            final int rateDownX, final int rateDownY, final int rateUpX, final int rateUpY,
            final PhysicsSprite entity) {
        // see left button for note on body type
        if (entity._physBody.getType() == BodyType.StaticBody)
            entity._physBody.setType(BodyType.DynamicBody);

        HudEntity pe = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 vv) {
                Vector2 v = entity._physBody.getLinearVelocity();
                v.x = rateDownX;
                v.y = rateDownY;
                entity.updateVelocity(v.x, v.y);
            }

            @Override
            void onUpPress() {
                Vector2 v = entity._physBody.getLinearVelocity();
                v.x = rateUpX;
                v.y = rateUpY;
                entity.updateVelocity(v.x, v.y);
            }
        };
        Level._currLevel._controls.add(pe);
    }

    /**
     * Add a button that moves the given entity at one speed, but doesn't stop
     * the entity when the button is released
     * 
     * @param x X coordinate of top left corner of the button
     * @param y Y coordinate of top left corner of the button
     * @param width Width of the button
     * @param height Height of the button
     * @param imgName Name of the image to use for this button
     * @param rateX Rate (X) at which the entity moves when the button is
     *            pressed
     * @param rateY Rate (Y) at which the entity moves when the button is
     *            pressed
     * @param entity The entity that should move left when the button is pressed
     */
    public static void addDampenedMotionButton(int x, int y, int width, int height, String imgName,
            final float rateX, final float rateY, final float dampening, final PhysicsSprite entity) {
        // see left button for note on body type
        if (entity._physBody.getType() == BodyType.StaticBody)
            entity._physBody.setType(BodyType.DynamicBody);

        HudEntity pe = new HudEntity(imgName, x, y, width, height) {
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
        Level._currLevel._controls.add(pe);
    }

    /**
     * Add a button that puts the hero into crawl mode when depressed, and
     * regular mode when released
     * 
     * @param x X coordinate of top left corner of the button
     * @param y Y coordinate of top left corner of the button
     * @param width Width of the button
     * @param height Height of the button
     * @param imgName Name of the image to use for this button
     */
    public static void addCrawlButton(int x, int y, int width, int height, String imgName,
            final Hero h) {
        HudEntity pe = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 vv) {
                h.crawlOn();
            }

            @Override
            void onUpPress() {
                h.crawlOff();
            }
        };
        Level._currLevel._controls.add(pe);
    }

    /**
     * Add a button to make the hero jump
     * 
     * @param x X coordinate of top left corner of the button
     * @param y Y coordinate of top left corner of the button
     * @param width Width of the button
     * @param height Height of the button
     * @param imgName Name of the image to use for this button
     */
    public static void addJumpButton(int x, int y, int width, int height, String imgName,
            final Hero h) {
        HudEntity pe = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 vv) {
                h.jump();
            }
        };
        Level._currLevel._controls.add(pe);
    }

    /**
     * Add a button to make the hero throw a projectile
     * 
     * @param x X coordinate of top left corner of the button
     * @param y Y coordinate of top left corner of the button
     * @param width Width of the button
     * @param height Height of the button
     * @param imgName Name of the image to use for this button
     */
    public static void addThrowButton(int x, int y, int width, int height, String imgName,
            final Hero h, final int milliDelay) {
        HudEntity pe = new HudEntity(imgName, x, y, width, height) {
            long lastThrow;

            @Override
            void onDownPress(Vector3 vv) {
                Projectile.throwFixed(h._physBody.getPosition().x, h._physBody.getPosition().y, h);
                lastThrow = System.nanoTime();
            }

            @Override
            void onHold(Vector3 vv) {
                long now = System.nanoTime();
                if (lastThrow + milliDelay * 1000000 < now) {
                    lastThrow = now;
                    Projectile.throwFixed(h._physBody.getPosition().x, h._physBody.getPosition().y,
                            h);
                }
            }
        };
        Level._currLevel._controls.add(pe);
    }

    /**
     * Add a button to make the hero throw a projectile, but holding doesn't
     * make it throw more often
     * 
     * @param x X coordinate of top left corner of the button
     * @param y Y coordinate of top left corner of the button
     * @param width Width of the button
     * @param height Height of the button
     * @param imgName Name of the image to use for this button
     */
    public static void addSingleThrowButton(int x, int y, int width, int height, String imgName,
            final Hero h) {
        HudEntity pe = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 vv) {
                Projectile.throwFixed(h._physBody.getPosition().x, h._physBody.getPosition().y, h);
            }

            @Override
            void onUpPress() {
            }
        };
        Level._currLevel._controls.add(pe);
    }

    /**
     * The default behavior for throwing is to throw in a straight line. If we
     * instead desire that the bullets have some sort of aiming to them, we need
     * to use this method, which throws toward where the screen was pressed
     * Note: you probably want to use an invisible button that covers the
     * screen...
     * 
     * @param x X coordinate of top left corner of the button
     * @param y Y coordinate of top left corner of the button
     * @param width Width of the button
     * @param height Height of the button
     * @param imgName Name of the image to use for this button
     */
    public static void addVectorThrowButton(int x, int y, int width, int height, String imgName,
            final Hero h, final long milliDelay) {
        HudEntity pe = new HudEntity(imgName, x, y, width, height) {
            long lastThrow;

            @Override
            void onDownPress(Vector3 vv) {
                Projectile.throwAt(h._physBody.getPosition().x, h._physBody.getPosition().y, vv.x,
                        vv.y, h);
                lastThrow = System.nanoTime();
            }

            @Override
            void onHold(Vector3 vv) {
                long now = System.nanoTime();
                if (lastThrow + milliDelay * 1000000 < now) {
                    lastThrow = now;
                    Projectile.throwAt(h._physBody.getPosition().x, h._physBody.getPosition().y,
                            vv.x, vv.y, h);
                }
            }

        };
        Level._currLevel._controls.add(pe);
    }

    /**
     * The default behavior for throwing projectiles is to throw in a straight
     * line. If we instead desire that the bullets have some sort of aiming to
     * them, we need to use this method, which throws toward where the screen
     * was pressed. Note that with this command, the button that is drawn on the
     * screen cannot be held down to throw multipel projectiles in rapid
     * succession.
     * 
     * @param x X coordinate of top left corner of the button
     * @param y Y coordinate of top left corner of the button
     * @param width Width of the button
     * @param height Height of the button
     * @param imgName Name of the image to use for this button
     */
    public static void addVectorSingleThrowButton(int x, int y, int width, int height,
            String imgName, final Hero h) {
        HudEntity pe = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 vv) {
                Projectile.throwAt(h._physBody.getPosition().x, h._physBody.getPosition().y, vv.x,
                        vv.y, h);
            }

            @Override
            void onUpPress() {
            }
        };
        Level._currLevel._controls.add(pe);
    }

    /**
     * Display a zoom in button
     * 
     * @param x X coordinate of top left corner of the button
     * @param y Y coordinate of top left corner of the button
     * @param width Width of the button
     * @param height Height of the button
     * @param imgName Name of the image to use for this button
     * @param maxZoom Maximum zoom. 8 is usually a good default
     */
    public static void addZoomOutButton(int x, int y, int width, int height, String imgName,
            final float maxZoom) {
        HudEntity pe = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 v) {
                float curzoom = Level._currLevel._gameCam.zoom;
                if (curzoom < maxZoom)
                    Level._currLevel._gameCam.zoom *= 2;
            }

            @Override
            void onUpPress() {
            }
        };
        Level._currLevel._controls.add(pe);
    }

    /**
     * Display a zoom out button
     * 
     * @param x X coordinate of top left corner of the button
     * @param y Y coordinate of top left corner of the button
     * @param width Width of the button
     * @param height Height of the button
     * @param imgName Name of the image to use for this button
     * @param minZoom Minimum zoom. 0.25f is usually a good default
     */
    public static void addZoomInButton(int x, int y, int width, int height, String imgName,
            final float minZoom) {
        HudEntity pe = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 v) {
                float curzoom = Level._currLevel._gameCam.zoom;
                if (curzoom > minZoom)
                    Level._currLevel._gameCam.zoom /= 2;
            }

            @Override
            void onUpPress() {
            }
        };
        Level._currLevel._controls.add(pe);
    }

    /**
     * Add a button that rotates the hero
     * 
     * @param x X coordinate of top left corner of the button
     * @param y Y coordinate of top left corner of the button
     * @param width Width of the button
     * @param height Height of the button
     * @param imgName Name of the image to use for this button
     * @param rate Amount of rotation to apply to the hero on each press
     */
    public static void addRotateButton(int x, int y, int width, int height, String imgName,
            final float rate, final Hero h) {
        HudEntity pe = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 vv) {
                h.increaseRotation(rate);
            }

            @Override
            void onHold(Vector3 vv) {
                h.increaseRotation(rate);
            }
        };
        Level._currLevel._controls.add(pe);
    }

    /**
     * Add an image to the heads-up display
     * 
     * @param x X coordinate of top left corner of the image
     * @param y Y coordinate of top left corner of the image
     * @param width Width of the image
     * @param height Height of the image
     * @param imgName Name of the image to use
     */
    public static void addImage(int x, int y, int width, int height, String imgName) {
        HudEntity pe = new HudEntity(imgName, x, y, width, height);
        // get rid of the HudEntity's range, so that it does not handle touches
        pe._range = null;
        Level._currLevel._controls.add(pe);
    }

    /**
     * Add a button to the heads-up display that runs custom code via
     * onControlPress
     * 
     * @param x X coordinate of top left corner of the image
     * @param y Y coordinate of top left corner of the image
     * @param width Width of the image
     * @param height Height of the image
     * @param imgName Name of the image to use
     * @param id An id to use for the trigger event
     */
    public static void addTriggerControl(int x, int y, int width, int height, String imgName,
            final int id) {
        HudEntity pe = new HudEntity(imgName, x, y, width, height) {
            @Override
            void onDownPress(Vector3 vv) {
                LOL._game.onControlPressTrigger(id, LOL._game._currLevel);
            }
        };
        Level._currLevel._controls.add(pe);
    }
}
