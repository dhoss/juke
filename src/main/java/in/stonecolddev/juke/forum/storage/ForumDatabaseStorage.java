package in.stonecolddev.juke.forum.storage;

import in.stonecolddev.juke.forum.resreq.*;
import in.stonecolddev.juke.forum.schema.ForumEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ForumDatabaseStorage implements ForumStorage {

  private final DatabaseHandler databaseHandler;

  public ForumDatabaseStorage(
      DatabaseHandler databaseHandler
  ) {
    this.databaseHandler = databaseHandler;
  }

  @Override
  public ListForumResponse listForums(ListForumRequest listForumRequest) {
    return databaseHandler.runQuery(
        "select * from forums",
        Map.of(),
        rs -> {
          List<ForumEntity> forums = new ArrayList<>();

          return ListForumResponse.builder().forums(forums).build();
        }
    );
  }

  @Override
  public FindForumResponse findForum(FindForumRequest findForumRequest) {
    return null;
  }

  @Override
  public CreateForumResponse createForum(CreateForumRequest createForumRequest) {
    return null;
  }

  @Override
  public DeleteForumResponse deleteForum(DeleteForumRequest deleteForumRequest) {
    return null;
  }

  @Override
  public UpdateForumResponse updateForum(UpdateForumRequest updateForumRequest) {
    return null;
  }

  @Override
  public ListThreadsResponse listThreads(ListThreadsRequest listThreadRequest) {
    return null;
  }

  @Override
  public FindThreadResponse findThread(FindThreadRequest findThreadRequest) {
    return null;
  }

  @Override
  public CreateThreadResponse createThread(CreateThreadRequest createThreadRequest) {
    return null;
  }

  @Override
  public DeleteThreadResponse deleteThread(DeleteThreadRequest deleteThreadRequest) {
    return null;
  }

  @Override
  public UpdateThreadResponse updateThread(UpdateThreadRequest updateThreadRequest) {
    return null;
  }
}