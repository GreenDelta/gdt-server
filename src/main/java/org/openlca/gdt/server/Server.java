package org.openlca.gdt.server;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.bundled.CorsPluginConfig;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.ServerConnector;
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
			c.jetty.server(() -> {
				var server = new org.eclipse.jetty.server.Server();
				var httpConfig = new HttpConfiguration();
				httpConfig.setRequestHeaderSize(16384);
				httpConfig.setResponseHeaderSize(16384);
				var connector = new ServerConnector(
						server, new HttpConnectionFactory(httpConfig));
				connector.setPort(config.port());
				server.setConnectors(new Connector[]{connector});
				return server;
			});
		}).start();

		app.get("/api/version", Version::get);

		// get data
		app.get("/data/providers", data::getProviders);
		app.get("/data/providers/{id}", data::getProvidersOfFlow);
		app.get("/data/{type-path}", data::getDescriptors);
		app.get("/data/{type-path}/all", data::getAll);
		app.get("/data/{type-path}/name/<name>", data::getForName);
		app.get("/data/{type-path}/{id}", data::get);
		app.get("/data/{type-path}/{id}/info", data::getDescriptor);
		app.get("/data/{type-path}/{id}/parameters", data::getParameters);

		// put & delete data
		if (!config.isReadonly()) {
			app.put("/data/{type-path}", data::put);
			app.post("/data/create-system", data::createSystem);
			app.delete("/data/{type-path}/{id}", data::delete);
		}

		// calculation & dispose
		app.post("/result/calculate", results::calculate);
		app.post("/result/simulate", results::simulate);
		app.post("/result/{id}/simulate/next", results::simulateNext);
		app.post("/result/{id}/dispose", results::dispose);
		app.delete("/result/{id}", results::dispose);
		app.get("/result/{id}/state", results::getState);

		// result queries
		app.get("/result/{id}/demand", results::getDemand);
		app.get("/result/{id}/tech-flows", results::getTechFlows);
		app.get("/result/{id}/envi-flows", results::getEnviFlows);
		app.get("/result/{id}/impact-categories", results::getImpactCategories);

		// region: tech. flows
		app.get("/result/{id}/scaling-factors", results::getScalingFactors);
		app.get("/result/{id}/total-requirements", results::getTotalRequirements);
		app.get("/result/{id}/total-requirements-of/{tech-flow}", results::getTotalRequirementsOf);
		app.get("/result/{id}/scaled-tech-flows-of/{tech-flow}", results::getScaledTechFlowsOf);
		app.get("/result/{id}/unscaled-tech-flows-of/{tech-flow}", results::getUnscaledTechFlowsOf);
		// endregion

		// region: flow results
		app.get("/result/{id}/total-flows", results::getTotalFlows);
		app.get("/result/{id}/total-flow-value-of/{envi-flow}", results::getTotalFlowValueOf);
		app.get("/result/{id}/flow-contributions-of/{envi-flow}", results::getFlowContributionsOf);
		app.get("/result/{id}/direct-interventions-of/{tech-flow}", results::getDirectInterventionsOf);
		app.get("/result/{id}/direct-intervention-of/{envi-flow}/{tech-flow}", results::getDirectInterventionOf);
		app.get("/result/{id}/flow-intensities-of/{tech-flow}", results::getFlowIntensitiesOf);
		app.get("/result/{id}/flow-intensity-of/{envi-flow}/{tech-flow}", results::getFlowIntensityOf);
		app.get("/result/{id}/total-interventions-of/{tech-flow}", results::getTotalInterventionsOf);
		app.get("/result/{id}/total-intervention-of/{envi-flow}/{tech-flow}", results::getTotalInterventionOf);
		app.post("/result/{id}/upstream-interventions-of/{envi-flow}", results::getUpstreamInterventionsOf);
		// endregion

		// region: impact results
		app.get("/result/{id}/total-impacts", results::getTotalImpacts);
		app.get("/result/{id}/total-impacts/normalized", results::getNormalizedImpacts);
		app.get("/result/{id}/total-impacts/weighted", results::getWeightedImpacts);
		app.get("/result/{id}/total-impact-value-of/{impact-category}", results::getTotalImpactValueOf);
		app.get("/result/{id}/impact-contributions-of/{impact-category}", results::getImpactContributionsOf);
		app.get("/result/{id}/direct-impacts-of/{tech-flow}", results::getDirectImpactsOf);
		app.get("/result/{id}/direct-impact-of/{impact-category}/{tech-flow}", results::getDirectImpactOf);
		app.get("/result/{id}/impact-intensities-of/{tech-flow}", results::getImpactIntensitiesOf);
		app.get("/result/{id}/impact-intensity-of/{impact-category}/{tech-flow}", results::getImpactIntensityOf);
		app.get("/result/{id}/total-impacts-of/{tech-flow}", results::getTotalImpactsOf);
		app.get("/result/{id}/total-impact-of/{impact-category}/{tech-flow}", results::getTotalImpactOf);
		app.get("/result/{id}/impact-factors-of/{impact-category}", results::getImpactFactorsOf);
		app.get("/result/{id}/impact-factor-of/{impact-category}/{envi-flow}", results::getImpactFactorOf);
		app.get("/result/{id}/flow-impacts-of/{impact-category}", results::getFlowImpactsOf);
		app.get("/result/{id}/flow-impact-of/{impact-category}/{envi-flow}", results::getFlowImpactOf);
		app.post("/result/{id}/upstream-impacts-of/{impact-category}", results::getUpstreamImpactsOf);
		// endregion

		// region: cost results
		app.get("/result/{id}/total-costs", results::getTotalCosts);
		app.get("/result/{id}/cost-contributions", results::getCostContributions);
		app.get("/result/{id}/total-cost-values", results::getTotalCostValues);
		app.get("/result/{id}/direct-costs-of/{tech-flow}", results::getDirectCostsOf);
		app.get("/result/{id}/cost-intensities-of/{tech-flow}", results::getCostIntensitiesOf);
		app.get("/result/{id}/total-costs-of/{tech-flow}", results::getTotalCostsOf);
		app.post("/result/{id}/upstream-costs-of", results::getUpstreamCostsOf);
		// endregion

		// Sankey graphs
		app.post("/result/{id}/sankey", results::getSankeyGraphOf);

		// TODO: deprecated `results` routes
		// these routes only exists for backwards compatibility with older API
		// versions and should not be used anymore
		app.post("/results/calculate", results::calculate);
		app.post("/results/{id}/dispose", results::dispose);
		app.delete("/results/{id}", results::dispose);
		app.get("/results/{id}/state", results::getState);
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
