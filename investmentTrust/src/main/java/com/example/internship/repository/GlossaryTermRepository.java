package com.example.internship.repository;

import com.example.internship.entity.GlossaryTerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

// 用語集(glossary_term)の取得を行うRepository。閲覧専用で、追加・編集機能は持たない
@Repository
public class GlossaryTermRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    // 用語集を表示順に全件取得する
    public List<GlossaryTerm> findAll() {
        String sql = "SELECT id, sort_order, term, description FROM glossary_term ORDER BY sort_order";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new GlossaryTerm(
                rs.getLong("id"),
                rs.getInt("sort_order"),
                rs.getString("term"),
                rs.getString("description")
        ));
    }
}
