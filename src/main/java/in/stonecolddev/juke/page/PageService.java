package in.stonecolddev.juke.page;


import in.stonecolddev.juke.data.storage.tree.TreeStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PageService {

  private final TreeStorageService<PageRecord> treeStorage;


  // TODO: TreeStorageService needs:
  //       1. a method that gathers columns in addition to the required columns for tree handling
  //          you should be able to do something like:
  //          // default columns don't need to be specified (id, parent_id, path, depth, etc)
  //          TreeQuery.table("page_trees")
  //                   .columns("t.approved", "t.author_id",...)
  //                    // default join, order by and with recursive stuff is already taken care of
  //                   .treeSelect()
  //       2. MapSqlParameterSource stuff should come from something already mapped in Query/TreeQuery
  //          e.g.:
  //          Query update = Query.builder()
  //                              .update("page_trees")
  //                              .set(newChangedValues)
  //                              .where("slug = :slug")
  //                              .compile();
  //          when .set() is handed newChangedValues (a HashMap of k/v column name -> column values),
  //          then it should assign those to a field that can be retrieved (update.updatedFields() or something)
  //       3. Query should be renamed and handle everything up to calling jdbcTemplate stuff
  //          Factor in how the MapSqlParamaterSource/updatedFields stuff factors in here


  public PageService(
      TreeStorageService<PageRecord> treeStorage
  ) {
    this.treeStorage = treeStorage;
  }


}