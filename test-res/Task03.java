/*******************************************************************************
 * Copyright (c) 2009 David Harrison.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.html
 *
 * Contributors:
 *     David Harrison - initial API and implementation
 ******************************************************************************/
//package com.sfs;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * The Class ConvertHtmlToTextTest.
 *
 * @author David Harrison
 */
public class ConvertHtmlToTextTest {

    /**
     * Instantiates a new convert html to text test.
     */
    public ConvertHtmlToTextTest() {
    }

    /**
     * Test of convert method, of class ConvertHtmlToText.
     *
     * @throws Exception the exception
     */
    @Test
    public final void testConvert() throws Exception {
        System.out.println("convert");

        ConvertHtmlToText instance = new ConvertHtmlToText();

        final String expResult = this.getExpResult();
        final String result = instance.convert(getSource());

        // System.out.println("Expected Result: " +
        // StringUtils.replace(expResult, "\n","~"));
        // System.out.println("Result: " + StringUtils.replace(result, "\n","~"));;

        assertEquals(expResult, result);
    }

    /**
     * Gets the source.
     *
     * @return the source
     */
    private String getSource() {
        StringBuffer source = new StringBuffer();

        source.append("<html>");
        source.append("<head>");
        source.append("<title> favorites / bookmark title goes here </title>");
        source.append("<style type=\"text/css\">");
        source.append("body { color: purple; background-color: #d8da3d }");
        source.append("</style>");
        source.append("</head>");
        source.append("<body bgcolor=\"white\" text=\"blue\">");
        source.append("<h1>My first page </h1>");
        source.append("This is my first web page and I can say anything ");
        source.append("I want in here.<br/>");
        source.append("I do that by putting text or images in the body section.");
        source.append("<p>Where I'm typing right now :)</p>");
        source.append("<p>Some text with <a href=\"http://www.google.com\">a link</a>");
        source.append("followed by some text.</p>");
        source.append("<hr/>");
        source.append("<h3>Unordered list</h3>");
        source.append("<ul>");
        source.append("<li>Item one</li>");
        source.append("<li>Item two</li>");
        source.append("<li>Item three</li>");
        source.append("<li>Item four</li>");
        source.append("</ul>");
        source.append("<p>Good bye...</p>");
        source.append("</body>");
        source.append("</html>");

        return source.toString();
    }

    /**
     * Gets the exp result.
     *
     * @return the exp result
     */
    private String getExpResult() {
        StringBuffer result = new StringBuffer();
        result.append("My first page \n");
        result.append("\n");
        result.append("This is my first web page and I can say anything ");
        result.append("I want in here.\n");
        result.append("I do that by putting text or images in the body section.\n");
        result.append("Where I'm typing right now :)\n");
        result.append("\n");
        result.append("\n");
        result.append("Some text with a link [http://www.google.com] followed ");
        result.append("by some text.\n");
        result.append("\n");
        result.append("_________________________________________\n");
        result.append("\n");
        result.append("\n");
        result.append("Unordered list\n");
        result.append("\n");
        result.append("  * Item one\n");
        result.append("  * Item two\n");
        result.append("  * Item three\n");
        result.append("  * Item four\n");
        result.append("\n");
        result.append("\n");
        result.append("Good bye...");

        return result.toString();
    }
}
