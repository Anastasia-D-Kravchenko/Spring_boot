package com.eventflow.repository;

import com.eventflow.model.AiConversation;
import com.eventflow.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {

    List<AiConversation> findByUserOrderByCreatedAtDesc(User user);

    Page<AiConversation> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<AiConversation> findTop20ByUserOrderByCreatedAtDesc(User user);
}
