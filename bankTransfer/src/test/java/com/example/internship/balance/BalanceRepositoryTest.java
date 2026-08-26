package com.example.internship.balance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

// 実DBに対して確認する。@JdbcTest は既定でトランザクションを張って
// テストごとに巻き戻すので、ここで入れた行は残らない
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(BalanceRepository.class)
@DisplayName("口座残高")
class BalanceRepositoryTest {

    private static final String TARO = "test-taro";

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private void give(String userId, int amount) {
        jdbcTemplate.update("""
                INSERT INTO balance (userId, amount) VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE amount = VALUES(amount)
                """, userId, amount);
    }

    @Test
    @DisplayName("残高が無い利用者には初期残高で行を作る")
    void 初回は初期残高() {
        assertThat(balanceRepository.amountOf(TARO)).isEqualTo(BalanceRepository.INITIAL_AMOUNT);
        // 2回目は作り直さない
        assertThat(balanceRepository.amountOf(TARO)).isEqualTo(BalanceRepository.INITIAL_AMOUNT);
    }

    @Test
    @DisplayName("引いた分だけ減る")
    void 引く() {
        give(TARO, 10000);

        assertThat(balanceRepository.withdraw(TARO, 3000)).isTrue();

        assertThat(balanceRepository.amountOf(TARO)).isEqualTo(7000);
    }

    @Test
    @DisplayName("残高ちょうどは引ける")
    void ちょうど引く() {
        give(TARO, 10000);

        assertThat(balanceRepository.withdraw(TARO, 10000)).isTrue();

        assertThat(balanceRepository.amountOf(TARO)).isZero();
    }

    @Test
    @DisplayName("足りなければ引かず、残高も動かない")
    void 足りなければ引かない() {
        give(TARO, 10000);

        assertThat(balanceRepository.withdraw(TARO, 10001)).isFalse();

        assertThat(balanceRepository.amountOf(TARO)).isEqualTo(10000);
    }

    @Test
    @DisplayName("残高いっぱいまで引いたあと、もう一度は引けない")
    void 二度は引けない() {
        give(TARO, 10000);

        // 確認と引き算がUPDATEの中で1手になっているので、
        // 1回目が通ったあと同じ額をもう一度引くことはできない
        assertThat(balanceRepository.withdraw(TARO, 10000)).isTrue();
        assertThat(balanceRepository.withdraw(TARO, 10000)).isFalse();

        assertThat(balanceRepository.amountOf(TARO)).isZero();
    }

    @Test
    @DisplayName("他人の残高は動かない")
    void 利用者ごとに分かれる() {
        give(TARO, 10000);
        give("test-hanako", 10000);

        balanceRepository.withdraw(TARO, 4000);

        assertThat(balanceRepository.amountOf(TARO)).isEqualTo(6000);
        assertThat(balanceRepository.amountOf("test-hanako")).isEqualTo(10000);
    }
}
