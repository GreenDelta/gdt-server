package org.openlca.gdt.server;

import io.javalin.http.Context;
import org.openlca.core.services.EnviFlowId;
import org.openlca.core.services.JsonResultService;
import org.openlca.core.services.ServerConfig;
import org.openlca.core.services.TechFlowId;

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

	void getTechFlows(Context ctx) {
		var id = ctx.pathParam("id");
		var r = service.getTechFlows(id);
		Http.respond(ctx, r);
	}

	void getEnviFlows(Context ctx) {
		var id = ctx.pathParam("id");
		var r = service.getEnviFlows(id);
		Http.respond(ctx, r);
	}

	void getImpactCategories(Context ctx) {
		var id = ctx.pathParam("id");
		var r = service.getImpactCategories(id);
		Http.respond(ctx, r);
	}

	void getTotalRequirements(Context ctx) {
		var id = ctx.pathParam("id");
		var r = service.getTotalRequirements(id);
		Http.respond(ctx, r);
	}

	void getTotalRequirementsOf(Context ctx) {
		var id = ctx.pathParam("id");
		var techFlow = TechFlowId.parse(ctx.pathParam("tech-flow"));
		var r = service.getTotalRequirementsOf(id, techFlow);
		Http.respond(ctx, r);
	}

	// region: flow results

	void getTotalFlows(Context ctx) {
		var id = ctx.pathParam("id");
		var r = service.getTotalFlows(id);
		Http.respond(ctx, r);
	}

	void getTotalFlowValueOf(Context ctx) {
		var id = ctx.pathParam("id");
		var enviFlow = EnviFlowId.parse(ctx.pathParam("envi-flow"));
		var r = service.getTotalFlowValueOf(id, enviFlow);
		Http.respond(ctx, r);
	}

	void getDirectFlowValuesOf(Context ctx) {
		var id = ctx.pathParam("id");
		var enviFlow = EnviFlowId.parse(ctx.pathParam("envi-flow"));
		var r = service.getDirectFlowValuesOf(id, enviFlow);
		Http.respond(ctx, r);
	}

	void getTotalFlowValuesOf(Context ctx) {
		var id = ctx.pathParam("id");
		var enviFlow = EnviFlowId.parse(ctx.pathParam("envi-flow"));
		var r = service.getTotalFlowValuesOf(id, enviFlow);
		Http.respond(ctx, r);
	}

	void getDirectFlowsOf(Context ctx) {
		var id = ctx.pathParam("id");
		var techFlow = TechFlowId.parse(ctx.pathParam("tech-flow"));
		var r = service.getDirectFlowsOf(id, techFlow);
		Http.respond(ctx, r);
	}

	void getDirectFlowOf(Context ctx) {
		var id = ctx.pathParam("id");
		var enviFlow = EnviFlowId.parse(ctx.pathParam("envi-flow"));
		var techFlow = TechFlowId.parse(ctx.pathParam("tech-flow"));
		var r = service.getDirectFlowOf(id, enviFlow, techFlow);
		Http.respond(ctx, r);
	}

	void getTotalFlowsOfOne(Context ctx) {
		var id = ctx.pathParam("id");
		var techFlow = TechFlowId.parse(ctx.pathParam("tech-flow"));
		var r = service.getTotalFlowsOfOne(id, techFlow);
		Http.respond(ctx, r);
	}

	void getTotalFlowOfOne(Context ctx) {
		var id = ctx.pathParam("id");
		var enviFlow = EnviFlowId.parse(ctx.pathParam("envi-flow"));
		var techFlow = TechFlowId.parse(ctx.pathParam("tech-flow"));
		var r = service.getTotalFlowOfOne(id, enviFlow, techFlow);
		Http.respond(ctx, r);
	}

	void getTotalFlowsOf(Context ctx) {
		var id = ctx.pathParam("id");
		var techFlow = TechFlowId.parse(ctx.pathParam("tech-flow"));
		var r = service.getTotalFlowsOf(id, techFlow);
		Http.respond(ctx, r);
	}

	void getTotalFlowOf(Context ctx) {
		var id = ctx.pathParam("id");
		var enviFlow = EnviFlowId.parse(ctx.pathParam("envi-flow"));
		var techFlow = TechFlowId.parse(ctx.pathParam("tech-flow"));
		var r = service.getTotalFlowOf(id, enviFlow, techFlow);
		Http.respond(ctx, r);
	}

	// endregion

	void getTotalImpacts(Context ctx) {
		var id = ctx.pathParam("id");
		var r = service.getTotalImpacts(id);
		Http.respond(ctx, r);
	}
}
