package com.baitolog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 起動確認用のトップページ。
 * 本番のトップ（ログイン画面へ）はシーケンス図 01 に合わせて作り直す。
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
