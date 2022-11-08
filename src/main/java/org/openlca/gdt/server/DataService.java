package org.openlca.gdt.server;

import com.google.gson.JsonArray;
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
import org.openlca.jsonld.output.JsonRefs;
import org.openlca.util.Strings;

import java.util.Objects;
import java.util.UUID;

class DataService {

	private final IDatabase db;

	DataService(IDatabase db) {
		this.db = db;
	}

	void getInfos(Context ctx) {
		var type = DataRequest.resolveType(ctx);
		if (type == null)
			return;
		var descriptors = db.getDescriptors(type);
		var refs = JsonRefs.of(db);
		var array = new JsonArray();
		for (var d : descriptors) {
			var ref = refs.asRef(d);
			array.add(ref);
		}
		Http.sendOk(ctx, array);
	}

	void get(Context ctx) {
		var ref = DataRequest.resolveEntity(ctx);
		if (ref == null)
			return;

		var entity = db.get(ref.type(), ref.id());
		if (entity == null) {
			Http.sendNotFound(ctx, "No dataset found for the given ID");
			return;
		}
		var json = new JsonExport(db, new MemStore())
				.withReferences(false)
				.getWriter(entity)
				.write(entity);
		if (json != null) {
			Http.sendOk(ctx, json);
		} else {
			Http.sendServerError(ctx, "Failed to convert: " + entity);
		}
	}

	void getInfo(Context ctx) {
		var ref = DataRequest.resolveEntity(ctx);
		if (ref == null)
			return;
		var info = db.getDescriptor(ref.type(), ref.id());
		if (info == null) {
			Http.sendNotFound(ctx, "No dataset found for the given ID");
			return;
		}
		var refs = JsonRefs.of(db);
		Http.sendOk(ctx, refs.asRef(info));
	}

	/**
	 * Creates or updates a dataset.
	 */
	<T extends RootEntity> void put(Context ctx) {

		Class<T> type = DataRequest.resolveType(ctx);
		if (type == null)
			return;
		EntityReader<T> reader = readerOf(type);
		if (reader == null) {
			Http.sendServerError(ctx,
					"Does not know how to read instances of " + type);
			return;
		}

		var json = Http.readBodyOf(ctx);
		if (json == null)
			return;

		var id = Json.getString(json, "@id");
		T entity = Strings.notEmpty(id)
				? db.get(type, id)
				: null;

		if (entity != null) {
			reader.update(entity, json);
			db.update(entity);
		} else {

			// add an ID, if not provided
			if (Strings.nullOrEmpty(id)) {
				Json.put(json, "@id", UUID.randomUUID().toString());
			}

			entity = reader.read(json);
			if (entity == null) {
				Http.sendBadRequest(ctx, "failed to read object");
				return;
			}
			db.insert(entity);
		}

		Http.sendOk(ctx, Json.asRef(entity));
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
