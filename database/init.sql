-- HomeVault Database Initialization Script
-- This script creates all necessary tables and indexes


-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create households table
CREATE TABLE IF NOT EXISTS households (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    owner_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create household_members junction table
CREATE TABLE IF NOT EXISTS household_members (
    household_id BIGINT REFERENCES households(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (household_id, user_id)
);

-- Create items table
CREATE TABLE IF NOT EXISTS items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    category VARCHAR(100),
    location VARCHAR(100),
    quantity INTEGER DEFAULT 1,
    price DECIMAL(10,2),
    purchase_date DATE,
    expiry_date DATE,
    brand VARCHAR(50),
    model VARCHAR(50),
    notes VARCHAR(500),
    image_url VARCHAR(500),
    owner_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    household_id BIGINT REFERENCES households(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes 
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

CREATE INDEX IF NOT EXISTS idx_households_owner ON households(owner_id);
CREATE INDEX IF NOT EXISTS idx_households_created_at ON households(created_at);

CREATE INDEX IF NOT EXISTS idx_household_members_household ON household_members(household_id);
CREATE INDEX IF NOT EXISTS idx_household_members_user ON household_members(user_id);

CREATE INDEX IF NOT EXISTS idx_items_owner ON items(owner_id);
CREATE INDEX IF NOT EXISTS idx_items_household ON items(household_id);
CREATE INDEX IF NOT EXISTS idx_items_category ON items(category);
CREATE INDEX IF NOT EXISTS idx_items_location ON items(location);
CREATE INDEX IF NOT EXISTS idx_items_expiry ON items(expiry_date);
CREATE INDEX IF NOT EXISTS idx_items_created_at ON items(created_at);

-- Create a function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers to automatically update updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_households_updated_at BEFORE UPDATE ON households
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_items_updated_at BEFORE UPDATE ON items
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert sample data (optional, for development)
INSERT INTO users (name, email, password) VALUES 
('Demo User', 'demo@homevault.com', '$2a$10$dXJ3SW6G7P4PlH4hzP.VZOL8I/5W5rj5W4M9Vt9ZvXHNnJF8.w9rO')
ON CONFLICT (email) DO NOTHING;

-- Insert sample household
INSERT INTO households (name, description, owner_id) 
SELECT 'Demo Household', 'Sample household for demo', id 
FROM users WHERE email = 'demo@homevault.com'
ON CONFLICT DO NOTHING;

-- Insert sample items
INSERT INTO items (name, description, category, location, quantity, owner_id, household_id)
SELECT 
    'Sample Item', 
    'This is a sample item for demo purposes', 
    'Electronics', 
    'Living Room', 
    1, 
    u.id, 
    h.id
FROM users u
JOIN households h ON h.owner_id = u.id
WHERE u.email = 'demo@homevault.com'
ON CONFLICT DO NOTHING;

-- Create a view for items with owner and household info (useful for API)
CREATE OR REPLACE VIEW items_view AS
SELECT 
    i.*,
    u.name as owner_name,
    u.email as owner_email,
    h.name as household_name,
    CASE 
        WHEN i.expiry_date IS NOT NULL AND i.expiry_date <= CURRENT_DATE THEN true
        ELSE false
    END as is_expired,
    CASE 
        WHEN i.expiry_date IS NOT NULL AND i.expiry_date <= CURRENT_DATE + INTERVAL '7 days' THEN true
        ELSE false
    END as is_expiring_soon
FROM items i
JOIN users u ON i.owner_id = u.id
LEFT JOIN households h ON i.household_id = h.id;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO homevault_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO homevault_user;