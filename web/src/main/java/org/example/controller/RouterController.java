package org.example.controller;

import org.example.entity.Router;
import org.example.util.WebUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class RouterController extends BaseController {

    @GetMapping(value = "/")
    public String login() {
        return Router.LOGIN.getView();
    }
}
