INSERT INTO Grown_In_Plant (plant_id, species, living_temp, living_light, water_tank_id)
VALUES
 (101, 'Water Lily', 22.5, 100.0, 1),
 (102, 'Seaweed', 18.0, 80.0, 2),
 (103, 'Mangrove', 25.0, 70.0, 3),
 (104, 'Coral', 26.0, 90.0, 4),
 (105, 'Anubias', 24.0, 60.0, 5);


INSERT INTO VendorLogistics (id, name, address)
VALUES
 (5, 'AquaLife Supplies', '123 Ocean Drive, Marine City, 45678'),
 (6, 'WaterWorld Equipment', '789 Coral Blvd, Reef Town, 12345'),
 (7, 'Marine Essentials', '456 Habitat St, Salt Lake, 91011'),
 (8, 'Underwater Gadgets', '135 Reef Rd, Blue Sea, 11345'),
 (9, 'Oceanic Items', '8643 Sea Road, Blue Harbour, 14151');


INSERT INTO VendorReputation (name, vendor_market_rating)
VALUES
 ('AquaLife Supplies', 'High'),
 ('WaterWorld Equipment', 'Medium'),
 ('Marine Essentials', 'Low'),
 ('Underwater Gadgets', 'Medium'),
 ('Oceanic Items', 'High');


INSERT INTO Exhibit(id, Name, State, LastMaintenanceDate, Size, NoOfWaterTanks)
VALUES
(17, 'Octopus Exhibit', 'OPEN'),
(18, 'Shark Exhibit', 'OPEN'),
(19, 'Dolphin Exhibit', 'OPEN'),
(20, 'Turtle Exhibit', 'OPEN'),
(21, 'Squid Exhibit', 'OPEN');


INSERT INTO Staff (id, salary, name, datehired)
VALUES
 (100, 1000.50, 'Sam', '2023-10-15'),
 (101, 1000.50, 'Anna', '2023-10-15'),
 (102, 145000.80, 'Kevin', '2023-10-16'),
 (103, 145000.80, 'John', '2023-10-16'),
 (104, 145000.80, 'Mohammed', '2023-10-17'),
 (105, 145000.80, 'James', '2023-10-17'),
 (106, 145000.80, 'Wataru', '2023-10-18'),
 (107, 100000.80, 'Michael', '2022-10-05'), 
 (108, 1000.50, 'Kim', '2022-10-30'),
 (109, 1000.50, 'Danny', '2022-09-15'),
 (110, 100000.80, 'Rachel', '2022-09-16'),
 (111, 100000.80, 'Baam', '2021-08-16'),
 (112, 100000.80, 'Megumi', '2021-07-16'),
 (113, 100000.80, 'Oshimhen', '2021-04-16'),
 (114, 1000.50, 'Messi', '2021-04-16');


INSERT INTO WaterTankLogistics(id, Name, Volume, Temperature, LightingLevel, exhibit_id)
VALUES
(1, 'Shark Tank', 1000.45, 27.5, 'Medium', 18),
(2, 'Octopus Tank', 1500.45, 27.8, 'Medium', 17),
(3, 'Dolphin Tank', 1000.45, 28.0, 'Medium', 19),
(4, 'Squid Tank', 1000.45, 27.9, 'Medium', 21),
(5, 'Turtle Tank', 500.50, 27.8,'Low', 20);


INSERT INTO WaterTankpH(Temperature, pH)
VALUES
(27.5, 7.0),
(27.8, 6.8),
(28, 6.7),
(27.9, 6.7),
(27.8, 6.8);


INSERT INTO Custodian (id, exhibit_id)
VALUES
 (100, 17),
 (101, 18),
 (108, 19),
 (109, 20),
 (114, 21);


INSERT INTO Aquarist (id, diving_level, water_tank_id)
VALUES
 (107, 100.00, 1),
 (110, 100.00, 2),
 (111, 100.00, 3),
 (112, 100.00, 4),
 (113, 100.00, 5);

INSERT INTO Veterinarian (id)
VALUES
 (102),
 (103),
 (104),
 (105),
 (106);


INSERT INTO Animal(id, Name, Species, Age, LivingTemp, water_tank_id, veterinarian_id)
VALUES
(31, 'Great White Shark', 'Carcharodon carcharias', 6, 27.5, 1, 102),
(32, 'Common Octopus', 'Octopus vulgaris', 7, 27.5, 2, 102),
(33, 'Orca', 'Orcinus orca', 5, 27.5, 3, 103),
(34, 'Vampire Squid', 'Vampyroteuthis infernalis', 3, 27.5, 4, 104),
(35, 'Aldabra giant tortoise', 'Aldabrachelys gigantea', 1, 27.5, 5, 105);


INSERT INTO ItemQuantity (id, name, quantity) VALUES
(1, 'Algae Wafers', 50),
(2, 'Coral Supplement', 30),
(3, 'Water Conditioner', 20),
(4, 'Medicated Feed', 65),
(5, 'Plankton', 80),
(6, 'Aquarium Salt', 120),
(7, 'Water Test Kits', 45);


INSERT INTO ItemUnit (name, unit) VALUES
('Algae Wafers', 'Packets'),
('Coral Supplement', 'Bottles'),
('Water Conditioner', 'Bottles'),
('Medicated Feed', 'Packets'),
('Plankton', 'Packets'),
('Aquarium Salt', 'Boxes'),
('Water Test Kits', 'Kits');


INSERT INTO Equipment (item_id, function, weight, size, date_installed) VALUES
(1, 'Water Filtration', 50.00, 'LARGE', '2020-01-15'),
(2, 'Protein Skimmer', 8.00, 'MEDIUM', '2021-07-30'),
(3, 'Heater', 2.00, 'SMALL', '2022-02-11'),
(4, 'LED Lighting', 5.00, 'MEDIUM', '2019-08-24'),
(5, 'UV Sterilizer', 6.00, 'SMALL', '2021-04-05'),
(6, 'Oxygen Pump', 4.50, 'SMALL', '2023-03-29'),
(7, 'CO2 System', 10.00, 'MEDIUM', '2018-12-10');


INSERT INTO Food (item_id, exp_date, food_type) VALUES
(8, '2024-01-15', 'Frozen Shrimp'),
(9, '2024-03-22', 'Fish Flakes'),
(10, '2024-02-11', 'Bloodworms'),
(11, '2024-05-19', 'Pellets'),
(12, '2024-04-13', 'Krill'),
(13, '2024-07-07', 'Squid'),
(14, '2024-06-25', 'Mysis Shrimp');


INSERT INTO Inventory (id, location) VALUES
(1, 'Main Storage'),
(2, 'Food Prep Area'),
(3, 'Maintenance Storage'),
(4, 'Medical Bay'),
(5, 'Temporary Storage');


INSERT INTO Custodian_Clean_Exhibit_Table (exhibit_id, custodian_id)
VALUES
 (17, 100),
 (18, 101),
 (19, 108),
 (20, 109),
 (21, 114);


INSERT INTO Supply (ItemID, VendorID)
VALUES
 (124, 5),
 (156, 6),
 (167, 7),
 (111, 8),
 (210, 9);


INSERT INTO ShelfInInventory (shelf_number, inventory_id, is_full) VALUES
(1, 1, 'true'),
(2, 1, 'false'),
(3, 2, 'true'),
(4, 2, 'false'),
(5, 3, 'true');


INSERT INTO InStock (item_id, shelf_number, inventory_id, quantity) VALUES
(1, 1, 1, 10),
(2, 2, 1, 15),
(3, 3, 2, 20),
(4, 4, 2, 25),
(5, 5, 3, 30),
(6, 6, 3, 12),
(7, 7, 4, 18);

