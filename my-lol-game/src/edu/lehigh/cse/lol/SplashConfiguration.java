/**
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org>
 */

package edu.lehigh.cse.lol;

/**
 * The SplashConfiguration interface describes the key characteristics of the
 * splash screen. In LOL, we assume there is a single image that comprises the
 * background, and that any text or graphics that are needed to instruct the
 * user (i.e., a "play button") are part of the image. That being the case, the
 * splash screen consists of an image, a sound file, and descriptions of the
 * regions that should be treated as Play, Help, and Quit buttons
 */
public interface SplashConfiguration {
    /**
     * Indicate the X coordinate of the bottom left corner of the "PLAY" button
     */
    public int getPlayX();

    /**
     * Indicate the Y coordinate of the bottom left corner of the "PLAY" button
     */
    public int getPlayY();

    /**
     * Indicate the width of the "PLAY" button
     */
    public int getPlayWidth();

    /**
     * Indicate the height of the "PLAY" button
     */
    public int getPlayHeight();

    /**
     * Indicate the X coordinate of the bottom left corner of the "HELP" button
     */
    public int getHelpX();

    /**
     * Indicate the Y coordinate of the bottom left corner of the "HELP" button
     */
    public int getHelpY();

    /**
     * Indicate the width of the "HELP" button
     */
    public int getHelpWidth();

    /**
     * Indicate the height of the "HELP" button
     */
    public int getHelpHeight();

    /**
     * Indicate the X coordinate of the bottom left corner of the "QUIT" button
     */
    public int getQuitX();

    /**
     * Indicate the Y coordinate of the bottom left corner of the "QUIT" button
     */
    public int getQuitY();

    /**
     * Indicate the width of the "QUIT" button
     */
    public int getQuitWidth();

    /**
     * Indicate the height of the "QUIT" button
     */
    public int getQuitHeight();

    /**
     * Indicate the name of the file that serves as the background image for the
     * opening screen
     */
    public String getBackgroundImage();

    /**
     * Indicate the name of the file that serves as the music for the opening
     * screen
     */
    public String getMusic();
}
