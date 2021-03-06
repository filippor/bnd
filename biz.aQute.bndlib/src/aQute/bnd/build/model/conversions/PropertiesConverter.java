package aQute.bnd.build.model.conversions;

import java.util.*;

import aQute.bnd.header.*;

public class PropertiesConverter implements Converter<Map<String,String>,String> {

	public Map<String,String> convert(String input) throws IllegalArgumentException {
		if (input == null)
			return null;
		return OSGiHeader.parseProperties(input);
	}

}
