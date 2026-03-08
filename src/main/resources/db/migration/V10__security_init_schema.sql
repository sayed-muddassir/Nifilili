CREATE TABLE "users" (
    "id" BIGINT NOT NULL,
    "email" VARCHAR(255) NOT NULL,
    "username" VARCHAR(255) NOT NULL,
    "password" VARCHAR(255) NOT NULL,
    "name" VARCHAR(255) NOT NULL
);
ALTER TABLE
    "users" ADD PRIMARY KEY("id");
CREATE TABLE "roles" (
    "id" BIGINT NOT NULL,
    "name" VARCHAR(255) NOT NULL
);
ALTER TABLE
    "roles" ADD PRIMARY KEY("id");
CREATE TABLE "users_roles" (
    "user_id" BIGINT NOT NULL,
    "role_id" BIGINT NOT NULL
);
ALTER TABLE
    "users_roles" ADD PRIMARY KEY ("user_id", "role_id");
COMMENT
ON COLUMN
    "users_roles"."user_id" IS 'References users(id)';
COMMENT
ON COLUMN
    "users_roles"."role_id" IS 'References roles(id)';

INSERT INTO "users" VALUES
(1,'ramesh@gmail.com','ramesh','$2a$10$5PiyN0MsG0y886d8xWXtwuLXK0Y7zZwcN5xm82b4oDSVr7yF0O6em','ramesh'),
(2,'admin@gmail.com','admin','$2a$10$gqHrslMttQWSsDSVRTK1OehkkBiXsJ/a4z2OURU./dizwOQu5Lovu','admin');

INSERT INTO "roles" VALUES (1,'ROLE_ADMIN'),(2,'ROLE_USER');

INSERT INTO "users_roles" VALUES (2,1),(1,2);