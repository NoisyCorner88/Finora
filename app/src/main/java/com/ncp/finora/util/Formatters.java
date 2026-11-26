package com.ncp.finora.util;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public final class Formatters {
    private static final Locale ID = new Locale("id", "ID");
    private static final DateTimeFormatter DAY_MONTH = DateTimeFormatter.ofPattern("d MMM", ID);
    private static final DateTimeFormatter MONTH_SHORT = DateTimeFormatter.ofPattern("MMM", ID);
    private static final DateTimeFormatter MONTH_YEAR = DateTimeFormatter.ofPattern("MMMM yyyy", ID);

    private Formatters() { }

    /** 18420850 -> "Rp 18.420.850" (negatif: "-Rp 1.000"). */
    public static String rupiah(double value) {
        NumberFormat nf = NumberFormat.getInstance(ID);
        nf.setMaximumFractionDigits(0);
        String body = "Rp " + nf.format(Math.abs(value));
        return value < 0 ? "-" + body : body;
    }

    public static String currency(double value) {
        return rupiah(value);
    }

    /** Dengan tanda arah: "+Rp 3.850.000" / "-Rp 249.000". */
    public static String signedRupiah(double amount, boolean income) {
        return (income ? "+" : "-") + rupiah(Math.abs(amount));
    }

    /** 1250000 -> "1,3jt", 850000 -> "850rb", 2500000000 -> "2,5M". */
    public static String compactRupiah(double value) {
        double a = Math.abs(value);
        String s;
        if (a >= 1_000_000_000d) s = oneDecimal(a / 1_000_000_000d) + "M";
        else if (a >= 1_000_000d) s = oneDecimal(a / 1_000_000d) + "jt";
        else if (a >= 1_000d) s = oneDecimal(a / 1_000d) + "rb";
        else s = String.valueOf(Math.round(a));
        return value < 0 ? "-" + s : s;
    }

    private static String oneDecimal(double d) {
        String r = String.format(ID, "%.1f", d);
        return r.endsWith(",0") ? r.substring(0, r.length() - 2) : r;
    }

    /** Nomor kartu tersamarkan; bila belum ada digit tampilkan 4 titik. */
    public static String maskedCardNumber(String digits) {
        String d = digits == null ? "" : digits.replaceAll("[^0-9]", "");
        return d.isEmpty() ? "\u2022\u2022\u2022\u2022 \u2022\u2022\u2022\u2022 \u2022\u2022\u2022\u2022 \u2022\u2022\u2022\u2022"
                : "\u2022\u2022\u2022\u2022 \u2022\u2022\u2022\u2022 \u2022\u2022\u2022\u2022 \u2022" + d;
    }

    public static String monthShort(YearMonth ym) {
        return ym.format(MONTH_SHORT);
    }

    public static String maskedRupiah() {
        return "Rp ••••••••";
    }

    public static String monthLabel(YearMonth ym) {
        return ym.format(MONTH_YEAR);
    }

    /** "Hari ini", "Kemarin", atau "21 Okt". */
    public static String relativeDate(String isoDate) {
        try {
            LocalDate date = LocalDate.parse(isoDate);
            long diff = ChronoUnit.DAYS.between(date, LocalDate.now());
            if (diff == 0) return "Hari ini";
            if (diff == 1) return "Kemarin";
            return date.format(DAY_MONTH);
        } catch (Exception e) {
            return isoDate;
        }
    }

    public static String greeting(int hourOfDay) {
        if (hourOfDay < 11) return "Selamat pagi";
        if (hourOfDay < 15) return "Selamat siang";
        if (hourOfDay < 18) return "Selamat sore";
        return "Selamat malam";
    }

    /** Batas bulan [from, to) dalam format YYYY-MM-DD untuk query. */
    public static String monthStart(YearMonth ym) {
        return ym.atDay(1).toString();
    }

    public static String nextMonthStart(YearMonth ym) {
        return ym.plusMonths(1).atDay(1).toString();
    }
}
