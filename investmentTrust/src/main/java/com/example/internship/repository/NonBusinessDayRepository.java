package com.example.internship.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// 非営業日カレンダー(non_business_day)の取得を行うRepository。
// 土日祝・証券会社休業日・国内外市場休場日・ファンド休日を1本のテーブルにまとめて管理しており、
// 約定日判定では種別を区別せず「非営業日かどうか」だけを見る
@Repository
public class NonBusinessDayRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    // 登録済みの非営業日を全件取得する(約定日計算のたびに最新の登録内容を反映するため都度取得する)
    public Set<LocalDate> findAllDates() {
        String sql = "SELECT non_business_date FROM non_business_day";
        List<LocalDate> dates = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getDate("non_business_date").toLocalDate());
        return new HashSet<>(dates);
    }
}
