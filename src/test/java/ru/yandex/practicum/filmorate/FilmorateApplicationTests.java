package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.dao.MpaDbStorage;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MotionPicture;
import ru.yandex.practicum.filmorate.model.User;


import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, FilmDbStorage.class, GenreDbStorage.class, MpaDbStorage.class,
        UserRowMapper.class, FilmRowMapper.class, MpaRowMapper.class, GenreRowMapper.class})
class FilmorateApplicationTests {
    @Autowired
    private UserDbStorage userStorage;
    @Autowired
    private FilmDbStorage filmStorage;
    @Autowired
    private GenreDbStorage genreStorage;
    @Autowired
    private MpaDbStorage mpaStorage;

    @Test
    void testGetAllUsers() {
        User user = new User();
        user.setEmail("mailmail@gmail.com");
        user.setLogin("login");
        user.setName("Борис");
        user.setBirthday(LocalDate.of(2002, 1, 20));
        User userTwo = new User();
        userTwo.setEmail("mypochta@gmail.com");
        userTwo.setLogin("Grib");
        userTwo.setName("Михаил");
        userTwo.setBirthday(LocalDate.of(2008, 1, 20));

        userStorage.createUser(user);
        userStorage.createUser(userTwo);

        assertThat(userStorage.getAllUsers()).hasSize(2);
    }


    @Test
    void testGetUserById() {
        User user = new User();
        user.setEmail("mailmail@gmail.com");
        user.setLogin("login");
        user.setName("Борис");
        user.setBirthday(LocalDate.of(2002, 1, 20));

        User createdUser = userStorage.createUser(user);
        User foundUser = userStorage.getUserById(createdUser.getId());

        assertThat(foundUser.getEmail()).isEqualTo("mailmail@gmail.com");
        assertThat(foundUser.getLogin()).isEqualTo("login");
        assertThat(foundUser.getName()).isEqualTo("Борис");
        assertThat(foundUser.getBirthday()).isEqualTo(LocalDate.of(2002, 1, 20));
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setEmail("mailmail@gmail.com");
        user.setLogin("login");
        user.setName("Борис");
        user.setBirthday(LocalDate.of(2002, 1, 20));

        User newUser = userStorage.createUser(user);

        newUser.setEmail("mypochta@gmail.com");
        newUser.setLogin("Grib");
        newUser.setName("Михаил");
        newUser.setBirthday(LocalDate.of(2008, 1, 20));

        userStorage.updateUser(newUser);

        User foundNewUser = userStorage.getUserById(newUser.getId());
        assertThat(foundNewUser.getEmail()).isEqualTo("mypochta@gmail.com");
        assertThat(foundNewUser.getLogin()).isEqualTo("Grib");
        assertThat(foundNewUser.getName()).isEqualTo("Михаил");
        assertThat(foundNewUser.getBirthday()).isEqualTo(LocalDate.of(2008, 1, 20));
    }

    @Test
    void testDeleteUser() {
        User user = new User();
        user.setEmail("mailmail@gmail.com");
        user.setLogin("login");
        user.setName("Борис");
        user.setBirthday(LocalDate.of(2002, 1, 20));

        User newUser = userStorage.createUser(user);
        userStorage.deleteUser(newUser.getId());

        assertThat(userStorage.getAllUsers()).isEmpty();
    }

    @Test
    void testAddFriend() {
        User userOne = new User();
        userOne.setEmail("mailmail@gmail.com");
        userOne.setLogin("login");
        userOne.setName("Борис");
        userOne.setBirthday(LocalDate.of(2002, 1, 20));

        User userTwo = new User();
        userTwo.setEmail("mypochta@gmail.com");
        userTwo.setLogin("Grib");
        userTwo.setName("Михаил");
        userTwo.setBirthday(LocalDate.of(2008, 1, 20));

        User createdUserOne = userStorage.createUser(userOne);
        User createdUserTwo = userStorage.createUser(userTwo);

        userStorage.addFriend(createdUserOne.getId(), createdUserTwo.getId());

        User updatedUser = userStorage.getUserById(createdUserOne.getId());

        assertThat(updatedUser.getFriends()).contains(createdUserTwo.getId());
    }

    @Test
    void testDeleteFriend() {
        User userOne = new User();
        userOne.setEmail("mailmail@gmail.com");
        userOne.setLogin("login");
        userOne.setName("Борис");
        userOne.setBirthday(LocalDate.of(2002, 1, 20));

        User userTwo = new User();
        userTwo.setEmail("mypochta@gmail.com");
        userTwo.setLogin("Grib");
        userTwo.setName("Михаил");
        userTwo.setBirthday(LocalDate.of(2008, 1, 20));

        User createdUserOne = userStorage.createUser(userOne);
        User createdUserTwo = userStorage.createUser(userTwo);

        userStorage.addFriend(createdUserOne.getId(), createdUserTwo.getId());
        userStorage.deleteFriend(createdUserOne.getId(), createdUserTwo.getId());

        User updatedUser = userStorage.getUserById(createdUserOne.getId());

        assertThat(updatedUser.getFriends()).doesNotContain(createdUserTwo.getId());
    }

    @Test
    void testGetAllFilms() {
        Film filmOne = new Film();
        filmOne.setName("Интерстеллар");
        filmOne.setDescription("Cool film about space");
        filmOne.setDuration(169);
        filmOne.setReleaseDate(LocalDate.of(2014, 11, 6));
        MotionPicture mpaOne = new MotionPicture();
        mpaOne.setId(3L);
        filmOne.setMpa(mpaOne);

        Film filmTwo = new Film();
        filmTwo.setName("Зеленая книга");
        filmTwo.setDescription("Cool film about friendship");
        filmTwo.setDuration(130);
        filmTwo.setReleaseDate(LocalDate.of(2019, 1, 24));
        MotionPicture mpaTwo = new MotionPicture();
        mpaTwo.setId(3L);
        filmTwo.setMpa(mpaTwo);

        filmStorage.createFilm(filmOne);
        filmStorage.createFilm(filmTwo);


        assertThat(filmStorage.getAllFilms()).hasSize(2);
    }

    @Test
    void testGetFilmById() {
        Film filmOne = new Film();
        filmOne.setName("Интерстеллар");
        filmOne.setDescription("Cool film about space");
        filmOne.setDuration(169);
        filmOne.setReleaseDate(LocalDate.of(2014, 11, 6));
        MotionPicture mpaOne = new MotionPicture();
        mpaOne.setId(3L);
        filmOne.setMpa(mpaOne);
        Genre genreOne = new Genre();
        Genre genreTwo = new Genre();
        genreOne.setId(5L);
        genreTwo.setId(2L);
        filmOne.getGenres().add(genreOne);
        filmOne.getGenres().add(genreTwo);

        Film createdFilm = filmStorage.createFilm(filmOne);
        Film foundFilm = filmStorage.getFilmById(createdFilm.getId());

        assertThat(foundFilm.getName()).isEqualTo("Интерстеллар");
        assertThat(foundFilm.getDescription()).isEqualTo("Cool film about space");
        assertThat(foundFilm.getDuration()).isEqualTo(169);
        assertThat(foundFilm.getReleaseDate()).isEqualTo(LocalDate.of(2014, 11, 6));
        assertThat(foundFilm.getMpa().getId()).isEqualTo(mpaOne.getId());
        assertThat(foundFilm.getGenres()).hasSize(2);
    }

    @Test
    void testUpdateFilm() {
        Film filmOne = new Film();
        filmOne.setName("Интерстеллар");
        filmOne.setDescription("Cool film about space");
        filmOne.setDuration(169);
        filmOne.setReleaseDate(LocalDate.of(2014, 11, 6));
        MotionPicture mpaOne = new MotionPicture();
        mpaOne.setId(3L);
        filmOne.setMpa(mpaOne);
        Genre genreOne = new Genre();
        Genre genreTwo = new Genre();
        genreOne.setId(5L);
        genreTwo.setId(2L);
        filmOne.getGenres().add(genreOne);
        filmOne.getGenres().add(genreTwo);

        Film createdFilm = filmStorage.createFilm(filmOne);
        createdFilm.setName("Interstellar");
        createdFilm.setDescription("Крутой фильм о космосе");
        createdFilm.setDuration(170);
        createdFilm.setReleaseDate(LocalDate.of(2014, 11, 7));
        filmStorage.updateFilm(createdFilm);

        Film foundNewFilm = filmStorage.getFilmById(createdFilm.getId());
        assertThat(foundNewFilm.getDescription()).isEqualTo("Крутой фильм о космосе");
        assertThat(foundNewFilm.getDuration()).isEqualTo(170);
        assertThat(foundNewFilm.getName()).isEqualTo("Interstellar");
        assertThat(foundNewFilm.getReleaseDate()).isEqualTo(LocalDate.of(2014, 11, 7));
    }

    @Test
    void testDeleteFilm() {
        Film filmOne = new Film();
        filmOne.setName("Интерстеллар");
        filmOne.setDescription("Cool film about space");
        filmOne.setDuration(169);
        filmOne.setReleaseDate(LocalDate.of(2014, 11, 6));
        MotionPicture mpaOne = new MotionPicture();
        mpaOne.setId(3L);
        filmOne.setMpa(mpaOne);
        Genre genreOne = new Genre();
        Genre genreTwo = new Genre();
        genreOne.setId(5L);
        genreTwo.setId(2L);
        filmOne.getGenres().add(genreOne);
        filmOne.getGenres().add(genreTwo);

        Film createdFilm = filmStorage.createFilm(filmOne);
        filmStorage.deleteFilm(createdFilm.getId());

        assertThat(filmStorage.getAllFilms()).isEmpty();
    }

    @Test
    void testAddLike() {
        Film filmOne = new Film();
        filmOne.setName("Интерстеллар");
        filmOne.setDescription("Cool film about space");
        filmOne.setDuration(169);
        filmOne.setReleaseDate(LocalDate.of(2014, 11, 6));
        MotionPicture mpaOne = new MotionPicture();
        mpaOne.setId(3L);
        filmOne.setMpa(mpaOne);
        Genre genreOne = new Genre();
        Genre genreTwo = new Genre();
        genreOne.setId(5L);
        genreTwo.setId(2L);
        filmOne.getGenres().add(genreOne);
        filmOne.getGenres().add(genreTwo);

        User user = new User();
        user.setEmail("mailmail@gmail.com");
        user.setLogin("login");
        user.setName("Борис");
        user.setBirthday(LocalDate.of(2002, 1, 20));

        User createdUser = userStorage.createUser(user);
        Film createdFilm = filmStorage.createFilm(filmOne);
        filmStorage.addLike(createdFilm.getId(), createdUser.getId());

        Film newFilm = filmStorage.getFilmById(createdFilm.getId());

        assertThat(newFilm.getLiked()).hasSize(1);
        assertThat(newFilm.getLiked()).contains(createdUser.getId());
    }

    @Test
    void testDeleteLike() {
        Film filmOne = new Film();
        filmOne.setName("Интерстеллар");
        filmOne.setDescription("Cool film about space");
        filmOne.setDuration(169);
        filmOne.setReleaseDate(LocalDate.of(2014, 11, 6));
        MotionPicture mpaOne = new MotionPicture();
        mpaOne.setId(3L);
        filmOne.setMpa(mpaOne);
        Genre genreOne = new Genre();
        Genre genreTwo = new Genre();
        genreOne.setId(5L);
        genreTwo.setId(2L);
        filmOne.getGenres().add(genreOne);
        filmOne.getGenres().add(genreTwo);

        User user = new User();
        user.setEmail("mailmail@gmail.com");
        user.setLogin("login");
        user.setName("Борис");
        user.setBirthday(LocalDate.of(2002, 1, 20));

        User createdUser = userStorage.createUser(user);
        Film createdFilm = filmStorage.createFilm(filmOne);
        filmStorage.addLike(createdFilm.getId(), createdUser.getId());
        filmStorage.deleteLike(createdFilm.getId(), createdUser.getId());

        Film newFilm = filmStorage.getFilmById(createdFilm.getId());

        assertThat(newFilm.getLiked()).hasSize(0);
        assertThat(newFilm.getLiked()).doesNotContain(createdUser.getId());
    }

    @Test
    void testGetAllMpa() {
        assertThat(mpaStorage.getAllMpa()).hasSize(5);
    }

    @Test
    void testGetMpaById() {
        assertThat(mpaStorage.getMpaById(3L).getId()).isEqualTo(3);
        assertThat(mpaStorage.getMpaById(3L).getName()).isEqualTo("PG-13");
    }

    @Test
    void testGetAllGenres() {
        assertThat(genreStorage.getAllGenres()).hasSize(6);
    }

    @Test
    void testGetGenreById() {
        assertThat(genreStorage.getGenreById(5L).getId()).isEqualTo(5);
        assertThat(genreStorage.getGenreById(5L).getName()).isEqualTo("Документальный");
    }
}