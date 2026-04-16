package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public List<Director> getAllDirectors() {
        return directorStorage.getAllDirectors();
    }

    public Director getDirectorById(Long id) {
        return directorStorage.getDirectorById(id);
    }

    public Director createDirector(Director director) {
        return directorStorage.createDirector(director);
    }

    public Director updateDirector(Director newDirector) {
        directorStorage.getDirectorById(newDirector.getId());
        return directorStorage.updateDirector(newDirector);
    }

    public void deleteDirector(Long id) {
        directorStorage.getDirectorById(id);
        directorStorage.deleteDirector(id);
    }
}
