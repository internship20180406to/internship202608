package com.example.internship.entity;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

// 画面4（金額と振込指定日）の入力
@Data
public class AmountForm {

    @NotNull(message = "金額を入力してください")
    @Positive(message = "金額は1円以上で入力してください")
    private Integer money;

    @NotNull(message = "振込指定日を入力してください")
    @FutureOrPresent(message = "振込指定日に過去の日付は指定できません")//当日は指定可
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate transferDateTime;
}
