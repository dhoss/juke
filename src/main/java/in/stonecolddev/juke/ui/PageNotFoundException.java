package in.stonecolddev.juke.ui;

public class PageNotFoundException extends RuntimeException {
  public PageNotFoundException(String message) {
    super(message);
  }
}