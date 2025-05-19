package in.stonecolddev.juke.ui;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

// TODO: figure out if we actually need this
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageComponentToPageMappingEntity {
    private Integer id;
    private Integer pageId;
    private Integer pageComponentId;
    private OffsetDateTime createdOn;
    private OffsetDateTime updatedOn;
    private OffsetDateTime publishedOn;
}