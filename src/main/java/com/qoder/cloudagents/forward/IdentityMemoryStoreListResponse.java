package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.Futures;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/** A mount page; the existing boxed hasMore signature remains source and binary compatible. */
public final class IdentityMemoryStoreListResponse extends JsonModel
    implements Iterable<MemoryStoreMount> {
  private final Page page;

  public IdentityMemoryStoreListResponse(JsonValue value, String requestId) {
    this(value, requestId, Collections.emptyMap(), Collections.emptySet(), null);
  }

  IdentityMemoryStoreListResponse(
      JsonValue value,
      String requestId,
      Map<String, String> query,
      Set<String> seen,
      BiFunction<
              Map<String, String>, Set<String>, CompletableFuture<IdentityMemoryStoreListResponse>>
          fetcher) {
    super(value, requestId);
    page = new Page(value, requestId, query, seen, fetcher);
  }

  public List<MemoryStoreMount> data() {
    return page.data();
  }

  public Boolean hasMore() {
    return optional("has_more", JsonValue::asBoolean);
  }

  public String firstId() {
    return optionalString("first_id");
  }

  public String lastId() {
    return optionalString("last_id");
  }

  public boolean hasNextPage() {
    return page.hasNextPage();
  }

  public IdentityMemoryStoreListResponse getNextPage() {
    return Futures.await(getNextPageAsync());
  }

  public CompletableFuture<IdentityMemoryStoreListResponse> getNextPageAsync() {
    return Futures.map(page.getNextPageAsync(), Page::response);
  }

  public Iterable<IdentityMemoryStoreListResponse> iterPages() {
    return () ->
        new Iterator<IdentityMemoryStoreListResponse>() {
          private final Iterator<Page> pages = page.iterPages().iterator();

          public boolean hasNext() {
            return pages.hasNext();
          }

          public IdentityMemoryStoreListResponse next() {
            return pages.next().response();
          }
        };
  }

  @Override
  public Iterator<MemoryStoreMount> iterator() {
    return page.iterator();
  }

  private final class Page extends CursorPage<MemoryStoreMount, Page> {
    Page(
        JsonValue value,
        String requestId,
        Map<String, String> query,
        Set<String> seen,
        BiFunction<
                Map<String, String>,
                Set<String>,
                CompletableFuture<IdentityMemoryStoreListResponse>>
            fetcher) {
      super(
          value,
          requestId,
          query,
          seen,
          fetcher == null
              ? null
              : (q, s) -> Futures.map(fetcher.apply(q, s), result -> result.page),
          item -> new MemoryStoreMount(item, requestId));
    }

    @Override
    protected Page self() {
      return this;
    }

    IdentityMemoryStoreListResponse response() {
      return IdentityMemoryStoreListResponse.this;
    }
  }
}
