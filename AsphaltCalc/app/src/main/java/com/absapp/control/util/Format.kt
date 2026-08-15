package com.absapp.control.util

import java.util.Locale

/** Форматирует число с фиксированным числом знаков после запятой, с запятой как разделителем. */
fun fmt(v: Double, decimals: Int = 1): String {
    return String.format(Locale.US, "%.${decimals}f", v).replace('.', ',')
}

/** Форматирует число, убирая незначащие нули (удобно для значений по умолчанию в полях ввода). */
fun fmtTrim(v: Double, decimals: Int = 2): String {
    var s = String.format(Locale.US, "%.${decimals}f", v)
    if (s.contains('.')) {
        s = s.trimEnd('0').trimEnd('.')
    }
    return s.replace('.', ',')
}
