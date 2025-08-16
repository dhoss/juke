package in.stonecolddev.juke.ui.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
public class SidebarMenuItemEntity {
  private Integer id;
  private Integer sidebarMenuId;
  private String title;
  private String body;
  private OffsetDateTime createdOn;
  private OffsetDateTime updatedOn;
}