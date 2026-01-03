package com.sandbox.api.infrastructure.persistence;

import com.sandbox.api.domain.model.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MessageMapper {
    Message findByCode(@Param("code") String code);
}
