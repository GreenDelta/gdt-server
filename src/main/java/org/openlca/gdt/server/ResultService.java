package org.openlca.gdt.server;

import io.javalin.http.Context;
import org.openlca.core.services.EnviFlowId;
import org.openlca.core.services.JsonResultService;
import org.openlca.core.services.ServerConfig;
import org.openlca.core.services.TechFlowId;

class ResultService {

	private final JsonResultService service;

	ResultService(ServerConfig config) {
		this.service = JsonResultService.of(config);
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

	void simulate(Context ctx) {
		var json = Http.readBodyOf(ctx);
		if (json == null)
			return;
		if (!json.isJsonObject()) {
			Http.sendBadRequest(ctx, "no valid calculation setup provided");
			return;
		}
		var r = service.simulate(json.getAsJsonObject());
		Http.respond(ctx, r);
	}

	void simulateNext(Context ctx) {
		var id = ctx.pathParam("id");
		var r = service.nextSimulationOf(id);
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

	void getDemand(Context ctx) {
		var id = ctx.pathParam("id");
		var r = service.getDemand(id);
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

	// region: tech-flows

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

	void getScalingFactors(Context ctx) {
		var id = ctx.pathParam("id");
		var r = service.getScalingFactors(id);
		Http.respond(ctx, r);
	}

	void getScaledTechFlowsOf(Context ctx) {
		var id = ctx.pathParam("id");
		var techFlow = TechFlowId.parse(ctx.pathParam("tech-flow"));
		var r = service.getScaledTechFlowsOf(id, techFlow);
		Http.respond(ctx, r);
	}

	void getUnscaledTechFlowsOf(Context ctx) {
		var id = ctx.pathParam("id");
		var techFlow = TechFlowId.parse(ctx.pathParam("tech-flow"));
		var r = service.getUnscaledTechFlowsOf(id, techFlow);
		Http.respond(ctx, r);
	}

	// endregion

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

	void getFlowContributionsOf(Context ctx) {
		var id = ctx.pathParam("id");
		var enviFlow = EnviFlowId.parse(ctx.pathParam("envi-flow"));
		var r = service.getFlowContributionsOf(id, enviFlow);
		Http.respond(ctx, r);
	}

	void getDirectInterventionsOf(Context ctx) {
		var id = ctx.pathParam("id");
		var techFlow = TechFlowId.parse(ctx.pathParam("tech-flow"));
		var r = service.getDirectInterventionsOf(id, techFlow);
		Http.respond(ctx, r);
	}

	void getDirectInterventionOf(Context ctx) {
		var id = ctx.pathParam("id");
		var enviFlow = EnviFlowId.parse(ctx.pathParam("envi-flow"));
		var techFlow = TechFlowId.parse(ctx.pathParam("tech-flow"));
		var r = service.getDirectInterventionOf(id, enviFlow, techFlow);
		Http.respond(ctx, r);
	}

	void getFlowIntensitiesOf(Context ctx) {
		var id = ctx.pathParam("id");
		var techFlow = TechFlowId.parse(ctx.pathParam("tech-flow"));
		var r = service.getFlowIntensitiesOf(id, techFlow);
		Http.respond(ctx, r);
	}

	void getFlowIntensityOf(Context ctx) {
		var id = ctx.pathParam("id");
		var enviFlow = EnviFlowId.parse(ctx.pathParam("envi-flow"));
		var techFlow = TechFlowId.parse(ctx.pathParam("tech-flow"));
		var r = service.getFlowIntensityOf(id, enviFlow, techFlow);
		Http.respond(ctx, r);
	}

	void getTotalInterventionsOf(Context ctx) {
		var id = ctx.pathParam("id");
		var techFlow = TechFlowId.parse(ctx.pathParam("tech-flow"));
		var r = service.getTotalInterventionsOf(id, techFlow);
		Http.respond(ctx, r);
	}

	void getTotalInterventionOf(Context ctx) {
		var id = ctx.pathParam("id");
		var enviFlow = EnviFlowId.parse(ctx.pathParam("envi-flow"));
		var techFlow = TechFlowId.parse(ctx.pathParam("tech-flow"));
		var r = service.getTotalInterventionOf(id, enviFlow, techFlow);
		Http.respond(ctx, r);
	}

	// endregion

	// region: impact results

	void getTotalImpacts(Context ctx) {
		var id = ctx.pathParam("id");
		var r = service.getTotalImpacts(id);
		Http.respond(ctx, r);
	}

	void getNormalizedImpacts(Context ctx) {
		var id = ctx.pathParam("id");
		var r = service.getNormalizedImpacts(id);
		Http.respond(ctx, r);
	}

	void getWeightedImpacts(Context ctx) {
		var id = ctx.pathParam("id");
		var r = service.getWeightedImpacts(id);
		Http.respond(ctx, r);
	}

	void getTotalImpactValueOf(Context ctx) {
		var id = ctx.pathParam("id");
		var impactCategory = ctx.pathParam("impact-category");
		var r = service.getTotalImpactValueOf(id, impactCategory);
		Http.respond(ctx, r);
	}

	void getImpactContributionsOf(Context ctx) {
		var id = ctx.pathParam("id");
		var impactCategory = ctx.pathParam("impact-category");
		var r = service.getImpactContributionsOf(id, impactCategory);
		Http.respond(ctx, r);
	}

	void getDirectImpactsOf(Context ctx) {
		var id = ctx.pathParam("id");
		var techFlow = TechFlowId.parse(ctx.pathParam("tech-flow"));
		var r = service.getDirectImpactsOf(id, techFlow);
		Http.respond(ctx, r);
	}

	void getDirectImpactOf(Context ctx) {
		var id = ctx.pathParam("id");
		var impactCategory = ctx.pathParam("impact-category");
		var techFlow = TechFlowId.parse(ctx.pathParam("tech-flow"));
		var r = service.getDirectImpactOf(id, impactCategory, techFlow);
		Http.respond(ctx, r);
	}

	void getImpactIntensitiesOf(Context ctx) {
		var id = ctx.pathParam("id");
		var techFlow = TechFlowId.parse(ctx.pathParam("tech-flow"));
		var r = service.getImpactIntensitiesOf(id, techFlow);
		Http.respond(ctx, r);
	}

	void getImpactIntensityOf(Context ctx) {
		var id = ctx.pathParam("id");
		var impactCategory = ctx.pathParam("impact-category");
		var techFlow = TechFlowId.parse(ctx.pathParam("tech-flow"));
		var r = service.getImpactIntensityOf(id, impactCategory, techFlow);
		Http.respond(ctx, r);
	}

	void getTotalImpactsOf(Context ctx) {
		var id = ctx.pathParam("id");
		var techFlow = TechFlowId.parse(ctx.pathParam("tech-flow"));
		var r = service.getTotalImpactsOf(id, techFlow);
		Http.respond(ctx, r);
	}

	void getTotalImpactOf(Context ctx) {
		var id = ctx.pathParam("id");
		var impactCategory = ctx.pathParam("impact-category");
		var techFlow = TechFlowId.parse(ctx.pathParam("tech-flow"));
		var r = service.getTotalImpactOf(id, impactCategory, techFlow);
		Http.respond(ctx, r);
	}

	void getImpactFactorsOf(Context ctx) {
		var id = ctx.pathParam("id");
		var impactCategory = ctx.pathParam("impact-category");
		var r = service.getImpactFactorsOf(id, impactCategory);
		Http.respond(ctx, r);
	}

	void getImpactFactorOf(Context ctx) {
		var id = ctx.pathParam("id");
		var impactCategory = ctx.pathParam("impact-category");
		var enviFlow = EnviFlowId.parse(ctx.pathParam("envi-flow"));
		var r = service.getImpactFactorOf(id, impactCategory, enviFlow);
		Http.respond(ctx, r);
	}

	void getFlowImpactsOf(Context ctx) {
		var id = ctx.pathParam("id");
		var impactCategory = ctx.pathParam("impact-category");
		var r = service.getFlowImpactsOf(id, impactCategory);
		Http.respond(ctx, r);
	}

	void getFlowImpactOf(Context ctx) {
		var id = ctx.pathParam("id");
		var impactCategory = ctx.pathParam("impact-category");
		var enviFlow = EnviFlowId.parse(ctx.pathParam("envi-flow"));
		var r = service.getFlowImpactOf(id, impactCategory, enviFlow);
		Http.respond(ctx, r);
	}

	// endregion

	// region: cost results

	void getTotalCosts(Context ctx) {
		var id = ctx.pathParam("id");
		var r = service.getTotalCosts(id);
		Http.respond(ctx, r);
	}

	void getCostContributions(Context ctx) {
		var id = ctx.pathParam("id");
		var r = service.getCostContributions(id);
		Http.respond(ctx, r);
	}

	void getTotalCostValues(Context ctx) {
		var id = ctx.pathParam("id");
		var r = service.getTotalCostValues(id);
		Http.respond(ctx, r);
	}

	void getDirectCostsOf(Context ctx) {
		var id = ctx.pathParam("id");
		var techFlow = TechFlowId.parse(ctx.pathParam("tech-flow"));
		var r = service.getDirectCostsOf(id, techFlow);
		Http.respond(ctx, r);
	}

	void getCostIntensitiesOf(Context ctx) {
		var id = ctx.pathParam("id");
		var techFlow = TechFlowId.parse(ctx.pathParam("tech-flow"));
		var r = service.getCostIntensitiesOf(id, techFlow);
		Http.respond(ctx, r);
	}

	void getTotalCostsOf(Context ctx) {
		var id = ctx.pathParam("id");
		var techFlow = TechFlowId.parse(ctx.pathParam("tech-flow"));
		var r = service.getTotalCostsOf(id, techFlow);
		Http.respond(ctx, r);
	}

	// endregion

}
