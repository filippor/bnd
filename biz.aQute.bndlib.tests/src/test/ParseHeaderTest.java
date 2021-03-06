package test;

import java.util.*;

import junit.framework.*;
import aQute.bnd.header.*;
import aQute.bnd.header.Attrs.Type;
import aQute.bnd.osgi.*;
import aQute.bnd.version.*;

public class ParseHeaderTest extends TestCase {

	
	public void testTyped() {
		Parameters p = new Parameters("a;a:Long=1;b:Double=3.2;c:String=abc;d:Version=1"
				+ ";e:List<Long>='1,2,3';f:List<Double>='1.0,1.1,1.2';g:List<String>='abc,def,ghi';h:List<Version>='1.0.1,1.0.2'");

		String s = p.toString();
		System.out.println(s);
		assertEquals("a;a:Long=1;b:Double=\"3.2\";c:String=abc;d:Version=1;"
				+ "e:List<Long>=\"1,2,3\";f:List<Double>=\"1.0,1.1,1.2\";g:List<String>=\"abc,def,ghi\";h:List<Version>=\"1.0.1,1.0.2\"", s);
		
		Attrs attrs = p.get("a");
		assertNotNull(attrs);
		
		assertEquals( 1L, attrs.getTyped("a"));
		assertEquals( 3.2d, attrs.getTyped("b"));
		assertEquals( "abc", attrs.getTyped("c"));
		assertEquals( new Version("1"), attrs.getTyped("d"));
		assertEquals( Arrays.asList(1L,2L,3L), attrs.getTyped("e"));
		assertEquals( Arrays.asList(1.0D,1.1D,1.2D), attrs.getTyped("f"));
		assertEquals( Arrays.asList("abc","def","ghi"), attrs.getTyped("g"));
		assertEquals( Arrays.asList(new Version("1.0.1"), new Version("1.0.2")), attrs.getTyped("h"));
	}
	
	public void testEscaping() {
		
		{
			// Spaces at end of quoted string 
			Parameters pp = new Parameters("a;string.list3:List<String>=\" aString , bString , cString \"");
			assertEquals("a;string.list3:List<String>=\" aString , bString , cString \"", pp.toString());
		}
		{
			// It should be string.list2:List="a\"quote,a\,comma, aSpace ,\"start,\,start,end\",end\," (not handling escape of comma) 
			Parameters pp = new Parameters("a;b:List=\"a\\\"quote,a\\\\backslash,a\\,comma, aSpace ,\\\"start,\\,start\\,end\"");
			assertEquals("a;b:List=\"a\\\"quote,a\\\\backslash,a\\,comma, aSpace ,\\\"start,\\,start\\,end\"", pp.toString());
		}

		{
			Parameters pp = new Parameters("a;a:List<String>='abc'");
			assertEquals("a;a:List<String>=abc", pp.toString());
		}

	}

	public static void testPropertiesSimple() {
		Map<String,String> p = OSGiHeader.parseProperties("a=1, b=\"3   3\", c=c");
		assertEquals("c", p.get("c"));
		assertEquals("1", p.get("a"));
		assertEquals("3   3", p.get("b"));
	}

	public static void testClauseName() {
		assertNames("a,b,c;", new String[] {
				"a", "b", "c"
		});
		assertNames("a,b,c", new String[] {
				"a", "b", "c"
		});
		assertNames("a;x=0,b;x=0,c;x=0", new String[] {
				"a", "b", "c"
		});
		assertNames("a;b;c;x=0", new String[] {
				"a", "b", "c"
		});
		assertNames(",", new String[] {}, null, "Empty clause, usually caused");
		assertNames("a;a,b", new String[] {
				"a", "a~", "b"
		}, null, "Duplicate name a used in header");
		assertNames("a;x=0;b", new String[] {
				"a", "b"
		}, "Header contains name field after attribute or directive", null);
		assertNames("a;x=0;x=0,b", new String[] {
				"a", "b"
		}, null, "Duplicate attribute/directive name");
		assertNames("a;;;,b", new String[] {
				"a", "b"
		});
		assertNames(",,a,,", new String[] {
			"a"
		}, null, "Empty clause, usually caused by repeating");
		assertNames(",a", new String[] {
			"a"
		}, null, "Empty clause, usually caused");
		assertNames(",a,b,c,", new String[] {
				"a", "b", "c"
		}, null, "Empty clause, usually caused");
		assertNames("a,b,c,", new String[] {
				"a", "b", "c"
		}, null, "Empty clause, usually caused");
		assertNames("a,b,,c", new String[] {
				"a", "b", "c"
		}, null, "Empty clause, usually caused");
	}

	static void assertNames(String header, String[] keys) {
		assertNames(header, keys, null, null);
	}

	static void assertNames(String header, String[] keys, String expectedError, String expectedWarning) {
		Processor p = new Processor();
		p.setPedantic(true);
		Parameters map = Processor.parseHeader(header, p);
		for (String key : keys)
			assertTrue(map.containsKey(key));

		assertEquals(keys.length, map.size());
		if (expectedError != null) {
			System.err.println(p.getErrors());
			assertTrue(p.getErrors().size() > 0);
			assertTrue(p.getErrors().get(0).indexOf(expectedError) >= 0);
		} else
			assertEquals(0, p.getErrors().size());
		if (expectedWarning != null) {
			System.err.println(p.getWarnings());
			assertTrue(p.getWarnings().size() > 0);
			String w = p.getWarnings().get(0);
			assertTrue(w.startsWith(expectedWarning));
		} else
			assertEquals(0, p.getWarnings().size());
	}

	public static void testSimple() {
		String s = "a;a=a1;b=a2;c=a3, b;a=b1;b=b2;c=b3, c;d;e;a=x1";
		Parameters map = Processor.parseHeader(s, null);
		assertEquals(5, map.size());

		Map<String,String> a = map.get("a");
		assertEquals("a1", a.get("a"));
		assertEquals("a2", a.get("b"));
		assertEquals("a3", a.get("c"));

		Map<String,String> d = map.get("d");
		assertEquals("x1", d.get("a"));

		Map<String,String> e = map.get("e");
		assertEquals(e, d);

		System.err.println(map);
	}

	public static void testParseMultiValueAttribute() {
		String s = "capability;foo:List<String>=\"MacOSX,Mac OS X\";version:List<Version>=\"1.0, 2.0, 2.1\"";
		Parameters map = Processor.parseHeader(s, null);

		Attrs attrs = map.get("capability");

		assertEquals(Type.STRINGS, attrs.getType("foo"));
		List<String> foo = (List<String>) attrs.getTyped("foo");
		assertEquals(2, foo.size());
		assertEquals("MacOSX", foo.get(0));
		assertEquals("Mac OS X", foo.get(1));

		assertEquals(Type.VERSIONS, attrs.getType("version"));
		List<Version> version = (List<Version>) attrs.getTyped("version");
		assertEquals(3, version.size());
		assertEquals(new Version(1), version.get(0));
		assertEquals(new Version(2), version.get(1));
		assertEquals(new Version(2, 1), version.get(2));
	}
}
