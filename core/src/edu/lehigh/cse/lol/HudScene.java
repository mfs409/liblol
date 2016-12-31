package edu.lehigh.cse.lol;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

/**
 * Hud is a heads-up display.  It provides a means for putting things on the "Phone Screen" instead
 * of "in the world".
 */
class HudScene {
    /// The physics world in which Hud actors exist
    protected final World mWorld;

    /// Anything in the world that can be rendered, in 5 planes [-2, -1, 0, 1, 2]
    protected final ArrayList<Renderable> mRenderables;

    /// The debug shape renderer, for putting boxes around Controls and Displays
    private final ShapeRenderer mShapeRender;

    /// Input Controls
    final ArrayList<Control> mControls;

    /// Output Displays
    final ArrayList<Display> mDisplays;

    /// Controls that have a tap event
    final ArrayList<Control> mTapControls;

    /// Controls that have a pan event
    final ArrayList<Control> mPanControls;

    /// Controls that have a pinch zoom event
    final ArrayList<Control> mZoomControls;

    /// Toggle Controls
    final ArrayList<Control> mToggleControls;

    /// This camera is for drawing controls that sit above the world
    final OrthographicCamera mHudCam;

    /// We use this to avoid garbage collection when converting screen touches to camera coordinates
    private final Vector3 mTouchVec;

    /// A copy of the config object
    private final Config mConfig;

    /**
     * Create a new heads-up display by providing the dimensions for its camera
     */
    HudScene(Config config) {
        int width = config.mWidth;
        int height = config.mHeight;
        mConfig = config;

        // create a world with no default gravitational forces
        mWorld = new World(new Vector2(0, 0), true);

        // set up the renderables
        mRenderables = new ArrayList<>();
//        float w = mConfig.mWidth / mConfig.PIXEL_METER_RATIO;
//        float h = mConfig.mHeight / mConfig.PIXEL_METER_RATIO;

        mHudCam = new OrthographicCamera(width, height);
        mHudCam.position.set(width / 2, height / 2, 0);
        mTouchVec = new Vector3();
        mControls = new ArrayList<>();
        mDisplays = new ArrayList<>();
        mTapControls = new ArrayList<>();
        mShapeRender = new ShapeRenderer();
        mPanControls = new ArrayList<>();
        mZoomControls = new ArrayList<>();
        mToggleControls = new ArrayList<>();
    }

    void reportTouch(Vector3 touchVec, Config config) {
        mHudCam.unproject(touchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0));
        Lol.message(config, "Screen Coordinates", touchVec.x + ", " + touchVec.y);
    }

    /**
     *
     * @param sb
     * @param delta
     *
     * TODO: switch the Timer so that we can use /delta/
     */
    void render(SpriteBatch sb, float delta) {
        mHudCam.update();

        // Advance the physics world by 1/45 of a second.
        //
        // NB: in Box2d, This is the recommended rate for phones, though it
        // seems like we should be using /delta/ instead of 1/45f
        mWorld.step(1 / 45f, 8, 3);

        sb.setProjectionMatrix(mHudCam.combined);
        sb.begin();
        for (Control c : mControls)
            if (c.mIsActive)
                c.render(sb);
        for (Display d : mDisplays)
            d.render(sb, delta);

        for (Renderable r : mRenderables) {
                r.render(sb, delta);
        }


        sb.end();

        // DEBUG: render Controls' outlines
        if (mConfig.mShowDebugBoxes) {
            mShapeRender.setProjectionMatrix(mHudCam.combined);
            mShapeRender.begin(ShapeRenderer.ShapeType.Line);
            mShapeRender.setColor(Color.RED);
            for (Control pe : mControls)
                if (pe.mRange != null)
                    mShapeRender.rect(pe.mRange.x, pe.mRange.y, pe.mRange.width, pe.mRange.height);
            mShapeRender.end();
        }
    }

    void liftAllButtons(Vector3 touchVec) {
        for (Control c : mToggleControls) {
            if (c.mIsActive && c.mIsTouchable) {
                c.mToggleHandler.isUp = true;
                c.mToggleHandler.go(touchVec.x, touchVec.y);
            }
        }
    }

    void reset() {
        mControls.clear();
        mDisplays.clear();
    }

    boolean handleTap(float x, float y, MainScene world) {
        mHudCam.unproject(mTouchVec.set(x, y, 0));
        for (Control c : mTapControls) {
            if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                world.mGameCam.unproject(mTouchVec.set(x, y, 0));
                c.mTapHandler.go(mTouchVec.x, mTouchVec.y);
                return true;
            }
        }
        return false;
    }

    boolean handlePan(float x, float y, float deltaX, float deltaY, MainScene world) {
        mHudCam.unproject(mTouchVec.set(x, y, 0));
        for (Control c : mPanControls) {
            if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                world.mGameCam.unproject(mTouchVec.set(x, y, 0));
                c.mPanHandler.deltaX = deltaX;
                c.mPanHandler.deltaY = deltaY;
                c.mPanHandler.go(mTouchVec.x, mTouchVec.y);
                return true;
            }
        }
        return false;
    }

    boolean handlePanStop(float x, float y, MainScene world) {
        mHudCam.unproject(mTouchVec.set(x, y, 0));
        for (Control c : mPanControls) {
            if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                world.mGameCam.unproject(mTouchVec.set(x, y, 0));
                c.mPanStopHandler.go(mTouchVec.x, mTouchVec.y);
                return true;
            }
        }
        return false;
    }

    boolean handleZoom(float initialDistance, float distance) {
        for (Control c : mZoomControls) {
            if (c.mIsTouchable && c.mIsActive) {
                c.mZoomHandler.go(initialDistance, distance);
                return true;
            }
        }
        return false;

    }

    boolean handleDown(float screenX, float screenY, MainScene world) {
        // check if we down-pressed a control
        mHudCam.unproject(mTouchVec.set(screenX, screenY, 0));
        for (Control c : mToggleControls) {
            if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                world.mGameCam.unproject(mTouchVec.set(screenX, screenY, 0));
                c.mToggleHandler.isUp = false;
                c.mToggleHandler.go(mTouchVec.x, mTouchVec.y);
                return true;
            }
        }

        // pass to pinch-zoom?
        for (Control c : mZoomControls) {
            if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                world.mGameCam.unproject(mTouchVec.set(screenX, screenY, 0));
                c.mDownHandler.go(mTouchVec.x, mTouchVec.y);
                return true;
            }
        }
        return false;
    }

    boolean handleUp(float screenX, float screenY, MainScene world) {
        mHudCam.unproject(mTouchVec.set(screenX, screenY, 0));
        for (Control c : mToggleControls) {
            if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                world.mGameCam.unproject(mTouchVec.set(screenX, screenY, 0));
                c.mToggleHandler.isUp = true;
                c.mToggleHandler.go(mTouchVec.x, mTouchVec.y);
                return true;
            }
        }
        return false;
    }
}
