package edu.lehigh.cse.lol;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;

/**
 * Hud is a heads-up display.  It provides a means for putting things on the "Phone Screen" instead
 * of "in the world".
 */
class Hud {

    /// The debug shape renderer, for putting boxes around Controls and Displays
    private final ShapeRenderer mShapeRender = new ShapeRenderer();

    /// Input Controls
    final ArrayList<Control> mControls = new ArrayList<>();

    /// Output Displays
    final ArrayList<Display> mDisplays = new ArrayList<>();

    /// Controls that have a tap event
    final ArrayList<Control> mTapControls = new ArrayList<>();

    /// Controls that have a pan event
    final ArrayList<Control> mPanControls = new ArrayList<>();

    /// Controls that have a pinch zoom event
    final ArrayList<Control> mZoomControls = new ArrayList<>();

    /// Toggle Controls
    final ArrayList<Control> mToggleControls = new ArrayList<>();

    /// This camera is for drawing controls that sit above the world
    OrthographicCamera mHudCam;

    /// We use this to avoid garbage collection when converting screen touches to camera coordinates
    private final Vector3 mTouchVec = new Vector3();

    /**
     * Create a new heads-up display by providing the dimensions for its camera
     */
    Hud(Config config) {
        int width = config.mWidth;
        int height = config.mHeight;

        mHudCam = new OrthographicCamera(width, height);
        mHudCam.position.set(width / 2, height / 2, 0);
    }

    void reportTouch(Vector3 touchVec, Config config) {
        mHudCam.unproject(touchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0));
        Lol.message(config, "Screen Coordinates", touchVec.x + ", " + touchVec.y);
    }

    void render(Config config, SpriteBatch sb) {
        mHudCam.update();
        sb.setProjectionMatrix(mHudCam.combined);
        sb.begin();
        for (Control c : mControls)
            if (c.mIsActive)
                c.render(sb);
        for (Display d : mDisplays)
            d.render(sb);
        sb.end();

        // DEBUG: render Controls' outlines
        if (config.mShowDebugBoxes) {
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
                c.mToggleHandler.handle(true, touchVec.x, touchVec.y);
            }
        }
    }

    void reset() {
        mControls.clear();
        mDisplays.clear();
    }

    boolean handleTap(float x, float y, PhysicsWorld world) {
        mHudCam.unproject(mTouchVec.set(x, y, 0));
        for (Control c : mTapControls) {
            if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                world.mGameCam.unproject(mTouchVec.set(x, y, 0));
                c.mTapHandler.handle(mTouchVec.x, mTouchVec.y);
                return true;
            }
        }
        return false;
    }

    boolean handlePan(float x, float y, float deltaX, float deltaY, PhysicsWorld world) {
        mHudCam.unproject(mTouchVec.set(x, y, 0));
        for (Control c : mPanControls) {
            if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                world.mGameCam.unproject(mTouchVec.set(x, y, 0));
                c.mPanHandler.handle(mTouchVec.x, mTouchVec.y, deltaX, deltaY);
                return true;
            }
        }
        return false;
    }

    boolean handlePanStop(float x, float y, PhysicsWorld world) {
        mHudCam.unproject(mTouchVec.set(x, y, 0));
        for (Control c : mPanControls) {
            if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                world.mGameCam.unproject(mTouchVec.set(x, y, 0));
                c.mPanStopHandler.handle(mTouchVec.x, mTouchVec.y);
                return true;
            }
        }
        return false;
    }

    boolean handleZoom(float initialDistance, float distance) {
        for (Control c : mZoomControls) {
            if (c.mIsTouchable && c.mIsActive) {
                c.mZoomHandler.handle(initialDistance, distance);
                return true;
            }
        }
        return false;

    }

    boolean handleDown(float screenX, float screenY, PhysicsWorld world) {
        // check if we down-pressed a control
        mHudCam.unproject(mTouchVec.set(screenX, screenY, 0));
        for (Control c : mToggleControls) {
            if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                world.mGameCam.unproject(mTouchVec.set(screenX, screenY, 0));
                c.mToggleHandler.handle(false, mTouchVec.x, mTouchVec.y);
                return true;
            }
        }

        // pass to pinch-zoom?
        for (Control c : mZoomControls) {
            if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                world.mGameCam.unproject(mTouchVec.set(screenX, screenY, 0));
                c.mDownHandler.handle(mTouchVec.x, mTouchVec.y);
                return true;
            }
        }
        return false;
    }

    boolean handleUp(float screenX, float screenY, PhysicsWorld world) {
        mHudCam.unproject(mTouchVec.set(screenX, screenY, 0));
        for (Control c : mToggleControls) {
            if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                world.mGameCam.unproject(mTouchVec.set(screenX, screenY, 0));
                c.mToggleHandler.handle(true, mTouchVec.x, mTouchVec.y);
                return true;
            }
        }
        return false;
    }
}
