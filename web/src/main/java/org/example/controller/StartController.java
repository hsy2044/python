package org.example.controller;

import org.example.entity.Router;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;


@Controller
public class StartController extends BaseController {

    @PostMapping("/first")
    public String first() {
        return Router.FIRST.getView();
    }
    @PostMapping("/submitChoices")
    @ResponseBody
    public String handleChoices(@RequestParam("college") String[] colleges) {
        System.out.print(Arrays.toString(colleges));
        return "Selected: " + Arrays.toString(colleges);
    }
}
