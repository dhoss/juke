package in.stonecolddev.juke.ui.page;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
public class PageEntity {
    private Integer id;
    private AuthorEntity author;
    private String title;
    private List<SidebarMenuEntity> sidebarMenus;
    private String body;
    // TODO: isDeleted, createdOn etc should go in an interface and table entities should extend it
    private Boolean isDeleted;
    private String slug;
    private OffsetDateTime createdOn;
    private OffsetDateTime updatedOn;
    private OffsetDateTime publishedOn;

    public void setPublishedOn(OffsetDateTime dt) {
        this.publishedOn(dt);
    }
}