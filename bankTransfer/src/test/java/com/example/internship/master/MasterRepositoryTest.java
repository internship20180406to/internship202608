package com.example.internship.master;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// マスタの読み取りを実DBに対して確認する。参照だけなのでデータは書き換えない。
// replace = NONE で application.yml の MySQL をそのまま使う（組み込みDBに差し替えない）。
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ BankMasterRepository.class, BranchMasterRepository.class })
@DisplayName("マスタの読み取り")
class MasterRepositoryTest {

    @Autowired
    private BankMasterRepository bankMasterRepository;

    @Autowired
    private BranchMasterRepository branchMasterRepository;

    @Nested
    @DisplayName("金融機関マスタ")
    class BankMaster {

        @Test
        @DisplayName("コード順に並ぶ")
        void 一覧() {
            List<Bank> banks = bankMasterRepository.findAll();

            assertThat(banks).extracting(Bank::bankCode).isSorted();
            // デモ用の4行と、初期からある6行
            assertThat(banks).extracting(Bank::bankName)
                    .contains("ふくよか銀行", "丸菱USJ銀行", "二井往友銀行", "ウォーターほ銀行")
                    .contains("AAA銀行", "BBB銀行", "CCC銀行", "DDD銀行", "EEE銀行", "FFF銀行");
        }

        @Test
        @DisplayName("コードで1件引ける")
        void コード指定() {
            Optional<Bank> bank = bankMasterRepository.findByCode("0001");

            assertThat(bank).isPresent();
            assertThat(bank.get().bankName()).isEqualTo("AAA銀行");
        }

        @Test
        @DisplayName("存在しないコードは空で返る（例外にしない）")
        void 存在しないコード() {
            assertThat(bankMasterRepository.findByCode("9999")).isEmpty();
        }

        @Test
        @DisplayName("名前の一部で検索できる")
        void 名前で検索() {
            assertThat(bankMasterRepository.search("BBB"))
                    .extracting(Bank::bankName).containsExactly("BBB銀行");
            assertThat(bankMasterRepository.search("ふくよか"))
                    .extracting(Bank::bankName).containsExactly("ふくよか銀行");
            // どの行も名前に「銀行」が入る
            assertThat(bankMasterRepository.search("銀行"))
                    .hasSameSizeAs(bankMasterRepository.findAll());
        }

        @Test
        @DisplayName("コードの前方一致で検索できる")
        void コードで検索() {
            assertThat(bankMasterRepository.search("0003"))
                    .extracting(Bank::bankName).containsExactly("CCC銀行");
            assertThat(bankMasterRepository.search("0177"))
                    .extracting(Bank::bankName).containsExactly("ふくよか銀行");
            // 前方一致なので、途中に含むだけの行は拾わない
            assertThat(bankMasterRepository.search("177")).isEmpty();
        }

        @Test
        @DisplayName("% を打っても全件にならない（ワイルドカードとして扱わない）")
        void ワイルドカードを打ち消す() {
            assertThat(bankMasterRepository.search("%")).isEmpty();
            assertThat(bankMasterRepository.search("_")).isEmpty();
        }

        @Test
        @DisplayName("一覧に出すのは主な金融機関だけ。残りは検索でのみ到達できる")
        void 主な金融機関に絞る() {
            assertThat(bankMasterRepository.findMajor())
                    .extracting(Bank::bankName)
                    .containsExactlyInAnyOrder("ふくよか銀行", "丸菱USJ銀行",
                            "二井往友銀行", "ウォーターほ銀行");

            // 一覧に無いものも検索では見つかる
            assertThat(bankMasterRepository.search("FFF"))
                    .extracting(Bank::bankName).containsExactly("FFF銀行");
        }

        @Test
        @DisplayName("該当が無ければ空のリスト")
        void 該当なし() {
            assertThat(bankMasterRepository.search("ZZZ")).isEmpty();
        }
    }

    @Nested
    @DisplayName("支店マスタ")
    class BranchMaster {

        @Test
        @DisplayName("銀行ごとに9行あり、頭文字が銀行名と対応する")
        void 銀行ごとの一覧() {
            // 全支店名に「支店」が入るので、これで銀行0002の全件が並ぶ
            List<Branch> branches = branchMasterRepository.search("0002", "支店");

            assertThat(branches).hasSize(9);
            assertThat(branches).extracting(Branch::branchCode)
                    .containsExactly("001", "002", "003", "004", "005", "006", "007", "008", "009");
            assertThat(branches).extracting(Branch::branchName)
                    .containsExactly("B1支店", "B2支店", "B3支店", "B4支店",
                            "B5支店", "B6支店", "B7支店", "B8支店", "B9支店");
        }

        @Test
        @DisplayName("銀行コードと支店コードの組で1件引ける")
        void 組で指定() {
            Optional<Branch> branch = branchMasterRepository.find("0003", "005");

            assertThat(branch).isPresent();
            assertThat(branch.get().branchName()).isEqualTo("C5支店");
        }

        @Test
        @DisplayName("支店コードは銀行をまたぐと別物として扱われる")
        void 支店コードは銀行の中でだけ一意() {
            String nameOfBankA = branchMasterRepository.find("0001", "001").orElseThrow().branchName();
            String nameOfBankB = branchMasterRepository.find("0002", "001").orElseThrow().branchName();

            assertThat(nameOfBankA).isEqualTo("A1支店");
            assertThat(nameOfBankB).isEqualTo("B1支店");
        }

        @Test
        @DisplayName("他の銀行の支店は引けない")
        void 別の銀行の支店は見えない() {
            assertThat(branchMasterRepository.find("9999", "001")).isEmpty();
        }

        @Test
        @DisplayName("検索は指定した銀行の中だけを対象にする")
        void 検索は銀行内に限定される() {
            List<Branch> hits = branchMasterRepository.search("0001", "1");

            assertThat(hits).extracting(Branch::branchName).containsExactly("A1支店");
            assertThat(hits).extracting(Branch::bankCode).containsOnly("0001");
        }

        @Test
        @DisplayName("支店名の一部で検索できる")
        void 支店名で検索() {
            assertThat(branchMasterRepository.search("0004", "支店")).hasSize(9);
            assertThat(branchMasterRepository.search("0004", "D7"))
                    .extracting(Branch::branchName).containsExactly("D7支店");
        }
    }
}
