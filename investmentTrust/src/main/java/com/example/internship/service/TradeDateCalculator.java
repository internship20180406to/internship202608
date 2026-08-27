package com.example.internship.service;

import com.example.internship.repository.NonBusinessDayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

/**
 * 約定日を計算する。
 * ルール: 平日かつ締切時間より前の注文は当日約定、それ以外(締切時間以降・非営業日)は翌営業日約定。
 * 締切時間は固定値(15:00)。以前は行員モードから変更できたが、運用上変更する必要がないため固定化した。
 * 非営業日(土日祝・証券会社休業日・国内外市場休場日・ファンド休日)はDB(non_business_day)にまとめて登録されたものを使用する。
 */
@Component
public class TradeDateCalculator {

    private static final LocalTime CUTOFF_TIME = LocalTime.of(15, 0);

    @Autowired
    private NonBusinessDayRepository nonBusinessDayRepository;

    // 注文日時から約定日を算出する
    public LocalDate calculate(LocalDateTime orderDatetime) {
        Set<LocalDate> nonBusinessDays = nonBusinessDayRepository.findAllDates();

        LocalDate date = orderDatetime.toLocalDate();
        LocalTime time = orderDatetime.toLocalTime();

        if (!isBusinessDay(date, nonBusinessDays)) {
            return nextBusinessDay(date, nonBusinessDays);
        }

        if (time.isBefore(CUTOFF_TIME)) {
            return date;
        }

        return nextBusinessDay(date, nonBusinessDays);
    }

    // 注文日時と約定日を比較し、当日中の約定扱いかどうかを判定する(画面表示用)
    public boolean isSameDayTrade(LocalDateTime orderDatetime, LocalDate tradeDate) {
        return orderDatetime.toLocalDate().equals(tradeDate);
    }

    // 受付締切時間を取得する(画面表示用)
    public LocalTime getCutoffTime() {
        return CUTOFF_TIME;
    }

    // 指定日の翌営業日(非営業日を飛ばした次の営業日)を返す
    private LocalDate nextBusinessDay(LocalDate date, Set<LocalDate> nonBusinessDays) {
        LocalDate next = date.plusDays(1);
        while (!isBusinessDay(next, nonBusinessDays)) {
            next = next.plusDays(1);
        }
        return next;
    }

    // 土日でなく、かつ非営業日カレンダーにも登録されていなければ営業日とみなす
    private boolean isBusinessDay(LocalDate date, Set<LocalDate> nonBusinessDays) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }
        return !nonBusinessDays.contains(date);
    }
}
