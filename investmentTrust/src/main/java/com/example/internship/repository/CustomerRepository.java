package com.example.internship.repository;

import com.example.internship.entity.Customer;
import com.example.internship.entity.CustomerSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// 顧客マスタ(customer_master)への名寄せ登録と、行員モードの顧客一覧・詳細表示用の取得を行うRepository
// ログイン機構がなかった名残で、姓名+連絡先が完全一致する申し込みを同一顧客とみなして名寄せする(フェーズ1の簡易実装)。
// お客様モードの注文履歴確認だけは、連絡先+パスワードによるログイン形式にしている(フェーズ2)
@Repository
public class CustomerRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    // 行内で共通利用するCustomerのマッピング(パスワードのハッシュ値も含む。画面には表示しないこと)
    private static final RowMapper<Customer> CUSTOMER_ROW_MAPPER = (rs, rowNum) -> new Customer(
            rs.getLong("customer_id"),
            rs.getString("last_name"),
            rs.getString("first_name"),
            rs.getString("address"),
            rs.getString("contact"),
            rs.getString("password"),
            rs.getTimestamp("created_at").toLocalDateTime()
    );

    // 姓名+連絡先が一致する顧客がいればそのcustomer_idを返し、いなければ新規登録してcustomer_idを返す。
    // passwordHashは新規登録時にのみ設定する。既存顧客がヒットした場合はpasswordを一切上書きしない
    // (他人の姓名+連絡先を知るだけで、未設定パスワードを勝手に乗っ取れてしまうため)。
    // 住所は最新の申し込み内容で更新する(引っ越し等で変わり得るため)
    public Long resolveOrCreateCustomerId(String lastName, String firstName, String address, String contact, String passwordHash) {
        String insertSql = "INSERT INTO customer_master (last_name, first_name, address, contact, password) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE customer_id = LAST_INSERT_ID(customer_id), address = VALUES(address)";
        jdbcTemplate.update(insertSql, lastName, firstName, address, contact, passwordHash);
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    // 行員モード: 顧客詳細画面のヘッダー表示用に1件取得する
    public Optional<Customer> findById(Long customerId) {
        String sql = "SELECT customer_id, last_name, first_name, address, contact, password, created_at " +
                "FROM customer_master WHERE customer_id = ?";
        List<Customer> result = jdbcTemplate.query(sql, CUSTOMER_ROW_MAPPER, customerId);
        return result.stream().findFirst();
    }

    // お客様モード ログイン: 連絡先が一致する顧客を(あれば複数)取得し、呼び出し側でパスワードのハッシュを照合する。
    // contact単体はDB上ユニーク制約が無いため、複数件返る可能性がある。
    // 保存時は自動整形でハイフン付き("090-1234-5678")になるが、ログイン画面はハイフンなし入力も許容したいため、
    // ハイフンを除いた数字だけで比較する
    public List<Customer> findAllByContact(String contact) {
        String sql = "SELECT customer_id, last_name, first_name, address, contact, password, created_at " +
                "FROM customer_master WHERE REPLACE(contact, '-', '') = REPLACE(?, '-', '')";
        return jdbcTemplate.query(sql, CUSTOMER_ROW_MAPPER, contact);
    }

    // 行員モード: 顧客一覧画面用。申し込み件数・最終申し込み日時を注文テーブルから集計する
    public List<CustomerSummary> findAllCustomers() {
        String sql = "SELECT c.customer_id, c.last_name, c.first_name, c.address, c.contact, " +
                "COUNT(t.id) AS order_count, MAX(t.order_datetime) AS last_order_datetime " +
                "FROM customer_master c " +
                "JOIN investmentTrust_table t ON t.customer_id = c.customer_id " +
                "GROUP BY c.customer_id, c.last_name, c.first_name, c.address, c.contact " +
                "ORDER BY last_order_datetime DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new CustomerSummary(
                rs.getLong("customer_id"),
                rs.getString("last_name"),
                rs.getString("first_name"),
                rs.getString("address"),
                rs.getString("contact"),
                rs.getInt("order_count"),
                rs.getTimestamp("last_order_datetime").toLocalDateTime()
        ));
    }
}
