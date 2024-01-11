package org.example.controller;

import org.example.entity.Router;
import org.example.mapper.EatExtMapper;
import org.example.model.Drink;
import org.example.model.Eat;
import org.example.service.MybatisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Random;


@Controller
public class StartController extends BaseController {

    @Autowired(required = false)
    private MybatisService mybatisService;

    @PostMapping("/first")
    public String first(Model model) throws Exception {
        List<Eat> eatcolleges = mybatisService.findEatAll();
        List<Drink> drinkcolleges = mybatisService.findDrinkAll();
        model.addAttribute("eatcolleges", eatcolleges);
        model.addAttribute("drinkcolleges", drinkcolleges);
        return Router.FIRST.getView();
    }
    @PostMapping("/submitChoices")
    @ResponseBody
    public String handleChoices(@RequestParam(value = "eatcollege", required = false) String[] eatcolleges,@RequestParam(value = "drinkcollege", required = false) String[] drinkcolleges) {
        String selectedEatCollege ="";
        String selectedDrinkCollege ="";
        if (eatcolleges == null || eatcolleges.length == 0) {
            System.out.println("未選擇食物");
            selectedEatCollege = "無";
        }else{
            Random random = new Random();
            selectedEatCollege = eatcolleges[random.nextInt(eatcolleges.length)];
            System.out.println("Selected: " + selectedEatCollege);
        }

        if (drinkcolleges == null || drinkcolleges.length == 0) {
            System.out.println("未選擇飲品");
            selectedDrinkCollege="無";
        }else{
            Random random = new Random();
            selectedDrinkCollege = drinkcolleges[random.nextInt(drinkcolleges.length)];
            System.out.println("Selected: " + selectedDrinkCollege);
        }

        return "食物: " + selectedEatCollege + " 飲品:  "+selectedDrinkCollege;

    }
}
