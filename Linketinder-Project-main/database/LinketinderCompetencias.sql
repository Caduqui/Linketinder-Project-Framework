-- Banco isolado do MS Competências
-- Candidatos e vagas ficam no banco linketinder, então id_candidato e id_vaga não têm FK: quem garante a consistência é o MS Núcleo, que chama o MS Competências ao gravar ou apagar uma vaga ou um candidato.

CREATE TABLE "competencias"
(
    "id"   serial PRIMARY KEY,
    "nome" varchar(50) NOT NULL
);

CREATE UNIQUE INDEX "competencias_nome_sem_caixa" ON "competencias" (LOWER("nome"));

CREATE TABLE "candidato_competencia"
(
    "id"             serial PRIMARY KEY,
    "id_candidato"   int        NOT NULL,
    "id_competencia" int        NOT NULL REFERENCES "competencias" ("id"),
    "nivel"          varchar(8) NOT NULL DEFAULT 'BASICO'
        CHECK ("nivel" IN ('BASICO', 'MEDIO', 'AVANCADO')),
    UNIQUE ("id_candidato", "id_competencia")
);

CREATE TABLE "vaga_competencia"
(
    "id"             serial PRIMARY KEY,
    "id_vaga"        int NOT NULL,
    "id_competencia" int NOT NULL REFERENCES "competencias" ("id"),
    UNIQUE ("id_vaga", "id_competencia")
);

INSERT INTO competencias (nome)
VALUES ('Java'),
       ('Spring Framework'),
       ('SQL'),
       ('Angular'),
       ('Python'),
       ('UI/UX'),
       ('Figma'),
       ('Comunicação'),
       ('Soft Skills'),
       ('Groovy');

INSERT INTO candidato_competencia (id_candidato, id_competencia)
VALUES (1, 1),
       (1, 2),
       (1, 3);
INSERT INTO candidato_competencia (id_candidato, id_competencia)
VALUES (2, 1),
       (2, 4),
       (2, 3);
INSERT INTO candidato_competencia (id_candidato, id_competencia)
VALUES (3, 1),
       (3, 2),
       (3, 5);
INSERT INTO candidato_competencia (id_candidato, id_competencia)
VALUES (4, 5),
       (4, 3);
INSERT INTO candidato_competencia (id_candidato, id_competencia)
VALUES (5, 6),
       (5, 7);

INSERT INTO vaga_competencia (id_vaga, id_competencia)
VALUES (1, 1),
       (1, 4),
       (1, 3);
INSERT INTO vaga_competencia (id_vaga, id_competencia)
VALUES (2, 8),
       (2, 9);
INSERT INTO vaga_competencia (id_vaga, id_competencia)
VALUES (3, 1),
       (3, 2),
       (3, 3);
INSERT INTO vaga_competencia (id_vaga, id_competencia)
VALUES (4, 1),
       (4, 10),
       (4, 3);
INSERT INTO vaga_competencia (id_vaga, id_competencia)
VALUES (5, 6),
       (5, 7);
