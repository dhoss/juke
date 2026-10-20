package in.stonecolddev.juke.data.storage.tree;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import in.stonecolddev.juke.page.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.stringtemplate.v4.ST;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static in.stonecolddev.juke.data.storage.tree.DatabaseTree.findRootId;

@Slf4j
// TODO: this shouldn't be a component
//       instantiate or extend it from a specific implementation, PageTreeStorage etc
@Component
// TODO: rename this
public class TreeStorageService<T extends TreeRecord> {

  // TODO: CLEANUP

  private final NamedParameterJdbcTemplate jdbcTemplate;

  private final DatabaseTreeConfiguration configuration;

  private final TreeResultSet<T> treeResultSet;

  private final Map<String, TreeRecord> treeCache = new HashMap<>();

  private final LoadingCache<String, TreeRecord> treeCacheCaffeine;

  private final LoadingCache<Integer, NodeInfo> nodeInfoCacheCaffeine;

  private final LoadingCache<String, NodeInfo> nodeInfoCacheSlugIndexCaffeine;

  private final LoadingCache<Integer, Optional<Integer>> nodeToParentMappingCaffeine;

  private final LoadingCache<String, Boolean> rootNodeCache;

  private final TransactionTemplate transactionTemplate;

  public TreeStorageService(
      DatabaseTreeConfiguration configuration,
      NamedParameterJdbcTemplate jdbcTemplate,
      TreeResultSet<T> treeResultSet,
      TransactionTemplate transactionTemplate
  ) {
    this.configuration = configuration;
    this.jdbcTemplate = jdbcTemplate;
    this.transactionTemplate = transactionTemplate;
    this.treeResultSet = treeResultSet;

    // TODO: we need a cache wrapper

    // TODO: move common code to method
    this.nodeInfoCacheCaffeine =
        Caffeine.newBuilder().build(
            CacheLoader.bulk(
                keys -> {
                  Map<Integer, NodeInfo> nodes = new HashMap<>();
                  for (Integer id : keys) {
                    nodes.put(id, findNodeInfoById(id));
                  }
                  return nodes;
                }
            )
        );
    this.nodeInfoCacheSlugIndexCaffeine =
        Caffeine.newBuilder().build(
            CacheLoader.bulk(
                keys -> {
                  Map<String, NodeInfo> nodes = new HashMap<>();
                  for (String slug : keys) {
                    nodes.put(slug, findNodeInfoBySlug(slug));
                  }
                  return nodes;
                }
            )
        );
    this.nodeToParentMappingCaffeine =
        Caffeine.newBuilder()
            .build(
                CacheLoader.bulk(
                    keys -> {
                      Map<Integer, Optional<Integer>> parentMapping = new HashMap<>();

                      for (Integer id : keys) {
                        parentMapping.put(id, findNodeInfoById(id).parentId());
                      }

                      return parentMapping;
                    }));

    // TODO: move common code to method
    this.rootNodeCache =
        Caffeine.newBuilder()
            .build(key -> findNodeInfoBySlug(key).parentId().isEmpty());

    this.treeCacheCaffeine =
        Caffeine.newBuilder()
            .build(
                CacheLoader.bulk(
                    keys -> {
                      Map<String, TreeRecord> trees = new HashMap<>();
                      for (String key : keys) {
                        trees.put(key, findInDbBySlug(key)
                            .orElseThrow(
                                () -> new RuntimeException(
                                    "No such tree with slug " + key)));
                      }
                      return trees;

                    }));

  }

  // we want to track a few things about our node relationships
  // for certain cases that don't warrant building out or retrieving full TreeRecords
  record NodeInfo(Integer id, Optional<Integer> parentId, String slug) {
  }

  // TODO: move common code
  private NodeInfo findNodeInfoById(Integer id) {
    // load a simple mapping of a node's id to its parent's id and its slug
    // this allows for a simple cache look up for child nodes instead of the rigamarole of db calls every time
    log.info("Loading node info for id {}", id);
    return jdbcTemplate.queryForObject(
        Query.builder()
            .select(List.of("id", "parent_id", "slug"))
            .from("page_trees")
            .where("id = :id")
            .compile(),
        new MapSqlParameterSource().addValue("id", id),
        (rs, _) ->
            new NodeInfo(
                rs.getInt("id"),
                // TODO: move this to common method
                Optional.of(rs.getInt("parent_id"))
                    .filter(i -> !i.equals(0)),
                rs.getString("slug")));
  }

  // TODO: move common code
  private NodeInfo findNodeInfoBySlug(String slug) {
    // load a simple mapping of a node's id to its parent's id and its slug
    // this allows for a simple cache look up for child nodes instead of the rigamarole of db calls every time
    log.info("Loading node info for slug {}", slug);
    return jdbcTemplate.queryForObject(
        Query.builder()
            .select(List.of("id", "parent_id", "slug"))
            .from("page_trees")
            .where("slug = :slug")
            .compile(),
        new MapSqlParameterSource().addValue("slug", slug),
        (rs, _) ->
            new NodeInfo(
                rs.getInt("id"),
                // TODO: move this to common method
                Optional.of(rs.getInt("parent_id"))
                    .filter(i -> !i.equals(0)),
                rs.getString("slug")));
  }

  public Optional<TreeRecord> find(TreeRecord tree) {
    return find(tree.slug());
  }

  private boolean isRootNode(String slug) {
    // TODO: move common code to method
    return rootNodeCache.get(
        slug, key -> findNodeInfoBySlug(key).parentId().isEmpty());
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

  // TODO: this sucks rewrite it
  private Optional<TreeRecord> findRootBySlug(String slug) {
    log.info("**** NODEINFOCACHESLUGINDEX {}", nodeInfoCacheSlugIndexCaffeine.asMap());
    NodeInfo nodeInfo = nodeInfoCacheSlugIndexCaffeine//.asMap()
        .get(slug);
    log.info("**** NODEINFOCACHESLUGINDEX AFTER {}", nodeInfoCacheSlugIndexCaffeine.asMap());
    log.info("**** NODETOPARENTMAPPING {}", nodeToParentMappingCaffeine.asMap());
    // TODO: there should be a better place to do this
    // if (nodeToParentMappingCaffeine.asMap().isEmpty()) {
    // log.info("nodeToParentMapping is empty, loading");
    var maybeParent = nodeToParentMappingCaffeine.get(nodeInfo.id());
    log.info("NODETOPARENTAPPINGAFTER LOAD {}", nodeToParentMappingCaffeine.asMap());
    // TODO: recurse up to to the root, first level children are the only ones being populated right now
    while (maybeParent.isPresent()) {

      log.info("loading parent {} for {}", maybeParent.get(), nodeInfo.id());
      maybeParent = Optional.ofNullable(nodeToParentMappingCaffeine.get(maybeParent.get())).flatMap(p -> p);

      log.info("NODETOPARENTAPPINGAFTER PARENT LOAD {}", nodeToParentMappingCaffeine.asMap());
    }
    // if (maybeParent.isPresent()) {
    //   log.info("loading parent {} for {}", maybeParent.get(), nodeInfo.id());
    //   nodeToParentMappingCaffeine.get(maybeParent.get());

    //   log.info("NODETOPARENTAPPINGAFTER PARENT LOAD {}", nodeToParentMappingCaffeine.asMap());
    // }
    // }
    log.info("**** NODETOPARENTMAPPING AFTER {}", nodeToParentMappingCaffeine.asMap());
    return Optional.ofNullable(
        treeCacheCaffeine//.asMap()
            .get(
                nodeInfoCacheCaffeine//.asMap()
                    .get(
                        findRootId(
                            nodeInfo
                                .id(),
                            nodeToParentMappingCaffeine.asMap())
                    ).slug()
            ));
  }

  // TODO: memoize this
  private Optional<TreeRecord> findChildBySlug(String slug, TreeRecord parent) {
    // TODO: move common code to a method
    log.info("*** SLUG IN FINDCHILDBYSLUG {}", slug);
    // treeCacheCaffeine.get(parent.slug(), key -> findInDbBySlug(key).orElseThrow(() -> new RuntimeException("Can't find node by slug " + key)));
    log.info("**** FOUND CACHEDPARENT IN FINDCHILDBYSLUG {}", parent.slug());
    log.info("**** CHILDREN OF CACHEDPARENT ({}) IN FINDCHILDBYSLUG {}", parent.slug(), parent.children().stream().map(c -> c.slug()).collect(Collectors.toSet()));
    // if the parent record passed in contains no children, just return the child record itself
    if (parent.children().isEmpty())
      return Optional.of(treeCacheCaffeine.get(slug));
    //return Optional.of(treeCacheCaffeine.asMap().get(slug));
    for (TreeRecord child : parent.children()) {
      log.info("**** CHILD IN LOOP FINDCHILDBYSLUG {}", child.slug());
      if (child.slug().equals(slug))
        return Optional.of(child);

      Optional<TreeRecord> maybeFound = findChildBySlug(slug, child);
      if (maybeFound.isPresent())
        return maybeFound;
    }

    return Optional.empty();
  }

  // TODO: implement this: https://github.com/ben-manes/caffeine/tree/master/examples/indexable
  // TODO: make this take a TreeRecord instead of a String
  public Optional<TreeRecord> find(String slug) {

    // if the node we're looking for is a root node, carry on as usual
    if (isRootNode(slug)) {
      log.info("**** IS ROOT NODE INSIDE FIND {}", slug);
      return findNode(slug);
    } else {
      log.info("**** IS NOT ROOT NODE INSIDE FIND {}", slug);
      // otherwise, find the root node, retrieve its tree, pick out the node and return it
      TreeRecord parent = findRootBySlug(slug).orElseThrow(() -> new RuntimeException("Can't find root for slug " + slug));
      log.info("**** PARENT INSIDE FIND {}", parent.slug());
      return findChildBySlug(slug, parent);//findRootNodeInCache(slug));
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

  // TODO: I'm not sure we need this
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
    //  log.info("**** INSERT QUERY {}",
    //      Query.builder().insert(configuration.treeTable()).values(Map.of("title", "fart", "body", "fart body")).compile());
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
    String slug = tree.slug();
    TreeRecord createdTree = transactionTemplate.execute(status -> {
      // what does status do in the execute lambda
      // In the execute or executeWithoutResult lambda expression, the status object is an instance of TransactionStatus. It acts as your handle to control the current transaction and check its state while it is running.
      //Because the code inside the lambda is managed programmatically rather than by annotations, you use this object to explicitly talk to the underlying transaction manager.
      //Here are the primary roles that status fulfills:
      //
      //1. Triggering a Manual Rollback
      //
      //By default, the TransactionTemplate automatically rolls back only if an unhandled RuntimeException or Error escapes the lambda block. If you catch a checked exception or encounter a business logic failure and want to abort, you tell the transaction manager via status:
      //java
      //transactionTemplate.executeWithoutResult(status -> {
      //    try {
      //        paymentService.chargeCard();
      //        orderRepository.markPaid();
      //    } catch (PaymentFailedException e) {
      //        // Keeps the app from crashing, but safely rolls back the database changes
      //        status.setRollbackOnly();
      //    }
      //});
      //Use code with caution.
      //
      //2. Checking the Transaction State
      //
      //You can inspect the status object to make decisions based on the current state of the database transaction:
      //• status.isNewTransaction(): Returns true if this lambda block actually started a brand-new database transaction, or false if it joined an already existing one.
      //• status.isRollbackOnly(): Returns true if the transaction has already been marked for rollback (either by another step in the code or a previous error).
      //• status.isCompleted(): Returns true if the transaction has already been committed or rolled back.
      //
      //3. Creating Savepoints (Nested Transactions)
      //
      //If your underlying transaction manager supports it (like JDBC or Hibernate with specific configurations), you can use status to create points to roll back to without aborting the whole transaction.
      //java
      //transactionTemplate.executeWithoutResult(status -> {
      //    // Action A (Main action)
      //    userRepository.updateProfile(user);
      //
      //    // Create a savepoint before attempting an optional action
      //    Object savepoint = status.createSavepoint();
      //
      //    try {
      //        // Action B (Optional action)
      //        notificationRepository.logAuditTrail();
      //    } catch (Exception e) {
      //        // Only undoes Action B; Action A will still commit!
      //        status.rollbackToSavepoint(savepoint);
      //    }
      //});
      log.info("child count for node {}: {}", slug, tree.children().size());
      log.info("inserting node {}", slug);
      // TODO: maybe make this a method itself
      // TODO: we probably want to return the tree from here
      return treeCacheCaffeine.asMap().compute(
          slug, (key, oldTreeValue) -> {
            log.info("Updating cache for key {}", slug);
            jdbcTemplate.update(
                queryTemplate.render(),
                new MapSqlParameterSource().addValues(tree.valueMap())
            );

            Integer id = tree.id();
            Optional<Integer> parentId = tree.parentId();
            TreeRecord treeRecord = findInDbBySlug(key).orElseThrow(() -> new RuntimeException("Can't find tree inside cache write through with slug " + slug));
            log.info("**** TREE JUST INSERTED {} -> {}", treeRecord.slug(), treeRecord.children().stream().map(c -> c.slug()).collect(Collectors.joining(",")));

            log.info("**** TREE PASSED IN {} -> {}", tree.slug(), tree.children().stream().map(c -> c.slug()).collect(Collectors.joining(",")));
            return treeRecord;
          });
    });
    log.info("refreshing caches after insert");
    try {
      nodeInfoCacheCaffeine.refresh(createdTree.id()).get(); //.put(id, nodeInfo);
      nodeInfoCacheSlugIndexCaffeine.refresh(createdTree.slug()).get();//.put(slug, nodeInfo);
      nodeToParentMappingCaffeine.refresh(createdTree.id()).get(); //.put(id, parentId);
      rootNodeCache.refresh(createdTree.slug()).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException("error refreshing cache: " + e);
    }
    log.info("adding child nodes if they exist");
    for (TreeRecord child : tree.children()) {
      //for (TreeRecord child : tree.children()) {
      addChild(tree, child);
    }

    return find(tree.slug())//slug)
        //  return treeCacheCaffeine.get(tree.slug(), key -> findInDbBySlug(key).orElseThrow(() -> new RuntimeException("Can't find tree with slug " + key)));
        .orElseThrow(
            () -> new RuntimeException(
                "Can't find tree we just inserted with slug " + slug));
  }

  // TODO: wrap this in a transaction
  public TreeRecord addChild(TreeRecord parent, TreeRecord child) {
    log.info("adding child {} to parentId {}", child.slug(), parent.slug());
    log.info("**** BEFORE CREATE IN ADDCHILD");
    create(child.reparent(parent));
    log.info("refreshing tree in cache, parent slug: {}", parent.slug());
    // TODO: figure out something better than catch and rethrow
    Optional<TreeRecord> refreshed = Optional.empty();
    try {
      refreshed = Optional.of(treeCacheCaffeine.refresh(parent.slug()).get());
      log.info("refresh complete for {}", refreshed.map(r -> r.slug()));
      log.info("**** REFRESHED CHILDREN {}", refreshed.map(mc -> mc.children().stream().map(c -> c.slug()).collect(Collectors.joining(","))));
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException("Error refreshing cache entry for slug " + parent.slug() + ": " + e);
    }
    log.info("**** AFTER CREATE IN ADDCHILD");
    return refreshed
        //  return find(parent.slug())
        .orElseThrow(
            () -> new RuntimeException("Can't find parentId of child we just created for some reason"));
  }

}