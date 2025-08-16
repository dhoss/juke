package in.stonecolddev.juke.blog;

import java.util.List;

public interface Persistence<T> {

  T save(T post);

  T find(T post);

  List<T> list(Pager page);

  void delete(T post);

  List<T> bulkDelete(List<T> posts);
}