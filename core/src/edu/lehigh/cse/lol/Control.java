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

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import edu.lehigh.cse.lol.internals.GestureAction;
import edu.lehigh.cse.lol.internals.LolAction;

/**
 * LOL Games have a heads-up display (hud). The hud is a place for displaying
 * text and drawing touchable buttons, so that as the hero moves through the
 * level, the buttons and text can remain at the same place on the screen. This
 * class encapsulates all of the touchable buttons.
 */
public class Control {
    /**
     * Should we run code when this Control is touched?
     */
    boolean mIsTouchable;

    /**
     * Code to run when this Control is touched
     */
    GestureAction mGestureAction;

    /**
     * The rectangle on the screen that is touchable
     */
    Rectangle mRange;

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
     * Create a control on the heads up display
     *
     * @param imgName The name of the image to display. If "" is given as the name,
     *                it will not crash.
     * @param x       The X coordinate (in pixels) of the bottom left corner.
     * @param y       The Y coordinate (in pixels) of the bottom left corner.
     * @param width   The width of the Control
     * @param height  The height of the Control
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
     * Add a button that pauses the game (via a single tap) by causing a
     * PauseScene to be displayed. Note that you must configure a PauseScene, or
     * pressing this button will cause your game to crash.
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     */
    public static Control addPauseButton(int x, int y, int width, int height, String imgName) {
        Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            @Override
            public boolean onTap(Vector3 vv) {
                PauseScene.get().show();
                return true;
            }
        };
        Lol.sGame.mCurrentLevel.mControls.add(c);
        Lol.sGame.mCurrentLevel.mTapControls.add(c);
        return c;
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Add a button that makes an actor move as long as the button is being held
     * down
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     * @param actor   The actor to move downward
     * @param dx      The new X velocity
     * @param dy      The new Y velocity
     */
    public static Control addMoveButton(int x, int y, int width, int height, String imgName, final Actor actor,
                                        final float dx, final float dy) {
        final Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {

            /**
             * Run this when a control is down-pressed or up-pressed
             *
             * @param isUp
             *            True if it is an up-press
             */
            @Override
            public boolean toggle(boolean isUp, Vector3 touchVec) {
                if (isUp) {
                    Vector2 v = actor.mBody.getLinearVelocity();
                    if (dx != 0)
                        v.x = 0;
                    if (dy != 0)
                        v.y = 0;
                    actor.updateVelocity(v.x, v.y);
                    mHolding = false;
                } else {
                    mHolding = true;
                }
                return true;
            }
        };
        Lol.sGame.mCurrentLevel.mControls.add(c);
        Lol.sGame.mCurrentLevel.mToggleControls.add(c);
        Lol.sGame.mCurrentLevel.mRepeatEvents.add(new LolAction() {
            @Override
            public void go() {
                if (c.mGestureAction.mHolding) {
                    Vector2 v = actor.mBody.getLinearVelocity();
                    if (dx != 0)
                        v.x = dx;
                    if (dy != 0)
                        v.y = dy;
                    actor.updateVelocity(v.x, v.y);
                }
            }
        });
        return c;
    }

    /**
     * Add a button that moves an actor downward while the button is being held
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     * @param rate    Rate at which the actor moves
     * @param actor   The actor to move downward
     */
    public static Control addDownButton(int x, int y, int width, int height, String imgName, float rate, Actor actor) {
        return addMoveButton(x, y, width, height, imgName, actor, 0, -rate);
    }

    /**
     * Add a button that moves an actor upward while the button is being held
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     * @param rate    Rate at which the actor moves
     * @param actor   The actor to move upward
     */
    public static Control addUpButton(int x, int y, int width, int height, String imgName, float rate, Actor actor) {
        return addMoveButton(x, y, width, height, imgName, actor, 0, rate);
    }

    /**
     * Add a button that moves the given actor left while the button is being
     * held
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     * @param rate    Rate at which the actor moves
     * @param actor   The actor that should move left when the button is pressed
     */
    public static Control addLeftButton(int x, int y, int width, int height, String imgName, float rate, Actor actor) {
        return addMoveButton(x, y, width, height, imgName, actor, -rate, 0);
    }

    /**
     * Add a button that moves the given actor to the right while the button is
     * being held
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     * @param rate    Rate at which the actor moves
     * @param actor   The actor that should move right when the button is pressed
     */
    public static Control addRightButton(int x, int y, int width, int height, String imgName, float rate, Actor actor) {
        return addMoveButton(x, y, width, height, imgName, actor, rate, 0);
    }

    /**
     * Add a button that moves the given actor at one speed when it is
     * depressed, and at another otherwise
     *
     * @param x         The X coordinate of the bottom left corner (in pixels)
     * @param y         The Y coordinate of the bottom left corner (in pixels)
     * @param width     The width of the image
     * @param height    The height of the image
     * @param imgName   The name of the image to display. Use "" for an invisible
     *                  button
     * @param rateDownX Rate (X) at which the actor moves when the button is pressed
     * @param rateDownY Rate (Y) at which the actor moves when the button is pressed
     * @param rateUpX   Rate (X) at which the actor moves when the button is not
     *                  pressed
     * @param rateUpY   Rate (Y) at which the actor moves when the button is not
     *                  pressed
     * @param actor     The actor that the button controls
     */
    public static Control addTurboButton(int x, int y, int width, int height, String imgName, final int rateDownX,
                                         final int rateDownY, final int rateUpX, final int rateUpY, final Actor actor) {
        final Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            @Override
            public boolean toggle(boolean isUp, Vector3 touchVec) {
                mHolding = !isUp;
                return true;
            }
        };
        Lol.sGame.mCurrentLevel.mControls.add(c);
        Lol.sGame.mCurrentLevel.mToggleControls.add(c);
        Lol.sGame.mCurrentLevel.mRepeatEvents.add(new LolAction() {
            @Override
            public void go() {
                if (c.mGestureAction.mHolding) {
                    Vector2 v = actor.mBody.getLinearVelocity();
                    v.x = rateDownX;
                    v.y = rateDownY;
                    actor.updateVelocity(v.x, v.y);
                } else {
                    Vector2 v = actor.mBody.getLinearVelocity();
                    v.x = rateUpX;
                    v.y = rateUpY;
                    actor.updateVelocity(v.x, v.y);
                }
            }
        });
        return c;
    }

    /**
     * Add a button that moves the given actor at one speed, but doesn't
     * immediately stop the actor when the button is released
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     * @param rateX   Rate (X) at which the actor moves when the button is pressed
     * @param rateY   Rate (Y) at which the actor moves when the button is pressed
     * @param actor   The actor that the button controls
     */
    public static Control addDampenedMotionButton(int x, int y, int width, int height, String imgName,
                                                  final float rateX, final float rateY, final float dampening, final Actor actor) {
        final Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            @Override
            public boolean toggle(boolean isUp, Vector3 vv) {
                mHolding = !isUp;
                return true;
            }
        };
        Lol.sGame.mCurrentLevel.mControls.add(c);
        Lol.sGame.mCurrentLevel.mToggleControls.add(c);
        Lol.sGame.mCurrentLevel.mRepeatEvents.add(new LolAction() {
            @Override
            public void go() {
                if (c.mGestureAction.mHolding) {
                    Vector2 v = actor.mBody.getLinearVelocity();
                    v.x = rateX;
                    v.y = rateY;
                    actor.mBody.setLinearDamping(0);
                    actor.updateVelocity(v.x, v.y);
                } else {
                    actor.mBody.setLinearDamping(dampening);
                }
            }
        });
        return c;
    }

    /**
     * Add a button that puts a hero into crawl mode when depressed, and regular
     * mode when released
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     * @param h       The hero to control
     */
    public static Control addCrawlButton(int x, int y, int width, int height, String imgName, final Hero h) {
        Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            @Override
            public boolean toggle(boolean upPress, Vector3 touchVec) {
                if (upPress)
                    h.crawlOff();
                else
                    h.crawlOn();
                return true;
            }
        };
        Lol.sGame.mCurrentLevel.mControls.add(c);
        Lol.sGame.mCurrentLevel.mToggleControls.add(c);
        return c;
    }

    /**
     * Add a button to make a hero jump
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     * @param h       The hero to control
     */
    public static Control addJumpButton(int x, int y, int width, int height, String imgName, final Hero h) {
        Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            @Override
            public boolean onTap(Vector3 vv) {
                h.jump();
                return true;
            }
        };
        Lol.sGame.mCurrentLevel.mControls.add(c);
        Lol.sGame.mCurrentLevel.mTapControls.add(c);
        return c;
    }

    /**
     * Add a button to make the hero throw a projectile
     *
     * @param x          The X coordinate of the bottom left corner (in pixels)
     * @param y          The Y coordinate of the bottom left corner (in pixels)
     * @param width      The width of the image
     * @param height     The height of the image
     * @param imgName    The name of the image to display. Use "" for an invisible
     *                   button
     * @param h          The hero who should throw the projectile
     * @param milliDelay A delay between throws, so that holding doesn't lead to too
     *                   many throws at once
     * @param offsetX    specifies the x distance between the bottom left of the
     *                   projectile and the bottom left of the hero throwing the
     *                   projectile
     * @param offsetY    specifies the y distance between the bottom left of the
     *                   projectile and the bottom left of the hero throwing the
     *                   projectile
     * @param velocityX  The X velocity of the projectile when it is thrown
     * @param velocityY  The Y velocity of the projectile when it is thrown
     */
    public static Control addThrowButton(int x, int y, int width, int height, String imgName, final Hero h,
                                         final int milliDelay, final float offsetX, final float offsetY, final float velocityX, final float velocityY) {
        final Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            @Override
            public boolean toggle(boolean isUp, Vector3 touchVec) {
                mHolding = !isUp;
                return true;
            }
        };
        Lol.sGame.mCurrentLevel.mControls.add(c);
        Lol.sGame.mCurrentLevel.mToggleControls.add(c);
        Lol.sGame.mCurrentLevel.mRepeatEvents.add(new LolAction() {
            long mLastThrow;

            @Override
            public void go() {
                if (c.mGestureAction.mHolding) {
                    long now = System.currentTimeMillis();
                    if (mLastThrow + milliDelay < now) {
                        mLastThrow = now;
                        Lol.sGame.mCurrentLevel.mProjectilePool.throwFixed(h, offsetX, offsetY, velocityX, velocityY);
                    }
                }
            }
        });
        return c;
    }

    /**
     * Add a button to make the hero throw a projectile, but holding doesn't
     * make it throw more often
     *
     * @param x         The X coordinate of the bottom left corner (in pixels)
     * @param y         The Y coordinate of the bottom left corner (in pixels)
     * @param width     The width of the image
     * @param height    The height of the image
     * @param imgName   The name of the image to display. Use "" for an invisible
     *                  button
     * @param h         The hero who should throw the projectile
     * @param offsetX   specifies the x distance between the bottom left of the
     *                  projectile and the bottom left of the hero throwing the
     *                  projectile
     * @param offsetY   specifies the y distance between the bottom left of the
     *                  projectile and the bottom left of the hero throwing the
     *                  projectile
     * @param velocityX The X velocity of the projectile when it is thrown
     * @param velocityY The Y velocity of the projectile when it is thrown
     */
    public static Control addSingleThrowButton(int x, int y, int width, int height, String imgName, final Hero h,
                                               final float offsetX, final float offsetY, final float velocityX, final float velocityY) {
        Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            @Override
            public boolean onTap(Vector3 vv) {
                Lol.sGame.mCurrentLevel.mProjectilePool.throwFixed(h, offsetX, offsetY, velocityX, velocityY);
                return true;
            }
        };
        Lol.sGame.mCurrentLevel.mControls.add(c);
        Lol.sGame.mCurrentLevel.mTapControls.add(c);
        return c;
    }

    /**
     * The default behavior for throwing is to throw in a straight line. If we
     * instead desire that the projectiles have some sort of aiming to them, we
     * need to use this method, which throws toward where the screen was pressed
     *
     * Note: you probably want to use an invisible button that covers the
     * screen...
     *
     * @param x          The X coordinate of the bottom left corner (in pixels)
     * @param y          The Y coordinate of the bottom left corner (in pixels)
     * @param width      The width of the image
     * @param height     The height of the image
     * @param imgName    The name of the image to display. Use "" for an invisible
     *                   button
     * @param h          The hero who should throw the projectile
     * @param milliDelay A delay between throws, so that holding doesn't lead to too
     *                   many throws at once
     * @param offsetX    specifies the x distance between the bottom left of the
     *                   projectile and the bottom left of the hero throwing the
     *                   projectile
     * @param offsetY    specifies the y distance between the bottom left of the
     *                   projectile and the bottom left of the hero throwing the
     *                   projectile
     */
    public static Control addDirectionalThrowButton(int x, int y, int width, int height, String imgName, final Hero h,
                                                    final long milliDelay, final float offsetX, final float offsetY) {
        final Control c = new Control(imgName, x, y, width, height);
        final Vector3 v = new Vector3();
        c.mGestureAction = new GestureAction() {
            @Override
            public boolean toggle(boolean isUp, Vector3 touchVec) {
                if (isUp) {
                    mHolding = false;
                } else {
                    mHolding = true;
                    v.x = touchVec.x;
                    v.y = touchVec.y;
                    v.z = touchVec.z;
                }
                return true;
            }

            @Override
            public boolean onPan(Vector3 touchVec, float deltaX, float deltaY) {
                v.x = touchVec.x;
                v.y = touchVec.y;
                v.z = touchVec.z;
                return true;
            }
        };
        Lol.sGame.mCurrentLevel.mControls.add(c);
        // on toggle, we start or stop throwing; on pan, we change throw
        // direction
        Lol.sGame.mCurrentLevel.mToggleControls.add(c);
        Lol.sGame.mCurrentLevel.mPanControls.add(c);
        Lol.sGame.mCurrentLevel.mRepeatEvents.add(new LolAction() {
            long mLastThrow;

            @Override
            public void go() {
                if (c.mGestureAction.mHolding) {
                    long now = System.currentTimeMillis();
                    if (mLastThrow + milliDelay < now) {
                        mLastThrow = now;
                        Lol.sGame.mCurrentLevel.mProjectilePool.throwAt(h.mBody.getPosition().x,
                                h.mBody.getPosition().y, v.x, v.y, h, offsetX, offsetY);
                    }
                }
            }
        });
        return c;
    }

    /**
     * This is almost exactly like addDirectionalThrowButton. The only
     * difference is that holding won't cause the hero to throw more projectiles
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     * @param h       The hero who should throw the projectile
     * @param offsetX specifies the x distance between the bottom left of the
     *                projectile and the bottom left of the hero throwing the
     *                projectile
     * @param offsetY specifies the y distance between the bottom left of the
     *                projectile and the bottom left of the hero throwing the
     *                projectile
     */
    public static Control addDirectionalSingleThrowButton(int x, int y, int width, int height, String imgName,
                                                          final Hero h, final float offsetX, final float offsetY) {
        Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            @Override
            public boolean onTap(Vector3 touchVec) {
                Lol.sGame.mCurrentLevel.mProjectilePool.throwAt(h.mBody.getPosition().x, h.mBody.getPosition().y,
                        touchVec.x, touchVec.y, h, offsetX, offsetY);
                return true;
            }
        };
        Lol.sGame.mCurrentLevel.mControls.add(c);
        Lol.sGame.mCurrentLevel.mTapControls.add(c);
        return c;
    }

    /**
     * Display a zoom out button. Note that zooming in and out does not work
     * well with elements that hover on the screen. Use with care.
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     * @param maxZoom Maximum zoom. 8 is usually a good default
     */
    public static Control addZoomOutButton(int x, int y, int width, int height, String imgName, final float maxZoom) {
        Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            @Override
            public boolean onTap(Vector3 worldTouchCoord) {
                float curzoom = Lol.sGame.mCurrentLevel.mGameCam.zoom;
                if (curzoom < maxZoom) {
                    Lol.sGame.mCurrentLevel.mGameCam.zoom *= 2;
                    Lol.sGame.mCurrentLevel.mBgCam.zoom *= 2;
                }
                return true;
            }
        };
        Lol.sGame.mCurrentLevel.mControls.add(c);
        Lol.sGame.mCurrentLevel.mTapControls.add(c);
        return c;
    }

    /**
     * Display a zoom in button
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     * @param minZoom Minimum zoom. 0.25f is usually a good default
     */
    public static Control addZoomInButton(int x, int y, int width, int height, String imgName, final float minZoom) {
        Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            @Override
            public boolean onTap(Vector3 worldTouchCoord) {
                float curzoom = Lol.sGame.mCurrentLevel.mGameCam.zoom;
                if (curzoom > minZoom) {
                    Lol.sGame.mCurrentLevel.mGameCam.zoom /= 2;
                    Lol.sGame.mCurrentLevel.mBgCam.zoom /= 2;
                }
                return true;
            }
        };
        Lol.sGame.mCurrentLevel.mControls.add(c);
        Lol.sGame.mCurrentLevel.mTapControls.add(c);
        return c;
    }

    /**
     * Add a button that rotates the hero
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     * @param rate    Amount of rotation to apply to the hero on each press
     */
    public static Control addRotateButton(int x, int y, int width, int height, String imgName, final float rate,
                                          final Hero h) {
        final Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            @Override
            public boolean toggle(boolean isUp, Vector3 touchVec) {
                mHolding = !isUp;
                return true;
            }
        };
        Lol.sGame.mCurrentLevel.mControls.add(c);
        Lol.sGame.mCurrentLevel.mToggleControls.add(c);
        Lol.sGame.mCurrentLevel.mRepeatEvents.add(new LolAction() {
            @Override
            public void go() {
                if (c.mGestureAction.mHolding)
                    h.increaseRotation(rate);
            }
        });
        return c;
    }

    /**
     * Add a button to the heads-up display that runs custom code via an
     * onControlPress callback
     *
     * @param x        The X coordinate of the bottom left corner (in pixels)
     * @param y        The Y coordinate of the bottom left corner (in pixels)
     * @param width    The width of the image
     * @param height   The height of the image
     * @param imgName  The name of the image to display. Use "" for an invisible
     *                 button
     * @param callback The code to run when the button is pressed
     */
    public static Control addCallbackControl(int x, int y, int width, int height, String imgName,
                                             final LolCallback callback) {
        Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            @Override
            public boolean onTap(Vector3 vv) {
                callback.onEvent();
                return true;
            }
        };
        Lol.sGame.mCurrentLevel.mControls.add(c);
        Lol.sGame.mCurrentLevel.mTapControls.add(c);
        return c;
    }

    /**
     * Add a button to the heads-up display that runs custom code via an
     * onControlPress callback, but the button only works once
     *
     * @param x               The X coordinate of the bottom left corner (in pixels)
     * @param y               The Y coordinate of the bottom left corner (in pixels)
     * @param width           The width of the image
     * @param height          The height of the image
     * @param activeImgName   The name of the image to display before the button is pressed.
     *                        Use "" for an invisible button
     * @param inactiveImgName The name of the image to display after the button
     *                        is pressed.
     * @param callback        The code to run in response to the control press
     */
    public static Control addOneTimeCallbackControl(int x, int y, int width, int height, String activeImgName,
                                                    final String inactiveImgName, final LolCallback callback) {
        final Control c = new Control(activeImgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            @Override
            public boolean onTap(Vector3 vv) {
                callback.onEvent();
                c.mIsTouchable = false;
                TextureRegion[] trs = Media.getImage(inactiveImgName);
                if (trs != null) {
                    c.mImage = trs[0];
                    Util.message("issue", c.mImage + "");
                } else {
                    c.mImage = null;
                    Util.message("issue", "null image");
                }
                return true;
            }
        };
        Lol.sGame.mCurrentLevel.mControls.add(c);
        Lol.sGame.mCurrentLevel.mTapControls.add(c);
        return c;
    }

    /**
     * Allow panning to view more of the screen than is currently visible
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     */
    static public Control addPanControl(int x, int y, int width, int height, String imgName) {
        Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            /**
             * Use this to restore the chase actor when the Pan stops
             */
            Actor oldChaseactor;

            /**
             * Handle a pan stop event by restoring the chase actor, if there
             * was one
             *
             * @param touchVec
             *            The x/y/z coordinates of the touch
             */
            @Override
            public boolean onPanStop(Vector3 touchVec) {
                Level.setCameraChase(oldChaseactor);
                oldChaseactor = null;
                return true;
            }

            /**
             * Run this when the screen is panned
             *
             * @param touchVec
             *            The x/y/z world coordinates of the touch
             *
             * @param deltaX
             *            the change in X, in screen coordinates
             *
             * @param deltaY
             *            the change in Y, in screen coordinates
             */
            @Override
            public boolean onPan(Vector3 touchVec, float deltaX, float deltaY) {
                if (Lol.sGame.mCurrentLevel.mChaseActor != null) {
                    oldChaseactor = Lol.sGame.mCurrentLevel.mChaseActor;
                    Lol.sGame.mCurrentLevel.mChaseActor = null;
                }
                float x = Lol.sGame.mCurrentLevel.mGameCam.position.x - deltaX * .1f
                        * Lol.sGame.mCurrentLevel.mGameCam.zoom;
                float y = Lol.sGame.mCurrentLevel.mGameCam.position.y + deltaY * .1f
                        * Lol.sGame.mCurrentLevel.mGameCam.zoom;
                // if x or y is too close to MAX,MAX, stick with max acceptable
                // values
                if (x > Lol.sGame.mCurrentLevel.mCamBoundX - Lol.sGame.mWidth * Lol.sGame.mCurrentLevel.mGameCam.zoom
                        / Physics.PIXEL_METER_RATIO / 2)
                    x = Lol.sGame.mCurrentLevel.mCamBoundX - Lol.sGame.mWidth * Lol.sGame.mCurrentLevel.mGameCam.zoom
                            / Physics.PIXEL_METER_RATIO / 2;
                if (y > Lol.sGame.mCurrentLevel.mCamBoundY - Lol.sGame.mHeight * Lol.sGame.mCurrentLevel.mGameCam.zoom
                        / Physics.PIXEL_METER_RATIO / 2)
                    y = Lol.sGame.mCurrentLevel.mCamBoundY - Lol.sGame.mHeight * Lol.sGame.mCurrentLevel.mGameCam.zoom
                            / Physics.PIXEL_METER_RATIO / 2;

                // if x or y is too close to 0,0, stick with minimum acceptable
                // values
                //
                // NB: we do MAX before MIN, so that if we're zoomed out, we
                // show extra space at the top instead of the bottom
                if (x < Lol.sGame.mWidth * Lol.sGame.mCurrentLevel.mGameCam.zoom / Physics.PIXEL_METER_RATIO / 2)
                    x = Lol.sGame.mWidth * Lol.sGame.mCurrentLevel.mGameCam.zoom / Physics.PIXEL_METER_RATIO / 2;
                if (y < Lol.sGame.mHeight * Lol.sGame.mCurrentLevel.mGameCam.zoom / Physics.PIXEL_METER_RATIO / 2)
                    y = Lol.sGame.mHeight * Lol.sGame.mCurrentLevel.mGameCam.zoom / Physics.PIXEL_METER_RATIO / 2;

                // update the camera position
                Lol.sGame.mCurrentLevel.mGameCam.position.set(x, y, 0);
                return true;
            }
        };
        Lol.sGame.mCurrentLevel.mPanControls.add(c);
        return c;
    }

    /**
     * Allow pinch-to-zoom
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     * @param maxZoom The maximum zoom (out) factor. 8 is usually a good choice.
     * @param minZoom The minimum zoom (int) factor. .25f is usually a good choice.
     */
    static public Control addPinchZoomControl(int x, int y, int width, int height, String imgName, final float maxZoom,
                                              final float minZoom) {
        Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            float lastZoom = 1;

            /**
             * Handle a down press (hopefully to turn it into a hold/release)
             *
             * @param touchVec
             *            The x/y/z coordinates of the touch
             */
            public boolean onDown(Vector3 touchVec) {
                lastZoom = Lol.sGame.mCurrentLevel.mGameCam.zoom;
                return true;
            }

            /**
             * Handle a zoom-via-pinch event
             *
             * @param initialDistance
             *            The distance between fingers when the pinch started
             * @param distance
             *            The current distance between fingers
             */
            @Override
            public boolean zoom(float initialDistance, float distance) {
                float ratio = initialDistance / distance;
                float newZoom = lastZoom * ratio;
                if (newZoom > minZoom && newZoom < maxZoom)
                    Lol.sGame.mCurrentLevel.mGameCam.zoom = newZoom;
                return false;
            }
        };
        Lol.sGame.mCurrentLevel.mZoomControls.add(c);
        return c;
    }

    /**
     * Add an image to the heads-up display that changes its clipping rate to
     * seem to grow vertically, without stretching. Touching the image causes
     * its scale (0-100) to be sent to a ControlPressactor event
     *
     * @param x        The X coordinate of the bottom left corner (in pixels)
     * @param y        The Y coordinate of the bottom left corner (in pixels)
     * @param width    The width of the image
     * @param height   The height of the image
     * @param imgName  The name of the image to display. Use "" for an invisible
     *                 button
     * @param callback The code to run when the bar is pressed
     */
    public static Control addVerticalBar(final int x, final int y, final int width, final int height, String imgName,
                                         final LolCallback callback) {
        final Control c = new Control(imgName, x, y, width, height) {
            /**
             * Track if the bar is growing (true) or shrinking (false)
             */
            boolean mGrow = true;

            /**
             * The raw width of the image
             */
            int mTrueWidth;

            /**
             * The raw height of the image
             */
            int mTrueHeight;

            /**
             * The x position of the image's bottom left corner
             */
            int mTrueX;

            /**
             * This control requires run-time configuration... we track if it's
             * been done via this flag
             */
            boolean mConfigured = false;

            /**
             * This is the render method when we've got a valid TR. We're going
             * to play with how we draw, so that we can clip and stretch the
             * image
             *
             * @param sb
             *            The SpriteBatch to use to draw the image
             */
            @Override
            void render(SpriteBatch sb) {
                // one-time configuration
                if (!mConfigured) {
                    mTrueHeight = mImage.getRegionHeight();
                    mTrueWidth = mImage.getRegionWidth();
                    mTrueX = mImage.getRegionX();
                    mConfigured = true;
                }

                if (!mIsActive)
                    return;

                // draw it
                sb.draw(mImage.getTexture(), x, y, width / 2, height / 2, width,
                        (height * (int) callback.mFloatVal) / 100, 1, 1, 0, mTrueX, 0, mTrueWidth,
                        (mTrueHeight * (int) callback.mFloatVal) / 100, false, true);

                // don't keep showing anything if we've already received a
                // touch...
                if (!mIsTouchable)
                    return;

                // update size
                if (callback.mFloatVal == 100)
                    mGrow = false;
                if (callback.mFloatVal == 0)
                    mGrow = true;
                callback.mFloatVal = callback.mFloatVal + (mGrow ? 1 : -1);
            }
        };
        c.mGestureAction = new GestureAction() {
            /**
             * This is a touchable control...
             */
            @Override
            public boolean onTap(Vector3 v) {
                if (!c.mIsActive || !c.mIsTouchable)
                    return false;
                callback.onEvent();
                return true;
            }
        };
        // add to hud
        Lol.sGame.mCurrentLevel.mControls.add(c);
        Lol.sGame.mCurrentLevel.mTapControls.add(c);
        return c;
    }

    /**
     * Add a rotating button that generates a ControlPressactor event and passes
     * the rotation to the handler.
     *
     * @param x        The X coordinate of the bottom left corner (in pixels)
     * @param y        The Y coordinate of the bottom left corner (in pixels)
     * @param width    The width of the image
     * @param height   The height of the image
     * @param imgName  The name of the image to display. Use "" for an invisible
     *                 button
     * @param delta    Amount of rotation to add during each fraction of a second
     * @param callback The code to run when the rotator is pressed
     */
    public static Control addRotator(final int x, final int y, final int width, final int height, String imgName,
                                     final float delta, final LolCallback callback) {
        final Control c = new Control(imgName, x, y, width, height) {
            /**
             * This is the render method when we've got a valid TR. We're going
             * to play with how we draw, so that we can clip and stretch the
             * image
             *
             * @param sb
             *            The SpriteBatch to use to draw the image
             */
            @Override
            void render(SpriteBatch sb) {
                if (!mIsActive)
                    return;
                // draw it
                sb.draw(mImage, mRange.x, mRange.y, mRange.width / 2, 0, mRange.width, mRange.height, 1, 1,
                        callback.mFloatVal);

                // don't keep rotating if we've got a touch...
                if (!mIsTouchable)
                    return;

                // update rotation
                callback.mFloatVal += delta;
                if (callback.mFloatVal == 360)
                    callback.mFloatVal = 0;
            }
        };
        c.mGestureAction = new GestureAction() {
            /**
             * This is a touchable control...
             */
            @Override
            public boolean onTap(Vector3 v) {
                if (!c.mIsActive)
                    return false;
                callback.onEvent();
                return true;
            }
        };
        Lol.sGame.mCurrentLevel.mControls.add(c);
        Lol.sGame.mCurrentLevel.mTapControls.add(c);
        return c;
    }

    /**
     * Add an image to the heads-up display. Touching the image has no effect
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     */
    public static Control addImage(int x, int y, int width, int height, String imgName) {
        Control c = new Control(imgName, x, y, width, height);
        c.mIsTouchable = false;
        Lol.sGame.mCurrentLevel.mControls.add(c);
        return c;
    }

    /**
     * Add a control with callbacks for down, up, and pan
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
     * @param upCB
     *            The callback to run when the Control is released
     * @param dnCB
     *            The callback to run when the Control is pressed
     * @param mvCB
     *            The callback to run when there is a finger move (pan) on the
     *            Control
     */
    public static Control addPanCallbackControl(int x, int y, int width, int height, String imgName, final LolCallback upCB, final LolCallback dnCB, final LolCallback mvCB) {
        final Control c = new Control(imgName, x, y, width, height);
        // Pan only consists of pan-stop and pan events. That means we can't
        // capture a down-press or up-press that isn't also involved in a move.
        // To overcome this limitation, we'll make this BOTH a pan control and a
        // toggle control
        c.mGestureAction = new GestureAction() {
            /**
             * Toggle action: either call the "up" callback or the "down" callback
             */
            @Override
            public boolean toggle(boolean isUp, Vector3 touchVec) {
                // up event
                if (isUp) {
                    upCB.mUpLocation = touchVec.cpy();
                    upCB.onEvent();
                    mHolding = false;
                }
                // down event
                else {
                    mHolding = true;
                    dnCB.mDownLocation = touchVec.cpy();
                    dnCB.onEvent();
                }
                // toggle state
                mHolding = !isUp;
                return true;
            }
            /**
             * Finger move action: call the "pan" callback
             */
            @Override
            public boolean onPan(Vector3 touchVec, float deltaX, float deltaY) {
                // force a down event, if we didn't get one
                if (!mHolding) {
                    toggle(false, touchVec);
                    return true;
                }
                // pan event
                mvCB.mMoveLocation = touchVec.cpy();
                mvCB.onEvent();
                return true;
            }
            /**
             * Pan stop doesn't always trigger an up, so force one if necessary
             */
            @Override
            public boolean onPanStop(Vector3 touchVec) {
                // force an up event?
                if (mHolding) {
                    toggle(true, touchVec);
                    return true;
                }
                return false;
            }
        };
        Lol.sGame.mCurrentLevel.mControls.add(c);
        Lol.sGame.mCurrentLevel.mPanControls.add(c);
        Lol.sGame.mCurrentLevel.mToggleControls.add(c);
        return c;
    }

    /**
     * Render the control
     *
     * @param sb The SpriteBatch to use to draw the image
     */
    void render(SpriteBatch sb) {
        if (mIsActive && mImage != null)
            sb.draw(mImage, mRange.x, mRange.y, 0, 0, mRange.width, mRange.height, 1, 1, 0);
    }

    /**
     * Disable the control, so that it doesn't get displayed
     */
    public void setInactive() {
        mIsActive = false;
    }

    /**
     * Enable the control, so that it gets displayed again
     */
    public void setActive() {
        mIsActive = true;
    }

    /**
     * Disable touch for this control
     */
    public void disableTouch() {
        mIsTouchable = false;
    }

    /**
     * Enable touch for this control
     */
    public void enableTouch() {
        mIsTouchable = true;
    }
}
