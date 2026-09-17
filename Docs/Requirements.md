# Requirements & System Foundation

## 1. Project Overview

Project Name
Smart Farmer Procurement & Queue Management System

### DESCRIPTION 

An AI-powered web application designed to simplify the agricultural procurement process for farmers. The system enables farmers to register, manage crop information, find procurement centers, book available procurement slots, receive digital tokens, track their queue position, and monitor procurement and payment status.
The platform provides separate functionality for farmers, procurement-center operators, and administrators. It also uses AI-based waiting-time and demand prediction to help farmers and procurement centers make better decisions and manage queues more efficiently. A multilingual AI assistant can provide farmers with information about schedules, tokens, procurement status, and other system-related queries.
Technology Stack: React, Tailwind CSS, Java, Spring Boot, Spring Security, JWT, MySQL, Python, FastAPI, Pandas, and Scikit-learn.

### Objective
The system is designed to digitize the agricultural procurement process between 
farmers and procurement centers.
Currently, farmers may need to physically visit procurement centers, wait in long 
queues, and manually manage procurement schedules. This system allows farmers

to:
1. Register themselves.
2. Maintain their farmer profile.
3. Register crops for procurement.
4. Find suitable procurement centers.
5. Book available slots.
6. Receive a digital token.
7. Track their queue position.
8. Complete crop procurement.
9. Track payment status.
The system also provides center operators with tools to manage farmers, slots, 
queues, procurement records and payments.
Administrators can monitor and manage the entire platform.


## 2. User Roles
Your system will have three primary roles.

     SMART FARMER SYSTEM
             │
┌────────────┼────────────┐
│            |            |
│            │            |
FARMER   CENTER_OPERATOR  ADMIN
  

## 2.1 FARMER

The farmer is the primary user of the system.
Farmer capabilities
The farmer can:
Register an account.
Login/logout.
Manage profile.
Add/update farmer information.
Add crops.
View registered crops.
Search procurement centers.
View available slots.
Book a slot.
Receive a token.
View token details.
Track queue position.
Receive queue updates.
View procurement status.
View payment status.
View previous procurement history.
Login.
View center dashboard.
View today's bookings.
View farmer queue.
Call the next farmer.
Update queue status.
Verify farmer details.
Verify crop information.
Record actual crop quantity.
Record crop quality/grade.
Record procurement price.

## Example

Farmer
  ↓
Login
  ↓
Dashboard
  ↓
Select Crop
  ↓
Select Procurement Center
  ↓
Select Date
  ↓
Select Slot
  ↓
Book Slot
  ↓
Generate Token
↓

## 2.2 CENTER_OPERATOR

The Center Operator manages a specific procurement center.
Center Operator capabilities
The operator can:
Complete procurement.
Create payment record.
Update payment status.
View daily procurement statistics.
Manage farmers.
Manage center operators.
Manage procurement centers.
Manage crops.
Manage slots.
View procurement records.
View payment records.

## Operator workflow

Login
  ↓
Center Dashboard
  ↓
Today's Slots
  ↓
Farmer Queue
  ↓
Call Farmer
  ↓
Verify Farmer
  ↓
Verify Crop
  ↓
Record Quantity
  ↓
Record Quality
↓

## 2.3 ADMIN

The administrator controls the complete system.
Admin capabilities
Admin can:
View system statistics.
Block/unblock users.
Assign operators to centers.
Configure procurement rules.
Monitor system activity.

## Admin workflow

Admin Login
     ↓
Admin Dashboard
     ↓
Users
 ┌───┼───────────────┐
 ↓   ↓               ↓
Farmers Operators   Admins
     ↓
Procurement Centers
     ↓
Slots
     ↓
Procurement
     ↓
Payments
     ↓
Reports

## 3. Complete System Workflow
This is the most important part of Day 1.
Your complete workflow should be:

REGISTER
   ↓
LOGIN
   ↓
FARMER PROFILE
   ↓
CROP
   ↓
PROCUREMENT CENTER
   ↓
SLOT
   ↓
TOKEN
   ↓
QUEUE
   ↓
PROCUREMENT

### Now let's understand every step.
### 3.1 Registration

Farmer creates an account.
Possible information:
Name
Mobile Number
Email
Password
Address
Village
District
State
Farmer ID
Example:
Name: XYZ Kumar
Mobile: 98XXXXXXXX
Village: ABC Village
District: JKL
State: Uttar Pradesh
The backend creates the farmer account.

### 3.2 Login

User enters:
Mobile/Email
Password
Backend verifies credentials.
If valid:
Login successful
       ↓
JWT Token
       ↓
Farmer Dashboard
We will use JWT-based authentication.

### 3.3 Farmer Profile

After registration, the farmer can complete their profile.
Example:
Farmer Profile
Name
Mobile
Village
District
State
Farmer ID
Land Area
Preferred Crops
This information will be stored in the database.

### 3.4 Crop Registration
Farmer selects the crop they want to sell/procure.
Example:
Crop---------------
Wheat
Quantity---------------
500 KG
Expected Date---------------
20 September 2026
The system stores this information.
Possible crop information:
cropId
cropName
quantity
unit
expectedDate
quality
status

### 3.5 Procurement Center

The farmer selects a procurement center.
Example:
Available Centers
1. JKL Procurement Center
2. LKJ Procurement Center
3. abc Procurement Center
The system can eventually use location-based search.
For the first version, simple district/location filtering is enough.

### 3.6 Slot Booking

Each procurement center has available slots.
Example:
20 September
09:00 AM - 10:00 AM
10:00 AM - 11:00 AM
11:00 AM - 12:00 PM
12:00 PM - 01:00 PM
Farmer chooses a slot.
Example:
Center:
JKL Procurement Center
Date:
20 September 2026
Time:
10:00 AM - 11:00 AM
The system checks whether the slot has capacity.

### 3.7 Token Generation

After successful slot booking, the system generates a unique token.
Example:
TOKEN
#WHT-2026-0045
Token information:
Token ID
Farmer
Crop
Center
Date
Slot
Queue Number
Status
Example:
Token: WHT-2026-0045
Farmer: ABC Kumar
Crop: Wheat
Quantity: 500 KG
Center:
JKL Procurement Center
Slot:
10:00 AM - 11:00 AM
Queue Position:
12
Status:
WAITING

### 3.8 Queue Management

This is one of the most important features of your project.
Instead of farmers physically standing in a queue, the system manages a digital 
queue.
Example:
CURRENT QUEUE
Token       Farmer          Status
#0040       Farmer A        COMPLETED
#0041       Farmer B        PROCESSING
#0042       Farmer C        WAITING
#0043       Farmer D        WAITING
#0044       Farmer E        WAITING
The farmer can see:
Your Token: #0044
Current Token: #0041
People Before You: 2
Estimated Waiting Time:
~30 minutes
This can become one of the strongest features of your project.

### 3.9 Procurement

When the farmer reaches the center:
CENTER OPERATOR
       ↓
Verify Farmer
       ↓
Verify Token
       ↓
Verify Crop
       ↓
Measure Quantity
       ↓
Check Quality
       ↓
Calculate Amount
       ↓
Complete Procurement
Example:
Crop: Wheat
Quantity:
480 KG
Quality:
A
Rate:
₹2,425 / Quintal
Total:
₹11,640
The operator submits the procurement record.

### 3.10 Payment

After procurement:
PROCUREMENT COMPLETED
          ↓
PAYMENT CREATED
          ↓
PAYMENT PROCESSING
          ↓
PAYMENT COMPLETED
Example:
Payment ID: PAY-10245
Farmer:
Rahul Kumar
Amount:
₹11,640
Status:
PAID
Initially, I recommend implementing payment tracking, rather than integrating a real 
bank/payment gateway.
Later you can add payment gateway/banking integration if required.

##  4. Recommended Technology Stack
For your project, I recommend using a Java/Spring Boot backend, because you're 
already learning Java and Spring Boot and this project is strong for a Java developer 
resume.
Architecture
                    FRONTEND
                       │
                       │ REST API
                       ↓
                SPRING BOOT API
                       │
          ┌────────────┼────────────┐
          │            │            │
       MySQL          AI         Redis
          │         Service         │
          │            │            │
          └────────────┼────────────┘
                       │
                    Storage
## 5. Backend Technologies
Java
Use:
Java 21 LTS
I recommend Java 21 for this project rather than using the newest Java version, 
because Java 21 is an LTS release and is a very common enterprise choice.
Spring Boot
Use:
Spring Boot
Main responsibilities:
REST APIs
Business Logic
Authentication
Database Communication
Validation
Exception Handling
Spring Web
For REST APIs:
Spring Web
Example:
POST /api/auth/register
POST /api/auth/login
GET /api/farmers/profile
POST /api/crops
GET /api/centers
POST /api/slots/book
GET /api/queue/{tokenId}
POST /api/procurement
GET /api/payments

## 6. Database
MySQL
Use:
MySQL
This will store:
users
farmers
operators
centers
crops
slots
tokens
queues
procurements
payments
Possible database structure:
users
  │
  ├── farmers
  │
  └── operators
farmers
   │
   └── crops
centers
   │
   └── slots
slots
   │
   └── tokens

## 7. Spring Data JPA
Use:
Spring Data JPA
Hibernate
Instead of manually writing SQL for every operation.
Example:
public interface FarmerRepository
        extends JpaRepository<Farmer, Long> {
}

## 8. Authentication
Use:
Spring Security
+
JWT
Architecture:
Login
  ↓
Spring Security
  ↓
Verify User
  ↓
Generate JWT
  ↓
Frontend
  ↓
JWT sent with API requests
Example:
Authorization: Bearer <JWT>

## 9. Frontend
I recommend:
React.js
with:
Vite
and:
Tailwind CSS
Technology:
React
Vite
Tailwind CSS
Axios
React Router
Frontend structure
frontend/
│
├── src/
│   ├── components/
│   ├── pages/
│   ├── layouts/
│   ├── services/
│   ├── hooks/
│   ├── context/
│   └── utils/
│
└── package.json

## 10. API Communication
Use:
Axios
Example:
axios.get("/api/farmers/profile");
For production, you can configure:
Frontend
    ↓
REST API
    ↓
Spring Boot

## 11. AI Service
Use:
Python
FastAPI
The AI service will communicate with Spring Boot.
Architecture:
React
  ↓
Spring Boot
  ↓
AI Service
  ↓
Python
Potential AI features later:
### 1. Queue time prediction
Current Queue
+
Historical Processing Time
+
Number of Operators
+
Farmers Waiting
        ↓
AI Model
        ↓
Estimated Waiting Time
Example:
Current Queue: 15 farmers
Predicted waiting time:
42 minutes
### 2. Crop demand prediction
Historical procurement data can be used to predict demand.
### 3. Farmer/crop insights
You could later add analytics based on historical procurement.
Important: Don't start by building the AI model. Build the core procurement system 
first.
## 12. Redis
For a production-ready version, use:
Redis
especially for queue-related operations.
Why?
Because queue data can change frequently:
Token #101
Token #102
Token #103
...
Redis can provide fast access for:
Current queue
Current token
Queue position
Temporary booking data
Caching
However, Redis is not required on Day 1.
Start with:
MySQL
and introduce Redis when the basic queue works.

## 13. API Documentation
Use:
Swagger / OpenAPI
This allows you to test APIs from the browser.
Example:
http://localhost:8080/swagger-ui/index.html
You'll eventually document:
Authentication APIs
Farmer APIs
Crop APIs
Center APIs
Slot APIs
Token APIs
Queue APIs
Procurement APIs
Payment APIs
Admin APIs

## 14. Development Tools
Use:
Tool
VS Code
IntelliJ IDEA
MySQL
MySQL Purpose
Frontend 
+ general development
Java/Spring Boot
Database
Database 
Workbench
management
Postman API 
testing
Git Version 
control
GitHub Repository
Docker Deployment/environment
Swagger API 
documentation

## 15. Project Architecture
I recommend this architecture:
smart-farmer/
│
├── backend/
│   │
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com.smartfarmer/
│   │   │   │       ├── config/
│   │   │   │       ├── controller/
│   │   │   │       ├── dto/
│   │   │   │       ├── entity/
│   │   │   │       ├── exception/
│   │   │   │       ├── repository/
│   │   │   │       ├── security/
│   │   │   │       └── service/
│   │   │   │
## 16. Core Database Entities

User
Farmer
CenterOperator
ProcurementCenter
Crop
Slot
Token
Queue
Procurement
Payment
Later you may add:
Notification
AuditLog
FarmerDocument
CropPrice
QualityAssessment

## 17. Relationship Between Entities

USER
 │
 ├──────────────┐
 ↓              ↓
FARMER      CENTER_OPERATOR
 │              │
 │              ↓
 │       PROCUREMENT_CENTER
 │              │
 ↓              ↓
CROP          SLOT
 │              │
 └──────→ TOKEN ←──────┘
             │
             ↓
           QUEUE
             │
## 18. Status Values
Token status
WAITING
CALLED
PROCESSING
COMPLETED
CANCELLED
EXPIRED
Slot status
AVAILABLE
FULL
CLOSED
CANCELLED
Procurement status
PENDING
IN_PROGRESS
COMPLETED
REJECTED
Payment status
PENDING
PROCESSING
PAID
FAILED

## 19. Functional Requirements
Your 
requirements.md should contain requirements such as:
FR-01 — Registration
The system shall allow farmers to create an account.
FR-02 — Authentication
The system shall authenticate users securely using JWT.
FR-03 — Farmer Profile
The system shall allow farmers to create and update their profile.
FR-04 — Crop Management
The system shall allow farmers to register crops.
FR-05 — Center Discovery
The system shall allow farmers to view available procurement centers.
FR-06 — Slot Booking
The system shall allow farmers to book available procurement slots.
FR-07 — Token Generation
The system shall generate a unique token after successful booking.
FR-08 — Queue Management
The system shall maintain the farmer's position in the procurement queue.
FR-09 — Procurement
Center operators shall be able to process farmer procurement.
FR-10 — Payment
The system shall record and track farmer payments.
FR-11 — Administration
Administrators shall be able to manage users, centers and system data.
FR-12 — Notifications
The system should notify farmers about important queue and booking events.

## 20. Non-Functional Requirements

Security
JWT Authentication
Password Hashing
Role-Based Authorization
Input Validation
API Security
Performance
The system should respond quickly for normal API requests.
Scalability
The architecture should allow multiple procurement centers.
Reliability
Booking and queue operations should avoid duplicate bookings.
Maintainability
Use layered architecture:
Controller
↓
Service
↓
Repository
↓
DatabaseUsability
The farmer dashboard should be simple enough for users with limited technical experience.

## 21. Recommended Technology Stack 

┌─────────────────────────────────────┐
│             
FRONTEND
│ React + Vite + Tailwind CSS
                │
         │
│ React Router + Axios                │
└─────────────────┬───────────────────┘
                  │
                REST
                  │
┌─────────────────▼───────────────────┐
│             BACKEND                 │
│ Java 21                             │
│ Spring Boot                         │
│ Spring Web                          │
│ Spring Security                     │
│ JWT                                 │
│ Spring Data JPA                     │
│ Hibernate │
For deployment later:
Frontend → Vercel
Backend  → Render/Railway/AWS
Database → MySQL-compatible cloud DB
AI       → Render/AWS

