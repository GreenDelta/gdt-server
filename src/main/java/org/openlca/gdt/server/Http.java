package org.openlca.gdt.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.javalin.http.Context;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

class Http {

	static void sendOk(Context ctx, JsonElement json) {
		ctx.status(200);
		ctx.contentType("application/json");
		ctx.result(new Gson().toJson(json));
	}

	static void sendBadRequest(Context ctx, String message) {
		ctx.status(400);
		ctx.result(message);
	}

	static void sendNotFound(Context ctx, String message) {
		ctx.status(404);
		ctx.result(message);
	}

	static void sendServerError(Context ctx, String message) {
		ctx.status(500);
		ctx.result(message);
	}

	/**
	 * Reads the body of the given request as Json object. Returns {@code null}
	 * if it failed to read the body as Json object and writes an error as
	 * response in this case.
	 */
	static JsonObject readBodyOf(Context ctx) {
		try (var stream = ctx.bodyAsInputStream();
				 var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
			return new Gson().fromJson(reader, JsonObject.class);
		} catch (Exception e) {
			Http.sendBadRequest(ctx, "Unexpected request body");
			return null;
		}
	}

}
