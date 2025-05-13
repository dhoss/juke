package in.stonecolddev.juke.ui;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageComponentEntity {
    private Integer id;
    private AuthorEntity author;
    private String title;
    private String body;
    private String slug;
    private PageComponent.ComponentType type;
    private Boolean isDeleted;
    private OffsetDateTime createdOn;
    private OffsetDateTime updatedOn;
    private OffsetDateTime publishedOn;
}