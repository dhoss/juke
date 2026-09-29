package in.stonecolddev.juke.page;

import in.stonecolddev.juke.data.storage.tree.TreeRecord;
import io.soabase.recordbuilder.core.RecordBuilder;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

}