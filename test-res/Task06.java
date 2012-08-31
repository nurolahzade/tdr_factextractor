//package de.fzj.unicore.cis;

import static org.junit.Assert.*;

//import java.io.File;
import java.io.IOException;
import java.util.List;

//import org.apache.xmlbeans.XmlException;
//import org.custommonkey.xmlunit.DetailedDiff;
//import org.custommonkey.xmlunit.Diff;
import org.junit.Test;
//import org.ogf.schemas.glue.x2008.x05.spec20D42R01.DomainsDocument;
import org.xml.sax.SAXException;

public class TestXmlDiff {

	String xml1 = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +
		"<a>" +
		"<b>text1</b>" +
		"<c>text2</c>" +
		"</a>";

	String xml2 = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +
		"<!--  copy -->" +
		"<a>" +
		"<c>text2</c>" +
		"<b>text1</b>" +
		"</a>";

        /**
         * test the difference between the two xml docuemnts
         *
         * @throws IOException
         * @throws XmlException
         * @throws SAXException
         */
        @Test
        public void testDiff() throws /*XmlException,*/ IOException, SAXException {
//                DomainsDocument dd1 = DomainsDocument.Factory.parse(new File(
//                                "src/test/resources/instances/test1.xml"));
//                DomainsDocument dd2 = DomainsDocument.Factory.parse(new File(
//                                "src/test/resources/instances/test2.xml"));
//                Diff myDiff = new Diff(dd1.toString(), dd2.toString());
        		
        		Diff myDiff = new Diff(xml1, xml2);
                assertTrue("pieces of XML are similar " + myDiff, myDiff.similar());
                assertFalse("but are they identical? " + myDiff, myDiff.identical());

//                System.out.println("are both documents identical: " + myDiff.similar());

        }

//        @Test
//        public void testDetailedDiff() throws /*XmlException,*/ IOException, SAXException {
////                DomainsDocument dd1 = DomainsDocument.Factory.parse(new File(
////                                "src/test/resources/instances/test1.xml"));
////                DomainsDocument dd2 = DomainsDocument.Factory.parse(new File(
////                                "src/test/resources/instances/test2.xml"));
////                Diff myDiff = new Diff(dd1.toString(), dd2.toString());
//        	
//        		Diff myDiff = new Diff(xml1, xml2);
//                DetailedDiff myDetailedDiff = new DetailedDiff(myDiff);
//                List lst = myDetailedDiff.getAllDifferences();
//                assertTrue("are they identical?", myDetailedDiff.identical());
//
//        }
}