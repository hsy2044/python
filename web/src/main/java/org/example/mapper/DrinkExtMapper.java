package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.model.Drink;

import java.util.List;

@Mapper
public interface DrinkExtMapper extends DrinkMapper{
    List<Drink> findAll();
}