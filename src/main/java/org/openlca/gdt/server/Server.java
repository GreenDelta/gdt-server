package org.openlca.gdt.server;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class Server {

  public static void main(String[] sysArgs) {

		var args = Args.parse(sysArgs);

    var db = args.db();

    var data = new DataService(db);
    var results = new ResultService(args);

    var app = Javalin.create(config -> {
			if (args.staticDir() != null) {
				config.addStaticFiles(
						args.staticDir().getAbsolutePath(), Location.EXTERNAL);
			}
			config.enableCorsForAllOrigins();
		}).start(args.port());

    // get data
    app.get("/data/{type-path}", data::getInfos);
    app.get("/data/{type-path}/{id}", data::get);
    app.get("/data/{type-path}/{id}/info", data::getInfo);
		app.get("/data/{type-path}/{id}/parameters", data::getParameters);

		// put & delete data
		if (!args.isReadonly()) {
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
