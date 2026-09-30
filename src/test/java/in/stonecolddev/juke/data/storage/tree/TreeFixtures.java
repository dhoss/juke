package in.stonecolddev.juke.data.storage.tree;

import in.stonecolddev.juke.page.PageRecord;
import in.stonecolddev.juke.page.PageRecordBuilder;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TreeFixtures {

  public static final OffsetDateTime now =
      OffsetDateTime.parse(
              "2026-09-06 16:14:20 -0600",
              DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z"))
          .atZoneSameInstant(ZoneId.of("UTC"))
          .toOffsetDateTime();

  public static final PageRecord root = PageRecordBuilder.builder()
      .id(1)
      .author(1)
      .title("test root page")
      .slug("test-root-page")
      .body("test root page body")
      .path(List.of(1))
      .depth(1)
      .approved(true)
      .createdOn(now)
      .publishedOn(now)
      .build();

  public static final PageRecord firstChild = PageRecordBuilder.builder()
      .id(2)
      .author(1)
      .title("test root page first child page")
      .slug("test-root-page-first-child-page")
      .body("test root page first child page body")
      .path(List.of(1, 2))
      .depth(2)
      .parent(Optional.of(root.id()))
      .approved(true)
      .createdOn(now)
      .publishedOn(now)
      .build();

  public static final PageRecord firstChildFirstChild = PageRecordBuilder.builder()
      .id(3)
      .author(1)
      .title("test root page first child page first child")
      .slug("test-root-page-first-child-page-first-child")
      .body("test root page first child page body first child")
      .path(List.of(1, 2, 3))
      .depth(3)
      .parent(Optional.of(firstChild.id()))
      .approved(true)
      .createdOn(now)
      .publishedOn(now)
      .build();

  public static final PageRecord firstChildSecondChild = PageRecordBuilder.builder()
      .id(4)
      .author(1)
      .title("test root page first child page second child")
      .slug("test-root-page-first-child-page-second-child")
      .body("test root page first child page body second child")
      .path(List.of(1, 2, 4))
      .depth(3)
      .parent(Optional.of(firstChild.id()))
      .approved(true)
      .createdOn(now)
      .publishedOn(now)
      .build();

  public static final PageRecord fullTree =
      root.withChildren(
          Set.of(
              firstChild.withChildren(
                  Set.of(
                      firstChildFirstChild,
                      firstChildSecondChild))));

}
