# --- !Ups

CREATE TABLE images (
  image_id CHARACTER VARYING NOT NULL PRIMARY KEY,
  url CHARACTER VARYING NOT NULL,
  uploaded_at timestamp NOT NULL,
  owned_by character VARYING NOT NULL,
  classification_start timestamp,
  classification_duration BIGINT,
  FOREIGN KEY(owned_by) REFERENCES users(user_id)
);
# --- !Downs
DROP TABLE images;