ALTER TABLE jobs
    ADD COLUMN ai_root_cause TEXT,
    ADD COLUMN ai_suggestion TEXT,
    ADD COLUMN ai_severity VARCHAR(50),
    ADD COLUMN ai_decision BOOLEAN;
