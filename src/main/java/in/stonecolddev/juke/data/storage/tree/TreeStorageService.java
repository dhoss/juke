package in.stonecolddev.juke.data.storage.tree;

import in.stonecolddev.juke.page.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
// TODO: rename this
public class TreeStorageService<T extends TreeRecord> {


  private final NamedParameterJdbcTemplate jdbcTemplate;

  private final DatabaseTreeConfiguration configuration;

  private final TreeResultSet<T> treeResultSet;

  public TreeStorageService(
      DatabaseTreeConfiguration configuration,
      NamedParameterJdbcTemplate jdbcTemplate,
      TreeResultSet<T> treeResultSet
  ) {
    this.configuration = configuration;
    this.jdbcTemplate = jdbcTemplate;
    this.treeResultSet = treeResultSet;
  }

  // TODO: make this take a TreeRecord instead of a String
  public Optional<TreeRecord> find(String slug) {

    return jdbcTemplate.query(
        // TODO: make this a general query that can be customized with additional fields and joins etc
        Query.builder()
            .withRecursive(
                "tree",
                Query.builder()
                    .select(
                        List.of(
                            "t.id",
                            "t.parent",
                            "t.approved",
                            "t.created_on",
                            "t.author",
                            "t.published_on",
                            "t.title",
                            "t.body",
                            "t.slug",
                            "array[id] as path",
                            "1 as depth"
                        )
                    )
                    .from("page_trees t")
                    .where("t.slug = :slug"),
                Query.builder()
                    .select(
                        List.of(
                            "t.id",
                            "t.parent",
                            "t.approved",
                            "t.created_on",
                            "t.author",
                            "t.published_on",
                            "t.title",
                            "t.body",
                            "t.slug",
                            "tree.path || t.id",
                            "tree.depth +1 as depth"
                        )
                    )
                    .from("page_trees t")
                    .join("tree on t.parent = tree.id"),
                Query.builder()
                    .select(
                        List.of(
                            "id",
                            "parent",
                            "approved",
                            "created_on",
                            "author",
                            "published_on",
                            "title",
                            "body",
                            "slug",
                            "path",
                            "depth"
                        )
                    )
                    .from("tree")
                    .orderBy("path"))
            .compiled(),
        new MapSqlParameterSource().addValues(Map.of("slug", slug)),
        treeResultSet.resultSetExtractor()
    );
  }

  // TODO: pagination
  public List<TreeRecord> listTrees() {

    return jdbcTemplate.query(
        Query.builder()
            .withRecursive(
                "tree",
                Query.builder()
                    .select(
                        List.of(
                            "t.id",
                            "t.parent",
                            "t.approved",
                            "t.created_on",
                            "t.author",
                            "t.published_on",
                            "t.title",
                            "t.body",
                            "t.slug",
                            "array[id] as path",
                            "1 as depth"
                        )
                    )
                    .from("page_trees t")
                    .where("t.parent is null"),
                Query.builder()
                    .select(
                        List.of(
                            "t.id",
                            "t.parent",
                            "t.approved",
                            "t.created_on",
                            "t.author",
                            "t.published_on",
                            "t.title",
                            "t.body",
                            "t.slug",
                            "tree.path || t.id",
                            "tree.depth +1 as depth"
                        )
                    )
                    .from("page_trees t")
                    .join("tree on t.parent = tree.id"),
                Query.builder()
                    .select(
                        List.of(
                            "id",
                            "parent",
                            "approved",
                            "created_on",
                            "author",
                            "published_on",
                            "title",
                            "body",
                            "slug",
                            "path",
                            "depth"
                        )
                    )
                    .from("tree")
                    .orderBy("path"))
            .compiled(),
        treeResultSet.resultSetExtractorList()
    );
  }

  public List<TreeRecord> ancestors(TreeRecord node) {
    return jdbcTemplate.query(
        // TODO: make this a general query that can be customized with additional fields and joins etc
        Query.builder()
            .withRecursive(
                "tree",
                Query.builder()
                    .select(
                        List.of(
                            "t.id",
                            "t.parent",
                            "t.approved",
                            "t.created_on",
                            "t.author",
                            "t.published_on",
                            "t.title",
                            "t.body",
                            "t.slug",
                            "array[id] as path",
                            "1 as depth"
                        )
                    )
                    .from("page_trees t")
                    .where("t.slug = :slug"),
                Query.builder()
                    .select(
                        List.of(
                            "t.id",
                            "t.parent",
                            "t.approved",
                            "t.created_on",
                            "t.author",
                            "t.published_on",
                            "t.title",
                            "t.body",
                            "t.slug",
                            "tree.path || t.id",
                            "tree.depth +1 as depth"
                        )
                    )
                    .from("page_trees t")
                    .join("tree on t.id = tree.parent"),
                Query.builder()
                    .select(
                        List.of(
                            "id",
                            "parent",
                            "approved",
                            "created_on",
                            "author",
                            "published_on",
                            "title",
                            "body",
                            "slug",
                            "path",
                            "depth"
                        )
                    )
                    .from("tree")
                    .orderBy("path"))
            .compiled(),
        new MapSqlParameterSource().addValues(Map.of("slug", node.slug())),
        treeResultSet.resultSetExtractorList()
    );
  }

  // TODO: retrieve a tree's ancestor's
  public TreeRecord create(TreeRecord tree) {
    // TODO: GET RID OF THIS
    ST queryTemplate = new ST(
        """
            insert into <treeTable> (<valueSet>)
            values (<valueMap>);
            """
    );

    queryTemplate.add("treeTable", configuration.treeTable());
    Set<String> valueSet = tree.valueMap().keySet();
    queryTemplate.add("valueSet", String.join(",", valueSet));
    queryTemplate.add("valueMap",
        valueSet
            .stream()
            .map(k -> ":" + k)
            .map(String::valueOf)
            .collect(Collectors.joining(",")));

    // TODO: make this a transaction
    //          @Autowired
    //          private NamedParameterJdbcTemplate jdbcTemplate;
    //          @Autowired
    //          private TransactionTemplate transactionTemplate;
    //          public void executionWithManualControl() {
    //              transactionTemplate.execute(status -> {
    //                  // This block executes inside a managed transaction
    //                  jdbcTemplate.update("UPDATE users...", params1);
    //                  return null;
    //              });
    //          }
    log.info("child count for node {}: {}", tree.slug(), tree.children().size());
    log.info("inserting node {}", tree.slug());
    jdbcTemplate.update(
        queryTemplate.render(),
        new MapSqlParameterSource().addValues(tree.valueMap())
    );

    log.info("adding child nodes if they exist");
    for (TreeRecord child : tree.children()) {
      addChild(tree, child);
    }

    return find(tree.slug())
        .orElseThrow(
            () -> new RuntimeException(
                "Can't find tree we just inserted with slug " + tree.slug()));
  }

  // TODO: wrap this in a transaction
  public TreeRecord addChild(TreeRecord parent, TreeRecord child) {
    log.info("adding child {} to parent {}", child.slug(), parent.slug());
    create(child.reparent(parent));
    return find(parent.slug()).orElseThrow(
        () -> new RuntimeException("Can't find parent of child we just created for some reason"));
  }

}