package org.openlca.gdt.server;

import io.javalin.http.Context;
import org.openlca.core.database.IDatabase;
import org.openlca.core.services.JsonDataService;
import org.openlca.jsonld.Json;

class DataService {

	private final IDatabase db;
	private final JsonDataService service;

	DataService(IDatabase db) {
		this.db = db;
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

	void getParameters(Context ctx) {
		var ref = DataRequest.resolveEntity(ctx);
		if (ref == null)
			return;
		var resp = service.getParametersOf(ref.type(), ref.id());
		Http.respond(ctx, resp);
	}

	void delete(Context ctx) {
		var ref = DataRequest.resolveEntity(ctx);
		if (ref == null)
			return;


		var entity = db.get(ref.type(), ref.id());
		if (entity == null) {
			Http.sendNotFound(ctx, "No dataset found for the given ID");
			return;
		}
		db.delete(entity);
		Http.sendOk(ctx, Json.asRef(entity));
	}

}
