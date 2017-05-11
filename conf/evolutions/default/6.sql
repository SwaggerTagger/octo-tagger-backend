# --- !Ups
CREATE TABLE event_stream_tokens
(
  token varchar PRIMARY KEY NOT NULL,
  user_id UUID NOT NULL,
  expiry TIMESTAMP NOT NULL,
  CONSTRAINT event_stream_tokens_users_user_id_fk FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE
);
# --- !Downs

DROP TABLE event_stream_tokens;