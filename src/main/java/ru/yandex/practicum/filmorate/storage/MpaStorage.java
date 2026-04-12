package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.MotionPicture;

import java.util.Collection;

public interface MpaStorage {
    Collection<MotionPicture> getAllMpa();

    MotionPicture getMpaById(Long id);

    void mpaIdNotExist(Long mpaId);
}
