package in.stonecolddev.juke.data.storage.tree;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.List;
import java.util.Optional;

@RecordBuilder
public record TreeRecord<T>(
    T root,
    List<T> children,
    Optional<T> parent,
    List<T> siblings,
    List<T> ancestors
) implements TreeRecordBuilder.With<T> {

  public TreeRecord {

    children = maybeField(children);
    siblings = maybeField(siblings);
    ancestors = maybeField(ancestors);

  }

  private List<T> maybeField(List<T> fieldElements) {
    return Optional.ofNullable(fieldElements).orElseGet(List::of);
  }

  public void addChild(T child) {
  }

  public void reparent(T newParent) {
  }

}