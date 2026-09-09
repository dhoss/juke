package in.stonecolddev.juke.data.storage.tree;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
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

  public T save(T tree) {
    return null;
  }

}