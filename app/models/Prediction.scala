package models

import java.util.UUID

case class Prediction(imageId: UUID, category: String, probability: Double, left: Int, top: Int, right: Int, bottom: Int, predictionId: Int = -1)