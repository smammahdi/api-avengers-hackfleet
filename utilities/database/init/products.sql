-- Sample Products Data
-- Insert 50 diverse products across multiple categories

-- Electronics
INSERT INTO products (name, description, price, category, brand, image_url, active, created_at, updated_at) VALUES
('MacBook Pro 16"', 'Powerful laptop with M2 Pro chip, 16GB RAM, 512GB SSD', 2499.99, 'Electronics', 'Apple', 'https://via.placeholder.com/300x200?text=MacBook', true, NOW(), NOW()),
('iPhone 14 Pro', 'Latest iPhone with A16 Bionic chip, 6.1" display, 128GB', 999.99, 'Electronics', 'Apple', 'https://via.placeholder.com/300x200?text=iPhone', true, NOW(), NOW()),
('Samsung Galaxy S23', 'Flagship Android phone with 8GB RAM, 256GB storage', 899.99, 'Electronics', 'Samsung', 'https://via.placeholder.com/300x200?text=Galaxy', true, NOW(), NOW()),
('Dell XPS 15', 'Premium Windows laptop with Intel i7, 16GB RAM, 1TB SSD', 1799.99, 'Electronics', 'Dell', 'https://via.placeholder.com/300x200?text=Dell', true, NOW(), NOW()),
('iPad Air', 'Lightweight tablet with M1 chip, 10.9" display, 64GB', 599.99, 'Electronics', 'Apple', 'https://via.placeholder.com/300x200?text=iPad', true, NOW(), NOW()),
('Sony WH-1000XM5', 'Premium noise-canceling wireless headphones', 399.99, 'Electronics', 'Sony', 'https://via.placeholder.com/300x200?text=Sony+Headphones', true, NOW(), NOW()),
('AirPods Pro', 'Active noise cancellation, spatial audio', 249.99, 'Electronics', 'Apple', 'https://via.placeholder.com/300x200?text=AirPods', true, NOW(), NOW()),
('LG 27" 4K Monitor', 'Ultra HD IPS display, HDR support, USB-C', 449.99, 'Electronics', 'LG', 'https://via.placeholder.com/300x200?text=Monitor', true, NOW(), NOW()),
('Logitech MX Master 3', 'Advanced wireless mouse with ergonomic design', 99.99, 'Electronics', 'Logitech', 'https://via.placeholder.com/300x200?text=Mouse', true, NOW(), NOW()),
('Canon EOS R6', 'Full-frame mirrorless camera, 20.1MP, 4K video', 2499.99, 'Electronics', 'Canon', 'https://via.placeholder.com/300x200?text=Camera', true, NOW(), NOW());

-- Clothing
INSERT INTO products (name, description, price, category, brand, image_url, active, created_at, updated_at) VALUES
('Nike Air Max 270', 'Comfortable running shoes with Max Air cushioning', 149.99, 'Clothing', 'Nike', 'https://via.placeholder.com/300x200?text=Nike+Shoes', true, NOW(), NOW()),
('Levi''s 501 Jeans', 'Classic straight-fit denim jeans', 79.99, 'Clothing', 'Levi''s', 'https://via.placeholder.com/300x200?text=Jeans', true, NOW(), NOW()),
('Adidas Ultraboost', 'High-performance running shoes with Boost technology', 179.99, 'Clothing', 'Adidas', 'https://via.placeholder.com/300x200?text=Adidas', true, NOW(), NOW()),
('North Face Jacket', 'Waterproof winter jacket with insulation', 299.99, 'Clothing', 'The North Face', 'https://via.placeholder.com/300x200?text=Jacket', true, NOW(), NOW()),
('Polo Ralph Lauren Shirt', 'Classic fit cotton polo shirt', 89.99, 'Clothing', 'Ralph Lauren', 'https://via.placeholder.com/300x200?text=Polo', true, NOW(), NOW()),
('Under Armour Hoodie', 'Comfortable fleece hoodie for sports and casual wear', 59.99, 'Clothing', 'Under Armour', 'https://via.placeholder.com/300x200?text=Hoodie', true, NOW(), NOW()),
('Ray-Ban Aviator', 'Classic aviator sunglasses with UV protection', 179.99, 'Clothing', 'Ray-Ban', 'https://via.placeholder.com/300x200?text=Sunglasses', true, NOW(), NOW()),
('Casio G-Shock Watch', 'Durable digital watch with shock resistance', 129.99, 'Clothing', 'Casio', 'https://via.placeholder.com/300x200?text=Watch', true, NOW(), NOW()),
('Patagonia Fleece', 'Lightweight fleece jacket for outdoor activities', 139.99, 'Clothing', 'Patagonia', 'https://via.placeholder.com/300x200?text=Fleece', true, NOW(), NOW()),
('Columbia Hiking Boots', 'Waterproof hiking boots with ankle support', 119.99, 'Clothing', 'Columbia', 'https://via.placeholder.com/300x200?text=Boots', true, NOW(), NOW());

-- Books
INSERT INTO products (name, description, price, category, brand, image_url, active, created_at, updated_at) VALUES
('The Pragmatic Programmer', 'Essential guide for software developers', 49.99, 'Books', 'Addison-Wesley', 'https://via.placeholder.com/300x200?text=Book', true, NOW(), NOW()),
('Clean Code', 'A handbook of agile software craftsmanship', 44.99, 'Books', 'Prentice Hall', 'https://via.placeholder.com/300x200?text=Clean+Code', true, NOW(), NOW()),
('Design Patterns', 'Elements of reusable object-oriented software', 54.99, 'Books', 'Addison-Wesley', 'https://via.placeholder.com/300x200?text=Patterns', true, NOW(), NOW()),
('Atomic Habits', 'An easy & proven way to build good habits', 26.99, 'Books', 'Avery', 'https://via.placeholder.com/300x200?text=Habits', true, NOW(), NOW()),
('Sapiens', 'A brief history of humankind', 24.99, 'Books', 'Harper', 'https://via.placeholder.com/300x200?text=Sapiens', true, NOW(), NOW()),
('The Alchemist', 'A fable about following your dreams', 15.99, 'Books', 'HarperOne', 'https://via.placeholder.com/300x200?text=Alchemist', true, NOW(), NOW()),
('1984', 'Dystopian novel by George Orwell', 16.99, 'Books', 'Signet Classic', 'https://via.placeholder.com/300x200?text=1984', true, NOW(), NOW()),
('Think and Grow Rich', 'Classic guide to achieving success', 18.99, 'Books', 'Tarcher', 'https://via.placeholder.com/300x200?text=Rich', true, NOW(), NOW()),
('The 7 Habits', 'The 7 Habits of Highly Effective People', 22.99, 'Books', 'Free Press', 'https://via.placeholder.com/300x200?text=7+Habits', true, NOW(), NOW()),
('Start with Why', 'How great leaders inspire everyone to take action', 27.99, 'Books', 'Portfolio', 'https://via.placeholder.com/300x200?text=Why', true, NOW(), NOW());

-- Home & Kitchen
INSERT INTO products (name, description, price, category, brand, image_url, active, created_at, updated_at) VALUES
('Dyson V15 Vacuum', 'Cordless vacuum with laser dust detection', 649.99, 'Home', 'Dyson', 'https://via.placeholder.com/300x200?text=Vacuum', true, NOW(), NOW()),
('Instant Pot Duo', '7-in-1 electric pressure cooker, 6 quart', 89.99, 'Home', 'Instant Pot', 'https://via.placeholder.com/300x200?text=Instant+Pot', true, NOW(), NOW()),
('Ninja Air Fryer', 'Large capacity air fryer with 5 cooking functions', 129.99, 'Home', 'Ninja', 'https://via.placeholder.com/300x200?text=Air+Fryer', true, NOW(), NOW()),
('KitchenAid Stand Mixer', 'Professional 5-quart stand mixer', 379.99, 'Home', 'KitchenAid', 'https://via.placeholder.com/300x200?text=Mixer', true, NOW(), NOW()),
('Keurig K-Elite', 'Single-serve K-Cup coffee maker', 169.99, 'Home', 'Keurig', 'https://via.placeholder.com/300x200?text=Coffee', true, NOW(), NOW()),
('Roomba i7+', 'Robot vacuum with automatic dirt disposal', 799.99, 'Home', 'iRobot', 'https://via.placeholder.com/300x200?text=Roomba', true, NOW(), NOW()),
('Nest Thermostat', 'Smart learning thermostat', 249.99, 'Home', 'Google', 'https://via.placeholder.com/300x200?text=Nest', true, NOW(), NOW()),
('Ring Video Doorbell', 'Smart doorbell with HD video and motion detection', 99.99, 'Home', 'Ring', 'https://via.placeholder.com/300x200?text=Doorbell', true, NOW(), NOW()),
('Philips Hue Bulbs', 'Smart LED bulbs starter kit with hub', 199.99, 'Home', 'Philips', 'https://via.placeholder.com/300x200?text=Hue', true, NOW(), NOW()),
('All-Clad Cookware Set', '10-piece stainless steel cookware set', 699.99, 'Home', 'All-Clad', 'https://via.placeholder.com/300x200?text=Cookware', true, NOW(), NOW());

-- Sports & Outdoors
INSERT INTO products (name, description, price, category, brand, image_url, active, created_at, updated_at) VALUES
('Peloton Bike', 'Indoor exercise bike with live classes', 1495.00, 'Sports', 'Peloton', 'https://via.placeholder.com/300x200?text=Peloton', true, NOW(), NOW()),
('Bowflex Dumbbells', 'Adjustable dumbbells 5-52.5 lbs', 349.99, 'Sports', 'Bowflex', 'https://via.placeholder.com/300x200?text=Dumbbells', true, NOW(), NOW()),
('Yoga Mat Premium', 'Extra thick eco-friendly yoga mat', 39.99, 'Sports', 'Manduka', 'https://via.placeholder.com/300x200?text=Yoga+Mat', true, NOW(), NOW()),
('Fitbit Charge 5', 'Advanced fitness tracker with GPS', 179.99, 'Sports', 'Fitbit', 'https://via.placeholder.com/300x200?text=Fitbit', true, NOW(), NOW()),
('Hydro Flask 32oz', 'Insulated stainless steel water bottle', 44.99, 'Sports', 'Hydro Flask', 'https://via.placeholder.com/300x200?text=Hydro', true, NOW(), NOW()),
('Wilson Basketball', 'Official size basketball for indoor/outdoor', 29.99, 'Sports', 'Wilson', 'https://via.placeholder.com/300x200?text=Basketball', true, NOW(), NOW()),
('TRX Suspension Trainer', 'Full body resistance training system', 179.99, 'Sports', 'TRX', 'https://via.placeholder.com/300x200?text=TRX', true, NOW(), NOW()),
('Camping Tent 4-Person', 'Waterproof camping tent with easy setup', 149.99, 'Sports', 'Coleman', 'https://via.placeholder.com/300x200?text=Tent', true, NOW(), NOW()),
('Folding Bike', 'Lightweight folding bicycle for commuting', 399.99, 'Sports', 'Dahon', 'https://via.placeholder.com/300x200?text=Bike', true, NOW(), NOW()),
('Golf Club Set', 'Complete set of golf clubs with bag', 599.99, 'Sports', 'Callaway', 'https://via.placeholder.com/300x200?text=Golf', true, NOW(), NOW());
