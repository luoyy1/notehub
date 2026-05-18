package com.notehub.module.scheduler.util;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Period;
import java.time.temporal.ChronoUnit;

/**
 * 日期计算工具类
 * 处理：天数差计算、百天判断、生日倒计时、跨年处理、闰年处理
 */
public final class DateUtil {

    private DateUtil() {
        // 工具类，禁止实例化
    }

    /**
     * 计算从 eventDate 到今天经过了多少天（含当天）
     * 示例：2025-01-01 到今天 2025-01-10 → 10 天
     */
    public static long daysBetween(LocalDate eventDate) {
        return ChronoUnit.DAYS.between(eventDate, LocalDate.now()) + 1;
    }

    /**
     * 计算距离某个目标日期还有多少天（绝对值倒计时）
     * 示例：今天 2025-01-10 到目标 2025-01-20 -> 10 天
     */
    public static long daysUntilTarget(LocalDate targetDate) {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), targetDate);
        return days < 0 ? 0 : days; // 如果已经过了返回 0
    }

    /**
     * 判断天数是否达到 100 的倍数
     */
    public static boolean isHundredsMultiple(long days) {
        return days > 0 && days % 100 == 0;
    }

    /**
     * 计算距离生日的天数
     * 会正确处理跨年情况
     *
     * @param birthday 生日日期（只取月-日，忽略年份）
     * @return 距离下一个生日的天数（0 表示今天就是生日）
     */
    public static long daysUntilBirthday(LocalDate birthday) {
        LocalDate today = LocalDate.now();
        MonthDay birthdayMD = MonthDay.from(birthday);

        // 今年的生日日期
        LocalDate thisYearBirthday = birthdayMD.atYear(today.getYear());

        if (thisYearBirthday.isBefore(today)) {
            // 今年的生日已经过了，算明年的
            return ChronoUnit.DAYS.between(today, birthdayMD.atYear(today.getYear() + 1));
        } else {
            return ChronoUnit.DAYS.between(today, thisYearBirthday);
        }
    }

    /**
     * 处理闰年 2月29日：在非闰年时映射到 2月28日
     */
    public static MonthDay safeMonthDay(LocalDate date) {
        MonthDay md = MonthDay.from(date);
        // 如果生日是 2月29日，但今年不是闰年，返回 2月28日
        if (md.getMonthValue() == 2 && md.getDayOfMonth() == 29) {
            LocalDate today = LocalDate.now();
            if (!today.isLeapYear()) {
                return MonthDay.of(2, 28);
            }
        }
        return md;
    }

    /**
     * 计算年龄（周岁）
     */
    public static int calculateAge(LocalDate birthday) {
        return Period.between(birthday, LocalDate.now()).getYears();
    }
}