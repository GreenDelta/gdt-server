package org.openlca.gdt.server;

import io.javalin.http.Context;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Epd;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.Result;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.util.Strings;

class DataRequest {

	@SuppressWarnings("unchecked")
	static <T extends RootEntity> EntityId<T> resolveEntity(Context ctx) {
		var type = resolveType(ctx);
		if (type == null)
			return null;

		var id = ctx.pathParam("id");
		if (Strings.nullOrEmpty(id)) {
			Http.sendBadRequest(ctx, "No ID provided");
			return null;
		}
		return (EntityId<T>) new EntityId<>(type, id);
	}

	/**
	 * Returns the type for the given path segment. Writes an error to the context
	 * if the type could not be resolved and returns {@code null} in this case.
	 */
	@SuppressWarnings("unchecked")
	static <T extends RootEntity> Class<T> resolveType(Context ctx) {
		var path = ctx.pathParam("type-path");
		if (Strings.nullOrEmpty(path)) {
			Http.sendBadRequest(ctx, "No model path provided");
			return null;
		}

		var clazz = switch (path) {
			case "actor", "actors" -> Actor.class;
			case "category", "categories" -> Category.class;
			case "currency", "currencies" -> Currency.class;
			case "dq-system", "dq-systems" -> DQSystem.class;
			case "epd", "epds" -> Epd.class;
			case "flow", "flows" -> Flow.class;
			case "flow-property", "flow-properties" -> FlowProperty.class;
			case "impact-category", "impact-categories" -> ImpactCategory.class;
			case "impact-method", "method",
					"impact-methods", "methods" -> ImpactMethod.class;
			case "location", "locations" -> Location.class;
			case "parameter", "parameters" -> Parameter.class;
			case "process", "processes" -> Process.class;
			case "product-system", "model",
					"product-systems", "models" -> ProductSystem.class;
			case "project", "projects" -> Project.class;
			case "result", "results" -> Result.class;
			case "social-indicator", "social-indicators" -> SocialIndicator.class;
			case "source", "sources" -> Source.class;
			case "unit-group", "unit-groups" -> UnitGroup.class;
			default -> null;
		};

		if (clazz != null)
			return (Class<T>) clazz;
		Http.sendBadRequest(ctx, "Unknown request path: " + path);
		return null;
	}

	record EntityId<T extends RootEntity>(Class<T> type, String id) {
	}

}
