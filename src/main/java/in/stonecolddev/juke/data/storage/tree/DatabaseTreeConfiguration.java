package in.stonecolddev.juke.data.storage.tree;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

// TODO: we might be able to derive this from TreeRecord
@RecordBuilder
public record DatabaseTreeConfiguration(
    String idColumn,
    String treeTableAlias,
    Map<String, String> queryParameters,
    // TODO: I really don't know if we need anything other than anchorQueryColumnSet
    Set<String> anchorQueryColumnSet,
    String treeTable,
    String remainingAnchorQuery,
    Set<String> recursiveQueryColumnSet,
    String parentColumn,
    String remainingRecursiveQuery,
    Set<String> remainingCteQueryColumnsSet,
    String whereColumn // TODO: this is pretty unnecessary since slug is used everywhere
) implements DatabaseTreeConfigurationBuilder.With {

  public DatabaseTreeConfiguration {

    treeTableAlias = maybeField(treeTableAlias, "tree");
    whereColumn = maybeField(whereColumn, "slug");
    queryParameters = maybeField(queryParameters);
    anchorQueryColumnSet = maybeField(anchorQueryColumnSet, Set.of(whereColumn));
    recursiveQueryColumnSet = maybeField(recursiveQueryColumnSet);
    remainingCteQueryColumnsSet = maybeField(remainingCteQueryColumnsSet);

  }

  public Set<String> anchorQueryColumnSet() {
    Set<String> columnSet = new HashSet<>(anchorQueryColumnSet);
    columnSet.add(whereColumn);
    return columnSet;
  }

  public Set<String> recursiveQueryColumnSet() {
    return defaultQueryColumnSet(recursiveQueryColumnSet);
  }

  public Set<String> remainingCteQueryColumnsSet() {
    return defaultQueryColumnSet(recursiveQueryColumnSet);
  }

  private String maybeField(String fieldName, String defaultFieldName) {
    return Optional.ofNullable(fieldName).orElseGet(() -> defaultFieldName);
  }

  private Set<String> defaultQueryColumnSet(Set<String> queryColumnSet) {
    if (queryColumnSet.isEmpty() && !anchorQueryColumnSet.isEmpty())
      return anchorQueryColumnSet;
    return queryColumnSet;
  }

  private <T> Map<T, T> maybeField(Map<T, T> fieldElements) {
    return Optional.ofNullable(fieldElements).orElseGet(Map::of);
  }

  private <T> Set<T> maybeField(Set<T> fieldElements) {
    return maybeField(fieldElements, Set::of);
  }

  private <T> Set<T> maybeField(Set<T> fieldElements, Supplier<Set<T>> supplier) {
    return Optional.ofNullable(fieldElements).orElseGet(supplier);
  }

  private <T> Set<T> maybeField(Set<T> fieldElements, Set<T> extraElements) {
    Set<T> currentFieldElements = new HashSet<>(fieldElements);
    currentFieldElements.addAll(extraElements);
    return currentFieldElements;
  }
}