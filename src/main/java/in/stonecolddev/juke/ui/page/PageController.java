package in.stonecolddev.juke.ui.page;


import in.stonecolddev.juke.data.storage.tree.TreeRecord;
import in.stonecolddev.juke.data.storage.tree.TreeStorageService;
import in.stonecolddev.juke.page.PageRecord;
import in.stonecolddev.juke.page.PageRecordBuilder;
import in.stonecolddev.juke.ui.view.View;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static in.stonecolddev.juke.page.PageRecord.generateSlug;

@Slf4j
@Controller
public class PageController {

  private final TreeStorageService<PageRecord> treeStorageService;

  private final Clock clock;

  public PageController(
      TreeStorageService<PageRecord> treeStorageService,
      Clock clock
  ) {
    this.treeStorageService = treeStorageService;
    this.clock = clock;
  }

  @GetMapping("/pages/{pageSlug}.html")
  public ModelAndView findPage(@PathVariable("pageSlug") String pageSlug) {
    return View.builder()
        .name("pages/page")
        .templateData(
            Map.of("page",
                treeStorageService.find(pageSlug).orElseThrow(
                    () -> new RuntimeException("No such page: " + pageSlug))))
        .toView();

  }

  // TODO: pagination
  @GetMapping("/pages")
  public ModelAndView listTreePages() {
    // ModelAndView mv = new ModelAndView("pages/list-pages");
    // mv.addAllObjects(Map.of("pages", treeStorageService.listTrees()));

    // return mv;

    return View.builder()
        .name("pages/list-pages")
        .templateData(Map.of("pages", treeStorageService.listTrees()))
        .toView();
  }

  @GetMapping("/admin/pages/new")
  public ModelAndView newPage() {
    // TODO: clean up
    //ModelAndView mv = new ModelAndView("admin/pages/new");

    //mv.addAllObjects(
    // Map.of(
    //     "page", new CreateOrEditPageForm(),
    //     "action", "/admin/pages/create"
    // )
    //);

    //return mv;

    return View.builder()
        .name("admin/pages/new")
        .templateData(
            Map.of(
                "page", new CreateOrEditPageForm(),
                "action", "/admin/pages/create"))
        .toView();
  }

  @GetMapping("/admin/pages/{slug}/add-child")
  public ModelAndView addChildPage(@PathVariable("slug") String pageSlug) {
    // TODO: clean up
    //ModelAndView mv = new ModelAndView("admin/pages/add-child");

    CreateOrEditPageForm pageForm = new CreateOrEditPageForm();
    pageForm.setParentSlug(pageSlug);
    //  mv.addAllObjects(
    //      Map.of(
    //          "page", pageForm,
    //          "pageData",
    //          treeStorageService.findInDbBySlug(pageSlug)
    //              .orElseThrow(
    //                  () -> new PageNotFoundException(
    //                      "Page not found with slug " + pageSlug)),
    //          // TODO: create helper to create form action urls
    //          "action", "/admin/pages/" + pageSlug + "/add-child/save"
    //      )
    //  );

    //  return mv;
    return View.builder()
        .name("admin/pages/add-child")
        .templateData(
            Map.of(
                "page", pageForm,
                "pageData",
                treeStorageService.findInDbBySlug(pageSlug)
                    .orElseThrow(
                        () -> new PageNotFoundException(
                            "Page not found with slug " + pageSlug)),
                // TODO: create helper to create form action urls
                "action", "/admin/pages/" + pageSlug + "/add-child/save"
            ))
        .toView();
  }

  @PostMapping("/admin/pages/{slug}/add-child/save")
  public ModelAndView saveAddChildPage(
      @PathVariable("slug") String pageSlug,
      @ModelAttribute CreateOrEditPageForm pageFormData,
      RedirectAttributes redirectAttributes) {

    pageFormData.setParentSlug(pageSlug);
    PageRecord childPage = (PageRecord) treeStorageService.addChild(
        treeStorageService.findInDbBySlug(pageSlug)
            .orElseThrow(
                () -> new PageNotFoundException(
                    "Page not found with slug " + pageSlug)),
        mapPageFormToPageRecord(pageFormData));

    redirectAttributes.addFlashAttribute("formSubmissionMessage", "Page saved");
    return new ModelAndView("redirect:/admin/pages/" + childPage.slug() + "/edit");
  }

  @GetMapping("/admin/pages/{slug}/edit")
  public ModelAndView editPage(@PathVariable("slug") String pageSlug) {
    ModelAndView mv = new ModelAndView("admin/pages/edit");

    mv.addAllObjects(
        Map.of(
            "page",
            mapPageRecordToPageForm(
                (PageRecord) treeStorageService.findInDbBySlug(pageSlug)
                    .orElseThrow(
                        () -> new PageNotFoundException(
                            "Page not found with slug " + pageSlug))),
            // TODO: create helper to create form action urls
            "action", "/admin/pages/" + pageSlug + "/edit/save"
        )
    );

    return mv;
  }

  @PostMapping("/admin/pages/{slug}/edit/save")
  public ModelAndView savePageEdits(
      @PathVariable("slug") String pageSlug,
      @ModelAttribute CreateOrEditPageForm pageFormData,
      RedirectAttributes redirectAttributes) {

    pageFormData.setSlug(pageSlug);
    treeStorageService.update(mapPageFormToPageRecord(pageFormData));

    redirectAttributes.addFlashAttribute("formSubmissionMessage", "Page saved");
    return new ModelAndView("redirect:/admin/pages/" + pageSlug + "/edit");
  }

  // TODO: Add test for creating a page
  // TODO: validate title before submission so existing slugs etc can't be sent in the request (see create() in DefaultPageHandler)
  @PostMapping("/admin/pages/create")
  public ModelAndView createPage(
      @ModelAttribute CreateOrEditPageForm pageFormData,
      RedirectAttributes redirectAttributes) {

    // TODO: validation/error handling
    treeStorageService.create(mapPageFormToPageRecord(pageFormData));
    redirectAttributes.addFlashAttribute("formSubmissionMessage", "Page created");

    return new ModelAndView("redirect:/admin/pages");
  }

  @GetMapping("/admin/pages")
  public ModelAndView listPages() {
    // TODO: pages listing needs to display dates nicely
    // TODO: pages listing should allow for sorting by dates
    // TODO: pages listing should allow pagination
    ModelAndView mv = new ModelAndView("admin/pages/list");

    mv.addObject("pages", treeStorageService.listTrees());

    return mv;
  }

  private PageRecord mapPageFormToPageRecord(CreateOrEditPageForm pageForm) {
    return PageRecordBuilder.builder()
        .parent(
            Optional.of(
                PageRecordBuilder.builder()
                    .slug(maybeParentSlug(pageForm.getParentSlug()))
                    .build()))
        .slug(findOrCreateSlug(pageForm))
        .title(pageForm.getTitle())
        .body(pageForm.getBody())
        .approved(false)
        // TODO: FIX THIS
        .authorId(1)
        .publishedOn(
            OffsetDateTime.from(
                pageForm.getPublishedOn()
                    .atZone(clock.getZone())))
        .build();
  }

  private String maybeParentSlug(String maybeSlug) {
    return maybeString(maybeSlug).orElseGet(() -> "");
  }

  private String findOrCreateSlug(CreateOrEditPageForm pageForm) {
    return maybeString(pageForm.getSlug())
        .orElseGet(
            () -> generateSlug(pageForm.getTitle(), OffsetDateTime.now()));
  }

  private String maybeParentSlug(Optional<TreeRecord> maybeParent) {
    return maybeParent.map(TreeRecord::slug).orElseGet(() -> "");
  }

  private Optional<String> maybeString(String maybe) {
    return Optional.ofNullable(maybe)
        .filter(s -> !s.isBlank());

  }

  private CreateOrEditPageForm mapPageRecordToPageForm(PageRecord page) {
    return CreateOrEditPageForm.builder()
        .approved(page.approved())
        .body(page.body())
        .children(
            page.children()
                .stream()
                .map(TreeRecord::slug)
                .collect(Collectors.toSet()))
        .parentSlug(maybeParentSlug(page.parent()))
        .publishedOn(
            // TODO: this sucks
            LocalDateTime.parse(
                page.publishedOn()
                    .atZoneSameInstant(clock.getZone())
                    .toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))))
        .slug(page.slug())
        .title(page.title())
        .build();
  }
}