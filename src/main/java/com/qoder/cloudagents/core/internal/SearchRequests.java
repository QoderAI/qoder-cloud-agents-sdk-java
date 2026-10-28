package com.qoder.cloudagents.core.internal;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** POST search pagination: only the opaque page token changes in the JSON body. */
public final class SearchRequests {
  private SearchRequests() {}

  public static <P> CompletableFuture<ApiResponse<P>> search(
      ClientEngine engine,
      String[] path,
      JsonValue body,
      Map<String, String> params,
      RequestOptions options,
      CursorRequests.PageFactory<P> factory) {
    if (body == null || options == null)
      throw new ConfigurationException("Search parameters and options must not be null");
    if (!body.get("identity_id").isMissing())
      throw new ConfigurationException("identityId belongs in the query, not the search body");
    Map<String, String> query = new LinkedHashMap<>(params);
    query.putAll(options.query());
    Map<String, String> cursor = new LinkedHashMap<>();
    Set<String> seen = new HashSet<>();
    JsonValue page = body.get("page");
    if (!page.isMissing() && !page.isNull()) {
      cursor.put("page", page.asString());
      seen.add(page.asString());
    }
    return fetch(
        engine,
        path.clone(),
        body,
        query,
        engine.withBeta(options, "search-2026-08-31").toBuilder().clearQuery().build(),
        cursor,
        seen,
        factory);
  }

  private static <P> CompletableFuture<ApiResponse<P>> fetch(
      ClientEngine engine,
      String[] path,
      JsonValue body,
      Map<String, String> query,
      RequestOptions options,
      Map<String, String> cursor,
      Set<String> seen,
      CursorRequests.PageFactory<P> factory) {
    Map<String, JsonValue> fields = new LinkedHashMap<>(body.asObject());
    if (cursor.containsKey("page")) fields.put("page", JsonValue.of(cursor.get("page")));
    return Futures.map(
        engine.request("POST", path, query, JsonValue.of(fields), true, options),
        response ->
            response.map(
                value ->
                    factory.create(
                        value,
                        response.requestId(),
                        cursor,
                        seen,
                        (nextCursor, nextSeen) ->
                            Futures.map(
                                fetch(
                                    engine,
                                    path,
                                    body,
                                    query,
                                    options,
                                    nextCursor,
                                    nextSeen,
                                    factory),
                                ApiResponse::data))));
  }
}
