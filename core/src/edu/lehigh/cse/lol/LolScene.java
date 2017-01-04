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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

/**
 * LolScene is the parent of all Scene types
 * <p>
 * A Scene consists of a physics world and a bunch of actors who exist within that world.  Notably,
 * a Scene can be rendered, which advances its physics world.
 * <p>
 * There is a close relationship between a BaseActor and a LolScene, namely that a BaseActor should
 * not need any scene functionality that is not present in LolScene.
 */
abstract class LolScene {
    /// A reference to the object that stores all of the sounds and images we use in the game
    protected final Media mMedia;
    /// A reference to the game-wide configuration variables
    protected final Config mConfig;

    /// The physics world in which all actors interact
    final World mWorld;
    /// Anything in the world that can be rendered, in 5 planes [-2, -1, 0, 1, 2]
    final ArrayList<ArrayList<Renderable>> mRenderables;

    /// This camera is for drawing actors that exist in the physics world
    final OrthographicCamera mCamera;
    /// The maximum x and y values of the camera
    final Vector2 mCamBound;

    /// We use this to avoid garbage collection when converting screen touches to camera coordinates
    final Vector3 mTouchVec;
    /// This callback is used to get a touched actor from the physics world
    final QueryCallback mTouchCallback;
    /// When there is a touch of an actor in the physics world, this is how we find it
    BaseActor mHitActor = null;

    /// Use this for determining bounds of text boxes
    private final GlyphLayout mGlyphLayout;

    /// Actions that run in response to a screen tap
    final ArrayList<TouchEventHandler> mTapHandlers;
    /// Events that get processed on the next render, then discarded
    final ArrayList<LolAction> mOneTimeEvents;
    /// Events that get processed on every render
    final ArrayList<LolAction> mRepeatEvents;

    /**
     * Construct a new scene
     *
     * @param media  All image and sound assets for the game
     * @param config The game-wide configuration
     */
    LolScene(Media media, Config config) {
        mMedia = media;
        mConfig = config;
        // compute the width and height, in meters
        float w = config.mWidth / config.mPixelMeterRatio;
        float h = config.mHeight / config.mPixelMeterRatio;
        // set up the game camera, with (0, 0) in the bottom left
        mCamera = new OrthographicCamera(w, h);
        mCamera.position.set(w / 2, h / 2, 0);
        mCamera.zoom = 1;

        // set up the event lists
        mOneTimeEvents = new ArrayList<>();
        mRepeatEvents = new ArrayList<>();

        // set default camera bounds
        mCamBound = new Vector2();
        mCamBound.set(w, h);

        // create a world with no default gravitational forces
        mWorld = new World(new Vector2(0, 0), true);

        // set up the containers for holding anything we can render
        mRenderables = new ArrayList<>(5);
        for (int i = 0; i < 5; ++i) {
            mRenderables.add(new ArrayList<Renderable>());
        }

        // set up the callback for finding out who in the physics world was touched
        mTouchVec = new Vector3();
        mTouchCallback = new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                // if the hit point is inside the fixture of the body we report
                // it
                if (fixture.testPoint(mTouchVec.x, mTouchVec.y)) {
                    BaseActor hs = (BaseActor) fixture.getBody().getUserData();
                    if (hs.mEnabled) {
                        mHitActor = hs;
                        return false;
                    }
                }
                return true;
            }
        };

        // prepare other collections
        mGlyphLayout = new GlyphLayout();
        mTapHandlers = new ArrayList<>();
    }

    /**
     * Add an actor to the level, putting it into the appropriate z plane
     *
     * @param actor  The actor to add
     * @param zIndex The z plane. valid values are -2, -1, 0, 1, and 2. 0 is the default.
     */
    void addActor(Renderable actor, int zIndex) {
        // Coerce index into legal range, then add the actor
        zIndex = (zIndex < -2) ? -2 : zIndex;
        zIndex = (zIndex > 2) ? 2 : zIndex;
        mRenderables.get(zIndex + 2).add(actor);
    }

    /**
     * Remove an actor from its z plane
     *
     * @param actor  The actor to remove
     * @param zIndex The z plane where it is expected to be
     */
    void removeActor(Renderable actor, int zIndex) {
        // Coerce index into legal range, then remove the actor
        zIndex = (zIndex < -2) ? -2 : zIndex;
        zIndex = (zIndex > 2) ? 2 : zIndex;
        mRenderables.get(zIndex + 2).remove(actor);
    }

    /**
     * Respond to a screen tap
     *
     * @param screenX The screen x coordinate of the touch
     * @param screenY The screen y coordinate of the touch
     * @return True if the tap was handled by the screen
     */
    boolean onTap(float screenX, float screenY) {
        // update mHitActor based on the touch
        getActorFromTouch(screenX, screenY);
        // Attempt to handle the tap on an actor
        if (mHitActor != null && mHitActor.onTap(mTouchVec))
            return true;
        // If that failed, attempt to handle the tap as a raw screen tap
        for (TouchEventHandler ga : mTapHandlers)
            if (ga.go(mTouchVec.x, mTouchVec.y))
                return true;
        return false;
    }

    /**
     * Given x and y coordinates on the screen, figure out which actor in this scene's world is
     * being touched, and update mHitActor appropriately
     *
     * @param screenX The screen x coordinate of the touch
     * @param screenY The screen y coordinate of the touch
     */
    void getActorFromTouch(float screenX, float screenY) {
        // Convert x/y to Hud coordinates, and check if they hit an actor
        mCamera.unproject(mTouchVec.set(screenX, screenY, 0));
        mHitActor = null;
        mWorld.QueryAABB(mTouchCallback, mTouchVec.x - 0.1f, mTouchVec.y - 0.1f, mTouchVec.x + 0.1f,
                mTouchVec.y + 0.1f);
    }

    /**
     * Report the position of a touch.  This is a useful debug mechanism, which allows a programmer
     * to click on the screen and then view the log in order to determine the corresponding position
     *
     * @param screenX The x coordinate on screen
     * @param screenY The y coordinate on screen
     * @param prefix  Some text to print as a prefix to the output message
     */
    void reportTouch(float screenX, float screenY, String prefix) {
        mCamera.unproject(mTouchVec.set(screenX, screenY, 0));
        Lol.message(mConfig, prefix + "Coordinates", mTouchVec.x + ", " + mTouchVec.y);
    }

    /**
     * Draw some text, centered on a specific coordinate
     *
     * @param centerX The x coordinate of the center point, in meters
     * @param centerY The y coordinate of the center point, in meters
     * @param message The text to display
     * @param bf      The BitmapFont object to use for the text's font
     * @param sb      The SpriteBatch used to render the text
     */
    private void renderTextCentered(float centerX, float centerY, String message, BitmapFont bf, SpriteBatch sb) {
        bf.getData().setScale(1 / mConfig.mPixelMeterRatio);
        mGlyphLayout.setText(bf, message);
        final float x = centerX - mGlyphLayout.width / 2;
        final float y = centerY + mGlyphLayout.height / 2;
        bf.draw(sb, message, x, y);
        bf.getData().setScale(1);
    }

    /**
     * Render this scene
     *
     * @param sb    The SpriteBatch used to render the scene
     * @param delta The time since the last render
     * @return True if the scene was rendered, false if it was not
     */
    abstract boolean render(SpriteBatch sb, float delta);

    /**
     * Draw some text, based on a bottom-left corner
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param message The text to display
     * @param bf      The BitmapFont object to use for the text's font
     * @param sb      The SpriteBatch used to render the text
     */
    private void renderText(float x, float y, String message, BitmapFont bf, SpriteBatch sb) {
        // NB: LibGDX uses top-left for text, so we need to convert to bottom-left
        bf.getData().setScale(1 / mConfig.mPixelMeterRatio);
        mGlyphLayout.setText(bf, message);
        bf.draw(sb, message, x, y + mGlyphLayout.height);
        bf.getData().setScale(1);
    }

    /**
     * Reset a scene by clearing all of its lists
     */
    void reset() {
        mTapHandlers.clear();
        mOneTimeEvents.clear();
        mRepeatEvents.clear();
        for (ArrayList<Renderable> a : mRenderables)
            a.clear();
    }

    /**
     * Add an image to the scene.  The image will not have any physics attached to it.
     *
     * @param x       The X coordinate of the bottom left corner, in meters
     * @param y       The Y coordinate of the bottom left corner, in meters
     * @param width   The image width, in meters
     * @param height  The image height, in meters
     * @param imgName The file name for the image, or ""
     * @param zIndex  The z index of the text
     * @return A Renderable of the image, so it can be enabled/disabled by program code
     */
    public Renderable makePicture(final float x, final float y, final float width,
                                  final float height, String imgName, int zIndex) {
        // set up the image to display
        // NB: this will fail gracefully (no crash) for invalid file names
        final TextureRegion tr = mMedia.getImage(imgName);
        Renderable r = new Renderable() {
            @Override
            public void onRender(SpriteBatch sb, float elapsed) {
                if (tr != null)
                    sb.draw(tr, x, y, 0, 0, width, height, 1, 1, 0);
            }
        };
        addActor(r, zIndex);
        return r;
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
    public Renderable addText(final float x, final float y, String fontName, String fontColor,
                              int fontSize, final String prefix, final String suffix,
                              final TextProducer tp, int zIndex) {
        // Choose a font color and get the BitmapFont
        final Color mColor = Color.valueOf(fontColor);
        final BitmapFont mFont = mMedia.getFont(fontName, fontSize);
        // Create a renderable that updates its text on every render, and add it to the scene
        Renderable d = new Renderable() {
            @Override
            void onRender(SpriteBatch sb, float delta) {
                mFont.setColor(mColor);
                String txt = prefix + tp.makeText() + suffix;
                renderText(x, y, txt, mFont, sb);
            }
        };
        addActor(d, zIndex);
        return d;
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
    public Renderable addTextCentered(final float centerX, final float centerY, String fontName,
                                      String fontColor, int fontSize, final String prefix,
                                      final String suffix, final TextProducer tp, int zIndex) {
        // Choose a font color and get the BitmapFont
        final Color mColor = Color.valueOf(fontColor);
        final BitmapFont mFont = mMedia.getFont(fontName, fontSize);
        // Create a renderable that updates its text on every render, and add it to the scene
        Renderable d = new Renderable() {
            @Override
            void onRender(SpriteBatch sb, float delta) {
                mFont.setColor(mColor);
                String txt = prefix + tp.makeText() + suffix;
                renderTextCentered(centerX, centerY, txt, mFont, sb);
            }
        };
        addActor(d, zIndex);
        return d;
    }

    /**
     * Add a button to the Scene, and provide code to run when the button is tapped
     *
     * @param x       The X coordinate of the bottom left corner
     * @param y       The Y coordinate of the bottom left corner
     * @param width   The width of the button
     * @param height  The height of the button
     * @param imgName The name of the image to display. Use "" for an invisible button
     * @param action  The action to run in response to a tap
     * @param zIndex  The z index for where this button will be drawn
     * @return The button that was created
     */
    public SceneActor addTapControl(float x, float y, float width, float height, String imgName,
                                    final TouchEventHandler action, int zIndex) {
        SceneActor c = new SceneActor(this, imgName, width, height);
        c.setBoxPhysics(BodyDef.BodyType.StaticBody, x, y);
        c.mTapHandler = action;
        action.mSource = c;
        addActor(c, zIndex);
        return c;
    }
}
