package in.stonecolddev.juke.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrEditUserForm {

  private String userName;
  private String password;
  private String passwordConfirm;
  private String email;

}