drop table Exhibit;
drop table Staff;
drop table Custodian;
drop table Veterinarian;
drop table Aquarist;
drop table WaterTankLogistics;
drop table WaterTankpH;
drop table Animal;
drop table Inventory;
drop table ShelfInInventory;
drop table ItemQuantity;
drop table ItemUnit;
drop table Equipment;
drop table Food;
drop table InStock;
drop table Installed;
drop table Feed;
drop table Custodian_Clean_Exhibit_Table;
drop table VendorLogistics;
drop table VendorReputation;
drop table Grown_In_Plant;
drop table Aquarist_Maintain_WaterTank;
drop table Supply;

CREATE TABLE Exhibit (
 id INTEGER PRIMARY KEY,
 Name VARCHAR(255) NOT NULL,
 Status ENUM('OPEN', 'CLOSED', 'UNDER MAINTENANCE', 'COMING SOON') NOT NULL
);


CREATE TABLE Staff (
 id INTEGER PRIMARY KEY,
 salary DECIMAL(10, 2) NOT NULL,
 name VARCHAR(255) NOT NULL,
 datehired DATE NOT NULL
);


CREATE TABLE Custodian (
 id INTEGER PRIMARY KEY,
 exhibit_id INTEGER,
 FOREIGN KEY (exhibit_id) REFERENCES Exhibit(id) ON DELETE CASCADE ON UPDATE CASCADE,
 FOREIGN KEY (id) REFERENCES
 Staff(id) ON DELETE CASCADE);
 CREATE TABLE Veterinarian (
 id INTEGER PRIMARY KEY,
 FOREIGN KEY (id) REFERENCES
 Staff(id) ON DELETE CASCADE
);

CREATE TABLE Aquarist (
 id INTEGER PRIMARY KEY,
 FOREIGN KEY (id) REFERENCES
 Staff(id) ON DELETE CASCADE,
 diving_level DECIMAL (10, 2) NOT NULL,
 water_tank_id INTEGER,
 FOREIGN KEY (water_tank_id) REFERENCES WaterTank(id) ON DELETE CASCADE ON UPDATE
CASCADE
);

CREATE TABLE WaterTankLogistics (
 id INTEGER PRIMARY KEY,
 Name VARCHAR(255) NOT NULL,
 Volume DECIMAL (10, 2) NOT NULL,
 Temperature DECIMAL (3, 1) NOT NULL,
 LightingLevel ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL,
 exhibit_id INTEGER,
 FOREIGN KEY (exhibit_id) REFERENCES Exhibit(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE WaterTankpH (
Temperature DECIMAL (3, 1) PRIMARY KEY,
pH DECIMAL (2,1) NOT NULL
);


CREATE TABLE Animal (
 id INTEGER PRIMARY KEY,
 Name VARCHAR(255) NOT NULL,
 Species VARCHAR(255) NOT NULL,
 Age INTEGER NOT NULL,
 LivingTemp DECIMAL (3, 1) NOT NULL,
 water_tank_id INTEGER,
 veterinarian_id INTEGER,
 FOREIGN KEY (water_tank_id) REFERENCES WaterTankLogistics(id) ON DELETE CASCADE ON
UPDATE CASCADE,
 FOREIGN KEY (veterinarian_id) REFERENCES Veterinarian(id) ON DELETE CASCADE ON UPDATE
CASCADE
);


CREATE TABLE Inventory (
 id INTEGER PRIMARY KEY,
 location VARCHAR(255) NOT NULL
);


CREATE TABLE ShelfInInventory (
 shelf_number INTEGER,
 inventory_id INTEGER,
 is_full ENUM('true', 'false') NOT NULL,
 PRIMARY KEY (shelf_number, inventory_id),
 FOREIGN KEY (inventory_id) REFERENCES Inventory(id)
 ON DELETE CASCADE ON UPDATE CASCADE
);


CREATE TABLE ItemQuantity (
 id INTEGER PRIMARY KEY,
 name VARCHAR(255) NOT NULL,
 quantity INTEGER NOT NULL
);


CREATE TABLE ItemUnit (
 name VARCHAR(255) PRIMARY KEY,
 unit VARCHAR(255) NOT NULL
);


CREATE TABLE Equipment (
 item_id INTEGER PRIMARY KEY,
 function VARCHAR(255) NOT NULL,
 weight DECIMAL(10,2) NOT NULL,
 size VARCHAR(255) NOT NULL,
 date_installed DATE NOT NULL,
 FOREIGN KEY (item_id) REFERENCES ItemQuantity(id)
 ON DELETE CASCADE ON UPDATE CASCADE
);


CREATE TABLE Food (
 item_id INTEGER PRIMARY KEY,
 exp_date DATE NOT NULL,
 food_type VARCHAR(255) NOT NULL,
 FOREIGN KEY (item_id) REFERENCES ItemQuantity(id)
 ON DELETE CASCADE ON UPDATE CASCADE
);


CREATE TABLE InStock (
 item_id INTEGER,
 shelf_number INTEGER,
 inventory_id INTEGER,
 quantity INTEGER NOT NULL,
 PRIMARY KEY (item_id, shelf_number, inventory_id),
 FOREIGN KEY (shelf_number, inventory_id) REFERENCES ShelfInInventory(shelf_number,
inventory_id)
 ON DELETE CASCADE ON UPDATE CASCADE,
 FOREIGN KEY (item_id) REFERENCES ItemQuantity(id)
 ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE Installed (
 equipment_id INTEGER,
 water_tank_id INTEGER,
 quantity INTEGER NOT NULL,
 date_installed DATE NOT NULL,
 PRIMARY KEY (equipment_id, water_tank_id),
 FOREIGN KEY (equipment_id) REFERENCES Equipment(item_id)
 ON DELETE CASCADE ON UPDATE CASCADE,
 FOREIGN KEY (water_tank_id) REFERENCES ItemQuantity(id)
 ON DELETE CASCADE ON UPDATE CASCADE
);


CREATE TABLE Feed (
 food_id INTEGER,
 animal_id INTEGER,
 aquarist_id INTEGER,
 quantity INTEGER NOT NULL,
 last_fed DATE NOT NULL,
 method VARCHAR(255) NOT NULL,
 PRIMARY KEY (food_id, animal_id, aquarist_id),
 FOREIGN KEY (food_id) REFERENCES Food(item_id)
 ON DELETE CASCADE ON UPDATE CASCADE
);


CREATE TABLE Custodian_Clean_Exhibit_Table (
 exhibit_id INTEGER,
 custodian_id INTEGER,
 PRIMARY KEY (exhibit_id, custodian_id),
 FOREIGN KEY (exhibit_id) REFERENCES Exhibit(id) ON DELETE CASCADE ON UPDATE CASCADE,
 FOREIGN KEY (custodian_id) REFERENCES Custodian(id) ON DELETE CASCADE ON UPDATE
CASCADE
);


CREATE TABLE VendorLogistics (
 id INTEGER PRIMARY KEY,
 FOREIGN KEY (name) REFERENCES VendorReputation(name) ON DELETE CASCADE ON UPDATE
CASCADE,
 address VARCHAR(255) NOT NULL
);


CREATE TABLE VendorReputation (
 name VARCHAR(255) NOT NULL,
 vendor_market_rating VARCHAR(255) NOT NULL
);


CREATE TABLE Grown_In_Plant (
 plant_id INTEGER,
 species VARCHAR (255) NOT NULL,
 living_temp DECIMAL(10, 2) NOT NULL,
 living_light DECIMAL(10, 2) NOT NULL,
 water_tank_id INTEGER NOT NULL,
 PRIMARY KEY (plant_id),
 FOREIGN KEY (water_tank_id) REFERENCES WaterTank
 ON DELETE NO ACTION
 ON UPDATE CASCADE
);


CREATE TABLE Aquarist_Maintain_WaterTank (
 aquarist_id INTEGER,
 water_tank_id INTEGER,
 FOREIGN KEY (aquarist_id) REFERENCES Aquarist(id) ON DELETE CASCADE ON UPDATE
CASCADE,
 FOREIGN KEY (water_tank_id REFERENCES WaterTank(ID) ON DELETE CASCADE ON UPDATE
CASCADE
);


CREATE TABLE Supply (
 ItemID INTEGER,
 VendorID INTEGER,
 PRIMARY KEY (ItemID, VendorID),
 FOREIGN KEY (ItemID) REFERENCES Item(ID) ON DELETE CASCADE ON UPDATE CASCADE,
 FOREIGN KEY (VendorID) REFERENCES Vendor(id) ON DELETE CASCADE ON UPDATE CASCADE
);
