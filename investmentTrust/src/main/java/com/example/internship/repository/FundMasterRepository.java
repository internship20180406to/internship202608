package com.example.internship.repository;

import com.example.internship.entity.FundMaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// 銘柄マスタの取得を行うRepository
@Repository
public class FundMasterRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final String SELECT_COLUMNS =
            "fund_code, fund_name, purchase_fee_rate, trust_fee_rate, redemption_reserve_rate, reference_price";

    // 銘柄マスタを全件取得する(選択肢表示用)
    public List<FundMaster> findAll() {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM fund_master ORDER BY fund_code";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs));
    }

    // 銘柄コードを指定して1件取得する(手数料計算・名称解決で使用)
    public Optional<FundMaster> findByCode(String fundCode) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM fund_master WHERE fund_code = ?";
        List<FundMaster> result = jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs), fundCode);
        return result.stream().findFirst();
    }

    private FundMaster mapRow(ResultSet rs) throws SQLException {
        return new FundMaster(
                rs.getString("fund_code"),
                rs.getString("fund_name"),
                rs.getBigDecimal("purchase_fee_rate"),
                rs.getBigDecimal("trust_fee_rate"),
                rs.getBigDecimal("redemption_reserve_rate"),
                rs.getInt("reference_price")
        );
    }
}
