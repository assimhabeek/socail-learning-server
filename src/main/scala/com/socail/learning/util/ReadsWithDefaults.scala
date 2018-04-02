package com.socail.learning.util

import play.api.libs.json.Reads.of
import play.api.libs.json._

class ReadsWithDefaults[A](defaults: A)(implicit format: Format[A]) extends Reads[A] {

  private val mergeWithDefaults = {
    val defaultJson = Json.fromJson[JsObject](Json.toJson(defaults)).get
    __.json.update(of[JsObject] map { o => defaultJson ++ o })
  }

  def reads(json: JsValue): JsResult[A] = {
    val jsObject = json.transform(mergeWithDefaults).get
    Json.fromJson[A](jsObject)(format)
  }
}
