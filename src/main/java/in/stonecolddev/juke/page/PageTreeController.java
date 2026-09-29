package in.stonecolddev.juke.page;

import in.stonecolddev.juke.data.storage.tree.TreeRecord2;
import in.stonecolddev.juke.data.storage.tree.TreeStorageService2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/v1/pages")
public class PageTreeController {

  // TODO: implement PageTreeService that converts PageRecord -> PageDTO or something
  private final TreeStorageService2<PageRecord2> pageTreeService;

  public PageTreeController(
      TreeStorageService2<PageRecord2> pageTreeService
  ) {
    this.pageTreeService = pageTreeService;
  }

  // TODO: we should be able to pass a full tree path and drill down into the subpages here
  //       e.g: /root/child/subchild/subsubchild etc
  @GetMapping(value = "/{pageSlug}", produces = APPLICATION_JSON_VALUE)
  public ResponseEntity<TreeRecord2> find(@PathVariable String pageSlug) {
    return pageTreeService.find(pageSlug)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

}