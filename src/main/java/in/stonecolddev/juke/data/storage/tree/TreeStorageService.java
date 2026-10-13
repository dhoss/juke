package in.stonecolddev.juke.data.storage.tree;

import in.stonecolddev.juke.page.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import java.util.*;
import java.util.stream.Collectors;

import static in.stonecolddev.juke.data.storage.tree.DatabaseTree.findRootId;

@Slf4j
// TODO: this shouldn't be a component
//       instantiate or extend it from a specific implementation, PageTreeStorage etc
@Component
// TODO: rename this
public class TreeStorageService<T extends TreeRecord> {


  private final NamedParameterJdbcTemplate jdbcTemplate;

  private final DatabaseTreeConfiguration configuration;

  private final TreeResultSet<T> treeResultSet;

  private final Map<Integer, NodeInfo> nodeInfoMap;
  private final Map<String, NodeInfo> nodeInfoMapSlugIndexed;

  private final Map<String, TreeRecord> treeCache = new HashMap<>();

  public TreeStorageService(
      DatabaseTreeConfiguration configuration,
      NamedParameterJdbcTemplate jdbcTemplate,
      TreeResultSet<T> treeResultSet
  ) {
    this.configuration = configuration;
    this.jdbcTemplate = jdbcTemplate;
    this.treeResultSet = treeResultSet;
    this.nodeInfoMap = loadNodeToParentMapping();
    this.nodeInfoMapSlugIndexed = mapNodeToParentMappingBySlug();

  }

  record NodeInfo(Integer id, Optional<Integer> parentId, String slug) {
  }

  private Map<String, NodeInfo> mapNodeToParentMappingBySlug() {
    Map<String, NodeInfo> mapping = new HashMap<>();
    this.nodeInfoMap.forEach((_, value) -> mapping.put(value.slug, value));
    return mapping;
  }

  // TODO: tests
  private Map<Integer, NodeInfo> loadNodeToParentMapping() {
    return jdbcTemplate.query(
        Query.builder()
            .select(List.of("id", "parent_id", "slug"))
            .from("page_trees")
            .compiled(),
        (rs) -> {
          Map<Integer, NodeInfo> mapping = new HashMap<>();
          while (rs.next()) {
            int id = rs.getInt("id");
            int parentId = rs.getInt("parent_id");
            mapping.put(
                id,
                new NodeInfo(
                    id,
                    Optional.ofNullable(parentId != 0 ? parentId : null),
                    rs.getString("slug")));
          }

          return mapping;
        }
    );
  }

  public Optional<TreeRecord> find(TreeRecord tree) {
    return find(tree.slug());
  }

  private boolean isRootNode(String slug) {
    NodeInfo value = nodeInfoMapSlugIndexed.get(slug);
    return Optional.ofNullable(value)
        .filter(n -> n.parentId().isEmpty())
        .isPresent();
  }


  private Optional<TreeRecord> findNode(String slug) {
    return Optional.of(
        treeCache.computeIfAbsent(
            slug,
            (k) ->
                findInDbBySlug(k).orElseThrow(
                    () -> new RuntimeException(
                        "Can't find node by slug " + slug + " to populate in tree cache"))));
  }

  // TODO: should probably cache/memoize this
  private Map<Integer, Optional<Integer>> nodeToParentMap() {
    Map<Integer, Optional<Integer>> mapping = new HashMap<>();
    nodeInfoMap.forEach((key, value) -> mapping.put(key, value.parentId()));
    return mapping;
  }

  private Optional<TreeRecord> findRootBySlug(String slug) {
    return find(
        nodeInfoMap.get(
                findRootId(
                    nodeInfoMapSlugIndexed.get(slug).id(),
                    nodeToParentMap()))
            .slug());
  }

  private TreeRecord findRootNodeInCache(String slug) {
    if (treeCache.isEmpty()) {
      TreeRecord root = findRootBySlug(slug).orElseThrow(
          () -> new RuntimeException("Can't find root node for child " + slug));
      treeCache.put(root.slug(), root);
    }

    return treeCache.values()
        .stream()
        .filter(e ->
            e.id().equals(
                findRootId(nodeInfoMapSlugIndexed.get(slug).id(), nodeToParentMap())))
        .findFirst()
        .orElseThrow(
            () -> new RuntimeException(
                "No root node found when searching for root node for slug " + slug));
  }

  private Optional<TreeRecord> findChildBySlug(String slug, TreeRecord parent) {
    for (TreeRecord child : parent.children()) {
      if (child.slug().equals(slug))
        return Optional.of(child);

      Optional<TreeRecord> maybeFound = findChildBySlug(slug, child);
      if (maybeFound.isPresent())
        return maybeFound;
    }

    return Optional.empty();
  }

  // TODO: make this take a TreeRecord instead of a String
  public Optional<TreeRecord> find(String slug) {

    // if the node we're looking for is a root node, carry on as usual
    if (isRootNode(slug)) {
      return findNode(slug);
    } else {
      // otherwise, find the root node, retrieve its tree, pick out the node and return it
      return findChildBySlug(slug, findRootNodeInCache(slug));
    }
  }

  private Optional<TreeRecord> findInDbBySlug(String slug) {

    return jdbcTemplate.query(
        // TODO: make this a general query that can be customized with additional fields and joins etc
        Query.builder()
            .withRecursive(
                "tree",
                Query.builder()
                    .select(
                        List.of(
                            "t.id",
                            "t.parent_id",
                            "t.approved",
                            "t.created_on",
                            "t.author_id",
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
                            "t.parent_id",
                            "t.approved",
                            "t.created_on",
                            "t.author_id",
                            "t.published_on",
                            "t.title",
                            "t.body",
                            "t.slug",
                            "tree.path || t.id",
                            "tree.depth +1 as depth"
                        )
                    )
                    .from("page_trees t")
                    .join("tree on t.parent_id = tree.id"),
                Query.builder()
                    .select(
                        List.of(
                            "id",
                            "parent_id",
                            "approved",
                            "created_on",
                            "author_id",
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
                            "t.parent_id",
                            "t.approved",
                            "t.created_on",
                            "t.author_id",
                            "t.published_on",
                            "t.title",
                            "t.body",
                            "t.slug",
                            "array[id] as path",
                            "1 as depth"
                        )
                    )
                    .from("page_trees t")
                    .where("t.parent_id is null"),
                Query.builder()
                    .select(
                        List.of(
                            "t.id",
                            "t.parent_id",
                            "t.approved",
                            "t.created_on",
                            "t.author_id",
                            "t.published_on",
                            "t.title",
                            "t.body",
                            "t.slug",
                            "tree.path || t.id",
                            "tree.depth +1 as depth"
                        )
                    )
                    .from("page_trees t")
                    .join("tree on t.parent_id = tree.id"),
                Query.builder()
                    .select(
                        List.of(
                            "id",
                            "parent_id",
                            "approved",
                            "created_on",
                            "author_id",
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
                            "t.parent_id",
                            "t.approved",
                            "t.created_on",
                            "t.author_id",
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
                            "t.parent_id",
                            "t.approved",
                            "t.created_on",
                            "t.author_id",
                            "t.published_on",
                            "t.title",
                            "t.body",
                            "t.slug",
                            "tree.path || t.id",
                            "tree.depth +1 as depth"
                        )
                    )
                    .from("page_trees t")
                    .join("tree on t.id = tree.parent_id"),
                Query.builder()
                    .select(
                        List.of(
                            "id",
                            "parent_id",
                            "approved",
                            "created_on",
                            "author_id",
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

  // TODO: retrieve a tree's ancestors
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
    log.info("adding child {} to parentId {}", child.slug(), parent.slug());
    create(child.reparent(parent));
    return find(parent.slug()).orElseThrow(
        () -> new RuntimeException("Can't find parentId of child we just created for some reason"));
  }

}