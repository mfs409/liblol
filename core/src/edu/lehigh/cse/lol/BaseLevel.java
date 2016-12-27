package edu.lehigh.cse.lol;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;

/**
 * BaseLevel stores the functionality that is common across all of the different renderable,
 * updatable containers that store actors in a physical world.
 */
class BaseLevel {
    /// A reference to the game object, so we can access session facts and the state machine
    protected final Lol mGame;

    /// A reference to the game-wide configuration variables
    protected final Config mConfig;

    /// A reference to the object that stores all of the sounds and images we use in the game
    protected final Media mMedia;

    /// The maximum x and y values of the camera
    protected final Vector2 mCamBound = new Vector2();

    /// This camera is for drawing actors that exist in the physics world
    protected final OrthographicCamera mGameCam;

    /// The physics world in which all actors interact
    protected final World mWorld;

    /// Events that get processed on the next render, then discarded
    protected final ArrayList<LolAction> mOneTimeEvents;

    /// Events that get processed on every render
    protected final ArrayList<LolAction> mRepeatEvents;

    /// Anything in the world that can be rendered, in 5 planes [-2, -1, 0, 1, 2]
    protected final ArrayList<ArrayList<Renderable>> mRenderables;

    /// This callback is used to get a touched actor from the physics world
    protected final QueryCallback mTouchCallback;

    /// We use this to avoid garbage collection when converting screen touches to camera coordinates
    protected final Vector3 mTouchVec = new Vector3();

    /// When there is a touch of an actor in the physics world, this is how we find it
    protected Actor mHitActor = null;

    /**
     * Construct a basic level.  A level has a camera and a phyics world, actors who live in that
     * world, and the supporting infrastructure to make it all work.
     *
     * @param config The configuration object describing this game
     * @param media  References to all image and sound assets
     * @param game   The game that is being played
     */
    BaseLevel(Config config, Media media, Lol game) {
        // clear any timers
        Timer.instance().clear();

        // save game configuration information
        mGame = game;
        mConfig = config;
        mMedia = media;

        // set up the event lists
        mOneTimeEvents = new ArrayList<>();
        mRepeatEvents = new ArrayList<>();

        // set up the renderables
        mRenderables = new ArrayList<>(5);
        for (int i = 0; i < 5; ++i)
            mRenderables.add(new ArrayList<Renderable>());

        // set up the game camera, with (0, 0) in the bottom left
        float w = mConfig.mWidth / mConfig.PIXEL_METER_RATIO;
        float h = mConfig.mHeight / mConfig.PIXEL_METER_RATIO;
        mGameCam = new OrthographicCamera(w, h);
        mGameCam.position.set(w / 2, h / 2, 0);
        mGameCam.zoom = 1;

        // set default camera bounds
        setCameraBounds(w, h);

        // create a world with no default gravitational forces
        mWorld = new World(new Vector2(0, 0), true);

        // set up the callback for finding out who in the physics world was
        // touched
        mTouchCallback = new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                // if the hit point is inside the fixture of the body we report
                // it
                if (fixture.testPoint(mTouchVec.x, mTouchVec.y)) {
                    Actor hs = (Actor) fixture.getBody().getUserData();
                    if (hs.mVisible) {
                        mHitActor = hs;
                        return false;
                    }
                }
                return true;
            }
        };

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
        mCamBound.set(width, height);

        // warn on strange dimensions
        if (width < mConfig.mWidth / mConfig.PIXEL_METER_RATIO)
            Lol.message(mConfig, "Warning", "Your game width is less than 1/10 of the screen width");
        if (height < mConfig.mHeight / mConfig.PIXEL_METER_RATIO)
            Lol.message(mConfig, "Warning", "Your game height is less than 1/10 of the screen height");
    }

    /**
     * This code is called every 1/45th of a second to update the game state and
     * re-draw the screen
     *
     * @param delta The time since the last render
     */
    void render(float delta, Box2DDebugRenderer debugRender, SpriteBatch sb) {
        // Advance the physics world by 1/45 of a second.
        //
        // NB: in Box2d, This is the recommended rate for phones, though it
        // seems like we should be using /delta/ instead of 1/45f
        mWorld.step(1 / 45f, 8, 3);

        // now handle any events that occurred on account of the world movement
        // or screen touches
        for (LolAction pe : mOneTimeEvents)
            pe.go();
        mOneTimeEvents.clear();

        // handle repeat events
        for (LolAction pe : mRepeatEvents) {
            if (pe.mIsActive)
                pe.go();
        }
    }
}