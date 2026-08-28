package in.stonecolddev.juke.data.storage.tree;

import org.stringtemplate.v4.ST;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TreeStorageService<T> {

  // private final NamedParameterJdbcTemplate jdbcTemplate;

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
  //           , ${remainingCteQueryColumns}
  //         from cte
  //         order by path;

  private final DatabaseTreeConfiguration configuration;

//  private final ResultSetExtractor<T> resultSetExtractor;

  public TreeStorageService(
      DatabaseTreeConfiguration configuration//,
//      NamedParameterJdbcTemplate jdbcTemplate,
//      ResultSetExtractor<T> resultSetExtractor
  ) {
    this.configuration = configuration;
//    this.jdbcTemplate = jdbcTemplate;
//    this.resultSetExtractor = resultSetExtractor;
  }


  public Optional<T> find(String slug) {

    ST queryTemplate = new ST(
        """
            with recursive cte as (
                  select
                      alias.<idColumn>
                    <anchorQueryColumnList>
                    , array[<idColumn>] as "path"
                    , 1 as "depth"
                  from <treeTable> alias
                  <remainingAnchorQuery>
                  where <whereColumn> = :slug
            
                  union all
            
                  select
                      alias.<idColumn>
                    <recursiveQueryColumnList>
                    , cte."path"  || alias.<idColumn>
                    , cte.depth + 1 as depth
                  from <treeTable> alias
                  join cte on <parentColumn> = cte.<idColumn>
                  <remainingRecursiveQuery>
                )
                select
                    <idColumn>
                  , path
                  , depth
                  <remainingCteQueryColumns>
                from cte
                order by path;
            """
    );

    queryTemplate.add("idColumn", configuration.idColumn());
    queryTemplate.add("anchorQueryColumnList",
        joinListToString(configuration.anchorQueryColumnList(), ", "));
    queryTemplate.add("treeTable", configuration.treeTable());
    queryTemplate.add("remainingAnchorQuery", configuration.remainingAnchorQuery());
    queryTemplate.add("recursiveQueryColumnList",
        joinListToString(configuration.recursiveQueryColumnList(), ", "));
    queryTemplate.add("parentColumn", configuration.parentColumn());
    queryTemplate.add("remainingRecursiveQuery", configuration.remainingAnchorQuery());
    queryTemplate.add("remainingCteQueryColumns",
        joinListToString(configuration.remainingCteQueryColumns(), ", "));
    queryTemplate.add("whereColumn", configuration.whereColumn());

    Map<String, String> queryParameters = new java.util.HashMap<>(Map.of("slug", slug));
    queryParameters.putAll(configuration.queryParameters());

    System.out.println("**** QUERY " + queryTemplate.render());

    return Optional.empty();
    //  return Optional.ofNullable(
    //      jdbcTemplate.query(
    //          queryTemplate.render(),
    //          new MapSqlParameterSource().addValues(queryParameters),
    //          resultSetExtractor)
    //  );
  }

  private String joinListToString(List<?> toString, String joinWith) {
    return toString.stream().map(ts -> joinWith + ts).collect(Collectors.joining());
  }

  public T save(T tree) {
    return null;
  }

}