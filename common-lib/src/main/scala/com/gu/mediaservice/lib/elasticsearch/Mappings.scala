// TODO: Throw errors on invalid query parameters
package com.gu.mediaservice.lib.elasticsearch

import play.api.libs.json.Json

object Mappings {

  val nonAnalyzedString = Json.obj("type" -> "string", "index" -> "not_analyzed")
  val nonIndexedString  = Json.obj("type" -> "string", "index" -> "no")

  val snowballAnalysedString = Json.obj("type" -> "string", "analyzer" -> "snowball")
  val standardAnalysedString = Json.obj("type" -> "string", "analyzer" -> "standard")

  val dateFormat = Json.obj("type" -> "date")

  val metadataMapping = Json.obj(
    "properties" -> Json.obj(
      "description" -> snowballAnalysedString,
      "byline" -> standardAnalysedString,
      "title" -> snowballAnalysedString,
      "credit" -> nonAnalyzedString,
      "copyright" -> standardAnalysedString,
      "copyrightNotice" -> standardAnalysedString,
      "suppliersReference" -> standardAnalysedString,
      "source" -> standardAnalysedString,
      "specialInstructions" -> nonAnalyzedString,
      "keywords" -> Json.obj("type" -> "string", "index" -> "not_analyzed", "index_name" -> "keyword"),
      "city" -> standardAnalysedString,
      "country" -> standardAnalysedString
    )
  )

  val fileMetadataMapping = Json.obj(
    "properties" -> Json.obj(
      "iptc"    -> nonIndexedString,
      "exif"    -> nonIndexedString,
      "exifSub" -> nonIndexedString,
      "xmp"     -> nonIndexedString
    )
  )

  val userMetadataMapping = Json.obj(
    "properties" -> Json.obj(
      "labels"   -> Json.obj("type" -> "string", "index" -> "not_analyzed", "index_name" -> "label"),
      "archived" -> nonIndexedString
    )
  )

  val imageMapping: String =
    Json.stringify(Json.obj(
      "image" -> Json.obj(
        "properties" -> Json.obj(
          // TODO: add source and thumbnail?
          "metadata" -> metadataMapping,
          "fileMetadata" -> fileMetadataMapping,
          "userMetadata" -> userMetadataMapping,
          "uploadTime" -> dateFormat,
          "uploadedBy" -> nonAnalyzedString,
          "archived" -> Json.obj("type" -> "boolean", "analyzer" -> "standard")
        )
      )
    ))

}
