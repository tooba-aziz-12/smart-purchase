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
* Product catalogue filtering

    * Category
    * Size
    * Minimum price
    * Maximum price
* Deliver to selection (customer delivery city — affects delivery estimates, not catalogue results)
* Paginated product listing API and UI controls
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

The customer can narrow the catalogue using:

* Category
* Size
* Minimum price
* Maximum price

They can also set **Deliver to**, which is described below.

---

### Deliver To: Customer Context, Not a Catalogue Filter

**Deliver to** is the customer's delivery city. It is intentionally **not** a catalogue filter.

| Input | What it changes | What it does *not* change |
| --- | --- | --- |
| Category, size, price | Which products appear in the catalogue | Delivery estimates |
| **Deliver to** | Delivery estimates on listing cards, product details, and similar products | Which products or sizes appear |

#### Why have this selection if the product list stays the same?

Because the assessment is about purchase **confidence**, not just discovery.

Without a delivery city, every product would show the same generic delivery date. That would not honestly answer:

> *Can I trust the delivery promise?*

With **Deliver to** selected, the API still returns the same in-stock products, but the response includes **personalized delivery estimates** based on where each size would ship from.

Example for Product 1 with **Deliver to = Lahore**:

* Size **L** (stock in Lahore) → 5-day delivery
* Size **M** (stock in Karachi) → 8-day delivery

Both sizes remain available. The customer is not blocked from buying cross-city stock — they just see a longer delivery promise.

This matches a national marketplace model: **the catalogue is nationwide; delivery time depends on fulfillment location.**

It is similar to entering a postcode on a large e-commerce site. The product list often stays the same, but delivery dates change.

#### Why not filter products by warehouse city?

An earlier approach was to only show products stocked in the selected city. That breaks down quickly:

* A customer in Lahore can still receive a product from Karachi
* Hiding Karachi stock makes valid products look unavailable
* Size availability on the product page becomes inconsistent with customer expectations

So warehouse city is used for **delivery calculation**, not catalogue filtering.

#### What exactly changes in the API?

The optional `city` query parameter represents the customer's delivery city.

```http
GET /products?city=Lahore
GET /products/1?city=Lahore
GET /products/1/similar?city=Lahore
```

When `city` is **not** provided, the API returns `estimatedDeliveryRange` instead of a single date:

* `from`: earliest possible delivery (5 days, same-city fulfillment)
* `to`: latest possible delivery (8 days, cross-city fulfillment)

This gives an honest national estimate before the customer selects a delivery city. The UI shows this as a date range.

When `city` is provided:

* Product and size **availability stay the same** (any warehouse with stock)
* `estimatedDelivery` changes based on fulfillment location
* Same-city fulfillment: 5 days
* Cross-city fulfillment: 8 days (+3 days)

On the listing page, each card shows the **earliest** possible delivery for that product in the selected city. On the product details page, the estimate updates when the customer selects a size, because different sizes may ship from different warehouses.

#### UI note

In this prototype, **Deliver to** sits near the catalogue filters for layout simplicity. In production, I would present it separately — for example, "Your delivery city" — so customers do not expect the product list to shrink when they change it.

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

Products are treated as **nationally available** when any warehouse has stock. The **Deliver to** selection does not remove products from the catalogue. Instead, it helps the customer understand **when** the product can reach them.

### Is my preferred size in stock?

The product details page displays all supported sizes.

Available sizes are selectable from **any warehouse with stock**, not just the selected delivery city.

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

The customer is shown an estimated delivery date based on their selected delivery city.

* Same-city fulfillment: 5 days
* Cross-city fulfillment: 8 days

On the product details page, the estimate updates when the customer selects a size, because different sizes may ship from different warehouses.

In a production environment this could also use courier performance, holidays, and historical delivery metrics.

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
* Page through catalogue results
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
&page=
&pageSize=
```

`page` is zero-based. `pageSize` is used instead of `size` because `size` already means product size (`S`, `M`, `L`) in this API.

Response:

```json
{
  "content": [
    {
      "id": 1,
      "name": "Sky Blue Embroidered Lawn Suit",
      "category": "Lawn",
      "price": 7500,
      "imageUrl": "/products/Blue Lawn Suit.png",
      "estimatedDelivery": null,
      "estimatedDeliveryRange": {
        "from": "2026-06-18",
        "to": "2026-06-21"
      },
      "availableSizes": ["M", "L"]
    }
  ],
  "page": 0,
  "size": 6,
  "totalElements": 12,
  "totalPages": 2,
  "last": false
}
```

Each product includes:

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
* Price range within ±1000 PKR (widens to ±3000 PKR only if the tight band returns nothing)
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
imageUrl
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

### Filtering And Pagination

The product listing API uses Spring Data pagination for catalogue reads.

Filtering is done through a JPQL product query with an `EXISTS` inventory predicate, then available sizes are loaded only for the products on the returned page.

This avoids paginating over duplicated inventory rows and avoids vendor-specific string aggregation for the main listing query.

---

### Delivery Estimation Service

Products do not store delivery dates.

Instead, delivery estimates are calculated through a dedicated service using the customer's delivery city and the warehouse city that would fulfill the order.

Current implementation:

```text
Same-city fulfillment: Current Date + 5 Days
Cross-city fulfillment: Current Date + 8 Days
```

The optional `city` query parameter is the customer's **deliver-to** city. It does not filter catalogue results.

When `city` is not provided, the API returns `estimatedDeliveryRange` (5–8 days) instead of a single `estimatedDelivery`.

When `city` is provided:

* Listing cards show the earliest possible delivery for each product
* Product details return a per-size `estimatedDelivery`
* Similar products use the same delivery context

When choosing a warehouse for a size, the service prefers local stock in the deliver-to city when available. Otherwise it uses cross-city stock and adds the extra delivery days.

See **Deliver To: Customer Context, Not a Catalogue Filter** above for the product rationale.

---

### Pricing Assumptions

The price breakdown is calculated using configured values in `application.yaml`:

```text
Platform Fee: 200 PKR
Delivery Fee: 250 PKR
VAT Rate: 15%
```

These values are intentionally simple for the assessment. In production they would likely be managed by pricing, tax, and delivery configuration rather than static application settings.

#### Money Representation And Display

Money is kept in `BigDecimal` end to end (entities, DTOs, pricing config) with a `DECIMAL(10,2)` column, so all arithmetic is exact and there is no floating-point rounding risk. Amounts are displayed as whole rupees with thousands separators (for example `PKR 14,500`) because PKR is not transacted in paisa at retail, which matches how South Asian storefronts present prices.

This is a deliberate, market-appropriate call rather than a shortcut. If this grew into real checkout and payments, I would switch the persisted representation to integer minor units (paisa) to align with payment gateways, which typically operate in the smallest currency unit.

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

If the tight ±1000 PKR band returns no alternatives (for example in categories where the closest priced item is further away), the query falls back to a wider ±3000 PKR band so the customer still sees relevant options instead of an empty section. The tight band is always tried first, so close matches are preferred when they exist.

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
47 tests passing
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
23 tests passing
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
* Filter by deliver-to city (delivery estimate only)
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
* Product listing pagination controls
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

### Similar products price band

The similar products feature prefers a tight ±1000 PKR price range within the same category. With the current seed data, Festive products (9500 PKR and 11000 PKR) and Formal products (12500 PKR and 14500 PKR) are more than 1000 PKR apart, so the tight band alone would return nothing for those categories. To avoid an empty alternatives section, the query falls back to a wider ±3000 PKR band only when the tight band returns no results. Close matches are still preferred when they exist. In production this band would likely be tuned per category or replaced by a recommendation service.

---

## Future Improvements

### Inventory

* Inventory reservations
* Low stock indicators
* Real-time inventory updates

### Delivery

* Separate deliver-to UI from catalogue filters
* Delivery fee or SLA changes by distance
* Delivery range instead of a single date on listing cards
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
* Monitoring
* Security
* API versioning
* Performance testing

---

## AI Usage

### AI Tool Used

* ChatGPT
* Claude

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


