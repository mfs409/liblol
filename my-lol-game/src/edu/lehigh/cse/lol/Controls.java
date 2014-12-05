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

import edu.lehigh.cse.lol.Level.Action;
import edu.lehigh.cse.lol.Level.GestureAction;

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
    private Controls() {
    }

    /*
     * PUBLIC INTERFACE
     */

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
    public static Control addPauseButton(int x, int y, int width, int height, String imgName) {
        Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
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
    public static Control addMoveButton(int x, int y, int width, int height, String imgName,
            final PhysicsSprite entity, final float dx, final float dy) {
        final Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {

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
                    mHolding = false;
                } else {
                    mHolding = true;
                }
                return true;
            }
        };
        Level.sCurrent.mControls.add(c);
        Level.sCurrent.mToggleControls.add(c);
        Level.sCurrent.mRepeatEvents.add(new Action() {
            @Override
            public void go() {
                if (c.mGestureAction.mHolding) {
                    Vector2 v = entity.mBody.getLinearVelocity();
                    if (dx != 0)
                        v.x = dx;
                    if (dy != 0)
                        v.y = dy;
                    entity.updateVelocity(v.x, v.y);
                }
            }
        });
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
    public static Control addDownButton(int x, int y, int width, int height, String imgName, float rate,
            PhysicsSprite entity) {
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
    public static Control addUpButton(int x, int y, int width, int height, String imgName, float rate,
            PhysicsSprite entity) {
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
    public static Control addLeftButton(int x, int y, int width, int height, String imgName, float rate,
            PhysicsSprite entity) {
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
    public static Control addRightButton(int x, int y, int width, int height, String imgName, float rate,
            PhysicsSprite entity) {
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
    public static Control addTurboButton(int x, int y, int width, int height, String imgName, final int rateDownX,
            final int rateDownY, final int rateUpX, final int rateUpY, final PhysicsSprite entity) {
        final Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            @Override
            boolean toggle(boolean isUp, Vector3 touchVec) {
                mHolding = !isUp;
                return true;
            }
        };
        Level.sCurrent.mControls.add(c);
        Level.sCurrent.mToggleControls.add(c);
        Level.sCurrent.mRepeatEvents.add(new Action() {
            @Override
            public void go() {
                if (c.mGestureAction.mHolding) {
                    Vector2 v = entity.mBody.getLinearVelocity();
                    v.x = rateDownX;
                    v.y = rateDownY;
                    entity.updateVelocity(v.x, v.y);
                } else {
                    Vector2 v = entity.mBody.getLinearVelocity();
                    v.x = rateUpX;
                    v.y = rateUpY;
                    entity.updateVelocity(v.x, v.y);
                }
            }
        });
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
    public static Control addDampenedMotionButton(int x, int y, int width, int height, String imgName,
            final float rateX, final float rateY, final float dampening, final PhysicsSprite entity) {
        final Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            @Override
            boolean toggle(boolean isUp, Vector3 vv) {
                mHolding = !isUp;
                return true;
            }
        };
        Level.sCurrent.mControls.add(c);
        Level.sCurrent.mToggleControls.add(c);
        Level.sCurrent.mRepeatEvents.add(new Action() {
            @Override
            public void go() {
                if (c.mGestureAction.mHolding) {
                    Vector2 v = entity.mBody.getLinearVelocity();
                    v.x = rateX;
                    v.y = rateY;
                    entity.mBody.setLinearDamping(0);
                    entity.updateVelocity(v.x, v.y);
                } else {
                    entity.mBody.setLinearDamping(dampening);
                }
            }
        });
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
    public static Control addCrawlButton(int x, int y, int width, int height, String imgName, final Hero h) {
        Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
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
    public static Control addJumpButton(int x, int y, int width, int height, String imgName, final Hero h) {
        Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
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
     * @param offsetX
     *            specifies the x distance between the bottom left of the
     *            projectile and the bottom left of the hero throwing the
     *            projectile
     * @param offsetY
     *            specifies the y distance between the bottom left of the
     *            projectile and the bottom left of the hero throwing the
     *            projectile
     * @param velocityX
     *            The X velocity of the projectile when it is thrown
     * @param velocityY
     *            The Y velocity of the projectile when it is thrown
     */
    public static Control addThrowButton(int x, int y, int width, int height, String imgName, final Hero h,
            final int milliDelay, final float offsetX, final float offsetY, final float velocityX, final float velocityY) {
        final Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            @Override
            boolean toggle(boolean isUp, Vector3 touchVec) {
                mHolding = !isUp;
                return true;
            }
        };
        Level.sCurrent.mControls.add(c);
        Level.sCurrent.mToggleControls.add(c);
        Level.sCurrent.mRepeatEvents.add(new Action() {
            long mLastThrow;

            @Override
            public void go() {
                if (c.mGestureAction.mHolding) {
                    long now = System.nanoTime();
                    if (mLastThrow + milliDelay * 1000000 < now) {
                        mLastThrow = now;
                        Level.sCurrent.mProjectilePool.throwFixed(h, offsetX, offsetY, velocityX, velocityY);
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
     * @param offsetX
     *            specifies the x distance between the bottom left of the
     *            projectile and the bottom left of the hero throwing the
     *            projectile
     * @param offsetY
     *            specifies the y distance between the bottom left of the
     *            projectile and the bottom left of the hero throwing the
     *            projectile
     * @param velocityX
     *            The X velocity of the projectile when it is thrown
     * @param velocityY
     *            The Y velocity of the projectile when it is thrown
     */
    public static Control addSingleThrowButton(int x, int y, int width, int height, String imgName, final Hero h,
            final float offsetX, final float offsetY, final float velocityX, final float velocityY) {
        Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            @Override
            boolean onTap(Vector3 vv) {
                Level.sCurrent.mProjectilePool.throwFixed(h, offsetX, offsetY, velocityX, velocityY);
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
     * @param offsetX
     *            specifies the x distance between the bottom left of the
     *            projectile and the bottom left of the hero throwing the
     *            projectile
     * @param offsetY
     *            specifies the y distance between the bottom left of the
     *            projectile and the bottom left of the hero throwing the
     *            projectile
     */
    public static Control addVectorThrowButton(int x, int y, int width, int height, String imgName, final Hero h,
            final long milliDelay, final float offsetX, final float offsetY) {
        final Control c = new Control(imgName, x, y, width, height);
        final Vector3 v = new Vector3();
        c.mGestureAction = new GestureAction() {

            @Override
            boolean toggle(boolean isUp, Vector3 touchVec) {
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
            boolean onPan(Vector3 touchVec, float deltaX, float deltaY) {
                v.x = touchVec.x;
                v.y = touchVec.y;
                v.z = touchVec.z;
                return true;
            }
        };
        Level.sCurrent.mControls.add(c);
        // on toggle, we start or stop throwing; on pan, we change throw
        // direction
        Level.sCurrent.mToggleControls.add(c);
        Level.sCurrent.mPanControls.add(c);
        Level.sCurrent.mRepeatEvents.add(new Action() {
            long mLastThrow;

            @Override
            public void go() {
                if (c.mGestureAction.mHolding) {
                    long now = System.nanoTime();
                    if (mLastThrow + milliDelay * 1000000 < now) {
                        mLastThrow = now;
                        Level.sCurrent.mProjectilePool.throwAt(h.mBody.getPosition().x, h.mBody.getPosition().y, v.x,
                                v.y, h, offsetX, offsetY);
                    }
                }
            }
        });
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
     * @param offsetX
     *            specifies the x distance between the bottom left of the
     *            projectile and the bottom left of the hero throwing the
     *            projectile
     * @param offsetY
     *            specifies the y distance between the bottom left of the
     *            projectile and the bottom left of the hero throwing the
     *            projectile
     */
    public static Control addVectorSingleThrowButton(int x, int y, int width, int height, String imgName, final Hero h,
            final float offsetX, final float offsetY) {
        Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            @Override
            boolean onTap(Vector3 touchVec) {
                Level.sCurrent.mProjectilePool.throwAt(h.mBody.getPosition().x, h.mBody.getPosition().y, touchVec.x,
                        touchVec.y, h, offsetX, offsetY);
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
    public static Control addZoomOutButton(int x, int y, int width, int height, String imgName, final float maxZoom) {
        Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
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
    public static Control addZoomInButton(int x, int y, int width, int height, String imgName, final float minZoom) {
        Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
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
    public static Control addRotateButton(int x, int y, int width, int height, String imgName, final float rate,
            final Hero h) {
        final Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            @Override
            boolean toggle(boolean isUp, Vector3 touchVec) {
                mHolding = !isUp;
                return true;
            }
        };
        Level.sCurrent.mControls.add(c);
        Level.sCurrent.mToggleControls.add(c);
        Level.sCurrent.mRepeatEvents.add(new Action() {
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
     * @param sc
     *            The code to run when the button is pressed
     */
    public static Control addCallbackControl(int x, int y, int width, int height, String imgName,
            final SimpleCallback sc) {
        Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            @Override
            boolean onTap(Vector3 vv) {
                sc.onEvent();
                return true;
            }
        };
        Level.sCurrent.mControls.add(c);
        Level.sCurrent.mTapControls.add(c);
        return c;
    }

    /**
     * Add a button to the heads-up display that runs custom code via an
     * onControlPress callback, but the button only works once
     * 
     * @param x
     *            The X coordinate of the bottom left corner (in pixels)
     * @param y
     *            The Y coordinate of the bottom left corner (in pixels)
     * @param width
     *            The width of the image
     * @param height
     *            The height of the image
     * @param activeImgName
     *            The name of the image to display before the button is pressed.
     *            Use "" for an invisible button
     * @params inactiveImgName The name of the image to display after the button
     *         is pressed.
     * @param sc
     *            The code to run in response to the control press
     */
    public static Control addOneTimeCallbackControl(int x, int y, int width, int height, String activeImgName,
            final String inactiveImgName, final SimpleCallback sc) {
        final Control c = new Control(activeImgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            @Override
            boolean onTap(Vector3 vv) {
                sc.onEvent();
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
        Level.sCurrent.mControls.add(c);
        Level.sCurrent.mTapControls.add(c);
        return c;
    }

    /**
     * Allow panning to view more of the screen than is currently visible
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
    static public Control addPanControl(int x, int y, int width, int height, String imgName) {
        Control c = new Control(imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            /**
             * Use this to restore the chase entity when the Pan stops
             */
            PhysicsSprite oldChaseEntity;

            /**
             * Handle a pan stop event by restoring the chase entity, if there
             * was one
             * 
             * @param touchVec
             *            The x/y/z coordinates of the touch
             */
            @Override
            boolean onPanStop(Vector3 touchVec) {
                Level.setCameraChase(oldChaseEntity);
                oldChaseEntity = null;
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
                if (Level.sCurrent.mChaseEntity != null) {
                    oldChaseEntity = Level.sCurrent.mChaseEntity;
                    Level.sCurrent.mChaseEntity = null;
                }
                float x = Level.sCurrent.mGameCam.position.x - deltaX * .1f * Level.sCurrent.mGameCam.zoom;
                float y = Level.sCurrent.mGameCam.position.y + deltaY * .1f * Level.sCurrent.mGameCam.zoom;
                // if x or y is too close to MAX,MAX, stick with max acceptable
                // values
                if (x > Level.sCurrent.mCamBoundX - Lol.sGame.mConfig.getScreenWidth() * Level.sCurrent.mGameCam.zoom
                        / Physics.PIXEL_METER_RATIO / 2)
                    x = Level.sCurrent.mCamBoundX - Lol.sGame.mConfig.getScreenWidth() * Level.sCurrent.mGameCam.zoom
                            / Physics.PIXEL_METER_RATIO / 2;
                if (y > Level.sCurrent.mCamBoundY - Lol.sGame.mConfig.getScreenHeight() * Level.sCurrent.mGameCam.zoom
                        / Physics.PIXEL_METER_RATIO / 2)
                    y = Level.sCurrent.mCamBoundY - Lol.sGame.mConfig.getScreenHeight() * Level.sCurrent.mGameCam.zoom
                            / Physics.PIXEL_METER_RATIO / 2;

                // if x or y is too close to 0,0, stick with minimum acceptable
                // values
                //
                // NB: we do MAX before MIN, so that if we're zoomed out, we
                // show extra space at the top instead of the bottom
                if (x < Lol.sGame.mConfig.getScreenWidth() * Level.sCurrent.mGameCam.zoom / Physics.PIXEL_METER_RATIO
                        / 2)
                    x = Lol.sGame.mConfig.getScreenWidth() * Level.sCurrent.mGameCam.zoom / Physics.PIXEL_METER_RATIO
                            / 2;
                if (y < Lol.sGame.mConfig.getScreenHeight() * Level.sCurrent.mGameCam.zoom / Physics.PIXEL_METER_RATIO
                        / 2)
                    y = Lol.sGame.mConfig.getScreenHeight() * Level.sCurrent.mGameCam.zoom / Physics.PIXEL_METER_RATIO
                            / 2;

                // update the camera position
                Level.sCurrent.mGameCam.position.set(x, y, 0);
                return true;
            }
        };
        Level.sCurrent.mPanControls.add(c);
        return c;
    }

    /**
     * Allow pinch-to-zoom
     * 
     * TODO: this isn't quite right, because we are treating zoom like a
     * touchable control, but it's really a whole-screen affair...
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
     *            The maximum zoom (out) factor. 8 is usually a good choice.
     * @param minZoom
     *            The minimum zoom (int) factor. .25f is usually a good choice.
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
                lastZoom = Level.sCurrent.mGameCam.zoom;
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
                    Level.sCurrent.mGameCam.zoom = newZoom;
                return false;
            }
        };
        Level.sCurrent.mZoomControls.add(c);
        return c;
    }

    /**
     * Add an image to the heads-up display that changes its clipping rate to
     * seem to grow vertically, without stretching. Touching the image causes
     * its scale (0-100) to be sent to a ControlPressEntity event
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
     *            The id to use when this generates an onControlPressCallback
     *            event
     * @param entity
     *            An entity that can be passed to the onControlPressCallback
     *            event
     */
    public static Control addVerticalBar(final int x, final int y, final int width, final int height, String imgName,
            final int id, final PhysicsSprite entity) {
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
                sb.draw(mImage.getTexture(), x, y, width / 2, height / 2, width, (height * (int) mVal) / 100, 1, 1, 0,
                        mTrueX, 0, mTrueWidth, (mTrueHeight * (int) mVal) / 100, false, true);

                // don't keep showing anything if we've already received a
                // touch...
                if (!mIsTouchable)
                    return;

                // update size
                if (mVal == 100)
                    mGrow = false;
                if (mVal == 0)
                    mGrow = true;
                mVal = mVal + (mGrow ? 1 : -1);
            }
        };
        c.mGestureAction = new GestureAction() {
            /**
             * This is a touchable control...
             */
            @Override
            boolean onTap(Vector3 v) {
                if (!c.mIsActive || !c.mIsTouchable)
                    return false;
                Lol.sGame.onControlPressEntityCallback(id, (int) c.mVal, entity, Lol.sGame.mCurrLevelNum);
                return true;
            }
        };
        // add to hud
        Level.sCurrent.mControls.add(c);
        Level.sCurrent.mTapControls.add(c);
        return c;
    }

    /**
     * Add a rotating button that generates a ControlPressEntity event and
     * passes the rotation to the handler.
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
     * @param delta
     *            Amount of rotation to add during each fraction of a second
     * @param id
     *            The id to use when this generates an onControlPressCallback
     *            event
     * @param entity
     *            An entity that can be passed to the onControlPressCallback
     *            event
     */
    public static Control addRotator(final int x, final int y, final int width, final int height, String imgName,
            final float delta, final int id, final PhysicsSprite entity) {
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
                sb.draw(mImage, mRange.x, mRange.y, mRange.width / 2, 0, mRange.width, mRange.height, 1, 1, mVal);

                // don't keep rotating if we've got a touch...
                if (!mIsTouchable)
                    return;

                // update rotation
                mVal += delta;
                if (mVal == 360)
                    mVal = 0;
            }
        };
        c.mGestureAction = new GestureAction() {
            /**
             * This is a touchable control...
             */
            @Override
            boolean onTap(Vector3 v) {
                if (!c.mIsActive)
                    return false;
                Lol.sGame.onControlPressEntityCallback(id, c.mVal, entity, Lol.sGame.mCurrLevelNum);
                return true;
            }
        };
        Level.sCurrent.mControls.add(c);
        Level.sCurrent.mTapControls.add(c);
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
    public static Control addImage(int x, int y, int width, int height, String imgName) {
        Control c = new Control(imgName, x, y, width, height);
        c.mIsTouchable = false;
        Level.sCurrent.mControls.add(c);
        return c;
    }
}
