package com.monew.monew_server.domain.interest.repository;

import com.monew.monew_server.domain.interest.entity.Interest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InterestRepository extends JpaRepository<Interest, UUID> {

  @Query(
      value =
          """
        SELECT *
        FROM interests
        WHERE (
                1.0 - (levenshtein(name, :name) / (GREATEST(length(name), length(:name))::float))
            ) > 0.8
        """,
      nativeQuery = true)
  List<Interest> findSimilarInterests(@Param("name") String name);
}
