package in.stonecolddev.juke.blog;

import java.util.List;

public interface Thread<T> {

  T newThread(T post);

  T addReply(T parent, T reply);

  List<T> retrieveThread(T post);

  void deletePost(T post);

  List<T> deleteThread(T post);

  void approveReply(T post);

}