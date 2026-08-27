package com.example.internship.repository;

import com.example.internship.entity.InvestmentTrustForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class InvestmentTrustRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void create(InvestmentTrustForm form) {

        String sql =
                "INSERT INTO investmentTrust_table " +
                        "(bankName, branchName, bankAccountType, bankAccountNum, " +
                        "name, fundName, money, applicationDate, purchaseDate, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NULL, ?)";

        jdbcTemplate.update(
                sql,
                form.getBankName(),
                form.getBranchName(),
                form.getBankAccountTypeName(),
                form.getBankAccountNum(),
                form.getName(),
                form.getFundName(),
                form.getMoney(),
                "確認中"
        );
    }

    public List<InvestmentTrustForm> findAll() {

        String sql =
                "SELECT bankName, branchName, bankAccountType, " +
                        "bankAccountNum, name, fundName, money, " +
                        "applicationDate, purchaseDate, status " +
                        "FROM investmentTrust_table " +
                        "ORDER BY applicationDate DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {

            InvestmentTrustForm form = new InvestmentTrustForm();

            form.setBankName(rs.getString("bankName"));
            form.setBranchName(rs.getString("branchName"));
            form.setBankAccountTypeName(
                    rs.getString("bankAccountType")
            );
            form.setBankAccountNum(
                    rs.getString("bankAccountNum")
            );
            form.setName(rs.getString("name"));
            form.setFundName(rs.getString("fundName"));
            form.setMoney(rs.getInt("money"));

            Timestamp applicationTimestamp =
                    rs.getTimestamp("applicationDate");

            if (applicationTimestamp != null) {
                form.setApplicationDate(
                        applicationTimestamp.toLocalDateTime()
                );
            }

            Timestamp purchaseTimestamp =
                    rs.getTimestamp("purchaseDate");

            if (purchaseTimestamp != null) {
                form.setPurchaseDate(
                        purchaseTimestamp.toLocalDateTime()
                );
            }

            form.setStatus(rs.getString("status"));

            return form;
        });
    }
}