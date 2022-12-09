package org.openlca.gdt.server;

import io.javalin.http.Context;
import org.openlca.core.database.IDatabase;
import org.openlca.core.io.DbEntityResolver;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Epd;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterizedEntity;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.Result;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.services.JsonDataService;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.MemStore;
import org.openlca.jsonld.input.ActorReader;
import org.openlca.jsonld.input.CurrencyReader;
import org.openlca.jsonld.input.DQSystemReader;
import org.openlca.jsonld.input.EntityReader;
import org.openlca.jsonld.input.EpdReader;
import org.openlca.jsonld.input.FlowPropertyReader;
import org.openlca.jsonld.input.FlowReader;
import org.openlca.jsonld.input.ImpactCategoryReader;
import org.openlca.jsonld.input.ImpactMethodReader;
import org.openlca.jsonld.input.LocationReader;
import org.openlca.jsonld.input.ParameterReader;
import org.openlca.jsonld.input.ProcessReader;
import org.openlca.jsonld.input.ProductSystemReader;
import org.openlca.jsonld.input.ProjectReader;
import org.openlca.jsonld.input.ResultReader;
import org.openlca.jsonld.input.SocialIndicatorReader;
import org.openlca.jsonld.input.SourceReader;
import org.openlca.jsonld.input.UnitGroupReader;
import org.openlca.jsonld.output.JsonExport;

import java.util.Objects;

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
		var obj = json.getAsJsonObject();
		if (Json.getString(obj, "@type") == null) {
			obj.addProperty("@type", type.getSimpleName());
		}

		var resp = service.put(obj);
		Http.respond(ctx, resp);
	}

	void getParameters(Context ctx) {
		var ref = DataRequest.resolveEntity(ctx);
		if (ref == null)
			return;

		var entity = db.get(ref.type(), ref.id());
		if (entity == null) {
			Http.sendNotFound(ctx, "Not found: " + ref);
			return;
		}

		if (entity instanceof ParameterizedEntity pe) {
			var exp = new JsonExport(db, new MemStore())
					.withReferences(false);
			var array = Parameters.of(exp, pe);
			Http.sendOk(ctx, array);
			return;
		}

		if (entity instanceof ProductSystem sys) {
			var array = Parameters.of(db, sys);
			Http.sendOk(ctx, array);
			return;
		}

		Http.sendBadRequest(ctx,
				"Parameter request not supported for: " + ref.type());
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

	@SuppressWarnings("unchecked")
	<T extends RootEntity> EntityReader<T> readerOf(Class<T> type) {
		var resolver = new DbEntityResolver(db);
		if (Objects.equals(type, Actor.class))
			return (EntityReader<T>) new ActorReader(resolver);
		if (Objects.equals(type, Currency.class))
			return (EntityReader<T>) new CurrencyReader(resolver);
		if (Objects.equals(type, DQSystem.class))
			return (EntityReader<T>) new DQSystemReader(resolver);
		if (Objects.equals(type, Epd.class))
			return (EntityReader<T>) new EpdReader(resolver);
		if (Objects.equals(type, Flow.class))
			return (EntityReader<T>) new FlowReader(resolver);
		if (Objects.equals(type, FlowProperty.class))
			return (EntityReader<T>) new FlowPropertyReader(resolver);
		if (Objects.equals(type, ImpactCategory.class))
			return (EntityReader<T>) new ImpactCategoryReader(resolver);
		if (Objects.equals(type, ImpactMethod.class))
			return (EntityReader<T>) new ImpactMethodReader(resolver);
		if (Objects.equals(type, Location.class))
			return (EntityReader<T>) new LocationReader(resolver);
		if (Objects.equals(type, Parameter.class))
			return (EntityReader<T>) new ParameterReader(resolver);
		if (Objects.equals(type, Process.class))
			return (EntityReader<T>) new ProcessReader(resolver);
		if (Objects.equals(type, ProductSystem.class))
			return (EntityReader<T>) new ProductSystemReader(resolver);
		if (Objects.equals(type, Project.class))
			return (EntityReader<T>) new ProjectReader(resolver);
		if (Objects.equals(type, Result.class))
			return (EntityReader<T>) new ResultReader(resolver);
		if (Objects.equals(type, SocialIndicator.class))
			return (EntityReader<T>) new SocialIndicatorReader(resolver);
		if (Objects.equals(type, Source.class))
			return (EntityReader<T>) new SourceReader(resolver);
		if (Objects.equals(type, UnitGroup.class))
			return (EntityReader<T>) new UnitGroupReader(resolver);
		return null;
	}
}
