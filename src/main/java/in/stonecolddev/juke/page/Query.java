package in.stonecolddev.juke.page;


import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Optional;

@Data
@Builder
@Accessors(fluent = true)
@With
public class Query {

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

    // TODO: clean this up
    public Query withRecursive(
        String cteName,
        QueryBuilder anchor,
        QueryBuilder recursive,
        QueryBuilder aggregate) {
      this.compiled = "with recursive" +
          " \n " +
          cteName +
          " " +
          "as (" +
          " " +
          constructSelectQueryPart(anchor) +
          " " +
          "union all" +
          " \n " +
          " " +
          constructSelectQueryPart(recursive) +
          ")" +
          " \n " +
          constructSelectQueryPart(aggregate);


      return this.build();
    }

    // TODO: clean this up
    private String constructSelectQueryPart(QueryBuilder qb) {
      return "select" +
          " \n " +
          Optional.ofNullable(qb.select)
              .map(s -> String.join(",", s))
              .orElseThrow(() -> new RuntimeException("no column names provided to select")) +
          " \n " +
          "from" +
          "  " +
          qb.from +
          " \n " +
          Optional.ofNullable(qb.where).map(w -> "where " + w + "\n ").orElseGet(() -> "") +
          Optional.ofNullable(qb.join).map(j -> "join " + j + "\n ").orElseGet(() -> "") +
          Optional.ofNullable(qb.orderBy).map(o -> "order by " + o + "\n ").orElseGet(() -> "");
    }


  }


}