package org.xiaoxingqi.shengxi.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import org.xiaoxingqi.shengxi.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by yzm on 2017/11/23.
 * 判断当前时间
 */
@SuppressLint("SimpleDateFormat")
public class TimeUtils {
    private static SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);
    private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
    private static SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm", Locale.CHINA);
    private static SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    private static SimpleDateFormat simpleYYmmdd = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);

    private static SimpleDateFormat sdfymd = new SimpleDateFormat("yyyy年_MM月dd日");
    private static SimpleDateFormat sdfhm = new SimpleDateFormat("H点mm分");
    private static SimpleDateFormat sdfmdhm = new SimpleDateFormat("M月dd日_H点mm分");
    private static SimpleDateFormat homeFriend = new SimpleDateFormat("M月dd日 H点mm分");
    private static SimpleDateFormat timeMachine = new SimpleDateFormat("yyyy年M月dd日 H点mm分");

    private static final SimpleDateFormat monthSdf = new SimpleDateFormat("yyyy-M", Locale.CHINA);
    private static SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy.MM.dd");
    private static TimeUtils sTimeUtils;
    private static final SimpleDateFormat currentYM = new SimpleDateFormat("yyyyMM", Locale.CHINA);
    private static final SimpleDateFormat sdf3 = new SimpleDateFormat("yyyym_d", Locale.CHINA);


    public static TimeUtils getInstance() {
        return sTimeUtils;
    }

    private TimeUtils(Context context) {

    }

    public static void initTimeUtils(Context context) {
        if (sTimeUtils == null) {
            synchronized (TimeUtils.class) {
                if (sTimeUtils == null) {
                    sTimeUtils = new TimeUtils(context);
                }
            }
        }
    }

    public static String parseCalender(int time) {
        return simpleYYmmdd.format(new Date(time * 1000L));
    }

    public static String getCurrentYM() {
        return currentYM.format(new Date(System.currentTimeMillis()));
    }

    public static int getCurrentDays() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public static String parse(int time) {
        return sdf4.format(new Date(time * 1000L));
    }

    //获取年月 1月 Jan 2 Feb 3 Mar 4 Apr 5 May 6 Jun 7 Jul 8 Aug 9 Sep 10 Oct 11 Nov 12 Dec

    /**
     * 时光机获取中的年月
     *
     * @param time 时间撮
     * @return
     */
    public String timeMachine(int time) {
        Date date = new Date(time * 1000L);
        String format = monthSdf.format(date);
        String[] split = format.split("-");
        String month = split[1].equals("1") ? "Jan" : split[1].equals("2") ? "Feb" : split[1].equals("3") ? "Mar" : split[1].equals("4") ? "Apr" : split[1].equals("5")
                ? "May" : split[1].equals("6") ? "Jun" : split[1].equals("7") ? "Jul" : split[1].equals("8") ? "Aug" : split[1].equals("9") ? "Sep" : split[1].equals("10")
                ? "Oct" : split[1].equals("11") ? "Nov" : split[1].equals("12") ? "Dec" : "";
        String parse = month + " " + split[0];
        return parse;
    }

    /**
     * 什么逼需求
     *
     * @param intMonth
     * @return
     */
    public static String getCharMonth(String intMonth) {
        return intMonth.equals("1") ? "Jan" : intMonth.equals("2") ? "Feb" : intMonth.equals("3") ? "Mar" : intMonth.equals("4") ? "Apr" : intMonth.equals("5")
                ? "May" : intMonth.equals("6") ? "Jun" : intMonth.equals("7") ? "Jul" : intMonth.equals("8") ? "Aug" : intMonth.equals("9") ? "Sep" : intMonth.equals("10")
                ? "Oct" : intMonth.equals("11") ? "Nov" : intMonth.equals("12") ? "Dec" : "";
    }

    private static SimpleDateFormat logSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA);

    public String parseLogTime(long time) {
        Date date = new Date(time);
        return logSDF.format(date);
    }

    public String parseYearMonth(int time) {
        return monthSdf.format(new Date(time * 1000L));
    }

    public String paserLong(long time) {
        Date date = new Date(time * 1000);
        return sdf2.format(date);
    }

    public long paserString(String time) {
        try {
            Date date = sdf.parse(time);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String paserTime(Context context, String time) {
        try {
            int today = IsToday(time);
            long createTime = Long.parseLong(time) * 1000;
            if (today == 0) {
                long currentTime = System.currentTimeMillis();
                long dTime = (currentTime - createTime) / 1000;// 秒
                if (dTime / 60 < 60) {
                    if (dTime / 60 == 0) {
                        return context.getResources().getString(R.string.string_Just);
                    }
                    return dTime / 60 + " " + context.getResources().getString(R.string.string_minute_ago);
                } else if (dTime / 60 / 60 <= 24) {//判断小时
                    return dTime / 60 / 60 + " " + context.getString(R.string.string_hour_ago);
                } else {

                }
            } else if (today == -1) {//返回格式  昨天 xx:xx:xx;
                return context.getString(R.string.string_yesterday) + sdf1.format(new Date(createTime));
            } else {
                return sdf.format(new Date(createTime));
            }
        } catch (ParseException e) {
            e.printStackTrace();
            /**
             * 说明是long 数据
             */
            try {
                Date date = new Date(Long.parseLong(time));
                String format = sdf.format(date);
                return format;
            } catch (NumberFormatException e1) {
                e1.printStackTrace();
            }
        }
        return time;
    }

    /**
     * 1.判断是否是今年
     * 2判断是否是今天
     * 用于心情薄中记忆的展示
     *
     * @param context
     * @param time
     * @return
     */
    public String formatterTime(Context context, int time) {
        try {
            int isToday = IsToday(time);
            if (isToday == 0) {//今天
                return "今天_" + sdfhm.format(new Date(time * 1000l)).replace("点", context.getResources().getString(R.string.string_clock_dian));
            } else if (isToday == -1) {//返回格式  昨天
                return context.getString(R.string.string_yesterday) + "_" + sdfhm.format(new Date(time * 1000l)).replace("点", context.getResources().getString(R.string.string_clock_dian));
            } else if (isToday == 100) {//不是今年
                return sdfymd.format(new Date(time * 1000l));
            } else {
                return sdfmdhm.format(new Date(time * 1000l)).replace("点", context.getResources().getString(R.string.string_clock_dian));
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return sdfmdhm.format(new Date(time * 1000l)).replace("点", context.getResources().getString(R.string.string_clock_dian));
        }
    }

    public int IsToday(int day) throws ParseException {
        Calendar pre = Calendar.getInstance();
        Date predate = new Date(System.currentTimeMillis());//当前时间
        pre.setTime(predate);
        Calendar cal = Calendar.getInstance();
        try {
            Date date = new Date(day * 1000L);
            cal.setTime(date);
            if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {//同一年
                int diffDay = cal.get(Calendar.DAY_OF_YEAR) - pre.get(Calendar.DAY_OF_YEAR);
                return diffDay;
            } else if (cal.get(Calendar.YEAR) > (pre.get(Calendar.YEAR))) {
                long times = day - System.currentTimeMillis() / 1000;
                return (int) Math.ceil(times / 60f / 60 / 24);
            }
            return 100;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 100;
        }
    }

    /**
     * 判断是否是今天
     *
     * @param day
     * @return 0 今天 -1是昨天 其他为超过时间 不做处理
     * @throws ParseException
     */
    public int IsToday(String day) throws ParseException {
        Calendar pre = Calendar.getInstance();
        Date predate = new Date(System.currentTimeMillis());//当前时间
        pre.setTime(predate);
        Calendar cal = Calendar.getInstance();
        try {
            Date date = new Date(Long.parseLong(day) * 1000l);
            cal.setTime(date);
            if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {
                int diffDay = cal.get(Calendar.DAY_OF_YEAR) - pre.get(Calendar.DAY_OF_YEAR);
                return diffDay;
            }
            return 100;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 100;
        }
    }

    /**
     * 判断是否是昨天
     *
     * @param day
     * @return
     * @throws ParseException
     */
    public boolean IsYesterday(String day) throws ParseException {
        Calendar pre = Calendar.getInstance();
        pre.setTime(new Date(System.currentTimeMillis()));
        Calendar cal = Calendar.getInstance();
        Date date = getDateFormat().parse(day);
        cal.setTime(date);

        if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {
            int diffDay = cal.get(Calendar.DAY_OF_YEAR)
                    - pre.get(Calendar.DAY_OF_YEAR);
            if (diffDay == -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否是昨天  格式 20160911
     *
     * @return
     */
    public String niumsDays(Context context, String time) throws ParseException {
        Calendar pre = Calendar.getInstance();
        Date predate = new Date(System.currentTimeMillis());//当前时间
        pre.setTime(predate);
        Date judedate = new Date(Long.parseLong(time) * 1000);
        Calendar cal = Calendar.getInstance();
        cal.setTime(judedate);
        if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {
            int diffDay = cal.get(Calendar.DAY_OF_YEAR) - pre.get(Calendar.DAY_OF_YEAR);
            if (diffDay == 0) {
                return context.getResources().getString(R.string.date_Today);
            } else if (diffDay == -1) {
                return context.getResources().getString(R.string.date_Yesterday);
            } else if (diffDay == -2) {
                return context.getResources().getString(R.string.string_before_yesterday);
            } else {
                return sSimpleDateFormat.format(judedate);
            }
        } else {
            return sSimpleDateFormat.format(judedate);
        }
    }

    private ThreadLocal<SimpleDateFormat> DateLocal = new ThreadLocal<>();

    public SimpleDateFormat getDateFormat() {
        if (null == DateLocal.get()) {
            DateLocal.set(sdf);
        }
        return DateLocal.get();
    }

    /**
     * 匹配年
     *
     * @param time
     * @return
     */
    public String paserYyMm(int time) {
        if (time == 0)
            return "未知";
        return sSimpleDateFormat.format(new Date(time * 1000L));
    }


    public String paserYyMm(long time) {
        if (time == 0)
            return "未知";
        return simpleYYmmdd.format(new Date(time * 1000L));
    }

    private static SimpleDateFormat parseFile = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);

    public String parseFileTime(long time) {
        if (time == 0)
            return parseFile.format(new Date(System.currentTimeMillis()));
        return parseFile.format(new Date(time));
    }

    /**
     * 1-300  显示S
     * >300 显示分钟
     *
     * @param time
     * @return
     */
    public static String formatterS(Context context, int time) {
        if (time < 300) {
            return time + "秒";
        } else {
            return time / 60 + context.getResources().getString(R.string.string_second);
        }
    }

    public String nearbyTime(Context context, int time) {
        try {
            int isToday = IsToday(time);
            if (isToday == 0) {
                long currentTime = System.currentTimeMillis();
                long dTime = (currentTime - time * 1000l) / 1000;// 秒
                if (dTime / 60 < 60) {
                    if (dTime / 60 == 0) {
                        return context.getResources().getString(R.string.string_Just);
                    }
                    return dTime / 60 + context.getResources().getString(R.string.string_minute_ago);
                } else if (dTime / 60 / 60 <= 3) {//判断小时
                    return dTime / 60 / 60 + context.getString(R.string.string_hour_ago);
                } else {
                    return "今天";
                }
            } else if (isToday == -1) {//返回格式  昨天 xx:xx:xx;
                return context.getString(R.string.string_yesterday);
            } else if (isToday == -2) {
                return "前天";
            } else {
                return Math.abs(isToday) + "天前";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * 首页心情展示时间格式
     *
     * @param context
     * @param time
     * @return
     */
    public String paserFriends(Context context, int time) {

        try {
            int isToday = IsToday(time);
            if (isToday == 0) {
                long currentTime = System.currentTimeMillis();
                long dTime = (currentTime - time * 1000L) / 1000;// 秒
                if (dTime / 60 < 60) {
                    if (dTime / 60 == 0) {
                        return context.getResources().getString(R.string.string_Just);
                    }
                    return dTime / 60 + context.getResources().getString(R.string.string_minute_ago);
                } else {//判断小时
                    return dTime / 60 / 60 + context.getString(R.string.string_hour_ago);
                }
            } else if (isToday == -1) {//返回格式  昨天 xx:xx:xx;
                return context.getString(R.string.string_yesterday) + sdfhm.format(new Date(time * 1000L))/*.replace("点", context.getResources().getString(R.string.string_clock_dian))*/;
            } else {
                return homeFriend.format(new Date(time * 1000L))/*.replace("点", context.getResources().getString(R.string.string_clock_dian))*/;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 用于世界
     *
     * @return
     */
    public String paserWorl(Context context, int time) {
        try {
            int isToday = IsToday(time);
            if (isToday == 0) {
                long currentTime = System.currentTimeMillis();
                long dTime = (currentTime - time * 1000l) / 1000;// 秒
                if (dTime / 60 < 60) {
                    if (dTime / 60 == 0) {
                        return context.getResources().getString(R.string.string_Just);
                    }
                    return dTime / 60 + context.getResources().getString(R.string.string_minute_ago);
                } else {//判断小时
                    return dTime / 60 / 60 + context.getString(R.string.string_hour_ago);
                }
            } else if (isToday == -1) {//返回格式  昨天 xx:xx:xx;
                return context.getString(R.string.string_yesterday);
            } else {
                return sdfmdhm.format(new Date(time * 1000l)).split("_")[0].replace("点", context.getResources().getString(R.string.string_clock_dian));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String paserTimeMachine(Context context, int time) {
        return timeMachine.format(new Date(time * 1000l)).replace("点", context.getResources().getString(R.string.string_clock_dian));
    }

    /**
     * 计算起始时间到当前的天数
     *
     * @return
     */
    public int getLimitDay(int time) {
        int today = differentDays(time);
        return Math.abs(today);
    }

    public static int differentDays(int start) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(new Date(start * 1000l));
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(new Date(System.currentTimeMillis()));
        int day1 = cal1.get(Calendar.DAY_OF_YEAR);
        int day2 = cal2.get(Calendar.DAY_OF_YEAR);
        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);
        if (year1 != year2) { //同一年
            int timeDistance = 0;
            for (int i = year1; i < year2; i++) {
                if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0) {//闰年
                    timeDistance += 366;
                } else {//不是闰年
                    timeDistance += 365;
                }
            }
            return timeDistance + (day2 - day1);
        } else {//不同年
            return day2 - day1;
        }
    }

    /**
     * 时间转成时间撮
     *
     * @return
     */
    public static int string2Long(String formatterTime) {
        try {
            return (int) (sdf2.parse(formatterTime).getTime() / 1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int achieveS2Int(String date) {
        try {
            return (int) (simpleYYmmdd.parse(date).getTime() / 1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
