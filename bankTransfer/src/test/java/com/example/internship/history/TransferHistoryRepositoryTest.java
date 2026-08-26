package com.example.internship.history;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// 実DBに対して確認する。@JdbcTest は既定でトランザクションを張って
// テストごとに巻き戻すので、ここで入れた行は残らない。
// （マスタと違い履歴は利用者ごとに違うため、確かめるには行を用意する必要がある）
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TransferHistoryRepository.class)
@DisplayName("振込履歴の読み取り")
class TransferHistoryRepositoryTest {

    private static final String TARO = "test-taro";
    private static final String HANAKO = "test-hanako";

    @Autowired
    private TransferHistoryRepository transferHistoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private void insert(String userId, String bankCode, String bankName, String branchCode,
                        String branchName, String type, String num, String name, String date) {
        jdbcTemplate.update("""
                INSERT INTO bankTransfer_table
                    (userId, bankCode, bankName, branchCode, branchName,
                     bankAccountType, bankAccountNum, name, money, transferDateTime)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1000, ?)
                """, userId, bankCode, bankName, branchCode, branchName, type, num, name, date);
    }

    @BeforeEach
    void setUp() {
        // taro は同じ相手へ3回、別の相手へ1回
        insert(TARO, "0001", "AAA銀行", "001", "A1支店", "普通", "1234567", "ｱ", "2026-08-01");
        insert(TARO, "0001", "AAA銀行", "001", "A1支店", "普通", "1234567", "ｱ", "2026-08-10");
        insert(TARO, "0001", "AAA銀行", "001", "A1支店", "普通", "1234567", "ｲ", "2026-08-20");
        insert(TARO, "0002", "BBB銀行", "003", "B3支店", "当座", "7654321", "ｳ", "2026-08-05");
        // hanako は別の相手へ1回
        insert(HANAKO, "0003", "CCC銀行", "005", "C5支店", "貯蓄", "1112223", "ｴ", "2026-08-15");
    }

    @Test
    @DisplayName("同じ相手への振込は1件にまとまり、最後に振り込んだ日が付く")
    void 重複はまとまる() {
        List<RecentPayee> payees = transferHistoryRepository.findRecent(TARO);

        assertThat(payees).hasSize(2);
        RecentPayee aaa = payees.stream()
                .filter(p -> p.bankCode().equals("0001")).findFirst().orElseThrow();
        assertThat(aaa.lastTransferredOn()).hasToString("2026-08-20");
        // 名義が途中で変わっていたら、最後に振り込んだときの名義を採る
        assertThat(aaa.name()).isEqualTo("ｲ");
    }

    @Test
    @DisplayName("新しく振り込んだ相手が先に並ぶ")
    void 新しい順() {
        List<RecentPayee> payees = transferHistoryRepository.findRecent(TARO);

        assertThat(payees).extracting(RecentPayee::bankCode)
                .containsExactly("0001", "0002");
    }

    @Test
    @DisplayName("他人の履歴は出ない")
    void 利用者ごとに分かれる() {
        assertThat(transferHistoryRepository.findRecent(HANAKO))
                .extracting(RecentPayee::bankCode).containsExactly("0003");

        assertThat(transferHistoryRepository.findRecent(TARO))
                .extracting(RecentPayee::bankCode).doesNotContain("0003");
    }

    @Test
    @DisplayName("科目が違えば別の振込先として扱う")
    void 科目が違えば別口座() {
        insert(TARO, "0001", "AAA銀行", "001", "A1支店", "当座", "1234567", "ｱ", "2026-08-21");

        assertThat(transferHistoryRepository.findRecent(TARO)).hasSize(3);
    }

    @Test
    @DisplayName("自分の履歴にある振込先は引き当てられる")
    void 引き当て() {
        Optional<RecentPayee> found =
                transferHistoryRepository.find(TARO, "0001", "001", "普通", "1234567");

        assertThat(found).isPresent();
        assertThat(found.get().branchName()).isEqualTo("A1支店");
    }

    @Test
    @DisplayName("他人の振込先は引き当てられない")
    void 他人の振込先は引けない() {
        // hanako の振込先を taro として引こうとする
        assertThat(transferHistoryRepository.find(TARO, "0003", "005", "貯蓄", "1112223"))
                .isEmpty();
        // 持ち主なら引ける
        assertThat(transferHistoryRepository.find(HANAKO, "0003", "005", "貯蓄", "1112223"))
                .isPresent();
    }

    @Test
    @DisplayName("履歴が無ければ空のリスト")
    void 履歴なし() {
        assertThat(transferHistoryRepository.findRecent("test-誰でもない")).isEmpty();
    }
}
