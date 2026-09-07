CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  email VARCHAR(191) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'patient',
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

SELECT * from HSDD.audit_logs;

SELECT id, username, role FROM users;

ALTER TABLE HSDD.symptoms 
ADD COLUMN description TEXT;

SELECT id, username, email, password_hash, role, active
FROM users
WHERE username = 'testuser1';

SELECT username, password_hash 
FROM users 
WHERE username = 'testuser1';

UPDATE users
SET password_hash = '<BCRYPT_HASH>'
WHERE username = 'admin';

SELECT * from HSDD.predictions p ;
SELECT * from HSDD.users u ;
SELECT * from HSDD.symptoms s ;


SELECT COUNT(*) FROM users WHERE active = 1;
UPDATE users
SET username = 'Dr.House'
WHERE username = 'dr.house';

UPDATE HSDD.notifications n
JOIN HSDD.predictions pr ON n.prediction_id = pr.id
JOIN HSDD.patients p ON pr.patient_id = p.patient_id
SET n.message = CONCAT(
    'Low confidence prediction for ',
    p.first_name, ' ', p.last_name,
    ' (ID ', p.patient_id, ')'
)
WHERE n.message NOT LIKE '%(%'

SELECT id, username, role, active 
FROM users 
WHERE id = 5;

ALTER TABLE symptoms
CHANGE COLUMN text description VARCHAR(255) NOT NULL;

ALTER TABLE symptoms
DROP COLUMN text;

DESCRIBE patients;

DESCRIBE symptoms;

SELECT * FROM patients LIMIT 5;

INSERT INTO notifications (user_id, prediction_id, message)
VALUES (1, 1, 'Test notification');

ALTER TABLE HSDD.predictions
ADD COLUMN reviewed TINYINT(1) NOT NULL DEFAULT 0;

ALTER TABLE HSDD.predictions
DROP COLUMN symptom_entity_id;



CREATE TABLE IF NOT EXISTS patients (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,            -- internal key
  user_id BIGINT NOT NULL UNIQUE,                  -- links to users.id
  patient_id VARCHAR(8) NOT NULL UNIQUE,           -- public-facing 8-digit ID
  first_name VARCHAR(80) NOT NULL,
  last_name VARCHAR(80) NOT NULL,
  date_of_birth DATE NULL,
  phone VARCHAR(32) NULL,
  CONSTRAINT fk_patient_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS audit_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  event_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actor VARCHAR(191) NULL,
  event_type VARCHAR(50) NOT NULL,
  details TEXT NULL,
  ip_address VARCHAR(45) NULL
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_audit_time ON audit_logs(event_time);
CREATE INDEX idx_audit_actor ON audit_logs(actor);
CREATE INDEX idx_audit_type ON audit_logs(event_type);

CREATE TABLE IF NOT EXISTS symptoms (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  text TEXT NOT NULL,
  tags JSON NULL,
  submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_sym_patient_time (patient_id, submitted_at),
  CONSTRAINT fk_sym_patient FOREIGN KEY (patient_id)
    REFERENCES patients(id) ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS predictions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  symptom_id BIGINT NOT NULL,
  label VARCHAR(100) NOT NULL,
  confidence DOUBLE NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_pred_patient_time (patient_id, created_at),
  CONSTRAINT fk_pred_patient FOREIGN KEY (patient_id)
    REFERENCES patients(id) ON DELETE CASCADE,
  CONSTRAINT fk_pred_symptom FOREIGN KEY (symptom_id)
    REFERENCES symptoms(id) ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;



INSERT INTO users (username, email, password_hash, role, active)
VALUES (
    'admin',
    'admin@carepath.ca',
    '<BCRYPT_HASH>',
    'admin',
    TRUE
);

ALTER TABLE predictions
ADD COLUMN doctor_id BIGINT NULL;

ALTER TABLE HSDD.symptoms
DROP FOREIGN KEY fk_sym_patient;

ALTER TABLE HSDD.predictions
DROP FOREIGN KEY fk_pred_patient;

ALTER TABLE HSDD.predictions
DROP FOREIGN KEY fk_pred_symptom;

ALTER TABLE HSDD.symptoms
MODIFY COLUMN patient_id VARCHAR(8) NOT NULL;

ALTER TABLE HSDD.symptoms
DROP INDEX idx_sym_patient_time,
ADD INDEX idx_sym_patient_time (patient_id, submitted_at);

ALTER TABLE HSDD.symptoms
ADD CONSTRAINT fk_sym_patient
  FOREIGN KEY (patient_id)
  REFERENCES HSDD.patients(patient_id)
  ON DELETE CASCADE;

SET FOREIGN_KEY_CHECKS = 1;
ALTER TABLE HSDD.predictions
MODIFY COLUMN patient_id VARCHAR(8) NOT NULL;

ALTER TABLE HSDD.predictions
DROP INDEX idx_pred_patient_time,
ADD INDEX idx_pred_patient_time (patient_id, created_at);

SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE HSDD.predictions
ADD CONSTRAINT fk_pred_patient
  FOREIGN KEY (patient_id)
  REFERENCES HSDD.patients(patient_id)
  ON DELETE CASCADE;

SET FOREIGN_KEY_CHECKS = 1;

SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE HSDD.predictions
ADD CONSTRAINT fk_pred_symptom
  FOREIGN KEY (symptom_id)
  REFERENCES HSDD.symptoms(id)
  ON DELETE CASCADE;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE IF NOT EXISTS HSDD.medical_history (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  patient_id   VARCHAR(8)   NOT NULL,   -- links to patients.patient_id
  title        VARCHAR(100) NOT NULL,   -- e.g., 'Flu detected'
  details      TEXT         NULL,       -- e.g., 'Allergic to penicillin...'
  diagnosed_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  INDEX idx_hist_patient_time (patient_id, diagnosed_at),

  CONSTRAINT fk_hist_patient
    FOREIGN KEY (patient_id)
    REFERENCES HSDD.patients(patient_id)
    ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

SELECT table_name 
FROM information_schema.tables
WHERE table_schema = 'HSDD'
ORDER BY table_name;

SHOW COLUMNS FROM HSDD.users;

SHOW COLUMNS FROM HSDD.patients;

SHOW COLUMNS FROM HSDD.audit_logs;

SHOW COLUMNS FROM HSDD.symptoms;

SHOW COLUMNS FROM HSDD.predictions;

SHOW COLUMNS FROM HSDD.medical_history;

SHOW INDEX FROM HSDD.users;
SHOW INDEX FROM HSDD.patients;
SHOW INDEX FROM HSDD.audit_logs;
SHOW INDEX FROM HSDD.symptoms;
SHOW INDEX FROM HSDD.predictions;
SHOW INDEX FROM HSDD.medical_history;

SELECT 
    table_name,
    constraint_name,
    referenced_table_name,
    update_rule,
    delete_rule
FROM information_schema.referential_constraints
WHERE constraint_schema = 'HSDD'
ORDER BY table_name;

SELECT table_name, table_collation
FROM information_schema.tables
WHERE table_schema = 'HSDD';


-- ================================
-- Doctor-related tables
-- ================================

-- 1) Annotations: doctor notes on predictions
CREATE TABLE IF NOT EXISTS annotations (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  prediction_id   BIGINT NOT NULL,
  doctor_id       BIGINT NOT NULL,
  notes           TEXT NOT NULL,
  corrected_label VARCHAR(100),
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_annotation_prediction
    FOREIGN KEY (prediction_id) REFERENCES predictions(id) ON DELETE CASCADE,
  CONSTRAINT fk_annotation_doctor
    FOREIGN KEY (doctor_id)     REFERENCES users(id)       ON DELETE CASCADE
);

-- 2) Issue reports: doctors flag incorrect predictions or problems
CREATE TABLE IF NOT EXISTS issue_reports (
  id                BIGINT PRIMARY KEY AUTO_INCREMENT,
  prediction_id     BIGINT NOT NULL,
  doctor_id         BIGINT NOT NULL,
  issue_description TEXT NOT NULL,
  correct_label     VARCHAR(100),
  created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_issue_prediction
    FOREIGN KEY (prediction_id) REFERENCES predictions(id) ON DELETE CASCADE,
  CONSTRAINT fk_issue_doctor
    FOREIGN KEY (doctor_id)     REFERENCES users(id)       ON DELETE CASCADE
);

-- 3) Notifications: low-confidence alerts for doctors
CREATE TABLE IF NOT EXISTS notifications (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id        BIGINT NOT NULL,
  prediction_id  BIGINT NOT NULL,
  message        VARCHAR(255) NOT NULL,
  read_flag      TINYINT(1) NOT NULL DEFAULT 0,
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_notification_user
    FOREIGN KEY (user_id)        REFERENCES users(id)       ON DELETE CASCADE,
  CONSTRAINT fk_notification_prediction
    FOREIGN KEY (prediction_id)  REFERENCES predictions(id) ON DELETE CASCADE
);

SHOW TABLES LIKE 'annotations';
SHOW TABLES LIKE 'issue_reports';
SHOW TABLES LIKE 'notifications';

DESCRIBE annotations;
DESCRIBE issue_reports;
DESCRIBE notifications;
