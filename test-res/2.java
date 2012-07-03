import org.junit.Assert;

import junit.framework.TestCase;

public class Base64UtilTest extends TestCase {
	
    public void testEncodeString() {
    	final String text = "This is a test";
    	Base64Util util = new Base64Util();
    	String base64 = util.encodeString(text);
    	System.out.println(base64);
        String restore = util.decodeString(base64);
        Assert.assertEquals(text, restore);
    }
/*
    public void testEncode() {
        final byte[] data = "abcdefg \r\n hijklmn \t opqrst \u3000 uvwxyz".getBytes();
    	Base64Util util = new Base64Util();
        String base64 = util.encode(data);
        byte[] restore = util.decode(base64);
        assertEquals(data.length, restore.length);
        for(int i=0; i<data.length; i++) {
            assertEquals(data[i], restore[i]);
        }
    }

    public void testDecodeBadBase64() {
    	final String base64 = "ABCDEFG@@@\u3000\n\n@@..=";
    	Base64Util util = new Base64Util();
        assertNull(util.decode(base64));
    }
*/
}