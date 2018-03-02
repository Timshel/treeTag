# --- !Ups
CREATE TABLE article (
    uuid 					TEXT NOT NULL,
    url 					TEXT NOT NULL,
    title 				TEXT NOT NULL,
    description		TEXT,
    content 			TEXT,
    
    PRIMARY KEY (uuid)
);

# --- !Downs
DROP TABLE article;