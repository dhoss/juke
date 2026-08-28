package in.stonecolddev.juke.data.storage.tree;

import org.junit.Test;

import java.util.List;
import java.util.Map;

public class TreeStorageServiceTest {


  @Test
  public void find() {

    TreeStorageService<TestRecord> ts = new TreeStorageService<>(
        DatabaseTreeConfigurationBuilder.builder()
            .idColumn("id")
            .queryParameters(Map.of("test", "param1"))
            .anchorQueryColumnList(List.of("b.column1", "b.column2"))
            .treeTable("tree_table")
            .remainingAnchorQuery("left join table2 t2 on t2.id=b.other_id")
            .recursiveQueryColumnList(List.of("column1", "column2"))
            .parentColumn("parent_id")
            .remainingRecursiveQuery("left join other_table o on o.id=b.other_id")
            .remainingCteQueryColumns(List.of("author_id", "email"))
            .whereColumn("slug")
            .build()
    );

    ts.find("test");

  }

  record TestRecord(String id) {
  }

}