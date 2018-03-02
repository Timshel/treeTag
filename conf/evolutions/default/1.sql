# --- !Ups
CREATE TABLE article (
    uuid 					TEXT NOT NULL,
    url 					TEXT NOT NULL,
    title 				TEXT NOT NULL,
    description		TEXT,
    content 			TEXT,
    created 			TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated 			TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    
    PRIMARY KEY (uuid)
);

# --- !Downs
DROP TABLE article;