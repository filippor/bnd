package aQute.libg.gzip;

import java.io.*;

import junit.framework.*;
import aQute.lib.io.*;

public class TestGzipUtils extends TestCase {

	public void testUnzipped() throws Exception {
		InputStream stream = GZipUtils.detectCompression(TestGzipUtils.class.getResourceAsStream("unzipped.dat"));
		try {
			assertEquals("A plan, a plan, a canal, Panama.", IO.collect(stream));
		} finally {
			stream.close();
		}
	}

	public void testZipped() throws Exception {
		InputStream stream = GZipUtils.detectCompression(TestGzipUtils.class.getResourceAsStream("zipped.dat"));
		try {
			assertEquals("A plan, a plan, a canal, Panama.", IO.collect(stream));
		} finally {
			stream.close();
		}
	}

}
