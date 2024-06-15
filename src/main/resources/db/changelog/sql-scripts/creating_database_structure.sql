CREATE SCHEMA IF NOT EXISTS posts_scheme;

CREATE TABLE IF NOT EXISTS posts_scheme.posts (
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	title CHARACTER VARYING NOT NULL,
    user_id UUID NOT NULL,
    description CHARACTER VARYING NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    modification_date TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS posts_scheme.photos (
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	link CHARACTER VARYING NOT NULL,
    name CHARACTER VARYING NOT NULL,
    post_id UUID NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    modification_date TIMESTAMP NOT NULL,
    FOREIGN KEY (post_id) REFERENCES posts_scheme.posts (id) ON DELETE CASCADE ON UPDATE CASCADE
);


----- Index creation section -----
CREATE INDEX index_fk_user_id ON posts_scheme.posts USING HASH(user_id);
CREATE INDEX index_fk_post_id ON posts_scheme.photos USING HASH(post_id);