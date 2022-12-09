package org.openlca.gdt.server;

import io.javalin.http.Context;
import org.openlca.core.database.IDatabase;
import org.openlca.core.services.JsonDataService;

class DataService {

	private final JsonDataService service;

	DataService(IDatabase db) {
		this.service = new JsonDataService(db);
	}

	void getDescriptors(Context ctx) {
		var type = DataRequest.resolveType(ctx);
		if (type == null)
			return;
		var resp = service.getDescriptors(type);
		Http.respond(ctx, resp);
	}

	void get(Context ctx) {
		var ref = DataRequest.resolveEntity(ctx);
		if (ref == null)
			return;
		var resp = service.get(ref.type(), ref.id());
		Http.respond(ctx, resp);
	}

	void getDescriptor(Context ctx) {
		var ref = DataRequest.resolveEntity(ctx);
		if (ref == null)
			return;
		var resp = service.getDescriptor(ref.type(), ref.id());
		Http.respond(ctx, resp);
	}

	void put(Context ctx) {
		var type = DataRequest.resolveType(ctx);
		if (type == null)
			return;
		var json = Http.readBodyOf(ctx);
		if (json == null)
			return;
		if (!json.isJsonObject()) {
			Http.sendBadRequest(ctx, "not an object provided");
			return;
		}
		var resp = service.put(type, json.getAsJsonObject());
		Http.respond(ctx, resp);
	}

	void delete(Context ctx) {
		var ref = DataRequest.resolveEntity(ctx);
		if (ref == null)
			return;
		var resp = service.delete(ref.type(), ref.id());
		Http.respond(ctx, resp);
	}

	void getParameters(Context ctx) {
		var ref = DataRequest.resolveEntity(ctx);
		if (ref == null)
			return;
		var resp = service.getParametersOf(ref.type(), ref.id());
		Http.respond(ctx, resp);
	}

	void getProviders(Context ctx) {
		var resp = service.getProviders();
		Http.respond(ctx, resp);
	}

	void getProvidersOfFlow(Context ctx) {
		var id = DataRequest.resolveId(ctx);
		if (id == null)
			return;
		var resp = service.getProvidersOfFlow(id);
		Http.respond(ctx, resp);
	}
}
