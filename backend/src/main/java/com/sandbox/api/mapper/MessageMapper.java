package com.sandbox.api.mapper;

import com.sandbox.api.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MessageMapper {
    Message findByCode(@Param("code") String code);
}