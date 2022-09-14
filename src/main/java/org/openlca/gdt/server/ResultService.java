package org.openlca.gdt.server;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.javalin.http.Context;
import org.openlca.core.database.IDatabase;
import org.openlca.core.io.DbEntityResolver;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.FlowValue;
import org.openlca.core.results.ImpactValue;
import org.openlca.core.results.LcaResult;
import org.openlca.core.results.TagResult;
import org.openlca.core.services.CalculationQueue;
import org.openlca.core.services.JsonCalculationSetup;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.output.DbRefs;

class ResultService {

	private final IDatabase db;
	private final CalculationQueue queue;

	ResultService(Args args) {
		this.db = args.db();
		this.queue = new CalculationQueue(db, 2)
				.withLibraryDir(args.dataDir().getLibraryDir());
	}

	void calculate(Context ctx) {
		var json = Http.readBodyOf(ctx);
		if (json == null)
			return;
		var r = JsonCalculationSetup.readFrom(json, DbEntityResolver.of(db));
		if (r.hasError()) {
			Http.sendBadRequest(ctx, r.error());
			return;
		}
		var setup = r.setup();
		var state = queue.schedule(setup);
		Http.sendOk(ctx, encodeState(state));
	}

	void getState(Context ctx) {
		var id = ctx.pathParam("id");
		var state = queue.get(id);
		if (state.isEmpty()) {
			Http.sendNotFound(ctx, "no result state for: " + id);
			return;
		}
		Http.sendOk(ctx, encodeState(state));
	}

	void dispose(Context ctx) {
		var id = ctx.pathParam("id");
		queue.dispose(id);
	}

	void getTotalImpacts(Context ctx) {
		var result = resultOf(ctx);
		if (result == null)
			return;
		if (!result.hasImpacts()) {
			Http.sendOk(ctx, new JsonArray());
			return;
		}
		var impacts = result.getTotalImpactResults();
		var refs = DbRefs.of(db);
		var array = encodeArray(impacts, i -> encodeImpact(i, refs));
		Http.sendOk(ctx, array);
	}

	LcaResult resultOf(Context ctx) {
		var id = ctx.pathParam("id");
		var state = queue.get(id);
		if (state.isEmpty()) {
			Http.sendNotFound(ctx, "no result for: " + id);
			return null;
		}
		if (state.isError()) {
			Http.sendBadRequest(ctx, "no result: " + state.error());
			return null;
		}
		if (state.isReady())
			return state.result();
		Http.sendBadRequest(ctx, "no result yet");
		return null;
	}

	private JsonObject encodeState(CalculationQueue.State state) {
		var obj = new JsonObject();
		if (state == null || state.isEmpty()) {
			Json.put(obj, "error", "does not exist");
			return obj;
		}
		Json.put(obj, "@id", state.id());
		if (state.isError()) {
			Json.put(obj, "error", state.error());
			return obj;
		}
		Json.put(obj, "isReady", state.isReady());
		Json.put(obj, "isScheduled", state.isScheduled());
		Json.put(obj, "time", state.time());
		return obj;
	}


	private <T> JsonArray encodeArray(
			Iterable<T> items, Function<T, JsonObject> fn) {
		var array = new JsonArray();
		for (var next : items) {
			var json = fn.apply(next);
			if (json != null) {
				array.add(json);
			}
		}
		return array;
	}

	private <T, S> JsonArray encode(
			Collection<T> l1, Collection<S> l2, BiFunction<T, Collection<S>, JsonObject> encoder) {
		JsonArray array = new JsonArray();
		for (T t : l1) {
			JsonObject item = encoder.apply(t, l2);
			if (item != null) {
				array.add(item);
			}
		}
		return array;
	}

	private JsonObject encode(FlowValue r) {
		if (r == null)
			return null;
		JsonObject obj = new JsonObject();
		obj.addProperty("@type", "FlowResult");
		obj.add("flow", Json.asRef(r.flow()));
		obj.addProperty("input", r.isInput());
		obj.addProperty("value", r.value());
		return obj;
	}

	private JsonObject encodeImpact(ImpactValue r, DbRefs refs) {
		if (r == null)
			return null;
		JsonObject obj = new JsonObject();
		obj.addProperty("@type", "ImpactResult");
		obj.add("impactCategory", refs.asRef(r.impact()));
		obj.addProperty("value", r.value());
		return obj;
	}

	private JsonObject encode(ImpactDescriptor impact, Collection<TagResult> tagResults) {
		if (tagResults == null)
			return null;
		JsonObject obj = new JsonObject();
		obj.addProperty("@type", "TagResult");
		obj.add("impactCategory", Json.asRef(impact));
		JsonArray array = new JsonArray();
		for (var r : tagResults) {
			JsonObject tro = new JsonObject();
			tro.addProperty("tag", r.tag());
			tro.addProperty("value", r.impactResultOf(impact).value());
			array.add(tro);
		}
		obj.add("tags", array);
		return obj;
	}
}
