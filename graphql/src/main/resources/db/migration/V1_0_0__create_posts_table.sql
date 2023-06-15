CREATE TABLE posts (
  id BIGSERIAL
  , title VARCHAR(255)
  , summary VARCHAR(255)
  , date TIMESTAMP
  , location VARCHAR(255)
  , image_src VARCHAR(255)
  , CONSTRAINT posts_pk PRIMARY KEY (id)
);