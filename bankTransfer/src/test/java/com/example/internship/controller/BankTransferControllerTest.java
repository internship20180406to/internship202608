package com.example.internship.controller;

import com.example.internship.entity.BankTransferInput;
import com.example.internship.master.Bank;
import com.example.internship.master.BankMasterRepository;
import com.example.internship.master.Branch;
import com.example.internship.master.BranchMasterRepository;
import com.example.internship.service.ApplyBankTransferService;
import com.example.internship.user.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

// Web層だけを起動する。DataSourceは読み込まれないのでMySQLが動いていなくても実行できる。
// マスタとDB登録は差し替え、画面の流れとガードだけを見る。
@WebMvcTest(BankTransferController.class)
@Import(CurrentUser.class)
@DisplayName("振込6画面の流れ")
class BankTransferControllerTest {

    private static final String TOKEN_KEY = "transferToken";
    private static final String INPUT_KEY = "bankTransferInput";

    private static final Bank BANK = new Bank("0001", "AAA銀行");
    private static final Branch BRANCH = new Branch("0001", "001", "A1支店");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplyBankTransferService applyBankTransferService;

    @MockitoBean
    private BankMasterRepository bankMasterRepository;

    @MockitoBean
    private BranchMasterRepository branchMasterRepository;

    @BeforeEach
    void setUpMaster() {
        when(bankMasterRepository.findMajor()).thenReturn(List.of(BANK));
        when(bankMasterRepository.findByCode("0001")).thenReturn(Optional.of(BANK));
        when(bankMasterRepository.findByCode("9999")).thenReturn(Optional.empty());
        when(branchMasterRepository.find("0001", "001")).thenReturn(Optional.of(BRANCH));
        when(branchMasterRepository.find("0001", "999")).thenReturn(Optional.empty());
    }

    // ステッパーの段数を数える。文字列の出現回数をそのまま数えるだけ
    private static int countOccurrences(String text, String word) {
        int count = 0;
        for (int at = text.indexOf(word); at >= 0; at = text.indexOf(word, at + word.length())) {
            count++;
        }
        return count;
    }

    private String tomorrow() {
        return LocalDate.now().plusDays(1).toString();
    }

    // 画面1から画面4までを順に通し、確認画面で発行されたトークンを返す
    private String walkToConfirmation(MockHttpSession session) throws Exception {
        mockMvc.perform(post("/bankTransfer/bank").param("bankCode", "0001").session(session))
                .andExpect(redirectedUrl("/bankTransfer/branch"));
        mockMvc.perform(post("/bankTransfer/branch").param("branchCode", "001").session(session))
                .andExpect(redirectedUrl("/bankTransfer/account"));
        mockMvc.perform(post("/bankTransfer/account").session(session)
                        .param("bankAccountType", "普通")
                        .param("bankAccountNum", "1234567")
                        .param("name", "ﾔﾏﾀﾞ ﾀﾛｳ"))
                .andExpect(redirectedUrl("/bankTransfer/amount"));
        mockMvc.perform(post("/bankTransfer/amount").session(session)
                        .param("money", "1000")
                        .param("transferDateTime", tomorrow()))
                .andExpect(redirectedUrl("/bankTransfer/confirmation"));
        mockMvc.perform(get("/bankTransfer/confirmation").session(session))
                .andExpect(view().name("bankTransferConfirmation"));

        Object token = session.getAttribute(TOKEN_KEY);
        assertThat(token).as("確認画面でトークンが発行されること").isNotNull();
        return token.toString();
    }

    @Nested
    @DisplayName("画面の表示と選択")
    class Screens {

        @Test
        @DisplayName("入口に振込先の指定方法が出る")
        void 入口() throws Exception {
            mockMvc.perform(get("/bankTransfer"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bankTransferStart"))
                    // 手順に入っていないのでステッパーは出さない
                    .andExpect(content().string(not(containsString("stepper-item"))));
        }

        @Test
        @DisplayName("画面1に金融機関の一覧が出る")
        void 金融機関の選択画面() throws Exception {
            mockMvc.perform(get("/bankTransfer/bank"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bankTransferBank"))
                    .andExpect(model().attribute("banks", List.of(BANK)));
        }

        @Test
        @DisplayName("通常の振込は6段のステッパーになる")
        void 通常経路のステッパー() throws Exception {
            String html = mockMvc.perform(get("/bankTransfer/bank"))
                    .andReturn().getResponse().getContentAsString();

            assertThat(html).contains("金融機関", "支店", "口座情報", "金額", "確認", "完了");
            // 狭い幅で出る方の数え上げも同じ総数を指している
            assertThat(html).contains("/ <span>6</span>");
            assertThat(countOccurrences(html, "stepper-item")).isEqualTo(6);
        }

        @Test
        @DisplayName("金融機関を選ぶと支店の選択へ進む")
        void 金融機関を選ぶ() throws Exception {
            MockHttpSession session = new MockHttpSession();

            mockMvc.perform(post("/bankTransfer/bank").param("bankCode", "0001").session(session))
                    .andExpect(redirectedUrl("/bankTransfer/branch"));

            BankTransferInput input = (BankTransferInput) session.getAttribute(INPUT_KEY);
            assertThat(input.getBankName()).isEqualTo("AAA銀行");
        }

        @Test
        @DisplayName("一覧に無い金融機関コードは受け付けない")
        void 存在しない金融機関() throws Exception {
            MockHttpSession session = new MockHttpSession();

            mockMvc.perform(post("/bankTransfer/bank").param("bankCode", "9999").session(session))
                    .andExpect(redirectedUrl("/bankTransfer"));

            BankTransferInput input = (BankTransferInput) session.getAttribute(INPUT_KEY);
            assertThat(input == null || input.getBankCode() == null).isTrue();
        }

        @Test
        @DisplayName("その銀行の支店でないコードは受け付けない")
        void 別の銀行の支店() throws Exception {
            MockHttpSession session = new MockHttpSession();
            mockMvc.perform(post("/bankTransfer/bank").param("bankCode", "0001").session(session));

            mockMvc.perform(post("/bankTransfer/branch").param("branchCode", "999").session(session))
                    .andExpect(redirectedUrl("/bankTransfer/branch"));

            BankTransferInput input = (BankTransferInput) session.getAttribute(INPUT_KEY);
            assertThat(input.getBranchCode()).isNull();
        }

        @Test
        @DisplayName("金融機関を選び直すと支店は取り消される")
        void 金融機関を変えると支店が外れる() throws Exception {
            MockHttpSession session = new MockHttpSession();
            mockMvc.perform(post("/bankTransfer/bank").param("bankCode", "0001").session(session));
            mockMvc.perform(post("/bankTransfer/branch").param("branchCode", "001").session(session));

            mockMvc.perform(post("/bankTransfer/bank").param("bankCode", "0001").session(session));

            BankTransferInput input = (BankTransferInput) session.getAttribute(INPUT_KEY);
            assertThat(input.getBranchCode()).isNull();
        }
    }

    @Nested
    @DisplayName("順番を飛ばしたアクセスへのガード")
    class Guards {

        @Test
        @DisplayName("金融機関が未選択なら支店の画面は開けない")
        void 支店画面への直接アクセス() throws Exception {
            mockMvc.perform(get("/bankTransfer/branch"))
                    .andExpect(redirectedUrl("/bankTransfer"));
        }

        @Test
        @DisplayName("支店が未選択なら口座情報の画面は開けない")
        void 口座画面への直接アクセス() throws Exception {
            mockMvc.perform(get("/bankTransfer/account"))
                    .andExpect(redirectedUrl("/bankTransfer"));
        }

        @Test
        @DisplayName("口座情報が未入力なら金額の画面は開けない")
        void 金額画面への直接アクセス() throws Exception {
            mockMvc.perform(get("/bankTransfer/amount"))
                    .andExpect(redirectedUrl("/bankTransfer"));
        }

        @Test
        @DisplayName("入力が揃っていなければ確認画面は開けない")
        void 確認画面への直接アクセス() throws Exception {
            mockMvc.perform(get("/bankTransfer/confirmation"))
                    .andExpect(redirectedUrl("/bankTransfer"));
        }

        @Test
        @DisplayName("完了画面に直接アクセスすると入力画面へ戻される")
        void 完了画面への直接アクセス() throws Exception {
            mockMvc.perform(get("/bankTransfer/completion"))
                    .andExpect(redirectedUrl("/bankTransfer"));
        }

        @Test
        @DisplayName("口座情報が一部しか埋まっていなければ金額の画面は開けない")
        void 口座情報が欠けている() throws Exception {
            // 口座番号だけ入っていて名義が無い、という中途半端な状態を直接作る
            BankTransferInput input = new BankTransferInput();
            input.setBankCode("0001");
            input.setBankName("AAA銀行");
            input.setBranchCode("001");
            input.setBranchName("A1支店");
            input.setBankAccountNum("1234567");

            MockHttpSession session = new MockHttpSession();
            session.setAttribute(INPUT_KEY, input);

            mockMvc.perform(get("/bankTransfer/amount").session(session))
                    .andExpect(redirectedUrl("/bankTransfer"));
        }
    }

    @Nested
    @DisplayName("入力チェック")
    class Validation {

        @Test
        @DisplayName("口座情報に誤りがあればその画面に留まる")
        void 口座情報のエラー() throws Exception {
            MockHttpSession session = new MockHttpSession();
            mockMvc.perform(post("/bankTransfer/bank").param("bankCode", "0001").session(session));
            mockMvc.perform(post("/bankTransfer/branch").param("branchCode", "001").session(session));

            mockMvc.perform(post("/bankTransfer/account").session(session)
                            .param("bankAccountType", "普通")
                            .param("bankAccountNum", "12345678")
                            .param("name", "ﾔﾏﾀﾞ"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bankTransferAccount"))
                    .andExpect(model().attributeHasFieldErrors("accountForm", "bankAccountNum"));
        }

        @Test
        @DisplayName("金額に誤りがあればその画面に留まる")
        void 金額のエラー() throws Exception {
            MockHttpSession session = new MockHttpSession();
            mockMvc.perform(post("/bankTransfer/bank").param("bankCode", "0001").session(session));
            mockMvc.perform(post("/bankTransfer/branch").param("branchCode", "001").session(session));
            mockMvc.perform(post("/bankTransfer/account").session(session)
                    .param("bankAccountType", "普通")
                    .param("bankAccountNum", "1234567")
                    .param("name", "ﾔﾏﾀﾞ"));

            mockMvc.perform(post("/bankTransfer/amount").session(session)
                            .param("money", "0")
                            .param("transferDateTime", tomorrow()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bankTransferAmount"))
                    .andExpect(model().attributeHasFieldErrors("amountForm", "money"));
        }
    }

    @Nested
    @DisplayName("申し込みの確定")
    class Completion {

        @Test
        @DisplayName("最後まで通すと登録され、コードも一緒に保存される")
        void 正常系() throws Exception {
            MockHttpSession session = new MockHttpSession();
            String token = walkToConfirmation(session);

            mockMvc.perform(post("/bankTransfer/completion").param(TOKEN_KEY, token).session(session))
                    .andExpect(redirectedUrl("/bankTransfer/completion"))
                    .andExpect(flash().attributeExists("bankTransferResult"));

            ArgumentCaptor<BankTransferInput> captor = ArgumentCaptor.forClass(BankTransferInput.class);
            verify(applyBankTransferService).applyBankTransfer(eq("demo"), captor.capture());

            BankTransferInput saved = captor.getValue();
            assertThat(saved.getBankCode()).isEqualTo("0001");
            assertThat(saved.getBankName()).isEqualTo("AAA銀行");
            assertThat(saved.getBranchCode()).isEqualTo("001");
            assertThat(saved.getBranchName()).isEqualTo("A1支店");
            assertThat(saved.getMoney()).isEqualTo(1000);
        }

        @Test
        @DisplayName("利用者を切り替えると、その利用者の振込として記録される")
        void 記録される利用者() throws Exception {
            MockHttpSession session = new MockHttpSession();
            // 入口で利用者を切り替えてから、最後まで通す
            mockMvc.perform(get("/bankTransfer").param("userId", "taro").session(session));
            String token = walkToConfirmation(session);

            mockMvc.perform(post("/bankTransfer/completion").param(TOKEN_KEY, token).session(session))
                    .andExpect(redirectedUrl("/bankTransfer/completion"));

            // 履歴をこの利用者の分だけに絞れるかは、ここが正しいことに乗っている
            verify(applyBankTransferService)
                    .applyBankTransfer(eq("taro"), any(BankTransferInput.class));
        }

        @Test
        @DisplayName("同じトークンで2回送っても登録は1回だけ")
        void 二重送信は登録されない() throws Exception {
            MockHttpSession session = new MockHttpSession();
            String token = walkToConfirmation(session);

            mockMvc.perform(post("/bankTransfer/completion").param(TOKEN_KEY, token).session(session))
                    .andExpect(redirectedUrl("/bankTransfer/completion"));
            mockMvc.perform(post("/bankTransfer/completion").param(TOKEN_KEY, token).session(session))
                    .andExpect(redirectedUrl("/bankTransfer"));

            verify(applyBankTransferService, times(1)).applyBankTransfer(anyString(), any(BankTransferInput.class));
        }

        @Test
        @DisplayName("トークンが無ければ登録されない")
        void トークン無し() throws Exception {
            MockHttpSession session = new MockHttpSession();
            walkToConfirmation(session);

            mockMvc.perform(post("/bankTransfer/completion").session(session))
                    .andExpect(redirectedUrl("/bankTransfer"));

            verify(applyBankTransferService, never()).applyBankTransfer(anyString(), any(BankTransferInput.class));
        }

        @Test
        @DisplayName("登録が済むとセッションの入力内容は破棄される")
        void 登録後の後片付け() throws Exception {
            MockHttpSession session = new MockHttpSession();
            String token = walkToConfirmation(session);

            mockMvc.perform(post("/bankTransfer/completion").param(TOKEN_KEY, token).session(session));

            assertThat(session.getAttribute(INPUT_KEY)).isNull();
            assertThat(session.getAttribute(TOKEN_KEY)).isNull();
        }
    }
}
