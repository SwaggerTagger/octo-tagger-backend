package models

import java.sql.Timestamp
import java.util.UUID

/**
 * Created by jlzie on 26.04.2017.
 */
case class TaggingImage(imageId: UUID, url: String, uploadedAt: Timestamp, ownedBy: UUID, height: Int, width: Int, classificationStart: Option[Timestamp] = None, classificationDuration: Option[Long] = None)
