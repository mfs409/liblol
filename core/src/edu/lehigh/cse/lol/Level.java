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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Timer;

/**
 * Level provides a broad, public, declarative interface to the core functionality of LibLOL.
 * <p>
 * Game designers will spend most of their time in the <code>display</code> function of the various
 * <code>ScreenManager</code> objects that comprise the game (i.e., Chooser, Help, Levels, Splash,
 * Store).  Within that function, a <code>Level</code> object is available.  It corresponds to a
 * pre-configured, blank, interactive portion of the game.  By calling functions on the level, a
 * programmer can realize their game.
 * <p>
 * Conceptually, a Level consists of many screens:
 * <ul>
 * <li>MainScreen: This is where the Actors of the game are drawn</li>
 * <li>Hud: A heads-up display onto which text and input controls can be drawn</li>
 * <li>PreScene: A quick scene to display before the level starts</li>
 * <li>PostScene (WinScene or LoseScene): Two quick scenes to display at the end of the level</li>
 * <li>PauseScene: A scene to show when the game is paused</li>
 * </ul>
 */
public class Level {
    /// A reference to the game object, so we can access session facts and the state machine
    private final Lol mGame;
    /// A reference to the game-wide configuration variables
    protected final Config mConfig;
    /// A reference to the object that stores all of the sounds and images we use in the game
    protected final Media mMedia;

    /**
     * Construct a level.  Since Level is merely a facade, this method need only store references to
     * the actual game objects.
     *
     * @param config The configuration object describing this game
     * @param media  References to all image and sound assets
     * @param game   The top-level game object
     */
    Level(Config config, Media media, Lol game) {
        // save game configuration information
        mGame = game;
        mConfig = config;
        mMedia = media;
    }

    /**
     * Configure the camera bounds for a level
     * <p>
     * TODO: set upper and lower bounds, instead of assuming a lower bound of (0, 0)
     *
     * @param width  width of the camera
     * @param height height of the camera
     */
    public void setCameraBounds(float width, float height) {
        mGame.mManager.mWorld.mCamBound.set(width, height);

        // warn on strange dimensions
        if (width < mConfig.mWidth / mConfig.mPixelMeterRatio)
            Lol.message(mConfig, "Warning", "Your game width is less than 1/10 of the screen width");
        if (height < mConfig.mHeight / mConfig.mPixelMeterRatio)
            Lol.message(mConfig, "Warning", "Your game height is less than 1/10 of the screen height");
    }

    /**
     * Identify the actor that the camera should try to keep on screen at all times
     *
     * @param actor The actor the camera should chase
     */
    public void setCameraChase(WorldActor actor) {
        mGame.mManager.mWorld.mChaseActor = actor;
    }

    /**
     * Set the background music for this level
     *
     * @param musicName Name of the Music file to play
     */
    public void setMusic(String musicName) {
        mGame.mManager.mWorld.mMusic = mMedia.getMusic(musicName);
    }

    /**
     * Specify that you want some code to run after a fixed amount of time passes.
     *
     * @param howLong  How long to wait before the timer code runs
     * @param callback The code to run
     */
    public void setTimerCallback(float howLong, final LolAction callback) {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (!mGame.mManager.mGameOver)
                    callback.go();
            }
        }, howLong);
    }

    /**
     * Specify that you want some code to run repeatedly
     *
     * @param howLong  How long to wait before the timer code runs for the first time
     * @param interval The time between subsequent executions of the code
     * @param callback The code to run
     */
    public void setTimerCallback(float howLong, float interval, final LolAction callback) {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (!mGame.mManager.mGameOver)
                    callback.go();
            }
        }, howLong, interval);
    }

    /**
     * Turn on scribble mode, so that scene touch events draw circular objects
     * <p>
     * Note: this code should be thought of as serving to demonstrate, only. If you really wanted to
     * do anything clever with scribbling, you'd certainly want to change this code.
     *
     * @param imgName          The name of the image to use for scribbling
     * @param width            Width of the individual components of the scribble
     * @param height           Height of the individual components of the scribble
     * @param interval         Time (in milliseconds) that must transpire between scribble events...
     *                         use this to avoid outrageously high rates of scribbling
     * @param onCreateCallback A callback to run in order to modify the scribble behavior. The
     *                         obstacle that is drawn in the scribble will be passed to the callback
     */
    public void setScribbleMode(final String imgName, final float width, final float height,
                                final int interval, final LolActorEvent onCreateCallback) {
        // we set a callback on the Level, so that any touch to the level (down, drag, up) will
        // affect our scribbling
        mGame.mManager.mWorld.mPanHandlers.add(new PanEventHandler() {
            /// The time of the last touch event... we use this to prevent high rates of scribble
            long mLastTime;

            /**
             * Draw a new obstacle if enough time has transpired
             */
            public boolean go(float worldX, float worldY, float deltaX, float deltaY) {
                // check if enough milliseconds have passed
                long now = System.currentTimeMillis();
                if (now < mLastTime + interval) {
                    return true;
                }
                mLastTime = now;

                // make a circular obstacle
                final Obstacle o = makeObstacleAsCircle(worldX - width / 2, worldY - height / 2,
                        width, height, imgName);
                if (onCreateCallback != null) {
                    onCreateCallback.go(o);
                }

                return true;
            }
        });
    }

    /**
     * Manually set the zoom level of the game
     *
     * @param zoom The amount of zoom (1 is no zoom, &gt;1 zooms out)
     */
    public void setZoom(float zoom) {
        mGame.mManager.mWorld.mCamera.zoom = zoom;
        mGame.mManager.mBackground.mBgCam.zoom = zoom;
        mGame.mManager.mForeground.mBgCam.zoom = zoom;
    }

    /**
     * Register a callback so that custom code will run when the level is won
     *
     * @param callback The code to run
     */
    public void setWinCallback(LolAction callback) {
        mGame.mManager.mWinCallback = callback;
    }

    /**
     * Register a callback so that custom code will run when the level is lost
     *
     * @param callback The code to run
     */
    public void setLoseCallback(LolAction callback) {
        mGame.mManager.mLoseCallback = callback;
    }


    /**
     * Manually increment the number of goodies of type 1 that have been collected.
     */
    public void incrementGoodiesCollected1() {
        mGame.mManager.mGoodiesCollected[0]++;
    }

    /**
     * Manually increment the number of goodies of type 2 that have been collected.
     */
    public void incrementGoodiesCollected2() {
        mGame.mManager.mGoodiesCollected[1]++;
    }

    /**
     * Manually increment the number of goodies of type 3 that have been collected.
     */
    public void incrementGoodiesCollected3() {
        mGame.mManager.mGoodiesCollected[2]++;
    }

    /**
     * Manually increment the number of goodies of type 4 that have been collected.
     */
    public void incrementGoodiesCollected4() {
        mGame.mManager.mGoodiesCollected[3]++;
    }

    /**
     * Getter for number of goodies of type 1 that have been collected.
     *
     * @return The number of goodies collected.
     */
    public int getGoodiesCollected1() {
        return mGame.mManager.mGoodiesCollected[0];
    }

    /**
     * Manually set the number of goodies of type 1 that have been collected.
     *
     * @param value The new value
     */
    public void setGoodiesCollected1(int value) {
        mGame.mManager.mGoodiesCollected[0] = value;
    }

    /**
     * Getter for number of goodies of type 2 that have been collected.
     *
     * @return The number of goodies collected.
     */
    public int getGoodiesCollected2() {
        return mGame.mManager.mGoodiesCollected[1];
    }

    /**
     * Manually set the number of goodies of type 2 that have been collected.
     *
     * @param value The new value
     */
    public void setGoodiesCollected2(int value) {
        mGame.mManager.mGoodiesCollected[1] = value;
    }

    /**
     * Getter for number of goodies of type 3 that have been collected.
     *
     * @return The number of goodies collected.
     */
    public int getGoodiesCollected3() {
        return mGame.mManager.mGoodiesCollected[2];
    }

    /**
     * Manually set the number of goodies of type 3 that have been collected.
     *
     * @param value The new value
     */
    public void setGoodiesCollected3(int value) {
        mGame.mManager.mGoodiesCollected[2] = value;
    }

    /**
     * Getter for number of goodies of type 4 that have been collected.
     *
     * @return The number of goodies collected.
     */
    public int getGoodiesCollected4() {
        return mGame.mManager.mGoodiesCollected[3];
    }

    /**
     * Manually set the number of goodies of type 4 that have been collected.
     *
     * @param value The new value
     */
    public void setGoodiesCollected4(int value) {
        mGame.mManager.mGoodiesCollected[3] = value;
    }

    /**
     * Indicate that the level is won by defeating all the enemies. This version is useful if the
     * number of enemies isn't known, or if the goal is to defeat all enemies before more are are
     * created.
     */
    public void setVictoryEnemyCount() {
        mGame.mManager.mVictoryType = LolManager.VictoryType.ENEMYCOUNT;
        mGame.mManager.mVictoryEnemyCount = -1;
    }

    /**
     * Indicate that the level is won by defeating a certain number of enemies
     *
     * @param howMany The number of enemies that must be defeated to win the level
     */
    public void setVictoryEnemyCount(int howMany) {
        mGame.mManager.mVictoryType = LolManager.VictoryType.ENEMYCOUNT;
        mGame.mManager.mVictoryEnemyCount = howMany;
    }

    /**
     * Indicate that the level is won by collecting enough goodies
     *
     * @param v1 Number of type-1 goodies that must be collected to win the level
     * @param v2 Number of type-2 goodies that must be collected to win the level
     * @param v3 Number of type-3 goodies that must be collected to win the level
     * @param v4 Number of type-4 goodies that must be collected to win the level
     */
    public void setVictoryGoodies(int v1, int v2, int v3, int v4) {
        mGame.mManager.mVictoryType = LolManager.VictoryType.GOODIECOUNT;
        mGame.mManager.mVictoryGoodieCount[0] = v1;
        mGame.mManager.mVictoryGoodieCount[1] = v2;
        mGame.mManager.mVictoryGoodieCount[2] = v3;
        mGame.mManager.mVictoryGoodieCount[3] = v4;
    }

    /**
     * Indicate that the level is won by having a certain number of heroes reach destinations
     *
     * @param howMany Number of heroes that must reach destinations
     */
    public void setVictoryDestination(int howMany) {
        mGame.mManager.mVictoryType = LolManager.VictoryType.DESTINATION;
        mGame.mManager.mVictoryHeroCount = howMany;
    }

    /**
     * Change the amount of time left in a countdown timer
     *
     * @param delta The amount of time to add before the timer expires
     */
    public void updateTimerExpiration(float delta) {
        mGame.mManager.mLoseCountDownRemaining += delta;
    }

    /**
     * Report the total distance the hero has traveled
     *
     * @return The distance the hero has traveled
     */
    public int getDistance() {
        return mGame.mManager.mDistance;
    }

    /**
     * Report the stopwatch value
     *
     * @return the stopwatch value
     */
    public int getStopwatch() {
        // Inactive stopwatch should return 0
        if (mGame.mManager.mStopWatchProgress == -100)
            return 0;
        return (int) mGame.mManager.mStopWatchProgress;
    }

    /**
     * Report the number of enemies that have been defeated
     *
     * @return the number of defeated enemies
     */
    public int getEnemiesDefeated() {
        return mGame.mManager.mEnemiesDefeated;
    }

    /**
     * Force the level to end in victory
     * <p>
     * This is useful in callbacks, where we might want to immediately end the game
     */
    public void winLevel() {
        mGame.mManager.endLevel(true);
    }

    /**
     * Force the level to end in defeat
     * <p>
     * This is useful in callbacks, where we might want to immediately end the game
     */
    public void loseLevel() {
        mGame.mManager.endLevel(false);
    }

    /**
     * Change the gravity in a running level
     *
     * @param newXGravity The new X gravity
     * @param newYGravity The new Y gravity
     */
    public void resetGravity(float newXGravity, float newYGravity) {
        mGame.mManager.mWorld.mWorld.setGravity(new Vector2(newXGravity, newYGravity));
    }

    /**
     * Turn on accelerometer support so that tilt can control actors in this level
     *
     * @param xGravityMax Max X force that the accelerometer can produce
     * @param yGravityMax Max Y force that the accelerometer can produce
     */
    public void enableTilt(float xGravityMax, float yGravityMax) {
        mGame.mManager.mWorld.mTiltMax = new Vector2(xGravityMax, yGravityMax);
    }

    /**
     * Turn off accelerometer support so that tilt stops controlling actors in this level
     */
    public void disableTilt() {
        mGame.mManager.mWorld.mTiltMax = null;
    }

    /**
     * This method lets us change the behavior of tilt, so that instead of applying a force, we
     * directly set the velocity of objects using the accelerometer data.
     *
     * @param toggle This should usually be false. Setting it to true means that tilt does not cause
     *               forces upon objects, but instead the tilt of the phone directly sets velocities
     */
    public void setTiltAsVelocity(boolean toggle) {
        mGame.mManager.mWorld.mTiltVelocityOverride = toggle;
    }

    /**
     * Use this to make the accelerometer more or less responsive, by multiplying accelerometer
     * values by a constant.
     *
     * @param multiplier The constant that should be multiplied by the accelerometer data. This can
     *                   be a fraction, like 0.5f, to make the accelerometer less sensitive
     */
    public void setGravityMultiplier(float multiplier) {
        mGame.mManager.mWorld.mTiltMultiplier = multiplier;
    }

    /**
     * Generate text that doesn't change
     *
     * @param text The text to generate each time the TextProducer is called
     * @return A TextProducer who generates the text
     */
    public TextProducer DisplayFixedText(final String text) {
        return new TextProducer() {
            @Override
            public String makeText() {
                return text;
            }
        };
    }

    /**
     * Generate text indicating the current FPS
     */
    public final TextProducer DisplayFPS = new TextProducer() {
        @Override
        public String makeText() {
            return "" + Gdx.graphics.getFramesPerSecond();
        }
    };

    /**
     * Generate text indicating the current count of Type 1 Goodies
     */
    public final TextProducer DisplayGoodies1 = new TextProducer() {
        @Override
        public String makeText() {
            return "" + mGame.mManager.mGoodiesCollected[0];
        }
    };

    /**
     * Generate text indicating the current count of Type 2 Goodies
     */
    public final TextProducer DisplayGoodies2 = new TextProducer() {
        @Override
        public String makeText() {
            return "" + mGame.mManager.mGoodiesCollected[1];
        }
    };

    /**
     * Generate text indicating the current count of Type 3 Goodies
     */
    public final TextProducer DisplayGoodies3 = new TextProducer() {
        @Override
        public String makeText() {
            return "" + mGame.mManager.mGoodiesCollected[2];
        }
    };

    /**
     * Generate text indicating the current count of Type 4 Goodies
     */
    public final TextProducer DisplayGoodies4 = new TextProducer() {
        @Override
        public String makeText() {
            return "" + mGame.mManager.mGoodiesCollected[3];
        }
    };

    /**
     * Generate text indicating the time until the level is lost
     */
    public final TextProducer DisplayLoseCountdown = new TextProducer() {
        @Override
        public String makeText() {
            return "" + (int) mGame.mManager.mLoseCountDownRemaining;
        }
    };

    /**
     * Generate text indicating the time until the level is won
     */
    public final TextProducer DisplayWinCountdown = new TextProducer() {
        @Override
        public String makeText() {
            return "" + (int) mGame.mManager.mWinCountRemaining;
        }
    };

    /**
     * Generate text indicating the number of defeated enemies
     */
    public final TextProducer DisplayEnemiesDefeated = new TextProducer() {
        @Override
        public String makeText() {
            return "" + mGame.mManager.mEnemiesDefeated;
        }
    };

    /**
     * Generate text indicating the value of the stopwatch
     */
    public final TextProducer DisplayStopwatch = new TextProducer() {
        @Override
        public String makeText() {
            return "" + (int) mGame.mManager.mStopWatchProgress;
        }
    };

    /**
     * Generate text indicating the remaining projectiles
     */
    public final TextProducer DisplayRemainingProjectiles = new TextProducer() {
        @Override
        public String makeText() {
            return "" + mGame.mManager.mWorld.mProjectilePool.mProjectilesRemaining;
        }
    };

    /**
     * Generate text indicating the strength of a hero
     *
     * @param h The hero whose strength is to be displayed
     * @return A TextProducer who produces the hero's strength
     */
    public TextProducer DisplayStrength(final Hero h) {
        return new TextProducer() {
            @Override
            public String makeText() {
                return "" + h.getStrength();
            }
        };
    }

    /**
     * Generate text indicating the value of a Level fact
     *
     * @param key The key to use to get the Level fact
     * @return A TextProducer who reports the current value
     */
    public TextProducer DisplayLevelFact(final String key) {
        return new TextProducer() {
            @Override
            public String makeText() {
                return "" + getLevelFact(key, -1);
            }
        };
    }

    /**
     * Generate text indicating the value of a Session fact
     *
     * @param key The key to use to get the Session fact
     * @return A TextProducer who reports the current value
     */
    public TextProducer DisplaySessionFact(final String key) {
        return new TextProducer() {
            @Override
            public String makeText() {
                return "" + getSessionFact(key, -1);
            }
        };
    }

    /**
     * Generate text indicating the value of a Game fact
     *
     * @param key The key to use to get the Game fact
     * @return A TextProducer who reports the current value
     */
    public TextProducer DisplayGameFact(final String key) {
        return new TextProducer() {
            @Override
            public String makeText() {
                return "" + getGameFact(key, -1);
            }
        };
    }

    /**
     * Generate text indicating the distance that an actor has travelled.
     * <p>
     * Note: This distance will also become the Distance Score for the level.
     *
     * @param actor The actor whose distance is being monitored
     * @return A TextProducer that reports the current value
     */
    public TextProducer DisplayDistance(final WorldActor actor) {
        return new TextProducer() {
            @Override
            public String makeText() {
                mGame.mManager.mDistance = (int) actor.getXPosition();
                return "" + mGame.mManager.mDistance;
            }
        };
    }

    /**
     * Place some text on the screen.  The text will be generated by tp, which is called on every
     * screen render
     *
     * @param x         The X coordinate of the bottom left corner (in pixels)
     * @param y         The Y coordinate of the bottom left corner (in pixels)
     * @param fontName  The name of the font to use
     * @param fontColor The color to use for the text
     * @param size      The font size
     * @param prefix    Text to display before the produced text
     * @param suffix    Text to display after the produced text
     * @param tp        The TextProducer
     * @param zIndex    The z index where the text should go
     * @return The display, so that it can be controlled further if needed
     */
    public Renderable addDisplay(final float x, final float y, final String fontName,
                                 final String fontColor, final int size, final String prefix,
                                 final String suffix, final TextProducer tp, int zIndex) {
        return mGame.mManager.mHud.addText(x, y, fontName, fontColor, size, prefix, suffix, tp,
                zIndex);
    }

    /**
     * Indicate that the level will end in defeat if it is not completed in a given amount of time.
     *
     * @param timeout The amount of time until the level will end in defeat
     * @param text    The text to display when the level ends in defeat
     */
    public void setLoseCountdown(float timeout, String text) {
        // Once the Lose CountDown is not -100, it will start counting down
        this.mGame.mManager.mLoseCountDownRemaining = timeout;
        this.mGame.mManager.mLoseCountDownText = text;
    }

    /**
     * Indicate that the level will end in victory if the hero survives for a given amount of time
     *
     * @param timeout The amount of time until the level will end in victory
     * @param text    The text to display when the level ends in victory
     */
    public void setWinCountdown(float timeout, String text) {
        // Once the Win CountDown is not -100, it will start counting down
        this.mGame.mManager.mWinCountRemaining = timeout;
        this.mGame.mManager.mWinCountText = text;
    }

    /**
     * Set the current value of the stopwatch.  Use -100 to disable the stopwatch, otherwise it will
     * start counting immediately.
     *
     * @param newVal The new value of the stopwatch
     */
    public void setStopwatch(float newVal) {
        this.mGame.mManager.mStopWatchProgress = newVal;
    }

    /**
     * Add a button that pauses the game (via a single tap) by causing a PauseScene to be
     * displayed. Note that you must configure a PauseScene, or pressing this button will cause your
     * game to crash.
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible button
     * @param action  The action to run in response to a tap
     */
    public SceneActor addTapControl(float x, float y, float width, float height, String imgName,
                                    final TouchEventHandler action) {
        SceneActor c = new SceneActor(mGame.mManager.mHud, imgName, width, height);
        c.setBoxPhysics(BodyDef.BodyType.StaticBody, x, y);
        c.mTapHandler = action;
        action.mSource = c;
        mGame.mManager.mHud.addActor(c, 0);
        return c;
    }

    /**
     * An action to pause the game.  This action can be used as the action taken on a Control tap.
     */
    public TouchEventHandler PauseAction = new TouchEventHandler() {
        @Override
        public boolean go(float x, float y) {
            getPauseScene().show();
            return true;
        }
    };

    /**
     * Create an action that makes a hero jump.  This action can be used as the action taken on a
     * Control tap.
     *
     * @param hero The hero who we want to jump
     * @return The action object
     */
    public TouchEventHandler JumpAction(final Hero hero) {
        return new TouchEventHandler() {
            @Override
            public boolean go(float x, float y) {
                hero.jump();
                return true;
            }
        };
    }

    /**
     * Create an action that makes a hero throw a projectile
     *
     * @param hero      The hero who should throw the projectile
     * @param offsetX   specifies the x distance between the bottom left of the projectile and the
     *                  bottom left of the hero throwing the projectile
     * @param offsetY   specifies the y distance between the bottom left of the projectile and the
     *                  bottom left of the hero throwing the projectile
     * @param velocityX The X velocity of the projectile when it is thrown
     * @param velocityY The Y velocity of the projectile when it is thrown
     * @return The action object
     */
    public TouchEventHandler ThrowFixedAction(final Hero hero, final float offsetX,
                                              final float offsetY, final float velocityX,
                                              final float velocityY) {
        return new TouchEventHandler() {
            public boolean go(float x, float y) {
                mGame.mManager.mWorld.mProjectilePool.throwFixed(hero, offsetX, offsetY, velocityX,
                        velocityY);
                return true;
            }
        };
    }

    /**
     * Create an action that makes a hero throw a projectile in a direction that relates to how the
     * screen was touched
     *
     * @param hero    The hero who should throw the projectile
     * @param offsetX specifies the x distance between the bottom left of the projectile and the
     *                bottom left of the hero throwing the projectile
     * @param offsetY specifies the y distance between the bottom left of the projectile and the
     *                bottom left of the hero throwing the projectile
     * @return The action object
     */
    public TouchEventHandler ThrowDirectionalAction(final Hero hero, final float offsetX,
                                                    final float offsetY) {
        return new TouchEventHandler() {
            public boolean go(float worldX, float worldY) {
                mGame.mManager.mWorld.mProjectilePool.throwAt(hero.mBody.getPosition().x,
                        hero.mBody.getPosition().y, worldX, worldY, hero, offsetX, offsetY);
                return true;
            }
        };
    }

    /**
     * Create an action that makes the screen zoom out
     *
     * @param maxZoom The maximum zoom factor to allow
     * @return The action object
     */
    public TouchEventHandler ZoomOutAction(final float maxZoom) {
        return new TouchEventHandler() {
            public boolean go(float x, float y) {
                float curzoom = mGame.mManager.mWorld.mCamera.zoom;
                if (curzoom < maxZoom) {
                    mGame.mManager.mWorld.mCamera.zoom *= 2;
                    mGame.mManager.mBackground.mBgCam.zoom *= 2;
                    mGame.mManager.mForeground.mBgCam.zoom *= 2;
                }
                return true;
            }
        };
    }

    /**
     * Create an action that makes the screen zoom in
     *
     * @param minZoom The minimum zoom factor to allow
     * @return The action object
     */
    public TouchEventHandler ZoomInAction(final float minZoom) {
        return new TouchEventHandler() {
            public boolean go(float x, float y) {
                float curzoom = mGame.mManager.mWorld.mCamera.zoom;
                if (curzoom > minZoom) {
                    mGame.mManager.mWorld.mCamera.zoom /= 2;
                    mGame.mManager.mBackground.mBgCam.zoom /= 2;
                    mGame.mManager.mForeground.mBgCam.zoom /= 2;
                }
                return true;
            }
        };
    }

    /**
     * Add a button that has one behavior while it is being pressed, and another when it is released
     *
     * @param x               The X coordinate of the bottom left corner
     * @param y               The Y coordinate of the bottom left corner
     * @param width           The width of the image
     * @param height          The height of the image
     * @param imgName         The name of the image to display.  Use "" for an invisible button
     * @param whileDownAction The action to execute, repeatedly, whenever the button is pressed
     * @param onUpAction      The action to execute once any time the button is released
     * @return The control, so we can do more with it as needed.
     */
    public SceneActor addToggleButton(int x, int y, int width, int height, String imgName,
                                      final LolAction whileDownAction, final LolAction onUpAction) {
        SceneActor c = new SceneActor(mGame.mManager.mHud, imgName, width, height);
        c.setBoxPhysics(BodyDef.BodyType.StaticBody, x, y);
        // initially the down action is not active
        whileDownAction.mIsActive = false;
        // set up the toggle behavior
        c.mToggleHandler = new ToggleEventHandler() {
            public boolean go(boolean isUp, float x, float y) {
                if (isUp) {
                    whileDownAction.mIsActive = false;
                    if (onUpAction != null)
                        onUpAction.go();
                } else {
                    whileDownAction.mIsActive = true;
                }
                return true;
            }
        };
        // Put the control and events in the appropriate lists
        c.mToggleHandler.mSource = c;
        mGame.mManager.mHud.addActor(c, 0);
        mGame.mManager.mHud.mToggleControls.add(c);
        mGame.mManager.mWorld.mRepeatEvents.add(whileDownAction);
        return c;
    }

    /**
     * Create an action for moving an actor in the X direction.  This action can be used by a
     * Control.
     *
     * @param actor The actor to move
     * @param xRate The rate at which the actor should move in the X direction (negative values are
     *              allowed)
     * @return The action
     */
    public LolAction makeXMotionAction(final WorldActor actor, final float xRate) {
        return new LolAction() {
            @Override
            public void go() {
                Vector2 v = actor.mBody.getLinearVelocity();
                v.x = xRate;
                actor.updateVelocity(v.x, v.y);
            }
        };
    }

    /**
     * Create an action for moving an actor in the Y direction.  This action can be used by a
     * Control.
     *
     * @param actor The actor to move
     * @param yRate The rate at which the actor should move in the Y direction (negative values are
     *              allowed)
     * @return The action
     */
    public LolAction makeYMotionAction(final WorldActor actor, final float yRate) {
        return new LolAction() {
            @Override
            public void go() {
                Vector2 v = actor.mBody.getLinearVelocity();
                v.y = yRate;
                actor.updateVelocity(v.x, v.y);
            }
        };
    }

    /**
     * Create an action for moving an actor in the X and Y directions.  This action can be used by a
     * Control.
     *
     * @param actor The actor to move
     * @param xRate The rate at which the actor should move in the X direction (negative values are
     *              allowed)
     * @param yRate The rate at which the actor should move in the Y direction (negative values are
     *              allowed)
     * @return The action
     */
    public LolAction makeXYMotionAction(final WorldActor actor, final float xRate,
                                        final float yRate) {
        return new LolAction() {
            @Override
            public void go() {
                actor.updateVelocity(xRate, yRate);
            }
        };
    }

    /**
     * Create an action for moving an actor in the X and Y directions, with dampening on release.
     * This action can be used by a Control.
     *
     * @param actor     The actor to move
     * @param xRate     The rate at which the actor should move in the X direction (negative values
     *                  are allowed)
     * @param yRate     The rate at which the actor should move in the Y direction (negative values
     *                  are allowed)
     * @param dampening The dampening factor
     * @return The action
     */
    public LolAction makeXYDampenedMotionAction(final WorldActor actor, final float xRate,
                                                final float yRate, final float dampening) {
        return new LolAction() {
            @Override
            public void go() {
                actor.updateVelocity(xRate, yRate);
                actor.mBody.setLinearDamping(dampening);
            }
        };
    }

    /**
     * Create an action for making a hero either start or stop crawling
     *
     * @param hero       The hero to control
     * @param crawlState True to start crawling, false to stop
     * @return The action
     */
    public LolAction makeCrawlToggle(final Hero hero, final boolean crawlState) {
        return new LolAction() {
            @Override
            public void go() {
                if (crawlState)
                    hero.crawlOn();
                else
                    hero.crawlOff();
            }
        };
    }

    /**
     * Create an action for making a hero rotate
     *
     * @param hero The hero to rotate
     * @param rate Amount of rotation to apply to the hero on each press
     * @return The action
     */
    public LolAction makeRotator(final Hero hero, final float rate) {
        return new LolAction() {
            @Override
            public void go() {
                hero.increaseRotation(rate);
            }
        };
    }

    /**
     * Create an action for making a hero throw a projectile
     *
     * @param hero       The hero who should throw the projectile
     * @param milliDelay A delay between throws, so that holding doesn't lead to too many throws at
     *                   once
     * @param offsetX    specifies the x distance between the bottom left of the projectile and the
     *                   bottom left of the hero throwing the projectile
     * @param offsetY    specifies the y distance between the bottom left of the projectile and the
     *                   bottom left of the hero throwing the projectile
     * @param velocityX  The X velocity of the projectile when it is thrown
     * @param velocityY  The Y velocity of the projectile when it is thrown
     * @return The action object
     */
    public LolAction makeRepeatThrow(final Hero hero, final int milliDelay, final float offsetX,
                                     final float offsetY, final float velocityX,
                                     final float velocityY) {
        return new LolAction() {
            long mLastThrow;

            @Override
            public void go() {
                long now = System.currentTimeMillis();
                if (mLastThrow + milliDelay < now) {
                    mLastThrow = now;
                    mGame.mManager.mWorld.mProjectilePool.throwFixed(hero, offsetX, offsetY,
                            velocityX, velocityY);
                }
            }
        };
    }

    /**
     * The default behavior for throwing is to throw in a straight line. If we instead desire that
     * the projectiles have some sort of aiming to them, we need to use this method, which throws
     * toward where the screen was pressed
     * <p>
     * Note: you probably want to use an invisible button that covers the screen...
     *
     * @param x          The X coordinate of the bottom left corner (in pixels)
     * @param y          The Y coordinate of the bottom left corner (in pixels)
     * @param width      The width of the image
     * @param height     The height of the image
     * @param imgName    The name of the image to display. Use "" for an invisible button
     * @param h          The hero who should throw the projectile
     * @param milliDelay A delay between throws, so that holding doesn't lead to too many throws at
     *                   once
     * @param offsetX    specifies the x distance between the bottom left of the projectile and the
     *                   bottom left of the hero throwing the projectile
     * @param offsetY    specifies the y distance between the bottom left of the projectile and the
     *                   bottom left of the hero throwing the projectile
     * @return The button that was created
     */
    public SceneActor addDirectionalThrowButton(int x, int y, int width, int height, String imgName,
                                                final Hero h, final long milliDelay,
                                                final float offsetX, final float offsetY) {
        final SceneActor c = new SceneActor(mGame.mManager.mHud, imgName, width, height);
        c.setBoxPhysics(BodyDef.BodyType.StaticBody, x, y);
        final Vector2 v = new Vector2();
        c.mToggleHandler = new ToggleEventHandler() {
            public boolean go(boolean isUp, float worldX, float worldY) {
                if (isUp) {
                    isHolding = false;
                } else {
                    isHolding = true;
                    v.x = worldX;
                    v.y = worldY;
                }
                return true;
            }
        };
        c.mPanHandler = new PanEventHandler() {
            public boolean go(float worldX, float worldY, float deltaX, float deltaY) {
                if (c.mToggleHandler.isHolding) {
                    v.x = worldX;
                    v.y = worldY;
                }
                return c.mToggleHandler.isHolding;
            }
        };
        mGame.mManager.mHud.addActor(c, 0);
        // on toggle, we start or stop throwing; on pan, we change throw direction
        mGame.mManager.mHud.mToggleControls.add(c);

        c.mToggleHandler.mSource = c;
        c.mPanHandler.mSource = c;

        mGame.mManager.mWorld.mRepeatEvents.add(new LolAction() {
            long mLastThrow;

            @Override
            public void go() {
                if (c.mToggleHandler.isHolding) {
                    long now = System.currentTimeMillis();
                    if (mLastThrow + milliDelay < now) {
                        mLastThrow = now;
                        mGame.mManager.mWorld.mProjectilePool.throwAt(h.mBody.getPosition().x,
                                h.mBody.getPosition().y, v.x, v.y, h, offsetX, offsetY);
                    }
                }
            }
        });
        return c;
    }


    /**
     * Allow panning to view more of the screen than is currently visible
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible button
     * @return The button that was created
     */
    public SceneActor addPanControl(int x, int y, int width, int height, String imgName) {
        final SceneActor c = new SceneActor(mGame.mManager.mHud, imgName, width, height);
        c.setBoxPhysics(BodyDef.BodyType.StaticBody, x, y);
        c.mPanStopHandler = new TouchEventHandler() {
            /**
             * Handle a pan stop event by restoring the chase actor, if there was one
             */
            public boolean go(float worldX, float worldY) {
                setCameraChase((WorldActor) mSource);
                mSource = null;
                return true;
            }
        };
        c.mPanHandler = new PanEventHandler() {
            public boolean go(float worldX, float worldY, float deltaX, float deltaY) {
                if (mGame.mManager.mWorld.mChaseActor != null) {
                    c.mPanStopHandler.mSource = mGame.mManager.mWorld.mChaseActor;
                    mGame.mManager.mWorld.mChaseActor = null;
                }
                OrthographicCamera cam = mGame.mManager.mWorld.mCamera;
                Vector2 camBound = mGame.mManager.mWorld.mCamBound;
                float x = cam.position.x - deltaX * .1f * cam.zoom;
                float y = cam.position.y + deltaY * .1f * cam.zoom;
                // if x or y is too close to MAX,MAX, stick with max acceptable values
                if (x > camBound.x - mConfig.mWidth * cam.zoom / mConfig.mPixelMeterRatio / 2)
                    x = camBound.x - mConfig.mWidth * cam.zoom / mConfig.mPixelMeterRatio / 2;
                if (y > camBound.y - mConfig.mHeight * cam.zoom / mConfig.mPixelMeterRatio / 2)
                    y = camBound.y - mConfig.mHeight * cam.zoom / mConfig.mPixelMeterRatio / 2;

                // if x or y is too close to 0,0, stick with minimum acceptable values
                //
                // NB: we do MAX before MIN, so that if we're zoomed out, we show extra space at the
                // top instead of the bottom
                if (x < mConfig.mWidth * cam.zoom / mConfig.mPixelMeterRatio / 2)
                    x = mConfig.mWidth * cam.zoom / mConfig.mPixelMeterRatio / 2;
                if (y < mConfig.mHeight * cam.zoom / mConfig.mPixelMeterRatio / 2)
                    y = mConfig.mHeight * cam.zoom / mConfig.mPixelMeterRatio / 2;

                // update the camera position
                cam.position.set(x, y, 0);
                return true;
            }
        };
        c.mPanHandler.mSource = c;
        c.mPanStopHandler.mSource = c;
        mGame.mManager.mHud.addActor(c, 0);
        return c;
    }

    /**
     * Allow pinch-to-zoom
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible button
     * @param maxZoom The maximum zoom (out) factor. 8 is usually a good choice.
     * @param minZoom The minimum zoom (int) factor. .25f is usually a good choice.
     * @return The button that was created
     */
    public SceneActor addPinchZoomControl(float x, float y, float width, float height,
                                          String imgName, final float maxZoom,
                                          final float minZoom) {
        final SceneActor c = new SceneActor(mGame.mManager.mHud, imgName, width, height);
        c.setBoxPhysics(BodyDef.BodyType.StaticBody, x, y);
        c.mDownHandler = new TouchEventHandler() {
            public boolean go(float worldX, float worldY) {
                // this handler is being used for up/down, so we can safely use the deltaX as a way
                // of storing the last zoom value
                c.setInfoInt((int) (mGame.mManager.mWorld.mCamera.zoom * 1000));
                return false;
            }
        };
        c.mZoomHandler = new TouchEventHandler() {
            public boolean go(float initialDistance, float distance) {
                float ratio = initialDistance / distance;
                float newZoom = ((float) c.getInfoInt()) / 1000 * ratio;
                if (newZoom > minZoom && newZoom < maxZoom)
                    mGame.mManager.mWorld.mCamera.zoom = newZoom;
                return true;
            }
        };
        mGame.mManager.mHud.addActor(c, 0);
        return c;
    }

    /**
     * Add an image to the heads-up display. Touching the image has no effect
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible button
     * @return The image that was created
     */
    public SceneActor addImage(int x, int y, int width, int height, String imgName) {
        final SceneActor c = new SceneActor(mGame.mManager.mHud, imgName, width, height);
        c.setBoxPhysics(BodyDef.BodyType.StaticBody, x, y);
        mGame.mManager.mHud.addActor(c, 0);
        return c;
    }

    /**
     * Add a control with callbacks for down, up, and pan
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible button
     * @param upCB    The callback to run when the Control is released
     * @param dnCB    The callback to run when the Control is pressed
     * @param mvCB    The callback to run when there is a finger move (pan) on the Control
     * @return The button that was created
     */
    // TODO: we never test this code!
    public SceneActor addPanCallbackControl(float x, float y, float width, float height,
                                            String imgName, final TouchEventHandler upCB,
                                            final TouchEventHandler dnCB,
                                            final TouchEventHandler mvCB) {
        final SceneActor c = new SceneActor(mGame.mManager.mHud, imgName, width, height);
        c.setBoxPhysics(BodyDef.BodyType.StaticBody, x, y);
        // Pan only consists of pan-stop and pan events. That means we can't capture a down-press or
        // up-press that isn't also involved in a move.  To overcome this limitation, we'll make
        // this BOTH a pan control and a toggle control
        c.mToggleHandler = new ToggleEventHandler() {
            public boolean go(boolean isUp, float worldX, float worldY) {
                // up event
                if (isUp) {
                    upCB.go(worldX, worldY);
                    isHolding = false;
                }
                // down event
                else {
                    isHolding = true;
                    dnCB.go(worldX, worldY);
                }
                // toggle state
                isHolding = !isUp;
                return true;
            }
        };
        c.mPanHandler = new PanEventHandler() {
            public boolean go(float worldX, float worldY, float deltaX, float deltaY) {
                // force a down event, if we didn't get one
                if (!c.mToggleHandler.isHolding) {
                    c.mToggleHandler.go(false, worldX, worldY);
                    return true;
                }
                // pan event
                mvCB.go(worldX, worldY);
                return true;
            }
        };
        c.mPanStopHandler = new TouchEventHandler() {
            public boolean go(float worldX, float worldY) {
                // force an up event?
                if (c.mToggleHandler.isHolding) {
                    c.mToggleHandler.go(true, worldX, worldY);
                    return true;
                }
                return false;
            }
        };
        c.mPanHandler.mSource = c;
        c.mPanStopHandler.mSource = c;
        c.mToggleHandler.mSource = c;
        mGame.mManager.mHud.addActor(c, 0);
        mGame.mManager.mHud.mToggleControls.add(c);
        return c;
    }

    /**
     * Look up a fact that was stored for the current level. If no such fact exists, defaultVal will
     * be returned.
     *
     * @param factName   The name used to store the fact
     * @param defaultVal The default value to use if the fact cannot be found
     * @return The integer value corresponding to the last value stored
     */
    public int getLevelFact(String factName, int defaultVal) {
        Integer i = mGame.mManager.mWorld.mLevelFacts.get(factName);
        if (i == null) {
            Lol.message(mConfig, "ERROR", "Error retreiving level fact '" + factName + "'");
            return defaultVal;
        }
        return i;
    }

    /**
     * Save a fact about the current level. If the factName has already been used for this level,
     * the new value will overwrite the old.
     *
     * @param factName  The name for the fact being saved
     * @param factValue The integer value that is the fact being saved
     */
    public void putLevelFact(String factName, int factValue) {
        mGame.mManager.mWorld.mLevelFacts.put(factName, factValue);
    }

    /**
     * Look up a fact that was stored for the current game session. If no such fact exists, -1 will
     * be returned.
     *
     * @param factName   The name used to store the fact
     * @param defaultVal The default value to use if the fact cannot be found
     * @return The integer value corresponding to the last value stored
     */
    public int getSessionFact(String factName, int defaultVal) {
        Integer i = mGame.mManager.mSessionFacts.get(factName);
        if (i == null) {
            Lol.message(mConfig, "ERROR", "Error retreiving level fact '" + factName + "'");
            return defaultVal;
        }
        return i;
    }

    /**
     * Save a fact about the current game session. If the factName has already been used for this
     * game session, the new value will overwrite the old.
     *
     * @param factName  The name for the fact being saved
     * @param factValue The integer value that is the fact being saved
     */
    public void putSessionFact(String factName, int factValue) {
        mGame.mManager.mSessionFacts.put(factName, factValue);
    }

    /**
     * Look up a fact that was stored for the current game session. If no such fact exists,
     * defaultVal will be returned.
     *
     * @param factName   The name used to store the fact
     * @param defaultVal The value to return if the fact does not exist
     * @return The integer value corresponding to the last value stored
     */
    public int getGameFact(String factName, int defaultVal) {
        return Lol.getGameFact(mConfig, factName, defaultVal);
    }

    /**
     * Save a fact about the current game session. If the factName has already been used for this
     * game session, the new value will overwrite the old.
     *
     * @param factName  The name for the fact being saved
     * @param factValue The integer value that is the fact being saved
     */
    public void putGameFact(String factName, int factValue) {
        Lol.putGameFact(mConfig, factName, factValue);
    }

    /**
     * Look up an WorldActor that was stored for the current level. If no such WorldActor exists,
     * null will be returned.
     *
     * @param actorName The name used to store the WorldActor
     * @return The last WorldActor stored with this name
     */
    public WorldActor getLevelActor(String actorName) {
        WorldActor actor = mGame.mManager.mWorld.mLevelActors.get(actorName);
        if (actor == null) {
            Lol.message(mConfig, "ERROR", "Error retreiving level fact '" + actorName + "'");
            return null;
        }
        return actor;
    }

    /**
     * Save a WorldActor from the current level. If the actorName has already been used for this
     * level, the new value will overwrite the old.
     *
     * @param actorName The name for the WorldActor being saved
     * @param actor     The WorldActor that is the fact being saved
     */
    public void putLevelActor(String actorName, WorldActor actor) {
        mGame.mManager.mWorld.mLevelActors.put(actorName, actor);
    }

    /**
     * Set the background color for the current level
     *
     * @param color The color, formated as #RRGGBB
     */
    public void setBackgroundColor(String color) {
        mGame.mManager.mBackground.mColor = Color.valueOf(color);
    }

    /**
     * Add a picture that may repeat in the X dimension
     *
     * @param xSpeed  Speed that the picture seems to move in the X direction. "1" is the same speed
     *                as the hero; "0" is not at all; ".5f" is at half the hero's speed
     * @param ySpeed  Speed that the picture seems to move in the Y direction. "1" is the same speed
     *                as the hero; "0" is not at all; ".5f" is at half the hero's speed
     * @param imgName The name of the image file to use as the background
     * @param yOffset The default is to draw the image at y=0. This field allows the picture to be
     *                moved up or down.
     * @param width   The width of the image being used as a background layer
     * @param height  The height of the image being used as a background layer
     */
    public void addHorizontalBackgroundLayer(float xSpeed, float ySpeed, String imgName,
                                             float yOffset, float width, float height) {
        ParallaxLayer pl = new ParallaxLayer(xSpeed, ySpeed,
                mMedia.getImage(imgName), 0, yOffset
                * mConfig.mPixelMeterRatio, width, height);
        pl.mXRepeat = xSpeed != 0;
        mGame.mManager.mBackground.mLayers.add(pl);
    }

    /**
     * Add a picture that may repeat in the X dimension, and which moves automatically
     *
     * @param xSpeed  Speed, in pixels per second
     * @param imgName The name of the image file to use as the background
     * @param yOffset The default is to draw the image at y=0. This field allows the picture to be
     *                moved up or down.
     * @param width   The width of the image being used as a background layer
     * @param height  The height of the image being used as a background layer
     */
    public void addHorizontalAutoBackgroundLayer(float xSpeed, String imgName,
                                                 float yOffset, float width, float height) {
        ParallaxLayer pl = new ParallaxLayer(xSpeed, 0,
                mMedia.getImage(imgName), 0, yOffset
                * mConfig.mPixelMeterRatio, width, height);
        pl.mAutoX = true;
        pl.mXRepeat = xSpeed != 0;
        mGame.mManager.mBackground.mLayers.add(pl);
    }

    /**
     * Add a picture that may repeat in the Y dimension
     *
     * @param xSpeed  Speed that the picture seems to move in the X direction. "1" is the same speed
     *                as the hero; "0" is not at all; ".5f" is at half the hero's speed
     * @param ySpeed  Speed that the picture seems to move in the Y direction. "1" is the same speed
     *                as the hero; "0" is not at all; ".5f" is at half the hero's speed
     * @param imgName The name of the image file to use as the background
     * @param xOffset The default is to draw the image at x=0. This field allows the picture to be
     *                moved left or right.
     * @param width   The width of the image being used as a background layer
     * @param height  The height of the image being used as a background layer
     */
    public void addVerticalBackgroundLayer(float xSpeed, float ySpeed, String imgName,
                                           float xOffset, float width, float height) {
        ParallaxLayer pl = new ParallaxLayer(xSpeed, ySpeed,
                mMedia.getImage(imgName),
                xOffset * mConfig.mPixelMeterRatio, 0, width, height);
        pl.mYRepeat = ySpeed != 0;
        mGame.mManager.mBackground.mLayers.add(pl);
    }

    /**
     * Create a particle effect system
     *
     * @param filename The file holding the particle definition
     * @param zIndex   The z index of the particle system.
     * @param x        The x coordinate of the starting point of the particle system
     * @param y        The y coordinate of the starting point of the particle system
     * @return the Effect, so that it can be modified further
     */
    public Effect makeParticleSystem(String filename, int zIndex, float x, float y) {
        Effect e = new Effect();

        // create the particle effect system.
        ParticleEffect pe = new ParticleEffect();
        pe.load(Gdx.files.internal(filename), Gdx.files.internal(""));
        e.mParticleEffect = pe;

        // update the effect's coordinates to reflect world coordinates
        pe.getEmitters().first().setPosition(x, y);

        // NB: we pretend effects are Actors, so that we can have them in front of or behind Actors
        mGame.mManager.mWorld.addActor(e, zIndex);

        // start emitting particles
        pe.start();
        return e;
    }

    /**
     * Add a picture that may repeat in the X dimension
     *
     * @param xSpeed  Speed that the picture seems to move in the X direction. "1" is the same speed
     *                as the hero; "0" is not at all; ".5f" is at half the hero's speed
     * @param ySpeed  Speed that the picture seems to move in the Y direction. "1" is the same speed
     *                as the hero; "0" is not at all; ".5f" is at half the hero's speed
     * @param imgName The name of the image file to use as the foreground
     * @param yOffset The default is to draw the image at y=0. This field allows the picture to be
     *                moved up or down.
     * @param width   The width of the image being used as a foreground layer
     * @param height  The height of the image being used as a foreground layer
     */
    public void addHorizontalForegroundLayer(float xSpeed, float ySpeed, String imgName,
                                             float yOffset, float width, float height) {
        ParallaxLayer pl = new ParallaxLayer(xSpeed, ySpeed,
                mMedia.getImage(imgName), 0, yOffset
                * mConfig.mPixelMeterRatio, width, height);
        pl.mXRepeat = xSpeed != 0;
        mGame.mManager.mForeground.mLayers.add(pl);
    }

    /**
     * Add a picture that may repeat in the X dimension, and which moves automatically
     *
     * @param xSpeed  Speed, in pixels per second
     * @param imgName The name of the image file to use as the foreground
     * @param yOffset The default is to draw the image at y=0. This field allows the picture to be
     *                moved up or down.
     * @param width   The width of the image being used as a foreground layer
     * @param height  The height of the image being used as a foreground layer
     */
    public void addHorizontalAutoForegroundLayer(float xSpeed, String imgName,
                                                 float yOffset, float width, float height) {
        ParallaxLayer pl = new ParallaxLayer(xSpeed, 0,
                mMedia.getImage(imgName), 0, yOffset
                * mConfig.mPixelMeterRatio, width, height);
        pl.mAutoX = true;
        pl.mXRepeat = xSpeed != 0;
        mGame.mManager.mForeground.mLayers.add(pl);
    }

    /**
     * Add a picture that may repeat in the Y dimension
     *
     * @param xSpeed  Speed that the picture seems to move in the Y direction. "1" is the same speed
     *                as the hero; "0" is not at all; ".5f" is at half the hero's speed
     * @param ySpeed  Speed that the picture seems to move in the Y direction. "1" is the same speed
     *                as the hero; "0" is not at all; ".5f" is at half the hero's speed
     * @param imgName The name of the image file to use as the foreground
     * @param xOffset The default is to draw the image at x=0. This field allows the picture to be
     *                moved left or right.
     * @param width   The width of the image being used as a foreground layer
     * @param height  The height of the image being used as a foreground layer
     */
    public void addVerticalForegroundLayer(float xSpeed, float ySpeed, String imgName,
                                           float xOffset, float width, float height) {
        ParallaxLayer pl = new ParallaxLayer(xSpeed, ySpeed,
                mMedia.getImage(imgName),
                xOffset * mConfig.mPixelMeterRatio, 0, width, height);
        pl.mYRepeat = ySpeed != 0;
        mGame.mManager.mForeground.mLayers.add(pl);
    }

    /**
     * Get the LoseScene that is configured for the current level, or create a blank one if none
     * exists.
     *
     * @return The current LoseScene
     */
    public QuickScene getLoseScene() {
        return mGame.mManager.mLoseScene;
    }

    /**
     * Get the PreScene that is configured for the current level, or create a blank one if none
     * exists.
     *
     * @return The current PreScene
     */
    public QuickScene getPreScene() {
        mGame.mManager.mPreScene.mVisible = true;
        mGame.mManager.mPreScene.suspendClock();
        return mGame.mManager.mPreScene;
    }

    /**
     * Get the PauseScene that is configured for the current level, or create a blank one if none
     * exists.
     *
     * @return The current PauseScene
     */
    public QuickScene getPauseScene() {
        return mGame.mManager.mPauseScene;
    }

    /**
     * Get the WinScene that is configured for the current level, or create a blank one if none
     * exists.
     *
     * @return The current WinScene
     */
    public QuickScene getWinScene() {
        return mGame.mManager.mWinScene;
    }

    /**
     * Make an enemy that has an underlying rectangular shape.
     *
     * @param x       The X coordinate of the bottom left corner
     * @param y       The Y coordinate of the bottom right corner
     * @param width   The width of the enemy
     * @param height  The height of the enemy
     * @param imgName The name of the image to display
     * @return The enemy, so that it can be modified further
     */
    public Enemy makeEnemyAsBox(float x, float y, float width, float height, String imgName) {
        Enemy e = new Enemy(mGame, mGame.mManager.mWorld, width, height, imgName);
        mGame.mManager.mEnemiesCreated++;
        e.setBoxPhysics(BodyDef.BodyType.StaticBody, x, y);
        mGame.mManager.mWorld.addActor(e, 0);
        return e;
    }

    /**
     * Draw an enemy with an underlying polygon shape
     *
     * @param x       X coordinate of the bottom left corner
     * @param y       Y coordinate of the bottom left corner
     * @param width   Width of the obstacle
     * @param height  Height of the obstacle
     * @param imgName Name of image file to use
     * @param verts   Up to 16 coordinates representing the vertexes of this polygon, listed as
     *                x0,y0,x1,y1,x2,y2,...
     * @return The enemy, so that it can be further modified
     */
    public Enemy makeEnemyAsPolygon(float x, float y, float width, float height, String imgName,
                                    float... verts) {
        Enemy e = new Enemy(mGame, mGame.mManager.mWorld, width, height, imgName);
        mGame.mManager.mEnemiesCreated++;
        e.setPolygonPhysics(BodyDef.BodyType.StaticBody, x, y, verts);
        mGame.mManager.mWorld.addActor(e, 0);
        return e;
    }

    /**
     * Make an enemy that has an underlying circular shape.
     *
     * @param x       The X coordinate of the bottom left corner
     * @param y       The Y coordinate of the bottom right corner
     * @param width   The width of the enemy
     * @param height  The height of the enemy
     * @param imgName The name of the image to display
     * @return The enemy, so that it can be modified further
     */
    public Enemy makeEnemyAsCircle(float x, float y, float width, float height, String imgName) {
        float radius = Math.max(width, height);
        Enemy e = new Enemy(mGame, mGame.mManager.mWorld, radius, radius, imgName);
        mGame.mManager.mEnemiesCreated++;
        e.setCirclePhysics(BodyDef.BodyType.StaticBody, x, y, radius / 2);
        mGame.mManager.mWorld.addActor(e, 0);
        return e;
    }

    /**
     * Make a destination that has an underlying rectangular shape.
     *
     * @param x       The X coordinate of the bottom left corner
     * @param y       The Y coordinate of the bottom right corner
     * @param width   The width of the destination
     * @param height  The height of the destination
     * @param imgName The name of the image to display
     * @return The destination, so that it can be modified further
     */
    public Destination makeDestinationAsBox(float x, float y, float width, float height,
                                            String imgName) {
        Destination d = new Destination(mGame, mGame.mManager.mWorld, width, height, imgName);
        d.setBoxPhysics(BodyDef.BodyType.StaticBody, x, y);
        d.setCollisionsEnabled(false);
        mGame.mManager.mWorld.addActor(d, 0);
        return d;
    }

    /**
     * Draw a destination with an underlying polygon shape
     *
     * @param x       X coordinate of the bottom left corner
     * @param y       Y coordinate of the bottom left corner
     * @param width   Width of the obstacle
     * @param height  Height of the obstacle
     * @param imgName Name of image file to use
     * @param verts   Up to 16 coordinates representing the vertexes of this polygon, listed as
     *                x0,y0,x1,y1,x2,y2,...
     * @return The destination, so that it can be further modified
     */
    public Destination makeDestinationAsPolygon(float x, float y, float width, float height,
                                                String imgName, float... verts) {
        Destination d = new Destination(mGame, mGame.mManager.mWorld, width, height, imgName);
        d.setPolygonPhysics(BodyDef.BodyType.StaticBody, x, y, verts);
        d.setCollisionsEnabled(false);
        mGame.mManager.mWorld.addActor(d, 0);
        return d;
    }

    /**
     * Make a destination that has an underlying circular shape.
     *
     * @param x       The X coordinate of the bottom left corner
     * @param y       The Y coordinate of the bottom right corner
     * @param width   The width of the destination
     * @param height  The height of the destination
     * @param imgName The name of the image to display
     * @return The destination, so that it can be modified further
     */
    public Destination makeDestinationAsCircle(float x, float y, float width, float height,
                                               String imgName) {
        float radius = Math.max(width, height);
        Destination d = new Destination(mGame, mGame.mManager.mWorld, radius, radius, imgName);
        d.setCirclePhysics(BodyDef.BodyType.StaticBody, x, y, radius / 2);
        d.setCollisionsEnabled(false);
        mGame.mManager.mWorld.addActor(d, 0);
        return d;
    }

    /**
     * Draw an obstacle with an underlying box shape
     *
     * @param x       X coordinate of the bottom left corner
     * @param y       Y coordinate of the bottom left corner
     * @param width   Width of the obstacle
     * @param height  Height of the obstacle
     * @param imgName Name of image file to use
     * @return The obstacle, so that it can be further modified
     */
    public Obstacle makeObstacleAsBox(float x, float y, float width, float height, String imgName) {
        Obstacle o = new Obstacle(mGame, mGame.mManager.mWorld, width, height, imgName);
        o.setBoxPhysics(BodyDef.BodyType.StaticBody, x, y);
        mGame.mManager.mWorld.addActor(o, 0);
        return o;
    }

    /**
     * Draw an obstacle with an underlying polygon shape
     *
     * @param x       X coordinate of the bottom left corner
     * @param y       Y coordinate of the bottom left corner
     * @param width   Width of the obstacle
     * @param height  Height of the obstacle
     * @param imgName Name of image file to use
     * @param verts   Up to 16 coordinates representing the vertexes of this polygon, listed as
     *                x0,y0,x1,y1,x2,y2,...
     * @return The obstacle, so that it can be further modified
     */
    public Obstacle makeObstacleAsPolygon(float x, float y, float width, float height,
                                          String imgName, float... verts) {
        Obstacle o = new Obstacle(mGame, mGame.mManager.mWorld, width, height, imgName);
        o.setPolygonPhysics(BodyDef.BodyType.StaticBody, x, y, verts);
        mGame.mManager.mWorld.addActor(o, 0);
        return o;
    }

    /**
     * Draw an obstacle with an underlying circle shape
     *
     * @param x       X coordinate of the bottom left corner
     * @param y       Y coordinate of the bottom left corner
     * @param width   Width of the obstacle
     * @param height  Height of the obstacle
     * @param imgName Name of image file to use
     * @return The obstacle, so that it can be further modified
     */
    public Obstacle makeObstacleAsCircle(float x, float y, float width, float height,
                                         String imgName) {
        float radius = Math.max(width, height);
        Obstacle o = new Obstacle(mGame, mGame.mManager.mWorld, width, height, imgName);
        o.setCirclePhysics(BodyDef.BodyType.StaticBody, x, y, radius / 2);
        mGame.mManager.mWorld.addActor(o, 0);
        return o;
    }

    /**
     * Draw a goodie with an underlying box shape, and a default score of [1,0,0,0]
     *
     * @param x       X coordinate of bottom left corner
     * @param y       Y coordinate of bottom left corner
     * @param width   Width of the image
     * @param height  Height of the image
     * @param imgName Name of image file to use
     * @return The goodie, so that it can be further modified
     */
    public Goodie makeGoodieAsBox(float x, float y, float width, float height, String imgName) {
        Goodie g = new Goodie(mGame, mGame.mManager.mWorld, width, height, imgName);
        g.setBoxPhysics(BodyDef.BodyType.StaticBody, x, y);
        g.setCollisionsEnabled(false);
        mGame.mManager.mWorld.addActor(g, 0);
        return g;
    }

    /**
     * Draw a goodie with an underlying circle shape, and a default score of [1,0,0,0]
     *
     * @param x       X coordinate of bottom left corner
     * @param y       Y coordinate of bottom left corner
     * @param width   Width of the image
     * @param height  Height of the image
     * @param imgName Name of image file to use
     * @return The goodie, so that it can be further modified
     */
    public Goodie makeGoodieAsCircle(float x, float y, float width, float height, String imgName) {
        float radius = Math.max(width, height);
        Goodie g = new Goodie(mGame, mGame.mManager.mWorld, width, height, imgName);
        g.setCirclePhysics(BodyDef.BodyType.StaticBody, x, y, radius / 2);
        g.setCollisionsEnabled(false);
        mGame.mManager.mWorld.addActor(g, 0);
        return g;
    }

    /**
     * Draw a goodie with an underlying polygon shape
     *
     * @param x       X coordinate of the bottom left corner
     * @param y       Y coordinate of the bottom left corner
     * @param width   Width of the obstacle
     * @param height  Height of the obstacle
     * @param imgName Name of image file to use
     * @param verts   Up to 16 coordinates representing the vertexes of this polygon, listed as
     *                x0,y0,x1,y1,x2,y2,...
     * @return The goodie, so that it can be further modified
     */
    public Goodie makeGoodieAsPolygon(float x, float y, float width, float height, String imgName,
                                      float... verts) {
        Goodie g = new Goodie(mGame, mGame.mManager.mWorld, width, height, imgName);
        g.setPolygonPhysics(BodyDef.BodyType.StaticBody, x, y, verts);
        g.setCollisionsEnabled(false);
        mGame.mManager.mWorld.addActor(g, 0);
        return g;
    }

    /**
     * Make a Hero with an underlying rectangular shape
     *
     * @param x       X coordinate of the hero
     * @param y       Y coordinate of the hero
     * @param width   width of the hero
     * @param height  height of the hero
     * @param imgName File name of the default image to display
     * @return The hero that was created
     */
    public Hero makeHeroAsBox(float x, float y, float width, float height, String imgName) {
        Hero h = new Hero(mGame, mGame.mManager.mWorld, width, height, imgName);
        mGame.mManager.mHeroesCreated++;
        h.setBoxPhysics(BodyDef.BodyType.DynamicBody, x, y);
        mGame.mManager.mWorld.addActor(h, 0);
        return h;
    }

    /**
     * Make a Hero with an underlying circular shape
     *
     * @param x       X coordinate of the hero
     * @param y       Y coordinate of the hero
     * @param width   width of the hero
     * @param height  height of the hero
     * @param imgName File name of the default image to display
     * @return The hero that was created
     */
    public Hero makeHeroAsCircle(float x, float y, float width, float height, String imgName) {
        float radius = Math.max(width, height);
        Hero h = new Hero(mGame, mGame.mManager.mWorld, width, height, imgName);
        mGame.mManager.mHeroesCreated++;
        h.setCirclePhysics(BodyDef.BodyType.DynamicBody, x, y, radius / 2);
        mGame.mManager.mWorld.addActor(h, 0);
        return h;
    }

    /**
     * Draw a hero with an underlying polygon shape
     *
     * @param x       X coordinate of the bottom left corner
     * @param y       Y coordinate of the bottom left corner
     * @param width   Width of the obstacle
     * @param height  Height of the obstacle
     * @param imgName Name of image file to use
     * @param verts   Up to 16 coordinates representing the vertexes of this polygon, listed as
     *                x0,y0,x1,y1,x2,y2,...
     * @return The hero, so that it can be further modified
     */
    public Hero makeHeroAsPolygon(float x, float y, float width, float height, String imgName,
                                  float... verts) {
        Hero h = new Hero(mGame, mGame.mManager.mWorld, width, height, imgName);
        mGame.mManager.mHeroesCreated++;
        h.setPolygonPhysics(BodyDef.BodyType.StaticBody, x, y, verts);
        mGame.mManager.mWorld.addActor(h, 0);
        return h;
    }

    /**
     * Specify a limit on how far away from the Hero a projectile can go.  Without this, projectiles
     * could keep on traveling forever.
     *
     * @param distance Maximum distance from the hero that a projectile can travel
     */
    public void setProjectileRange(float distance) {
        for (Projectile p : mGame.mManager.mWorld.mProjectilePool.mPool)
            p.mRange = distance;
    }

    /**
     * Indicate that projectiles should feel the effects of gravity. Otherwise, they will be (more
     * or less) immune to gravitational forces.
     */
    public void setProjectileGravityOn() {
        for (Projectile p : mGame.mManager.mWorld.mProjectilePool.mPool)
            p.mBody.setGravityScale(1);
    }

    /**
     * Specify the image file from which to randomly choose projectile images
     *
     * @param imgName The file to use when picking images
     */
    // TODO: this is probably broken now that we removed Animatable images
    public void setProjectileImageSource(String imgName) {
        for (Projectile p : mGame.mManager.mWorld.mProjectilePool.mPool)
            p.mAnimator.updateImage(mGame.mMedia, imgName);
        mGame.mManager.mWorld.mProjectilePool.mRandomizeImages = true;
    }

    /**
     * The "directional projectile" mechanism might lead to the projectiles moving too fast. This
     * will cause the speed to be multiplied by a factor
     *
     * @param factor The value to multiply against the projectile speed.
     */
    public void setProjectileVectorDampeningFactor(float factor) {
        mGame.mManager.mWorld.mProjectilePool.mDirectionalDamp = factor;
    }

    /**
     * Indicate that all projectiles should participate in collisions, rather than disappearing when
     * they collide with other actors
     */
    public void enableCollisionsForProjectiles() {
        mGame.mManager.mWorld.mProjectilePool.mSensorProjectiles = false;
    }

    /**
     * Indicate that projectiles thrown with the "directional" mechanism should have a fixed
     * velocity
     *
     * @param velocity The magnitude of the velocity for projectiles
     */
    public void setFixedVectorThrowVelocityForProjectiles(float velocity) {
        mGame.mManager.mWorld.mProjectilePool.mEnableFixedVectorVelocity = true;
        mGame.mManager.mWorld.mProjectilePool.mFixedVectorVelocity = velocity;
    }

    /**
     * Indicate that projectiles thrown via the "directional" mechanism should be rotated to face in
     * their direction or movement
     */
    public void setRotateVectorThrowForProjectiles() {
        mGame.mManager.mWorld.mProjectilePool.mRotateVectorThrow = true;
    }

    /**
     * Indicate that when two projectiles collide, they should both remain on screen
     */
    public void setCollisionOkForProjectiles() {
        for (Projectile p : mGame.mManager.mWorld.mProjectilePool.mPool)
            p.mDisappearOnCollide = false;
    }

    /**
     * Describe the behavior of projectiles in a scene. You must call this if you intend to use
     * projectiles in your scene.
     *
     * @param size     number of projectiles that can be thrown at once
     * @param width    width of a projectile
     * @param height   height of a projectile
     * @param imgName  image to use for projectiles
     * @param strength specifies the amount of damage that a projectile does to an enemy
     * @param zIndex   The z plane on which the projectiles should be drawn
     * @param isCircle Should projectiles have an underlying circle or box shape?
     */
    public void configureProjectiles(int size, float width, float height, String imgName,
                                     int strength, int zIndex, boolean isCircle) {
        mGame.mManager.mWorld.mProjectilePool = new ProjectilePool(mGame, mGame.mManager.mWorld,
                size, width, height, imgName, strength, zIndex, isCircle);
    }

    /**
     * Set a limit on the total number of projectiles that can be thrown
     *
     * @param number How many projectiles are available
     */
    public void setNumberOfProjectiles(int number) {
        mGame.mManager.mWorld.mProjectilePool.mProjectilesRemaining = number;
    }

    /**
     * Specify a sound to play when the projectile is thrown
     *
     * @param soundName Name of the sound file to play
     */
    public void setThrowSound(String soundName) {
        mGame.mManager.mWorld.mProjectilePool.mThrowSound = mMedia.getSound(soundName);
    }

    /**
     * Specify the sound to play when a projectile disappears
     *
     * @param soundName the name of the sound file to play
     */
    public void setProjectileDisappearSound(String soundName) {
        mGame.mManager.mWorld.mProjectilePool.mProjectileDisappearSound =
                mMedia.getSound(soundName);
    }

    /**
     * Specify how projectiles should be animated
     *
     * @param animation The animation object to use for each projectile that is thrown
     */
    public void setProjectileAnimation(Animation animation) {
        for (Projectile p : mGame.mManager.mWorld.mProjectilePool.mPool)
            p.setDefaultAnimation(animation);
    }

    /**
     * Draw a box on the scene
     * <p>
     * Note: the box is actually four narrow rectangles
     *
     * @param x0         X coordinate of top left corner
     * @param y0         Y coordinate of top left corner
     * @param x1         X coordinate of bottom right corner
     * @param y1         Y coordinate of bottom right corner
     * @param imgName    name of the image file to use when drawing the rectangles
     * @param density    Density of the rectangle. When in doubt, use 1
     * @param elasticity Elasticity of the rectangle. When in doubt, use 0
     * @param friction   Friction of the rectangle. When in doubt, use 1
     */
    public void drawBoundingBox(float x0, float y0, float x1, float y1, String imgName,
                                float density, float elasticity, float friction) {
        Obstacle bottom = makeObstacleAsBox(x0 - 1, y0 - 1, Math.abs(x0 - x1) + 2, 1, imgName);
        bottom.setPhysics(density, elasticity, friction);

        Obstacle top = makeObstacleAsBox(x0 - 1, y1, Math.abs(x0 - x1) + 2, 1, imgName);
        top.setPhysics(density, elasticity, friction);

        Obstacle left = makeObstacleAsBox(x0 - 1, y0 - 1, 1, Math.abs(y0 - y1) + 2, imgName);
        left.setPhysics(density, elasticity, friction);

        Obstacle right = makeObstacleAsBox(x1, y0 - 1, 1, Math.abs(y0 - y1) + 2, imgName);
        right.setPhysics(density, elasticity, friction);
    }

    /**
     * Load an SVG line drawing generated from Inkscape. The SVG will be loaded as a bunch of
     * Obstacles. Note that not all Inkscape drawings will work as expected... if you need more
     * power than this provides, you'll have to modify Svg.java
     *
     * @param svgName    Name of the svg file to load. It should be in the assets folder
     * @param stretchX   Stretch the drawing in the X dimension by this percentage
     * @param stretchY   Stretch the drawing in the Y dimension by this percentage
     * @param transposeX Shift the drawing in the X dimension. NB: shifting occurs after stretching
     * @param transposeY Shift the drawing in the Y dimension. NB: shifting occurs after stretching
     * @param callback   A callback for customizing each (obstacle) line segment of the SVG
     */
    public void importLineDrawing(String svgName, float stretchX, float stretchY,
                                  float transposeX, float transposeY, LolActorEvent callback) {
        // Create an SVG object to hold all the parameters, then use it to parse the file
        Svg s = new Svg(this, stretchX, stretchY, transposeX, transposeY, callback);
        s.parse(svgName);
    }

    /**
     * Use this to manage the state of Mute
     */
    public void toggleMute() {
        // volume is either 1 or 0
        if (getGameFact("volume", 1) == 1) {
            // set volume to 0, set image to 'unmute'
            putGameFact("volume", 0);
        } else {
            // set volume to 1, set image to 'mute'
            putGameFact("volume", 1);
        }
        // update all music
        mMedia.resetMusicVolume();
    }

    /**
     * Use this to determine if the game is muted or not. True corresponds to not muted, false
     * corresponds to muted.
     */
    public boolean getVolume() {
        return getGameFact("volume", 1) == 1;
    }

    /**
     * Draw a picture on the current level
     * <p>
     * Note: the order in which this is called relative to other actors will determine whether they
     * go under or over this picture.
     *
     * @param x       X coordinate of bottom left corner
     * @param y       Y coordinate of bottom left corner
     * @param width   Width of the picture
     * @param height  Height of this picture
     * @param imgName Name of the picture to display
     * @param zIndex  The z index of the image. There are 5 planes: -2, -2, 0, 1, and 2. By default,
     *                everything goes to plane 0
     */
    public void drawPicture(final float x, final float y, final float width, final float height,
                            final String imgName, int zIndex) {
        mGame.mManager.mWorld.makePicture(x, y, width, height, imgName, zIndex);
    }

    /**
     * Draw some text in the scene, using a bottom-left coordinate
     *
     * @param x         The x coordinate of the bottom left corner
     * @param y         The y coordinate of the bottom left corner
     * @param fontName  The name of the font to use
     * @param fontColor The color of the font
     * @param fontSize  The size of the font
     * @param prefix    Prefix text to put before the generated text
     * @param suffix    Suffix text to put after the generated text
     * @param tp        A TextProducer that will generate the text to display
     * @param zIndex    The z index of the text
     * @return A Renderable of the text, so it can be enabled/disabled by program code
     */
    public Renderable addText(float x, float y, String fontName, String fontColor, int fontSize,
                              String prefix, String suffix, TextProducer tp, int zIndex) {
        return mGame.mManager.mWorld.addText(x, y, fontName, fontColor, fontSize, prefix, suffix,
                tp, zIndex);
    }

    /**
     * Draw some text in the scene, centering it on a specific point
     *
     * @param centerX   The x coordinate of the center
     * @param centerY   The y coordinate of the center
     * @param fontName  The name of the font to use
     * @param fontColor The color of the font
     * @param fontSize  The size of the font
     * @param prefix    Prefix text to put before the generated text
     * @param suffix    Suffix text to put after the generated text
     * @param tp        A TextProducer that will generate the text to display
     * @param zIndex    The z index of the text
     * @return A Renderable of the text, so it can be enabled/disabled by program code
     */
    public Renderable addTextCentered(float centerX, float centerY, String fontName,
                                      String fontColor, int fontSize, String prefix, String suffix,
                                      TextProducer tp, int zIndex) {
        return mGame.mManager.mWorld.addTextCentered(centerX, centerY, fontName, fontColor,
                fontSize, prefix, suffix, tp, zIndex);
    }

    /**
     * Generate a random number x in the range [0,max)
     *
     * @param max The largest number returned will be one less than max
     * @return a random integer
     */
    public int getRandom(int max) {
        return mGame.mManager.mWorld.mGenerator.nextInt(max);
    }

    /**
     * Report whether all levels should be treated as unlocked. This is useful in Chooser, where we
     * might need to prevent some levels from being played.
     */
    public boolean getUnlockMode() {
        return mConfig.mUnlockAllLevels;
    }

    /**
     * load the splash screen
     */
    public void doSplash() {
        mGame.mManager.doSplash();
    }

    /**
     * load the level-chooser screen. Note that when the chooser is disabled, we jump straight to
     * level 1.
     *
     * @param whichChooser The chooser screen to create
     */
    public void doChooser(int whichChooser) {
        mGame.mManager.doChooser(whichChooser);
    }

    /**
     * load a playable level.
     *
     * @param which The index of the level to load
     */
    public void doLevel(int which) {
        mGame.mManager.doPlay(which);
    }

    /**
     * load a help level.
     *
     * @param which The index of the help level to load
     */
    public void doHelp(int which) {
        mGame.mManager.doHelp(which);
    }

    /**
     * load a screen of the store.
     *
     * @param which The index of the help level to load
     */
    public void doStore(int which) {
        mGame.mManager.doStore(which);
    }

    /**
     * quit the game
     */
    public void doQuit() {
        mGame.mManager.doQuit();
    }

    /**
     * simulate pressing the back button (e.g., to go to the chooser)
     */
    public void doBack() {
        mGame.mManager.handleBack();
    }

    /**
     * Create a new animation that can be populated via the "to" function
     *
     * @param sequenceCount The number of frames in the animation
     * @param repeat        True if the animation should repeat when it reaches the end
     * @return The animation
     */
    public Animation makeAnimation(int sequenceCount, boolean repeat) {
        return new Animation(this.mMedia, sequenceCount, repeat);
    }

    /**
     * Create a new animation that shows a set of images for the same amount of time
     *
     * @param timePerFrame The time to show each image
     * @param repeat       True if the animation should repeat when it reaches the end
     * @param imgNames     The names of the images that comprise the animation
     * @return The animation
     */
    public Animation makeAnimation(int timePerFrame, boolean repeat, String... imgNames) {
        return new Animation(this.mMedia, timePerFrame, repeat, imgNames);
    }
}
