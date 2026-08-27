package com.example.internship.repository;

import com.example.internship.entity.Bank;
import com.example.internship.entity.Branch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 金融機関マスタ・支店マスタの参照。
 *
 * SQLとテーブル構造の知識は、このクラスの中だけに閉じ込める。
 * ServiceもControllerもテーブル名・列名を知らないので、
 * 将来テーブルを作り変えても直すのはここだけで済む。
 *
 * 参照専用なので、InvestmentTrustRepository のような更新系のメソッドは持たない。
 */
@Repository
public class BankMasterRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    /*
     * RowMapper:検索結果の1行（ResultSet）をJavaのオブジェクトに詰め替える処理。
     *
     * BeanPropertyRowMapper を使えば列名とフィールド名が一致していれば自動で詰めてくれるが、
     * 「どの列がどのフィールドになるか」がコードから読み取れる方が分かりやすいので、
     * ここでは手で書いている。
     *
     * 引数の rowNum は「何行目か」（0始まり）。今回は使わないが、RowMapper の
     * 決まった形なので省略できない。
     */
    private static final RowMapper<Bank> BANK_ROW_MAPPER = (rs, rowNum) -> {
        Bank bank = new Bank();
        bank.setBankCode(rs.getString("bankCode"));
        bank.setBankName(rs.getString("bankName"));
        bank.setBankKana(rs.getString("bankKana"));
        return bank;
    };

    private static final RowMapper<Branch> BRANCH_ROW_MAPPER = (rs, rowNum) -> {
        Branch branch = new Branch();
        branch.setBankCode(rs.getString("bankCode"));
        branch.setBranchCode(rs.getString("branchCode"));
        branch.setBranchName(rs.getString("branchName"));
        branch.setBranchKana(rs.getString("branchKana"));
        return branch;
    };

    /**
     * 金融機関コードで1件検索する。該当が無ければ空のOptionalを返す。
     *
     * ※jdbcTemplate.queryForObject は0件のときに例外（EmptyResultDataAccessException）を
     *   投げる仕様。「入力されたコードが存在しない」のは異常ではなく普通に起きることなので、
     *   例外ではなく query でListを受け取り、先頭を Optional として返している。
     */
    public Optional<Bank> findBank(String bankCode) {
        String sql = "SELECT bankCode, bankName, bankKana FROM bank_master WHERE bankCode = ?";
        return jdbcTemplate.query(sql, BANK_ROW_MAPPER, bankCode).stream().findFirst();
    }

    /** 金融機関を全件、コード順で返す（一覧から選ばせる画面用） */
    public List<Bank> findAllBanks() {
        String sql = "SELECT bankCode, bankName, bankKana FROM bank_master ORDER BY bankCode";
        return jdbcTemplate.query(sql, BANK_ROW_MAPPER);
    }

    /**
     * 支店を1件検索する。
     *
     * ★必ず bankCode と branchCode の両方で絞り込むこと。
     *   支店コードは銀行ごとに振られているので、branchCode だけで検索すると
     *   別の銀行の同じ番号の支店がヒットしてしまう
     *   （初期データでは 001「本店営業部」が4つの銀行すべてに存在する）。
     */
    public Optional<Branch> findBranch(String bankCode, String branchCode) {
        String sql = "SELECT bankCode, branchCode, branchName, branchKana FROM branch_master"
                + " WHERE bankCode = ? AND branchCode = ?";
        return jdbcTemplate.query(sql, BRANCH_ROW_MAPPER, bankCode, branchCode).stream().findFirst();
    }

    /** 指定した金融機関の支店を、コード順で全件返す（支店の絞り込み用） */
    public List<Branch> findBranchesByBank(String bankCode) {
        String sql = "SELECT bankCode, branchCode, branchName, branchKana FROM branch_master"
                + " WHERE bankCode = ? ORDER BY branchCode";
        return jdbcTemplate.query(sql, BRANCH_ROW_MAPPER, bankCode);
    }
}
