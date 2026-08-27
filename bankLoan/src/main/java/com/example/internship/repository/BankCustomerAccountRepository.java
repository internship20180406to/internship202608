package com.example.internship.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.example.internship.entity.BankCustomerAccount;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BankCustomerAccountRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Optional<BankCustomerAccount> findActiveAccount(
            String branchName,
            String accountType,
            String accountNumber,
            LocalDate birthDate) {

        String sql =
                "SELECT " +
                        "a.account_id, " +
                        "a.customer_id, " +
                        "c.customer_number, " +
                        "a.branch_name, " +
                        "a.account_type, " +
                        "a.account_number, " +
                        "c.last_name, " +
                        "c.first_name, " +
                        "c.last_name_kana, " +
                        "c.first_name_kana, " +
                        "c.birth_date " +
                        "FROM bank_account_master a " +
                        "JOIN bank_customer_master c " +
                        "ON a.customer_id = c.customer_id " +
                        "WHERE a.branch_name = ? " +
                        "AND a.account_type = ? " +
                        "AND a.account_number = ? " +
                        "AND c.birth_date = ? " +
                        "AND a.active = TRUE " +
                        "AND c.active = TRUE";

        List<BankCustomerAccount> accounts =
                jdbcTemplate.query(
                        sql,
                        (resultSet, rowNumber) ->
                                new BankCustomerAccount(
                                        resultSet.getInt(
                                                "account_id"
                                        ),
                                        resultSet.getInt(
                                                "customer_id"
                                        ),
                                        resultSet.getString(
                                                "customer_number"
                                        ),
                                        resultSet.getString(
                                                "branch_name"
                                        ),
                                        resultSet.getString(
                                                "account_type"
                                        ),
                                        resultSet.getString(
                                                "account_number"
                                        ),
                                        resultSet.getString(
                                                "last_name"
                                        ),
                                        resultSet.getString(
                                                "first_name"
                                        ),
                                        resultSet.getString(
                                                "last_name_kana"
                                        ),
                                        resultSet.getString(
                                                "first_name_kana"
                                        ),
                                        resultSet.getDate(
                                                "birth_date"
                                        ).toLocalDate()
                                ),
                        branchName,
                        accountType,
                        accountNumber,
                        Date.valueOf(birthDate)
                );

        return accounts.stream().findFirst();
    }
}