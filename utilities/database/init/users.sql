-- Sample Users Data
-- 10 sample users (1 admin + 9 customers)
-- All passwords are 'password123' hashed with BCrypt

-- Admin User
INSERT INTO users (email, password, first_name, last_name, role, active, created_at, updated_at) VALUES
('admin@ecommerce.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Admin', 'User', 'ADMIN', true, NOW(), NOW());

-- Customer Users
INSERT INTO users (email, password, first_name, last_name, role, active, created_at, updated_at) VALUES
('john.doe@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'John', 'Doe', 'CUSTOMER', true, NOW(), NOW()),
('jane.smith@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Jane', 'Smith', 'CUSTOMER', true, NOW(), NOW()),
('mike.johnson@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Mike', 'Johnson', 'CUSTOMER', true, NOW(), NOW()),
('sarah.williams@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Sarah', 'Williams', 'CUSTOMER', true, NOW(), NOW()),
('david.brown@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'David', 'Brown', 'CUSTOMER', true, NOW(), NOW()),
('emily.davis@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Emily', 'Davis', 'CUSTOMER', true, NOW(), NOW()),
('chris.miller@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Chris', 'Miller', 'CUSTOMER', true, NOW(), NOW()),
('lisa.wilson@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Lisa', 'Wilson', 'CUSTOMER', true, NOW(), NOW()),
('robert.moore@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Robert', 'Moore', 'CUSTOMER', true, NOW(), NOW());

-- NOTE: All users have the password 'password123'
-- Use these credentials to login:
-- admin@ecommerce.com / password123
-- john.doe@example.com / password123
-- etc.
