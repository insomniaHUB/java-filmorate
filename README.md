# java-filmorate
![ER-диаграмма базы данных проекта filmorate](filmorate_db_diagram.png)

ER-диаграмма базы данных проекта Filmorate в третьей нормальной форме.

Основные сущности:
+ users - пользователи (email, логин, имя, день рождения)
+ films - фильмы (название, описание, длительность, дата выхода, рейтинг MPA)
+ friendship - дружба между пользователями (статус: подтвержденная/неподтверждённая)
+ likes - лайки фильмов пользователями
+ genres - жанры фильмов
+ film_genres - связь фильмов с жанрами
+ motion_picture_association - возрастные рейтинги

1. Получение всех фильмов
```
SELECT *
FROM films;
```
2. Получение всех пользователей
```
SELECT *
FROM users;
```
   
3. Топ 10 наиболее популярных фильмов
```
SELECT f.name,
       COUNT(l.film_id) AS film_likes
FROM films AS f
LEFT OUTER JOIN likes AS l ON l.film_id=f.film_id
GROUP BY f.name
ORDER BY film_likes DESC
LIMIT 10;
```

4. Список лайкнутых фильмов у пользователя
```
SELECT f.name
FROM likes AS l
LEFT OUTER JOIN films AS f ON f.film_id=l.film_id
WHERE l.user_id = 1;
```

Template repository for Filmorate project.
