package com.example.internship.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;

// Springを起動せずに素で組み立てられる。コンストラクタ引数が無いので new するだけで済む
@DisplayName("利用者の判定")
class CurrentUserTest {

    private final CurrentUser currentUser = new CurrentUser();

    @Test
    @DisplayName("まだ決まっていなければ既定の利用者になる")
    void 既定の利用者() {
        MockHttpSession session = new MockHttpSession();

        assertThat(currentUser.resolve(session)).isEqualTo("demo");
    }

    @Test
    @DisplayName("一度決まった利用者は、同じセッションの間は変わらない")
    void 同じセッションでは変わらない() {
        MockHttpSession session = new MockHttpSession();

        String first = currentUser.resolve(session);

        assertThat(currentUser.resolve(session)).isEqualTo(first);
        assertThat(currentUser.resolve(session)).isEqualTo(first);
    }

    @Test
    @DisplayName("セッションが違えば別々に決まる")
    void セッションごとに独立している() {
        MockHttpSession taro = new MockHttpSession();
        MockHttpSession hanako = new MockHttpSession();
        currentUser.switchTo(taro, "taro");

        assertThat(currentUser.resolve(taro)).isEqualTo("taro");
        assertThat(currentUser.resolve(hanako)).isEqualTo("demo");
    }

    @Test
    @DisplayName("切り替えたらそちらが返る")
    void 切り替え() {
        MockHttpSession session = new MockHttpSession();
        currentUser.resolve(session);

        currentUser.switchTo(session, "hanako");

        assertThat(currentUser.resolve(session)).isEqualTo("hanako");
    }

    @Test
    @DisplayName("空の利用者IDでは切り替わらない。既定へ落ちるより今のままの方が安全")
    void 空では切り替えない() {
        MockHttpSession session = new MockHttpSession();
        currentUser.switchTo(session, "taro");

        currentUser.switchTo(session, "");
        currentUser.switchTo(session, "   ");
        currentUser.switchTo(session, null);

        assertThat(currentUser.resolve(session)).isEqualTo("taro");
    }
}
