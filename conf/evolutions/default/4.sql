# --- !Ups

ALTER TABLE public.predictions DROP CONSTRAINT predictions_image_id_fkey;
ALTER TABLE public.predictions
  ADD CONSTRAINT predictions_image_id_fkey
FOREIGN KEY (image_id) REFERENCES images (image_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE public.images
  ADD CONSTRAINT images_users_user_id_fk
FOREIGN KEY (owned_by) REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE;