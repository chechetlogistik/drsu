package com.absapp.control.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.widget.Toast

/**
 * Открывает текущую точку GPS в приложении Яндекс.Карты (если установлено),
 * иначе — в браузере на yandex.ru/maps. Не требует API-ключа и SDK: используется
 * обычный Intent, поэтому это работает "из коробки" сразу после установки APK.
 */
fun openInYandexMaps(context: Context, location: Location?) {
    if (location == null) {
        Toast.makeText(context, "Нет данных GPS", Toast.LENGTH_SHORT).show()
        return
    }
    val lat = location.latitude
    val lon = location.longitude

    val yandexUri = Uri.parse("yandexmaps://maps.yandex.ru/?whatshere%5Bpoint%5D=$lon,$lat&whatshere%5Bzoom%5D=17")
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, yandexUri))
        return
    } catch (e: ActivityNotFoundException) {
        // Яндекс.Карты не установлены — пробуем открыть в браузере
    }

    val webUri = Uri.parse("https://yandex.ru/maps/?whatshere%5Bpoint%5D=$lon,$lat&whatshere%5Bzoom%5D=17")
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Не удалось открыть карту", Toast.LENGTH_SHORT).show()
    }
}
