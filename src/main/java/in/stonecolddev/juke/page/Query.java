package in.stonecolddev.juke.page;


import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
  //          QueryBuilder.select("t.id, t.authorId...")
  //                      .from("page_trees t")
  //                      .where("t.slug = :slug"),
  //          QueryBuilder.select("t.id, t.authorId...")
  //                      .from("page_trees t")
  //                      .join("tree on t.parentId = tree.id"),
  //          QueryBuilder.select("id, path, depth")
  //                       .from("tree")
  //                       .orderBy("path")
  //
  //       );
  //     .

  // TODO: it's going to be a lot better to define each of these methods
  //       rather than relying on them as lombok setter/getters
  // this must be a List, Set sorts things automatically and can screw up column order
  private final List<String> select;
  private final String from;
  private final String join;
  private final String where;
  private final String orderBy;

  // TODO: if an insert value is provided, there must be an accompanying values set
  private final String insert;
  private final Map<String, Object> values;

  private final String update;
  private final Map<String, Object> set;
  private final String onDuplicate;

  @Setter(AccessLevel.NONE)
  private final String compiled;

  public static class QueryBuilder {

    // TODO: I would like this to be a fluent api or DSL at some point
    //       e.g. SELECT(what).FROM(where).JOIN(what).ON(what).WHERE(where).ORDER_BY(what)
    private final String SPACE = " ";
    private final String NEW_LINE = SPACE + "\n" + SPACE;
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
    private final String INSERT_INTO = "insert into" + SPACE;
    private final String INSERT_INTO_VALUES = "values";
    private final String UPDATE = "update" + SPACE;
    private final String UPDATE_SET = "set" + SPACE;

    public Query withRecursive(
        String cteName,
        QueryBuilder anchor,
        QueryBuilder recursive,
        QueryBuilder aggregate) {

      cteName = cteName + SPACE;
      // I guess this is ok for cases like with recursive
      this.compiled =
          WITH_RECURSIVE +
              cteName +
              AS +
              constructSelect(anchor) +
              UNION_ALL +
              constructSelect(recursive) +
              CLOSE_PAREN +
              NEW_LINE +
              constructSelect(aggregate);

      return this.build();
    }

    public Query compile() {
      String compiled = "";

      //TODO: use a sealed class + pattern matching here
      if (hasQueryKeyWord(this.select))
        compiled = constructSelect(this);

      if (hasQueryKeyWord(this.insert))
        compiled = constructInsert(this);

      if (hasQueryKeyWord(this.update))
        compiled = constructUpdate(this);

      // TODO: check if compiled is blank string here
      return this.compiled(compiled).build();
    }

    private Boolean hasQueryKeyWord(List<String> keywords) {
      return Optional.ofNullable(keywords).filter(k -> !k.isEmpty()).isPresent();
    }

    private Boolean hasQueryKeyWord(String keyword) {
      return Optional.ofNullable(keyword).filter(k -> !k.isEmpty()).isPresent();
    }

    private String constructUpdate(QueryBuilder qb) {
      // TODO: find a better way to filter immutable fields
      Set<String> columnNames =
          qb.set.keySet()
              .stream()
              .filter(c -> !c.equals("slug"))
              .collect(Collectors.toSet());

      return
          UPDATE +
              qb.update +
              NEW_LINE +
              UPDATE_SET +
              NEW_LINE +
              mapColumnValuesToNamedParams(columnNames, true) +
              NEW_LINE +
              createWhereClause(qb.where);
    }

    private String constructInsert(QueryBuilder qb) {
      Set<String> columnNames = qb.values.keySet();
      // TODO: it would be a good idea to build a tree here
      return
          INSERT_INTO +
              qb.insert +
              OPEN_PAREN +
              // TODO: check for empty stuff here
              String.join(",", columnNames) +
              CLOSE_PAREN +
              NEW_LINE +
              INSERT_INTO_VALUES +
              OPEN_PAREN +
              // TODO: check for empty stuff here
              // just map placeholders for now
              mapColumnValuesToNamedParams(columnNames) +
              CLOSE_PAREN;
    }

    private String mapColumnValuesToNamedParams(Set<String> columnNames) {
      return mapColumnValuesToNamedParams(columnNames, false);
    }

    private String mapColumnValuesToNamedParams(Set<String> columnNames, Boolean setKeyPairs) {
      return columnNames
          .stream()
          .map(k -> (setKeyPairs ? k + "=" : "") + ":" + k)
          .map(Object::toString)
          .collect(Collectors.joining(","));
    }

    private String constructSelect(QueryBuilder qb) {
      return
          SELECT +
              Optional.ofNullable(qb.select)
                  .map(s -> String.join(",", s))
                  .orElseThrow(() -> new RuntimeException("no column names provided to select")) +
              NEW_LINE +
              FROM +
              qb.from +
              NEW_LINE +
              createWhereClause(qb.where) +
              NEW_LINE +
              // createClause(qb.where, (w) -> WHERE + w) +
              createClause(qb.join, (j) -> JOIN + j) +
              NEW_LINE +
              createClause(qb.orderBy, (o) -> ORDER_BY + o);
    }

    private String createWhereClause(String where) {
      return createClause(where, (w) -> WHERE + w);
    }

    private String createClause(String clause, Function<String, String> mapper) {
      return Optional.ofNullable(clause)
          .map(mapper)
          .map(e -> e + NEW_LINE + SPACE)
          .orElseGet(() -> SPACE);
    }
  }
}