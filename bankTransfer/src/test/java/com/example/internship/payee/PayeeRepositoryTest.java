package com.example.internship.payee;

import com.example.internship.entity.BankTransferInput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// 実DBに対して確認する。@JdbcTest は既定でトランザクションを張って
// テストごとに巻き戻すので、ここで入れた行は残らない
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PayeeRepository.class)
@DisplayName("登録した振込先")
class PayeeRepositoryTest {

    private static final String TARO = "test-taro";
    private static final String HANAKO = "test-hanako";

    @Autowired
    private PayeeRepository payeeRepository;

    private static BankTransferInput input(String bankCode, String branchCode,
                                           String type, String num) {
        BankTransferInput input = new BankTransferInput();
        input.setBankCode(bankCode);
        input.setBankName("AAA銀行");
        input.setBranchCode(branchCode);
        input.setBranchName("A1支店");
        input.setBankAccountType(type);
        input.setBankAccountNum(num);
        input.setName("ﾔﾏﾀﾞ ﾀﾛｳ");
        return input;
    }

    private static BankTransferInput taroPayee() {
        return input("0001", "001", "普通", "1234567");
    }

    @Nested
    @DisplayName("登録")
    class Create {

        @Test
        @DisplayName("登録すると一覧に出る")
        void 登録して読む() {
            assertThat(payeeRepository.create(TARO, "家賃", taroPayee())).isTrue();

            List<Payee> payees = payeeRepository.findAll(TARO);
            assertThat(payees).hasSize(1);
            assertThat(payees.get(0).nickname()).isEqualTo("家賃");
            assertThat(payees.get(0).bankName()).isEqualTo("AAA銀行");
            assertThat(payees.get(0).id()).isPositive();
        }

        @Test
        @DisplayName("同じ振込先は二重に登録できない。呼び名が違っても弾く")
        void 二重登録を弾く() {
            assertThat(payeeRepository.create(TARO, "家賃", taroPayee())).isTrue();

            assertThat(payeeRepository.create(TARO, "別の呼び名", taroPayee())).isFalse();
            assertThat(payeeRepository.findAll(TARO)).hasSize(1);
        }

        @Test
        @DisplayName("科目が違えば別の振込先として登録できる")
        void 科目が違えば別口座() {
            payeeRepository.create(TARO, "普通の方", input("0001", "001", "普通", "1234567"));

            assertThat(payeeRepository.create(TARO, "当座の方",
                    input("0001", "001", "当座", "1234567"))).isTrue();
            assertThat(payeeRepository.findAll(TARO)).hasSize(2);
        }

        @Test
        @DisplayName("利用者が違えば同じ振込先を登録できる")
        void 利用者が違えば重複しない() {
            payeeRepository.create(TARO, "家賃", taroPayee());

            assertThat(payeeRepository.create(HANAKO, "家賃", taroPayee())).isTrue();
        }

        @Test
        @DisplayName("登録済みかどうかを先に調べられる")
        void 登録済みの判定() {
            assertThat(payeeRepository.exists(TARO, taroPayee())).isFalse();

            payeeRepository.create(TARO, "家賃", taroPayee());

            assertThat(payeeRepository.exists(TARO, taroPayee())).isTrue();
            // 他人の登録は自分の判定に影響しない
            assertThat(payeeRepository.exists(HANAKO, taroPayee())).isFalse();
        }
    }

    @Nested
    @DisplayName("利用者ごとの切り分け")
    class Isolation {

        @Test
        @DisplayName("他人の登録先は一覧に出ない")
        void 一覧は自分の分だけ() {
            payeeRepository.create(TARO, "家賃", taroPayee());
            payeeRepository.create(HANAKO, "会費", input("0002", "003", "当座", "7654321"));

            assertThat(payeeRepository.findAll(TARO))
                    .extracting(Payee::nickname).containsExactly("家賃");
            assertThat(payeeRepository.findAll(HANAKO))
                    .extracting(Payee::nickname).containsExactly("会費");
        }

        @Test
        @DisplayName("他人の登録先はidを知っていても引けない")
        void 他人のidでは引けない() {
            payeeRepository.create(HANAKO, "会費", taroPayee());
            int id = payeeRepository.findAll(HANAKO).get(0).id();

            assertThat(payeeRepository.find(TARO, id)).isEmpty();
            assertThat(payeeRepository.find(HANAKO, id)).isPresent();
        }

        @Test
        @DisplayName("他人の登録先はidを知っていても消せない")
        void 他人のidでは消せない() {
            payeeRepository.create(HANAKO, "会費", taroPayee());
            int id = payeeRepository.findAll(HANAKO).get(0).id();

            assertThat(payeeRepository.delete(TARO, id)).isFalse();
            assertThat(payeeRepository.findAll(HANAKO)).hasSize(1);
        }
    }

    @Nested
    @DisplayName("削除")
    class Delete {

        @Test
        @DisplayName("自分の登録先は消せる")
        void 消せる() {
            payeeRepository.create(TARO, "家賃", taroPayee());
            int id = payeeRepository.findAll(TARO).get(0).id();

            assertThat(payeeRepository.delete(TARO, id)).isTrue();
            assertThat(payeeRepository.findAll(TARO)).isEmpty();
        }

        @Test
        @DisplayName("消したあとは同じ振込先をまた登録できる")
        void 消してから登録し直す() {
            payeeRepository.create(TARO, "家賃", taroPayee());
            payeeRepository.delete(TARO, payeeRepository.findAll(TARO).get(0).id());

            assertThat(payeeRepository.create(TARO, "家賃", taroPayee())).isTrue();
        }

        @Test
        @DisplayName("無いidを消しても何も起きない")
        void 無いものを消す() {
            assertThat(payeeRepository.delete(TARO, 999999)).isFalse();
        }
    }

    @Test
    @DisplayName("新しく登録したものが先に並ぶ")
    void 新しい順() {
        payeeRepository.create(TARO, "先に登録", input("0001", "001", "普通", "1111111"));
        payeeRepository.create(TARO, "後に登録", input("0001", "001", "普通", "2222222"));

        assertThat(payeeRepository.findAll(TARO))
                .extracting(Payee::nickname).containsExactly("後に登録", "先に登録");
    }

    @Test
    @DisplayName("登録が無ければ空のリスト")
    void 登録なし() {
        assertThat(payeeRepository.findAll("test-誰でもない")).isEmpty();
    }
}
