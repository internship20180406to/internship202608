package com.example.internship.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * 申込の確定（残高の引き落とし）のテスト。
 *
 * @Transactional を付けているので、申込が登録されても残高が減っても
 * テストの最後に自動でロールバックされる。接続先も internship_test。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InvestmentTrustCompletionTest {

    private static final String FORM_NAME = "investmentTrustApplication";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** 残高10,000,000円の口座を使う申込 */
    private MultiValueMap<String, String> validParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("bankCode", "0001");
        params.add("branchCode", "002");
        params.add("bankAccountType", "貯蓄");
        params.add("bankAccountNum", "0031111");
        params.add("name", "ｵｶﾈ ﾅｲ");
        params.add("fundName", "キャピタル１");
        params.add("money", "50000");
        return params;
    }

    private long balanceOf(String bankCode, String branchCode, String type, String num) {
        return jdbcTemplate.queryForObject(
                "SELECT balance FROM account_balance"
                        + " WHERE bankCode = ? AND branchCode = ? AND accountType = ? AND accountNum = ?",
                Long.class, bankCode, branchCode, type, num);
    }

    private int countOrders() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM investmenttrust_table", Integer.class);
    }

    @Test
    @DisplayName("申込が成立すると、残高が減り、申込が1件登録され、完了画面へリダイレクトされる")
    void 申込の成立() throws Exception {
        int ordersBefore = countOrders();

        mockMvc.perform(post("/investmentTrustCompletion").params(validParams()))
                //  POSTの結果を直接表示せず、リダイレクトしている（PRGパターン）
                .andExpect(view().name("redirect:/investmentTrustCompletion"))
                .andExpect(redirectedUrl("/investmentTrustCompletion"))
                .andExpect(flash().attribute("balanceAfter", 9950000L));

        assertThat(balanceOf("0001", "002", "貯蓄", "0031111")).isEqualTo(9950000L);
        assertThat(countOrders()).isEqualTo(ordersBefore + 1);
    }

    @Test
    @DisplayName("登録される金融機関名は、送られてきた値ではなくマスタから引いた値")
    void 登録される名称はマスタの値() throws Exception {
        MultiValueMap<String, String> params = validParams();
        params.add("bankName", "こぶた銀行");     //  hidden項目を書き換えた状況を再現

        mockMvc.perform(post("/investmentTrustCompletion").params(params))
                .andExpect(redirectedUrl("/investmentTrustCompletion"));

        String registered = jdbcTemplate.queryForObject(
                "SELECT bankName FROM investmenttrust_table ORDER BY id DESC LIMIT 1", String.class);
        assertThat(registered).isEqualTo("山陰共同銀行");
    }

    @Test
    @DisplayName("残高が足りなければ入力画面に戻され、残高も申込件数も変わらない")
    void 残高不足() throws Exception {
        MultiValueMap<String, String> params = validParams();
        params.set("bankCode", "0002");
        params.set("branchCode", "012");
        params.set("bankAccountType", "普通");
        params.set("bankAccountNum", "9999999");    //  残高15,000円の口座
        params.set("name", "ﾉｺﾘ ｽｸﾅｲ");
        params.set("money", "50000");               //  残高より多い

        int ordersBefore = countOrders();

        mockMvc.perform(post("/investmentTrustCompletion").params(params))
                .andExpect(view().name("investmentTrustMain"))
                .andExpect(model().attributeHasFieldErrorCode(FORM_NAME, "money", "insufficientBalance"));

        assertThat(balanceOf("0002", "012", "普通", "9999999")).isEqualTo(15000L);
        assertThat(countOrders()).isEqualTo(ordersBefore);
    }

    @Test
    @DisplayName("存在しない口座なら入力画面に戻され、申込は登録されない")
    void 存在しない口座() throws Exception {
        MultiValueMap<String, String> params = validParams();
        params.set("bankAccountNum", "0000000");    //  実在しない口座番号

        int ordersBefore = countOrders();

        mockMvc.perform(post("/investmentTrustCompletion").params(params))
                .andExpect(view().name("investmentTrustMain"))
                .andExpect(model().attributeHasFieldErrorCode(FORM_NAME, "bankAccountNum", "accountNotFound"));

        assertThat(countOrders()).isEqualTo(ordersBefore);
    }

    @Test
    @DisplayName("支店だけ間違っている場合も、口座を特定する4項目すべてがエラーになる")
    void 口座が見つからないときは4項目すべてがエラーになる() throws Exception {
        MultiValueMap<String, String> params = validParams();
        //  支店コード101（博多支店）は実在するが、この支店に口座0031111は無い。
        //  「支店が間違っている」のか「口座番号が間違っている」のかはサーバには分からない。
        params.set("branchCode", "101");

        mockMvc.perform(post("/investmentTrustCompletion").params(params))
                .andExpect(view().name("investmentTrustMain"))
                //  口座番号だけを赤くすると「口座番号が間違っている」と誤解されるので、
                //  組み合わせを構成する4項目すべてに印を付ける
                .andExpect(model().attributeHasFieldErrors(FORM_NAME,
                        "bankCode", "branchCode", "bankAccountType", "bankAccountNum"))
                .andExpect(result -> {
                    BindingResult binding = (BindingResult) result.getModelAndView().getModel()
                            .get(BindingResult.MODEL_KEY_PREFIX + FORM_NAME);
                    //  理由はフォーム全体のエラーとして1回だけ出す
                    assertThat(binding.getGlobalErrors()).hasSize(1);
                    assertThat(binding.getGlobalErrors().get(0).getDefaultMessage())
                            .contains("登録されていません");
                    //  項目ごとのメッセージは空。同じ文言が4回並ばないようにしている
                    assertThat(binding.getFieldError("bankAccountNum").getDefaultMessage()).isEmpty();
                });
    }

    @Test
    @DisplayName("完了画面を直接開いても、表示するものが無ければ入力画面に戻される")
    void 完了画面への直接アクセス() throws Exception {
        //  リダイレクト後にもう一度再読み込みした場合もこれと同じ状態になる。
        //  申込がもう一度実行されることはない（GETなので何も登録しない）。
        mockMvc.perform(get("/investmentTrustCompletion"))
                .andExpect(view().name("redirect:/investmentTrust"));
    }
}
