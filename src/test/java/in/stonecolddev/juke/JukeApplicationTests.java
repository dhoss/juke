package in.stonecolddev.juke;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import static in.stonecolddev.juke.util.DbMigrations.initDb;

@ActiveProfiles("it-test")
@SpringBootTest
class JukeApplicationTests {

	static String username = "juke";
	static String password = "juke";
	static String databaseName = "juke";

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
			.withUsername(username)
			.withPassword(password)
			.withDatabaseName(databaseName);


	@BeforeAll
	static void beforeAll() {
		postgres.start();
		System.setProperty("spring.datasource.hikari.jdbc-url", postgres.getJdbcUrl());
		System.setProperty("spring.datasource.hikari.username", postgres.getUsername());
		System.setProperty("spring.datasource.hikari.password", postgres.getPassword());


		initDb(postgres.getJdbcUrl(), username, password);
	}

	@AfterAll
	static void afterAll() {
		postgres.stop();
	}

	@Test
	void contextLoads() {
	}

}
