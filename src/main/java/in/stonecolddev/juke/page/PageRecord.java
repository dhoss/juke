package in.stonecolddev.juke.page;

import com.github.slugify.Slugify;
import in.stonecolddev.juke.data.storage.tree.TreeRecord;
import in.stonecolddev.juke.user.JukeUser;
import io.soabase.recordbuilder.core.RecordBuilder;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.codec.digest.MurmurHash3.hash32x86;

@RecordBuilder
public record PageRecord(
    Integer id,
    Integer authorId,
    JukeUser author,
    String title,
    String slug,
    String body,
    Optional<Integer> parentId,
    Optional<TreeRecord> parent,
    Set<TreeRecord> children,
    List<Integer> ancestorIds,
    Set<TreeRecord> ancestors,
    List<Integer> path,
    Integer depth,
    Boolean approved,
    OffsetDateTime createdOn,
    OffsetDateTime publishedOn) implements TreeRecord, PageRecordBuilder.With {

  public PageRecord {
    children = Optional.ofNullable(children).orElseGet(HashSet::new);
    ancestorIds = Optional.ofNullable(ancestorIds).orElseGet(ArrayList::new);
  }

  public void addChild(TreeRecord child) {
    this.children.add(child);
  }

  public PageRecord reparent(TreeRecord parent) {
    return this.withParentId(Optional.of(parent.id()));
  }

  public String slug() {
    return Optional.ofNullable(slug).orElseGet(this::toSlug);
  }

  // TODO: memoize this
  public List<Integer> ancestorIds() {
    System.out.println("**** ANCESTORS FROM PATH " + path());
    return Optional.ofNullable(ancestorIds)
        .or(() -> Optional.ofNullable(path))
        .map(a ->
            a.stream()
                .peek(p -> System.out.println("**** ANCESTOR ID " + p))
                .filter(id -> !id.equals(id()))
                .collect(Collectors.toList()))
        .orElseGet(ArrayList::new);
  }

  public Map<String, ?> valueMap() {
    Map<String, Object> valueMap = new HashMap<>(Map.of(
        "author_id", authorId,
        "title", title,
        "slug", toSlug(), //slug,
        "body", body,
        "approved", approved,
        "published_on", publishedOn,
        "created_on", Optional.ofNullable(createdOn).orElseGet(OffsetDateTime::now)
    ));
    parentId.ifPresent(integer -> valueMap.put("parent_id", integer));
    return valueMap;
  }

  // TODO: tests
  public PageRecord withTitle(String title) {
    PageRecord updatedTitle = this.with(p -> p.title(title));
    return updatedTitle
        .withSlug(updatedTitle.toSlug());
  }

  // TODO: write tests to make sure this works properly when title is changed
  public String toSlug() {
    byte[] bytes = (title + createdOn).getBytes(StandardCharsets.UTF_8);
    return Slugify.builder()
        .build()
        .slugify(title +
            Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                    ByteBuffer.allocate(4)
                        .putInt(hash32x86(bytes, 0, bytes.length, 0))
                        .array()));
  }

}