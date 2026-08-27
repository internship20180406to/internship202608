package com.example.internship.repository;

import com.example.internship.entity.InvestmentTrustForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InvestmentTrustRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * 申込を1件登録する。
     *
     * コードと名称の両方を保存している（正規化するならコードだけで足りる）。
     * これは「申込時点のスナップショット」を残すため。
     * 将来マスタの名称が変わっても、あるいはマスタから消えても、
     * 申込時にどう表示されていたかが分かるようにしている。
     *
     * 名称は画面から送られてきた値ではなく、サーバがマスタから引き直した値が入っている
     * （InvestmentTrustController#validateAndResolveMaster を参照）。
     */
    public void create(InvestmentTrustForm investmentTrustForm) {
        String sql = "INSERT INTO investmentTrust_table"
                + "(bankCode, bankName, branchCode, branchName, bankAccountType, bankAccountNum, name, fundName, money)"
                + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                investmentTrustForm.getBankCode(),
                investmentTrustForm.getBankName(),
                investmentTrustForm.getBranchCode(),
                investmentTrustForm.getBranchName(),
                investmentTrustForm.getBankAccountType(),
                investmentTrustForm.getBankAccountNum(),
                investmentTrustForm.getName(),
                investmentTrustForm.getFundName(),
                investmentTrustForm.getMoney());
    }

}
