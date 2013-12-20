
package edu.lehigh.cse.lol;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
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

import edu.lehigh.cse.lol.PhysicsSprite.SpriteId;

/**
 * SVG allows the game designer to load SVG line drawings into a game. SVG line
 * drawings can be made in Inkscape. In LOL, we do not use line drawings to the
 * full extend. We only use them to define a set of invisible lines for a
 * immobile obstacle. You should draw a picture on top of your line drawing, so
 * that the player knows that there is a physics entity on the screen.
 */
public class SVG {
    /*
     * INTERNAL: STATE
     */

    /**
     * This description will be used for every line we create
     */
    private FixtureDef _fixture;

    /**
     * The offset, in the X dimension, by which we shift the line drawing
     */
    private float _userTransformX = 0f;

    /**
     * The offset, in the Y dimension, by which we shift the line drawing
     */
    private float _userTransformY = 0f;

    /**
     * The amount by which we stretch the drawing in the X dimension
     */
    private float _userStretchX = 1f;

    /**
     * The amount by which we stretch the drawing in the Y dimension
     */
    private float _userStretchY = 1f;

    /**
     * SVG files can have an internal "translate" field... while parsing, we
     * save the field here
     */
    private float _svgTranslateX = 0f;

    /**
     * SVG files can have an internal "translate" field... while parsing, we
     * save the field here
     */
    private float _svgTranslateY = 0f;

    /**
     * X coordinate of the last point we drew
     */
    private float _lastX = 0;

    /**
     * Y coordinate of the last point we drew
     */
    private float _lastY = 0;

    /**
     * X coordinate of the first point we drew
     */
    private float _firstX = 0;

    /**
     * Y coordinate of the first point we drew
     */
    private float _firstY = 0;

    /**
     * X coordinate of the current point being drawn
     */
    private float _nextX = 0;

    /**
     * Y coordinate of the current point being drawn
     */
    private float _nextY = 0;

    /**
     * The parser is essentially a finite state machine. The states are 0 for
     * "read next x", 1 for "read next y", -2 for "read first x", and -1 for
     * "read first y"
     */
    private int _state = 0;

    /**
     * Our parser can't handle curves. When we encounter a curve, we use this
     * field to swallow a fixed number of values, so that the curve definition
     * becomes a line definition
     */
    private int _swallow = 0;

    /**
     * Track if we're parsing a curve or a line. Valid values are 0 for
     * "uninitialized", 1 for "starting to read", 2 for "parsing curve", and 3
     * for "parsing line"
     */
    private int _mode = 0;

    /**
     * Configure a parser that we can use to load an SVG file and draw each of
     * its lines as a Box2d line
     * 
     * @param density density of each line
     * @param elasticity elasticity of each line
     * @param friction friction of each line
     * @param stretchX Stretch the drawing in the X dimension by this percentage
     * @param stretchY Stretch the drawing in the Y dimension by this percentage
     * @param xposeX Shift the drawing in the X dimension. Note that shifting
     *            occurs after stretching
     * @param xposeY Shift the drawing in the Y dimension. Note that shifting
     *            occurs after stretching
     */
    private SVG(float density, float elasticity, float friction, float stretchX, float stretchY,
            float xposeX, float xposeY) {
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
    }

    /*
     * INTERNAL METHODS RELATED TO PARSING
     */

    /**
     * When we encounter a "transform" attribute, we use this code to parse it,
     * in case there is a "translate" directive that we should handle
     * 
     * @param attribute The attribute being processed... we hope it's a valid
     *            translate directive
     */
    private void processTransform(String attribute) {
        // if we get a valid "translate" attribute, split it into two floats and
        // save them
        if (attribute.startsWith("translate(")) {
            String x2 = attribute.replace("translate(", "");
            x2 = x2.replace(")", ",");
            String delims = "[,]+";
            String[] points = x2.split(delims);
            try {
                _svgTranslateX = Float.valueOf(points[0]).floatValue();
                _svgTranslateY = Float.valueOf(points[1]).floatValue();
            } catch (NumberFormatException nfs) {
            }
        }
    }

    /**
     * The root of an SVG drawing will have a <g> element, which will have some
     * number of <path> elements. Each <path> will have a "d=" attribute, which
     * stores the points and information about how to connect them. The "d" is a
     * single string, which we parse in this file
     * 
     * @param d
     */
    private void processD(String d) {
        // split the string into characters and floating point values
        String delims = "[ ,]+";
        String[] points = d.split(delims);
        // SVG can give point coords in absolute or relative terms
        boolean absolute = false;
        for (String s0 : points) {
            String s = s0.trim();
            // start of the path, relative mode
            if (s.equals("m")) {
                _state = -2;
                absolute = false;
            }
            // start of the path, absolute mode
            else if (s.equals("M")) {
                _state = -2;
                absolute = true;
            }
            // beginning of a (set of) curve definitions, relative mode
            //
            // NB: we coerce curves into lines by ignoring the first four
            // parameters... this leaves us with just the
            // endpoints
            else if (s.equals("c")) {
                _mode = 2;
                _swallow = 4;
            }
            // end of path, relative mode
            else if (s.equals("z")) {
                // draw a connecting line to complete the shape
                addLine((_userStretchX * (_lastX + _svgTranslateX) + _userTransformX),
                        (_userStretchY * (_lastY + _svgTranslateY) + _userTransformY),
                        (_userStretchX * (_firstX + _svgTranslateX) + _userTransformX),
                        (_userStretchY * (_firstY + _svgTranslateY) + _userTransformY));
            }
            // beginning of a (set of) line definitions, relative mode
            else if (s.equals("l")) {
                _mode = 3;
                absolute = false;
                _swallow = 0;
            }
            // beginning of a (set of) line definitions, absolute mode
            else if (s.equals("L")) {
                _mode = 3;
                absolute = true;
                _swallow = 0;
            }
            // floating point data that defines an endpoint of a line or curve
            else {
                // if it's a curve, we might need to swallow this value
                if (_swallow > 0) {
                    _swallow--;
                }
                // get the next point
                else {
                    try {
                        // convert next point to float
                        float val = Float.valueOf(s).floatValue();
                        // if it's the initial x, save it
                        if (_state == -2) {
                            _state = -1;
                            _lastX = val;
                            _firstX = val;
                        }
                        // if it's the initial y, save it... can't draw a line
                        // yet, because we have 1 endpoint
                        else if (_state == -1) {
                            _state = 0;
                            _lastY = val;
                            _firstY = val;
                        }
                        // if it's an X value, save it
                        else if (_state == 0) {
                            if (absolute)
                                _nextX = val;
                            else
                                _nextX = _lastX + val;
                            _state = 1;
                        }
                        // if it's a Y value, save it and draw a line
                        else if (_state == 1) {
                            _state = 0;
                            if (absolute)
                                _nextY = val;
                            else
                                _nextY = _lastY - val;
                            // draw the line
                            addLine((_userStretchX * (_lastX + _svgTranslateX) + _userTransformX),
                                    (_userStretchY * (_lastY + _svgTranslateY) + _userTransformY),
                                    (_userStretchX * (_nextX + _svgTranslateX) + _userTransformX),
                                    (_userStretchY * (_nextY + _svgTranslateY) + _userTransformY));
                            _lastX = _nextX;
                            _lastY = _nextY;
                            // if we are in curve _mode, reinitialize the
                            // swallower
                            if (_mode == 2)
                                _swallow = 4;
                        }
                    }
                    // ignore errors...
                    catch (NumberFormatException nfs) {
                        Gdx.app.log("SVG Error", "error parsing SVG file");
                        nfs.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Internal method used by the SVG parser to draw a line. This is a bit of a
     * hack, in that we create a simple Box2d Edge, and then we make an
     * invisible PhysicsSprite that we connect to the Edge, so that LOL
     * collision detection works correctly. There are no images being displayed,
     * and this is an "SVG" entity, not an "Obstacle"
     * 
     * @param x1 X coordinate of first endpoint
     * @param y1 Y coordinate of first endpoint
     * @param x2 X coordinate of second endpoint
     * @param y2 Y coordinate of second endpoint
     */
    private void addLine(float x1, float y1, float x2, float y2) {
        // Create a static body for an Edge shape
        BodyDef bd = new BodyDef();
        bd.type = BodyType.StaticBody;
        // compute center and length
        float centerX = (x1 + x2) / 2;
        float centerY = (y1 + y2) / 2;
        float len = (float)Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        bd.position.set(centerX, centerY);
        bd.angle = 0;
        Body b = Level._currLevel._world.createBody(bd);
        EdgeShape line = new EdgeShape();

        // set the line position as an offset from center, rotate it, and
        // connect a fixture
        line.set(-len / 2, 0, len / 2, 0);
        _fixture.shape = line;
        b.createFixture(_fixture);
        _fixture.shape.dispose(); // i.e., line.dispose()
        b.setTransform(centerX, centerY, MathUtils.atan2(y2 - y1, x2 - x1));

        // connect it to an invisible PhysicsSprite, so that collision callbacks
        // will work (i.e., for _inAir)
        PhysicsSprite invis = new PhysicsSprite(null, SpriteId.SVG, len, .1f) {
            @Override
            void onCollide(PhysicsSprite other, Contact contact) {
            }
        };
        invis._physBody = b;
        b.setUserData(invis);
        // NB: we probably don't need to put the invisible sprite on the screen,
        // since we don't overload render()... this is invisible.
        Level._currLevel.addSprite(invis, 0);
    }

    /**
     * The main parse routine. We slurp the file into an XML DOM object, and
     * then iterate over it, finding the <path>s within the <g>, and processing
     * their "d" attributes
     * 
     * @param svgName The name of the file to parse
     */
    private void parse(String svgName) {
        XmlReader r = new XmlReader();
        try {
            Element root = r.parse(Gdx.files.internal(svgName));
            // get the <g> tags
            Array<Element> gs = root.getChildrenByName("g");
            for (Element g : gs) {
                // Get the g's transform attribute
                String xform = g.getAttribute("transform");
                if (xform != null)
                    processTransform(xform);
                // get each g's paths
                Array<Element> paths = g.getChildrenByName("path");
                for (Element p : paths)
                    processD(p.getAttribute("d"));
            }
        } catch (IOException e) {
            Gdx.app.log("SVG Error", "error parsing SVG file");
            e.printStackTrace();
        }
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Load an SVG line drawing generated from Inkscape. The SVG will be loaded
     * as an immobile obstacle. Note that not all Inkscape drawings will work as
     * expected... if you need more power than this provides, you'll have to
     * modify SVG.java
     * 
     * @param svgName Name of the svg file to load. It should be in the assets
     *            folder
     * @param density density of each line
     * @param elasticity elasticity of each line
     * @param friction friction of each line
     * @param stretchX Stretch the drawing in the X dimension by this percentage
     * @param stretchY Stretch the drawing in the Y dimension by this percentage
     * @param xposeX Shift the drawing in the X dimension. Note that shifting
     *            occurs after stretching
     * @param xposeY Shift the drawing in the Y dimension. Note that shifting
     *            occurs after stretching
     */
    public static void importLineDrawing(String svgName, float density, float elasticity,
            float friction, float stretchX, float stretchY, float xposeX, float xposeY) {
        // Create an SVG object to hold all the parameters, then use it to parse
        // the file
        SVG s = new SVG(density, elasticity, friction, stretchX, stretchY, xposeX, xposeY);
        s.parse(svgName);
    }
}
