package in.stonecolddev.juke.forum;

import in.stonecolddev.juke.forum.resreq.*;

public interface ForumThreadStorage {

  ListForumResponse listForums(ListForumResquest listForumResquest);

  FindForumResponse findForum(FindForumRequest findForumRequest);

  CreateForumResponse createForum(CreateForumRequest createForumRequest);

  DeleteForumResponse deleteForum(DeleteForumRequest deleteForumRequest);

  UpdateForumResponse updateForum(UpdateForumRequest updateForumRequest);

}