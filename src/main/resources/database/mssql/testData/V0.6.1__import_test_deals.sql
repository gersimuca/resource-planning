INSERT INTO deal
(deal_id, customer_id, title, stage, amount, expected_close_date, owner_id, notes)
VALUES
    (1, 1, 'Northwind annual renewal', 'NEGOTIATION', 42000.00,
     DATEADD(DAY, 18, CAST(GETDATE() AS DATE)), 2,
     'Finalizing multi-year discount terms.'),

    (2, 2, 'Bluepeak second-office expansion', 'PROPOSAL', 27500.00,
     DATEADD(DAY, 30, CAST(GETDATE() AS DATE)), 3,
     'Proposal sent, awaiting procurement review.'),

    (3, 3, 'Harbor & Vine new business', 'QUALIFICATION', 18000.00,
     DATEADD(DAY, 45, CAST(GETDATE() AS DATE)), 2,
     'Still evaluating against two competitors.'),

    (4, 4, 'Solstice Robotics platform upgrade', 'CLOSED_WON', 30000.00,
     DATEADD(DAY, -5, CAST(GETDATE() AS DATE)), 3,
     'Closed after a successful pilot period.'),

    (5, 6, 'Cedar & Co. logistics pilot', 'PROSPECTING', 12000.00,
     DATEADD(DAY, 60, CAST(GETDATE() AS DATE)), 3,
     'Warm referral, first call being scheduled.');
