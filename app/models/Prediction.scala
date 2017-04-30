package models

import java.util.UUID

/**
 * Created by jlzie on 28.04.2017.
 */
case class Prediction(imageId: UUID, category: String, probability: Double, left: Int, top: Int, right: Int, bottom: Int, predictionId: Int = -1)