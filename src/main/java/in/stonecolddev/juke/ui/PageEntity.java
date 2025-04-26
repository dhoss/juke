package in.stonecolddev.juke.ui;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageEntity {
    private Integer id;
    private AuthorEntity author;
    private String title;
    private Map<String, PageComponentEntity> pageComponents;
    // TODO: isDeleted, createdOn etc should go in an interface and table entities should extend it
    private Boolean isDeleted;
    private String slug;
    private OffsetDateTime createdOn;
    private OffsetDateTime updatedOn;
    private OffsetDateTime publishedOn;
}