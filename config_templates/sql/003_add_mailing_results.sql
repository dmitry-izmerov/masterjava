CREATE TYPE mailing_result_type AS ENUM('SUCCESS', 'FAILED');

CREATE TABLE mailing_results
(
  id INTEGER PRIMARY KEY DEFAULT nextval('common_seq'),
  value mailing_result_type NOT NULL,
  "to" TEXT NOT NULL,
  cc TEXT NOT NULL,
  subject TEXT NOT NULL
);