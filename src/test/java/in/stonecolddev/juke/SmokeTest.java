package in.stonecolddev.juke;

import in.stonecolddev.juke.ui.AdminController;
import in.stonecolddev.juke.ui.HomeController;
import in.stonecolddev.juke.ui.PageController;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static in.stonecolddev.juke.util.Fixtures.Database.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles("it-test")
@SpringBootTest
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

	@AfterAll
	public static void afterAll() {
		stopDatabase();
	}

	@Test
	public void contextLoads() {
		for ( var controller : List.of(homeController, pageController, adminController)) {
			assertThat(controller).isNotNull();
		}

	}

}