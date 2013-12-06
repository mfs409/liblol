package edu.lehigh.cse.ale;

// STATUS: in progress

import java.util.Random;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import edu.lehigh.cse.ale.Level.Renderable;

public class Util
{
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
        Obstacle b = Obstacle.makeAsBox(x0-1, y0-1, Math.abs(x0-x1)+2, 1, imgName);
        b.setPhysics(density, elasticity, friction);

        Obstacle t = Obstacle.makeAsBox(x0-1, y1, Math.abs(x0-x1)+2, 1, imgName);
        t.setPhysics(density, elasticity, friction);

        Obstacle l = Obstacle.makeAsBox(x0-1, y0-1, 1, Math.abs(y0 - y1)+2, imgName);
        l.setPhysics(density, elasticity, friction);

        Obstacle r = Obstacle.makeAsBox(x1, y0-1, 1, Math.abs(y0-y1)+2, imgName);
        r.setPhysics(density, elasticity, friction);
    }

    /**
     * Draw a picture on the _current level
     * 
     * Note: the order in which this is called relative to other entities will determine whether they go under or over
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
            final TextureRegion tr = Media.getImage(imgName);

            @Override
            void render(SpriteBatch sb)
            {
                sb.draw(tr, x, y, 0,
                        0, width, height, 1, 1, 0);
            }
            
        };
        Level._currLevel._pix.add(r);
    }

    /**
     * Draw a picture on the _current level, but unlike the regular drawPicture, this draws a picture behind the rest of
     * the scene
     * 
     * Note: the order in which this is called relative to other entities will determine whether they go under or over
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
    public static void drawPictureBehindScene(final float x, final float y, final float width, final float height, final String imgName)
    {
        Renderable r = new Renderable()
        {
            final TextureRegion tr = Media.getImage(imgName);
            @Override
            void render(SpriteBatch sb)
            {
                sb.draw(tr, x, y, 0, 0, width, height, 1, 1, 0);
            }
        };
        Level._currLevel._pix_minus_two.add(r);
    }

    /**
     * Load an SVG line drawing generated from Inkscape.
     * 
     * Note that not all Inkscape drawings will work as expected. See SVGParser.java for more information.
     * 
     * @param svgFileName
     *            Name of the svg file to load. It should be in the assets folder
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
     *            Shift the drawing in the X dimension. Note that shifting occurs after stretching
     * @param xposeY
     *            Shift the drawing in the Y dimension. Note that shifting occurs after stretching
     *//*
    static public void importSVGLineDrawing(String svgFileName, float red, float green, float blue, float density,
            float elasticity, float friction, float stretchX, float stretchY, float xposeX, float xposeY)
    {
        try {
            // create a SAX parser for SVG files
            final SAXParserFactory spf = SAXParserFactory.newInstance();
            final SAXParser sp = spf.newSAXParser();

            final XMLReader xmlReader = sp.getXMLReader();
            SVGParser Parser = new SVGParser();

            // make the color values visible to the addLine routine of the
            // parser
            Parser._lineRed = red / 255;
            Parser._lineGreen = green / 255;
            Parser._lineBlue = blue / 255;

            // create the _physics _fixture in a manner that is visible to the
            // addLine
            // routine of the parser
            Parser._fixture = PhysicsFactory.createFixtureDef(density, elasticity, friction);

            // specify transpose and stretch information
            Parser._userStretchX = stretchX;
            Parser._userStretchY = stretchY;
            Parser._userTransformX = xposeX;
            Parser._userTransformY = xposeY;

            // start parsing!
            xmlReader.setContentHandler(Parser);
            AssetManager am = ALE._self.getAssets();
            InputStream inputStream = am.open(svgFileName);
            xmlReader.parse(new InputSource(new BufferedInputStream(inputStream)));
        }
        // if the read fails, just print a stack trace
        catch (Exception e) {
            e.printStackTrace();
        }
    }
*/
}
