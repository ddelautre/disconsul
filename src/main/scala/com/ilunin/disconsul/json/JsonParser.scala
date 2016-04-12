package com.ilunin.disconsul.json

trait JsonParser {

  def parse(json: String): Seq[ConsulService]

}
