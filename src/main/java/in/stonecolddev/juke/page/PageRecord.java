package in.stonecolddev.juke.page;

import com.github.slugify.Slugify;
import in.stonecolddev.juke.data.storage.tree.TreeRecord;
import io.soabase.recordbuilder.core.RecordBuilder;

import java.time.OffsetDateTime;
import java.util.*;

@RecordBuilder
public record PageRecord(
    Integer id,
    Integer author,
    String title,
    String slug,
    String body,
    Optional<Integer> parent,
    Set<TreeRecord> children,
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

  public String slug() {
    Slugify slug = Slugify.builder().build();
    return slug.slugify(title);
  }

  public String tableName() {
    return "page_trees";
  }

  public Optional<String> tableAlias() {
    return Optional.of("pt");
  }

  public Set<String> columnList() {
    return Set.of("id", "author", "title", "slug", "body", "parent", "path", "depth", "approved", "created_on", "published_on");
  }

  public String whereClause() {
    return "where p.slug = :slug";
  }

  public Map<String, ?> valueMap() {
    Map<String, Object> valueMap = new HashMap<>(Map.of(
        "author", author,
        "title", title,
        "slug", slug(),
        "body", body,
        "approved", approved,
        "published_on", publishedOn,
        "created_on", Optional.ofNullable(createdOn).orElseGet(OffsetDateTime::now)
    ));
    parent.ifPresent(integer -> valueMap.put("parent", integer));
    return valueMap;
  }

}