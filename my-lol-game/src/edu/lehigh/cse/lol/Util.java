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
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Contact;

import java.util.Random;

/**
 * The Util class stores a few helper functions that we use inside of LOL, and a
 * few simple wrappers that we give to the game developer
 */
public class Util {

    /**
     * When there is a gesture on the screen, we will convert the event's
     * coordinates to world coordinates, then use this to handle it. This object
     * can be attached to PhysicsSprites, Controls, or to the Level itself, to
     * specify a handler for certain events.
     */
    static class GestureAction {
        /**
         * We offer a HOLD/RELEASE gesture. This flag tells us if we're in a
         * hold event.
         */
        boolean mHolding;

        /**
         * Handle a drag event
         * 
         * @param touchVec
         *            The x/y/z coordinates of the touch
         */
        boolean onDrag(Vector3 touchVec) {
            return false;
        }

        /**
         * Handle a down press (hopefully to turn it into a hold/release)
         * 
         * @param touchVec
         *            The x/y/z coordinates of the touch
         */
        public boolean onDown(Vector3 touchVec) {
            return false;
        }

        /**
         * Handle an up press (hopefully to turn it into a release)
         * 
         * @param touchVec
         *            The x/y/z coordinates of the touch
         */
        public boolean onUp(Vector3 touchVec) {
            return false;
        }

        /**
         * Handle a tap event
         * 
         * @param touchVec
         *            The x/y/z coordinates of the touch
         */
        boolean onTap(Vector3 touchVec) {
            return false;
        }

        /**
         * Handle a pan event
         * 
         * @param touchVec
         *            The x/y/z world coordinates of the touch
         * @param deltaX
         *            the change in X scale, in screen coordinates
         * @param deltaY
         *            the change in Y scale, in screen coordinates
         */
        boolean onPan(Vector3 touchVec, float deltaX, float deltaY) {
            return false;
        }

        /**
         * Handle a pan stop event
         * 
         * @param touchVec
         *            The x/y/z coordinates of the touch
         */
        boolean onPanStop(Vector3 touchVec) {
            return false;
        }

        /**
         * Handle a fling event
         * 
         * @param touchVec
         *            The x/y/z coordinates of the touch
         */
        boolean onFling(Vector3 touchVec) {
            return false;
        }

        /**
         * Handle a toggle event. This is usually built from a down and an up.
         * 
         * @param touchVec
         *            The x/y/z coordinates of the touch
         */
        boolean toggle(boolean isUp, Vector3 touchVec) {
            return false;
        }

        /**
         * Handle a zoom event
         * 
         * @param initialDistance
         *            The distance between fingers when the pinch started
         * @param distance
         *            The current distance between fingers
         */
        boolean zoom(float initialDistance, float distance) {
            return false;
        }
    }

    /**
     * Custom camera that can do parallax... taken directly from GDX tests
     */
    static class ParallaxCamera extends OrthographicCamera {
        /**
         * This matrix helps us compute the view
         */
        private final Matrix4 parallaxView = new Matrix4();

        /**
         * This matrix helps us compute the camera.combined
         */
        private final Matrix4 parallaxCombined = new Matrix4();

        /**
         * A temporary vector for doing the calculations
         */
        private final Vector3 tmp = new Vector3();

        /**
         * Another temporary vector for doing the calculations
         */
        private final Vector3 tmp2 = new Vector3();

        /**
         * The constructor simply forwards to the OrthographicCamera constructor
         * 
         * @param viewportWidth
         *            Width of the camera
         * @param viewportHeight
         *            Height of the camera
         */
        ParallaxCamera(float viewportWidth, float viewportHeight) {
            super(viewportWidth, viewportHeight);
        }

        /**
         * This is how we calculate the position of a parallax camera
         */
        Matrix4 calculateParallaxMatrix(float parallaxX, float parallaxY) {
            update();
            tmp.set(position);
            tmp.x *= parallaxX;
            tmp.y *= parallaxY;

            parallaxView.setToLookAt(tmp, tmp2.set(tmp).add(direction), up);
            parallaxCombined.set(projection);
            Matrix4.mul(parallaxCombined.val, parallaxView.val);
            return parallaxCombined;
        }
    }

    /**
     * This interface allows items that can be displayed on the screen to
     * describe how they ought to be displayed. This allows us, for example, to
     * let a text item describe how its display value should change over time.
     */
    interface Renderable {
        /**
         * Render something to the screen
         * 
         * @param sb
         *            The SpriteBatch to use for rendering
         * @param elapsed
         *            The time since the last render
         */
        void render(SpriteBatch sb, float elapsed);
    }

    /**
     * When an Actor collides with another Actor, and that
     * collision is intended to cause some custom code to run, we use this
     * interface
     */
    interface CollisionCallback {
        /**
         * Respond to a collision with a PhysicsSprite. Note that one of the
         * collision entities is not named; it should be clear from the context
         * in which this was constructed.
         * 
         * @param actor
         *            The PhysicsSprite involved in the collision
         * @param contact
         *            A description of the contact, in case it is useful
         */
        void go(final Actor actor, Contact contact);
    }

    /**
     * Wrapper for actions that we generate and then want handled during the
     * render loop
     */
    interface Action {
        void go();
    }

    /**
     * AnimationDriver is an internal class that PhysicsSprites can use to
     * figure out which frame of an animation to show next
     */
    static class AnimationDriver {
        /**
         * The images that comprise the current animation will be the elements
         * of this array
         */
        TextureRegion[] mImages;

        /**
         * The index to display from mImages for the case where there is no
         * active animation. This is useful for animateByGoodieCount.
         */
        int mImageIndex;

        /**
         * The currently running animation
         */
        Animation mCurrentAnimation;

        /**
         * The frame of the currently running animation that is being displayed
         */
        private int mCurrentAnimationFrame;

        /**
         * The amout of time for which the current frame has been displayed
         */
        private float mCurrentAnimationTime;

        /**
         * Build an AnimationDriver by giving it an imageName. This allows us to
         * use AnimationDriver for displaying non-animated images
         * 
         * @param imgName
         *            The name of the image file to use
         */
        AnimationDriver(String imgName) {
            updateImage(imgName);
        }

        /**
         * Set the current animation, and reset internal fields
         * 
         * @param a
         *            The animation to start using
         */
        void setCurrentAnimation(Animation a) {
            mCurrentAnimation = a;
            mCurrentAnimationFrame = 0;
            mCurrentAnimationTime = 0;
        }

        /**
         * Change the source for the default image to display
         * 
         * @param imgName
         *            The name of the image file to use
         */
        void updateImage(String imgName) {
            mImages = Media.getImage(imgName);
            mImageIndex = 0;
        }

        /**
         * Change the index of the default image to display
         * 
         * @param i
         *            The index to use
         */
        void setIndex(int i) {
            mImageIndex = i;
        }

        /**
         * Request a random index from the mImages array to pick an image to
         * display
         */
        void pickRandomIndex() {
            mImageIndex = Util.getRandom(mImages.length);
        }

        /**
         * When a PhysicsSprite renders, we use this method to figure out which
         * textureRegion to display
         * 
         * @param delta
         *            The time since the last render
         * @return The TextureRegion to display
         */
        TextureRegion getTr(float delta) {
            if (mCurrentAnimation == null) {
                if (mImages == null)
                    return null;
                return mImages[mImageIndex];
            }
            mCurrentAnimationTime += delta;
            long millis = (long) (1000 * mCurrentAnimationTime);
            // are we still in this frame?
            if (millis <= mCurrentAnimation.mDurations[mCurrentAnimationFrame]) {
                return mCurrentAnimation.mCells[mCurrentAnimation.mFrames[mCurrentAnimationFrame]];
            }
            // are we on the last frame, with no loop? If so, stay where we
            // are...
            else if (mCurrentAnimationFrame == mCurrentAnimation.mNextCell - 1 && !mCurrentAnimation.mLoop) {
                return mCurrentAnimation.mCells[mCurrentAnimation.mFrames[mCurrentAnimationFrame]];
            }
            // else advance, reset, go
            else {
                mCurrentAnimationFrame = (mCurrentAnimationFrame + 1) % mCurrentAnimation.mNextCell;
                mCurrentAnimationTime = 0;
                return mCurrentAnimation.mCells[mCurrentAnimation.mFrames[mCurrentAnimationFrame]];
            }
        }
    }

    /**
     * This object holds the configuration information for a Parallax layer.
     */
    static class ParallaxLayer {
        /**
         * How fast should this layer scroll in the X dimension
         */
        final float mXSpeed;

        /**
         * How fast should it scroll in Y
         */
        final float mYSpeed;

        /**
         * The image to display
         */
        final TextureRegion mImage;

        /**
         * How much X offset when drawing this (only useful for Y repeat)
         */
        final float mXOffset;

        /**
         * How much Y offset when drawing this (only useful for X repeat)
         */
        final float mYOffset;

        /**
         * Loop in X?
         */
        boolean mXRepeat;

        /**
         * Loop in Y?
         */
        boolean mYRepeat;

        /**
         * Width of the image
         */
        float mWidth;

        /**
         * Height of the image
         */
        float mHeight;

        /**
         * Simple constructor... just set the fields
         * 
         * @param xSpeed
         *            Speed that the layer moves in the X dimension
         * @param ySpeed
         *            Y speed
         * @param tr
         *            Image to use
         * @param xOffset
         *            X offset
         * @param yOffset
         *            Y offset
         * @param width
         *            Width of the image
         * @param height
         *            Height of the image
         */
        ParallaxLayer(float xSpeed, float ySpeed, TextureRegion tr, float xOffset, float yOffset, float width,
                float height) {
            mXSpeed = xSpeed;
            mYSpeed = ySpeed;
            mImage = tr;
            mXOffset = xOffset;
            mYOffset = yOffset;
            mWidth = width;
            mHeight = height;
        }
    }

    /**
     * RouteDriver is an internal class, used by LOL to determine placement for an
     * Entity whose motion is controlled by a Route.
     */
    static class RouteDriver {

        /**
         * The route that is being applied
         */
        private final Route mRoute;

        /**
         * The entity to which the route is being applied
         */
        private final Actor mEntity;

        /**
         * The speed at which the entity moves along the route
         */
        private final float mRouteVelocity;

        /**
         * When the entity reaches the end of the route, should it start again?
         */
        private final boolean mRouteLoop;

        /**
         * A temp for computing position
         */
        private final Vector2 mRouteVec = new Vector2();

        /**
         * Is the route still running?
         */
        private boolean mRouteDone;

        /**
         * Index of the next point in the route
         */
        private int mNextRouteGoal;

        /**
         * The constructor actually gets the route motion started
         * 
         * @param route
         *            The route to apply
         * @param velocity
         *            The speed at which the entity moves
         * @param loop
         *            Should the route repeat when it completes?
         * @param entity
         *            The entity to which the route should be applied
         */
        RouteDriver(Route route, float velocity, boolean loop, Actor entity) {
            mRoute = route;
            mRouteVelocity = velocity;
            mRouteLoop = loop;
            mEntity = entity;
            // kick off the route, indicate that we aren't all done yet
            startRoute();
            mRouteDone = false;
        }

        /**
         * Stop a route
         */
        void haltRoute() {
            mRouteDone = true;
            // NB: third parameter doesn't matter, because the entity isn't a static
            // body, so its bodytype won't change.
            mEntity.setAbsoluteVelocity(0, 0, false);
        }

        /**
         * Begin running a route
         */
        private void startRoute() {
            // move to the starting point
            mEntity.mBody.setTransform(mRoute.mXIndices[0] + mEntity.mSize.x / 2,
                    mRoute.mYIndices[0] + mEntity.mSize.y / 2, 0);
            // set up our next goal, start moving toward it
            mNextRouteGoal = 1;
            mRouteVec.x = mRoute.mXIndices[mNextRouteGoal] - mEntity.getXPosition();
            mRouteVec.y = mRoute.mYIndices[mNextRouteGoal] - mEntity.getYPosition();
            mRouteVec.nor();
            mRouteVec.scl(mRouteVelocity);
            mEntity.mBody.setLinearVelocity(mRouteVec);
        }

        /**
         * Internal method for figuring out where we need to go next when driving a
         * route
         */
        void drive() {
            // quit if we're done and we don't loop
            if (mRouteDone)
                return;
            // if we haven't passed the goal, keep going. we tell if we've passed
            // the goal by comparing the magnitudes of the vectors from source (s)
            // to here and from goal (g) to here
            float sx = mRoute.mXIndices[mNextRouteGoal - 1] - mEntity.getXPosition();
            float sy = mRoute.mYIndices[mNextRouteGoal - 1] - mEntity.getYPosition();
            float gx = mRoute.mXIndices[mNextRouteGoal] - mEntity.getXPosition();
            float gy = mRoute.mYIndices[mNextRouteGoal] - mEntity.getYPosition();
            boolean sameXSign = (gx >= 0 && sx >= 0) || (gx <= 0 && sx <= 0);
            boolean sameYSign = (gy >= 0 && sy >= 0) || (gy <= 0 && sy <= 0);
            if (((gx == gy) && (gx == 0)) || (sameXSign && sameYSign)) {
                mNextRouteGoal++;
                if (mNextRouteGoal == mRoute.mPoints) {
                    // reset if it's a loop, else terminate Route
                    if (mRouteLoop) {
                        startRoute();
                    } else {
                        mRouteDone = true;
                        mEntity.mBody.setLinearVelocity(0, 0);
                    }
                } else {
                    // advance to next point
                    mRouteVec.x = mRoute.mXIndices[mNextRouteGoal] - mEntity.getXPosition();
                    mRouteVec.y = mRoute.mYIndices[mNextRouteGoal] - mEntity.getYPosition();
                    mRouteVec.nor();
                    mRouteVec.scl(mRouteVelocity);
                    mEntity.mBody.setLinearVelocity(mRouteVec);
                }
            }
            // NB: 'else keep going at current velocity'
        }
    }

    /**
     * A random number generator... We provide this so that new game developers
     * don't create lots of Random()s throughout their code
     */
    private static Random sGenerator = new Random();

    /**
     * Create a Renderable that consists of an image
     * 
     * @param x
     *            The X coordinate of the bottom left corner, in pixels
     * @param y
     *            The Y coordinate of the bottom left corner, in pixels
     * @param width
     *            The image width, in pixels
     * @param height
     *            The image height, in pixels
     * @param imgName
     *            The file name for the image, or ""
     * @return A Renderable of the image
     */
    static Util.Renderable makePicture(final float x, final float y, final float width, final float height,
            String imgName) {
        // set up the image to display
        //
        // NB: this will fail gracefully (no crash) for invalid file names
        TextureRegion[] trs = Media.getImage(imgName);
        final TextureRegion tr = (trs != null) ? trs[0] : null;
        return new Util.Renderable() {
            @Override
            public void render(SpriteBatch sb, float elapsed) {
                if (tr != null)
                    sb.draw(tr, x, y, 0, 0, width, height, 1, 1, 0);
            }
        };
    }

    /**
     * Create a Renderable that consists of some text to draw
     * 
     * @param x
     *            The X coordinate of the bottom left corner, in pixels
     * @param y
     *            The Y coordinate of the bottom left corner, in pixels
     * @param message
     *            The text to display... note that it can't change on the fly
     * @param red
     *            The red component of the font color (0-255)
     * @param green
     *            The green component of the font color (0-255)
     * @param blue
     *            The blue component of the font color (0-255)
     * @param fontName
     *            The font to use
     * @param size
     *            The font size
     * @return A Renderable of the text
     */
    static Util.Renderable makeText(final int x, final int y, final String message, final int red, final int green,
            final int blue, String fontName, int size) {
        final BitmapFont bf = Media.getFont(fontName, size);
        return new Util.Renderable() {
            @Override
            public void render(SpriteBatch sb, float elapsed) {
                bf.setColor(((float) red) / 256, ((float) green) / 256, ((float) blue) / 256, 1);
                bf.drawMultiLine(sb, message, x, y + bf.getMultiLineBounds(message).height);
            }
        };
    }

    /**
     * Create a Renderable that consists of some text to draw. The text will be
     * centered vertically and horizontally
     * 
     * @param message
     *            The text to display... note that it can't change on the fly
     * @param red
     *            The red component of the font color (0-255)
     * @param green
     *            The green component of the font color (0-255)
     * @param blue
     *            The blue component of the font color (0-255)
     * @param fontName
     *            The font to use
     * @param size
     *            The font size
     * @return A Renderable of the text
     */
    static Util.Renderable makeText(final String message, final int red, final int green, final int blue,
            String fontName, int size) {
        final BitmapFont bf = Media.getFont(fontName, size);
        final float x = Lol.sGame.mWidth / 2 - bf.getMultiLineBounds(message).width / 2;
        final float y = Lol.sGame.mHeight / 2 + bf.getMultiLineBounds(message).height / 2;
        return new Util.Renderable() {
            @Override
            public void render(SpriteBatch sb, float elapsed) {
                bf.setColor(((float) red) / 256, ((float) green) / 256, ((float) blue) / 256, 1);
                bf.drawMultiLine(sb, message, x, y);
            }
        };
    }

    /**
     * Instead of using Gdx.app.log directly, and potentially writing a lot of
     * debug info in a production setting, we use this to only dump to the log
     * when debug mode is on
     * 
     * @param tag
     *            The message tag
     * @param text
     *            The message text
     */
    static void message(String tag, String text) {
        if (Lol.sGame.mShowDebugBoxes)
            Gdx.app.log(tag, text);
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Generate a random number x such that 0 <= x < max
     * 
     * @param max
     *            The largest number returned will be one less than max
     * @return a random integer
     */
    public static int getRandom(int max) {
        return sGenerator.nextInt(max);
    }

    /**
     * Draw a box on the scene Note: the box is actually four narrow rectangles
     * 
     * @param x0
     *            X coordinate of top left corner
     * @param y0
     *            Y coordinate of top left corner
     * @param x1
     *            X coordinate of bottom right corner
     * @param y1
     *            Y coordinate of bottom right corner
     * @param imgName
     *            name of the image file to use when drawing the rectangles
     * @param density
     *            Density of the rectangle. When in doubt, use 1
     * @param elasticity
     *            Elasticity of the rectangle. When in doubt, use 0
     * @param friction
     *            Friction of the rectangle. When in doubt, use 1
     */
    static public void drawBoundingBox(float x0, float y0, float x1, float y1, String imgName, float density,
            float elasticity, float friction) {
        // draw four rectangles and we're good
        Obstacle bottom = Obstacle.makeAsBox(x0 - 1, y0 - 1, Math.abs(x0 - x1) + 2, 1, imgName);
        bottom.setPhysics(density, elasticity, friction);

        Obstacle top = Obstacle.makeAsBox(x0 - 1, y1, Math.abs(x0 - x1) + 2, 1, imgName);
        top.setPhysics(density, elasticity, friction);

        Obstacle left = Obstacle.makeAsBox(x0 - 1, y0 - 1, 1, Math.abs(y0 - y1) + 2, imgName);
        left.setPhysics(density, elasticity, friction);

        Obstacle right = Obstacle.makeAsBox(x1, y0 - 1, 1, Math.abs(y0 - y1) + 2, imgName);
        right.setPhysics(density, elasticity, friction);
    }

    /**
     * Draw a picture on the current level Note: the order in which this is
     * called relative to other entities will determine whether they go under or
     * over this picture.
     * 
     * @param x
     *            X coordinate of bottom left corner
     * @param y
     *            Y coordinate of bottom left corner
     * @param width
     *            Width of the picture
     * @param height
     *            Height of this picture
     * @param imgName
     *            Name of the picture to display
     * @param zIndex
     *            The z index of the image. There are 5 planes: -2, -2, 0, 1,
     *            and 2. By default, everything goes to plane 0
     */
    public static void drawPicture(final float x, final float y, final float width, final float height,
            final String imgName, int zIndex) {
        Level.sCurrent.addSprite(Util.makePicture(x, y, width, height, imgName), zIndex);
    }

    /**
     * Draw some text on the current level
     * 
     * Note: the order in which this is called relative to other entities will
     * determine whether they go under or over this text.
     * 
     * @param x
     *            X coordinate of bottom left corner of the text
     * @param y
     *            Y coordinate of bottom left corner of the text
     * @param text
     *            The text to display
     * @param red
     *            The red component of the color (0-255)
     * @param green
     *            The green component of the color (0-255)
     * @param blue
     *            The blue component of the color (0-255)
     * @param fontName
     *            The name of the font file to use
     * @param size
     *            The font size to use
     * @param zIndex
     *            The z index of the image. There are 5 planes: -2, -2, 0, 1,
     *            and 2. By default, everything goes to plane 0
     */
    public static void drawText(final float x, final float y, final String text, final int red, final int green,
            final int blue, String fontName, int size, int zIndex) {
        final BitmapFont bf = Media.getFont(fontName, size);
        Util.Renderable r = new Util.Renderable() {
            @Override
            public void render(SpriteBatch sb, float elapsed) {
                bf.setColor(((float) red) / 256, ((float) green) / 256, ((float) blue) / 256, 1);
                bf.setScale(1 / Physics.PIXEL_METER_RATIO);
                bf.drawMultiLine(sb, text, x, y + bf.getMultiLineBounds(text).height);
                bf.setScale(1);
            }
        };
        Level.sCurrent.addSprite(r, zIndex);
    }

    /**
     * Draw some text on the current level
     * 
     * Note: the order in which this is called relative to other entities will
     * determine whether they go under or over this text.
     * 
     * @param centerX
     *            X coordinate of center of the text
     * @param centerY
     *            Y coordinate of center of the text
     * @param text
     *            The text to display
     * @param red
     *            The red component of the color (0-255)
     * @param green
     *            The green component of the color (0-255)
     * @param blue
     *            The blue component of the color (0-255)
     * @param fontName
     *            The name of the font file to use
     * @param size
     *            The font size to use
     * @param zIndex
     *            The z index of the image. There are 5 planes: -2, -2, 0, 1,
     *            and 2. By default, everything goes to plane 0
     */
    public static void drawTextCentered(final float centerX, final float centerY, final String text, final int red,
            final int green, final int blue, String fontName, int size, int zIndex) {
        final BitmapFont bf = Media.getFont(fontName, size);

        // figure out the image dimensions
        bf.setScale(1 / Physics.PIXEL_METER_RATIO);
        final float w = bf.getMultiLineBounds(text).width;
        final float h = bf.getMultiLineBounds(text).height;
        bf.setScale(1);

        // describe how to render it
        Util.Renderable r = new Util.Renderable() {
            @Override
            public void render(SpriteBatch sb, float elapsed) {
                bf.setColor(((float) red) / 256, ((float) green) / 256, ((float) blue) / 256, 1);
                bf.setScale(1 / Physics.PIXEL_METER_RATIO);
                bf.drawMultiLine(sb, text, centerX - w / 2, centerY + h / 2);
                bf.setScale(1);
            }
        };
        Level.sCurrent.addSprite(r, zIndex);
    }
}
