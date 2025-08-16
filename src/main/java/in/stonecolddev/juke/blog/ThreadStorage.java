package in.stonecolddev.juke.blog;

import java.util.Optional;

public interface ThreadStorage<T, U> {

  T newThread(T thread);

  T addReply(U reply);

  Optional<T> retrieveThread(T thread);

  void deletePost(U post);

  void deleteThread(T thread);

  void approveReply(U post);

}