# API Documentation - FinSight AI

## Base URL
```
Local: http://localhost:5000/api
Production: https://finsight-api.example.com/api
```

---

## 📊 Transaction Endpoints

### 1. Create Transaction
**Endpoint**: `POST /transactions`

**Description**: Create a new financial transaction. Category can be auto-populated by Gemini if not provided.

**Request**:
```json
{
  "merchant": "Zomato",
  "amount": 450.50,
  "category": "Food",
  "paymentMethod": "Credit Card",
  "description": "Lunch order"
}
```

**Response** (201 Created):
```json
{
  "id": "507f1f77bcf86cd799439011",
  "merchant": "Zomato",
  "amount": 450.50,
  "category": "Food",
  "paymentMethod": "Credit Card",
  "timestamp": "2024-05-17T10:30:00",
  "status": "PENDING",
  "description": "Lunch order"
}
```

**Validation Rules**:
- `merchant`: Required, non-empty string
- `amount`: Required, must be > 0
- `category`: Optional, auto-categorized if missing
- `paymentMethod`: Required, non-empty string

---

### 2. Get All Transactions
**Endpoint**: `GET /transactions`

**Description**: Retrieve all transactions in the system.

**Response** (200 OK):
```json
[
  {
    "id": "507f1f77bcf86cd799439011",
    "merchant": "Zomato",
    "amount": 450.50,
    "category": "Food",
    "paymentMethod": "Credit Card",
    "timestamp": "2024-05-17T10:30:00",
    "status": "PROCESSED"
  },
  {
    "id": "507f1f77bcf86cd799439012",
    "merchant": "Uber",
    "amount": 200.00,
    "category": "Transport",
    "paymentMethod": "Digital Wallet",
    "timestamp": "2024-05-17T10:45:00",
    "status": "PROCESSED"
  }
]
```

---

### 3. Get Transaction by ID
**Endpoint**: `GET /transactions/{id}`

**Description**: Retrieve a specific transaction by ID.

**Path Parameters**:
- `id` (string): Transaction MongoDB ObjectID

**Response** (200 OK):
```json
{
  "id": "507f1f77bcf86cd799439011",
  "merchant": "Zomato",
  "amount": 450.50,
  "category": "Food",
  "paymentMethod": "Credit Card",
  "timestamp": "2024-05-17T10:30:00",
  "status": "PROCESSED"
}
```

**Error** (404 Not Found):
```json
{
  "errorCode": "NOT_FOUND",
  "message": "Transaction not found",
  "statusCode": 404,
  "timestamp": "2024-05-17T10:50:00",
  "path": "/api/transactions/invalid_id"
}
```

---

### 4. Get Transaction Summary
**Endpoint**: `GET /transactions/summary`

**Description**: Get aggregated transaction summary including total spending and category breakdown.

**Response** (200 OK):
```json
{
  "totalTransactions": 15,
  "totalSpending": 8750.50,
  "categoryBreakdown": {
    "Food": 2150.00,
    "Transport": 1500.00,
    "Entertainment": 1200.00,
    "Shopping": 3900.50
  },
  "generatedAt": "2024-05-17T10:50:00"
}
```

---

### 5. Upload Transactions from CSV
**Endpoint**: `POST /transactions/upload`

**Description**: Bulk upload transactions from CSV file.

**Request**:
- Content-Type: `multipart/form-data`
- File parameter: `file` (CSV file)

**CSV Format**:
```csv
merchant,amount,category,paymentMethod
Zomato,450.50,Food,Credit Card
Uber,200.00,Transport,Debit Card
Netflix,499.00,Entertainment,Digital Wallet
Amazon,5000.00,Shopping,Credit Card
```

**Response** (201 Created):
```json
{
  "message": "CSV uploaded successfully",
  "transactionCount": 4,
  "transactions": [
    {
      "id": "507f1f77bcf86cd799439011",
      "merchant": "Zomato",
      "amount": 450.50,
      "category": "Food",
      "paymentMethod": "Credit Card",
      "timestamp": "2024-05-17T10:30:00",
      "status": "PENDING"
    }
    // ... more transactions
  ]
}
```

**Error** (400 Bad Request):
```json
{
  "errorCode": "CSV_UPLOAD_ERROR",
  "message": "Failed to upload CSV: Invalid file format",
  "statusCode": 400,
  "timestamp": "2024-05-17T10:50:00",
  "path": "/api/transactions/upload"
}
```

---

## 🤖 AI Endpoints

### 1. Generate Spending Insights
**Endpoint**: `GET /ai/insights`

**Description**: Generate AI-powered spending insights and analysis using Gemini API.

**Response** (200 OK):
```json
{
  "type": "spending_insights",
  "message": "Your food spending increased by 25% this month compared to last month. Consider reducing restaurant visits and trying meal prep. You spent ₹2,150 on food out of ₹8,750 total (24.6%).",
  "timestamp": 1716177000
}
```

**Notes**:
- Analyzes transaction patterns
- Provides spending breakdowns
- Suggests budget optimization
- Uses Gemini AI for natural language generation

---

### 2. Detect Anomalies
**Endpoint**: `GET /ai/anomalies`

**Description**: Detect unusual transaction patterns and anomalies.

**Response** (200 OK):
```json
{
  "type": "anomaly_detection",
  "anomalies": [
    "Watch for impulse purchases over ₹5,000",
    "Monitor subscription services - you have 3 recurring subscriptions",
    "Shopping category has highest variance - consider setting a budget",
    "Weekend spending is 40% higher than weekday spending"
  ],
  "count": 4,
  "timestamp": 1716177000
}
```

**Anomaly Types**:
- High-value transactions (>₹5,000)
- Unusual spending patterns
- Subscription tracking
- Weekend vs weekday variance

---

### 3. Get Savings Recommendations
**Endpoint**: `GET /ai/recommendations`

**Description**: Generate personalized savings recommendations based on spending patterns.

**Response** (200 OK):
```json
{
  "type": "savings_recommendations",
  "message": "1. Consolidate subscriptions: You're paying for Netflix, Prime Video, and Spotify. Consider family plans to save ₹500/month.\n2. Reduce dining out: Set a weekly food budget of ₹1,500. Meal prep on Sundays.\n3. Transport optimization: Use ride-sharing loyalty programs to save 15% on commute costs.",
  "timestamp": 1716177000
}
```

**Recommendations Include**:
- Subscription consolidation
- Spending reduction tips
- Budget optimization
- Loyalty program suggestions

---

## ⚕️ Health Check

### Health Check Endpoint
**Endpoint**: `GET /health`

**Description**: Check if the service is running and healthy.

**Response** (200 OK):
```json
{
  "status": "UP",
  "application": "FinSight AI",
  "version": "1.0.0"
}
```

---

## ❌ Error Handling

### Standard Error Response Format
```json
{
  "errorCode": "ERROR_CODE",
  "message": "Human readable error message",
  "statusCode": 400,
  "timestamp": "2024-05-17T10:50:00",
  "path": "/api/transactions"
}
```

### Common HTTP Status Codes

| Code | Meaning | Example |
|------|---------|---------|
| 200 | Success | GET request successful |
| 201 | Created | Transaction created |
| 400 | Bad Request | Invalid input data |
| 401 | Unauthorized | Missing auth token |
| 404 | Not Found | Transaction not found |
| 500 | Server Error | Internal server error |

### Common Error Codes

| Error Code | HTTP Status | Description |
|-----------|-------------|-------------|
| VALIDATION_ERROR | 400 | Input validation failed |
| INVALID_AMOUNT | 400 | Amount is not positive |
| NOT_FOUND | 404 | Resource not found |
| CSV_UPLOAD_ERROR | 400 | CSV file upload failed |
| AI_ERROR | 500 | Gemini API error |
| ANOMALY_ERROR | 500 | Anomaly detection failed |
| RECOMMENDATION_ERROR | 500 | Recommendation generation failed |
| TRANSACTION_CREATION_ERROR | 500 | Transaction creation failed |
| INTERNAL_SERVER_ERROR | 500 | Unexpected server error |

---

## 🔐 Authentication (Future)

Currently, the API is open (no authentication). Future versions will include:

```bash
# Bearer Token Authentication
Authorization: Bearer <jwt_token>
```

---

## 📈 Rate Limiting (Future)

Planned rate limiting:
- 100 requests per minute for authenticated users
- 10 requests per minute for unauthenticated users
- Kafka-based rate limiter

---

## 📝 Example Workflows

### Workflow 1: Create and Analyze Transaction

```bash
# Step 1: Create a transaction
curl -X POST http://localhost:5000/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "merchant": "Zomato",
    "amount": 450.50,
    "category": "Food",
    "paymentMethod": "Credit Card"
  }'

# Step 2: Get summary
curl http://localhost:5000/api/transactions/summary

# Step 3: Get AI insights
curl http://localhost:5000/api/ai/insights

# Step 4: Detect anomalies
curl http://localhost:5000/api/ai/anomalies
```

### Workflow 2: Bulk Upload and Analyze

```bash
# Step 1: Upload CSV
curl -X POST -F "file=@transactions.csv" \
  http://localhost:5000/api/transactions/upload

# Step 2: Wait for Kafka processing (2-5 seconds)

# Step 3: Get recommendations
curl http://localhost:5000/api/ai/recommendations
```

---

## 🧪 Testing with Postman/Insomnia

### Import Collection
```json
{
  "info": {
    "name": "FinSight AI",
    "version": "1.0.0"
  },
  "item": [
    {
      "name": "Create Transaction",
      "request": {
        "method": "POST",
        "url": "{{baseUrl}}/transactions"
      }
    }
  ]
}
```

### Environment Variables
```
baseUrl = http://localhost:5000/api
```

---

## 📞 API Support

- Issues: GitHub Issues with `[API]` tag
- Documentation: This file
- Examples: See SETUP_GUIDE.md

---

**API Version**: 1.0.0  
**Last Updated**: 2024-05-17

