package in.stonecolddev.juke.page;

import com.github.slugify.Slugify;
import in.stonecolddev.juke.data.storage.tree.TreeRecord;
import io.soabase.recordbuilder.core.RecordBuilder;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;

import static org.apache.commons.codec.digest.MurmurHash3.hash32x86;

@RecordBuilder
public record PageRecord(
    Integer id,
    Integer author,
    String title,
    String slug,
    String body,
    Optional<Integer> parent,
    Set<TreeRecord> children,
    Set<Integer> ancestors,
    List<Integer> path,
    Integer depth,
    Boolean approved,
    OffsetDateTime createdOn,
    OffsetDateTime publishedOn) implements TreeRecord, PageRecordBuilder.With {

  public PageRecord {
    children = Optional.ofNullable(children).orElseGet(HashSet::new);
  }

  public void addChild(TreeRecord child) {
    this.children.add(child);
  }

  public PageRecord reparent(TreeRecord parent) {
    return this.withParent(Optional.of(parent.id()));
  }

  public String slug() {
    return Optional.ofNullable(slug).orElseGet(this::toSlug);
  }

  public Map<String, ?> valueMap() {
    Map<String, Object> valueMap = new HashMap<>(Map.of(
        "author", author,
        "title", title,
        "slug", toSlug(), //slug,
        "body", body,
        "approved", approved,
        "published_on", publishedOn,
        "created_on", Optional.ofNullable(createdOn).orElseGet(OffsetDateTime::now)
    ));
    parent.ifPresent(integer -> valueMap.put("parent", integer));
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