package ru.elias.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.elias.server.model.Joke;

public interface JokeRepository extends CrudRepository<Joke, Long>, QuerydslPredicateExecutor<Joke> {

    @Query(nativeQuery = true,
           value = "select j.* "
                   + "from jokes j "
                   + "order by random() "
                   + "limit 1")
    Joke findRandomJoke();

    @Query("select j "
            + "from Category c "
            + "join c.jokes j "
            + "where c.name = :categoryName")
    List<Joke> findAllByCategory(@Param("categoryName") String categoryName);

    @Query(nativeQuery = true,
           value = "select j.* "
                   + "from jokes j "
                   + "where category_id = :categoryId "
                   + "order by random() "
                   + "limit 1")
    Joke findRandomJokeByCategoryId(@Param("categoryId") Long categoryId);

    @Query("select count(j) "
            + "from Category c "
            + "left join c.jokes j "
            + "where c.name = :categoryName "
            + "group by c.id")
    Long countJokeByCategoryName(@Param("categoryName") String categoryName);

}
