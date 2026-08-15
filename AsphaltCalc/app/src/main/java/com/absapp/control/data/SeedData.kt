package com.absapp.control.data

/**
 * Значения перенесены вручную с фотографии таблицы 2 (нормы расхода асфальтобетонной
 * смеси, СТБ 1033-2016 / ТУ BY 100019869.001-2011). Перед серьёзным использованием
 * сверьте их с оригиналом документа в разделе "Справочник" — фотография могла быть
 * прочитана неточно.
 */
object SeedData {
    val initialMaterials = listOf(
        MaterialEntity(name = "Плотная мелкозернистая А,Б,В (плотность 2,5–2,9 т/м³)", normPerCm = 25.2, normPer4cm = 100.4),
        MaterialEntity(name = "Плотная мелкозернистая А,Б,В (плотность 3,0 и более т/м³)", normPerCm = 26.6, normPer4cm = 106.0),
        MaterialEntity(name = "Плотная крупнозернистая А,Б (плотность 2,5–2,9 т/м³)", normPerCm = 25.0, normPer4cm = 99.6),
        MaterialEntity(name = "Плотная крупнозернистая А,Б (плотность 3,0 и более т/м³)", normPerCm = 26.6, normPer4cm = 106.0),
        MaterialEntity(name = "Плотная песчаная Г,Д (плотность 2,5–3,0 т/м³)", normPerCm = 23.6, normPer4cm = 94.2),
        MaterialEntity(name = "Пористая крупнозернистая (плотность 2,5–2,9 т/м³)", normPerCm = 24.6, normPer4cm = 98.5),
        MaterialEntity(name = "Пористая крупнозернистая (плотность 3,0 и более т/м³)", normPerCm = 25.6, normPer4cm = 102.6),
        MaterialEntity(name = "Пористая мелкозернистая (плотность 2,5–2,9 т/м³)", normPerCm = 25.0, normPer4cm = 99.8),
        MaterialEntity(name = "Пористая мелкозернистая (плотность 3,0 и более т/м³)", normPerCm = 26.0, normPer4cm = 103.8),
        MaterialEntity(name = "Высокопористая крупнозернистая (плотность 2,5–2,9 т/м³)", normPerCm = 24.4, normPer4cm = 97.4),
        MaterialEntity(name = "Высокопористая крупнозернистая (плотность 3,0 и более т/м³)", normPerCm = 25.4, normPer4cm = 101.4),
        MaterialEntity(name = "Высокопористая мелкозернистая (плотность 2,5–2,9 т/м³)", normPerCm = 24.6, normPer4cm = 98.2),
        MaterialEntity(name = "Высокопористая мелкозернистая (плотность 3,0 и более т/м³)", normPerCm = 25.6, normPer4cm = 102.2),
        MaterialEntity(name = "Пористая песчаная", normPerCm = 22.4, normPer4cm = 89.5),
        MaterialEntity(name = "ТУ BY: Плотная мелкозернистая тип С (плотность 2,5–2,9 т/м³)", normPerCm = 25.0, normPer4cm = 100.2),
        MaterialEntity(name = "ТУ BY: Плотная мелкозернистая тип С (плотность 3,0 и более т/м³)", normPerCm = 26.4, normPer4cm = 105.7),
        MaterialEntity(name = "ТУ BY: Мелко/среднезернистые АПДУ 1", normPerCm = 25.1, normPer4cm = 100.5),
        MaterialEntity(name = "ТУ BY: Мелко/среднезернистые АПДУ 2", normPerCm = 25.2, normPer4cm = 100.7),
        MaterialEntity(name = "ТУ BY: Мелко/среднезернистые АПДУ 3", normPerCm = 25.0, normPer4cm = 99.8),
        MaterialEntity(name = "ТУ BY: Мелко/среднезернистые марок I, II", normPerCm = 24.8, normPer4cm = 99.2),
        MaterialEntity(name = "ТУ BY: Мелко/среднезернистые марки III", normPerCm = 24.6, normPer4cm = 98.2)
    )
}
