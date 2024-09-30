package com.ebook.common.util;

import android.text.TextUtils;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Description: <DateUtil><br>
 */
public class DateUtil {
    private final static String TAG = "DateUtil";

    /**
     * 格式化时间字符串
     */
    public static String formatDate(String time, String formatStr) {
        Date setdate = parseTime(time);
        SimpleDateFormat sdf = new SimpleDateFormat(formatStr, Locale.CHINA);
        return sdf.format(setdate);
    }

    public static String formatDate(String time, FormatType type) {
        if (TextUtils.isEmpty(time)) {
            return "";
        }
        Date date = parseTime(time);
        return formatDate(date, type);
    }

    public static String formatDate(String time, FormatType fromType, FormatType toType) {
        if (TextUtils.isEmpty(time)) {
            return "";
        }
        Date date = parseTime(time, fromType);
        return formatDate(date, toType);
    }

    public static String formatDate(Date time, FormatType type) {
        if (time == null) {
            return "";
        }
        SimpleDateFormat sdf = getSimpleDateFormat(type);
        return sdf.format(time);
    }

    private static SimpleDateFormat getSimpleDateFormat(FormatType type) {
        return switch (type) {
            case yyyy -> new SimpleDateFormat("yyyy", Locale.CHINA);
            case yyyyMM -> new SimpleDateFormat("yyyy-MM", Locale.CHINA);
            case yyyyMMdd -> new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            case yyyyMMddHHmm -> new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
            case yyyyMMddHHmmss -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            case MMdd -> new SimpleDateFormat("MM-dd", Locale.CHINA);
            case HHmm -> new SimpleDateFormat("HH:mm", Locale.CHINA);
            case MM -> new SimpleDateFormat("MM", Locale.CHINA);
            case dd -> new SimpleDateFormat("dd", Locale.CHINA);
            case MMddHHmm -> new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);
        };
    }

    /**
     * 将时间字符串转化为date
     */
    public static Date parseTime(String dateStr, String formatStr) {
        Date date = null;
        try {
            DateFormat sdf = new SimpleDateFormat(formatStr, Locale.CHINA);
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            Log.e(TAG, "parseTime: ", e);
        }
        return date;
    }

    /**
     * 将字符串转换成date
     */
    public static Date parseTime(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Date date = null;
        try {
            date = sdf.parse(time);
        } catch (Exception e) {
            Log.e(TAG, "parseTime: ", e);
        }
        return date;
    }

    /**
     * 将字符串转换成date
     */
    public static Date parseTime(String time, FormatType type) {
        Date date = null;
        SimpleDateFormat sdf = getSimpleDateFormat(type);
        try {
            date = sdf.parse(time);
        } catch (Exception e) {
            Log.e(TAG, "parseTime: ", e);
        }
        return date;
    }

    /**
     * 增/减 offset 后的日期
     */
    public static Date addAndSubtractDate(int offset, Date date, int unit) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if (unit == Calendar.MONTH) {
            calendar.set(Calendar.DATE, 1);
        }
        calendar.set(unit, calendar.get(unit) + offset);
        return calendar.getTime();
    }

    /**
     * 计算两个日期之间相差的天数
     */
    public static int daysBetween(Date smdate, Date bdate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        smdate = sdf.parse(sdf.format(smdate));
        bdate = sdf.parse(sdf.format(bdate));
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);

        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     * 比较日期大小
     */
    public static int compareDate(Date smdate, Date bdate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        smdate = sdf.parse(sdf.format(smdate));
        bdate = sdf.parse(sdf.format(bdate));
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        return Long.compare(time2, time1);
    }

    /**
     * 根据日期算周几
     */
    public static String whatDay(Date date) {
        Calendar calendar = Calendar.getInstance();// 获得一个日历
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        calendar.set(year, month, day);// 设置当前时间,月份是从0月开始计算
        int number = calendar.get(Calendar.DAY_OF_WEEK);// 周表示1-7，是从周日开始，
        String[] str = {"", "周日", "周一", "周二", "周三", "周四", "周五", "周六",};
        return str[number];
    }

    public static String formatSecondToHourMinuteSecond(int second) {
        int h = 0;
        int d = 0;
        int s = 0;
        int temp = second % 3600;
        if (second > 3600) {
            h = second / 3600;
            if (temp != 0) {
                if (temp > 60) {
                    d = temp / 60;
                    if (temp % 60 != 0) {
                        s = temp % 60;
                    }
                } else {
                    s = temp;
                }
            }
        } else {
            d = second / 60;
            if (second % 60 != 0) {
                s = second % 60;
            }
        }
        return h + "时" + d + "分" + s + "秒";
    }

    public static String formatSecondToHourMinute(int duration) {
        if (duration < 60) {
            return duration + "秒";
        } else if (duration < 60 * 60) {
            return Math.round((float) duration / 60) + "分钟";
        } else {
            int second = duration % (60 * 60);
            if (second == 0) {
                return (duration / (60 * 60)) + "小时";
            } else {

                int round = Math.round((float) second / 60);
                if (round == 0) {
                    return (duration / (60 * 60)) + "小时";
                } else {
                    if (round == 60) {
                        return ((duration / (60 * 60)) + 1) + "小时";
                    } else {
                        return (duration / (60 * 60)) + "小时" + round + "分钟";
                    }
                }
            }
        }
    }

    /**
     * 说明 小于1分钟：”刚刚“ 小于1小时：”X分钟前“ 小于一天：”X小时前“ 小于一月：”X天前“ 小于一年：6-23 大于一年：2015-6-23
     */
    public static String formatTimeToDay(String dateStr) {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        DateFormat sdf2 = new SimpleDateFormat("MM-dd", Locale.CHINA);
        DateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        try {
            Date date = sdf.parse(dateStr);
            if (date == null) {
                return "";
            }
            int minute = (int) ((System.currentTimeMillis() - date.getTime()) / 1000 / 60);
            if (minute <= 0) {
                return "刚刚";

            } else if (minute / 60 == 0) {
                return minute + "分钟前";

            } else if (minute / (60 * 24) == 0) {
                return minute / 60 + "小时前";

            } else if (minute / (60 * 24 * 30) == 0) {
                return minute / (60 * 24) + "天前";
            } else if (minute / (60 * 24 * 30 * 12) == 0) {
                return sdf2.format(date);
            } else {
                return sdf3.format(date);
            }

        } catch (Exception e) {
            Log.e(TAG, "formatTimeToDay: ", e);
        }
        return dateStr;
    }

    /**
     * 获取几个小时以后的时间戳
     */
    public static String getLaterTimeByHour(int hour) {
        Date d = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.HOUR, now.get(Calendar.HOUR) + hour);
        // now.set(Calendar.MINUTE, now.get(Calendar.MINUTE) + 30);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        return sdf.format(now.getTime());
    }

    /**
     * 获取几天以后的时间戳
     */
    public static String getLaterTimeByDay(int day) {
        return getLaterTimeByHour(day * 24);
    }

    /**
     * 获取给定时间以后几天的时间戳
     */
    public static String getLaterTimeByDay(String date, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseTime(date, FormatType.yyyyMMdd));
        calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) + day * 24);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        return sdf.format(calendar.getTime());
    }

    /**
     * 获取当前时间的位置：一天24小时以半小时为单位划分为48个单元格
     */
    public static int getCurrTimePosition() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        return (int) Math.ceil((double) (calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)) / 30);
    }

    public enum FormatType {
        yyyy, yyyyMM, yyyyMMdd, yyyyMMddHHmm, yyyyMMddHHmmss, MMdd, HHmm, MM, dd, MMddHHmm
    }
}