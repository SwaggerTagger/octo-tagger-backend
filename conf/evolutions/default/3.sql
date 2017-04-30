# --- !Ups

CREATE TABLE predictions (
  prediction_id SERIAL PRIMARY KEY,
  image_id UUID NOT NULL,
  category CHARACTER VARYING NOT NULL,
  probability DOUBLE PRECISION NOT NULL,
  leftx INTEGER NOT NULL,
  topy INTEGER NOT NULL,
  rightx INTEGER NOT NULL,
  bottomy INTEGER NOT NULL,
  FOREIGN KEY (image_id) REFERENCES images(image_id)
);
# --- !Downs
DROP TABLE predictions;
