package com.example.internship.controller;

import com.example.internship.entity.BankTransferForm;
import com.example.internship.service.ApplyBankTransferService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

// Web層だけを起動する。DataSourceは読み込まれないのでMySQLが動いていなくても実行できる。
@WebMvcTest(BankTransferController.class)
@DisplayName("BankTransferController の画面遷移")
class BankTransferControllerTest {

    private static final String TOKEN_KEY = "transferToken";
    private static final String FORM_NAME = "bankTransferApplication";
    private static final String INPUT_SESSION_KEY = "bankTransferInput";

    @Autowired
    private MockMvc mockMvc;

    // DBへ書き込む処理は差し替える。呼ばれたかどうかだけを見る
    @MockitoBean
    private ApplyBankTransferService applyBankTransferService;

    // すべての項目が正しい状態。壊したい項目だけ set で上書きして使う
    private MultiValueMap<String, String> validParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("bankName", "ながれぼし銀行");
        params.add("branchName", "本店");
        params.add("bankAccountType", "普通");
        params.add("bankAccountNum", "1234567");
        params.add("name", "ﾔﾏﾀﾞ ﾀﾛｳ");
        params.add("money", "1000");
        params.add("transferDateTime", LocalDate.now().plusDays(1).toString());
        return params;
    }

    // 確認画面まで進めて、発行されたトークンを取り出す
    private String startTransfer(MockHttpSession session) throws Exception {
        mockMvc.perform(post("/bankTransferConfirmation").params(validParams()).session(session))
                .andExpect(view().name("bankTransferConfirmation"));
        Object token = session.getAttribute(TOKEN_KEY);
        assertThat(token).as("確認画面でトークンが発行されること").isNotNull();
        return token.toString();
    }

    @Test
    @DisplayName("入力画面が表示され、選択肢と本日の日付が渡される")
    void 入力画面の表示() throws Exception {
        mockMvc.perform(get("/bankTransfer"))
                .andExpect(status().isOk())
                .andExpect(view().name("bankTransferMain"))
                .andExpect(model().attributeExists(FORM_NAME, "nameOptions", "accountTypeOptions", "today"));
    }

    @Test
    @DisplayName("入力が正しければ確認画面へ進み、トークンが発行される")
    void 確認画面への遷移() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/bankTransferConfirmation").params(validParams()).session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("bankTransferConfirmation"))
                .andExpect(model().attributeExists(TOKEN_KEY));

        assertThat(session.getAttribute(TOKEN_KEY)).isNotNull();
    }

    @Test
    @DisplayName("入力に誤りがあれば入力画面に留まり、トークンは発行されない")
    void 入力エラー時は進まない() throws Exception {
        MockHttpSession session = new MockHttpSession();
        MultiValueMap<String, String> params = validParams();
        params.set("money", "0");

        mockMvc.perform(post("/bankTransferConfirmation").params(params).session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("bankTransferMain"))
                .andExpect(model().attributeHasFieldErrors(FORM_NAME, "money"));

        assertThat(session.getAttribute(TOKEN_KEY)).isNull();
    }

    @Test
    @DisplayName("正しいトークンを添えると登録され、完了画面へリダイレクトする")
    void 申し込みの確定() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String token = startTransfer(session);

        MultiValueMap<String, String> params = validParams();
        params.add(TOKEN_KEY, token);

        mockMvc.perform(post("/bankTransferCompletion").params(params).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bankTransferCompletion"))
                .andExpect(flash().attributeExists(FORM_NAME));

        verify(applyBankTransferService, times(1)).applyBankTransfer(any(BankTransferForm.class));
        assertThat(session.getAttribute(TOKEN_KEY)).as("使ったトークンは破棄されること").isNull();
    }

    @Test
    @DisplayName("同じトークンで2回送っても登録は1回だけ（ブラウザバックからの再送信を防ぐ）")
    void 二重送信は登録されない() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String token = startTransfer(session);

        MultiValueMap<String, String> params = validParams();
        params.add(TOKEN_KEY, token);

        mockMvc.perform(post("/bankTransferCompletion").params(params).session(session))
                .andExpect(redirectedUrl("/bankTransferCompletion"));

        mockMvc.perform(post("/bankTransferCompletion").params(params).session(session))
                .andExpect(redirectedUrl("/bankTransfer"));

        verify(applyBankTransferService, times(1)).applyBankTransfer(any(BankTransferForm.class));
    }

    @Test
    @DisplayName("トークンが無ければ登録されない（確認画面を経由しない直接送信）")
    void トークン無しの直接送信() throws Exception {
        mockMvc.perform(post("/bankTransferCompletion").params(validParams()).session(new MockHttpSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bankTransfer"));

        verify(applyBankTransferService, never()).applyBankTransfer(any(BankTransferForm.class));
    }

    @Test
    @DisplayName("完了処理はセッションの内容だけを登録し、送られてきた値は無視する")
    void 送信値ではなくセッションの内容を登録する() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String token = startTransfer(session);

        // 確認画面では 1000 円で進んだのに、送信時だけ金額と口座番号をすり替えてみる
        MultiValueMap<String, String> tampered = validParams();
        tampered.set("money", "9999999");
        tampered.set("bankAccountNum", "7654321");
        tampered.add(TOKEN_KEY, token);

        mockMvc.perform(post("/bankTransferCompletion").params(tampered).session(session))
                .andExpect(redirectedUrl("/bankTransferCompletion"));

        ArgumentCaptor<BankTransferForm> captor = ArgumentCaptor.forClass(BankTransferForm.class);
        verify(applyBankTransferService).applyBankTransfer(captor.capture());

        assertThat(captor.getValue().getMoney()).isEqualTo(1000);
        assertThat(captor.getValue().getBankAccountNum()).isEqualTo("1234567");
    }

    @Test
    @DisplayName("セッションが切れて入力内容が消えていれば登録しない")
    void セッション切れ() throws Exception {
        // トークンだけ残っていて入力内容が無い状態を作る
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(TOKEN_KEY, "dummy-token");

        mockMvc.perform(post("/bankTransferCompletion")
                        .param(TOKEN_KEY, "dummy-token")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bankTransfer"));

        verify(applyBankTransferService, never()).applyBankTransfer(any(BankTransferForm.class));
    }

    @Test
    @DisplayName("登録が済むとセッションの入力内容は破棄される")
    void 登録後にセッションを片付ける() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String token = startTransfer(session);
        assertThat(session.getAttribute(INPUT_SESSION_KEY)).isNotNull();

        MultiValueMap<String, String> params = validParams();
        params.add(TOKEN_KEY, token);
        mockMvc.perform(post("/bankTransferCompletion").params(params).session(session))
                .andExpect(redirectedUrl("/bankTransferCompletion"));

        assertThat(session.getAttribute(INPUT_SESSION_KEY)).isNull();
    }

    @Test
    @DisplayName("完了画面に直接アクセスすると入力画面へ戻される")
    void 完了画面への直接アクセス() throws Exception {
        mockMvc.perform(get("/bankTransferCompletion"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bankTransfer"));
    }
}
