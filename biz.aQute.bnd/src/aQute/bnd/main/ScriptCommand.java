package aQute.bnd.main;

import java.io.*;
import java.util.*;

import javax.script.*;

import aQute.bnd.main.bnd.scriptOptions;
import aQute.lib.io.*;
import aQute.lib.osgi.*;

public class ScriptCommand {
	static ScriptEngineManager	mgr		= new ScriptEngineManager();
	static ScriptEngine			engine	= mgr.getEngineByName("JavaScript");

	public ScriptCommand(bnd bnd, scriptOptions opts) throws IOException, ScriptException {
		String f = opts.file();
		String s;

		if (f != null) {
			File ff = bnd.getFile(f);
			if (ff.isFile()) {
				s = IO.collect(ff);
			} else {
				bnd.error("No such file " + ff);
				return;
			}
		} else
			s = Processor.join(opts._(), " ");

		Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
		bindings.put("bnd", this);
		engine.eval(s);
	}

	@SuppressWarnings("unchecked") public Object copy(Map from, Map to) {
		for ( Object key : from.keySet()) {
			to.put(key,from.get(key));
		}
		return to;
	}
}