package com.example.internship.entity;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
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
    //1回の振込の上限。残高とは別の決まりなのでここで持つ
    @Max(value = 2_000_000, message = "1回の振込は2,000,000円までです")
    private Integer money;

    //手数料を入力金額に含めるか。
    //含める場合、入力額から手数料を引いた分が相手に届く
    private boolean feeIncluded;

    @NotNull(message = "振込指定日を入力してください")
    @FutureOrPresent(message = "振込指定日に過去の日付は指定できません")//当日は指定可
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate transferDateTime;
}
