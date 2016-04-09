package com.ilunin.disconsul.json

import com.ilunin.disconsul.Service

trait JsonParser {

  def parse(json: String): Seq[Service]

}
