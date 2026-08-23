import urllib.request
import urllib.error
import json

url = 'https://posbackend-production-4e83.up.railway.app/api/transactions'
data = {
    "ownerUsername": "akshay",
    "products": "mac",
    "soldQuantity": 3,
    "finalAmount": 300000.0,
    "paymentMethod": "Cash"
}

req = urllib.request.Request(url, data=json.dumps(data).encode('utf-8'), headers={'Content-Type': 'application/json'})

try:
    with urllib.request.urlopen(req) as response:
        print("Status Code:", response.getcode())
        print("Response:", response.read().decode('utf-8'))
except urllib.error.HTTPError as e:
    print("HTTP Error:", e.code)
    print("Response:", e.read().decode('utf-8'))
except Exception as e:
    print("Error:", e)
