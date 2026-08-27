package com.example.internship.repository;

import com.example.internship.entity.Fund;
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


    /**
     * 投資信託の注文を登録
     *
     * money = 手数料込みの金額
     * fee   = 手数料
     */
    public void create(InvestmentTrustForm form) {

        String sql =
                "INSERT INTO investmentTrust_table " +
                        "(bankName, branchName, bankAccountType, bankAccountNum, " +
                        "name, fundName, money, fee, applicationDate, purchaseDate, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NULL, ?)";

        jdbcTemplate.update(
                sql,
                form.getBankName(),
                form.getBranchName(),
                form.getBankAccountTypeName(),
                form.getBankAccountNum(),
                form.getName(),
                form.getFundName(),
                form.getMoney(),
                form.getFee(),
                "確認中"
        );
    }


    /**
     * 投資信託の注文履歴を取得
     */
    public List<InvestmentTrustForm> findAll() {

        String sql =
                "SELECT bankName, branchName, bankAccountType, " +
                        "bankAccountNum, name, fundName, money, fee, " +
                        "applicationDate, purchaseDate, status " +
                        "FROM investmentTrust_table " +
                        "ORDER BY applicationDate DESC";

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    InvestmentTrustForm form =
                            new InvestmentTrustForm();

                    form.setBankName(
                            rs.getString("bankName")
                    );

                    form.setBranchName(
                            rs.getString("branchName")
                    );

                    form.setBankAccountTypeName(
                            rs.getString("bankAccountType")
                    );

                    form.setBankAccountNum(
                            rs.getString("bankAccountNum")
                    );

                    form.setName(
                            rs.getString("name")
                    );

                    form.setFundName(
                            rs.getString("fundName")
                    );

                    form.setMoney(
                            rs.getInt("money")
                    );

                    form.setFee(
                            rs.getInt("fee")
                    );

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

                    form.setStatus(
                            rs.getString("status")
                    );

                    return form;
                }
        );
    }


    /**
     * 銘柄一覧を取得
     *
     * feeRateも取得する
     */
    public List<Fund> findFunds() {

        String sql =
                "SELECT id, fundName, unitPrice, feeRate " +
                        "FROM fund_table " +
                        "ORDER BY id";

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    Fund fund =
                            new Fund();

                    fund.setId(
                            rs.getInt("id")
                    );

                    fund.setFundName(
                            rs.getString("fundName")
                    );

                    fund.setUnitPrice(
                            rs.getInt("unitPrice")
                    );

                    fund.setFeeRate(
                            rs.getBigDecimal("feeRate")
                    );

                    return fund;
                }
        );
    }


    /**
     * 銘柄名を部分一致検索
     *
     * feeRateも取得する
     */
    public List<Fund> searchFunds(String keyword) {

        String sql =
                "SELECT id, fundName, unitPrice, feeRate " +
                        "FROM fund_table " +
                        "WHERE fundName LIKE ? " +
                        "ORDER BY id " +
                        "LIMIT 20";

        String searchKeyword =
                "%" + keyword + "%";

        return jdbcTemplate.query(
                sql,
                new Object[]{searchKeyword},
                (rs, rowNum) -> {

                    Fund fund =
                            new Fund();

                    fund.setId(
                            rs.getInt("id")
                    );

                    fund.setFundName(
                            rs.getString("fundName")
                    );

                    fund.setUnitPrice(
                            rs.getInt("unitPrice")
                    );

                    fund.setFeeRate(
                            rs.getBigDecimal("feeRate")
                    );

                    return fund;
                }
        );
    }


    /**
     * 金融機関名を取得
     */
    public List<String> findBankNames() {

        String sql =
                "SELECT bankName " +
                        "FROM bank_table " +
                        "ORDER BY id";

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        rs.getString("bankName")
        );
    }


    /**
     * 支店名を取得
     */
    public List<String> findBranchNames() {

        String sql =
                "SELECT branchName " +
                        "FROM branch_table " +
                        "ORDER BY id";

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        rs.getString("branchName")
        );
    }


    /**
     * 口座種別を取得
     */
    public List<String> findBankAccountTypes() {

        String sql =
                "SELECT bankAccountType " +
                        "FROM bankAccountType_table " +
                        "ORDER BY id";

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        rs.getString("bankAccountType")
        );
    }
}