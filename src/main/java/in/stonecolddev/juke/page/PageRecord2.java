package in.stonecolddev.juke.page;

import in.stonecolddev.juke.data.storage.tree.TreeRecord2;
import io.soabase.recordbuilder.core.RecordBuilder;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RecordBuilder
public record PageRecord2(
    Integer id,
    Integer author,
    String title,
    String slug,
    String body,
    Optional<Integer> parent,
    Set<PageRecord2> children,
    List<Integer> path,
    Integer depth,
    Boolean approved,
    OffsetDateTime createdOn,
    OffsetDateTime publishedOn) implements TreeRecord2<PageRecord2>, PageRecord2Builder.With {

  public PageRecord2 {
    children = Optional.ofNullable(children).orElseGet(HashSet::new);
  }

  public void addChild(PageRecord2 child) {
    this.children.add(child);
  }

}