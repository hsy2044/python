package org.example.mapper;

import jakarta.annotation.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.example.model.Eat;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
@Mapper
public interface EatExtMapper extends EatMapper{
    List<Eat> findAll();
}