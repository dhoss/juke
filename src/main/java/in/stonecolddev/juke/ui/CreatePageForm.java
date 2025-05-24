package in.stonecolddev.juke.ui;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreatePageForm {

  private String title;
  private String body;
  private LocalDateTime publishedOn;

}