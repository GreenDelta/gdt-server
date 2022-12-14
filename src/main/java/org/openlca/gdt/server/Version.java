package org.openlca.gdt.server;

import com.google.gson.JsonObject;
import io.javalin.http.Context;
import org.openlca.jsonld.Json;
import org.openlca.nativelib.Module;
import org.openlca.nativelib.NativeLib;

class Version {

	private static final String VERSION = "2.0.21";

	static void get(Context ctx) {
		var obj = new JsonObject();
		Json.put(obj, "version", VERSION);
		/* better do not send more details?
		var os = System.getProperty("os.name")
				+ "; " + System.getProperty("os.version")
				+ "; " + System.getProperty("os.arch");
		Json.put(obj, "os", os);
		var jvm = System.getProperty("java.vendor")
				+ "; " + System.getProperty("java.version");
		Json.put(obj, "jvm", jvm);
		*/
		Json.put(obj, "isBlasEnabled", NativeLib.isLoaded(Module.BLAS));
		Json.put(obj, "isUmfpackEnabled", NativeLib.isLoaded(Module.UMFPACK));
		Http.sendOk(ctx, obj);
	}

}
