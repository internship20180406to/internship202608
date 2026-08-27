package com.example.internship.repository;

import com.example.internship.entity.BranchMaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

// 支店マスタの取得を行うRepository
@Repository
public class BranchMasterRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    // 支店マスタを全件取得する(金融機関コード→支店コード順)
    public List<BranchMaster> findAll() {
        String sql = "SELECT institution_code, branch_code, branch_name FROM branch_master ORDER BY institution_code, branch_code";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new BranchMaster(
                rs.getString("institution_code"),
                rs.getString("branch_code"),
                rs.getString("branch_name")
        ));
    }
}
