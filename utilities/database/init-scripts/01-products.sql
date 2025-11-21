-- Mock Product Data for E-Commerce Platform
-- This script populates the product database with sample data

-- Categories:
-- Electronics, Fashion, Home & Garden, Sports, Books

INSERT INTO products (name, description, price, category, image_url, created_at, updated_at) VALUES
-- Electronics
('Laptop Pro 15"', 'High-performance laptop with 16GB RAM, 512GB SSD, and latest Intel processor', 1299.99, 'Electronics', 'https://via.placeholder.com/300x200?text=Laptop', NOW(), NOW()),
('Wireless Mouse', 'Ergonomic wireless mouse with precision tracking and long battery life', 29.99, 'Electronics', 'https://via.placeholder.com/300x200?text=Mouse', NOW(), NOW()),
('Mechanical Keyboard', 'RGB backlit mechanical keyboard with customizable keys', 89.99, 'Electronics', 'https://via.placeholder.com/300x200?text=Keyboard', NOW(), NOW()),
('4K Monitor 27"', 'Ultra HD 4K monitor with HDR support and slim bezels', 399.99, 'Electronics', 'https://via.placeholder.com/300x200?text=Monitor', NOW(), NOW()),
('Wireless Earbuds', 'Noise-canceling wireless earbuds with 24-hour battery life', 149.99, 'Electronics', 'https://via.placeholder.com/300x200?text=Earbuds', NOW(), NOW()),
('Smartphone X', 'Latest flagship smartphone with 5G, 128GB storage, and triple camera', 899.99, 'Electronics', 'https://via.placeholder.com/300x200?text=Smartphone', NOW(), NOW()),
('Tablet 10"', 'Lightweight tablet perfect for reading and browsing', 329.99, 'Electronics', 'https://via.placeholder.com/300x200?text=Tablet', NOW(), NOW()),
('USB-C Hub', '7-in-1 USB-C hub with HDMI, USB 3.0, and SD card reader', 39.99, 'Electronics', 'https://via.placeholder.com/300x200?text=USB-Hub', NOW(), NOW()),
('Webcam HD', '1080p HD webcam with auto-focus and built-in microphone', 59.99, 'Electronics', 'https://via.placeholder.com/300x200?text=Webcam', NOW(), NOW()),
('External SSD 1TB', 'Portable SSD with fast transfer speeds and rugged design', 119.99, 'Electronics', 'https://via.placeholder.com/300x200?text=SSD', NOW(), NOW()),

-- Fashion
('Cotton T-Shirt', 'Comfortable 100% cotton t-shirt available in multiple colors', 19.99, 'Fashion', 'https://via.placeholder.com/300x200?text=T-Shirt', NOW(), NOW()),
('Denim Jeans', 'Classic fit denim jeans with stretch fabric', 49.99, 'Fashion', 'https://via.placeholder.com/300x200?text=Jeans', NOW(), NOW()),
('Running Shoes', 'Lightweight running shoes with cushioned sole', 79.99, 'Fashion', 'https://via.placeholder.com/300x200?text=Shoes', NOW(), NOW()),
('Leather Wallet', 'Genuine leather wallet with RFID protection', 34.99, 'Fashion', 'https://via.placeholder.com/300x200?text=Wallet', NOW(), NOW()),
('Sunglasses', 'UV protection polarized sunglasses with stylish frame', 59.99, 'Fashion', 'https://via.placeholder.com/300x200?text=Sunglasses', NOW(), NOW()),
('Backpack', 'Durable backpack with laptop compartment and multiple pockets', 44.99, 'Fashion', 'https://via.placeholder.com/300x200?text=Backpack', NOW(), NOW()),
('Watch', 'Elegant wristwatch with leather strap', 129.99, 'Fashion', 'https://via.placeholder.com/300x200?text=Watch', NOW(), NOW()),
('Belt', 'Reversible leather belt with classic buckle', 24.99, 'Fashion', 'https://via.placeholder.com/300x200?text=Belt', NOW(), NOW()),
('Cap', 'Adjustable baseball cap with embroidered logo', 14.99, 'Fashion', 'https://via.placeholder.com/300x200?text=Cap', NOW(), NOW()),
('Scarf', 'Soft cashmere blend scarf perfect for winter', 29.99, 'Fashion', 'https://via.placeholder.com/300x200?text=Scarf', NOW(), NOW()),

-- Home & Garden
('Coffee Maker', 'Programmable coffee maker with thermal carafe', 79.99, 'Home & Garden', 'https://via.placeholder.com/300x200?text=Coffee-Maker', NOW(), NOW()),
('Air Purifier', 'HEPA filter air purifier for large rooms', 149.99, 'Home & Garden', 'https://via.placeholder.com/300x200?text=Air-Purifier', NOW(), NOW()),
('Desk Lamp', 'LED desk lamp with adjustable brightness and color temperature', 34.99, 'Home & Garden', 'https://via.placeholder.com/300x200?text=Desk-Lamp', NOW(), NOW()),
('Throw Pillow Set', 'Set of 4 decorative throw pillows with removable covers', 39.99, 'Home & Garden', 'https://via.placeholder.com/300x200?text=Pillows', NOW(), NOW()),
('Plant Pot Set', 'Ceramic plant pots with drainage holes, set of 3', 24.99, 'Home & Garden', 'https://via.placeholder.com/300x200?text=Plant-Pots', NOW(), NOW()),
('Wall Art Canvas', 'Modern abstract canvas wall art, 24x36 inches', 89.99, 'Home & Garden', 'https://via.placeholder.com/300x200?text=Wall-Art', NOW(), NOW()),
('Vacuum Cleaner', 'Bagless vacuum cleaner with HEPA filter', 129.99, 'Home & Garden', 'https://via.placeholder.com/300x200?text=Vacuum', NOW(), NOW()),
('Blender', '1000W blender with multiple speed settings and glass jar', 59.99, 'Home & Garden', 'https://via.placeholder.com/300x200?text=Blender', NOW(), NOW()),
('Toaster', '4-slice toaster with wide slots and bagel setting', 39.99, 'Home & Garden', 'https://via.placeholder.com/300x200?text=Toaster', NOW(), NOW()),
('Cookware Set', '10-piece non-stick cookware set with lids', 199.99, 'Home & Garden', 'https://via.placeholder.com/300x200?text=Cookware', NOW(), NOW()),

-- Sports
('Yoga Mat', 'Thick non-slip yoga mat with carrying strap', 29.99, 'Sports', 'https://via.placeholder.com/300x200?text=Yoga-Mat', NOW(), NOW()),
('Dumbbells Set', 'Adjustable dumbbells set, 5-25 lbs', 79.99, 'Sports', 'https://via.placeholder.com/300x200?text=Dumbbells', NOW(), NOW()),
('Resistance Bands', 'Set of 5 resistance bands with different strengths', 19.99, 'Sports', 'https://via.placeholder.com/300x200?text=Resistance-Bands', NOW(), NOW()),
('Jump Rope', 'Speed jump rope with adjustable length', 12.99, 'Sports', 'https://via.placeholder.com/300x200?text=Jump-Rope', NOW(), NOW()),
('Water Bottle', 'Insulated stainless steel water bottle, 32oz', 24.99, 'Sports', 'https://via.placeholder.com/300x200?text=Water-Bottle', NOW(), NOW()),
('Gym Bag', 'Spacious gym bag with shoe compartment', 34.99, 'Sports', 'https://via.placeholder.com/300x200?text=Gym-Bag', NOW(), NOW()),
('Tennis Racket', 'Lightweight tennis racket with grip', 89.99, 'Sports', 'https://via.placeholder.com/300x200?text=Tennis-Racket', NOW(), NOW()),
('Basketball', 'Official size basketball for indoor/outdoor use', 29.99, 'Sports', 'https://via.placeholder.com/300x200?text=Basketball', NOW(), NOW()),
('Bicycle Helmet', 'Safety certified bicycle helmet with adjustable fit', 44.99, 'Sports', 'https://via.placeholder.com/300x200?text=Helmet', NOW(), NOW()),
('Foam Roller', 'High-density foam roller for muscle recovery', 19.99, 'Sports', 'https://via.placeholder.com/300x200?text=Foam-Roller', NOW(), NOW()),

-- Books
('The Great Novel', 'Bestselling fiction novel by acclaimed author', 14.99, 'Books', 'https://via.placeholder.com/300x200?text=Novel', NOW(), NOW()),
('Cooking Made Easy', 'Comprehensive cookbook with 500+ recipes', 24.99, 'Books', 'https://via.placeholder.com/300x200?text=Cookbook', NOW(), NOW()),
('Learning Programming', 'Beginners guide to modern programming languages', 39.99, 'Books', 'https://via.placeholder.com/300x200?text=Programming-Book', NOW(), NOW()),
('History of Innovation', 'Fascinating look at technological breakthroughs', 19.99, 'Books', 'https://via.placeholder.com/300x200?text=History-Book', NOW(), NOW()),
('Mindfulness Guide', 'Practical guide to meditation and mindfulness', 16.99, 'Books', 'https://via.placeholder.com/300x200?text=Mindfulness', NOW(), NOW()),
('Science Encyclopedia', 'Illustrated encyclopedia of science for all ages', 29.99, 'Books', 'https://via.placeholder.com/300x200?text=Encyclopedia', NOW(), NOW()),
('Travel Adventures', 'Inspiring travel stories from around the world', 18.99, 'Books', 'https://via.placeholder.com/300x200?text=Travel-Book', NOW(), NOW()),
('Business Strategy', 'Essential strategies for business success', 34.99, 'Books', 'https://via.placeholder.com/300x200?text=Business-Book', NOW(), NOW()),
('Art & Design', 'Beautiful collection of modern art and design', 44.99, 'Books', 'https://via.placeholder.com/300x200?text=Art-Book', NOW(), NOW()),
('Self-Improvement', 'Practical tips for personal growth and development', 22.99, 'Books', 'https://via.placeholder.com/300x200?text=Self-Help', NOW(), NOW());

-- Show count
SELECT 'Inserted ' || COUNT(*) || ' products' as summary FROM products;
