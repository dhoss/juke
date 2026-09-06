package in.stonecolddev.juke.data.storage.tree;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.stringtemplate.v4.ST;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TreeStorageService<T extends TreeRecord> {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  // TODO: generic tree retrieval query:
  //     with recursive cte as (
  //           select
  //               alias.${idColumn}
  //             , ${anchorQueryColumnList}
  //             , array[${idColumn}] as "path"
  //             -- TODO: make depth configurable
  //             , 1 as "depth"
  //           from ${treeTable} alias
  //           ${remainingAnchorQuery}
  //
  //           union all
  //
  //           select
  //               alias.${idColumn}
  //             , ${recursiveQueryColumnList}
  //             , cte."path"  || alias.${idColumn}
  //             , cte.depth + 1 as depth
  //           from ${treeTable} alias
  //           join cte on ${parentColumn} = cte.${idColumn}
  //           ${remainingRecursiveQuery}
  //         )
  //         select
  //             ${idColumn}
  //           , path
  //           , depth
  //           , ${remainingCteQueryColumnsList}
  //         from cte
  //         order by path;

  private final DatabaseTreeConfiguration configuration;

  private final ResultSetExtractor<Optional<DatabaseTree<T>>> resultSetExtractor;

  public TreeStorageService(
      DatabaseTreeConfiguration configuration,
      NamedParameterJdbcTemplate jdbcTemplate,
      ResultSetExtractor<Optional<DatabaseTree<T>>> resultSetExtractor
  ) {
    this.configuration = configuration;
    this.jdbcTemplate = jdbcTemplate;
    this.resultSetExtractor = resultSetExtractor;
  }


  public Optional<DatabaseTree<T>> find(String slug) {

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
                  join cte on <parentColumn> = cte.<idColumn>
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
        joinColumnListToString(configuration.anchorQueryColumnList(), ", "));
    queryTemplate.add("treeTable", configuration.treeTable());
    queryTemplate.add("remainingAnchorQuery", configuration.remainingAnchorQuery());
    // TODO: if this is empty, just use anchorQueryColumnList
    queryTemplate.add("recursiveQueryColumnList",
        joinColumnListToString(configuration.recursiveQueryColumnList(), ", "));
    queryTemplate.add("parentColumn", configuration.parentColumn());
    queryTemplate.add("remainingRecursiveQuery", configuration.remainingAnchorQuery());
    queryTemplate.add("remainingCteQueryColumnsList",
        joinColumnListToString(configuration.remainingCteQueryColumnsList(), ", ", false));
    queryTemplate.add("whereColumn", configuration.whereColumn());

    Map<String, String> queryParameters = new java.util.HashMap<>(Map.of(configuration.whereColumn(), slug));
    queryParameters.putAll(configuration.queryParameters());

    System.out.println("**** QUERY " + queryTemplate.render());

    return jdbcTemplate.query(
        queryTemplate.render(),
        new MapSqlParameterSource().addValues(queryParameters),
        resultSetExtractor
    );
  }

  private String joinColumnListToString(List<?> toString, String joinWith) {
    return joinColumnListToString(toString, joinWith, true);
  }

  private String joinColumnListToString(List<?> toString, String joinWith, Boolean useAlias) {
    return toString.stream()
        .map(ts -> {
          if (useAlias)
            return joinWith + configuration.treeTableAlias() + "." + ts;
          return joinWith + ts;
        })
        .peek(c -> System.out.println("**** USE ALIAS " + useAlias + " JOIN WITH " + joinWith + " JOINED " + c))
        .collect(Collectors.joining());
  }

  public T save(T tree) {
    return null;
  }

}