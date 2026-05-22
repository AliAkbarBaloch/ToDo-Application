package com.todoapp.repository;

import com.todoapp.model.Todo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository Pattern — data access layer. Spring Data JPA generates all SQL automatically. The
 * Service layer uses this interface without knowing how data is stored.
 */
@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByCompletedOrderByCreatedAtDesc(boolean completed);

    List<Todo> findAllByOrderByCreatedAtDesc();

    @Query(
            "SELECT t FROM Todo t WHERE "
                    + "LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
                    + "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')) "
                    + "ORDER BY t.createdAt DESC")
    List<Todo> searchByKeyword(@Param("keyword") String keyword);
}
