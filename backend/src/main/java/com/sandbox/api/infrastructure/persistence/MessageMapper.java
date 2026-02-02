package com.sandbox.api.infrastructure.persistence;

import com.sandbox.api.domain.model.Message;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** MyBatis mapper for message database operations. */
@Mapper
public interface MessageMapper {
  Message findByCode(@Param("code") String code);

  List<Message> findAll();

  List<Message> findAllWithPagination(
      @Param("offset") long offset,
      @Param("limit") int limit,
      @Param("sortField") String sortField,
      @Param("sortDirection") String sortDirection);

  long count();

  Message findById(@Param("id") Long id);

  void insert(Message message);

  void update(Message message);

  void deleteById(@Param("id") Long id);

  boolean existsByCode(@Param("code") String code);

  boolean existsById(@Param("id") Long id);
}
