package com.qoder.cloudagents.core.internal;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/** Shared cursor fetching; merges request overrides only on the first page. */
public final class CursorRequests {
  private CursorRequests() {}

  public interface PageFactory<P> {
    P create(
        JsonValue value,
        String requestId,
        Map<String, String> query,
        Set<String> seen,
        BiFunction<Map<String, String>, Set<String>, CompletableFuture<P>> fetcher);
  }

  public static <P> CompletableFuture<ApiResponse<P>> list(
      ClientEngine engine,
      String[] path,
      Map<String, String> params,
      RequestOptions options,
      PageFactory<P> factory) {
    if (params == null || options == null)
      throw new ConfigurationException("List parameters and options must not be null");
    Map<String, String> query = new LinkedHashMap<>(params);
    query.putAll(options.query());
    if ((query.containsKey("after_id") ? 1 : 0)
            + (query.containsKey("before_id") ? 1 : 0)
            + (query.containsKey("page") ? 1 : 0)
        > 1) throw new ConfigurationException("page, afterId and beforeId are mutually exclusive");
    if (query.containsKey("scope_id")
        && (query.containsKey("after_id")
            || query.containsKey("before_id")
            || query.containsKey("page")))
      throw new ConfigurationException("scopeId cannot be combined with a cursor");
    Set<String> seen = new HashSet<>();
    if (query.containsKey("page")) seen.add(query.get("page"));
    if (query.containsKey("after_id")) seen.add(query.get("after_id"));
    if (query.containsKey("before_id")) seen.add(query.get("before_id"));
    return fetch(
        engine, path.clone(), query, options.toBuilder().clearQuery().build(), seen, factory);
  }

  private static <P> CompletableFuture<ApiResponse<P>> fetch(
      ClientEngine engine,
      String[] path,
      Map<String, String> query,
      RequestOptions options,
      Set<String> seen,
      PageFactory<P> factory) {
    return Futures.map(
        engine.get(path, query, options),
        response ->
            response.map(
                value ->
                    factory.create(
                        value,
                        response.requestId(),
                        query,
                        seen,
                        (nextQuery, nextSeen) ->
                            Futures.map(
                                fetch(engine, path, nextQuery, options, nextSeen, factory),
                                ApiResponse::data))));
  }
}
