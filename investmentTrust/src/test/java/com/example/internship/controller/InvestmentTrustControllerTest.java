package com.example.internship.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * 入力画面から確認画面へ進むときのサーバ側チェックのテスト。
 *
 * MockMvc を使うと、ブラウザもTomcatも起動せずにPOSTを再現できる。
 * つまり「JSを無効化して直接POSTした場合」をそのまま再現できるので、
 * フロントのチェックを迂回されても守れているかを確認できる。
 *
 * ※このテストは /investmentTrustConfirmation だけを叩く。
 *   この画面遷移は参照（マスタ検索）しか行わないので、開発用DBのデータは変化しない。
 *   登録まで行う /investmentTrustCompletion のテストを書くときは、
 *   データが増えてしまうのでテスト専用DBを用意すること。
 *
 * ※マスタを参照するので、事前に db/01〜04 のSQLが流してあることが前提。
 */
@SpringBootTest
@AutoConfigureMockMvc
class InvestmentTrustControllerTest {

    private static final String FORM_NAME = "investmentTrustApplication";

    @Autowired
    private MockMvc mockMvc;

    /** 全項目が正しいリクエストパラメータ。各テストはここから1つだけ書き換える */
    private MultiValueMap<String, String> validParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("bankCode", "0001");         //  山陰共同銀行
        params.add("branchCode", "002");        //  和白支店
        params.add("bankAccountType", "普通");
        params.add("bankAccountNum", "1234567");
        params.add("name", "ﾔﾏﾀﾞ ﾀﾛｳ");
        params.add("fundName", "キャピタル１");
        params.add("money", "10000");
        return params;
    }

    @Test
    @DisplayName("正しい入力なら確認画面へ進み、コードから引いた名称がフォームにセットされる")
    void 正常系() throws Exception {
        mockMvc.perform(post("/investmentTrustConfirmation").params(validParams()))
                .andExpect(status().isOk())
                .andExpect(view().name("investmentTrustConfirmation"))
                .andExpect(model().attribute(FORM_NAME, hasProperty("bankName", is("山陰共同銀行"))))
                .andExpect(model().attribute(FORM_NAME, hasProperty("branchName", is("和白支店"))));
    }

    @Test
    @DisplayName("画面から送られた金融機関名は無視され、コードから引き直した名称になる")
    void 名称を改ざんしても無視される() throws Exception {
        MultiValueMap<String, String> params = validParams();
        //  hidden項目を書き換えて、コードと食い違う名称を送り込んだ状況を再現する
        params.add("bankName", "こぶた銀行");
        params.add("branchName", "みどり支店");

        mockMvc.perform(post("/investmentTrustConfirmation").params(params))
                .andExpect(view().name("investmentTrustConfirmation"))
                //  送りつけた名称ではなく、コード0001/002 に対応する名称になっていること
                .andExpect(model().attribute(FORM_NAME, hasProperty("bankName", is("山陰共同銀行"))))
                .andExpect(model().attribute(FORM_NAME, hasProperty("branchName", is("和白支店"))));
    }

    @Test
    @DisplayName("存在しない金融機関コードなら入力画面に差し戻される")
    void 存在しない金融機関コード() throws Exception {
        MultiValueMap<String, String> params = validParams();
        params.set("bankCode", "9999");

        mockMvc.perform(post("/investmentTrustConfirmation").params(params))
                .andExpect(view().name("investmentTrustMain"))
                .andExpect(model().attributeHasFieldErrorCode(FORM_NAME, "bankCode", "notFound"))
                //  引けなかったので名称は空のまま
                .andExpect(model().attribute(FORM_NAME, hasProperty("bankName", nullValue())));
    }

    @Test
    @DisplayName("他の銀行にしか無い支店コードは弾かれる（複合キーで判定している）")
    void 他行の支店コードは通らない() throws Exception {
        MultiValueMap<String, String> params = validParams();
        params.set("bankCode", "0002");     //  こぶた銀行
        params.set("branchCode", "002");    //  002は山陰共同銀行の和白支店。こぶた銀行には存在しない

        mockMvc.perform(post("/investmentTrustConfirmation").params(params))
                .andExpect(view().name("investmentTrustMain"))
                .andExpect(model().attributeHasFieldErrorCode(FORM_NAME, "branchCode", "notFound"));
    }

    @Test
    @DisplayName("同じ支店コードでも、銀行が違えば別の支店として引かれる")
    void 同じ支店コードでも銀行ごとに別の支店() throws Exception {
        MultiValueMap<String, String> params = validParams();
        params.set("bankCode", "0002");     //  こぶた銀行
        params.set("branchCode", "001");    //  こぶた銀行の本店営業部

        mockMvc.perform(post("/investmentTrustConfirmation").params(params))
                .andExpect(view().name("investmentTrustConfirmation"))
                .andExpect(model().attribute(FORM_NAME, hasProperty("bankName", is("こぶた銀行"))))
                .andExpect(model().attribute(FORM_NAME, hasProperty("branchName", is("本店営業部"))));
    }

    @Test
    @DisplayName("金融機関コードの書式が不正なら、マスタ照会はせず書式エラーだけを出す")
    void 書式エラーのときはマスタ照会しない() throws Exception {
        MultiValueMap<String, String> params = validParams();
        params.set("bankCode", "abc");

        mockMvc.perform(post("/investmentTrustConfirmation").params(params))
                .andExpect(view().name("investmentTrustMain"))
                //  「該当する金融機関がありません(notFound)」ではなく書式エラー(Pattern)であること。
                //  1つの項目にメッセージを重ねて出さないようにしている
                .andExpect(model().attributeHasFieldErrorCode(FORM_NAME, "bankCode", "Pattern"));
    }

    @Test
    @DisplayName("金融機関が引けないときは、支店の判定まで進まない")
    void 金融機関が不正なら支店は判定しない() throws Exception {
        MultiValueMap<String, String> params = validParams();
        params.set("bankCode", "9999");
        params.set("branchCode", "999");    //  どの銀行にも無い支店コード

        mockMvc.perform(post("/investmentTrustConfirmation").params(params))
                .andExpect(view().name("investmentTrustMain"))
                .andExpect(model().attributeHasFieldErrors(FORM_NAME, "bankCode"))
                //  先に金融機関を直してもらうため、支店にはエラーを出さない
                .andExpect(model().attributeHasNoErrors());
    }

    @Test
    @DisplayName("科目名に選択肢に無い値が送られたら弾かれる")
    void 選択肢に無い科目名() throws Exception {
        MultiValueMap<String, String> params = validParams();
        params.set("bankAccountType", "普通口座");    //  ラジオボタンに変える前の古い値

        mockMvc.perform(post("/investmentTrustConfirmation").params(params))
                .andExpect(view().name("investmentTrustMain"))
                .andExpect(model().attributeHasFieldErrorCode(FORM_NAME, "bankAccountType", "invalidOption"));
    }

    @Test
    @DisplayName("パラメータを何も送らなくても落ちず、入力画面に差し戻される")
    void 空のリクエスト() throws Exception {
        mockMvc.perform(post("/investmentTrustConfirmation"))
                .andExpect(status().isOk())
                .andExpect(view().name("investmentTrustMain"))
                .andExpect(model().attributeHasFieldErrors(FORM_NAME,
                        "bankCode", "branchCode", "bankAccountType", "bankAccountNum", "name", "fundName", "money"));
    }
}
