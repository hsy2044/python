package org.example.controller;

import org.example.entity.Router;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class StartController extends BaseController {

    @PostMapping(value = "/first")
    public String start() {
        return Router.FIRST.getView();
//        return this.redirectToUrl(Router.FIRST.getUrl());
    }
}
