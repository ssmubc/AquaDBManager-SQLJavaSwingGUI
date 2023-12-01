CREATE TABLE Exhibit (
                         id INTEGER PRIMARY KEY,
                         exhibit_name VARCHAR(255) NOT NULL,
                         exhibit_status VARCHAR(255) NOT NULL,
                         CONSTRAINT status_check CHECK (exhibit_status IN ('OPEN', 'CLOSED', 'UNDER MAINTENANCE', 'COMING SOON'))
);
COMMIT;

INSERT INTO Exhibit VALUES (17, 'Octopus Exhibit', 'OPEN');
COMMIT;
INSERT INTO Exhibit VALUES (18, 'Shark Exhibit', 'OPEN');
COMMIT;
INSERT INTO Exhibit VALUES (19, 'Dolphin Exhibit', 'OPEN');
COMMIT;
INSERT INTO Exhibit VALUES (20, 'Turtle Exhibit', 'OPEN');
COMMIT;
INSERT INTO Exhibit VALUES (21, 'Squid Exhibit', 'OPEN');
COMMIT;



CREATE TABLE Staff (
                       id INTEGER PRIMARY KEY,
                       salary DECIMAL(10, 2) NOT NULL,
                       staff_name VARCHAR(255) NOT NULL,
                       datehired DATE NOT NULL
);
COMMIT;

INSERT INTO Staff VALUES (100, 1000.50, 'Sam', '2023-10-15');
COMMIT;
INSERT INTO Staff VALUES (101, 1000.50, 'Anna', '2023-10-15');
COMMIT;
INSERT INTO Staff VALUES (102, 145000.80, 'Kevin', '2023-10-16');
COMMIT;
INSERT INTO Staff VALUES (103, 145000.80, 'John', '2023-10-16');
COMMIT;
INSERT INTO Staff VALUES (104, 145000.80, 'Mohammed', '2023-10-17');
COMMIT;
INSERT INTO Staff VALUES (105, 145000.80, 'James', '2023-10-17');
COMMIT;
INSERT INTO Staff VALUES (106, 145000.80, 'Wataru', '2023-10-18');
COMMIT;
INSERT INTO Staff VALUES (107, 100000.80, 'Michael', '2022-10-05');
COMMIT;
INSERT INTO Staff VALUES (108, 1000.50, 'Kim', '2022-10-30');
COMMIT;
INSERT INTO Staff VALUES (109, 1000.50, 'Danny', '2022-09-15');
COMMIT;
INSERT INTO Staff VALUES (110, 100000.80, 'Rachel', '2022-09-16');
COMMIT;
INSERT INTO Staff VALUES (111, 100000.80, 'Baam', '2021-08-16');
COMMIT;
INSERT INTO Staff VALUES (112, 100000.80, 'Megumi', '2021-07-16');
COMMIT;
INSERT INTO Staff VALUES (113, 100000.80, 'Oshimhen', '2021-04-16');
COMMIT;
INSERT INTO Staff VALUES (114, 1000.50, 'Messi', '2021-04-16');
COMMIT;


CREATE TABLE Custodian (
                           id INTEGER PRIMARY KEY,
                           exhibit_id INTEGER,
                           FOREIGN KEY (exhibit_id) REFERENCES Exhibit(id) ON DELETE CASCADE,
                           FOREIGN KEY (id) REFERENCES Staff(id) ON DELETE CASCADE
);
COMMIT;

INSERT INTO Custodian VALUES (100, 17);
COMMIT;
INSERT INTO Custodian VALUES (101, 18);
COMMIT;
INSERT INTO Custodian VALUES (108, 19);
COMMIT;
INSERT INTO Custodian VALUES (109, 20);
COMMIT;
INSERT INTO Custodian VALUES (114, 21);
COMMIT;


CREATE TABLE Veterinarian (
                              id INTEGER PRIMARY KEY,
                              FOREIGN KEY (id) REFERENCES
                                  Staff(id) ON DELETE CASCADE
);
COMMIT;

INSERT INTO Veterinarian VALUES (102);
COMMIT;
INSERT INTO Veterinarian VALUES (103);
COMMIT;
INSERT INTO Veterinarian VALUES (104);
COMMIT;
INSERT INTO Veterinarian VALUES (105);
COMMIT;
INSERT INTO Veterinarian VALUES (106);
COMMIT;




CREATE TABLE WaterTankLogistics (
                                    id INTEGER PRIMARY KEY,
                                    water_tank_logistics_name VARCHAR(255) NOT NULL,
                                    Volume NUMBER NOT NULL,
                                    Temperature NUMBER NOT NULL,
                                    LightingLevel VARCHAR(255) NOT NULL,
                                    CONSTRAINT lighting_check CHECK (LightingLevel IN ('LOW', 'MEDIUM', 'HIGH')),
                                    exhibit_id INTEGER,
                                    FOREIGN KEY (exhibit_id) REFERENCES Exhibit(id) ON DELETE CASCADE
);
COMMIT;

-- ERRORS IN WATERTANKLOGISTICS INSERT statements (FIXED)
INSERT INTO WaterTankLogistics VALUES (1, 'Shark Tank', 1000.45, 27.5, 'MEDIUM', 18);
COMMIT;
INSERT INTO WaterTankLogistics VALUES (2, 'Octopus Tank', 1500.45, 27.8, 'MEDIUM', 17);
COMMIT;
INSERT INTO WaterTankLogistics VALUES (3, 'Dolphin Tank', 1000.45, 28.0, 'MEDIUM', 19);
COMMIT;
INSERT INTO WaterTankLogistics VALUES (4, 'Squid Tank', 1000.45, 27.9, 'MEDIUM', 21);
COMMIT;
INSERT INTO WaterTankLogistics VALUES (5, 'Turtle Tank', 500.50, 27.8,'LOW', 20);
COMMIT;


CREATE TABLE Aquarist (
                          id INTEGER PRIMARY KEY,
                          diving_level DECIMAL (10, 2) NOT NULL,
                          water_tank_id INTEGER,
                          FOREIGN KEY (id) REFERENCES Staff(id) ON DELETE CASCADE,
                          FOREIGN KEY (water_tank_id) REFERENCES WaterTankLogistics(id)
);
COMMIT;

INSERT INTO Aquarist VALUES (107, 100.00, 1);
COMMIT;
INSERT INTO Aquarist VALUES (110, 100.00, 2);
COMMIT;
INSERT INTO Aquarist VALUES (111, 100.00, 3);
COMMIT;
INSERT INTO Aquarist VALUES (112, 100.00, 4);
COMMIT;
INSERT INTO Aquarist VALUES (113, 100.00, 5);
COMMIT;


CREATE TABLE WaterTankpH (
                             Temperature DECIMAL (3, 1) PRIMARY KEY,
                             pH DECIMAL (2,1) NOT NULL
);
COMMIT;

INSERT INTO WaterTankpH(Temperature, pH) VALUES (27.5, 7.0);
COMMIT;
INSERT INTO WaterTankpH(Temperature, pH) VALUES (27.8, 6.8);
COMMIT;
INSERT INTO WaterTankpH(Temperature, pH) VALUES (28, 6.7);
COMMIT;
INSERT INTO WaterTankpH(Temperature, pH) VALUES (27.9, 6.7);
COMMIT;
INSERT INTO WaterTankpH(Temperature, pH) VALUES (27.6, 6.8);
COMMIT;


CREATE TABLE Animal (
                        id INTEGER PRIMARY KEY,
                        animal_name VARCHAR(255) NOT NULL,
                        Species VARCHAR(255) NOT NULL,
                        Age INTEGER NOT NULL,
                        LivingTemp DECIMAL (3, 1) NOT NULL,
                        water_tank_id INTEGER,
                        veterinarian_id INTEGER,
                        FOREIGN KEY (water_tank_id) REFERENCES WaterTankLogistics(id) ON DELETE CASCADE,
                        FOREIGN KEY (veterinarian_id) REFERENCES Veterinarian(id) ON DELETE CASCADE
);
COMMIT;
INSERT INTO Animal(id, animal_name, Species, Age, LivingTemp, water_tank_id, veterinarian_id)
VALUES (31, 'Great White Shark', 'Carcharodon carcharias', 6, 27.5, 1, 102);
COMMIT;
INSERT INTO Animal(id, animal_name, Species, Age, LivingTemp, water_tank_id, veterinarian_id)
VALUES (32, 'Common Octopus', 'Octopus vulgaris', 7, 27.5, 2, 102);
COMMIT;
INSERT INTO Animal(id, animal_name, Species, Age, LivingTemp, water_tank_id, veterinarian_id)
VALUES (33, 'Orca', 'Orcinus orca', 5, 27.5, 3, 103);
COMMIT;
INSERT INTO Animal(id, animal_name, Species, Age, LivingTemp, water_tank_id, veterinarian_id)
VALUES (34, 'Vampire Squid', 'Vampyroteuthis infernalis', 3, 27.5, 4, 104);
COMMIT;
INSERT INTO Animal(id, animal_name, Species, Age, LivingTemp, water_tank_id, veterinarian_id)
VALUES (35, 'Aldabra giant tortoise', 'Aldabrachelys gigantea', 1, 27.5, 5, 105);
COMMIT;
INSERT INTO Animal(id, animal_name, Species, Age, LivingTemp, water_tank_id, veterinarian_id)
VALUES (36, 'wrasse', 'Humphead wrasse', 1, 27.8, 5, 105);
COMMIT;
INSERT INTO Animal(id, animal_name, Species, Age, LivingTemp, water_tank_id, veterinarian_id)
VALUES (37, 'Phinius', 'Fin Whale', 4, 27.8, 5, 105);
COMMIT;
INSERT INTO Animal(id, animal_name, Species, Age, LivingTemp, water_tank_id, veterinarian_id)
VALUES (38, 'Benjamin', 'Beluga Whale', 3, 27.7, 5, 105);
COMMIT;


CREATE TABLE Inventory (
                           id INTEGER PRIMARY KEY,
                           location VARCHAR(255) NOT NULL
);
COMMIT;
INSERT INTO Inventory (id, location) VALUES (1, 'Main Storage');
COMMIT;
INSERT INTO Inventory (id, location) VALUES (2, 'Food Prep Area');
COMMIT;
INSERT INTO Inventory (id, location) VALUES (3, 'Maintenance Storage');
COMMIT;
INSERT INTO Inventory (id, location) VALUES (4, 'Medical Bay');
COMMIT;
INSERT INTO Inventory (id, location) VALUES (5, 'Temporary Storage');
COMMIT;


CREATE TABLE ShelfInInventory (
                                  shelf_number INTEGER,
                                  inventory_id INTEGER,
                                  is_full VARCHAR(255) NOT NULL,
                                  CONSTRAINT check_full CHECK (is_full IN ('true', 'false')),
                                  PRIMARY KEY (shelf_number, inventory_id),
                                  FOREIGN KEY (inventory_id) REFERENCES Inventory(id) ON DELETE CASCADE
);
COMMIT;
INSERT INTO ShelfInInventory (shelf_number, inventory_id, is_full) VALUES (1, 1, 'true');
COMMIT;
INSERT INTO ShelfInInventory (shelf_number, inventory_id, is_full) VALUES (2, 1, 'false');
COMMIT;
INSERT INTO ShelfInInventory (shelf_number, inventory_id, is_full) VALUES  (3, 2, 'true');
COMMIT;
INSERT INTO ShelfInInventory (shelf_number, inventory_id, is_full) VALUES (4, 2, 'false');
COMMIT;
INSERT INTO ShelfInInventory (shelf_number, inventory_id, is_full) VALUES (5, 3, 'true');
COMMIT;

CREATE TABLE ItemQuantity (
                              id INTEGER PRIMARY KEY,
                              name VARCHAR(255) NOT NULL,
                              quantity INTEGER NOT NULL
);
COMMIT;
INSERT INTO ItemQuantity (id, name, quantity) VALUES (1, 'Algae Wafers', 50);
COMMIT;
INSERT INTO ItemQuantity (id, name, quantity) VALUES (2, 'Coral Supplement', 30);
COMMIT;
INSERT INTO ItemQuantity (id, name, quantity) VALUES (3, 'Water Conditioner', 20);
COMMIT;
INSERT INTO ItemQuantity (id, name, quantity) VALUES (4, 'Medicated Feed', 65);
COMMIT;
INSERT INTO ItemQuantity (id, name, quantity) VALUES (5, 'Plankton', 80);
COMMIT;
INSERT INTO ItemQuantity (id, name, quantity) VALUES (6, 'Aquarium Salt', 120);
COMMIT;
INSERT INTO ItemQuantity (id, name, quantity) VALUES (7, 'Water Test Kits', 45);
COMMIT;
INSERT INTO ItemQuantity (id, name, quantity) VALUES (8, 'Frozen Shrimp', 45);
COMMIT;
INSERT INTO ItemQuantity (id, name, quantity) VALUES (9, 'Fish Flakes', 145);
COMMIT;
INSERT INTO ItemQuantity (id, name, quantity) VALUES (10, 'Bloodworms', 155);
COMMIT;
INSERT INTO ItemQuantity (id, name, quantity) VALUES (11, 'Pellets', 200);
COMMIT;
INSERT INTO ItemQuantity (id, name, quantity) VALUES (12, 'Krill', 195);
COMMIT;
INSERT INTO ItemQuantity (id, name, quantity) VALUES (13, 'Squid', 155);
COMMIT;
INSERT INTO ItemQuantity (id, name, quantity) VALUES (14, 'Mysis Shrimp', 250);
COMMIT;


CREATE TABLE ItemUnit (
                          name VARCHAR(255) PRIMARY KEY,
                          unit VARCHAR(255) NOT NULL
);
INSERT INTO ItemUnit (name, unit) VALUES ('Algae Wafers', 'Packets');
INSERT INTO ItemUnit (name, unit) VALUES  ('Coral Supplement', 'Bottles');
INSERT INTO ItemUnit (name, unit) VALUES  ('Water Conditioner', 'Bottles');
INSERT INTO ItemUnit (name, unit) VALUES  ('Medicated Feed', 'Packets');
INSERT INTO ItemUnit (name, unit) VALUES  ('Plankton', 'Packets');
INSERT INTO ItemUnit (name, unit) VALUES  ('Aquarium Salt', 'Boxes');
INSERT INTO ItemUnit (name, unit) VALUES  ('Water Test Kits', 'Kits');


CREATE TABLE Equipment (
                           item_id INTEGER PRIMARY KEY,
                           equipment_function VARCHAR(255) NOT NULL,
                           weight DECIMAL(10,2) NOT NULL,
                           equipment_size VARCHAR(255) NOT NULL,
                           date_installed DATE NOT NULL,
                           FOREIGN KEY (item_id) REFERENCES ItemQuantity(id) ON DELETE CASCADE
);
COMMIT;
INSERT INTO Equipment (item_id, equipment_function, weight, equipment_size, date_installed) VALUES (1, 'Water Filtration', 50.00, 'LARGE', '2020-01-15');
COMMIT;
INSERT INTO Equipment (item_id, equipment_function, weight, equipment_size, date_installed) VALUES (2, 'Protein Skimmer', 8.00, 'MEDIUM', '2021-07-30');
COMMIT;
INSERT INTO Equipment (item_id, equipment_function, weight, equipment_size, date_installed) VALUES (3, 'Heater', 2.00, 'SMALL', '2022-02-11');
COMMIT;
INSERT INTO Equipment (item_id, equipment_function, weight, equipment_size, date_installed) VALUES (4, 'LED Lighting', 5.00, 'MEDIUM', '2019-08-24');
COMMIT;
INSERT INTO Equipment (item_id, equipment_function, weight, equipment_size, date_installed) VALUES (5, 'UV Sterilizer', 6.00, 'SMALL', '2021-04-05');
COMMIT;
INSERT INTO Equipment (item_id, equipment_function, weight, equipment_size, date_installed) VALUES (6, 'Oxygen Pump', 4.50, 'SMALL', '2023-03-29');
COMMIT;
INSERT INTO Equipment (item_id, equipment_function, weight, equipment_size, date_installed) VALUES (7, 'CO2 System', 10.00, 'MEDIUM', '2018-12-10');
COMMIT;

CREATE TABLE Food (
                      item_id INTEGER PRIMARY KEY,
                      exp_date DATE NOT NULL,
                      food_type VARCHAR(255) NOT NULL,
                      FOREIGN KEY (item_id) REFERENCES ItemQuantity(id)
                          ON DELETE CASCADE
);
COMMIT;

INSERT INTO Food (item_id, exp_date, food_type) VALUES (8, '2024-01-15', 'Frozen Shrimp');
COMMIT;
INSERT INTO Food (item_id, exp_date, food_type) VALUES (9, '2024-03-22', 'Fish Flakes');
COMMIT;
INSERT INTO Food (item_id, exp_date, food_type) VALUES (10, '2024-02-11', 'Bloodworms');
COMMIT;
INSERT INTO Food (item_id, exp_date, food_type) VALUES (11, '2024-05-19', 'Pellets');
COMMIT;
INSERT INTO Food (item_id, exp_date, food_type) VALUES (12, '2024-04-13', 'Krill');
COMMIT;
INSERT INTO Food (item_id, exp_date, food_type) VALUES (13, '2024-07-07', 'Squid');
COMMIT;
INSERT INTO Food (item_id, exp_date, food_type) VALUES (14, '2024-06-25', 'Mysis Shrimp');
COMMIT;


CREATE TABLE InStock (
                         item_id INTEGER,
                         shelf_number INTEGER,
                         inventory_id INTEGER,
                         quantity INTEGER NOT NULL,
                         PRIMARY KEY (item_id, shelf_number, inventory_id),
                         FOREIGN KEY (shelf_number, inventory_id) REFERENCES ShelfInInventory(shelf_number,
                                                                                              inventory_id)
                             ON DELETE CASCADE,
                         FOREIGN KEY (item_id) REFERENCES ItemQuantity(id)
                             ON DELETE CASCADE
);

INSERT INTO InStock (item_id, shelf_number, inventory_id, quantity) VALUES (1, 1, 1, 10);
INSERT INTO InStock (item_id, shelf_number, inventory_id, quantity) VALUES  (2, 2, 1, 15);
INSERT INTO InStock (item_id, shelf_number, inventory_id, quantity) VALUES  (3, 3, 2, 20);
INSERT INTO InStock (item_id, shelf_number, inventory_id, quantity) VALUES  (4, 4, 2, 25);
INSERT INTO InStock (item_id, shelf_number, inventory_id, quantity) VALUES  (5, 5, 3, 30);
INSERT INTO InStock (item_id, shelf_number, inventory_id, quantity) VALUES  (6, 6, 3, 12);
INSERT INTO InStock (item_id, shelf_number, inventory_id, quantity) VALUES  (7, 7, 4, 18);

CREATE TABLE Installed (
                           equipment_id INTEGER,
                           water_tank_id INTEGER,
                           quantity INTEGER NOT NULL,
                           date_installed DATE NOT NULL,
                           PRIMARY KEY (equipment_id, water_tank_id),
                           FOREIGN KEY (equipment_id) REFERENCES Equipment(item_id)
                               ON DELETE CASCADE,
                           FOREIGN KEY (water_tank_id) REFERENCES ItemQuantity(id)
                               ON DELETE CASCADE
);
COMMIT;
INSERT INTO Installed(equipment_id, water_tank_id, quantity, date_installed) VALUES (1, 1, 50, '2023-11-20');
COMMIT;
-- ADD MORE HERE



CREATE TABLE Feed (
                      food_id INTEGER,
                      animal_id INTEGER,
                      aquarist_id INTEGER,
                      quantity INTEGER NOT NULL,
                      last_fed DATE NOT NULL,
                      method VARCHAR(255) NOT NULL,
                      PRIMARY KEY (food_id, animal_id, aquarist_id),
                      FOREIGN KEY (food_id) REFERENCES Food(item_id)
                          ON DELETE CASCADE,
                      FOREIGN KEY (animal_id) REFERENCES Animal(id)
                          ON DELETE CASCADE,
                      FOREIGN KEY (aquarist_id) REFERENCES Aquarist(id)
                          ON DELETE CASCADE
);
COMMIT;
INSERT INTO FEED(food_id, animal_id, aquarist_id, quantity, last_fed, method) VALUES (8, 31, 107, 40, '2023-11-20', 'Feeding Tube');
COMMIT;
-- ADD MORE HERE


CREATE TABLE Custodian_Clean_Exhibit_Table (
                                               exhibit_id INTEGER,
                                               custodian_id INTEGER,
                                               PRIMARY KEY (exhibit_id, custodian_id),
                                               FOREIGN KEY (exhibit_id) REFERENCES Exhibit(id) ON DELETE CASCADE,
                                               FOREIGN KEY (custodian_id) REFERENCES Custodian(id) ON DELETE CASCADE
);
COMMIT;

INSERT INTO Custodian_Clean_Exhibit_Table (exhibit_id, custodian_id) VALUES (17, 100);
COMMIT;
INSERT INTO Custodian_Clean_Exhibit_Table (exhibit_id, custodian_id) VALUES (18, 101);
COMMIT;
INSERT INTO Custodian_Clean_Exhibit_Table (exhibit_id, custodian_id) VALUES (19, 108);
COMMIT;
INSERT INTO Custodian_Clean_Exhibit_Table (exhibit_id, custodian_id) VALUES (20, 109);
COMMIT;
INSERT INTO Custodian_Clean_Exhibit_Table (exhibit_id, custodian_id) VALUES (21, 114);
COMMIT;



CREATE TABLE VendorReputation (
                                  vendor_name VARCHAR(255) NOT NULL,
                                  vendor_market_rating VARCHAR(255) NOT NULL,
                                  UNIQUE (vendor_name)
);
COMMIT;

INSERT INTO VendorReputation VALUES ('AquaLife Supplies', 'High');
COMMIT;
INSERT INTO VendorReputation VALUES ('WaterWorld Equipment', 'Medium');
COMMIT;
INSERT INTO VendorReputation VALUES ('Marine Essentials', 'Low');
COMMIT;
INSERT INTO VendorReputation VALUES ('Underwater Gadgets', 'Medium');
COMMIT;
INSERT INTO VendorReputation VALUES ('Oceanic Items', 'High');
COMMIT;


CREATE TABLE VendorLogistics (
                                 id INTEGER PRIMARY KEY,
                                 vendor_logistics_name VARCHAR(255),
                                 address VARCHAR(255) NOT NULL,
                                 FOREIGN KEY (vendor_logistics_name) REFERENCES VendorReputation(vendor_name) ON DELETE CASCADE
);
COMMIT;

INSERT INTO VendorLogistics VALUES (5, 'AquaLife Supplies', '123 Ocean Drive, Marine City, 45678');
COMMIT;
INSERT INTO VendorLogistics VALUES (6, 'WaterWorld Equipment', '789 Coral Blvd, Reef Town, 12345');
COMMIT;
INSERT INTO VendorLogistics VALUES (7, 'Marine Essentials', '456 Habitat St, Salt Lake, 91011');
COMMIT;
INSERT INTO VendorLogistics VALUES (8, 'Underwater Gadgets', '135 Reef Rd, Blue Sea, 11345');
COMMIT;
INSERT INTO VendorLogistics VALUES (9, 'Oceanic Items', '8643 Sea Road, Blue Harbour, 14151');
COMMIT;


CREATE TABLE Grown_In_Plant (
                                plant_id INTEGER,
                                species VARCHAR (255) NOT NULL,
                                living_temp DECIMAL(10, 2) NOT NULL,
                                living_light DECIMAL(10, 2) NOT NULL,
                                water_tank_id INTEGER NOT NULL,
                                PRIMARY KEY (plant_id),
                                FOREIGN KEY (water_tank_id) REFERENCES WaterTankLogistics ON DELETE CASCADE
);
COMMIT;

INSERT INTO Grown_In_Plant VALUES (101, 'Water Lily', 22.5, 100.0, 1);
COMMIT;
INSERT INTO Grown_In_Plant VALUES (102, 'Seaweed', 18.0, 80.0, 2);
COMMIT;
INSERT INTO Grown_In_Plant VALUES (103, 'Mangrove', 25.0, 70.0, 3);
COMMIT;
INSERT INTO Grown_In_Plant VALUES (104, 'Coral', 26.0, 90.0, 4);
COMMIT;
INSERT INTO Grown_In_Plant VALUES (105, 'Anubias', 24.0, 60.0, 5);
COMMIT;


CREATE TABLE Aquarist_Maintain_WaterTank (
                                             aquarist_id INTEGER,
                                             water_tank_id INTEGER,
                                             FOREIGN KEY (aquarist_id) REFERENCES Aquarist(id) ON DELETE CASCADE,
                                             FOREIGN KEY (water_tank_id) REFERENCES WaterTankLogistics(id) ON DELETE CASCADE
);


CREATE TABLE Supply (
                        ItemID INTEGER,
                        VendorID INTEGER,
                        PRIMARY KEY (ItemID, VendorID),
                        FOREIGN KEY (ItemID) REFERENCES ItemQuantity(id) ON DELETE CASCADE,
                        FOREIGN KEY (VendorID) REFERENCES VendorLogistics(id) ON DELETE CASCADE
);
COMMIT;

INSERT INTO Supply (ItemID, VendorID) VALUES (5, 5);
COMMIT;
INSERT INTO Supply (ItemID, VendorID) VALUES (6, 6);
COMMIT;
INSERT INTO Supply (ItemID, VendorID) VALUES (7, 7);
COMMIT;
INSERT INTO Supply (ItemID, VendorID) VALUES (8, 8);
COMMIT;
INSERT INTO Supply (ItemID, VendorID) VALUES (9, 9);
COMMIT;