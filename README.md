# Smart Purchase

## Problem Understanding

The goal of this assessment is to help customers make confident purchase decisions before adding a product to their cart.

When browsing fashion products, customers often hesitate because they are unsure:

* Is this product available for me?
* Is my preferred size in stock?
* What will I actually pay?
* Can I trust the delivery promise?
* Are there similar alternatives if this product is not suitable?

My interpretation of the problem was that confidence comes from inventory visibility, transparent pricing, realistic delivery expectations, and relevant alternatives.

Rather than building a complete marketplace, I focused on solving the decision-making phase of the shopping journey.

---

## Scope

### Implemented

* Product listing page
* Product filtering

    * Category
    * Size
    * City
    * Minimum price
    * Maximum price
* Product details page
* Size availability visibility
* Estimated delivery date
* Complete price breakdown
* Similar product recommendations
* Structured backend error responses
* Backend APIs
* Unit tests
* Integration tests
* Frontend API and component tests

### Not Implemented

* Authentication
* Persistent shopping cart
* Checkout flow
* Payment processing
* Real warehouse allocation
* Real delivery estimation
* Personalized recommendations

These were intentionally excluded to stay within the 3–4 hour time limit.

---

## User Flow

### 1. Product Discovery

The customer lands on the product listing page.

All available products are displayed by default.

The customer can narrow results using:

* Category
* Size
* City
* Price range

---

### 2. Product Evaluation

When a customer opens a product they can immediately see:

* Available sizes
* Estimated delivery date
* Complete pricing breakdown
* Similar alternatives

This directly addresses the core confidence questions.

### Is this product available for me?

Only products with available inventory are returned from the API.

Availability is derived from warehouse inventory rather than manually maintained flags.

### Is my preferred size in stock?

The product details page displays all supported sizes.

Available sizes are selectable.

Unavailable sizes are disabled.

### What will I actually pay?

The customer is shown a complete price breakdown:

* Product Price
* Platform Fee
* Delivery Fee
* VAT
* Total

This removes uncertainty around final pricing.

### Can I trust the delivery promise?

The customer is shown an estimated delivery date.

The current implementation uses a delivery estimation service.

In a production environment this could be calculated using:

* Customer location
* Warehouse location
* Inventory availability
* Courier performance
* Historical delivery metrics

### Are there similar alternatives if this product is not suitable?

The product details page displays similar products.

Products are selected using:

* Same category
* Similar price range

If no matching alternatives are available, the similar products section is hidden instead of showing an empty recommendation area.

This reduces abandonment when a customer is unsure about a particular product.

---

## Technical Approach

### Frontend

Technology:

* React
* React Router
* Vite

Structure:

```text
src
├── api
├── pages
│   ├── ProductListPage.jsx
│   └── ProductDetailsPage.jsx
├── App.jsx
└── main.jsx
```

### ProductListPage

Responsibilities:

* Display products
* Apply filters
* Navigate to product details
* Show user-facing loading failure messages

Product listing error handling:

* Network or backend unavailable: `We couldn’t load products right now. Please try again.`
* Invalid filters from the backend: displays the backend validation message
* Unexpected API/server errors: `Something went wrong while loading products.`
* Successful empty result: `No products found.`

### ProductDetailsPage

Responsibilities:

* Display product information
* Display available sizes
* Display estimated delivery
* Display pricing breakdown
* Display similar products

---

### Backend

Technology:

* Kotlin
* Spring Boot
* Spring Data JPA
* H2 Database

Structure:

```text
controller
    ↓
service
    ↓
repository
    ↓
database
```

---

## APIs

### Get Products

```http
GET /products
```

Optional filters:

```http
?category=
&size=
&city=
&minPrice=
&maxPrice=
```

Returns:

* Product details
* Available sizes
* Estimated delivery

---

### Get Product Details

```http
GET /products/{id}
```

Returns:

* Product details
* Size availability
* Price breakdown
* Estimated delivery

---

### Get Similar Products

```http
GET /products/{id}/similar
```

Returns available alternatives selected by:

* Same category
* Price range within ±1000 PKR
* Excluding the current product

---

### Error Response

Expected API errors return a structured response:

```json
{
  "timestamp": "2026-06-13T20:00:00+05:00",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found",
  "path": "/products/999"
}
```

The frontend uses these responses to distinguish validation errors from unexpected failures.

---

## Data Model

### Product

```text
id
name
category
price
```

### Warehouse

```text
id
city
```

### Warehouse Inventory

```text
id
warehouseId
productId
size
quantity
```

`WarehouseInventory` is modeled as a JPA entity and backed by database constraints:

* `warehouse_id` references `warehouses.id`
* `product_id` references `products.id`
* `(warehouse_id, product_id, size)` is unique
* `quantity` must be greater than or equal to zero

---

## Key Technical Decisions

### Inventory Driven Availability

Product availability is derived from warehouse inventory.

This avoids duplication and keeps inventory as the source of truth.

---

### Delivery Estimation Service

Products do not store delivery dates.

Instead, delivery estimates are calculated through a dedicated service.

Current implementation:

```text
Current Date + 5 Days
```

Future implementations could use:

* Customer location
* Nearest warehouse
* Inventory allocation
* Courier performance
* Public holidays

---

### Pricing Assumptions

The price breakdown is calculated using configured values in `application.yaml`:

```text
Platform Fee: 200 PKR
Delivery Fee: 250 PKR
VAT Rate: 15%
```

These values are intentionally simple for the assessment. In production they would likely be managed by pricing, tax, and delivery configuration rather than static application settings.

---

### Similar Product Recommendations

Similar products are exposed through a dedicated backend endpoint:

```http
GET /products/{id}/similar
```

The backend selects alternatives using:

* Same category
* Price range within ±1000 PKR
* Available inventory
* Current product excluded

This keeps recommendation rules testable and consistent across clients without introducing a full recommendation engine.

---

## How To Run

### Backend

```bash
cd backend

./mvnw spring-boot:run
```

Runs on:

```text
http://localhost:8095
```

---

### Frontend

```bash
cd frontend

npm install

# Optional: override API base URL
cp .env.example .env

npm run dev
```

Runs on:

```text
http://localhost:5173
```

---

## Tests

### Commands

Backend:

```bash
cd backend
./mvnw test
```

Current backend suite:

```text
25 tests passing
```

Frontend:

```bash
cd frontend
npm test
npm run lint
npm run build
```

Current frontend suite:

```text
10 tests passing
lint passing
production build passing
```

### Unit Tests

ProductServiceTest
DeliveryEstimatorTest

Coverage:

* Product mapping
* Product details generation
* Size availability calculation
* Price breakdown calculation
* Product not found scenario
* Delivery estimate calculation

---

### Integration Tests

ProductControllerIT

Coverage:

* Retrieve all products
* Filter by category
* Filter by city
* Filter by size
* Filter by minimum and maximum price
* Retrieve product details
* Retrieve similar products
* Invalid filters and not-found responses

Integration tests validate:

```text
HTTP
→ Controller
→ Service
→ Repository
→ H2 Database
```

---

### Frontend Tests

Frontend tests include API-helper tests and focused component tests.

API helper tests validate:

* Product listing request construction
* Product details request construction
* Similar products request construction
* API error handling for non-2xx responses

Component tests validate:

* Product listing rendering and filter submission
* Product details rendering
* Size selection behavior
* Price breakdown rendering
* Similar product rendering

These are not exhaustive browser end-to-end tests.

---

## Tradeoffs

Given the 3–4 hour time limit, I prioritized solving the core customer confidence problem.

### Chosen

* H2 database
* Mock inventory
* Simple recommendation strategy
* Simple delivery estimation
* Minimal but complete user flow

### Deferred

* Checkout
* Authentication
* Persistent cart
* Inventory reservations
* Real delivery calculations
* Recommendation engine
* Personalization

---

## Future Improvements

### Inventory

* Inventory reservations
* Low stock indicators
* Real-time inventory updates

### Delivery

* Warehouse selection
* Courier integration
* Delivery confidence scoring
* SLA monitoring

### Recommendations

* Brand similarity
* Behavioral recommendations
* Personalized recommendations

### Commerce

* Shopping cart
* Checkout
* Payments
* Order tracking

### Engineering

* Pagination
* Monitoring
* Security
* API versioning
* Performance testing

---

## AI Usage

### AI Tool Used

* ChatGPT

### What AI Helped With

* Brainstorming product scope
* API design discussions
* UI ideas
* Test strategy discussions
* Architecture reviews

### What Was Manually Reviewed

* Repository queries
* Service logic
* React implementation
* DTO design
* Test implementation

### Example Where AI Output Was Improved

An early suggestion proposed exposing warehouse information directly to customers:

```text
Ships From: Lahore Warehouse
```

I rejected this approach because it exposes operational details and is not commonly shown by ecommerce platforms.

Instead, the final implementation communicates:

```text
Estimated Delivery: Jun 18
```

which better aligns with real-world marketplace experiences while still improving customer confidence.

---

## AI Audit Trail

| Step | Activity                   | AI Contribution                                                               | Human Review                                |
| ---- | -------------------------- | ----------------------------------------------------------------------------- | ------------------------------------------- |
| 1    | Problem Scoping            | Brainstormed approaches to improve customer purchase confidence               | Selected final scope and user flow manually |
| 2    | Data Model Design          | Suggested Product, Warehouse, and Inventory relationships                     | Reviewed and simplified data model          |
| 3    | API Design                 | Discussed filtering API and product details API structure                     | Implemented final API contracts             |
| 4    | Backend Implementation     | Assisted with repository queries and service layer structure                  | Reviewed, corrected, and implemented logic  |
| 5    | Frontend Design            | Generated initial React page structure and filtering UI ideas                 | Refined UI layout and user experience       |
| 6    | Product Details Experience | Suggested size selection, delivery visibility, and pricing breakdown concepts | Selected final customer-facing information  |
| 7    | Similar Products Feature   | Brainstormed recommendation approaches                                        | Chose category + price similarity strategy  |
| 8    | Error Handling             | Suggested structured API errors and frontend response handling                | Added explicit error DTO and response checks |
| 9    | Validation                 | Reviewed filter edge cases and API behavior                                   | Kept strict validation for malformed filters |
| 10   | Data Integrity             | Suggested inventory constraints                                               | Added entity mapping, foreign keys, uniqueness, and quantity checks |
| 11   | Testing                    | Suggested unit, integration, and frontend test scenarios                      | Implemented backend and frontend tests      |
| 12   | Architecture Review        | Reviewed design decisions and tradeoffs                                       | Made final implementation decisions         |
| 13   | Documentation              | Assisted in drafting README structure and content                             | Reviewed and edited final documentation     |


