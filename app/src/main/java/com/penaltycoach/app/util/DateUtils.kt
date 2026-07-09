package com.penaltycoach.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Small, defensive date/time helpers. Everything is string based (YYYY-MM-DD and
 * HH:mm) and every parse is guarded so invalid input can never crash the app.
 */
object DateUtils {

    private const val DATE_PATTERN = "yyyy-MM-dd"
    private const val TIME_PATTERN = "HH:mm"

    private fun dateFormat(): SimpleDateFormat =
        SimpleDateFormat(DATE_PATTERN, Locale.US).apply { isLenient = false }

    private fun timeFormat(): SimpleDateFormat =
        SimpleDateFormat(TIME_PATTERN, Locale.US).apply { isLenient = false }

    /** Today as YYYY-MM-DD in the device's local time zone. */
    fun today(): String = dateFormat().format(Date())

    /** Current time as HH:mm in the device's local time zone. */
    fun nowTime(): String = timeFormat().format(Date())

    /** ISO-ish timestamp for createdAt/updatedAt bookkeeping. */
    fun nowIso(): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())

    /** Returns [base] (or today) shifted by [days], formatted as YYYY-MM-DD. */
    fun datePlusDays(days: Int, base: String? = null): String {
        val cal = Calendar.getInstance()
        val parsed = base?.let { parseDateOrNull(it) }
        if (parsed != null) cal.time = parsed
        cal.add(Calendar.DAY_OF_YEAR, days)
        return dateFormat().format(cal.time)
    }

    /** The default Match Schedule window end: today + 9 days (10-day window). */
    fun defaultDateTo(): String = datePlusDays(9)

    fun parseDateOrNull(value: String?): Date? {
        if (value.isNullOrBlank()) return null
        return try {
            dateFormat().parse(value)
        } catch (e: Exception) {
            null
        }
    }

    /** True when [value] is empty (allowed) or a valid YYYY-MM-DD date. */
    fun isValidOrEmptyDate(value: String?): Boolean {
        if (value.isNullOrBlank()) return true
        return parseDateOrNull(value) != null
    }

    /** True when [from] <= [to]; empty values are treated as valid. */
    fun isRangeValid(from: String?, to: String?): Boolean {
        val f = parseDateOrNull(from)
        val t = parseDateOrNull(to)
        if (f == null || t == null) return true
        return !t.before(f)
    }

    /** Human friendly date like "Mon, 08 Jul 2026"; falls back to raw input. */
    fun displayDate(value: String?): String {
        val parsed = parseDateOrNull(value) ?: return value.orEmpty()
        return SimpleDateFormat("EEE, dd MMM yyyy", Locale.US).format(parsed)
    }

    /**
     * Convert a UTC ISO timestamp (e.g. "2026-07-08T18:30:00Z") into a local
     * YYYY-MM-DD / HH:mm pair. Returns empty strings if it cannot be parsed.
     */
    fun localDateTimeFromUtc(utc: String?): Pair<String, String> {
        if (utc.isNullOrBlank()) return "" to ""
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ssX",
            "yyyy-MM-dd'T'HH:mm'Z'",
            "yyyy-MM-dd'T'HH:mm:ss"
        )
        for (p in patterns) {
            try {
                val parser = SimpleDateFormat(p, Locale.US)
                parser.timeZone = TimeZone.getTimeZone("UTC")
                val parsed = parser.parse(utc) ?: continue
                val d = dateFormat().format(parsed)
                val t = timeFormat().format(parsed)
                return d to t
            } catch (_: Exception) {
                // try next pattern
            }
        }
        return "" to ""
    }

    /** True if [date] falls within the last 7 days (inclusive of today). */
    fun isWithinLastWeek(date: String?): Boolean {
        val parsed = parseDateOrNull(date) ?: return false
        val weekAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6) }.time
        val todayEnd = Calendar.getInstance().time
        return !parsed.before(truncate(weekAgo)) && !parsed.after(todayEnd)
    }

    private fun truncate(date: Date): Date {
        val c = Calendar.getInstance()
        c.time = date
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.time
    }
}
