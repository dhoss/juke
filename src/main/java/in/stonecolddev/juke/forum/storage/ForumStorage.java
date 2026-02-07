package in.stonecolddev.juke.forum.storage;

import in.stonecolddev.juke.forum.resreq.*;

public interface ForumStorage {

  ListForumResponse listForums(ListForumRequest listForumRequest);

  FindForumResponse findForum(FindForumRequest findForumRequest);

  CreateForumResponse createForum(CreateForumRequest createForumRequest);

  DeleteForumResponse deleteForum(DeleteForumRequest deleteForumRequest);

  UpdateForumResponse updateForum(UpdateForumRequest updateForumRequest);

  ListThreadsResponse listThreads(ListThreadsRequest listThreadRequest);

  FindThreadResponse findThread(FindThreadRequest findThreadRequest);

  CreateThreadResponse createThread(CreateThreadRequest createThreadRequest);

  DeleteThreadResponse deleteThread(DeleteThreadRequest deleteThreadRequest);

  UpdateThreadResponse updateThread(UpdateThreadRequest updateThreadRequest);

}