package com.example.internship.master;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// 金融機関マスタの読み取り。マスタなので参照だけで、更新系は持たない
@Repository
public class BankMasterRepository {

    private static final RowMapper<Bank> ROW_MAPPER =
            (rs, rowNum) -> new Bank(rs.getString("bankCode"), rs.getString("bankName"));

    private final JdbcTemplate jdbcTemplate;

    // コンストラクタが1つだけなら @Autowired は不要（Spring 4.3以降）
    public BankMasterRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Bank> findAll() {
        String sql = "SELECT bankCode, bankName FROM bank_master ORDER BY bankCode";
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    // 画面の一覧に出す分だけ。ここに出ないものは検索でのみ到達できる
    public List<Bank> findMajor() {
        String sql = "SELECT bankCode, bankName FROM bank_master WHERE isMajor = 1 ORDER BY bankCode";
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    // 選択された金融機関が実在するかの確認に使う
    public Optional<Bank> findByCode(String bankCode) {
        String sql = "SELECT bankCode, bankName FROM bank_master WHERE bankCode = ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, bankCode).stream().findFirst();
    }

    // 検索欄用。名前の部分一致か、コードの前方一致で拾う
    public List<Bank> search(String keyword) {
        String sql = """
                SELECT bankCode, bankName
                  FROM bank_master
                 WHERE bankName LIKE ? ESCAPE '!'
                    OR bankCode LIKE ? ESCAPE '!'
                 ORDER BY bankCode
                """;
        String escaped = escapeLike(keyword);
        return jdbcTemplate.query(sql, ROW_MAPPER, "%" + escaped + "%", escaped + "%");
    }

    // 利用者が入力した % や _ をワイルドカードとして扱わせない。
    // SQL側の ESCAPE '!' と対になっているので、片方だけ変えると効かなくなる
    static String escapeLike(String keyword) {
        return keyword.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}
