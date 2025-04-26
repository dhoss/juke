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
public class AuthorEntity {
    private Integer id;
    private String userName;
    private String email;
    private OffsetDateTime createdOn;
    private OffsetDateTime updatedOn;
    private OffsetDateTime publishedOn;
}