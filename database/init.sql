-- HomeVault Database Initialization Script
-- Full schema including family auth, reminders, and notification log

-- ── Users ─────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    email       VARCHAR(255)  UNIQUE NOT NULL,
    password    VARCHAR(255)  NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ── Households ────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS households (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    description VARCHAR(500),
    owner_id    BIGINT REFERENCES users(id) ON DELETE CASCADE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ── Household membership junction ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS household_members (
    household_id BIGINT REFERENCES households(id) ON DELETE CASCADE,
    user_id      BIGINT REFERENCES users(id)      ON DELETE CASCADE,
    joined_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (household_id, user_id)
);

-- ── Items ─────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS items (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(200)    NOT NULL,
    description   VARCHAR(1000),
    category      VARCHAR(100),
    location      VARCHAR(100),
    quantity      INTEGER DEFAULT 1,
    price         DECIMAL(10,2),
    purchase_date DATE,
    expiry_date   DATE,
    brand         VARCHAR(50),
    model         VARCHAR(50),
    notes         VARCHAR(500),
    image_url     VARCHAR(500),
    owner_id      BIGINT REFERENCES users(id)      ON DELETE CASCADE,
    household_id  BIGINT REFERENCES households(id) ON DELETE SET NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ── Reminders ─────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS reminders (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(200)  NOT NULL,
    description     VARCHAR(1000),
    due_date        DATE,
    status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING', 'DONE')),
    created_by_id   BIGINT REFERENCES users(id)      ON DELETE CASCADE,
    household_id    BIGINT REFERENCES households(id) ON DELETE SET NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ── Notification log (audit trail of sent emails) ─────────────────────────────

CREATE TABLE IF NOT EXISTS notification_log (
    id          BIGSERIAL PRIMARY KEY,
    recipient   VARCHAR(255) NOT NULL,
    subject     VARCHAR(500) NOT NULL,
    type        VARCHAR(50)  NOT NULL,  -- 'EXPIRY' | 'REMINDER'
    sent_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    success     BOOLEAN NOT NULL DEFAULT TRUE,
    error_msg   TEXT
);

-- ── Indexes ───────────────────────────────────────────────────────────────────

CREATE INDEX IF NOT EXISTS idx_users_email            ON users(email);
CREATE INDEX IF NOT EXISTS idx_households_owner       ON households(owner_id);
CREATE INDEX IF NOT EXISTS idx_hm_household           ON household_members(household_id);
CREATE INDEX IF NOT EXISTS idx_hm_user                ON household_members(user_id);
CREATE INDEX IF NOT EXISTS idx_items_owner            ON items(owner_id);
CREATE INDEX IF NOT EXISTS idx_items_household        ON items(household_id);
CREATE INDEX IF NOT EXISTS idx_items_expiry           ON items(expiry_date);
CREATE INDEX IF NOT EXISTS idx_items_category         ON items(category);
CREATE INDEX IF NOT EXISTS idx_reminders_created_by   ON reminders(created_by_id);
CREATE INDEX IF NOT EXISTS idx_reminders_household    ON reminders(household_id);
CREATE INDEX IF NOT EXISTS idx_reminders_due_date     ON reminders(due_date);
CREATE INDEX IF NOT EXISTS idx_reminders_status       ON reminders(status);

-- ── Auto-update trigger ───────────────────────────────────────────────────────

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

DO $$ BEGIN
    CREATE TRIGGER update_users_updated_at
        BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TRIGGER update_households_updated_at
        BEFORE UPDATE ON households FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TRIGGER update_items_updated_at
        BEFORE UPDATE ON items FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TRIGGER update_reminders_updated_at
        BEFORE UPDATE ON reminders FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- ── Expiry view ───────────────────────────────────────────────────────────────

CREATE OR REPLACE VIEW items_view AS
SELECT
    i.*,
    u.name          AS owner_name,
    u.email         AS owner_email,
    h.name          AS household_name,
    CASE WHEN i.expiry_date IS NOT NULL AND i.expiry_date < CURRENT_DATE THEN true ELSE false END AS is_expired,
    CASE WHEN i.expiry_date IS NOT NULL AND i.expiry_date <= CURRENT_DATE + INTERVAL '7 days' THEN true ELSE false END AS is_expiring_soon
FROM items i
JOIN users u ON i.owner_id = u.id
LEFT JOIN households h ON i.household_id = h.id;

-- ── Sample data (development only) ───────────────────────────────────────────

-- Demo user  (password: "password123" — BCrypt hash)
INSERT INTO users (name, email, password) VALUES
    ('Demo User', 'demo@homevault.com',
     '$2a$10$dXJ3SW6G7P4PlH4hzP.VZOL8I/5W5rj5W4M9Vt9ZvXHNnJF8.w9rO')
ON CONFLICT (email) DO NOTHING;

-- Demo household
INSERT INTO households (name, description, owner_id)
SELECT 'Smith Family', 'Our family household', id
FROM users WHERE email = 'demo@homevault.com'
ON CONFLICT DO NOTHING;

-- Add demo user as member of their own household
INSERT INTO household_members (household_id, user_id)
SELECT h.id, u.id
FROM users u
JOIN households h ON h.owner_id = u.id
WHERE u.email = 'demo@homevault.com'
ON CONFLICT DO NOTHING;

-- Sample item
INSERT INTO items (name, description, category, location, quantity, expiry_date, owner_id, household_id)
SELECT 'Milk', '2% whole milk', 'Dairy', 'Refrigerator', 2,
       CURRENT_DATE + INTERVAL '5 days',
       u.id, h.id
FROM users u JOIN households h ON h.owner_id = u.id
WHERE u.email = 'demo@homevault.com'
ON CONFLICT DO NOTHING;

-- Sample reminder
INSERT INTO reminders (title, description, due_date, status, created_by_id, household_id)
SELECT 'Restock pantry', 'Low on pasta and olive oil', CURRENT_DATE + INTERVAL '3 days',
       'PENDING', u.id, h.id
FROM users u JOIN households h ON h.owner_id = u.id
WHERE u.email = 'demo@homevault.com'
ON CONFLICT DO NOTHING;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES    IN SCHEMA public TO homevault_user;
GRANT USAGE, SELECT                  ON ALL SEQUENCES IN SCHEMA public TO homevault_user;
