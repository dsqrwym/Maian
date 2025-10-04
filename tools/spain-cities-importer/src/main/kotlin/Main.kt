package org.dsqrwym

import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.net.URL
import java.util.zip.ZipInputStream

fun main() {
    try {
        // 1. 下载 GeoNames ES.txt
        val geonamesUrl = "https://download.geonames.org/export/dump/ES.zip"
        val zipFile = File("ES.zip")
        val destDir = File("es_data")
        val sqlFile = File("src/main/resources/insert_cities.sql")

        if (!zipFile.exists()) {
            println("Downloading ES.zip ...")
            URL(geonamesUrl).openStream().use { input ->
                zipFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        // 2. 解压缩
        unzip(zipFile, destDir)

        val esFile = File(destDir, "ES.txt")
        if (!esFile.exists()) {
            println("ES.txt not found! Available files:")
            destDir.listFiles()?.forEach { println(it.name) }
            return
        }

        BufferedWriter(OutputStreamWriter(FileOutputStream(sqlFile), Charsets.UTF_8)).use { writer ->
            val seenCities = mutableSetOf<String>()
            esFile.bufferedReader(Charsets.UTF_8).useLines { lines ->
                var count = 0
                lines.forEach { line ->
                    val parts = line.split("\t")
                    if (parts[6] == "P" && (parts[14].toLongOrNull() ?: 0L) > 600L) {  // 只处理城市
                        for (i in 0 until parts.size) {
                            print("${i}->${parts[i]} || ")
                        }
                        println("----------------")
                        val name = parts[1].replace("'", "''")
                        val localName = parts[1].replace("'", "''")

                        if (name !in seenCities) {
                            val admin1CodeRaw = parts[11].trim()
                            if (admin1CodeRaw.isNotEmpty()) {
                                val provinceId = mapProvince(admin1CodeRaw)
                                if (provinceId != null) {
                                    writer.write("INSERT INTO cities (province_id, name, name_local) VALUES ($provinceId, '$name', '$localName');\n")
                                    seenCities.add(name)
                                } else {
                                    println("Unmapped admin1_code: '$admin1CodeRaw' for city '$name'")
                                }
                            }
                        }
                    }
                    count++
                }
                println("Lines: $count")
            }
        }


        println("SQL file generated: ${sqlFile.absolutePath}")
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// 映射西班牙的 admin1_code 到 provinces.id
// https://es.wikipedia.org/wiki/Anexo:Provincias_de_Espa%C3%B1a_por_c%C3%B3digo_postal <-信息来源
fun mapProvince(admin1Code: String): Int? {
    val provinceMap = mapOf(
        "VI" to 1,   // Álava
        "AB" to 2,   // Albacete
        "A" to 3,    // Alicante
        "AL" to 4,   // Almería
        "O" to 5,    // Asturias
        "AV" to 6,   // Ávila
        "BA" to 7,   // Badajoz
        "B" to 8,    // Barcelona
        "BU" to 9,   // Burgos
        "CC" to 10,  // Cáceres
        "CA" to 11,  // Cádiz
        "S" to 12,   // Cantabria
        "CS" to 13,  // Castellón
        "CE" to 14,  // Ceuta
        "CR" to 15,  // Ciudad Real
        "CO" to 16,  // Córdoba
        "CU" to 17,  // Cuenca
        "GE" to 18,  // Gerona (Girona)
        "GI" to 18,  // Girona（备用写法）
        "GR" to 19,  // Granada
        "GU" to 20,  // Guadalajara
        "SS" to 21,  // Guipúzcoa (Gipuzkoa)
        "H" to 22,   // Huelva
        "HU" to 23,  // Huesca
        "J" to 25,   // Jaén
        "C" to 26,   // La Coruña (A Coruña)
        "LO" to 27,  // La Rioja
        "GC" to 28,  // Las Palmas
        "LE" to 29,  // León
        "L" to 30,   // Lérida (Lleida)
        "LU" to 31,  // Lugo
        "M" to 32,   // Madrid
        "MA" to 33,  // Málaga
        "ML" to 34,  // Melilla
        "ME" to 34,  // Melilla (GeoNames admin1_code)
        "MU" to 35,  // Murcia
        "NA" to 36,  // Navarra
        "OR" to 37,  // Orense (Ourense)
        "OU" to 37,  // Ourense（备用写法）
        "P" to 38,   // Palencia
        "PO" to 39,  // Pontevedra
        "SA" to 40,  // Salamanca
        "TF" to 41,  // Santa Cruz de Tenerife
        "SG" to 42,  // Segovia
        "SE" to 43,  // Sevilla
        "SO" to 44,  // Soria
        "T" to 45,   // Tarragona
        "TE" to 46,  // Teruel
        "TO" to 47,  // Toledo
        "V" to 48,   // Valencia
        "VA" to 49,  // Valladolid
        "BI" to 50,  // Vizcaya (Bizkaia)
        "ZA" to 51,  // Zamora
        "Z" to 52,   // Zaragoza
        "PM" to 24,  // Islas Baleares (Illes Balears)
        "IB" to 24   // Islas Baleares (Illes Balears)
    )


    return provinceMap[admin1Code]
}


// 使用 Kotlin/Java 自己解压 ZIP
fun unzip(zipFile: File, destDir: File) {
    if (!destDir.exists()) destDir.mkdirs()
    ZipInputStream(FileInputStream(zipFile)).use { zip ->
        var entry = zip.nextEntry
        while (entry != null) {
            val outFile = File(destDir, entry.name)

            // 检查路径，防止目录穿越攻击
            val destCanonicalPath = destDir.canonicalPath
            val outCanonicalPath = outFile.canonicalPath
            if (!outCanonicalPath.startsWith(destCanonicalPath + File.separator)) {
                throw SecurityException("Blocked path traversal attempt: ${entry.name}")
            }

            if (entry.isDirectory) {
                outFile.mkdirs()
            } else {
                outFile.parentFile.mkdirs()
                FileOutputStream(outFile).use { out ->
                    zip.copyTo(out)
                }
            }

            zip.closeEntry()
            entry = zip.nextEntry
        }
    }
}