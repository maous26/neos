package com.neostream.app.domain

private data class Bucket(val id: String, val rx: Regex)
private val BUCKETS = listOf(
  Bucket("series_netflix", Regex("(?i)s[ée]ries.*netflix")),
  Bucket("series_prime", Regex("(?i)s[ée]ries.*prime[ _-]?video")),
  Bucket("series_disney", Regex("(?i)s[ée]ries.*disney\\s*\\+")),
  Bucket("series_hbo", Regex("(?i)s[ée]ries.*hbo\\s*max")),
  Bucket("series_apple", Regex("(?i)s[ée]ries.*apple\\s*tv\\+")),
  Bucket("series_paramount", Regex("(?i)s[ée]ries.*paramount\\+")),
  Bucket("series_canal", Regex("(?i)s[ée]ries.*canal\\+")),
  Bucket("series_arabic", Regex("(?i)s[ée]ries.*arab")),
  Bucket("series_turk", Regex("(?i)s[ée]ries.*turqu|turk")),
  Bucket("series_cartoon", Regex("(?i)s[ée]ries.*(dessins|cartoon)")),
  Bucket("series_adult_anim", Regex("(?i)anim[ée]e.*adulte|adult.*anime")),
  Bucket("series_doc", Regex("(?i)s[ée]ries.*document")),
  Bucket("films_fr", Regex("(?i)^fr:.*films|films.*fr")),
  Bucket("live_fr", Regex("(?i)france\\s*hd|^fr[: ]")),
  Bucket("radio", Regex("(?i)^radio$"))
)

fun classifyBucket(group: String?): String =
  BUCKETS.firstOrNull { it.rx.containsMatchIn(group.orEmpty()) }?.id ?: "other"

fun detectQuality(text: String): String? {
  val t = text.lowercase()
  return when {
    "4k" in t || "uhd" in t -> "uhd"
    "1080" in t || "fhd" in t -> "fhd"
    "720" in t || " hd" in t || t.endsWith(" hd") -> "hd"
    "hevc" in t || "h265" in t || "h.265" in t -> "hevc"
    else -> null
  }
}

fun detectCountry(title: String, group: String?): String? {
  val t = (title + " " + group.orEmpty()).uppercase()
  val tags = listOf("FR","AR","ES","IT","US","TR","UK","BE","CA","DE","CH","NL","RO","BR","SE","PK","RS","AL","IN","SG")
  return tags.firstOrNull { " $it " in " $t " }
}

fun detectKind(title: String, group: String?): String {
  val g = group.orEmpty().lowercase()
  return when {
    g.startsWith("radio") -> "radio"
    g.startsWith("séri") || g.startsWith("seri") -> "series"
    g.contains("movies") || g.contains("films") -> "movie"
    else -> "live"
  }
}
