package edu.lehigh.cse.lol;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;

/**
 * Hud is a heads-up display.  It provides a means for putting things on the "Phone Screen" instead
 * of "in the world".
 */
class HudScene extends LolScene {
    /// Toggle Controls
    final ArrayList<SceneActor> mToggleControls;

    /**
     * Create a new heads-up display by providing the dimensions for its camera
     */
    HudScene(Media media, Config config) {
        super(media, config);
        mToggleControls = new ArrayList<>();
    }

    void reportTouch(float x, float y) {
        mCamera.unproject(mTouchVec.set(x, y, 0));
        Lol.message(mConfig, "Screen Coordinates", mTouchVec.x + ", " + mTouchVec.y);
    }

    /**
     * @param delta
     *
     * TODO: switch the Timer so that we can use /delta/
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
        for (ArrayList<Renderable> a : mRenderables) {
            for (Renderable r : a) {
                r.render(sb, delta);
            }
        }
        sb.end();

        // TODO: box2d shape renderer for controls2
        if (mConfig.mShowDebugBoxes) {
        }
        return true;
    }

    void liftAllButtons(float touchX, float touchY) {
        for (SceneActor c : mToggleControls) {
            if (c.mIsTouchable) {
                c.mToggleHandler.isUp = true;
                c.mToggleHandler.go(touchX, touchY);
            }
        }
    }

    void reset() {
        mToggleControls.clear();
        super.reset();
    }

    boolean handleTap(float x, float y, MainScene world) {
        mCamera.unproject(mTouchVec.set(x, y, 0));
        mHitActor = null;
        mWorld.QueryAABB(mTouchCallback, mTouchVec.x - 0.1f, mTouchVec.y - 0.1f, mTouchVec.x + 0.1f,
                mTouchVec.y + 0.1f);
        world.mCamera.unproject(mTouchVec.set(x, y, 0));
        return mHitActor != null && mHitActor.mTapHandler != null && mHitActor.mTapHandler.go(mTouchVec.x, mTouchVec.y);
    }

    boolean handlePan(float x, float y, float deltaX, float deltaY, MainScene world) {
        mCamera.unproject(mTouchVec.set(x, y, 0));
        mHitActor = null;
        mWorld.QueryAABB(mTouchCallback, mTouchVec.x - 0.1f, mTouchVec.y - 0.1f, mTouchVec.x + 0.1f,
                mTouchVec.y + 0.1f);
        world.mCamera.unproject(mTouchVec.set(x, y, 0));
        if (mHitActor != null && ((SceneActor)mHitActor).mPanHandler != null) {
            ((SceneActor)mHitActor).mPanHandler.deltaX = deltaX;
            ((SceneActor)mHitActor).mPanHandler.deltaY = deltaY;
            return ((SceneActor) mHitActor).mPanHandler.go(mTouchVec.x, mTouchVec.y);
        }
        return false;
    }

    boolean handlePanStop(float x, float y, MainScene world) {
        mCamera.unproject(mTouchVec.set(x, y, 0));
        mHitActor = null;
        mWorld.QueryAABB(mTouchCallback, mTouchVec.x - 0.1f, mTouchVec.y - 0.1f, mTouchVec.x + 0.1f,
                mTouchVec.y + 0.1f);
        world.mCamera.unproject(mTouchVec.set(x, y, 0));
        return mHitActor != null && ((SceneActor)mHitActor).mPanStopHandler != null && ((SceneActor)mHitActor).mPanStopHandler.go(mTouchVec.x, mTouchVec.y);
    }

    boolean handleZoom(float initialDistance, float distance) {
        mHitActor = null;
        mWorld.QueryAABB(mTouchCallback, mTouchVec.x - 0.1f, mTouchVec.y - 0.1f, mTouchVec.x + 0.1f,
                mTouchVec.y + 0.1f);
        return mHitActor != null && ((SceneActor)mHitActor).mZoomHandler != null && ((SceneActor)mHitActor).mZoomHandler.go(initialDistance, distance);
    }

    boolean handleDown(float screenX, float screenY, MainScene world) {
        // check if we down-pressed a control
        mCamera.unproject(mTouchVec.set(screenX, screenY, 0));

        mHitActor = null;
        mWorld.QueryAABB(mTouchCallback, mTouchVec.x - 0.1f, mTouchVec.y - 0.1f, mTouchVec.x + 0.1f,
                mTouchVec.y + 0.1f);
        world.mCamera.unproject(mTouchVec.set(screenX, screenY, 0));
        if (mHitActor != null && mHitActor.mToggleHandler != null) {
            mHitActor.mToggleHandler.isUp = false;
            if (mHitActor.mToggleHandler.go(mTouchVec.x, mTouchVec.y))
                return true;
        }
        return mHitActor != null && ((SceneActor) mHitActor).mDownHandler != null &&
                ((SceneActor) mHitActor).mDownHandler.go(mTouchVec.x, mTouchVec.y);
    }

    boolean handleUp(float screenX, float screenY, MainScene world) {
        mCamera.unproject(mTouchVec.set(screenX, screenY, 0));
        mHitActor = null;
        mWorld.QueryAABB(mTouchCallback, mTouchVec.x - 0.1f, mTouchVec.y - 0.1f, mTouchVec.x + 0.1f,
                mTouchVec.y + 0.1f);
        world.mCamera.unproject(mTouchVec.set(screenX, screenY, 0));
        if (mHitActor != null && mHitActor.mToggleHandler != null) {
            mHitActor.mToggleHandler.isUp = true;
            return mHitActor.mToggleHandler.go(mTouchVec.x, mTouchVec.y);
        }
        return false;
    }
}
