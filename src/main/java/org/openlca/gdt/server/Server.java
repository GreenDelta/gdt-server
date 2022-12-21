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
				server.setConnectors(new Connector[] {connector});
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
			app.delete("/data/{type-path}/{id}", data::delete);
		}

		// calculation & dispose
		app.post("/results/calculate", results::calculate);
		app.post("/results/{id}/dispose", results::dispose);
		app.delete("/results/{id}", results::dispose);
		app.get("/results/{id}/state", results::getState);

		// result queries
		app.get("/result/{id}/tech-flows", results::getTechFlows);
		app.get("/result/{id}/envi-flows", results::getEnviFlows);
		app.get("/result/{id}/impact-categories", results::getImpactCategories);

		app.get("/result/{id}/total-requirements", results::getTotalRequirements);
		app.get("/result/{id}/total-requirements-of/{tech-flow}", results::getTotalRequirementsOf);

		// region: flow results
		app.get("/result/{id}/total-flows", results::getTotalFlows);
		app.get("/result/{id}/total-flow-value-of/{envi-flow}", results::getTotalFlowValueOf);
		app.get("/result/{id}/direct-flow-values-of/{envi-flow}", results::getDirectFlowValuesOf);
		app.get("/result/{id}/total-flow-values-of/{envi-flow}", results::getTotalFlowValuesOf);
		app.get("/result/{id}/direct-flows-of/{tech-flow}", results::getDirectFlowsOf);
		app.get("/result/{id}/direct-flow-of/{envi-flow}/{tech-flow}", results::getDirectFlowOf);
		app.get("/result/{id}/total-flows-of-one/{tech-flow}", results::getTotalFlowsOfOne);
		app.get("/result/{id}/total-flow-of-one/{envi-flow}/{tech-flow}", results::getTotalFlowOfOne);
		app.get("/result/{id}/total-flows-of/{tech-flow}", results::getTotalFlowsOf);
		app.get("/result/{id}/total-flow-of/{envi-flow}/{tech-flow}", results::getTotalFlowOf);
		// endregion

		// region: impact results
		app.get("/results/{id}/total-impacts", results::getTotalImpacts);
		app.get("/results/{id}/total-impacts/normalized", results::getNormalizedImpacts);
		app.get("/results/{id}/total-impacts/weighted", results::getWeightedImpacts);
		app.get("/result/{id}/total-impact-value-of/{impact-category}", results::getTotalImpactValueOf);
		app.get("/result/{id}/direct-impact-values-of/{impact-category}", results::getDirectImpactValuesOf);
		app.get("/result/{id}/total-impact-values-of/{impact-category}", results::getTotalImpactValuesOf);
		app.get("/result/{id}/direct-impacts-of/{tech-flow}", results::getDirectImpactsOf);
		app.get("/result/{id}/direct-impact-of/{impact-category}/{tech-flow}", results::getDirectImpactOf);
		app.get("/result/{id}/total-impacts-of-one/{tech-flow}", results::getTotalImpactsOfOne);
		app.get("/result/{id}/total-impact-of-one/{impact-category}/{tech-flow}", results::getTotalImpactOfOne);
		app.get("/result/{id}/total-impacts-of/{tech-flow}", results::getTotalImpactsOf);
		app.get("/result/{id}/total-impact-of/{impact-category}/{tech-flow}", results::getTotalImpactOf);
		app.get("/result/{id}/impact-factors-of/{impact-category}", results::getImpactFactorsOf);
		app.get("/result/{id}/impact-factor-of/{impact-category}/{envi-flow}", results::getImpactFactorOf);
		app.get("/result/{id}/flow-impacts-of-one/{envi-flow}", results::getFlowImpactsOfOne);
		app.get("/result/{id}/flow-impacts-of/{envi-flow}", results::getFlowImpactsOf);
		app.get("/result/{id}/flow-impact-of/{impact-category}/{envi-flow}", results::getFlowImpactOf);
		app.get("/result/{id}/flow-impact-values-of/{impact-category}", results::getFlowImpactValuesOf);
		// endregion

		// region: cost results
		app.get("/result/{id}/total-costs", results::getTotalCosts);
		app.get("/result/{id}/direct-cost-values", results::getDirectCostValues);
		app.get("/result/{id}/total-cost-values", results::getTotalCostValues);
		app.get("/result/{id}/direct-costs-of/{tech-flow}", results::getDirectCostsOf);
		app.get("/result/{id}/total-costs-of-one/{tech-flow}", results::getTotalCostsOfOne);
		app.get("/result/{id}/total-costs-of/{tech-flow}", results::getTotalCostsOf);
		// endregion

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
