package models

import java.sql.Timestamp
import java.util.UUID

/**
 * Created by John on 11.05.2017.
 */
case class SSEToken(token: String, userId: UUID, expiry: Timestamp)