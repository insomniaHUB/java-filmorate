package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MotionPicture;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.Collection;

@Service
public class MpaService {
    private final MpaStorage mpaStorage;

    @Autowired
    public MpaService(@Qualifier("mpaDbStorage") MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public Collection<MotionPicture> getAllMpa() {
        return mpaStorage.getAllMpa();
    }

    public MotionPicture getMpaById(Long mpaId) {
        return mpaStorage.getMpaById(mpaId);
    }
}
