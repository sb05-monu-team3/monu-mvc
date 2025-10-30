package com.monew.monew_server.domain.notification.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.monew.monew_server.domain.notification.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

	@Query("""
		SELECT n FROM Notification n
		WHERE n.user.id = :userId
		  AND n.confirmed = false
		  AND (
		    :cursorId IS NULL
		    OR n.createdAt < :after
		    OR (n.createdAt = :after AND n.id < :cursorId)
		  )
		ORDER BY n.createdAt DESC, n.id DESC
		""")
	List<Notification> findUnconfirmedWithCursor(
		@Param("userId") UUID userId,
		@Param("cursorId") UUID cursorId,
		@Param("after") Instant after,
		@Param("limit") int limit);

	Optional<Notification> findByIdAndUserId(UUID id, UUID userId);

	@Modifying
	@Query(value = """
		UPDATE Notification n
		SET n.confirmed = true
		WHERE n.user.id = :userId
		AND n.confirmed = false
		""")
	int confirmByUserId(@Param("userId") UUID userId);

	long countByUserIdAndConfirmedFalse(UUID userId);

	void deleteAllByUserId(UUID userId);
}