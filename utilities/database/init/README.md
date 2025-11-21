# Database Mock Data

This directory contains SQL scripts to populate the database with sample data for testing and development.

## Files

- **products.sql** - 50 diverse products across 5 categories
- **inventory.sql** - Inventory data for all products
- **users.sql** - 10 sample users (1 admin + 9 customers)

## How to Load Data

### Option 1: Using psql (PostgreSQL)

```bash
# Connect to the database
psql -h localhost -U postgres -d productdb

# Load products
\i /path/to/products.sql

# Connect to inventory database
psql -h localhost -U postgres -d inventorydb
\i /path/to/inventory.sql

# Connect to user database
psql -h localhost -U postgres -d userdb
\i /path/to/users.sql
```

### Option 2: Using Docker Exec

```bash
# For products
docker exec -i postgres-product psql -U postgres -d productdb < database/init/products.sql

# For inventory
docker exec -i postgres-inventory psql -U postgres -d inventorydb < database/init/inventory.sql

# For users
docker exec -i postgres-user psql -U postgres -d userdb < database/init/users.sql
```

### Option 3: Auto-load with Docker Compose

Copy the SQL files to the appropriate PostgreSQL container's init directory.

## Sample Credentials

All users have the password: `password123`

**Admin:**
- Email: admin@ecommerce.com
- Password: password123

**Customers:**
- john.doe@example.com / password123
- jane.smith@example.com / password123
- mike.johnson@example.com / password123
- etc.

## Product Categories

1. **Electronics** (10 products) - Laptops, phones, cameras, etc.
2. **Clothing** (10 products) - Shoes, jeans, jackets, etc.
3. **Books** (10 products) - Technical books, self-help, fiction
4. **Home & Kitchen** (10 products) - Appliances, smart home devices
5. **Sports & Outdoors** (10 products) - Fitness equipment, camping gear
