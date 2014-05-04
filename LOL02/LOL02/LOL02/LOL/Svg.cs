using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;
using FarseerPhysics.Dynamics.Contacts;

namespace LOL
{
    public class Svg
    {
        /**
         * This description will be used for every line we create
         */
        private FixtureDef mFixture;

        /**
         * The offset by which we shift the line drawing
         */
        private Vector2 mUserTransform = new Vector2(0, 0);

        /**
         * The amount by which we stretch the drawing
         */
        private Vector2 mUserStretch = new Vector2(1, 1);

        /**
         * SVG files can have an internal "translate" field... while parsing, we
         * save the field here
         */
        private Vector2 mSvgTranslate = new Vector2(0, 0);

        /**
         * Coordinate of the last point we drew
         */
        private Vector2 mLast = new Vector2(0, 0);

        /**
         * Coordinate of the first point we drew
         */
        private Vector2 mFirst = new Vector2(0, 0);

        /**
         * X coordinate of the current point being drawn
         */
        private Vector2 mCurr = new Vector2(0, 0);

        /**
         * The parser is essentially a finite state machine. The states are 0 for
         * "read next x", 1 for "read next y", -2 for "read first x", and -1 for
         * "read first y"
         */
        private int mState = 0;

        /**
         * Our parser can't handle curves. When we encounter a curve, we use this
         * field to swallow a fixed number of values, so that the curve definition
         * becomes a line definition
         */
        private int mSwallow = 0;

        /**
         * Track if we're parsing a curve or a line. Valid values are 0 for
         * "uninitialized", 1 for "starting to read", 2 for "parsing curve", and 3
         * for "parsing line"
         */
        private int mMode = 0;

        /**
         * When we draw a line, we must make it a PhysicsSprite or else hero
         * collisions with the SVG won't enable it to re-jump. This class is a very
         * lightweight PhysicsSprite that serves our need.
         */
        public class SVGSprite: PhysicsSprite {
            public SVGSprite(String imgName, float width, float height): base(imgName, width, height) {
                
            }

            internal override void OnCollide(PhysicsSprite other, Contact contact)
            { }
        }

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
        private Svg(float density, float elasticity, float friction, float stretchX, float stretchY,
                float xposeX, float xposeY) {
            // create the physics fixture in a manner that is visible to the
            // addLine routine of the parser
            mFixture = new FixtureDef();
            // NOTE: UNCOMMENT
            /*mFixture.density = density;
            mFixture.restitution = elasticity;
            mFixture.friction = friction;*/

            // specify transpose and stretch information
            mUserStretch.X = stretchX / Physics.PIXEL_METER_RATIO;
            mUserStretch.Y = stretchY / Physics.PIXEL_METER_RATIO;
            mUserTransform.X = xposeX;
            mUserTransform.Y = xposeY;
        }

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
            if (attribute.StartsWith("translate(")) {
                String x2 = attribute.Replace("translate(", "");
                x2 = x2.Replace(")", ",");
                String delims = "[,]+";
                String[] points = x2.Split(delims.ToCharArray(), StringSplitOptions.None);
                mSvgTranslate.X = float.Parse(points[0]);
                mSvgTranslate.Y = float.Parse(points[1]);
            }
        }

        /**
         * The root of an SVG drawing will have a <g> element, which will have some
         * number of <path> elements. Each <path> will have a "d=" attribute, which
         * stores the points and information about how to connect them. The "d" is a
         * single string, which we parse in this file
         * 
         * @param d The string that describes the path
         */
        private void processD(String d) {
            // split the string into characters and floating point values
            String delims = "[ ,]+";
            String[] points = d.Split(delims.ToCharArray(), StringSplitOptions.None);
            // SVG can give point coords in absolute or relative terms
            bool absolute = false;
            foreach (String s0 in points) {
                String s = s0.Trim();
                // start of the path, relative mode
                if (s == "m") {
                    mState = -2;
                    absolute = false;
                }
                // start of the path, absolute mode
                else if (s == "M") {
                    mState = -2;
                    absolute = true;
                }
                // beginning of a (set of) curve definitions, relative mode
                //
                // NB: we coerce curves into lines by ignoring the first four
                // parameters... this leaves us with just the endpoints
                else if (s == "c") {
                    mMode = 2;
                    mSwallow = 4;
                }
                // end of path, relative mode
                else if (s == "z") {
                    // draw a connecting line to complete the shape
                    addLine((mUserStretch.X * (mLast.X + mSvgTranslate.X) + mUserTransform.X),
                            (mUserStretch.Y * (mLast.Y + mSvgTranslate.Y) + mUserTransform.Y),
                            (mUserStretch.X * (mFirst.X + mSvgTranslate.X) + mUserTransform.X),
                            (mUserStretch.Y * (mFirst.Y + mSvgTranslate.Y) + mUserTransform.Y));
                }
                // beginning of a (set of) line definitions, relative mode
                else if (s == "l") {
                    mMode = 3;
                    absolute = false;
                    mSwallow = 0;
                }
                // beginning of a (set of) line definitions, absolute mode
                else if (s == "L") {
                    mMode = 3;
                    absolute = true;
                    mSwallow = 0;
                }
                // floating point data that defines an endpoint of a line or curve
                else {
                    // if it's a curve, we might need to swallow this value
                    if (mSwallow > 0) {
                        mSwallow--;
                    }
                    // get the next point
                    else {
                        try {
                            // convert next point to float
                            float val = float.Parse(s);
                            // if it's the initial x, save it
                            if (mState == -2) {
                                mState = -1;
                                mLast.X = val;
                                mFirst.X = val;
                            }
                            // if it's the initial y, save it... can't draw a line
                            // yet, because we have 1 endpoint
                            else if (mState == -1) {
                                mState = 0;
                                mLast.Y = val;
                                mFirst.Y = val;
                            }
                            // if it's an X value, save it
                            else if (mState == 0) {
                                if (absolute)
                                    mCurr.X = val;
                                else
                                    mCurr.X = mLast.X + val;
                                mState = 1;
                            }
                            // if it's a Y value, save it and draw a line
                            else if (mState == 1) {
                                mState = 0;
                                if (absolute)
                                    mCurr.Y = val;
                                else
                                    mCurr.Y = mLast.Y - val;
                                // draw the line
                                addLine((mUserStretch.X * (mLast.X + mSvgTranslate.X) + mUserTransform.X),
                                        (mUserStretch.Y * (mLast.Y + mSvgTranslate.Y) + mUserTransform.Y),
                                        (mUserStretch.X * (mCurr.X + mSvgTranslate.X) + mUserTransform.X),
                                        (mUserStretch.Y * (mCurr.Y + mSvgTranslate.Y) + mUserTransform.Y));
                                mLast.X = mCurr.X;
                                mLast.Y = mCurr.Y;
                                // if we are in curve mode, reinitialize the
                                // swallower
                                if (mMode == 2)
                                    mSwallow = 4;
                            }
                        }
                        // ignore errors...
                        catch (Exception nfs) {
                            Util.log("SVG Error", "error parsing SVG file");
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
         * and this is not a proper "Obstacle"
         * 
         * @param x1 X coordinate of first endpoint
         * @param y1 Y coordinate of first endpoint
         * @param x2 X coordinate of second endpoint
         * @param y2 Y coordinate of second endpoint
         */
        private void addLine(float x1, float y1, float x2, float y2) {
            // Create a static body for an Edge shape
            // NOTE: UNCOMMENT
            /*BodyDef bd = new BodyDef();
            bd.type = BodyType.StaticBody;
            // compute center and length
            float centerX = (x1 + x2) / 2;
            float centerY = (y1 + y2) / 2;
            float len = (float)Math.Sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
            bd.position.set(centerX, centerY);
            bd.angle = 0;
            Body b = Level.sCurrent.mWorld.createBody(bd);
            EdgeShape line = new EdgeShape();

            // set the line position as an offset from center, rotate it, and
            // connect a fixture
            line.set(-len / 2, 0, len / 2, 0);
            mFixture.shape = line;
            b.createFixture(mFixture);
            mFixture.shape.dispose(); // i.e., line.dispose()
            b.setTransform(centerX, centerY, Math.Atan2(y2 - y1, x2 - x1));

            // connect it to an invisible PhysicsSprite, so that collision callbacks
            // will work (i.e., for inAir)
            SVGSprite invis = new SVGSprite("", (float) len, (float) 0.1);
            invis.mBody = b;
            b.setUserData(invis);
            // NB: we probably don't need to put the invisible sprite on the screen,
            // since we don't overload render()... this is invisible.
            Level.sCurrent.addSprite(invis, 0);*/
        }

        /**
         * The main parse routine. We slurp the file into an XML DOM object, and
         * then iterate over it, finding the <path>s within the <g>, and processing
         * their "d" attributes
         * 
         * @param svgName The name of the file to parse
         */
        private void parse(String svgName) {
            // TODO: Figure out equivalent of XmlReader and read SVG
            /*XmlReader r = new XmlReader();
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
            }*/
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
            Svg s = new Svg(density, elasticity, friction, stretchX, stretchY, xposeX, xposeY);
            s.parse(svgName);
        }

    }
}
