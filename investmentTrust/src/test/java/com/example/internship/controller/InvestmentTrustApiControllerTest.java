package com.example.internship.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * チャット画面が使うAPIのテスト。
 *
 * 画面版とAPIは InvestmentTrustValidator と OrderInvestmentTrustService を共有している。
 * ここで確かめたいのは「入口が変わっても同じように弾かれるか」で、
 * JSを通さず直接POSTしても素通りしないことを固定している。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InvestmentTrustApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** 残高10,000,000円の口座を使う申込 */
    private String validOrder() {
        return "{\"bankCode\":\"0001\",\"branchCode\":\"002\",\"bankAccountType\":\"貯蓄\","
                + "\"bankAccountNum\":\"0031111\",\"name\":\"ｵｶﾈ ﾅｲ\","
                + "\"fundName\":\"キャピタル１\",\"money\":50000}";
    }

    private int countOrders() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM investmenttrust_table", Integer.class);
    }

    private long balanceOf(String num) {
        return jdbcTemplate.queryForObject(
                "SELECT balance FROM account_balance WHERE accountNum = ?", Long.class, num);
    }

    @Test
    @DisplayName("口座照会は名義と残高を返す")
    void 口座照会() throws Exception {
        mockMvc.perform(get("/api/accounts")
                        .param("bankCode", "0001").param("branchCode", "002")
                        .param("accountType", "貯蓄").param("accountNum", "0031111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountName").value("ｵｶﾈ ﾅｲ"))
                .andExpect(jsonPath("$.balance").value(10000000));
    }

    @Test
    @DisplayName("口座照会も4点セットで判定する。支店が違えば見つからない")
    void 口座照会は4点セット() throws Exception {
        mockMvc.perform(get("/api/accounts")
                        .param("bankCode", "0001").param("branchCode", "101")
                        .param("accountType", "貯蓄").param("accountNum", "0031111"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("正しい申込は登録され、引き落とし後の残高が返る")
    void 申込の成立() throws Exception {
        int before = countOrders();

        mockMvc.perform(post("/api/investmentTrust/order")
                        .contentType(MediaType.APPLICATION_JSON).content(validOrder()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                //  名称は送っていない。サーバがコードから引いた値が返る
                .andExpect(jsonPath("$.bankName").value("山陰共同銀行"))
                .andExpect(jsonPath("$.branchName").value("和白支店"))
                .andExpect(jsonPath("$.balanceAfter").value(9950000));

        assertThat(countOrders()).isEqualTo(before + 1);
        assertThat(balanceOf("0031111")).isEqualTo(9950000L);
    }

    @Test
    @DisplayName("書式エラーは項目ごとのメッセージで返る")
    void 書式エラー() throws Exception {
        String body = validOrder().replace("\"0001\"", "\"abc\"");

        mockMvc.perform(post("/api/investmentTrust/order")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.fieldErrors.bankCode").value("金融機関コードは半角数字4桁で入力してください。"));
    }

    @Test
    @DisplayName("口座が無い場合は、項目ごとではなく全体のエラーとして返る")
    void 口座が見つからない() throws Exception {
        //  101 博多支店は実在するが、そこに口座0031111は無い
        String body = validOrder().replace("\"002\"", "\"101\"");
        int before = countOrders();

        mockMvc.perform(post("/api/investmentTrust/order")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                //  4項目のどれが違うかは分からないので、項目には空のエラーしか付けていない。
                //  APIでは空メッセージを落として全体エラーだけを返す
                .andExpect(jsonPath("$.globalErrors[0]").value(
                        "入力された口座は登録されていません。金融機関・支店・科目・口座番号の組み合わせをご確認ください。"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());

        assertThat(countOrders()).isEqualTo(before);
    }

    @Test
    @DisplayName("残高不足なら申込は登録されず、残高も変わらない")
    void 残高不足() throws Exception {
        //  残高15,000円の口座に50,000円の申込
        String body = "{\"bankCode\":\"0002\",\"branchCode\":\"012\",\"bankAccountType\":\"普通\","
                + "\"bankAccountNum\":\"9999999\",\"name\":\"ﾉｺﾘ ｽｸﾅｲ\","
                + "\"fundName\":\"キャピタル１\",\"money\":50000}";
        int before = countOrders();

        mockMvc.perform(post("/api/investmentTrust/order")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.money").value("残高が不足しています。（残高: 15,000円）"));

        assertThat(countOrders()).isEqualTo(before);
        assertThat(balanceOf("9999999")).isEqualTo(15000L);
    }

    @Test
    @DisplayName("画面から名称を送りつけても、コードから引き直した名称で登録される")
    void 名称を送っても無視される() throws Exception {
        String body = validOrder().replace("\"money\":50000",
                "\"bankName\":\"こぶた銀行\",\"branchName\":\"みどり支店\",\"money\":50000");

        mockMvc.perform(post("/api/investmentTrust/order")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bankName").value("山陰共同銀行"));

        String registered = jdbcTemplate.queryForObject(
                "SELECT bankName FROM investmenttrust_table ORDER BY id DESC LIMIT 1", String.class);
        assertThat(registered).isEqualTo("山陰共同銀行");
    }
}
