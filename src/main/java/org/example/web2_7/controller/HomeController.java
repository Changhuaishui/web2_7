package org.example.web2_7.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class HomeController {

    @GetMapping("/")  // 提供spring前端8081首页接口
    public String home() {
        return "欢迎访问微信公众号文章爬虫系统，这里是彩蛋";
    }
}