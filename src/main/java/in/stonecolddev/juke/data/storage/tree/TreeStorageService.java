package in.stonecolddev.juke.data.storage.tree;

import in.stonecolddev.juke.page.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import java.util.*;
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

  // TODO: get rid of all of the query template stuff, I hate it
  public Optional<TreeRecord> find(String slug) {

    log.info("****** QUERY BUILDER QUERY {}",
        Query.builder()
            .withRecursive(
                "tree",
                Query.builder()
                    .select(
                        """
                                 t.id
                               , t.parent, t.approved, t.created_on, t.author, t.published_on, t.title, t.body, t.slug
                               , array[id] as "path"
                               , 1 as "depth"
                            """)
                    .from("page_trees t")
                    .where("t.parent is null"),
                Query.builder()
                    .select(
                        """
                                 t.id
                               , t.parent, t.approved, t.created_on, t.author, t.published_on, t.title, t.body, t.slug
                               , tree."path"  || t.id
                               , tree.depth + 1 as depth
                            """)
                    .from("page_trees t")
                    .join("tree on t.parent = tree.id"),
                Query.builder()
                    .select(
                        """
                                 id
                               , path
                               , depth
                               , parent, approved, created_on, author, published_on, title, body, slug
                            """
                    )
                    .from("tree")
                    .orderBy("path"))
            .compiled()
    );
    //  ST queryTemplate = new ST(
    //      """
    //          with recursive tree as (
    //                select
    //                  t.<primaryKey>
    //                  <anchorQueryColumnList>
    //                  , array[<primaryKey>] as "path"
    //                  , 1 as "depth"
    //                from <treeTable> t
    //                <remainingAnchorQuery>
    //                where t.<whereColumn> = :slug
    //
    //                union all
    //
    //                select
    //                    t.<primaryKey>
    //                    <anchorQueryColumnList>
    //                  , tree."path"  || t.<primaryKey>
    //                  , tree.depth + 1 as depth
    //                from <treeTable> t
    //                join tree on t.parent = tree.<primaryKey>
    //                <remainingAnchorQuery>
    //              )
    //              select
    //                  <primaryKey>
    //                , path
    //                , depth
    //                <remainingCteQueryColumnsList>
    //              from tree
    //              order by path;
    //          """
    //  );

    //  queryTemplate.add("primaryKey", configuration.idColumn());
    //  queryTemplate.add("anchorQueryColumnList",
    //      joinColumnListToString(configuration.anchorQueryColumnSet(), ", "));
    //  queryTemplate.add("treeTable", configuration.treeTable());
    //  queryTemplate.add("remainingCteQueryColumnsList",
    //      joinColumnListToString(
    //          configuration.anchorQueryColumnSet(), ", ", false));
    //  queryTemplate.add("whereColumn", configuration.whereColumn());

    //  Map<String, String> queryParameters =
    //      new HashMap<>(Map.of(configuration.whereColumn(), slug));
    //  queryParameters.putAll(configuration.queryParameters());

    return jdbcTemplate.query(
        Query.builder()
            .withRecursive(
                "tree",
                Query.builder()
                    .select(
                        """
                                 t.id
                               , t.parent, t.approved, t.created_on, t.author, t.published_on, t.title, t.body, t.slug
                               , array[id] as "path"
                               , 1 as "depth"
                            """)
                    .from("page_trees t")
                    .where("t.parent is null"),
                Query.builder()
                    .select(
                        """
                                 t.id
                               , t.parent, t.approved, t.created_on, t.author, t.published_on, t.title, t.body, t.slug
                               , tree."path"  || t.id
                               , tree.depth + 1 as depth
                            """)
                    .from("page_trees t")
                    .join("tree on t.parent = tree.id"),
                Query.builder()
                    .select(
                        """
                                 id
                               , path
                               , depth
                               , parent, approved, created_on, author, published_on, title, body, slug
                            """
                    )
                    .from("tree")
                    .orderBy("path"))
            .compiled(),
        //queryTemplate.render(),
        new MapSqlParameterSource().addValues(Map.of("slug", slug)), //queryParameters),
        treeResultSet.resultSetExtractor()
    );
  }

  // TODO: pagination
  public List<TreeRecord> listTrees() {

    ST queryTemplate = new ST(
        """
            with recursive tree as (
                  select
                    t.<primaryKey>
                    <anchorQueryColumnList>
                    , array[<primaryKey>] as "path"
                    , 1 as "depth"
                  from <treeTable> t
                  <remainingAnchorQuery>
                  where t.parent is null
            
                  union all
            
                  select
                      t.<primaryKey>
                      <anchorQueryColumnList>
                    , tree."path"  || t.<primaryKey>
                    , tree.depth + 1 as depth
                  from <treeTable> t
                  join tree on t.parent = tree.<primaryKey>
                  <remainingAnchorQuery>
                )
                select
                    <primaryKey>
                  , path
                  , depth
                  <remainingCteQueryColumnsList>
                from tree
                order by path;
            """
    );

    queryTemplate.add("primaryKey", configuration.idColumn());
    queryTemplate.add("anchorQueryColumnList",
        joinColumnListToString(configuration.anchorQueryColumnSet(), ", "));
    queryTemplate.add("treeTable", configuration.treeTable());
    queryTemplate.add("remainingCteQueryColumnsList",
        joinColumnListToString(
            configuration.anchorQueryColumnSet(), ", ", false));
    queryTemplate.add("whereColumn", configuration.whereColumn());

    Map<String, String> queryParameters = new HashMap<>(configuration.queryParameters());

    // TODO: need a resultsetextractor that returns a list of nested full trees
    return jdbcTemplate.query(
        queryTemplate.render(),
        new MapSqlParameterSource().addValues(queryParameters),
        treeResultSet.resultSetExtractorList()
    );
  }

  // TODO: implement ancestor retrieval
  public List<TreeRecord> ancestors(TreeRecord node) {
    return List.of();
  }

  // TODO: retrieve a tree's ancestor's
  public TreeRecord create(TreeRecord tree) {
    // TODO: this should go in its own class
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

  public TreeRecord addChild(TreeRecord parent, TreeRecord child) {
    log.info("adding child {} to parent {}", child.slug(), parent.slug());
    create(child.reparent(parent));
    return find(parent.slug()).orElseThrow(
        () -> new RuntimeException("Can't find parent of child we just created for some reason"));
  }

  private String joinColumnListToString(Set<?> toString, String joinWith) {
    return joinColumnListToString(toString, joinWith, true);
  }

  private String joinColumnListToString(Set<?> toString, String joinWith, Boolean useAlias) {
    return toString.stream()
        .map(ts -> {
          if (useAlias)
            return joinWith + "t." + ts;
          //return joinWith + configuration.treeTableAlias() + "." + ts;
          return joinWith + ts;
        })
        .collect(Collectors.joining());
  }
}