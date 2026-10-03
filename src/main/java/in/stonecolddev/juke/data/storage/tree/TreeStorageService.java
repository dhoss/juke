package in.stonecolddev.juke.data.storage.tree;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TreeStorageService<T extends TreeRecord> {

  // TODO: do we want table information defined here or in the TreeRecord implementation?

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

  public Optional<TreeRecord> find(String slug) {

    ST queryTemplate = new ST(
        """
            with recursive cte as (
                  select
                      <treeTableAlias>.<idColumn>
                    <anchorQueryColumnList>
                    , array[<idColumn>] as "path"
                    , 1 as "depth"
                  from <treeTable> <treeTableAlias>
                  <remainingAnchorQuery>
                  where <treeTableAlias>.<whereColumn> = :slug
            
                  union all
            
                  select
                      <treeTableAlias>.<idColumn>
                    <recursiveQueryColumnList>
                    , cte."path"  || <treeTableAlias>.<idColumn>
                    , cte.depth + 1 as depth
                  from <treeTable> <treeTableAlias>
                  join cte on <treeTableAlias>.<parentColumn> = cte.<idColumn>
                  <remainingRecursiveQuery>
                )
                select
                    <idColumn>
                  , path
                  , depth
                  <remainingCteQueryColumnsList>
                from cte
                order by path;
            """
    );

    queryTemplate.add("idColumn", configuration.idColumn());
    queryTemplate.add("treeTableAlias", configuration.treeTableAlias());
    queryTemplate.add("anchorQueryColumnList",
        joinColumnListToString(configuration.anchorQueryColumnSet(), ", "));
    queryTemplate.add("treeTable", configuration.treeTable());
    queryTemplate.add("remainingAnchorQuery", configuration.remainingAnchorQuery());
    queryTemplate.add("recursiveQueryColumnList",
        joinColumnListToString(
            configuration.recursiveQueryColumnSet(), ", "));
    queryTemplate.add("parentColumn", configuration.parentColumn());
    queryTemplate.add("remainingRecursiveQuery", configuration.remainingAnchorQuery());
    queryTemplate.add("remainingCteQueryColumnsList",
        joinColumnListToString(
            configuration.remainingCteQueryColumnsSet(), ", ", false));
    queryTemplate.add("whereColumn", configuration.whereColumn());

    Map<String, String> queryParameters =
        new HashMap<>(Map.of(configuration.whereColumn(), slug));
    queryParameters.putAll(configuration.queryParameters());

    return jdbcTemplate.query(
        queryTemplate.render(),
        new MapSqlParameterSource().addValues(queryParameters),
        treeResultSet.resultSetExtractor()
    );
  }

  public TreeRecord create(TreeRecord tree) {
    // TODO: this should go in its own class
    ST queryTemplate = new ST(
        """
            insert into <treeTable> (<valueSet>)
            values (<valueMap>);
            """
    );

    queryTemplate.add("treeTable", tree.tableName());
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
    log.info("***** CHILD COUNT FOR NODE {}: {}", tree.slug(), tree.children().size());
    log.info("***** INSERTING {}", tree.slug());
    jdbcTemplate.update(
        queryTemplate.render(),
        new MapSqlParameterSource().addValues(tree.valueMap())
    );

    log.info("**** LOOPING OVER CHILDREN AND ADDING THEM");
    for (TreeRecord child : tree.children()) {

      addChild(tree, child);

    }

    return find(tree.slug())
        .orElseThrow(
            () -> new RuntimeException(
                "Can't find tree we just inserted with slug " + tree.slug()));
  }

  public TreeRecord addChild(TreeRecord parent, TreeRecord child) {
    log.info("**** ADDING CHILD {} TO PARENT {}", child.slug(), parent.slug());
    create(child.reparent(parent));
    return find(parent.slug()).orElseThrow(
        () -> new RuntimeException("Can't find parent of child we just created for some reason"));
  }

  // public TreeRecord update(TreeRecord tree) {
  //   // TODO: this should go in its own class
  //   ST queryTemplate = new ST(
  //       """
  //           update <treeTable>
  //           set <valueMap>
  //           <whereClause>
  //           """
  //   );
  // }

  private String joinColumnListToString(Set<?> toString, String joinWith) {
    return joinColumnListToString(toString, joinWith, true);
  }

  private String joinColumnListToString(Set<?> toString, String joinWith, Boolean useAlias) {
    return toString.stream()
        .map(ts -> {
          if (useAlias)
            return joinWith + configuration.treeTableAlias() + "." + ts;
          return joinWith + ts;
        })
        .collect(Collectors.joining());
  }


}