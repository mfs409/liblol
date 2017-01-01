package edu.lehigh.cse.lol;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;

/**
 * Hud is a heads-up display.  It provides a means for putting things on the "Phone Screen" instead
 * of "in the world".
 */
class HudScene extends LolScene {
    /// The debug shape renderer, for putting boxes around Controls and Displays
    private final ShapeRenderer mShapeRender;

    /// Input Controls
    final ArrayList<Control> mControls;

    final ArrayList<BaseActor> mControls2;

    /// Output Displays
    private final ArrayList<Display> mDisplays;

    /// Controls that have a tap event
    final ArrayList<Control> mTapControls;
    final ArrayList<BaseActor> mTapControls2;


    /// Controls that have a pan event
    final ArrayList<Control> mPanControls;

    /// Controls that have a pinch zoom event
    final ArrayList<Control> mZoomControls;

    /// Toggle Controls
    final ArrayList<Control> mToggleControls;

    /**
     * Create a new heads-up display by providing the dimensions for its camera
     */
    HudScene(Media media, Config config) {
        super(media, config);

        mControls = new ArrayList<>();
        mDisplays = new ArrayList<>();
        mTapControls = new ArrayList<>();
        mShapeRender = new ShapeRenderer();
        mPanControls = new ArrayList<>();
        mZoomControls = new ArrayList<>();
        mToggleControls = new ArrayList<>();

        mControls2 = new ArrayList<>();
        mTapControls2 = new ArrayList<>();
    }

    void reportTouch(Vector3 touchVec, Config config) {
        mCamera.unproject(touchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0));
        Lol.message(config, "Screen Coordinates", touchVec.x + ", " + touchVec.y);
    }

    /**
     * @param sb
     * @param delta TODO: switch the Timer so that we can use /delta/
     */
    boolean render(SpriteBatch sb, float delta) {
        mCamera.update();

        // Advance the physics world by 1/45 of a second.
        //
        // NB: in Box2d, This is the recommended rate for phones, though it
        // seems like we should be using /delta/ instead of 1/45f
        mWorld.step(1 / 45f, 8, 3);

        sb.setProjectionMatrix(mCamera.combined);
        sb.begin();
        for (Control c : mControls)
            c.render(sb, delta);
        for (BaseActor b : mControls2)
            b.render(sb, delta);
        for (Display d : mDisplays)
            d.render(sb, delta);
        for (ArrayList<Renderable> a : mRenderables) {
            for (Renderable r : a) {
                r.render(sb, delta);
            }
        }
        sb.end();

        // DEBUG: render Controls' outlines
        // TODO: box2d shape renderer for controls2
        if (mConfig.mShowDebugBoxes) {
            mShapeRender.setProjectionMatrix(mCamera.combined);
            mShapeRender.begin(ShapeRenderer.ShapeType.Line);
            mShapeRender.setColor(Color.RED);
            for (Control pe : mControls)
                if (pe.mRange != null)
                    mShapeRender.rect(pe.mRange.x, pe.mRange.y, pe.mRange.width, pe.mRange.height);
            mShapeRender.end();
        }
        return true;
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
        mCamera.unproject(mTouchVec.set(x, y, 0));
        for (Control c : mTapControls) {
            if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                world.mCamera.unproject(mTouchVec.set(x, y, 0));
                c.mTapHandler.go(mTouchVec.x, mTouchVec.y);
                return true;
            }
        }
        mHitActor = null;
        mHitActor = null;
        mWorld.QueryAABB(mTouchCallback, mTouchVec.x - 0.1f, mTouchVec.y - 0.1f, mTouchVec.x + 0.1f,
                mTouchVec.y + 0.1f);
        if (mHitActor != null && mHitActor.mTapHandler != null)
            return mHitActor.mTapHandler.go(x, y);
        return false;
    }

    boolean handlePan(float x, float y, float deltaX, float deltaY, MainScene world) {
        mCamera.unproject(mTouchVec.set(x, y, 0));
        for (Control c : mPanControls) {
            if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                world.mCamera.unproject(mTouchVec.set(x, y, 0));
                c.mPanHandler.deltaX = deltaX;
                c.mPanHandler.deltaY = deltaY;
                c.mPanHandler.go(mTouchVec.x, mTouchVec.y);
                return true;
            }
        }
        return false;
    }

    boolean handlePanStop(float x, float y, MainScene world) {
        mCamera.unproject(mTouchVec.set(x, y, 0));
        for (Control c : mPanControls) {
            if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                world.mCamera.unproject(mTouchVec.set(x, y, 0));
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
        mCamera.unproject(mTouchVec.set(screenX, screenY, 0));
        for (Control c : mToggleControls) {
            if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                world.mCamera.unproject(mTouchVec.set(screenX, screenY, 0));
                c.mToggleHandler.isUp = false;
                c.mToggleHandler.go(mTouchVec.x, mTouchVec.y);
                return true;
            }
        }

        // pass to pinch-zoom?
        for (Control c : mZoomControls) {
            if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                world.mCamera.unproject(mTouchVec.set(screenX, screenY, 0));
                c.mDownHandler.go(mTouchVec.x, mTouchVec.y);
                return true;
            }
        }
        return false;
    }

    boolean handleUp(float screenX, float screenY, MainScene world) {
        mCamera.unproject(mTouchVec.set(screenX, screenY, 0));
        for (Control c : mToggleControls) {
            if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                world.mCamera.unproject(mTouchVec.set(screenX, screenY, 0));
                c.mToggleHandler.isUp = true;
                c.mToggleHandler.go(mTouchVec.x, mTouchVec.y);
                return true;
            }
        }
        return false;
    }
}
