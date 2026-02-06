package in.stonecolddev.juke.forum;

public interface ForumStorage {

  ListForumResponse listForums(ListForumResquest listForumResquest);

  FindForumResponse findForum(FindForumRequest findForumRequest);

  CreateForumResponse createForum(CreateForumRequest createForumRequest);

  DeleteForumResponse deleteForum(DeleteForumRequest deleteForumRequest);

  UpdateForumResponse updateForum(UpdateForumRequest updateForumRequest);

}