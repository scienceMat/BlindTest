-- Création de la table des utilisateurs
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255), -- Nullable pour les utilisateurs sans mot de passe
    is_admin BOOLEAN DEFAULT FALSE,
    is_guest BOOLEAN DEFAULT FALSE, -- Marqueur pour les utilisateurs invités
    score INT DEFAULT 0,
    ready BOOLEAN DEFAULT FALSE,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Création de la table des musiques
CREATE TABLE music (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    artist VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    image VARCHAR(255) NOT NULL
);

-- Création de la table des sessions
CREATE TABLE session (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    current_music_index INT DEFAULT 0,
    status VARCHAR(255) DEFAULT 'waiting',
    session_code VARCHAR(10) NOT NULL, -- Ajout de la colonne session_code
    admin_id INT NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    question_start_time TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Table de liaison pour les utilisateurs dans les sessions
CREATE TABLE session_users (
    session_id INT NOT NULL,
    user_id INT NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (session_id, user_id),
    FOREIGN KEY (session_id) REFERENCES session(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Création de la table des réponses
CREATE TABLE answers (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    music_id INT NOT NULL,
    session_id INT NOT NULL,
    title_guess VARCHAR(255),
    artist_guess VARCHAR(255),
    is_title_correct BOOLEAN DEFAULT FALSE,
    is_artist_correct BOOLEAN DEFAULT FALSE,
    answer_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (music_id) REFERENCES music(id) ON DELETE CASCADE,
    FOREIGN KEY (session_id) REFERENCES session(id) ON DELETE CASCADE,
    UNIQUE (user_id, session_id, music_id) -- Assure qu'une réponse par utilisateur et par tour
);

-- Table de liaison pour les musiques dans les sessions
CREATE TABLE session_music (
    session_id INT NOT NULL,
    music_id INT NOT NULL,
    PRIMARY KEY (session_id, music_id),
    FOREIGN KEY (session_id) REFERENCES session(id) ON DELETE CASCADE,
    FOREIGN KEY (music_id) REFERENCES music(id) ON DELETE CASCADE
);

-- Création de la table des questions
CREATE TABLE questions (
    id SERIAL PRIMARY KEY,
    song_snippet VARCHAR(255) NOT NULL,
    correct_answer VARCHAR(255) NOT NULL
);

-- Création de la table des options
CREATE TABLE options (
    id SERIAL PRIMARY KEY,
    question_id INT NOT NULL,
    option_text VARCHAR(255) NOT NULL,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

-- Création de la table des scores (optionnel)
CREATE TABLE leaderboard (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    score INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);


CREATE INDEX idx_session_code ON session(session_code);


ALTER TABLE session ADD COLUMN current_round INT DEFAULT 1;
ALTER TABLE session ADD COLUMN round_active BOOLEAN DEFAULT false;
