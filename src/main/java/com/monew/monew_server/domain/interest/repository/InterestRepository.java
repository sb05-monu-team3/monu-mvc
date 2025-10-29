package com.monew.monew_server.domain.interest.repository;

import com.monew.monew_server.domain.interest.entity.Interest;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestRepository extends JpaRepository<Interest, UUID> {

}
