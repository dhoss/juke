package in.stonecolddev.juke.page;


import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Data
@Builder
@Accessors(fluent = true)
@With
public class Query {

  // TODO: Query could probably be an FSM
  //       everything up to an end state builds upon the query
  // Usage:
  // Query query =
  //     QueryBuilder
  //       .withRecursive(
  //          "tree",
  //          QueryBuilder.select("t.id, t.author...")
  //                      .from("page_trees t")
  //                      .where("t.slug = :slug"),
  //          QueryBuilder.select("t.id, t.author...")
  //                      .from("page_trees t")
  //                      .join("tree on t.parent = tree.id"),
  //          QueryBuilder.select("id, path, depth")
  //                       .from("tree")
  //                       .orderBy("path")
  //
  //       );
  //     .

  // this must be a List, Set sorts things automatically and can screw up column order
  private final List<String> select;
  private final String from;
  private final String join;
  private final String where;
  private final String orderBy;

  // TODO: if an insert value is provided, there must be an accompanying values set
  private final String insert;
  private final Map<String, String> values;

  @Setter(AccessLevel.NONE)
  private final String compiled;

  public static class QueryBuilder {

    // TODO: I would like this to be a fluent api at some point
    //       e.g.
    private final String NEW_LINE = " \n ";
    private final String SPACE = " ";
    private final String OPEN_PAREN = "(";
    private final String CLOSE_PAREN = ")";
    private final String AS = "as" + OPEN_PAREN;
    private final String WITH_RECURSIVE = "with recursive" + SPACE + NEW_LINE;
    private final String SELECT = "select" + SPACE + NEW_LINE;
    private final String UNION_ALL = "union all" + SPACE + NEW_LINE;
    private final String FROM = "from" + SPACE + NEW_LINE;
    private final String JOIN = "join" + SPACE;
    private final String WHERE = "where" + SPACE;
    private final String ORDER_BY = "order by" + SPACE;
    private final String INSERT = "insert into" + SPACE;
    private final String VALUES = "values(";

    public Query withRecursive(
        String cteName,
        QueryBuilder anchor,
        QueryBuilder recursive,
        QueryBuilder aggregate) {

      cteName = cteName + SPACE;
      this.compiled =
          WITH_RECURSIVE +
              cteName +
              AS +
              constructSelectQueryPart(anchor) +
              UNION_ALL +
              constructSelectQueryPart(recursive) +
              CLOSE_PAREN +
              NEW_LINE +
              constructSelectQueryPart(aggregate);

      return this.build();
    }

    private String constructSelectQueryPart(QueryBuilder qb) {
      return
          SELECT +
              Optional.ofNullable(qb.select)
                  .map(s -> String.join(",", s))
                  .orElseThrow(() -> new RuntimeException("no column names provided to select")) +
              NEW_LINE +
              FROM +
              qb.from +
              NEW_LINE +
              createClause(qb.where, (w) -> WHERE + w) +
              createClause(qb.join, (j) -> JOIN + j) +
              createClause(qb.orderBy, (o) -> ORDER_BY + o);
    }

    private String createClause(String clause, Function<String, String> mapper) {
      return Optional.ofNullable(clause)
          .map(mapper)
          .map(e -> e + NEW_LINE + SPACE)
          .orElseGet(() -> SPACE);
    }
  }
}