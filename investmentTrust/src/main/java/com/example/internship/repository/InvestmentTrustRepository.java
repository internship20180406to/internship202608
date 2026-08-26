package com.example.internship.repository;

import com.example.internship.entity.InvestmentTrustForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InvestmentTrustRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public void create(InvestmentTrustForm investmentTrustForm) {
//        investmentTrust_tableに1行データを挿入するSQL分文字列が長いため＋で書いている
        String sql = "INSERT INTO investmentTrust_table(" +
                "bankName, bankAccountNum, branchName, bankAccountType, " +
                "name, fundName, money) " +
                "VALUES(?,?,?,?,?,?,?)";//？を使用することで、INSERTぶんで値の入るところは、７か所あるという構文の骨格を先に確定させておく。
        jdbcTemplate.update(sql, investmentTrustForm.getBankName(), investmentTrustForm.getBankAccountNum(), investmentTrustForm.getBranch(),
                investmentTrustForm.getBankSubject(), investmentTrustForm.getPurchaserName(), investmentTrustForm.getInvestmentTrustName(),
                investmentTrustForm.getPurchaseAmount());//DBのカラム名と、Entityのフィールド名が一致してなく、読みづらいので改善が必要。
//        jdbcTemplate(SQL,値１、値２)
//        第一引数SQL分？が入ったもの第二引数からは、？に順番に当てはめる値。
    }

}
