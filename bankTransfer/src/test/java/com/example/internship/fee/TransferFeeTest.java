package com.example.internship.fee;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

// Springを起動せずに素で組み立てられる
@DisplayName("振込手数料")
class TransferFeeTest {

    private static final String OWN = TransferFee.OWN_BANK_CODE;
    private static final String OTHER = "0008";

    private final TransferFee transferFee = new TransferFee();

    @Nested
    @DisplayName("手数料の決まり")
    class Rule {

        @ParameterizedTest(name = "自行あて {0}円 は 0円")
        @CsvSource({ "1", "29999", "30000", "2000000" })
        @DisplayName("自行あては金額によらず無料")
        void 自行は無料(int amount) {
            assertThat(transferFee.of(OWN, amount)).isZero();
        }

        @ParameterizedTest(name = "他行あて {0}円 は {1}円")
        @CsvSource({ "1,220", "29999,220", "30000,330", "30001,330", "2000000,330" })
        @DisplayName("他行あては3万円で段が変わる")
        void 他行は段が変わる(int amount, int expected) {
            assertThat(transferFee.of(OTHER, amount)).isEqualTo(expected);
        }

        @Test
        @DisplayName("自行かどうかを判定できる")
        void 自行の判定() {
            assertThat(transferFee.isOwnBank(OWN)).isTrue();
            assertThat(transferFee.isOwnBank(OTHER)).isFalse();
            assertThat(transferFee.isOwnBank(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("金額の分け方")
    class Split {

        @Test
        @DisplayName("含めないときは、打った額がそのまま相手に届く")
        void 含めない() {
            TransferAmount amount = TransferAmount.of(10000, 220, false);

            assertThat(amount.money()).isEqualTo(10000);
            assertThat(amount.fee()).isEqualTo(220);
            assertThat(amount.total()).isEqualTo(10220);
        }

        @Test
        @DisplayName("含めるときは、打った額から手数料を引いた分が相手に届く")
        void 含める() {
            TransferAmount amount = TransferAmount.of(10000, 220, true);

            assertThat(amount.money()).isEqualTo(9780);
            assertThat(amount.fee()).isEqualTo(220);
            // 口座から引かれるのは打った額ちょうど
            assertThat(amount.total()).isEqualTo(10000);
        }

        @Test
        @DisplayName("手数料以下の額を含めようとすると、相手に届く額が残らない")
        void 手数料以下() {
            assertThat(TransferAmount.of(220, 220, true).money()).isZero();
            assertThat(TransferAmount.of(100, 220, true).money()).isNegative();
        }

        @Test
        @DisplayName("手数料の段は打った額で決まる。差し引いた額では決めない")
        void 段は打った額で決まる() {
            // 30,000円を打つと段は330円。差し引くと29,670円だが、
            // それで220円に下げると引き算が変わって堂々巡りになる
            int entered = 30000;
            int fee = transferFee.of(OTHER, entered);

            assertThat(fee).isEqualTo(330);
            assertThat(TransferAmount.of(entered, fee, true).money()).isEqualTo(29670);
        }
    }
}
