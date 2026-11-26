package com.ncp.finora.util;

import android.graphics.Rect;

import com.google.mlkit.vision.text.Text;
import com.ncp.finora.data.local.entity.ReceiptItemEntity;
import com.ncp.finora.data.local.model.ParsedReceipt;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser struk dari hasil ML Kit. Perbaikan utama:
 *  - baris digabung berdasarkan posisi vertikal (label & harga sering terpisah blok),
 *  - format angka Indonesia "15.000" / "15,000" / "1.500,50" dibaca benar,
 *  - tanggal divalidasi, jam tidak lagi salah mengambil harga ("1.50"),
 *  - kategori tidak lagi salah karena kata umum seperti "air".
 */
public class ReceiptOcrParser {
    public static final String DEFAULT_MERCHANT = "Toko / Merchant";

    private static final String AMT = "\\d{1,3}(?:[.,]\\d{3})+(?:[.,]\\d{1,2})?|\\d+(?:[.,]\\d{1,2})?";
    private static final Pattern PRICE = Pattern.compile("(?<![\\d.,])(?:rp\\.?\\s*)?(" + AMT + ")(?!\\d)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern QTY_X_PRICE = Pattern.compile(
            "(?<!\\d)(\\d{1,3})\\s*[xX@]\\s*(?:rp\\.?\\s*)?(" + AMT + ")(?!\\d)", Pattern.CASE_INSENSITIVE);

    private static final Pattern DATE_ISO = Pattern.compile("(?<!\\d)(\\d{4})[/\\-.](\\d{1,2})[/\\-.](\\d{1,2})(?!\\d)");
    private static final Pattern DATE_NUM = Pattern.compile("(?<!\\d)(\\d{1,2})[/\\-.](\\d{1,2})[/\\-.](\\d{4}|\\d{2})(?!\\d)");
    private static final Pattern DATE_TEXT = Pattern.compile(
            "(?<!\\d)(\\d{1,2})\\s*[-/ ]?\\s*(jan|feb|mar|apr|mei|may|jun|jul|agu|aug|sep|okt|oct|nov|des|dec)[a-z]*\\.?\\s*[-/ ]?\\s*(\\d{4}|\\d{2})(?!\\d)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern TIME_COLON = Pattern.compile("(?<!\\d)([01]?\\d|2[0-3]):([0-5]\\d)(?::[0-5]\\d)?(?!\\d)");
    private static final Pattern TIME_DOT = Pattern.compile("(?<![\\d.,])([01]?\\d|2[0-3])\\.([0-5]\\d)(?![\\d.,])");

    private static final String[] MONTHS_ID = {"Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"};

    private static final String[] MINIMARKETS = {"indomaret", "alfamart", "alfamidi", "superindo",
            "hypermart", "transmart", "lawson", "circle k", "familymart", "giant", "lotte", "carrefour"};

    public static ParsedReceipt parse(Text mlKitText, String imagePath) {
        ParsedReceipt receipt = new ParsedReceipt();
        receipt.imagePath = imagePath != null ? imagePath : "";
        if (mlKitText == null) { setDefaultDate(receipt); return receipt; }

        receipt.rawText = mlKitText.getText();
        List<String> rows = buildRows(mlKitText);
        if (rows.isEmpty()) { setDefaultDate(receipt); return receipt; }

        int merchantIdx = extractMerchantName(rows, receipt);
        extractDateTime(rows, receipt);
        extractTotals(rows, receipt);
        extractItems(rows, receipt, merchantIdx);

        double itemsSum = 0;
        for (ReceiptItemEntity it : receipt.items) itemsSum += it.unitPrice * it.quantity;

        if (receipt.total <= 0 && receipt.subtotal > 0) {
            receipt.total = receipt.subtotal + receipt.tax - receipt.discount;
        } else if (receipt.total <= 0 && itemsSum > 0) {
            receipt.total = Math.max(0, itemsSum + receipt.tax - receipt.discount);
            if (receipt.subtotal <= 0) receipt.subtotal = itemsSum;
        }
        if (receipt.total <= 0) {                       // terakhir: pakai angka terbesar di struk
            double max = 0;
            for (String r : rows) if (!isDateOrTimeRow(r)) max = Math.max(max, lastPrice(r));
            receipt.total = max;
        }
        if (receipt.subtotal <= 0 && receipt.total > 0) {
            receipt.subtotal = itemsSum > 0 ? itemsSum : Math.max(0, receipt.total - receipt.tax + receipt.discount);
        }
        if (receipt.items.isEmpty() && receipt.total > 0) {
            ReceiptItemEntity item = new ReceiptItemEntity();
            item.itemName = DEFAULT_MERCHANT.equals(receipt.merchantName) ? "Pembelian Struk" : receipt.merchantName;
            item.quantity = 1;
            item.unitPrice = receipt.total;
            receipt.items.add(item);
        }

        receipt.categoryName = predictCategory(receipt.merchantName, receipt.rawText);
        return receipt;
    }

    // ---------------------------------------------------------------- baris

    private static List<String> buildRows(Text text) {
        List<Text.Line> lines = new ArrayList<>();
        for (Text.TextBlock b : text.getTextBlocks()) lines.addAll(b.getLines());

        boolean hasBoxes = !lines.isEmpty();
        for (Text.Line l : lines) if (l.getBoundingBox() == null) { hasBoxes = false; break; }

        List<String> rows = new ArrayList<>();
        if (!hasBoxes) {
            for (Text.Line l : lines) {
                String t = l.getText().trim();
                if (!t.isEmpty()) rows.add(t);
            }
            return rows;
        }

        Collections.sort(lines, (a, b) -> Integer.compare(a.getBoundingBox().centerY(), b.getBoundingBox().centerY()));
        List<List<Text.Line>> groups = new ArrayList<>();
        List<Text.Line> cur = null;
        double sumY = 0;
        for (Text.Line l : lines) {
            Rect r = l.getBoundingBox();
            if (cur != null && Math.abs(r.centerY() - sumY / cur.size()) <= Math.max(6, r.height() * 0.5)) {
                cur.add(l);
                sumY += r.centerY();
            } else {
                cur = new ArrayList<>();
                groups.add(cur);
                cur.add(l);
                sumY = r.centerY();
            }
        }
        for (List<Text.Line> g : groups) {
            Collections.sort(g, (a, b) -> Integer.compare(a.getBoundingBox().left, b.getBoundingBox().left));
            StringBuilder sb = new StringBuilder();
            for (Text.Line l : g) {
                String t = l.getText().trim();
                if (t.isEmpty()) continue;
                if (sb.length() > 0) sb.append(' ');
                sb.append(t);
            }
            if (sb.length() > 0) rows.add(sb.toString());
        }
        return rows;
    }

    // ------------------------------------------------------------- merchant

    private static int extractMerchantName(List<String> rows, ParsedReceipt receipt) {
        String[] known = {"Indomaret", "Alfamart", "Alfamidi", "Kopi Kenangan", "Starbucks", "Janji Jiwa",
                "Superindo", "Hypermart", "Transmart", "Lawson", "Circle K", "McDonald", "Mc Donald",
                "KFC", "Burger King", "Pizza Hut", "J.CO", "Chatime", "FamilyMart", "Pertamina"};
        int limit = Math.min(rows.size(), 8);
        for (int i = 0; i < limit; i++) {
            String lower = rows.get(i).toLowerCase(Locale.ROOT);
            for (String m : known) {
                if (lower.contains(m.toLowerCase(Locale.ROOT))) {
                    receipt.merchantName = rows.get(i).trim();
                    return i;
                }
            }
        }
        for (int i = 0; i < Math.min(rows.size(), 6); i++) {
            String row = rows.get(i).trim();
            String lower = row.toLowerCase(Locale.ROOT);
            if (letterCount(row) < 3 || findDate(Collections.singletonList(row)) != null
                    || lower.startsWith("jl") || lower.startsWith("jalan") || lower.contains("telp")
                    || lower.contains("npwp") || lower.contains("struk") || lower.contains("nota")
                    || lower.contains("kasir") || lower.contains("www.") || lower.contains("@")) {
                continue;
            }
            receipt.merchantName = row;
            return i;
        }
        receipt.merchantName = DEFAULT_MERCHANT;
        return -1;
    }

    private static int letterCount(String s) {
        int n = 0;
        for (int i = 0; i < s.length(); i++) if (Character.isLetter(s.charAt(i))) n++;
        return n;
    }

    // ----------------------------------------------------------- tanggal/jam

    private static void extractDateTime(List<String> rows, ParsedReceipt receipt) {
        LocalDate date = findDate(rows);
        if (date == null) date = LocalDate.now();

        receipt.transactionDate = date.toString();
        String display = date.getDayOfMonth() + " " + MONTHS_ID[date.getMonthValue() - 1] + " " + date.getYear();

        String time = null;
        for (String r : rows) {
            Matcher m = TIME_COLON.matcher(r);
            if (m.find()) { time = pad(m.group(1)) + "." + m.group(2); break; }
        }
        if (time == null) {
            for (String r : rows) {
                if (findDate(Collections.singletonList(r)) == null) continue;
                Matcher m = TIME_DOT.matcher(r.replaceAll("(?<!\\d)\\d{1,2}[/\\-.]\\d{1,2}[/\\-.]\\d{2,4}", " "));
                if (m.find()) { time = pad(m.group(1)) + "." + m.group(2); break; }
            }
        }
        if (time == null) {
            java.util.Calendar c = java.util.Calendar.getInstance();
            time = String.format(Locale.US, "%02d.%02d", c.get(java.util.Calendar.HOUR_OF_DAY), c.get(java.util.Calendar.MINUTE));
        }
        receipt.formattedDateTime = display + ", " + time + " WIB";
    }

    private static String pad(String h) { return h.length() == 1 ? "0" + h : h; }

    private static LocalDate findDate(List<String> rows) {
        for (String r : rows) {
            Matcher m = DATE_ISO.matcher(r);
            while (m.find()) {
                LocalDate d = validDate(m.group(1), m.group(2), m.group(3));
                if (d != null) return d;
            }
            m = DATE_NUM.matcher(r);
            while (m.find()) {
                LocalDate d = validDate(m.group(3), m.group(2), m.group(1));
                if (d != null) return d;
            }
            m = DATE_TEXT.matcher(r);
            while (m.find()) {
                LocalDate d = validDate(m.group(3), String.valueOf(monthFromName(m.group(2))), m.group(1));
                if (d != null) return d;
            }
        }
        return null;
    }

    private static int monthFromName(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        switch (n) {
            case "jan": return 1;
            case "feb": return 2;
            case "mar": return 3;
            case "apr": return 4;
            case "mei": case "may": return 5;
            case "jun": return 6;
            case "jul": return 7;
            case "agu": case "aug": return 8;
            case "sep": return 9;
            case "okt": case "oct": return 10;
            case "nov": return 11;
            default: return 12;
        }
    }

    private static LocalDate validDate(String y, String mo, String d) {
        try {
            int year = Integer.parseInt(y);
            if (year < 100) year += 2000;
            LocalDate date = LocalDate.of(year, Integer.parseInt(mo), Integer.parseInt(d));
            LocalDate today = LocalDate.now();
            if (year < 2015 || date.isAfter(today.plusDays(1))) return null;
            return date;
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isDateOrTimeRow(String row) {
        return findDate(Collections.singletonList(row)) != null || TIME_COLON.matcher(row).find();
    }

    // ----------------------------------------------------------------- total

    private static void extractTotals(List<String> rows, ParsedReceipt receipt) {
        int totalPriority = 0;
        for (String row : rows) {
            double val = lastPrice(row);
            if (val <= 0) continue;
            String u = row.toUpperCase(Locale.ROOT);
            if (u.contains("KEMBALI") || u.contains("TUNAI") || u.contains("CASH") || u.contains("CHANGE")) continue;

            if (u.contains("SUBTOTAL") || u.contains("SUB TOTAL") || u.contains("SUB-TOTAL")) {
                receipt.subtotal = val;
            } else if (u.contains("DISKON") || u.contains("DISCOUNT") || u.contains("POTONGAN")
                    || u.contains("HEMAT") || u.contains("PROMO")) {
                if (val >= 100) receipt.discount += val;
            } else if (u.contains("PPN") || u.contains("PB1") || u.contains("PAJAK") || u.contains("TAX")
                    || u.contains("SERVICE") || u.contains("SVC")) {
                if (val >= 100) receipt.tax += val;
            } else if (u.contains("TOTAL") || u.contains("JUMLAH") || u.contains("TAGIHAN")) {
                if (u.contains("ITEM") || u.contains("QTY")) continue;
                int priority = u.contains("GRAND") ? 3
                        : (u.contains("BAYAR") || u.contains("TAGIHAN") || u.contains("BELANJA")) ? 2 : 1;
                if (priority >= totalPriority) {
                    totalPriority = priority;
                    receipt.total = val;
                }
            }
        }
    }

    // ----------------------------------------------------------------- item

    private static void extractItems(List<String> rows, ParsedReceipt receipt, int merchantIdx) {
        List<ReceiptItemEntity> items = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            if (i <= merchantIdx) continue;
            String row = rows.get(i);
            String u = row.toUpperCase(Locale.ROOT);

            boolean strongStop = u.contains("TOTAL") || u.contains("SUBTOTAL") || u.contains("SUB TOTAL");
            boolean weakStop = u.contains("BAYAR") || u.contains("TUNAI") || u.contains("KEMBALI")
                    || u.contains("DEBIT") || u.contains("KARTU") || u.contains("CASH") || u.contains("QRIS");
            if (strongStop && !items.isEmpty()) break;
            if (strongStop) continue;
            if (weakStop) {
                if (!items.isEmpty()) break;
                continue;
            }
            if (isDateOrTimeRow(row) || isHeaderRow(u)) continue;

            double price = lastPrice(row);
            if (price < 100) continue;

            double qty = 1;
            double unit = price;
            Matcher q = QTY_X_PRICE.matcher(row);
            if (q.find()) {
                double qn = Double.parseDouble(q.group(1));
                double up = toAmount(q.group(2));
                if (qn > 0 && up > 0 && Math.abs(qn * up - price) <= price * 0.02 + 1) {
                    qty = qn;
                    unit = up;
                } else if (qn > 0 && up > 0 && up == price) {
                    qty = qn;
                    unit = up;
                }
            }

            String name = cleanName(row);
            if (name.length() < 2 || letterCount(name) < 2 || isIgnoredWord(name)) continue;

            ReceiptItemEntity item = new ReceiptItemEntity();
            item.itemName = name;
            item.quantity = qty;
            item.unitPrice = unit;
            items.add(item);
        }
        receipt.items = items;
    }

    private static boolean isHeaderRow(String u) {
        return u.contains("TELP") || u.contains("NPWP") || u.contains("NO.") || u.contains("KASIR")
                || u.contains("JL.") || u.contains("JALAN") || u.contains("STRUK") || u.contains("NOTA")
                || u.contains("TANGGAL") || u.contains("TGL") || u.contains("WIB") || u.contains("MEMBER");
    }

    private static String cleanName(String row) {
        String name = row.replaceAll("(?i)(?<!\\d)(\\d{1,3})\\s*[xX@]\\s*", " ");
        name = name.replaceAll("(?i)(?:rp\\.?\\s*)?(?<![\\d.,])(" + AMT + ")(?!\\d)", " ");
        name = name.replaceAll("[\\s]+", " ").replaceAll("^[^\\p{L}]+|[^\\p{L})]+$", "").trim();
        return name;
    }

    private static boolean isIgnoredWord(String text) {
        String u = text.toUpperCase(Locale.ROOT);
        return u.contains("TERIMA KASIH") || u.contains("SELAMAT") || u.contains("DATANG")
                || u.contains("TUNAI") || u.contains("DEBIT") || u.contains("PPN") || u.contains("PAJAK");
    }

    // ----------------------------------------------------------------- angka

    /** Harga terakhir pada baris, atau -1. */
    static double lastPrice(String line) {
        Matcher m = PRICE.matcher(line);
        double last = -1;
        while (m.find()) {
            double v = toAmount(m.group(1));
            if (v > 0) last = v;
        }
        return last;
    }

    /** "15.000" -> 15000, "15,000" -> 15000, "1.500,50" -> 1500.5, "15,000.00" -> 15000. */
    static double toAmount(String s) {
        try {
            int lastDot = s.lastIndexOf('.');
            int lastComma = s.lastIndexOf(',');
            int sep = Math.max(lastDot, lastComma);
            if (sep < 0) return Double.parseDouble(s);
            int after = s.length() - sep - 1;
            char sc = s.charAt(sep);
            boolean otherPresent = sc == '.' ? lastComma >= 0 : lastDot >= 0;
            boolean repeated = s.indexOf(sc) != sep;
            if (after == 3 && !otherPresent || repeated || after > 2) {
                return Double.parseDouble(s.replace(".", "").replace(",", ""));
            }
            String intPart = s.substring(0, sep).replace(".", "").replace(",", "");
            return Double.parseDouble(intPart + "." + s.substring(sep + 1));
        } catch (Exception e) {
            return -1;
        }
    }

    // -------------------------------------------------------------- kategori

    private static String predictCategory(String merchant, String fullText) {
        String m = (merchant == null ? "" : merchant).toLowerCase(Locale.ROOT);
        for (String mm : MINIMARKETS) if (m.contains(mm)) return "Belanja";

        String merchantCat = matchCategory(m);
        if (merchantCat != null) return merchantCat;
        String textCat = matchCategory((fullText == null ? "" : fullText).toLowerCase(Locale.ROOT));
        return textCat != null ? textCat : "Belanja";
    }

    private static String matchCategory(String t) {
        if (hasAny(t, "kopi", "cafe", "coffee", "resto", "warung", "bakso", "nasi", "ayam", "burger",
                "pizza", "starbucks", "mcdonald", "kfc", "chatime", "janji jiwa", "j.co", "bakery", "dapur")) {
            return "Makanan";
        }
        if (hasAny(t, "spbu", "pertamina", "shell", "bensin", "pertalite", "pertamax", "grab", "gojek", "parkir")
                || Pattern.compile("\\btol\\b").matcher(t).find()) {
            return "Transportasi";
        }
        if (hasAny(t, "pln", "listrik", "pdam", "wifi", "indihome", "pulsa", "kuota", "token")) {
            return "Tagihan";
        }
        if (hasAny(t, "apotek", "farmasi", "klinik", "kimia farma", "guardian", "rumah sakit")) {
            return "Kesehatan";
        }
        return null;
    }

    private static boolean hasAny(String t, String... keys) {
        for (String k : keys) if (t.contains(k)) return true;
        return false;
    }

    private static void setDefaultDate(ParsedReceipt receipt) {
        LocalDate d = LocalDate.now();
        java.util.Calendar c = java.util.Calendar.getInstance();
        receipt.transactionDate = d.toString();
        receipt.formattedDateTime = d.getDayOfMonth() + " " + MONTHS_ID[d.getMonthValue() - 1] + " " + d.getYear()
                + String.format(Locale.US, ", %02d.%02d WIB", c.get(java.util.Calendar.HOUR_OF_DAY), c.get(java.util.Calendar.MINUTE));
    }
}
