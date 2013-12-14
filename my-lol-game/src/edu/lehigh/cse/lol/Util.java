package edu.lehigh.cse.lol;

// TODO: refactor the svg parsing out of this file, and maybe make it not static.

// TODO: update the drawpicture functions to take a z parameter

// TODO: should we allow drawing pngs over the SVG lines? If so, we'll need to have a height parameter

// TODO: clean up comments

import java.io.IOException;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

import edu.lehigh.cse.lol.Level.Renderable;
import edu.lehigh.cse.lol.PhysicsSprite.SpriteId;

public class Util
{
    // TODO: we should be able to use this in *lots* of places that are currently rolling their own...
    static Renderable makePicture(final int x, final int y, final int width, final int height, String imgName)
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

    // TODO: make the font name a parameter
    //
    // TODO: we should be able to use this in *lots* of places that are currently rolling their own...
    static Renderable makeText(final int x, final int y, final String message, final int red, final int green,
            final int blue, int size)
    {
        final BitmapFont bf = Media.getFont("arial.ttf", size);
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

    // TODO: make the font name a parameter
    //
    // TODO: we should be able to use this in *lots* of places that are currently rolling their own...
    static Renderable makeCenteredText(final String message, final int red, final int green,
            final int blue, int size)
    {
        final BitmapFont bf = Media.getFont("arial.ttf", size);
        final float x = LOL._game._config.getScreenWidth()/2 - bf.getMultiLineBounds(message).width / 2;
        final float y = LOL._game._config.getScreenHeight()/2 - bf.getMultiLineBounds(message).height / 2;
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
     * Draw a picture on the _current level
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
     */
    public static void drawPicture(final int x, final int y, final int width, final int height, final String imgName)
    {
        Renderable r = new Renderable()
        {
            final TextureRegion tr = Media.getImage(imgName)[0];

            @Override
            public void render(SpriteBatch sb, float delta)
            {
                sb.draw(tr, x, y, 0, 0, width, height, 1, 1, 0);
            }

        };
        Level._currLevel._sprites.add(r);
    }

    /**
     * Draw a picture on the _current level, but unlike the regular drawPicture,
     * this draws a picture behind the rest of
     * the scene
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
     */
    public static void drawPictureBehindScene(final float x, final float y, final float width, final float height,
            final String imgName)
    {
        Renderable r = new Renderable()
        {
            final TextureRegion tr = Media.getImage(imgName)[0];

            @Override
            public void render(SpriteBatch sb, float delta)
            {
                sb.draw(tr, x, y, 0, 0, width, height, 1, 1, 0);
            }
        };
        Level._currLevel._pix_minus_two.add(r);
    }

    /**
     * The loadSVG method of the Obstacle class uses this field to specify the
     * _physics behavior of this object
     */
    private static FixtureDef _fixture;

    /**
     * The loadSVG method of the Obstacle class uses this field to add an offset
     * to the X position of the lines being
     * drawn.
     */
    private static float      _userTransformX = 0f;

    /**
     * The loadSVG method of the Obstacle class uses this field to add an offset
     * to the Y position of the lines being
     * drawn.
     */
    private static float      _userTransformY = 0f;

    /**
     * The loadSVG method of the Obstacle class uses this field to stretch the X
     * dimension of the lines being drawn
     */
    private static float      _userStretchX   = 1f;

    /**
     * The loadSVG method of the Obstacle class uses this field to stretch the Y
     * dimension of the lines being drawn
     */
    private static float      _userStretchY   = 1f;

    /**
     * internal cache of the X position of the SVG "transform" field
     */
    private static float      _svgTransformX  = 0f;

    /**
     * internal cache of the Y position of the SVG "transform" field
     */
    private static float      _svgTransformY  = 0f;

    /**
     * internal field to track the X coordinate of the last point we drew
     */
    private static float      _lastX          = 0;

    /**
     * internal field to track the Y coordinate of the last point we drew
     */
    private static float      _lastY          = 0;

    /**
     * internal field to track the X coordinate of the first point we drew
     */
    private static float      _firstX         = 0;

    /**
     * internal field to track the Y coordinate of the first point we drew
     */
    private static float      _firstY         = 0;

    /**
     * internal field to track the X coordinate of the _current point
     */
    private static float      _nextX          = 0;

    /**
     * internal field to track the Y coordinate of the _current point
     */
    private static float      _nextY          = 0;

    /**
     * track the _state of the parser
     * 
     * We've got a lightweight FSM here for handling the first point. Valid
     * values are 0 for "read next x", 1 for
     * "read next y", -2 for "read first x", and -1 for "read first y"
     */
    private static int        _state          = 0;

    /**
     * internal field for faking the creation of curves. This gives the number
     * of values to ignore when parsing.
     */
    private static int        _swallow        = 0;

    /**
     * internal field for managing whether we're parsing a curve or a line.
     * Valid values are 0 for "uninitialized", 1 for "starting to read", 2 for
     * "parsing curve", and 3 for "parsing line"
     */
    private static int        _mode           = 0;

    private static void processTransform(String xform)
    {
        if (xform.startsWith("translate(")) {
            String x2 = xform.replace("translate(", "");
            x2 = x2.replace(")", ",");
            String delims = "[,]+";
            String[] points = x2.split(delims);
            try {
                _svgTransformX = Float.valueOf(points[0]).floatValue();
                _svgTransformY = Float.valueOf(points[1]).floatValue();
            }
            catch (NumberFormatException nfs) {
            }
        }
    }

    private static void processD(String d)
    {
        String delims = "[ ,]+";
        String[] points = d.split(delims);
        boolean absolute = false;
        for (String s0 : points) {
            String s = s0.trim();
            // start of the path
            if (s.equals("m")) {
                _state = -2;
                absolute = false;
            }
            else if (s.equals("M")) {
                _state = -2;
                // switch to absolute _mode
                absolute = true;
            }
            // switch to _mode for drawing curves
            else if (s.equals("c")) {
                _mode = 2;
                _swallow = 4;
            }
            // end of the path
            else if (s.equals("z")) {
                // draw a connecting line to complete the shape
                addLine((_userStretchX * (_lastX + _svgTransformX) + _userTransformX), (_userStretchY
                        * (_lastY + _svgTransformY) + _userTransformY),
                        (_userStretchX * (_firstX + _svgTransformX) + _userTransformX), (_userStretchY
                                * (_firstY + _svgTransformY) + _userTransformY));
            }
            // switch to _mode for drawing lines
            else if (s.equals("l")) {
                _mode = 3;
                absolute = false;
                _swallow = 0;
            }
            else if (s.equals("L")) {
                _mode = 3;
                absolute = true;
                _swallow = 0;
            }
            // handle content
            else {
                // ignore first parameters of a curve entry
                if (_swallow > 0) {
                    _swallow--;
                }
                // get the next point
                else {
                    try {
                        float val = Float.valueOf(s).floatValue();
                        // read initial x
                        if (_state == -2) {
                            _state = -1;
                            _lastX = val;
                            _firstX = val;
                        }
                        // read initial y: we can't draw a line yet because
                        // we only have one point
                        else if (_state == -1) {
                            _state = 0;
                            _lastY = val;
                            _firstY = val;
                        }
                        // read next x
                        else if (_state == 0) {
                            if (absolute)
                                _nextX = val;
                            else
                                _nextX = _lastX + val;
                            _state = 1;
                        }
                        // read next y, and then draw a line
                        else if (_state == 1) {
                            _state = 0;
                            if (absolute)
                                _nextY = val;
                            else
                                _nextY = _lastY - val;
                            // draw the line
                            addLine((_userStretchX * (_lastX + _svgTransformX) + _userTransformX), (_userStretchY
                                    * (_lastY + _svgTransformY) + _userTransformY), (_userStretchX
                                    * (_nextX + _svgTransformX) + _userTransformX), (_userStretchY
                                    * (_nextY + _svgTransformY) + _userTransformY));
                            _lastX = _nextX;
                            _lastY = _nextY;
                            // if we are in curve _mode, then reinitialize
                            // the swallower
                            if (_mode == 2)
                                _swallow = 4;
                        }
                    }
                    // ignore errors
                    catch (NumberFormatException nfs) {
                    }
                }
            }
        }
    }

    /**
     * Internal method used by the SVG parser to actually draw a line.
     * 
     * Note that this is a hack. We create a simple line, and give it a color
     * and a _physics body. Then we create an
     * invisible PhysicsSprite that wraps the body so that collision detection
     * can safely cast the line's body's
     * getUserData() to a PhysicsSprite.
     * 
     * @param x1
     *            X coordinate of first endpoint
     * @param y1
     *            Y coordinate of first endpoint
     * @param x2
     *            X coordinate of second endpoint
     * @param y2
     *            Y coordinate of second endpoint
     */
    private static void addLine(float x1, float y1, float x2, float y2)
    {
        // draw a line
        BodyDef bd = new BodyDef();
        bd.type = BodyType.StaticBody;
        // compute center and length
        float centerX = (x1 + x2) / 2;
        float centerY = (y1 + y2) / 2;
        float len = (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        bd.position.set(centerX, centerY);
        bd.angle = 0;
        // make the body, connect an edge fixture
        Body b = Level._currLevel._world.createBody(bd);
        EdgeShape line = new EdgeShape();// new Line(x1, y1, x2, y2, 2,
                                         // ALE._self.getVertexBufferObjectManager());
        // set the line position as an offset from center
        line.set(-len / 2, 0, len / 2, 0);
        _fixture.shape = line;
        b.createFixture(_fixture);
        _fixture.shape.dispose(); // i.e., line.dispose()
        b.setTransform(centerX, centerY, MathUtils.atan2(y2 - y1, x2 - x1));
        Gdx.app.log("line", "Drawing line from " + x1 + "," + y1 + " to " + x2 + "," + y2 + " (length " + len + ")");

        // wrap it all in a fake PhysicsSprite
        //
        // TODO: should we take in an image? If so, we'll need to adjust the last parameter
        PhysicsSprite phony = new PhysicsSprite(null, SpriteId.SVG, len, .1f)
        {
            @Override
            void onCollide(PhysicsSprite other, Contact contact)
            {
            }
        };
        phony._physBody = b;
        b.setUserData(phony);
        // put the line on the screen
        Level._currLevel._sprites.add(phony);
    }

    /**
     * Load an SVG line drawing generated from Inkscape.
     * 
     * Note that not all Inkscape drawings will work as expected. See
     * SVGParser.java for more information.
     * 
     * @param svgFileName
     *            Name of the svg file to load. It should be in the assets
     *            folder
     * @param red
     *            red component of the color to use for all lines
     * @param green
     *            green component of the color to use for all lines
     * @param blue
     *            blue component of the color to use for all lines
     * @param density
     *            density of all lines
     * @param elasticity
     *            elasticity of all lines
     * @param friction
     *            friction of all lines
     * @param stretchX
     *            Stretch the drawing in the X dimension by this percentage
     * @param stretchY
     *            Stretch the drawing in the Y dimension by this percentage
     * @param xposeX
     *            Shift the drawing in the X dimension. Note that shifting
     *            occurs after stretching
     * @param xposeY
     *            Shift the drawing in the Y dimension. Note that shifting
     *            occurs after stretching
     */
    static public void importSVGLineDrawing(String svgFileName, float density, float elasticity, float friction,
            float stretchX, float stretchY, float xposeX, float xposeY)
    {
        // save params:

        // create the _physics _fixture in a manner that is visible to the
        // addLine routine of the parser
        _fixture = new FixtureDef();
        _fixture.density = density;
        _fixture.restitution = elasticity;
        _fixture.friction = friction;

        // specify transpose and stretch information
        _userStretchX = stretchX / Physics.PIXEL_METER_RATIO;
        _userStretchY = stretchY / Physics.PIXEL_METER_RATIO;
        _userTransformX = xposeX;
        _userTransformY = xposeY;

        XmlReader r = new XmlReader();
        try {
            Element root = r.parse(Gdx.files.internal(svgFileName));
            // get the <g> tags
            Array<Element> gs = root.getChildrenByName("g");
            for (Element g : gs) {
                // Get the g's transform attribute
                String xform = g.getAttribute("transform");
                if (xform != null) {
                    processTransform(xform);
                }
                // get each g's paths
                Array<Element> paths = g.getChildrenByName("path");
                for (Element p : paths) {
                    processD(p.getAttribute("d"));
                }
            }
        }
        catch (IOException e1) {
            // we'll just dump and continue if we can't read the file into a DOM
            // object
            e1.printStackTrace();
        }
    }
}
