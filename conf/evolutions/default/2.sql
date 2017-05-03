# --- !Ups

CREATE TABLE images (
  image_id UUID NOT NULL PRIMARY KEY,
  url CHARACTER VARYING NOT NULL,
  uploaded_at timestamp NOT NULL,
  owned_by UUID NOT NULL,
  classification_start timestamp,
  classification_duration BIGINT,
  height INTEGER NOT NULL,
  width INTEGER NOT NULL
);
# --- !Downs
DROP TABLE images;