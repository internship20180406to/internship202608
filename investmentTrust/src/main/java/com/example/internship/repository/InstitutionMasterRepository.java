package com.example.internship.repository;

import com.example.internship.entity.InstitutionMaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

// 金融機関マスタの取得を行うRepository
@Repository
public class InstitutionMasterRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    // 金融機関マスタを全件取得する(コード順)
    public List<InstitutionMaster> findAll() {
        String sql = "SELECT institution_code, institution_name FROM institution_master ORDER BY institution_code";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new InstitutionMaster(
                rs.getString("institution_code"),
                rs.getString("institution_name")
        ));
    }
}
