package edu.lehigh.cse.lol;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

/**
 * LolScene is the parent of all Scene types
 * <p>
 * A Scene consists of a physics world and a bunch of actors who exist within that world.  Notably,
 * a Scene can be rendered, which advances the physics world.
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

    /// This camera is for drawing actors that exist in the physics world
    final OrthographicCamera mCamera;

    /// The maximum x and y values of the camera
    final Vector2 mCamBound;

    /// Anything in the world that can be rendered, in 5 planes [-2, -1, 0, 1, 2]
    final ArrayList<ArrayList<Renderable>> mRenderables;

    /// We use this to avoid garbage collection when converting screen touches to camera coordinates
    final Vector3 mTouchVec;

    /// This callback is used to get a touched actor from the physics world
    final QueryCallback mTouchCallback;

    /// When there is a touch of an actor in the physics world, this is how we find it
    BaseActor mHitActor = null;

    /// Use this for determining bounds of text boxes
    final GlyphLayout mGlyphLayout;

    /// Actors may need to set callbacks to run on a screen touch. If so, they can use these
    final ArrayList<TouchEventHandler> mTapHandlers;

    /// Events that get processed on the next render, then discarded
     final ArrayList<LolAction> mOneTimeEvents;

    /// Events that get processed on every render
     final ArrayList<LolAction> mRepeatEvents;

    LolScene(Media media, Config config) {
        float w = config.mWidth / config.PIXEL_METER_RATIO;
        float h = config.mHeight / config.PIXEL_METER_RATIO;
        mMedia = media;
        mConfig = config;
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

        mTouchVec = new Vector3();
        // set up the callback for finding out who in the physics world was
        // touched
        mTouchCallback = new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                // if the hit point is inside the fixture of the body we report
                // it
                if (fixture.testPoint(mTouchVec.x, mTouchVec.y)) {
                    BaseActor hs = (BaseActor) fixture.getBody().getUserData();
                    if (hs.mVisible) {
                        mHitActor = hs;
                        return false;
                    }
                }
                return true;
            }
        };

        mGlyphLayout = new GlyphLayout();
        mTapHandlers = new ArrayList<>();
    }

    /**
     * Add an actor to the level, putting it into the appropriate z plane
     *
     * @param actor  The actor to add
     * @param zIndex The z plane. valid values are -2, -1, 0, 1, and 2. 0 is the
     *               default.
     */
    void addActor(Renderable actor, int zIndex) {
        // Coerce index into legal range
        if (zIndex < -2)
            zIndex = -2;
        if (zIndex > 2)
            zIndex = 2;
        mRenderables.get(zIndex + 2).add(actor);
    }

    /**
     * Remove an actor from its z plane
     *
     * @param actor  The actor to remove
     * @param zIndex The z plane where it is expected to be
     */
    void removeActor(Renderable actor, int zIndex) {
        // Coerce index into legal range
        if (zIndex < -2) {
            zIndex = -2;
        }
        if (zIndex > 2) {
            zIndex = 2;
        }
        mRenderables.get(zIndex + 2).remove(actor);
    }

    boolean onTap(float x, float y) {
        // check if we tapped an actor
        mHitActor = null;
        mCamera.unproject(mTouchVec.set(x, y, 0));
        mWorld.QueryAABB(mTouchCallback, mTouchVec.x - 0.1f, mTouchVec.y - 0.1f, mTouchVec.x + 0.1f,
                mTouchVec.y + 0.1f);
        if (mHitActor != null && mHitActor.onTap(mTouchVec))
            return true;

        // is this a raw screen tap?
        for (TouchEventHandler ga : mTapHandlers)
            if (ga.go(mTouchVec.x, mTouchVec.y))
                return true;
        return false;
    }

    /**
     * Create a Renderable that consists of some text to draw
     *
     * @param x        The X coordinate of the bottom left corner, in pixels
     * @param y        The Y coordinate of the bottom left corner, in pixels
     * @param message  The text to display... note that it can't change on the fly
     * @param fontName The font to use
     * @param size     The font size
     * @return A Renderable of the text
     */
    Renderable makeText(final int x, final int y, final String message, final String fontColor, String fontName, int size) {
        final BitmapFont bf = mMedia.getFont(fontName, size);
        return new Renderable() {
            @Override
            public void render(SpriteBatch sb, float elapsed) {
                bf.setColor(Color.valueOf(fontColor));
                mGlyphLayout.setText(bf, message);
                bf.draw(sb, message, x, y + mGlyphLayout.height);
            }
        };
    }

    /**
     * Create a Renderable that consists of some text to draw. The text will be
     * centered vertically and horizontally on the screen
     *
     * @param message  The text to display... note that it can't change on the fly
     * @param fontName The font to use
     * @param size     The font size
     * @return A Renderable of the text
     */
    // TODO: this is broken: if we have a non-1 pixel-to-meter ratio, then we aren't centering well
    //       we should center on a provided x/y coordinate, and we need to decide how to handle
    //       pixel ratios
    Renderable makeTextCentered(float centerX, float centerY, final String message, final String fontColor, String fontName, int size) {
        final BitmapFont bf = mMedia.getFont(fontName, size);
        mGlyphLayout.setText(bf, message);
        final float x = centerX / 2 - mGlyphLayout.width / 2;
        final float y = centerY / 2 + mGlyphLayout.height / 2;
        return new Renderable() {
            @Override
            public void render(SpriteBatch sb, float elapsed) {
                bf.setColor(Color.valueOf(fontColor));
                bf.draw(sb, message, x, y);
            }
        };
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
    void drawTextTransposed(int x, int y, String message, BitmapFont bf, SpriteBatch sb) {
        mGlyphLayout.setText(bf, message);
        bf.draw(sb, message, x, y + mGlyphLayout.height);
    }

    /**
     * Create a Renderable that consists of an image
     *
     * @param x       The X coordinate of the bottom left corner, in pixels
     * @param y       The Y coordinate of the bottom left corner, in pixels
     * @param width   The image width, in pixels
     * @param height  The image height, in pixels
     * @param imgName The file name for the image, or ""
     * @return A Renderable of the image
     */
    Renderable makePicture(final float x, final float y, final float width, final float height,
                           String imgName) {
        // set up the image to display
        //
        // NB: this will fail gracefully (no crash) for invalid file names
        final TextureRegion tr = mMedia.getImage(imgName);
        return new Renderable() {
            @Override
            public void render(SpriteBatch sb, float elapsed) {
                if (tr != null)
                    sb.draw(tr, x, y, 0, 0, width, height, 1, 1, 0);
            }
        };
    }

    abstract boolean render(SpriteBatch sb, float delta);
}
