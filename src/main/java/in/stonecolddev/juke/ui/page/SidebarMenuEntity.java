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
public class SidebarMenuEntity {
  private Integer id;
  private Integer layoutId;
  private String title;
  private String slug;
  private Boolean isEnabled;
  private List<SidebarMenuItemEntity> sidebarMenuItems;
  private OffsetDateTime createdOn;
  private OffsetDateTime updatedOn;
}