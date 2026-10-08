package in.stonecolddev.juke.page;


import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Data
@Builder
@Accessors(fluent = true)
@With
public class Query {

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

  @Setter(AccessLevel.NONE)
  private final String compiled;

  public static class QueryBuilder {

    private final String NEW_LINE = " \n ";
    private final String SPACE = " ";
    private final String AS = "as";
    private final String OPEN_PAREN = "(";
    private final String CLOSE_PAREN = ")";
    private final String SELECT = "select" + SPACE;
    private final String UNION_ALL = "union all" + SPACE;
    private final String FROM = "from" + SPACE;
    private final String JOIN = "join" + SPACE;
    private final String WHERE = "where" + SPACE;
    private final String ORDER_BY = "order by" + SPACE;

    // TODO: clean this up
    public Query withRecursive(
        String cteName,
        QueryBuilder anchor,
        QueryBuilder recursive,
        QueryBuilder aggregate) {

      this.compiled = "with recursive" +
          NEW_LINE +
          cteName +
          SPACE +
          AS + OPEN_PAREN +
          SPACE +
          constructSelectQueryPart(anchor) +
          SPACE +
          UNION_ALL +
          NEW_LINE +
          SPACE +
          constructSelectQueryPart(recursive) +
          CLOSE_PAREN +
          NEW_LINE +
          constructSelectQueryPart(aggregate);

      return this.build();
    }

    // TODO: clean this up
    private String constructSelectQueryPart(QueryBuilder qb) {
      return
          SELECT +
              NEW_LINE +
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
      // Optional.ofNullable(qb.where).map(w -> WHERE + SPACE + w + NEW_LINE + SPACE).orElseGet(() -> "") +
      // Optional.ofNullable(qb.join).map(j -> JOIN + SPACE + j + NEW_LINE + SPACE).orElseGet(() -> "") +
      // Optional.ofNullable(qb.orderBy).map(o -> ORDER_BY + SPACE + o + NEW_LINE + SPACE).orElseGet(() -> "");
    }

    private String createClause(String clause, Function<String, String> mapper) {
      return Optional.ofNullable(clause)
          .map(mapper)
          .map(e -> e + NEW_LINE + SPACE)
          .orElseGet(() -> "");
    }


  }


}