package in.stonecolddev.juke.data.storage.tree;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RecordBuilder
record PageRecord(
    Integer id,
    Integer author,
    String title,
    String slug,
    String body,
    Optional<Integer> parent,
    List<Integer> path,
    Integer depth,
    Boolean approved,
    OffsetDateTime createdOn,
    OffsetDateTime publishedOn) implements TreeRecord, PageRecordBuilder.With {

  public static PageRecord fromResultSet(
      ResultSet rs, DatabaseTreeConfiguration databaseTreeConfig) throws SQLException {
    PageRecordBuilder pageRecord = PageRecordBuilder.builder();
    pageRecord.id(rs.getInt(databaseTreeConfig.idColumn()));
    pageRecord.slug(rs.getString(databaseTreeConfig.whereColumn()));
    pageRecord.title(rs.getString("title"));
    pageRecord.body(rs.getString("body"));
    // TODO: FIX ME
    pageRecord.author(1);
    pageRecord.path(
        new ArrayList<>(
            Arrays.asList(
                (Integer[]) rs.getArray("path")
                    .getArray())));
    pageRecord.depth(rs.getInt("depth"));
    pageRecord.approved(rs.getBoolean("approved"));
    pageRecord.createdOn(
        rs.getObject("created_on", OffsetDateTime.class)
            .truncatedTo(ChronoUnit.SECONDS));
    pageRecord.publishedOn(
        rs.getObject("published_on", OffsetDateTime.class)
            .truncatedTo(ChronoUnit.SECONDS));

    // rs.getInt(...) will return 0 if the column value is null so we have to do this
    int parentId = rs.getInt("parent");
    if (parentId != 0) {
      pageRecord.parent(Optional.of(parentId));
    }

    return pageRecord.build();
  }
}