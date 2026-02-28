package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@SpringBootTest
class FilmorateApplicationTests {

	@Test
	void userControllerCreateTest() {
		UserController userController = new UserController();
		User user1 = new User();
		user1.setName("Андрей");
		user1.setLogin("shark123");
		user1.setEmail("pochta12@gmail.com");
		user1.setBirthday(LocalDate.of(2000, 1, 24));

		User createdUser = userController.create(user1);
		Assertions.assertEquals(user1.getName(), createdUser.getName());
		Assertions.assertEquals(user1.getLogin(), createdUser.getLogin());
		Assertions.assertEquals(user1.getEmail(), createdUser.getEmail());
		Assertions.assertEquals(user1.getBirthday(), createdUser.getBirthday());
	}

	@Test
	void userControllerValidationDateCreateTestFail() {
		UserController userController = new UserController();
		User user1 = new User();
		user1.setName("Андрей");
		user1.setLogin("shark123");
		user1.setEmail("pochta12@gmail.com");
		user1.setBirthday(LocalDate.of(2027, 1, 24));

		Assertions.assertThrows(ValidationException.class, () -> userController.create(user1));
	}

	@Test
	void userControllerNameIsLoginTest() {
		UserController userController = new UserController();
		User user1 = new User();
		user1.setLogin("shark123");
		user1.setEmail("pochta12@gmail.com");
		user1.setBirthday(LocalDate.of(2000, 1, 24));

		User createdUser = userController.create(user1);
		Assertions.assertEquals("shark123", createdUser.getName());
	}

	@Test
	void userControllerValidationEmailCreateTestFail() {
		UserController userController = new UserController();
		User user1 = new User();
		user1.setName("Андрей");
		user1.setLogin("shark123");
		user1.setEmail("pochta12 @gmail.com");
		user1.setBirthday(LocalDate.of(2027, 1, 24));
		User user2 = new User();
		user2.setName("Андрей");
		user2.setLogin("shark123");
		user2.setBirthday(LocalDate.of(2027, 1, 24));
		User user3 = new User();
		user3.setName("Андрей");
		user3.setLogin("shark123");
		user3.setEmail("pochta12gmail.com");
		user3.setBirthday(LocalDate.of(2027, 1, 24));

		Assertions.assertThrows(ValidationException.class, () -> userController.create(user1));
		Assertions.assertThrows(ValidationException.class, () -> userController.create(user2));
		Assertions.assertThrows(ValidationException.class, () -> userController.create(user3));
	}

	@Test
	void userControllerUpdateTest() {
		UserController userController = new UserController();
		User user1 = new User();
		user1.setName("Андрей");
		user1.setLogin("shark123");
		user1.setEmail("pochta12@gmail.com");
		user1.setBirthday(LocalDate.of(2000, 1, 24));
		User user2 = new User();
		user2.setId(1L);
		user2.setName("Сергей");
		user2.setLogin("shark123");
		user2.setEmail("pochta12@gmail.com");
		user2.setBirthday(LocalDate.of(2000, 1, 24));
		userController.create(user1);
		User updatedUser = userController.update(user2);

		Assertions.assertEquals("Сергей", updatedUser.getName());
	}

	@Test
	void userControllerUpdateNotFoundTest() {
		UserController userController = new UserController();
		User user1 = new User();
		user1.setName("Андрей");
		user1.setLogin("shark123");
		user1.setEmail("pochta12@gmail.com");
		user1.setBirthday(LocalDate.of(2000, 1, 24));
		User user2 = new User();
		user2.setId(2L);
		user2.setName("Сергей");
		user2.setLogin("shark123");
		user2.setEmail("pochta12@gmail.com");
		user2.setBirthday(LocalDate.of(2027, 1, 24));
		userController.create(user1);

		Assertions.assertThrows(NotFoundException.class, () -> userController.update(user2));
	}

	@Test
	void userControllerUpdateConditionsNotMetTest() {
		UserController userController = new UserController();
		User user1 = new User();
		user1.setName("Андрей");
		user1.setLogin("shark123");
		user1.setEmail("pochta12@gmail.com");
		user1.setBirthday(LocalDate.of(2000, 1, 24));
		User user2 = new User();
		user2.setName("Андрей");
		user2.setLogin("shark123");
		user2.setEmail("pochta12@gmail.com");
		user2.setBirthday(LocalDate.of(2027, 1, 24));
		userController.create(user1);

		Assertions.assertThrows(ConditionsNotMetException.class, () -> userController.update(user2));
	}

	@Test
	void filmControllerCreateTest() {
		FilmController filmController = new FilmController();
		Film film1 = new Film();
		film1.setName("Зеленая книга");
		film1.setDuration(120);
		film1.setDescription("w".repeat(200));
		film1.setReleaseDate(LocalDate.of(2019, 1, 24));

		Film createdFilm = filmController.create(film1);
		Assertions.assertNotNull(createdFilm.getId());
		Assertions.assertEquals(film1.getName(), createdFilm.getName());
		Assertions.assertEquals(film1.getDuration(), createdFilm.getDuration());
		Assertions.assertEquals(film1.getDescription(), createdFilm.getDescription());
		Assertions.assertEquals(film1.getReleaseDate(), createdFilm.getReleaseDate());
	}

	@Test
	void filmControllerValidationDateCreateTestFail() {
		FilmController filmController = new FilmController();
		Film film1 = new Film();
		film1.setName("Зеленая книга");
		film1.setDuration(120);
		film1.setDescription("Это фильм о честности, достоинстве человека, настоящей дружбе и взаимовыручке между людьми.");
		film1.setReleaseDate(LocalDate.of(1850, 1, 24));

		Assertions.assertThrows(ValidationException.class, () -> filmController.create(film1));
	}

	@Test
	void filmControllerValidationNameCreateTestFail() {
		FilmController filmController = new FilmController();
		Film film1 = new Film();
		film1.setDuration(120);
		film1.setDescription("Это фильм о честности, достоинстве человека, настоящей дружбе и взаимовыручке между людьми.");
		film1.setReleaseDate(LocalDate.of(2019, 1, 24));
		Film film2 = new Film();
		film1.setName("   ");
		film1.setDuration(120);
		film1.setDescription("Это фильм о честности, достоинстве человека, настоящей дружбе и взаимовыручке между людьми.");
		film1.setReleaseDate(LocalDate.of(2019, 1, 24));

		Assertions.assertThrows(ValidationException.class, () -> filmController.create(film1));
	}

	@Test
	void filmControllerValidationDurationCreateTestFail() {
		FilmController filmController = new FilmController();
		Film film1 = new Film();
		film1.setName("Зеленая книга");
		film1.setDuration(-120);
		film1.setDescription("Это фильм о честности, достоинстве человека, настоящей дружбе и взаимовыручке между людьми.");
		film1.setReleaseDate(LocalDate.of(2019, 1, 24));

		Assertions.assertThrows(ValidationException.class, () -> filmController.create(film1));
	}

	@Test
	void filmControllerValidationDescriptionCreateTestFail() {
		FilmController filmController = new FilmController();
		Film film1 = new Film();
		film1.setName("Зеленая книга");
		film1.setDuration(120);
		film1.setDescription("w".repeat(201));
		film1.setReleaseDate(LocalDate.of(2019, 1, 24));

		Assertions.assertThrows(ValidationException.class, () -> filmController.create(film1));
	}

	@Test
	void filmControllerUpdateTest() {
		FilmController filmController = new FilmController();
		Film film1 = new Film();
		film1.setName("Зеленая книга");
		film1.setDuration(120);
		film1.setDescription("w".repeat(200));
		film1.setReleaseDate(LocalDate.of(2019, 1, 24));
		Film film2 = new Film();
		film2.setId(1L);
		film2.setName("Зеленая книга");
		film2.setDuration(120);
		film2.setDescription("Это фильм о честности, достоинстве человека, настоящей дружбе и взаимовыручке между людьми.");
		film2.setReleaseDate(LocalDate.of(2019, 1, 24));

		filmController.create(film1);
		Film createdFilm = filmController.update(film2);
		Assertions.assertEquals(1L, film1.getId());
		Assertions.assertEquals(film1.getName(), createdFilm.getName());
		Assertions.assertEquals(film1.getDuration(), createdFilm.getDuration());
		Assertions.assertEquals("Это фильм о честности, достоинстве человека," +
				" настоящей дружбе и взаимовыручке между людьми.", createdFilm.getDescription());
		Assertions.assertEquals(film1.getReleaseDate(), createdFilm.getReleaseDate());
	}

	@Test
	void filmControllerUpdateConditionsNotMetTest() {
		FilmController filmController = new FilmController();
		Film film1 = new Film();
		film1.setName("Зеленая книга");
		film1.setDuration(120);
		film1.setDescription("w".repeat(200));
		film1.setReleaseDate(LocalDate.of(2019, 1, 24));
		Film film2 = new Film();
		film2.setName("Зеленая книга");
		film2.setDuration(120);
		film2.setDescription("Это фильм о честности, достоинстве человека, настоящей дружбе и взаимовыручке между людьми.");
		film2.setReleaseDate(LocalDate.of(2019, 1, 24));

		filmController.create(film1);
		Assertions.assertThrows(ConditionsNotMetException.class, () -> filmController.update(film2));
	}

	@Test
	void filmControllerUpdateNotFoundTest() {
		FilmController filmController = new FilmController();
		Film film1 = new Film();
		film1.setName("Зеленая книга");
		film1.setDuration(120);
		film1.setDescription("w".repeat(200));
		film1.setReleaseDate(LocalDate.of(2019, 1, 24));
		Film film2 = new Film();
		film2.setId(2L);
		film2.setName("Зеленая книга");
		film2.setDuration(120);
		film2.setDescription("Это фильм о честности, достоинстве человека, настоящей дружбе и взаимовыручке между людьми.");
		film2.setReleaseDate(LocalDate.of(2019, 1, 24));

		filmController.create(film1);
		Assertions.assertThrows(NotFoundException.class, () -> filmController.update(film2));
	}

}
