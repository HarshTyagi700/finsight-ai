def create_sample_transactions():
    """Generate sample CSV for testing"""
    transactions = [
        {"merchant": "Zomato", "amount": 450.50, "category": "Food", "paymentMethod": "Credit Card"},
        {"merchant": "Uber", "amount": 200.00, "category": "Transport", "paymentMethod": "Debit Card"},
        {"merchant": "Netflix", "amount": 499.00, "category": "Entertainment", "paymentMethod": "Digital Wallet"},
        {"merchant": "Amazon", "amount": 5000.00, "category": "Shopping", "paymentMethod": "Credit Card"},
        {"merchant": "Starbucks", "amount": 120.00, "category": "Food", "paymentMethod": "Credit Card"},
        {"merchant": "Swiggy", "amount": 350.00, "category": "Food", "paymentMethod": "Digital Wallet"},
        {"merchant": "Ola", "amount": 180.50, "category": "Transport", "paymentMethod": "Credit Card"},
        {"merchant": "Spotify", "amount": 129.00, "category": "Entertainment", "paymentMethod": "Credit Card"},
        {"merchant": "Flipkart", "amount": 3500.00, "category": "Shopping", "paymentMethod": "Debit Card"},
        {"merchant": "Delhivery", "amount": 50.00, "category": "Other", "paymentMethod": "Credit Card"},
    ]
    
    import csv
    with open('sample_transactions.csv', 'w', newline='') as f:
        writer = csv.DictWriter(f, fieldnames=['merchant', 'amount', 'category', 'paymentMethod'])
        writer.writeheader()
        writer.writerows(transactions)
    
    print("✅ Created sample_transactions.csv")

if __name__ == "__main__":
    create_sample_transactions()
