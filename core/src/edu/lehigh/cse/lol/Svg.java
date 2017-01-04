/**
 * This is free and unencumbered software released into the public domain.
 * <p>
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * <p>
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * <p>
 * For more information, please refer to <http://unlicense.org>
 */

package edu.lehigh.cse.lol;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

import java.io.IOException;

/**
 * The Svg infrastructure allows the game designer to load SVG line drawings into a game. SVG line
 * drawings can be made in InkScape. In LOL, we do not use line drawings to their full potential. We
 * only use them to define a set of invisible lines for a simple, stationary obstacle. You should
 * draw a picture on top of your line drawing, so that the player knows that there is an actor on
 * the screen.
 */
public class Svg {
    /// A reference to the public API, so that we can easily make Obstacles as we draw the SVG
    private final Level mLevel;

    /// This callback will run whenever we create a new line segment
    private final LolActorEvent mActorCallback;
    /// The offset by which we shift the line drawing
    private final Vector2 mUserTransform;
    /// The amount by which we stretch the drawing
    private final Vector2 mUserStretch;
    /// SVG files can have an internal "translate" field... while parsing, we save the field here
    private final Vector2 mSvgTranslate;
    /// Coordinate of the last point we drew
    private final Vector2 mLast;
    /// Coordinate of the first point we drew
    private final Vector2 mFirst;
    /// Coordinate of the current point being drawn
    private final Vector2 mCurr;
    /// The parser is essentially a finite state machine. The states are 0 for "read next x", 1 for
    /// "read next y", -2 for "read first x", and -1 for "read first y"
    private int mState;
    /// Our parser can't handle curves. When we encounter a curve, we use this field to swallow a
    /// fixed number of values, so that the curve definition becomes a line definition
    private int mSwallow;
    /// Track if we're parsing a curve or a line. Valid values are 0 for "uninitialized", 1 for
    // "starting to read", 2 for "parsing curve", and 3 for "parsing line"
    private int mMode;

    /**
     * Configure a parser that we can use to load an SVG file and draw each of
     * its lines as an Obstacle
     *
     * @param stretchX   Stretch the drawing in the X dimension by this percentage
     * @param stretchY   Stretch the drawing in the Y dimension by this percentage
     * @param transposeX Shift the drawing in the X dimension. Note that shifting
     *                   occurs after stretching
     * @param transposeY Shift the drawing in the Y dimension. Note that shifting
     *                   occurs after stretching
     * @param callback   The callback to run whenever a line is created
     */
    Svg(Level level, float stretchX, float stretchY, float transposeX, float transposeY,
        LolActorEvent callback) {
        mLevel = level;

        // specify transpose and stretch information
        mUserStretch = new Vector2(1, 1);
        mUserStretch.x = stretchX;
        mUserStretch.y = stretchY;
        mUserTransform = new Vector2(0, 0);
        mUserTransform.x = transposeX;
        mUserTransform.y = transposeY;

        // save the callback
        mActorCallback = callback;

        // initialize other fields
        mSvgTranslate = new Vector2(0, 0);
        mLast = new Vector2(0, 0);
        mFirst = new Vector2(0, 0);
        mCurr = new Vector2(0, 0);
        mState = 0;
        mSwallow = 0;
        mMode = 0;
    }

    /**
     * When we encounter a "transform" attribute, we use this code to parse it, in case it has a
     * "translate" directive that we should go
     *
     * @param attribute The attribute being processed... we hope it's a valid translate directive
     */
    private void processTransform(String attribute) {
        // if we get a valid "translate" attribute, split it into two floats and save them
        if (attribute.startsWith("translate(")) {
            String x2 = attribute.replace("translate(", "");
            x2 = x2.replace(")", ",");
            String delimiters = "[,]+";
            String[] points = x2.split(delimiters);
            try {
                mSvgTranslate.x = Float.valueOf(points[0]);
                mSvgTranslate.y = Float.valueOf(points[1]);
            } catch (NumberFormatException nfs) {
                Lol.message(mLevel.mConfig, "svg error", "transform error");
            }
        }
    }

    /**
     * The root of an SVG drawing will have a g element, which will have some number of path
     * elements. Each path will have a "d=" attribute, which stores the points and information about
     * how to connect them. The "d" is a single string, which we parse in this function.
     *
     * @param d The string that describes the path
     */
    private void processD(String d) {
        // split the string into characters and floating point values
        String delimiters = "[ ,]+";
        String[] points = d.split(delimiters);
        // SVG can give point coordinates in absolute or relative terms
        boolean absolute = false;
        for (String s0 : points) {
            String s = s0.trim();
            switch (s) {
                // start of the path, relative mode
                case "m":
                    mState = -2;
                    absolute = false;
                    break;
                // start of the path, absolute mode
                case "M":
                    mState = -2;
                    absolute = true;
                    break;
                // beginning of a (set of) curve definitions, relative mode
                //
                // NB: we coerce curves into lines by ignoring the first four
                // parameters... this leaves us with just the endpoints
                case "c":
                    mMode = 2;
                    mSwallow = 4;
                    break;
                // end of path, relative mode
                case "z":
                    // draw a connecting line to complete the shape
                    addLine(mLast, mFirst);
                    break;
                // beginning of a (set of) line definitions, relative mode
                case "l":
                    mMode = 3;
                    absolute = false;
                    mSwallow = 0;
                    break;
                // beginning of a (set of) line definitions, absolute mode
                case "L":
                    mMode = 3;
                    absolute = true;
                    mSwallow = 0;
                    break;
                // floating point data that defines an endpoint of a line or curve
                default:
                    // if it's a curve, we might need to swallow this value
                    if (mSwallow > 0) {
                        mSwallow--;
                    }
                    // get the next point
                    else {
                        try {
                            // convert next point to float
                            float val = Float.valueOf(s);
                            // if it's the initial x, save it
                            if (mState == -2) {
                                mState = -1;
                                mLast.x = val;
                                mFirst.x = val;
                            }
                            // if it's the initial y, save it... can't draw a line yet, because we
                            // have one endpoint
                            else if (mState == -1) {
                                mState = 0;
                                mLast.y = val;
                                mFirst.y = val;
                            }
                            // if it's an X value, save it
                            else if (mState == 0) {
                                if (absolute)
                                    mCurr.x = val;
                                else
                                    mCurr.x = mLast.x + val;
                                mState = 1;
                            }
                            // if it's a Y value, save it and draw a line
                            else if (mState == 1) {
                                mState = 0;
                                if (absolute)
                                    mCurr.y = val;
                                else
                                    mCurr.y = mLast.y + val;
                                // draw the line
                                addLine(mLast, mCurr);
                                mLast.x = mCurr.x;
                                mLast.y = mCurr.y;
                                // if we are in curve mode, reinitialize the swallower
                                if (mMode == 2)
                                    mSwallow = 4;
                            }
                        }
                        // ignore errors...
                        catch (NumberFormatException nfs) {
                            Lol.message(mLevel.mConfig, "SVG Error", "error parsing SVG file");
                            nfs.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }

    /**
     * This is a convenience method to separate the transformation and stretch logic from the logic
     * for actually drawing lines
     * <p>
     * There are two challenges. The first is that an SVG deals with pixels, whereas we like to draw
     * actors in meters. This matters because user translations will be in meters, but SVG points
     * and SVG translations will be in pixels.
     * <p>
     * The second challenge is that SVGs appear to have a "down is plus" Y axis, whereas our system
     * has a "down is minus" Y axis. To getLoseScene around this, we reflect every Y coordinate over
     * the horizontal line that intersects with the first point drawn.
     *
     * @param start The point from which the line originates
     * @param stop  The point to which the line extends
     */
    private void addLine(Vector2 start, Vector2 stop) {
        // Get the pixel coordinates of the SVG line
        float x1 = start.x, x2 = stop.x, y1 = start.y, y2 = stop.y;
        // apply svg translation, since it is in pixels
        x1 += mSvgTranslate.x;
        x2 += mSvgTranslate.x;
        y1 += mSvgTranslate.y;
        y2 += mSvgTranslate.y;
        // reflect through mFirst.y
        y1 = mFirst.y - y1;
        y2 = mFirst.y - y2;
        // convert the coordinates to meters
        x1 /= mLevel.mConfig.mPixelMeterRatio;
        y1 /= mLevel.mConfig.mPixelMeterRatio;
        x2 /= mLevel.mConfig.mPixelMeterRatio;
        y2 /= mLevel.mConfig.mPixelMeterRatio;
        // multiply the coordinates by the stretch
        x1 *= mUserStretch.x;
        y1 *= mUserStretch.y;
        x2 *= mUserStretch.x;
        y2 *= mUserStretch.y;
        // add in the user transform in meters
        x1 += mUserTransform.x;
        y1 += mUserTransform.y;
        x2 += mUserTransform.x;
        y2 += mUserTransform.y;
        drawLine(x1, y1, x2, y2);
    }

    /**
     * Internal method used by the SVG parser to draw a line. We actually just draw a really skinny
     * Obstacle and rotate it
     *
     * @param x1 X coordinate of first endpoint
     * @param y1 Y coordinate of first endpoint
     * @param x2 X coordinate of second endpoint
     * @param y2 Y coordinate of second endpoint
     */
    private void drawLine(float x1, float y1, float x2, float y2) {
        // compute center and length
        float centerX = (x1 + x2) / 2;
        float centerY = (y1 + y2) / 2;
        float len = (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        // Make an obstacle and rotate it
        Obstacle o = mLevel.makeObstacleAsBox(x1, y1, len, .1f, "red.png");
        o.mBody.setTransform(centerX, centerY, MathUtils.atan2(y2 - y1, x2 - x1));
        // let the game code modify this line segment
        mActorCallback.go(o);
    }

    /**
     * The main parse routine. We slurp the file into an XML DOM object, and then iterate over it,
     * finding the paths within the g, and processing their "d" attributes
     *
     * @param svgName The name of the file to parse
     */
    void parse(String svgName) {
        XmlReader r = new XmlReader();
        try {
            Element root = r.parse(Gdx.files.internal(svgName));
            // get the <g> tags
            Array<Element> gs = root.getChildrenByName("g");
            for (Element g : gs) {
                // Get the g's transform attribute
                String transform = g.getAttribute("transform", "");
                if (!transform.equals(""))
                    processTransform(transform);
                // get each g's paths
                Array<Element> paths = g.getChildrenByName("path");
                for (Element p : paths)
                    processD(p.getAttribute("d"));
            }
        } catch (IOException e) {
            Lol.message(mLevel.mConfig, "SVG Error", "error parsing SVG file");
            e.printStackTrace();
        }
    }
}
