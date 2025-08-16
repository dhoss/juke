package in.stonecolddev.juke.blog;

import java.util.List;
import java.util.Optional;

public interface Persistence<T> {

  T save(T object);

  Optional<T> find(T object);

  List<T> list(Pager page);

  void deleteThread(T object);

  void bulkDeleteThreads(List<T> objects);
}