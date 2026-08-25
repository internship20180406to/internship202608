package com.example.internship.master;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// 支店マスタの読み取り。支店は必ず銀行に属するので、どのメソッドも銀行コードを受け取る
@Repository
public class BranchMasterRepository {

    private static final RowMapper<Branch> ROW_MAPPER = (rs, rowNum) -> new Branch(
            rs.getString("bankCode"), rs.getString("branchCode"), rs.getString("branchName"));

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Branch> findByBankCode(String bankCode) {
        String sql = """
                SELECT bankCode, branchCode, branchName
                  FROM branch_master
                 WHERE bankCode = ?
                 ORDER BY branchCode
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER, bankCode);
    }

    // 選択された支店が、その銀行の支店として実在するかの確認に使う
    public Optional<Branch> find(String bankCode, String branchCode) {
        String sql = """
                SELECT bankCode, branchCode, branchName
                  FROM branch_master
                 WHERE bankCode = ? AND branchCode = ?
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER, bankCode, branchCode).stream().findFirst();
    }

    // 検索欄用。銀行をまたいで検索しないよう、必ず銀行コードで絞ってから探す
    public List<Branch> search(String bankCode, String keyword) {
        String sql = """
                SELECT bankCode, branchCode, branchName
                  FROM branch_master
                 WHERE bankCode = ?
                   AND (branchName LIKE ? ESCAPE '!' OR branchCode LIKE ? ESCAPE '!')
                 ORDER BY branchCode
                """;
        String escaped = BankMasterRepository.escapeLike(keyword);
        return jdbcTemplate.query(sql, ROW_MAPPER, bankCode, "%" + escaped + "%", escaped + "%");
    }
}
