package com.example.internship.controller;

import com.example.internship.balance.BalanceRepository;
import com.example.internship.entity.BankTransferInput;
import com.example.internship.fee.TransferFee;
import com.example.internship.history.RecentPayee;
import com.example.internship.history.TransferHistoryRepository;
import com.example.internship.payee.Payee;
import com.example.internship.payee.PayeeRepository;
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
import org.springframework.test.web.servlet.MvcResult;

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
// 手数料の決まりは差し替えず、本物をそのまま動かす
@Import({ CurrentUser.class, TransferFee.class })
@DisplayName("振込6画面の流れ")
class BankTransferControllerTest {

    private static final String TOKEN_KEY = "transferToken";
    private static final String INPUT_KEY = "bankTransferInput";

    private static final Bank BANK = new Bank("0001", "AAA銀行");
    // 自行あては手数料が無料になるので、他行と分けて用意する
    private static final Bank OWN_BANK = new Bank(TransferFee.OWN_BANK_CODE, "ふくよか銀行");
    private static final Branch OWN_BRANCH =
            new Branch(TransferFee.OWN_BANK_CODE, "001", "本店");
    private static final RecentPayee PAYEE = new RecentPayee(
            "0001", "AAA銀行", "001", "A1支店", "普通", "1234567", "ﾔﾏﾀﾞ ﾀﾛｳ",
            java.time.LocalDate.of(2026, 8, 20));
    private static final Payee SAVED = new Payee(
            7, "家賃", "0001", "AAA銀行", "001", "A1支店", "普通", "1234567", "ﾔﾏﾀﾞ ﾀﾛｳ");
    private static final Branch BRANCH = new Branch("0001", "001", "A1支店");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplyBankTransferService applyBankTransferService;

    @MockitoBean
    private BankMasterRepository bankMasterRepository;

    @MockitoBean
    private BranchMasterRepository branchMasterRepository;

    @MockitoBean
    private TransferHistoryRepository transferHistoryRepository;

    @MockitoBean
    private PayeeRepository payeeRepository;

    @MockitoBean
    private BalanceRepository balanceRepository;

    @BeforeEach
    void setUpMaster() {
        when(bankMasterRepository.findMajor()).thenReturn(List.of(BANK));
        when(bankMasterRepository.findByCode("0001")).thenReturn(Optional.of(BANK));
        when(bankMasterRepository.findByCode("9999")).thenReturn(Optional.empty());
        when(bankMasterRepository.findByCode(TransferFee.OWN_BANK_CODE))
                .thenReturn(Optional.of(OWN_BANK));
        when(branchMasterRepository.find(TransferFee.OWN_BANK_CODE, "001"))
                .thenReturn(Optional.of(OWN_BRANCH));
        when(branchMasterRepository.find("0001", "001")).thenReturn(Optional.of(BRANCH));
        when(branchMasterRepository.find("0001", "999")).thenReturn(Optional.empty());
        when(transferHistoryRepository.findRecent("demo")).thenReturn(List.of(PAYEE));
        when(transferHistoryRepository.find("demo", "0001", "001", "普通", "1234567"))
                .thenReturn(Optional.of(PAYEE));
        when(payeeRepository.findAll("demo")).thenReturn(List.of(SAVED));
        when(payeeRepository.find("demo", 7)).thenReturn(Optional.of(SAVED));
        when(payeeRepository.find("demo", 999)).thenReturn(Optional.empty());
        when(payeeRepository.create(anyString(), anyString(), any(BankTransferInput.class)))
                .thenReturn(true);
        // 既定はどの利用者も同じ残高。個別に変えたいテストが後から上書きする
        when(balanceRepository.amountOf(anyString())).thenReturn(1_000_000);
        when(applyBankTransferService.applyBankTransfer(anyString(), any(BankTransferInput.class)))
                .thenReturn(true);
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

    // 画面1から画面3まで通し、金額画面に入れる状態にする
    private void walkToAccount(MockHttpSession session) throws Exception {
        mockMvc.perform(post("/bankTransfer/bank").param("bankCode", "0001").session(session));
        mockMvc.perform(post("/bankTransfer/branch").param("branchCode", "001").session(session));
        mockMvc.perform(post("/bankTransfer/account")
                .param("bankAccountType", "普通").param("bankAccountNum", "1234567")
                .param("name", "ﾔﾏﾀﾞ ﾀﾛｳ").session(session));
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
        @DisplayName("入口は履歴タブで、その利用者の履歴が出る")
        void 入口() throws Exception {
            mockMvc.perform(get("/bankTransfer"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bankTransferStart"))
                    .andExpect(model().attribute("tab", "history"))
                    .andExpect(model().attribute("payees", List.of(PAYEE)))
                    // 手順に入っていないのでステッパーは出さない
                    .andExpect(content().string(not(containsString("stepper-item"))));
        }

        @Test
        @DisplayName("登録済みタブは同じ画面で、中身だけ入れ替わる")
        void 登録済みタブ() throws Exception {
            mockMvc.perform(get("/bankTransfer/payees"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bankTransferStart"))
                    .andExpect(model().attribute("tab", "payees"))
                    .andExpect(model().attribute("payees", List.of(SAVED)));
        }

        @Test
        @DisplayName("古い履歴一覧のURLは入口へ送る")
        void 履歴URLは入口へ() throws Exception {
            mockMvc.perform(get("/bankTransfer/history"))
                    .andExpect(redirectedUrl("/bankTransfer"));
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
    @DisplayName("完了画面")
    class CompletionScreen {

        // 振込を最後まで通し、完了画面のHTMLを返す。
        // 完了画面の内容はflash属性で渡されるので、POSTが残したものをGETへ引き継ぐ
        private String renderCompletion() throws Exception {
            MockHttpSession session = new MockHttpSession();
            String token = walkToConfirmation(session);
            MvcResult posted = mockMvc.perform(post("/bankTransfer/completion")
                    .param(TOKEN_KEY, token).session(session)).andReturn();
            return mockMvc.perform(get("/bankTransfer/completion")
                            .session(session).flashAttrs(posted.getFlashMap()))
                    .andReturn().getResponse().getContentAsString();
        }

        @Test
        @DisplayName("完了の見出しと印が出る")
        void 見出し() throws Exception {
            String html = renderCompletion();

            assertThat(html).contains("振込が完了しました").contains("done-check");
        }

        @Test
        @DisplayName("未登録の相手には登録ボタンが出る")
        void 登録ボタンが出る() throws Exception {
            when(payeeRepository.exists(anyString(), any(BankTransferInput.class))).thenReturn(false);

            String html = renderCompletion();

            assertThat(html).contains("この口座情報を登録する")
                    .contains("/bankTransfer/completion/register");
        }

        @Test
        @DisplayName("登録済みの相手には登録ボタンを出さない")
        void 登録済みなら出さない() throws Exception {
            when(payeeRepository.exists(anyString(), any(BankTransferInput.class))).thenReturn(true);

            String html = renderCompletion();

            assertThat(html).doesNotContain("この口座情報を登録する")
                    .contains("登録済みです");
        }

        @Test
        @DisplayName("完了画面から登録すると、呼び名の画面へ振込先が埋まった状態で進む")
        void 完了画面から登録() throws Exception {
            MockHttpSession session = new MockHttpSession();

            mockMvc.perform(post("/bankTransfer/completion/register")
                            .param("bankCode", "0001").param("branchCode", "001")
                            .param("bankAccountType", "普通").param("bankAccountNum", "1234567")
                            .session(session))
                    .andExpect(redirectedUrl("/bankTransfer/payee/confirm"));

            BankTransferInput input = (BankTransferInput) session.getAttribute(INPUT_KEY);
            assertThat(input.getBankName()).isEqualTo("AAA銀行");
            assertThat(input.hasAccount()).isTrue();
            // 登録に金額は要らない
            assertThat(input.getMoney()).isNull();
        }

        @Test
        @DisplayName("自分の履歴に無い相手は完了画面からでも登録できない")
        void 履歴に無い相手は登録できない() throws Exception {
            MockHttpSession session = new MockHttpSession();

            mockMvc.perform(post("/bankTransfer/completion/register")
                            .param("bankCode", "9999").param("branchCode", "999")
                            .param("bankAccountType", "普通").param("bankAccountNum", "0000000")
                            .session(session))
                    .andExpect(redirectedUrl("/bankTransfer"));

            assertThat(session.getAttribute(INPUT_KEY)).isNull();
        }
    }

    @Nested
    @DisplayName("手数料")
    class Fee {

        // 自行（ふくよか銀行）あてで画面3まで進む
        private void walkToAccountOwnBank(MockHttpSession session) throws Exception {
            mockMvc.perform(post("/bankTransfer/bank")
                    .param("bankCode", TransferFee.OWN_BANK_CODE).session(session));
            mockMvc.perform(post("/bankTransfer/branch").param("branchCode", "001").session(session));
            mockMvc.perform(post("/bankTransfer/account")
                    .param("bankAccountType", "普通").param("bankAccountNum", "1234567")
                    .param("name", "ﾔﾏﾀﾞ ﾀﾛｳ").session(session));
        }

        @Test
        @DisplayName("含めないときは、打った額が振込額になり手数料が上乗せされる")
        void 含めない() throws Exception {
            MockHttpSession session = new MockHttpSession();
            walkToAccount(session);

            mockMvc.perform(post("/bankTransfer/amount")
                            .param("money", "10000").param("transferDateTime", tomorrow())
                            .session(session))
                    .andExpect(redirectedUrl("/bankTransfer/confirmation"));

            BankTransferInput input = (BankTransferInput) session.getAttribute(INPUT_KEY);
            assertThat(input.getMoney()).isEqualTo(10000);
            assertThat(input.getFee()).isEqualTo(220);
            assertThat(input.getTotal()).isEqualTo(10220);
        }

        @Test
        @DisplayName("含めるときは、打った額から手数料が引かれた分が振込額になる")
        void 含める() throws Exception {
            MockHttpSession session = new MockHttpSession();
            walkToAccount(session);

            mockMvc.perform(post("/bankTransfer/amount")
                            .param("money", "10000").param("feeIncluded", "true")
                            .param("transferDateTime", tomorrow()).session(session))
                    .andExpect(redirectedUrl("/bankTransfer/confirmation"));

            BankTransferInput input = (BankTransferInput) session.getAttribute(INPUT_KEY);
            assertThat(input.getMoney()).isEqualTo(9780);
            assertThat(input.getFee()).isEqualTo(220);
            // 口座から引かれるのは打った額ちょうど
            assertThat(input.getTotal()).isEqualTo(10000);
        }

        @Test
        @DisplayName("3万円以上は手数料が上がる")
        void 段が変わる() throws Exception {
            MockHttpSession session = new MockHttpSession();
            walkToAccount(session);

            mockMvc.perform(post("/bankTransfer/amount")
                    .param("money", "30000").param("transferDateTime", tomorrow()).session(session));

            BankTransferInput input = (BankTransferInput) session.getAttribute(INPUT_KEY);
            assertThat(input.getFee()).isEqualTo(330);
        }

        @Test
        @DisplayName("自行あては手数料がかからず、選択欄も出さない")
        void 自行あて() throws Exception {
            MockHttpSession session = new MockHttpSession();
            walkToAccountOwnBank(session);

            mockMvc.perform(get("/bankTransfer/amount").session(session))
                    .andExpect(model().attribute("ownBank", true))
                    .andExpect(content().string(not(containsString("手数料を入力金額に含める"))));

            mockMvc.perform(post("/bankTransfer/amount")
                    .param("money", "10000").param("transferDateTime", tomorrow()).session(session));

            BankTransferInput input = (BankTransferInput) session.getAttribute(INPUT_KEY);
            assertThat(input.getFee()).isZero();
            assertThat(input.getTotal()).isEqualTo(10000);
        }

        @Test
        @DisplayName("手数料以下の額を含めようとすると弾く")
        void 手数料が引けない() throws Exception {
            MockHttpSession session = new MockHttpSession();
            walkToAccount(session);

            mockMvc.perform(post("/bankTransfer/amount")
                            .param("money", "220").param("feeIncluded", "true")
                            .param("transferDateTime", tomorrow()).session(session))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("振込額が残りません")));
        }

        @Test
        @DisplayName("1回の上限200万円を超える額は弾く")
        void 上限() throws Exception {
            MockHttpSession session = new MockHttpSession();
            walkToAccount(session);

            mockMvc.perform(post("/bankTransfer/amount")
                            .param("money", "2000001").param("transferDateTime", tomorrow())
                            .session(session))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("2,000,000円までです")));

            // ちょうど200万は通る（残高は足りている前提）
            when(balanceRepository.amountOf(anyString())).thenReturn(10_000_000);
            mockMvc.perform(post("/bankTransfer/amount")
                            .param("money", "2000000").param("transferDateTime", tomorrow())
                            .session(session))
                    .andExpect(redirectedUrl("/bankTransfer/confirmation"));
        }

        @Test
        @DisplayName("確認画面に振込金額・手数料・合計金額が出る")
        void 確認画面の内訳() throws Exception {
            MockHttpSession session = new MockHttpSession();
            walkToConfirmation(session);

            String html = mockMvc.perform(get("/bankTransfer/confirmation").session(session))
                    .andReturn().getResponse().getContentAsString();

            assertThat(html).contains("振込金額").contains("手数料").contains("合計金額");
        }
    }

    @Nested
    @DisplayName("残高")
    class Balance {

        @Test
        @DisplayName("金額画面に振込可能額が出る")
        void 残高を出す() throws Exception {
            MockHttpSession session = new MockHttpSession();
            walkToAccount(session);

            mockMvc.perform(get("/bankTransfer/amount").session(session))
                    .andExpect(model().attribute("balance", 1_000_000))
                    .andExpect(content().string(containsString("1,000,000")));
        }

        @Test
        @DisplayName("残高より多い額は金額画面で弾く")
        void 残高超過は進めない() throws Exception {
            MockHttpSession session = new MockHttpSession();
            walkToAccount(session);

            mockMvc.perform(post("/bankTransfer/amount")
                            .param("money", "1000001").param("transferDateTime", tomorrow())
                            .session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bankTransferAmount"))
                    .andExpect(content().string(containsString("残高が不足しています")));

            BankTransferInput input = (BankTransferInput) session.getAttribute(INPUT_KEY);
            assertThat(input.getMoney()).isNull();
        }

        @Test
        @DisplayName("残高ちょうど（手数料込み）は通る")
        void 残高ちょうど() throws Exception {
            MockHttpSession session = new MockHttpSession();
            walkToAccount(session);

            // 手数料330円を足してちょうど残高100万円になる額
            mockMvc.perform(post("/bankTransfer/amount")
                            .param("money", "999670").param("transferDateTime", tomorrow())
                            .session(session))
                    .andExpect(redirectedUrl("/bankTransfer/confirmation"));
        }

        @Test
        @DisplayName("手数料を足すと残高を超える額は弾く")
        void 手数料込みで超過() throws Exception {
            MockHttpSession session = new MockHttpSession();
            walkToAccount(session);

            // 振込額だけなら足りるが、手数料330円を足すと足りない
            mockMvc.perform(post("/bankTransfer/amount")
                            .param("money", "1000000").param("transferDateTime", tomorrow())
                            .session(session))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("残高が不足しています")));
        }

        @Test
        @DisplayName("確認画面に振込前と振込後の残高が出る")
        void 確認画面の残高() throws Exception {
            MockHttpSession session = new MockHttpSession();
            walkToConfirmation(session);

            mockMvc.perform(get("/bankTransfer/confirmation").session(session))
                    .andExpect(model().attribute("balance", 1_000_000))
                    // 引かれるのは振込額1,000円と手数料220円の合計
                    .andExpect(model().attribute("balanceAfter", 998_780));
        }

        @Test
        @DisplayName("確定の直前に残高が足りなければ登録せず金額画面へ戻す")
        void 直前の残高不足() throws Exception {
            MockHttpSession session = new MockHttpSession();
            String token = walkToConfirmation(session);
            // 確認画面を見てから振込ボタンを押すまでの間に、残高が減った
            when(applyBankTransferService.applyBankTransfer(anyString(), any(BankTransferInput.class)))
                    .thenReturn(false);
            when(balanceRepository.amountOf("demo")).thenReturn(500);

            mockMvc.perform(post("/bankTransfer/completion").param(TOKEN_KEY, token).session(session))
                    .andExpect(redirectedUrl("/bankTransfer/amount"))
                    .andExpect(flash().attributeExists("balanceShort"));

            // 入力内容は消さない。金額を入れ直せば続けられる
            assertThat(session.getAttribute(INPUT_KEY)).isNotNull();
        }

        @Test
        @DisplayName("完了画面に振込前と振込後の残高が渡る")
        void 完了画面の残高() throws Exception {
            MockHttpSession session = new MockHttpSession();
            String token = walkToConfirmation(session);
            when(balanceRepository.amountOf("demo")).thenReturn(999_000);

            mockMvc.perform(post("/bankTransfer/completion").param(TOKEN_KEY, token).session(session))
                    .andExpect(flash().attribute("balanceAfter", 999_000))
                    // 振込前 = 振込後 + 合計額（振込額1,000円 + 手数料220円）
                    .andExpect(flash().attribute("balance", 1_000_220));
        }
    }

    @Nested
    @DisplayName("中止")
    class Cancel {

        @Test
        @DisplayName("入力途中の内容と経路の記憶を捨てて入口へ戻る")
        void 中止する() throws Exception {
            MockHttpSession session = new MockHttpSession();
            walkToConfirmation(session);
            assertThat(session.getAttribute(INPUT_KEY)).isNotNull();

            mockMvc.perform(post("/bankTransfer/cancel").session(session))
                    .andExpect(redirectedUrl("/bankTransfer"));

            assertThat(session.getAttribute(INPUT_KEY)).isNull();
            assertThat(session.getAttribute(TOKEN_KEY)).isNull();
        }

        @Test
        @DisplayName("履歴から来ていても経路の記憶ごと消える")
        void 経路も消える() throws Exception {
            MockHttpSession session = new MockHttpSession();
            mockMvc.perform(post("/bankTransfer/history")
                    .param("bankCode", "0001").param("branchCode", "001")
                    .param("bankAccountType", "普通").param("bankAccountNum", "1234567")
                    .session(session));

            mockMvc.perform(post("/bankTransfer/cancel").session(session));

            // 経路が消えているので、金額画面は通常の6段に戻る
            String html = mockMvc.perform(get("/bankTransfer/bank").session(session))
                    .andReturn().getResponse().getContentAsString();
            assertThat(countOccurrences(html, "stepper-item")).isEqualTo(6);
        }

        @Test
        @DisplayName("全画面に中止ボタンが出る")
        void 全画面にある() throws Exception {
            MockHttpSession session = new MockHttpSession();
            mockMvc.perform(post("/bankTransfer/bank").param("bankCode", "0001").session(session));
            mockMvc.perform(post("/bankTransfer/branch").param("branchCode", "001").session(session));

            for (String url : List.of("/bankTransfer/bank", "/bankTransfer/branch",
                    "/bankTransfer/account")) {
                String html = mockMvc.perform(get(url).session(session))
                        .andReturn().getResponse().getContentAsString();
                assertThat(html).as(url)
                        // ボタンと、それが指すフォームの両方が同じページに出ている
                        .contains("form=\"cancelForm\"")
                        .contains("id=\"cancelForm\"")
                        .contains("action=\"/bankTransfer/cancel\"");
            }
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
    @DisplayName("履歴から振り込む")
    class History {

        // 履歴から選んだ状態で金額画面まで来る
        private MockHttpSession pickFromHistory() throws Exception {
            MockHttpSession session = new MockHttpSession();
            mockMvc.perform(post("/bankTransfer/history")
                            .param("bankCode", "0001").param("branchCode", "001")
                            .param("bankAccountType", "普通").param("bankAccountNum", "1234567")
                            .session(session))
                    .andExpect(redirectedUrl("/bankTransfer/amount"));
            return session;
        }

        @Test
        @DisplayName("選ぶと振込先が一度に埋まり、金額画面へ進む")
        void 選ぶと振込先が埋まる() throws Exception {
            MockHttpSession session = pickFromHistory();

            BankTransferInput input = (BankTransferInput) session.getAttribute(INPUT_KEY);
            assertThat(input.getBankCode()).isEqualTo("0001");
            assertThat(input.getBankName()).isEqualTo("AAA銀行");
            assertThat(input.getBranchCode()).isEqualTo("001");
            assertThat(input.getBankAccountNum()).isEqualTo("1234567");
            assertThat(input.getName()).isEqualTo("ﾔﾏﾀﾞ ﾀﾛｳ");
            // 画面1〜3を通っていなくても、金額画面のガードは通る
            assertThat(input.hasAccount()).isTrue();
        }

        @Test
        @DisplayName("金額と振込指定日は引き継がない")
        void 金額は空になる() throws Exception {
            MockHttpSession session = pickFromHistory();

            BankTransferInput input = (BankTransferInput) session.getAttribute(INPUT_KEY);
            assertThat(input.getMoney()).isNull();
            assertThat(input.getTransferDateTime()).isNull();
        }

        @Test
        @DisplayName("履歴に無い振込先は選べない")
        void 履歴に無いものは弾く() throws Exception {
            MockHttpSession session = new MockHttpSession();

            mockMvc.perform(post("/bankTransfer/history")
                            .param("bankCode", "9999").param("branchCode", "999")
                            .param("bankAccountType", "普通").param("bankAccountNum", "0000000")
                            .session(session))
                    .andExpect(redirectedUrl("/bankTransfer"));

            assertThat(session.getAttribute(INPUT_KEY)).isNull();
        }

        @Test
        @DisplayName("履歴から来ると4段のステッパーになる")
        void 履歴経路のステッパー() throws Exception {
            MockHttpSession session = pickFromHistory();

            String html = mockMvc.perform(get("/bankTransfer/amount").session(session))
                    .andReturn().getResponse().getContentAsString();

            assertThat(countOccurrences(html, "stepper-item")).isEqualTo(3);
            assertThat(html).contains("金額", "確認", "完了").doesNotContain("口座情報");
            // 狭い幅で出る方も同じ総数を指す
            assertThat(html).contains("/ <span>3</span>");
        }

        @Test
        @DisplayName("金融機関から入力し直すと6段に戻る")
        void 経路を切り替える() throws Exception {
            MockHttpSession session = pickFromHistory();

            // 入口から「新しい振込先を指定する」へ入り直す
            mockMvc.perform(get("/bankTransfer/new").session(session))
                    .andExpect(redirectedUrl("/bankTransfer/bank"));
            String html = mockMvc.perform(get("/bankTransfer/amount").session(session))
                    .andReturn().getResponse().getContentAsString();

            assertThat(countOccurrences(html, "stepper-item")).isEqualTo(6);
        }
    }

    @Nested
    @DisplayName("登録した振込先")
    class Payees {

        @Test
        @DisplayName("選ぶと振込先が埋まり、金額は空のまま金額画面へ進む")
        void 選んで振り込む() throws Exception {
            MockHttpSession session = new MockHttpSession();

            mockMvc.perform(post("/bankTransfer/payees/select").param("id", "7").session(session))
                    .andExpect(redirectedUrl("/bankTransfer/amount"));

            BankTransferInput input = (BankTransferInput) session.getAttribute(INPUT_KEY);
            assertThat(input.getBankAccountNum()).isEqualTo("1234567");
            assertThat(input.hasAccount()).isTrue();
            assertThat(input.getMoney()).isNull();
        }

        @Test
        @DisplayName("自分のものでない登録先は選べない")
        void 他人の登録先は選べない() throws Exception {
            MockHttpSession session = new MockHttpSession();

            mockMvc.perform(post("/bankTransfer/payees/select").param("id", "999").session(session))
                    .andExpect(redirectedUrl("/bankTransfer/payees"));

            assertThat(session.getAttribute(INPUT_KEY)).isNull();
        }

        @Test
        @DisplayName("削除はその利用者のものとして実行される")
        void 削除() throws Exception {
            mockMvc.perform(post("/bankTransfer/payees/delete").param("id", "7"))
                    .andExpect(redirectedUrl("/bankTransfer/payees"));

            verify(payeeRepository).delete("demo", 7);
        }
    }

    @Nested
    @DisplayName("振込先の登録")
    class RegisterPayee {

        // 登録の経路で画面1〜3を通し、呼び名の画面まで来る
        private MockHttpSession walkToPayeeConfirm() throws Exception {
            MockHttpSession session = new MockHttpSession();
            mockMvc.perform(get("/bankTransfer/payees/new").session(session))
                    .andExpect(redirectedUrl("/bankTransfer/bank"));
            mockMvc.perform(post("/bankTransfer/bank").param("bankCode", "0001").session(session))
                    .andExpect(redirectedUrl("/bankTransfer/branch"));
            mockMvc.perform(post("/bankTransfer/branch").param("branchCode", "001").session(session))
                    .andExpect(redirectedUrl("/bankTransfer/account"));
            mockMvc.perform(post("/bankTransfer/account")
                            .param("bankAccountType", "普通")
                            .param("bankAccountNum", "1234567")
                            .param("name", "ﾔﾏﾀﾞ ﾀﾛｳ")
                            .session(session))
                    // 振込のときは金額画面だが、登録のときは呼び名の画面へ向かう
                    .andExpect(redirectedUrl("/bankTransfer/payee/confirm"));
            return session;
        }

        @Test
        @DisplayName("画面1〜3を通ったあと、金額ではなく呼び名の画面へ進む")
        void 登録の経路() throws Exception {
            MockHttpSession session = walkToPayeeConfirm();

            mockMvc.perform(get("/bankTransfer/payee/confirm").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bankTransferPayeeConfirm"));
        }

        @Test
        @DisplayName("登録の経路では4段のステッパーになる")
        void 登録経路のステッパー() throws Exception {
            MockHttpSession session = new MockHttpSession();
            mockMvc.perform(get("/bankTransfer/payees/new").session(session));

            String html = mockMvc.perform(get("/bankTransfer/bank").session(session))
                    .andReturn().getResponse().getContentAsString();

            assertThat(countOccurrences(html, "stepper-item")).isEqualTo(4);
            assertThat(html).contains("口座情報").doesNotContain("金額");
            assertThat(html).contains("/ <span>4</span>");
        }

        @Test
        @DisplayName("呼び名を付けて登録すると一覧へ戻り、経路の記憶も消える")
        void 登録する() throws Exception {
            MockHttpSession session = walkToPayeeConfirm();

            mockMvc.perform(post("/bankTransfer/payee/confirm")
                            .param("nickname", "家賃").session(session))
                    .andExpect(redirectedUrl("/bankTransfer/payees"));

            verify(payeeRepository).create(eq("demo"), eq("家賃"), any(BankTransferInput.class));
            assertThat(session.getAttribute(INPUT_KEY)).isNull();
        }

        @Test
        @DisplayName("呼び名が空なら登録しない")
        void 呼び名は必須() throws Exception {
            MockHttpSession session = walkToPayeeConfirm();

            mockMvc.perform(post("/bankTransfer/payee/confirm")
                            .param("nickname", "").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bankTransferPayeeConfirm"));

            verify(payeeRepository, never())
                    .create(anyString(), anyString(), any(BankTransferInput.class));
        }

        @Test
        @DisplayName("登録の経路でないのに呼び名の画面へ来たら一覧へ返す")
        void 経路が違えば入れない() throws Exception {
            MockHttpSession session = new MockHttpSession();
            // 通常の振込で口座情報まで進んだ状態
            walkToConfirmation(session);

            mockMvc.perform(get("/bankTransfer/payee/confirm").session(session))
                    .andExpect(redirectedUrl("/bankTransfer/payees"));
        }

        @Test
        @DisplayName("登録の途中で金額画面へ行こうとしたら呼び名の画面へ返す")
        void 金額画面へは行かせない() throws Exception {
            MockHttpSession session = walkToPayeeConfirm();

            mockMvc.perform(get("/bankTransfer/amount").session(session))
                    .andExpect(redirectedUrl("/bankTransfer/payee/confirm"));
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
