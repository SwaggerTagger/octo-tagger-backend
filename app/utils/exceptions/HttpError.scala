package utils.exceptions

import java.util.UUID

import play.api.mvc.Result

/**
 * Created by jlzie on 27.04.2017.
 */
case class HttpError(result: Result) extends Exception(result.body.toString)