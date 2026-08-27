package com.example.internship.repository;

import com.example.internship.entity.InvestmentTrustForm;
import com.example.internship.entity.InvestmentTrustOrderView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

// investmentTrust_tableへの注文登録と、行員モード一覧用の全件取得(マスタ名称解決込み)を行うRepository
@Repository
public class InvestmentTrustRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    // 1件の注文をDBに保存する(customerIdは事前にCustomerRepositoryで名寄せ・確定させたものを受け取る)。生成された注文IDを返す
    public Long create(InvestmentTrustForm investmentTrustForm, Long customerId) {
        String sql = "INSERT INTO investmentTrust_table(" +
                "customer_id, institution_code, branch_code, bank_account_num, bank_subject, " +
                "last_name, first_name, address, contact, fund_code, purchase_amount, purchase_fee, order_datetime, trade_date, estimated_units) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql,
                customerId,
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
                java.sql.Date.valueOf(investmentTrustForm.getTradeDate()),
                investmentTrustForm.getEstimatedUnits()
        );
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    // 行員モード一覧用: 3マスタとJOINして名称解決済みの注文一覧を新しい順に返す
    public List<InvestmentTrustOrderView> findAllOrders() {
        return queryOrders("ORDER BY t.order_datetime DESC");
    }

    // 行員モード 顧客詳細用: 指定した顧客の申し込み履歴を新しい順に返す
    public List<InvestmentTrustOrderView> findOrdersByCustomerId(Long customerId) {
        return queryOrders("WHERE t.customer_id = ? ORDER BY t.order_datetime DESC", customerId);
    }

    // 顧客向けステータス確認画面用: IDを指定して1件取得する(認証なしでアクセスされるため、IDを知っている本人のみ閲覧できる想定)
    public Optional<InvestmentTrustOrderView> findOrderById(Long id) {
        return queryOrders("WHERE t.id = ?", id).stream().findFirst();
    }

    // 約定処理の対象抽出用: 「発注済み」かつ約定日が到来している注文を返す(基準価額が登録され次第、約定処理する)
    public List<InvestmentTrustOrderView> findPendingSettlement() {
        return queryOrders("WHERE t.status = '発注済み' AND t.trade_date <= CURDATE()");
    }

    private List<InvestmentTrustOrderView> queryOrders(String whereAndOrder, Object... args) {
        String sql = "SELECT " +
                "t.id, t.customer_id, t.status, " +
                "im.institution_name, bm.branch_name, t.bank_account_num, t.bank_subject, " +
                "t.last_name, t.first_name, t.address, t.contact, t.fund_code, fm.fund_name, t.purchase_amount, t.purchase_fee, " +
                "t.order_datetime, t.trade_date, t.estimated_units, t.confirmed_units " +
                "FROM investmentTrust_table t " +
                "JOIN institution_master im ON im.institution_code = t.institution_code " +
                "JOIN branch_master bm ON bm.institution_code = t.institution_code AND bm.branch_code = t.branch_code " +
                "JOIN fund_master fm ON fm.fund_code = t.fund_code " +
                whereAndOrder;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new InvestmentTrustOrderView(
                rs.getLong("id"),
                rs.getLong("customer_id"),
                rs.getString("status"),
                rs.getString("institution_name"),
                rs.getString("branch_name"),
                rs.getString("bank_account_num"),
                rs.getString("bank_subject"),
                rs.getString("last_name"),
                rs.getString("first_name"),
                rs.getString("address"),
                rs.getString("contact"),
                rs.getString("fund_code"),
                rs.getString("fund_name"),
                rs.getInt("purchase_amount"),
                rs.getInt("purchase_fee"),
                rs.getTimestamp("order_datetime").toLocalDateTime(),
                rs.getDate("trade_date").toLocalDate(),
                rs.getLong("estimated_units"),
                rs.getObject("confirmed_units") == null ? null : rs.getLong("confirmed_units")
        ), args);
    }

    // 行員モード: 注文を「発注済み」に進める(申込受付の注文のみ対象。二重発注を防ぐためWHEREで状態を絞る)
    public void markAsPlaced(Long id) {
        String sql = "UPDATE investmentTrust_table SET status = '発注済み' WHERE id = ? AND status = '申込受付'";
        jdbcTemplate.update(sql, id);
    }

    // 約定処理: 確定口数を設定し、ステータスを「約定済み」に進める(発注済みの注文のみ対象)
    public void settleOrder(Long id, Long confirmedUnits) {
        String sql = "UPDATE investmentTrust_table SET status = '約定済み', confirmed_units = ? WHERE id = ? AND status = '発注済み'";
        jdbcTemplate.update(sql, confirmedUnits, id);
    }

}
