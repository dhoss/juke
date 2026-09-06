package in.stonecolddev.juke.data.storage.tree;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@RecordBuilder
public record DatabaseTreeConfiguration(
    String idColumn,
    String treeTableAlias,
    Map<String, String> queryParameters,
    List<String> anchorQueryColumnList,
    String treeTable,
    String remainingAnchorQuery,
    List<String> recursiveQueryColumnList,
    String parentColumn,
    String remainingRecursiveQuery,
    List<String> remainingCteQueryColumnsList,
    String whereColumn
) implements DatabaseTreeConfigurationBuilder.With {

  public DatabaseTreeConfiguration {

    treeTableAlias = Optional.ofNullable(treeTableAlias).orElseGet(() -> "tree");
    queryParameters = maybeField(queryParameters);
    anchorQueryColumnList = maybeField(anchorQueryColumnList);
    recursiveQueryColumnList = maybeField(recursiveQueryColumnList);
    remainingCteQueryColumnsList = maybeField(remainingCteQueryColumnsList);

  }

  public List<String> recursiveQueryColumnList() {
    return defaultQueryColumnList(recursiveQueryColumnList);
  }

  public List<String> remainingCteQueryColumnsList() {
    return defaultQueryColumnList(recursiveQueryColumnList);
  }

  private List<String> defaultQueryColumnList(List<String> queryColumnList) {
    if (queryColumnList.isEmpty() && !anchorQueryColumnList.isEmpty())
      return anchorQueryColumnList;
    return queryColumnList;
  }

  public static DatabaseTreeConfiguration newWithDefaults() {
    return DatabaseTreeConfigurationBuilder.builder()
        .idColumn("id")
        .treeTableAlias("tree")
        .parentColumn("parent_id")
        .whereColumn("slug")
        .build();
  }

  private <T> Map<T, T> maybeField(Map<T, T> fieldElements) {
    return Optional.ofNullable(fieldElements).orElseGet(Map::of);
  }

  private <T> List<T> maybeField(List<T> fieldElements) {
    return (List<T>) maybeField(fieldElements, List::of);
  }

  private <T> Collection<T> maybeField(Collection<T> fieldElements, Supplier<Collection<T>> supplier) {
    return Optional.ofNullable(fieldElements).orElseGet(supplier);
  }

}