# --- !Ups

CREATE TABLE images (
  image_id CHARACTER VARYING NOT NULL PRIMARY KEY,
  url CHARACTER VARYING NOT NULL,
  uploadedAt timestamp NOT NULL,
  ownedBy character VARYING NOT NULL,
  classificationStart timestamp,
  classificationDuration timestamp,
  FOREIGN KEY(ownedBy) REFERENCES users(user_id)
);