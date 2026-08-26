package com.example.internship.payee;

import com.example.internship.entity.BankTransferInput;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// 登録した振込先の読み書き。
//
// どのメソッドも必ず userId を受け取る。id だけで引けるようにすると、
// 他人の登録先を id 指定で読んだり消したりできてしまう。
@Repository
public class PayeeRepository {

    private static final RowMapper<Payee> ROW_MAPPER = (rs, rowNum) -> new Payee(
            rs.getInt("id"),
            rs.getString("nickname"),
            rs.getString("bankCode"),
            rs.getString("bankName"),
            rs.getString("branchCode"),
            rs.getString("branchName"),
            rs.getString("bankAccountType"),
            rs.getString("bankAccountNum"),
            rs.getString("name"));

    private static final String COLUMNS = """
            id, nickname, bankCode, bankName, branchCode, branchName,
            bankAccountType, bankAccountNum, name
            """;

    private final JdbcTemplate jdbcTemplate;

    public PayeeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Payee> findAll(String userId) {
        String sql = "SELECT " + COLUMNS + " FROM payee WHERE userId = ? ORDER BY id DESC";
        return jdbcTemplate.query(sql, ROW_MAPPER, userId);
    }

    // 選ばれた登録先が、その利用者のものとして実在するかの確認に使う
    public Optional<Payee> find(String userId, int id) {
        String sql = "SELECT " + COLUMNS + " FROM payee WHERE userId = ? AND id = ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, userId, id).stream().findFirst();
    }

    // 既に同じ振込先が登録されているか。登録の前に画面で知らせるために使う
    public boolean exists(String userId, BankTransferInput input) {
        String sql = """
                SELECT COUNT(*) FROM payee
                 WHERE userId = ? AND bankCode = ? AND branchCode = ?
                   AND bankAccountType = ? AND bankAccountNum = ?
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId,
                input.getBankCode(), input.getBranchCode(),
                input.getBankAccountType(), input.getBankAccountNum());
        return count != null && count > 0;
    }

    // 登録する。二重登録はDBのUNIQUE制約でも止まるので、
    // exists() の確認をすり抜けた同時登録もここで弾かれる。
    // 登録できたら true、既にあれば false
    public boolean create(String userId, String nickname, BankTransferInput input) {
        String sql = """
                INSERT INTO payee
                    (userId, nickname, bankCode, bankName, branchCode, branchName,
                     bankAccountType, bankAccountNum, name)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try {
            jdbcTemplate.update(sql, userId, nickname,
                    input.getBankCode(), input.getBankName(),
                    input.getBranchCode(), input.getBranchName(),
                    input.getBankAccountType(), input.getBankAccountNum(), input.getName());
            return true;
        } catch (DuplicateKeyException e) {
            return false;
        }
    }

    // 消せるのは自分の登録先だけ。消せたら true
    public boolean delete(String userId, int id) {
        String sql = "DELETE FROM payee WHERE userId = ? AND id = ?";
        return jdbcTemplate.update(sql, userId, id) > 0;
    }
}
