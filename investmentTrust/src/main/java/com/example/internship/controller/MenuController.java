package com.example.internship.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 入口となるメニュー画面。
 *
 * アプリを起動して http://localhost:8083/ を開くと、
 * 「口座を登録する」か「投資信託を申し込む」かを選べる。
 *
 * ※投資信託の申込は account_balance に登録済みの口座に対してしか行えないので、
 *   初めて使うときは口座登録が先になる。その順序が分かるようにメニューにも書いている。
 */
@Controller
public class MenuController {

    @GetMapping({"/", "/menu"})
    public String menu() {
        return "menu";
    }
}
