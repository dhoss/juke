package in.stonecolddev.juke.ui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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