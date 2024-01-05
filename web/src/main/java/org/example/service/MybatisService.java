package org.example.service;

import org.example.mapper.DrinkExtMapper;
import org.example.mapper.EatExtMapper;
import org.example.model.Drink;
import org.example.model.Eat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Component

public class MybatisService {
    @Autowired
    private EatExtMapper eatExtMapper;
    @Autowired
    private DrinkExtMapper drinkExtMapper;

    public List<Eat> findEatAll() throws Exception{
        try {
            List<Eat> eatList=eatExtMapper.findAll();
            return eatList;
        } catch (Exception e) {
            System.out.print(e);
            return null;
        }
    }

    public List<Drink> findDrinkAll() throws Exception{
        try {
            List<Drink> drinkList=drinkExtMapper.findAll();
            return drinkList;
        } catch (Exception e) {
            System.out.print(e);
            return null;
        }
    }
}
