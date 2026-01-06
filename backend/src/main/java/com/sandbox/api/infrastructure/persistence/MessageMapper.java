package com.sandbox.api.infrastructure.persistence;

import com.sandbox.api.domain.model.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {
    Message findByCode(@Param("code") String code);
    List<Message> findAll();
    Message findById(@Param("id") Long id);
    void insert(Message message);
    void update(Message message);
    void deleteById(@Param("id") Long id);
    boolean existsByCode(@Param("code") String code);
}
