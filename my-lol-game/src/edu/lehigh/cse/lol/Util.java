package edu.lehigh.cse.lol;

// TODO: clean up comments

// TODO: this file gets font horizontal position correct, but other files may not... verify other files! 

// TODO: should all these things be their own files, instead of hiding them in the bottom of Util.java?

import java.util.Random;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Contact;

public class Util
{
    static Renderable makePicture(final float x, final float y, final float width, final float height, String imgName)
    {
        // set up the image to display
        //
        // NB: this will fail gracefully (no crash) for invalid file names
        TextureRegion[] trs = Media.getImage(imgName);
        final TextureRegion tr = (trs != null) ? trs[0] : null;
        return new Renderable()
        {
            @Override
            public void render(SpriteBatch sb, float elapsed)
            {
                if (tr != null)
                    sb.draw(tr, x, y, 0, 0, width, height, 1, 1, 0);
            }
        };
    }

    static Renderable makeText(final int x, final int y, final String message, final int red, final int green,
            final int blue, String fontName, int size)
    {
        final BitmapFont bf = Media.getFont(fontName, size);
        return new Renderable()
        {
            @Override
            public void render(SpriteBatch sb, float elapsed)
            {
                bf.setColor(((float) red) / 256, ((float) green) / 256, ((float) blue) / 256, 1);
                bf.drawMultiLine(sb, message, x, y+bf.getMultiLineBounds(message).height);
            }
        };
    }

    static Renderable makeCenteredText(final String message, final int red, final int green, final int blue,
            String fontName, int size)
    {
        final BitmapFont bf = Media.getFont(fontName, size);
        final float x = LOL._game._config.getScreenWidth() / 2 - bf.getMultiLineBounds(message).width / 2;
        final float y = LOL._game._config.getScreenHeight() / 2 + bf.getMultiLineBounds(message).height / 2;
        return new Renderable()
        {
            @Override
            public void render(SpriteBatch sb, float elapsed)
            {
                bf.setColor(((float) red) / 256, ((float) green) / 256, ((float) blue) / 256, 1);
                bf.drawMultiLine(sb, message, x, y);
            }
        };
    }

    /**
     * A random number generator... students always seem to need this
     */
    private static Random _generator = new Random();

    /**
     * Generate a random number x such that 0 <= x < max
     * 
     * @param max
     *            The largest number returned will be one less than max
     * @return a random integer
     */
    public static int getRandom(int max)
    {
        return _generator.nextInt(max);
    }

    /**
     * Draw a box on the scene
     * 
     * Note: the box is actually four narrow rectangles
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
     *            Density of the obstacle. When in doubt, use 1
     * @param elasticity
     *            Elasticity of the obstacle. When in doubt, use 0
     * @param friction
     *            Friction of the obstacle. When in doubt, use 1
     */
    static public void drawBoundingBox(int x0, int y0, int x1, int y1, String imgName, float density, float elasticity,
            float friction)
    {
        // draw four rectangles and we're good
        Obstacle b = Obstacle.makeAsBox(x0 - 1, y0 - 1, Math.abs(x0 - x1) + 2, 1, imgName);
        b.setPhysics(density, elasticity, friction);

        Obstacle t = Obstacle.makeAsBox(x0 - 1, y1, Math.abs(x0 - x1) + 2, 1, imgName);
        t.setPhysics(density, elasticity, friction);

        Obstacle l = Obstacle.makeAsBox(x0 - 1, y0 - 1, 1, Math.abs(y0 - y1) + 2, imgName);
        l.setPhysics(density, elasticity, friction);

        Obstacle r = Obstacle.makeAsBox(x1, y0 - 1, 1, Math.abs(y0 - y1) + 2, imgName);
        r.setPhysics(density, elasticity, friction);
    }

    /**
     * Draw a picture on the current level
     * 
     * Note: the order in which this is called relative to other entities will
     * determine whether they go under or over
     * this picture.
     * 
     * @param x
     *            X coordinate of top left corner
     * @param y
     *            Y coordinate of top left corner
     * @param width
     *            Width of the picture
     * @param height
     *            Height of this picture
     * @param imgName
     *            Name of the picture to display
     * @param zIndex
     *            The z index of the image. There are 5 planes: -2, -2, 0, 1, and 2. By default, everything goes to
     *            plane 0
     */
    public static void drawPicture(final int x, final int y, final int width, final int height, final String imgName,
            int zIndex)
    {
        Level._currLevel.addSprite(Util.makePicture(x, y, width, height, imgName), zIndex);
    }
}

interface CollisionCallback
{
    void go(final PhysicsSprite ps, Contact c);
}

class RouteDriver
{
    Route         _myRoute;

    float         _routeVelocity;

    boolean       _routeLoop;

    Vector2       _routeVec = new Vector2();

    boolean       _routeDone;

    int           _nextRouteGoal;

    PhysicsSprite _entity;

    void haltRoute()
    {
        _routeDone = true;
        // TODO: verify third parameter is ok... this gets called from pokevelocity stuff...
        _entity.setAbsoluteVelocity(0, 0, false);
    }

    RouteDriver(Route route, float velocity, boolean loop, PhysicsSprite entity)
    {
        _myRoute = route;

        _routeVelocity = velocity;
        _routeLoop = loop;

        _entity = entity;

        // this is how we initialize a route driver:
        // first, move to the starting point
        _entity._physBody.setTransform(_myRoute._xIndices[0] + _entity._width / 2, _myRoute._yIndices[0]
                + _entity._height / 2, 0);
        // second, indicate that we are working on goal #1, and set velocity
        // TODO: this needs to be in one place, instead of duplicated elsewhere
        // TODO: note that we are not getting the x,y coordinates quite right, since we're dealing with world
        // center.
        _nextRouteGoal = 1;
        _routeVec.x = _myRoute._xIndices[_nextRouteGoal] - _entity.getXPosition();
        _routeVec.y = _myRoute._yIndices[_nextRouteGoal] - _entity.getYPosition();
        _routeVec.nor();
        _routeVec.scl(_routeVelocity);
        _entity._physBody.setLinearVelocity(_routeVec);
        // and indicate that we aren't all done yet
        _routeDone = false;

    }

    /**
     * Internal method for figuring out where we need to go next when driving a route
     */
    void drive()
    {
        // quit if we're done and we don't loop
        if (_routeDone)
            return;
        // if we haven't passed the goal, keep going. we tell if we've passed the goal by comparing the magnitudes
        // of
        // the vectors from source to here and from goal to here
        float sx = _myRoute._xIndices[_nextRouteGoal - 1] - _entity.getXPosition();
        float sy = _myRoute._yIndices[_nextRouteGoal - 1] - _entity.getYPosition();
        float gx = _myRoute._xIndices[_nextRouteGoal] - _entity.getXPosition();
        float gy = _myRoute._yIndices[_nextRouteGoal] - _entity.getYPosition();
        boolean sameXSign = (gx >= 0 && sx >= 0) || (gx <= 0 && sx <= 0);
        boolean sameYSign = (gy >= 0 && sy >= 0) || (gy <= 0 && sy <= 0);
        if (((gx == gy) && (gx == 0)) || (sameXSign && sameYSign)) {
            _nextRouteGoal++;
            if (_nextRouteGoal == _myRoute._points) {
                // reset if it's a loop, else terminate Route
                if (_routeLoop) {
                    _entity._physBody.setTransform(_myRoute._xIndices[0] + _entity._width / 2,
                            _myRoute._yIndices[0] + _entity._height / 2, 0);
                    _nextRouteGoal = 1;
                    _routeVec.x = _myRoute._xIndices[_nextRouteGoal] - _entity.getXPosition();
                    _routeVec.y = _myRoute._yIndices[_nextRouteGoal] - _entity.getYPosition();
                    _routeVec.nor();
                    _routeVec.scl(_routeVelocity);
                    _entity._physBody.setLinearVelocity(_routeVec);

                    return;
                }
                else {
                    _routeDone = true;
                    _entity._physBody.setLinearVelocity(0, 0);
                    return;
                }
            }
            else {
                // advance to next point
                _routeVec.x = _myRoute._xIndices[_nextRouteGoal] - _entity.getXPosition();
                _routeVec.y = _myRoute._yIndices[_nextRouteGoal] - _entity.getYPosition();
                _routeVec.nor();
                _routeVec.scl(_routeVelocity);
                _entity._physBody.setLinearVelocity(_routeVec);
                return;
            }
        }
        // NB: if we get here, we didn't need to change the velocity
    }

}

/**
 * This enum encapsulates the different types of PhysicsSprite entities
 */
enum SpriteId
{
    UNKNOWN(0), HERO(1), ENEMY(2), GOODIE(3), PROJECTILE(4), OBSTACLE(5), SVG(6), DESTINATION(7);

    /**
     * To each ID, we attach an integer value, so that we can compare the
     * different IDs and establish a hierarchy
     * for collision management
     */
    public final int _id;

    /**
     * Construct by providing the integral id
     * 
     * @param id
     *            The unique integer for this SpriteId
     */
    SpriteId(int id)
    {
        _id = id;
    }
}

class ParallaxLayer
{
    float         _xSpeed;

    float         _ySpeed;

    TextureRegion _tr;

    float         _xOffset;

    float         _yOffset;

    boolean       _xRepeat;

    boolean       _yRepeat;

    ParallaxLayer(float xSpeed, float ySpeed, TextureRegion tr, float xOffset, float yOffset)
    {
        _xSpeed = xSpeed;
        _ySpeed = ySpeed;
        _tr = tr;
        _xOffset = xOffset;
        _yOffset = yOffset;
    }
}

/**
 * Custom camera that can do parallax... taken directly from GDX tests
 */
class ParallaxCamera extends OrthographicCamera
{
    private Matrix4 parallaxView     = new Matrix4();

    private Matrix4 parallaxCombined = new Matrix4();

    private Vector3 tmp              = new Vector3();

    private Vector3 tmp2             = new Vector3();

    /**
     * The constructor simply forwards to the OrthographicCamera constructor
     * 
     * @param viewportWidth
     *            Width of the camera
     * @param viewportHeight
     *            Height of the camera
     */
    ParallaxCamera(float viewportWidth, float viewportHeight)
    {
        super(viewportWidth, viewportHeight);
    }

    Matrix4 calculateParallaxMatrix(float parallaxX, float parallaxY)
    {
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
 * Wrapper for actions that we generate and then want handled during the render loop
 */
interface Action
{
    void go();
}

class TouchAction
{
    void onDown(float x, float y)
    {
    }

    void onMove(float x, float y)
    {
    }

    void onUp(float x, float y)
    {
    }
}

interface Renderable
{
    void render(SpriteBatch sb, float elapsed);
}
