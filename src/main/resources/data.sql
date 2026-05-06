INSERT INTO customers (customer_id, name, email, membership_tier) VALUES
('C001', 'Alice Johnson', 'alice@example.com', 'GOLD'),
('C002', 'Bob Smith', 'bob@example.com', 'SILVER'),
('C003', 'Carol White', 'carol@example.com', 'BRONZE');

INSERT INTO transactions (transaction_id, customer_id, transaction_date, amount, description) VALUES
('T001', 'C001', DATEADD('DAY', 5, DATEADD('MONTH', -3, CURRENT_DATE())), 120.75, 'Electronics Purchase'),
('T002', 'C001', DATEADD('DAY', 15, DATEADD('MONTH', -3, CURRENT_DATE())), 75.50, 'Grocery Store'),
('T003', 'C001', DATEADD('DAY', -10, CURRENT_DATE()), 200.00, 'Fashion Retail'),
('T004', 'C002', DATEADD('DAY', 8, DATEADD('MONTH', -3, CURRENT_DATE())), 95.25, 'Restaurant'),
('T005', 'C002', DATEADD('DAY', -5, CURRENT_DATE()), 150.00, 'Home Goods'),
('T006', 'C003', DATEADD('DAY', 2, DATEADD('MONTH', -3, CURRENT_DATE())), 45.99, 'Books'),
('T007', 'C003', DATEADD('DAY', -1, CURRENT_DATE()), 125.50, 'Cosmetics');
