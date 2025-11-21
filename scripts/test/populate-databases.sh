#!/bin/bash

# Populate Databases with Mock Data
# This script loads sample data into the microservices databases

set -e

cd "$(dirname "$0")/../.."

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}  Populating Databases with Mock Data${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""

# Check if docker-compose is running
if ! docker ps | grep -q postgres-product; then
    echo -e "${RED}Error: Databases are not running${NC}"
    echo "Please start the services first with: ./scripts/quick-start.sh"
    exit 1
fi

echo -e "${GREEN}✓ Databases are running${NC}"
echo ""

# Wait for databases to be ready
echo -e "${BLUE}Waiting for databases to be fully ready...${NC}"
sleep 5

# Function to execute SQL script
execute_sql() {
    local container=$1
    local database=$2
    local script=$3
    local description=$4

    echo -e "${BLUE}Loading ${description}...${NC}"

    if docker exec -i "$container" psql -U postgres -d "$database" < "$script" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ ${description} loaded successfully${NC}"
        return 0
    else
        echo -e "${YELLOW}⚠ ${description} may already exist or had an error${NC}"
        return 1
    fi
}

# Populate Product Database
echo -e "${BLUE}1/2: Populating Product Database${NC}"
execute_sql "postgres-product" "productdb" "utilities/database/init-scripts/01-products.sql" "Product data"
echo ""

# Populate Inventory Database
echo -e "${BLUE}2/2: Populating Inventory Database${NC}"
execute_sql "postgres-inventory" "inventorydb" "utilities/database/init-scripts/02-inventory.sql" "Inventory data"
echo ""

# Verify data
echo -e "${BLUE}Verifying loaded data...${NC}"
echo ""

# Check products
PRODUCT_COUNT=$(docker exec postgres-product psql -U postgres -d productdb -t -c "SELECT COUNT(*) FROM products;" 2>/dev/null | tr -d ' ')
echo -e "Products: ${GREEN}${PRODUCT_COUNT}${NC} items"

# Check inventory
INVENTORY_COUNT=$(docker exec postgres-inventory psql -U postgres -d inventorydb -t -c "SELECT COUNT(*) FROM inventory;" 2>/dev/null | tr -d ' ')
echo -e "Inventory: ${GREEN}${INVENTORY_COUNT}${NC} items"

echo ""
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}  Mock Data Loaded Successfully!${NC}"
echo -e "${GREEN}=========================================${NC}"
echo ""
echo -e "${BLUE}Available test data:${NC}"
echo "  • $PRODUCT_COUNT products across 5 categories"
echo "  • $INVENTORY_COUNT inventory records with stock levels"
echo ""
echo -e "${BLUE}You can now:${NC}"
echo "  1. Browse products: curl http://localhost:8080/api/products"
echo "  2. Check inventory: curl http://localhost:8080/api/inventory/{productId}"
echo "  3. Run load tests: ./scripts/run-load-test.sh"
echo ""
echo -e "${BLUE}Categories:${NC}"
echo "  • Electronics"
echo "  • Fashion"
echo "  • Home & Garden"
echo "  • Sports"
echo "  • Books"
echo ""
