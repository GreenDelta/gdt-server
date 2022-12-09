package org.openlca.gdt.server;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.bundled.CorsPluginConfig;
import org.openlca.core.services.ServerConfig;

public class Server {

	public static void main(String[] sysArgs) {

		var config = ServerConfig.parse(sysArgs);

		var db = config.db();

		var data = new DataService(db);
		var results = new ResultService(config);

		var app = Javalin.create(c -> {
			if (config.staticDir() != null) {
				c.staticFiles.add(
						config.staticDir().getAbsolutePath(), Location.EXTERNAL);
			}
			c.plugins.enableCors(cors -> cors.add(CorsPluginConfig::anyHost));
		}).start(config.port());

		// get data
		app.get("/data/providers", data::getProviders);
		app.get("/data/providers/{id}", data::getProvidersOfFlow);
		app.get("/data/{type-path}", data::getDescriptors);
		app.get("/data/{type-path}/{id}", data::get);
		app.get("/data/{type-path}/{id}/info", data::getDescriptor);
		app.get("/data/{type-path}/{id}/parameters", data::getParameters);

		// put & delete data
		if (!config.isReadonly()) {
			app.put("/data/{type-path}", data::put);
			app.delete("/data/{type-path}/{id}", data::delete);
		}

		// calculation & dispose
		app.post("/results/calculate", results::calculate);
		app.post("/results/{id}/dispose", results::dispose);
		app.delete("/results/{id}", results::dispose);
		app.get("/results/{id}/state", results::getState);

		// result queries
		app.get("/results/{id}/total-impacts", results::getTotalImpacts);

		// register a shutdown hook for closing database and server
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				db.close();
				app.close();
			} catch (Exception e) {
				System.out.println("shutdown failed: " + e.getMessage());
			}
		}));
	}
}
