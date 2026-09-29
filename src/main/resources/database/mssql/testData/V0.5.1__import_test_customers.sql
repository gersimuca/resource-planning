INSERT INTO customer (customer_id, name, email, phone, company, address_line, city, state, postal_code, country,
                       customerStatus, owner_id, notes)
VALUES (1, 'Northwind Traders', 'contact@northwindtraders.com', '+1-206-555-0110', 'Northwind Traders',
        '400 Pike St', 'Seattle', 'WA', '98101', 'USA', 'ACTIVE', 2, 'Long-standing account, quarterly reviews.'),
       (2, 'Bluepeak Analytics', 'hello@bluepeak.io', '+1-415-555-0182', 'Bluepeak Analytics', '55 Market St',
        'San Francisco', 'CA', '94105', 'USA', 'ACTIVE', 3, 'Expanding to a second office next year.'),
       (3, 'Harbor & Vine Co.', 'info@harborandvine.com', '+1-617-555-0134', 'Harbor & Vine Co.', '12 Dockside Ln',
        'Boston', 'MA', '02110', 'USA', 'PROSPECT', 2, 'Evaluating us against two competitors.'),
       (4, 'Solstice Robotics', 'sales@solsticerobotics.com', '+1-512-555-0199', 'Solstice Robotics',
        '900 Congress Ave', 'Austin', 'TX', '78701', 'USA', 'ACTIVE', 3, 'Converted from an inbound lead.'),
       (5, 'Fernwood Clinics', 'ops@fernwoodclinics.com', '+1-303-555-0147', 'Fernwood Clinics', '77 Aspen Way',
        'Denver', 'CO', '80202', 'USA', 'INACTIVE', 2, 'Paused rollout, revisit next fiscal year.'),
       (6, 'Cedar & Co. Logistics', 'contact@cedarco-logistics.com', '+1-312-555-0166', 'Cedar & Co. Logistics',
        '210 Wacker Dr', 'Chicago', 'IL', '60606', 'USA', 'PROSPECT', 3, 'Referred by Bluepeak Analytics.');
