package org.openlca.gdt.server;

import io.javalin.http.Context;
import org.openlca.core.services.JsonResultService;
import org.openlca.core.services.ServerConfig;

class ResultService {

	private final JsonResultService service;

	ResultService(ServerConfig config) {
		// TODO: configure possible thread count
		this.service = JsonResultService.of(config.db());
	}

	void calculate(Context ctx) {
		var json = Http.readBodyOf(ctx);
		if (json == null)
			return;
		if (!json.isJsonObject()) {
			Http.sendBadRequest(ctx, "no valid calculation setup provided");
			return;
		}
		var r = service.calculate(json.getAsJsonObject());
		Http.respond(ctx, r);
	}

	void getState(Context ctx) {
		var id = ctx.pathParam("id");
		var r = service.getState(id);
		Http.respond(ctx, r);
	}

	void dispose(Context ctx) {
		var id = ctx.pathParam("id");
		var r = service.dispose(id);
		Http.respond(ctx, r);
	}

	void getTotalImpacts(Context ctx) {
		var id = ctx.pathParam("id");
		var r = service.getTotalImpacts(id);
		Http.respond(ctx, r);
	}
}
