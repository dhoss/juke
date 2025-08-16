package in.stonecolddev.juke;

import in.stonecolddev.juke.ui.admin.AdminController;
import in.stonecolddev.juke.ui.HomeController;
import in.stonecolddev.juke.ui.page.PageController;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static in.stonecolddev.juke.util.Fixtures.Database.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
@ActiveProfiles("it-test")
@SpringBootTest
@Tag("it-test")
public class SmokeTest {


	@Autowired
	private HomeController homeController;

	@Autowired
	private PageController pageController;

	@Autowired
	private AdminController adminController;

	@BeforeAll
	public static void beforeAll() {
		startDatabase();
	}

	@Test
	public void contextLoads() {
		for (var controller : List.of(homeController, pageController, adminController)) {
			assertThat(controller).isNotNull();
		}

	}

}