-- Mock Inventory Data for E-Commerce Platform
-- This script populates inventory levels for products

-- Product IDs 1-50 with varying stock levels
-- Some products have high stock, some low, some out of stock for testing

INSERT INTO inventory (product_id, quantity, reserved_quantity, created_at, updated_at) VALUES
-- Electronics (1-10) - Generally good stock
(1, 45, 0, NOW(), NOW()),  -- Laptop Pro 15"
(2, 150, 5, NOW(), NOW()), -- Wireless Mouse
(3, 89, 3, NOW(), NOW()),  -- Mechanical Keyboard
(4, 32, 2, NOW(), NOW()),  -- 4K Monitor 27"
(5, 200, 15, NOW(), NOW()), -- Wireless Earbuds
(6, 78, 8, NOW(), NOW()),  -- Smartphone X
(7, 120, 10, NOW(), NOW()), -- Tablet 10"
(8, 300, 20, NOW(), NOW()), -- USB-C Hub
(9, 95, 5, NOW(), NOW()),  -- Webcam HD
(10, 150, 12, NOW(), NOW()), -- External SSD 1TB

-- Fashion (11-20) - Mixed stock levels
(11, 500, 25, NOW(), NOW()), -- Cotton T-Shirt (high stock)
(12, 250, 15, NOW(), NOW()), -- Denim Jeans
(13, 180, 10, NOW(), NOW()), -- Running Shoes
(14, 400, 30, NOW(), NOW()), -- Leather Wallet
(15, 220, 8, NOW(), NOW()),  -- Sunglasses
(16, 150, 12, NOW(), NOW()), -- Backpack
(17, 75, 5, NOW(), NOW()),   -- Watch
(18, 320, 18, NOW(), NOW()), -- Belt
(19, 450, 35, NOW(), NOW()), -- Cap (high stock)
(20, 185, 10, NOW(), NOW()), -- Scarf

-- Home & Garden (21-30) - Some low stock items
(21, 95, 8, NOW(), NOW()),   -- Coffee Maker
(22, 45, 3, NOW(), NOW()),   -- Air Purifier (low stock)
(23, 180, 12, NOW(), NOW()), -- Desk Lamp
(24, 250, 20, NOW(), NOW()), -- Throw Pillow Set
(25, 300, 25, NOW(), NOW()), -- Plant Pot Set
(26, 65, 5, NOW(), NOW()),   -- Wall Art Canvas
(27, 55, 4, NOW(), NOW()),   -- Vacuum Cleaner (low stock)
(28, 110, 8, NOW(), NOW()),  -- Blender
(29, 140, 10, NOW(), NOW()), -- Toaster
(30, 80, 6, NOW(), NOW()),   -- Cookware Set

-- Sports (31-40) - Varied stock
(31, 350, 30, NOW(), NOW()), -- Yoga Mat (high stock)
(32, 120, 8, NOW(), NOW()),  -- Dumbbells Set
(33, 400, 35, NOW(), NOW()), -- Resistance Bands (high stock)
(34, 500, 40, NOW(), NOW()), -- Jump Rope (very high stock)
(35, 280, 22, NOW(), NOW()), -- Water Bottle
(36, 195, 15, NOW(), NOW()), -- Gym Bag
(37, 85, 6, NOW(), NOW()),   -- Tennis Racket
(38, 175, 12, NOW(), NOW()), -- Basketball
(39, 145, 10, NOW(), NOW()), -- Bicycle Helmet
(40, 260, 20, NOW(), NOW()), -- Foam Roller

-- Books (41-50) - Generally good stock
(41, 320, 25, NOW(), NOW()), -- The Great Novel
(42, 150, 12, NOW(), NOW()), -- Cooking Made Easy
(43, 200, 15, NOW(), NOW()), -- Learning Programming
(44, 180, 14, NOW(), NOW()), -- History of Innovation
(45, 240, 18, NOW(), NOW()), -- Mindfulness Guide
(46, 95, 7, NOW(), NOW()),   -- Science Encyclopedia
(47, 175, 13, NOW(), NOW()), -- Travel Adventures
(48, 130, 10, NOW(), NOW()), -- Business Strategy
(49, 85, 6, NOW(), NOW()),   -- Art & Design (low stock)
(50, 210, 16, NOW(), NOW()); -- Self-Improvement

-- Show statistics
SELECT
    'Inventory Summary:' as info,
    COUNT(*) as total_products,
    SUM(quantity) as total_quantity,
    SUM(reserved_quantity) as total_reserved,
    ROUND(AVG(quantity), 2) as avg_quantity_per_product
FROM inventory;

-- Show low stock items (less than 100 units)
SELECT 'Low Stock Items (< 100 units):' as info;
SELECT product_id, quantity, reserved_quantity, (quantity - reserved_quantity) as available
FROM inventory
WHERE quantity < 100
ORDER BY quantity ASC;
