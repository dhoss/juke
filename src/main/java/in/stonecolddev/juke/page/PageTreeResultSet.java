package in.stonecolddev.juke.page;

import in.stonecolddev.juke.data.storage.tree.DatabaseTreeConfiguration;
import in.stonecolddev.juke.data.storage.tree.TreeResultSet;
import io.soabase.recordbuilder.core.RecordBuilder;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@Component
@RecordBuilder
public record PageTreeResultSet(
    DatabaseTreeConfiguration configuration
) implements TreeResultSet<PageRecord> {

  public PageRecord fromResultSet(ResultSet rs) throws SQLException {
    PageRecordBuilder pageRecord = PageRecordBuilder.builder();
    pageRecord.id(rs.getInt(configuration.idColumn()));
    pageRecord.slug(rs.getString(configuration.whereColumn()));
    pageRecord.title(rs.getString("title"));
    pageRecord.body(rs.getString("body"));
    pageRecord.author(rs.getInt("author"));
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