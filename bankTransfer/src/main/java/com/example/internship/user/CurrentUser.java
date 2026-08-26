package com.example.internship.user;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

// 「今このブラウザを使っているのは誰か」を答える唯一の場所。
//
// このアプリにはまだログインが無い。本来ならログイン時に確定した利用者IDを
// 使うべきだが、それが無いので既定のIDを1つ置いている。
// 呼び出し側は resolve() しか知らないので、ログインを作るときは
// このクラスの中だけを書き換えれば済む。
//
// 履歴と登録先は「その人のもの」しか見せてはいけない。利用者IDを取る場所が
// 散らばっていると、1か所でも取り違えたときに他人の口座番号が見えてしまう。
// だから取得は必ずここを通す。
@Component
public class CurrentUser {

    // セッションに利用者IDを預けるときのキー
    static final String SESSION_KEY = "userId";

    // ログインが無い間、既定で使う利用者ID
    public static final String DEFAULT_USER_ID = "demo";

    // 利用者IDを返す。まだ決まっていなければ既定のIDを決めてから返す
    public String resolve(HttpSession session) {
        Object userId = session.getAttribute(SESSION_KEY);
        if (userId instanceof String stored && !stored.isBlank()) {
            return stored;
        }
        session.setAttribute(SESSION_KEY, DEFAULT_USER_ID);
        return DEFAULT_USER_ID;
    }

    // 【ログインを作るまでの仮の仕組み】
    // 利用者を切り替える。他人の履歴が見えないことを手で確かめるために用意している。
    // 本物のログインを入れる時点で、この入口ごと消すこと。
    // 誰にでもなりすませてしまうので、このまま実運用に出してはいけない。
    public void switchTo(HttpSession session, String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        session.setAttribute(SESSION_KEY, userId);
    }
}
