package in.stonecolddev.juke.data.storage.tree;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.List;
import java.util.Map;

@RecordBuilder
public record DatabaseTreeConfiguration(
    String idColumn,
    Map<String, String> queryParameters,
    List<String> anchorQueryColumnList,
    String treeTable,
    String remainingAnchorQuery,
    List<String> recursiveQueryColumnList,
    String parentColumn,
    String remainingRecursiveQuery,
    List<String> remainingCteQueryColumns,
    String whereColumn
) {
}