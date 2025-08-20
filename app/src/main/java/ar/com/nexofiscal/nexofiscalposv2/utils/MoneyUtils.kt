package ar.com.nexofiscal.nexofiscalposv2.utils

import java.text.NumberFormat
import java.util.Locale

object MoneyUtils {
    fun format(amount: Double, locale: Locale = Locale.getDefault()): String {
        val nf = NumberFormat.getNumberInstance(locale)
        nf.minimumFractionDigits = 2
        nf.maximumFractionDigits = 2
        return nf.format(amount)
    }
}

