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

    /**
     * Create a new heads-up display by providing the dimensions for its camera
     * @param width The width, in pixels, of the HUD
     * @param height The height, in pixels, of the HUD
     */
    Hud(int width, int height) {
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
                c.mGestureAction.toggle(true, touchVec);
            }
        }
    }

    void reset() {
        mControls.clear();
        mDisplays.clear();
    }

    boolean checkTap(Vector3 touchVec, float x, float y, OrthographicCamera gameCam) {
        mHudCam.unproject(touchVec.set(x, y, 0));
        for (Control c : mTapControls) {
            if (c.mIsTouchable && c.mIsActive && c.mRange.contains(touchVec.x, touchVec.y)) {
                gameCam.unproject(touchVec.set(x, y, 0));
                c.mGestureAction.onTap(touchVec);
                return true;
            }
        }
        return false;
    }
}
