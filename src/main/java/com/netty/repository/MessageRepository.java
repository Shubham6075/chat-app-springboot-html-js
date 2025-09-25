package com.netty.repository;

import com.netty.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByRoomOrderByTimestampAsc(String room);
}