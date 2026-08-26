package com.example.internship.repository;

import com.example.internship.entity.InvestmentTrustForm;
import com.example.internship.entity.InvestmentTrustOrderView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

// investmentTrust_tableへの注文登録と、行員モード一覧用の全件取得(マスタ名称解決込み)を行うRepository
@Repository
public class InvestmentTrustRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    // 1件の注文をDBに保存する
    public void create(InvestmentTrustForm investmentTrustForm) {
        String sql = "INSERT INTO investmentTrust_table(" +
                "institution_code, branch_code, bank_account_num, bank_subject, " +
                "last_name, first_name, address, contact, fund_code, purchase_amount, purchase_fee, order_datetime, trade_date) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql,
                investmentTrustForm.getInstitutionCode(),
                investmentTrustForm.getBranchCode(),
                investmentTrustForm.getBankAccountNum(),
                investmentTrustForm.getBankSubject(),
                investmentTrustForm.getLastName(),
                investmentTrustForm.getFirstName(),
                investmentTrustForm.getAddress(),
                investmentTrustForm.getContact(),
                investmentTrustForm.getFundCode(),
                investmentTrustForm.getPurchaseAmount(),
                investmentTrustForm.getPurchaseFee(),
                Timestamp.valueOf(investmentTrustForm.getOrderDatetime()),
                java.sql.Date.valueOf(investmentTrustForm.getTradeDate())
        );
    }

    // 行員モード一覧用: 3マスタとJOINして名称解決済みの注文一覧を新しい順に返す
    public List<InvestmentTrustOrderView> findAllOrders() {
        String sql = "SELECT " +
                "im.institution_name, bm.branch_name, t.bank_account_num, t.bank_subject, " +
                "t.last_name, t.first_name, t.address, t.contact, fm.fund_name, t.purchase_amount, t.purchase_fee, " +
                "t.order_datetime, t.trade_date " +
                "FROM investmentTrust_table t " +
                "JOIN institution_master im ON im.institution_code = t.institution_code " +
                "JOIN branch_master bm ON bm.institution_code = t.institution_code AND bm.branch_code = t.branch_code " +
                "JOIN fund_master fm ON fm.fund_code = t.fund_code " +
                "ORDER BY t.order_datetime DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new InvestmentTrustOrderView(
                rs.getString("institution_name"),
                rs.getString("branch_name"),
                rs.getString("bank_account_num"),
                rs.getString("bank_subject"),
                rs.getString("last_name"),
                rs.getString("first_name"),
                rs.getString("address"),
                rs.getString("contact"),
                rs.getString("fund_name"),
                rs.getInt("purchase_amount"),
                rs.getInt("purchase_fee"),
                rs.getTimestamp("order_datetime").toLocalDateTime(),
                rs.getDate("trade_date").toLocalDate()
        ));
    }

}
