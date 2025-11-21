import requests
import json
import time

BASE_URL = "http://localhost:8080/api"

def log(message):
    print(f"[TEST] {message}")

def test_flow():
    session = requests.Session()
    
    # 1. Register
    log("Registering user...")
    register_payload = {
        "email": f"test_{int(time.time())}@example.com",
        "password": "password123",
        "firstName": "Test",
        "lastName": "User"
    }
    try:
        resp = session.post(f"{BASE_URL}/users/register", json=register_payload)
        if resp.status_code != 200:
            log(f"Registration failed: {resp.status_code} {resp.text}")
            return
        data = resp.json()
        token = data.get("token")
        user_id = data.get("userId")
        log(f"Registered User ID: {user_id}")
    except Exception as e:
        log(f"Registration exception: {e}")
        return

    headers = {"Authorization": f"Bearer {token}"}

    # 2. Login (Optional since we have token, but good to test)
    log("Logging in...")
    login_payload = {
        "email": register_payload["email"],
        "password": "password123"
    }
    resp = session.post(f"{BASE_URL}/users/login", json=login_payload)
    if resp.status_code != 200:
        log(f"Login failed: {resp.status_code}")
    else:
        log("Login successful")

    # 3. Get Products
    log("Fetching products...")
    resp = session.get(f"{BASE_URL}/products", headers=headers)
    if resp.status_code != 200:
        log(f"Get products failed: {resp.status_code}")
        return
    products = resp.json()
    if not products:
        log("No products found")
        return
    product = products[0]
    product_id = product['id']
    log(f"Found product: {product['name']} (ID: {product_id})")

    # 4. Add to Cart
    log("Adding to cart...")
    cart_payload = {
        "productId": product_id,
        "productName": product['name'],
        "price": product['price'],
        "quantity": 1
    }
    resp = session.post(f"{BASE_URL}/cart/add", json=cart_payload, headers=headers)
    if resp.status_code != 200:
        log(f"Add to cart failed: {resp.status_code} {resp.text}")
    else:
        log("Added to cart")

    # 5. Place Order
    log("Placing order...")
    resp = session.post(f"{BASE_URL}/orders", headers=headers)
    if resp.status_code != 200:
        log(f"Place order failed: {resp.status_code} {resp.text}")
    else:
        order_data = resp.json()
        order_id = order_data.get('id')
        log(f"Order placed. ID: {order_id}")
        
        # 6. Check Order Status
        log("Checking order status...")
        # Wait a bit for saga to complete
        time.sleep(2)
        resp = session.get(f"{BASE_URL}/orders/{order_id}", headers=headers)
        if resp.status_code == 200:
            log(f"Order status: {resp.json().get('status')}")
        else:
            log(f"Get order failed: {resp.status_code}")

if __name__ == "__main__":
    try:
        test_flow()
    except Exception as e:
        log(f"Test failed with exception: {e}")
