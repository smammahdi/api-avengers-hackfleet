-- Sample Inventory Data
-- Inventory for all 50 products

INSERT INTO inventory (product_id, available_quantity, reserved_quantity, created_at, updated_at) VALUES
-- Electronics (1-10)
(1, 25, 0, NOW(), NOW()),
(2, 50, 0, NOW(), NOW()),
(3, 40, 0, NOW(), NOW()),
(4, 30, 0, NOW(), NOW()),
(5, 45, 0, NOW(), NOW()),
(6, 60, 0, NOW(), NOW()),
(7, 55, 0, NOW(), NOW()),
(8, 20, 0, NOW(), NOW()),
(9, 35, 0, NOW(), NOW()),
(10, 15, 0, NOW(), NOW()),

-- Clothing (11-20)
(11, 100, 0, NOW(), NOW()),
(12, 150, 0, NOW(), NOW()),
(13, 80, 0, NOW(), NOW()),
(14, 40, 0, NOW(), NOW()),
(15, 120, 0, NOW(), NOW()),
(16, 90, 0, NOW(), NOW()),
(17, 70, 0, NOW(), NOW()),
(18, 85, 0, NOW(), NOW()),
(19, 50, 0, NOW(), NOW()),
(20, 65, 0, NOW(), NOW()),

-- Books (21-30)
(21, 200, 0, NOW(), NOW()),
(22, 180, 0, NOW(), NOW()),
(23, 150, 0, NOW(), NOW()),
(24, 250, 0, NOW(), NOW()),
(25, 220, 0, NOW(), NOW()),
(26, 190, 0, NOW(), NOW()),
(27, 210, 0, NOW(), NOW()),
(28, 170, 0, NOW(), NOW()),
(29, 200, 0, NOW(), NOW()),
(30, 185, 0, NOW(), NOW()),

-- Home & Kitchen (31-40)
(31, 30, 0, NOW(), NOW()),
(32, 50, 0, NOW(), NOW()),
(33, 45, 0, NOW(), NOW()),
(34, 25, 0, NOW(), NOW()),
(35, 40, 0, NOW(), NOW()),
(36, 20, 0, NOW(), NOW()),
(37, 35, 0, NOW(), NOW()),
(38, 55, 0, NOW(), NOW()),
(39, 30, 0, NOW(), NOW()),
(40, 15, 0, NOW(), NOW()),

-- Sports & Outdoors (41-50)
(41, 10, 0, NOW(), NOW()),
(42, 30, 0, NOW(), NOW()),
(43, 100, 0, NOW(), NOW()),
(44, 60, 0, NOW(), NOW()),
(45, 120, 0, NOW(), NOW()),
(46, 150, 0, NOW(), NOW()),
(47, 40, 0, NOW(), NOW()),
(48, 25, 0, NOW(), NOW()),
(49, 20, 0, NOW(), NOW()),
(50, 15, 0, NOW(), NOW());
