# E-Commerce Development Roadmap

### Spring Boot + React/Next.js — From Zero to Production

> **How to use this document:** Follow phases in order. Each phase builds on the previous.
> Don't skip levels. Understand before you move on.
> Reference GitHub repos from the curated list at the end of each phase.

---

## Table of Contents

1. [Prerequisites & Setup](#phase-0-prerequisites--environment-setup)
2. [Phase 1 — Monolith Backend (Spring Boot)](#phase-1--monolith-backend-spring-boot)
3. [Phase 2 — Frontend (React / Next.js)](#phase-2--frontend-react--nextjs)
4. [Phase 3 — Security & Auth](#phase-3--security--authentication)
5. [Phase 4 — Advanced Features](#phase-4--advanced-features)
6. [Phase 5 — Microservices Architecture](#phase-5--microservices-architecture)
7. [Phase 6 — Messaging & Event-Driven](#phase-6--messaging--event-driven-kafka)
8. [Phase 7 — Observability & Monitoring](#phase-7--observability--monitoring)
9. [Phase 8 — Kubernetes & Cloud Deployment](#phase-8--kubernetes--cloud-deployment)
10. [Little Things That Matter](#little-things-that-matter)
11. [Reference GitHub Repos](#reference-github-repos)
12. [Checklist Before Going Live](#checklist-before-going-live)

---

## Phase 0: Prerequisites & Environment Setup

> Get your environment right before writing a single line of app code.

### Skills You Must Have Before Starting

- Java 17+ fundamentals (OOP, Collections, Generics, Streams, Optional)
- SQL basics (JOINs, indexes, transactions)
- REST API concepts (HTTP methods, status codes, JSON)
- Git basics (commit, branch, merge, rebase)
- Basic command line usage

### Tools to Install

```
JDK 17 or 21        → https://adoptium.net
Maven or Gradle     → bundled with Spring Initializr
Node.js 18+         → https://nodejs.org
Docker Desktop      → https://docker.com
IntelliJ IDEA       → Community edition is fine
Postman             → API testing
Git                 → https://git-scm.com
```

### Project Structure Convention (follow from Day 1)

```
ecommerce/
├── backend/                    # Spring Boot project
│   ├── src/main/java/com/yourname/ecom/
│   │   ├── controller/         # REST endpoints
│   │   ├── service/            # Business logic
│   │   ├── repository/         # Data access (JPA interfaces)
│   │   ├── model/              # JPA entities
│   │   ├── dto/                # Request/Response objects
│   │   ├── exception/          # Global exception handling
│   │   ├── config/             # Security, CORS, beans
│   │   └── EcommerceApplication.java
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── application-dev.properties
│   └── pom.xml
│
├── frontend/                   # React or Next.js project
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/           # API calls (axios)
│   │   ├── store/              # Redux / Zustand
│   │   └── utils/
│   └── package.json
│
└── docker-compose.yml          # Database + services
```

### Little Things — Phase 0

- Use `.gitignore` from the very start. Add `application-secrets.properties`, `.env`, `target/`, `node_modules/`.
- Never commit passwords or API keys to Git. Use environment variables.
- Create a `dev` branch. Keep `main` clean.
- Use `application-dev.properties` for local config and `application-prod.properties` for production.

---

## Phase 1 — Monolith Backend (Spring Boot)

> Build the entire backend as one application first. Learn the concepts before splitting into microservices.

### Step 1.1 — Project Initialization

Use [Spring Initializr](https://start.spring.io) with these dependencies:

```
Spring Web
Spring Data JPA
MySQL Driver (or PostgreSQL)
Lombok
Spring Boot DevTools
Spring Validation
```

### Step 1.2 — Database Design (Do This First!)

Think hard about your entities before writing code. A bad schema is painful to fix later.

**Core entities:**

```
User         → id, name, email, password, role, createdAt
Product      → id, name, description, price, brand, category, stock, imageUrl, active
Category     → id, name, parentCategory (self-join for subcategories)
Cart         → id, user (FK), items (OneToMany)
CartItem     → id, cart (FK), product (FK), quantity, price
Order        → id, user (FK), status, totalAmount, address, createdAt
OrderItem    → id, order (FK), product (FK), quantity, priceAtTime
Address      → id, user (FK), street, city, state, pincode, country
Review       → id, user (FK), product (FK), rating, comment, createdAt
```

**Relationships cheat sheet:**

```
User       ←→ Cart       : OneToOne  (one user, one active cart)
Cart       ←→ CartItem   : OneToMany
Product    ←→ CartItem   : ManyToOne
User       ←→ Order      : OneToMany (one user, many orders)
Order      ←→ OrderItem  : OneToMany
Product    ←→ OrderItem  : ManyToOne
Product    ←→ Category   : ManyToOne
User       ←→ Address    : OneToMany
User       ←→ Review     : OneToMany
Product    ←→ Review     : OneToMany
```

### Step 1.3 — JPA Entity Example

```java
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

**Little things — Entities:**

- Always use `BigDecimal` for price. Never `double` or `float` (floating point errors in money = disaster).
- Use `FetchType.LAZY` on all `@ManyToOne` and `@OneToMany` by default. Only use `EAGER` when you're sure you always need the related data.
- Always add `createdAt` and `updatedAt` using `@CreationTimestamp` / `@UpdateTimestamp`.
- Add `active = true` flag instead of deleting records (soft delete).

### Step 1.4 — Service Layer (Business Logic)

**Never put business logic in controllers.** Controllers only route, validate input, and delegate.

```java
@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Page<ProductDTO> getAllProducts(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return productRepository.findAllByActiveTrue(pageable)
                                .map(this::toDTO);
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return toDTO(product);
    }
}
```

### Step 1.5 — DTOs (Data Transfer Objects)

**Never return entities directly from your controller.** Always use DTOs.

Why: entities have lazy-loaded relations that cause JSON serialization errors, and you may expose sensitive fields.

```java
// Request DTO
public record CreateProductRequest(
    @NotBlank String name,
    @NotNull BigDecimal price,
    @Min(0) Integer stockQuantity,
    Long categoryId
) {}

// Response DTO
public record ProductDTO(
    Long id,
    String name,
    BigDecimal price,
    String categoryName,
    Integer stockQuantity,
    boolean inStock
) {}
```

### Step 1.6 — Global Exception Handling

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("NOT_FOUND", ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors()
                           .stream()
                           .map(e -> e.getField() + ": " + e.getDefaultMessage())
                           .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(new ErrorResponse("VALIDATION_FAILED", message, LocalDateTime.now()));
    }
}
```

### Step 1.7 — API Design Conventions

```
GET     /api/v1/products              → list products (paginated)
GET     /api/v1/products/{id}         → single product
POST    /api/v1/products              → create (admin only)
PUT     /api/v1/products/{id}         → update (admin only)
DELETE  /api/v1/products/{id}         → soft-delete (admin only)
GET     /api/v1/products/search?q=    → search
GET     /api/v1/products?category=    → filter by category
GET     /api/v1/products?sort=price   → sort

GET     /api/v1/cart                  → get user's cart
POST    /api/v1/cart/items            → add item
PUT     /api/v1/cart/items/{id}       → update quantity
DELETE  /api/v1/cart/items/{id}       → remove item

POST    /api/v1/orders                → place order
GET     /api/v1/orders                → user's orders
GET     /api/v1/orders/{id}           → order detail
PUT     /api/v1/orders/{id}/cancel    → cancel
```

**Little things — API design:**

- Always version your API: `/api/v1/...`. Changing it later without versioning breaks clients.
- Return consistent error shapes. Every error response should have the same fields.
- Use HTTP status codes correctly: 200 OK, 201 Created, 204 No Content, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict, 500 Internal Server Error.
- Always paginate list endpoints. Never return all records.

### Step 1.8 — Pagination & Search

```java
// Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAllByActiveTrue(Pageable pageable);
    Page<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);
    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);
}

// Controller
@GetMapping
public ResponseEntity<Page<ProductDTO>> getProducts(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "createdAt") String sortBy,
    @RequestParam(required = false) String search
) {
    return ResponseEntity.ok(productService.getProducts(page, size, sortBy, search));
}
```

### Step 1.9 — application.properties Setup

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/ecomdb?useSSL=false&serverTimezone=UTC
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# Logging
logging.level.com.yourname.ecom=DEBUG
logging.level.org.hibernate.SQL=WARN

# Server
server.port=8080
server.error.include-message=always

# Pagination defaults
spring.data.web.pageable.default-page-size=10
spring.data.web.pageable.max-page-size=50
```

**Little things — Configuration:**

- Use `ddl-auto=validate` in production, `update` only during active development, never `create-drop`.
- Externalize all secrets using environment variables: `${DB_PASSWORD}`.
- Keep `show-sql=false` in production — it floods logs and hurts performance.

---

## Phase 2 — Frontend (React / Next.js)

### Step 2.1 — Choose Your Frontend

| Feature          | React (Vite)        | Next.js                     |
| ---------------- | ------------------- | --------------------------- |
| Setup complexity | Simple              | Medium                      |
| SEO              | Poor (CSR)          | Excellent (SSR/SSG)         |
| Routing          | React Router        | File-based                  |
| API calls        | Axios / React Query | Server Actions / fetch      |
| When to use      | Admin panels, SPAs  | Customer-facing storefronts |

**Recommendation:** Use Next.js for the storefront (SEO matters for products), React for admin dashboard.

### Step 2.2 — Project Structure (Next.js)

```
frontend/
├── app/                        # App Router (Next.js 13+)
│   ├── (store)/                # Route group: customer-facing
│   │   ├── page.tsx            # Home
│   │   ├── products/
│   │   │   ├── page.tsx        # Product listing
│   │   │   └── [id]/page.tsx   # Product detail
│   │   ├── cart/page.tsx
│   │   └── checkout/page.tsx
│   ├── (admin)/                # Route group: admin
│   │   ├── dashboard/page.tsx
│   │   └── products/page.tsx
│   └── api/                    # Next.js API routes (proxy layer)
├── components/
│   ├── ui/                     # Generic: Button, Input, Modal
│   └── store/                  # Domain: ProductCard, CartItem
├── lib/
│   ├── api.ts                  # Axios instance + interceptors
│   └── types.ts                # TypeScript interfaces
├── store/                      # Zustand / Redux state
└── hooks/                      # Custom React hooks
```

### Step 2.3 — API Client Setup

```typescript
// lib/api.ts
import axios from "axios";

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api/v1",
  timeout: 10000,
  headers: { "Content-Type": "application/json" },
});

// Attach JWT token to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Handle 401 globally (redirect to login)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("token");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  },
);

export default api;
```

### Step 2.4 — State Management

Use **Zustand** (simpler than Redux for most ecommerce apps):

```typescript
// store/cartStore.ts
import { create } from "zustand";
import { persist } from "zustand/middleware";

interface CartStore {
  items: CartItem[];
  addItem: (product: Product, quantity: number) => void;
  removeItem: (productId: number) => void;
  updateQuantity: (productId: number, quantity: number) => void;
  clearCart: () => void;
  totalAmount: () => number;
}

export const useCartStore = create<CartStore>()(
  persist(
    (set, get) => ({
      items: [],
      addItem: (product, quantity) =>
        set((state) => {
          const existing = state.items.find((i) => i.product.id === product.id);
          if (existing) {
            return {
              items: state.items.map((i) =>
                i.product.id === product.id
                  ? { ...i, quantity: i.quantity + quantity }
                  : i,
              ),
            };
          }
          return { items: [...state.items, { product, quantity }] };
        }),
      removeItem: (productId) =>
        set((state) => ({
          items: state.items.filter((i) => i.product.id !== productId),
        })),
      updateQuantity: (productId, quantity) =>
        set((state) => ({
          items: state.items.map((i) =>
            i.product.id === productId ? { ...i, quantity } : i,
          ),
        })),
      clearCart: () => set({ items: [] }),
      totalAmount: () =>
        get().items.reduce(
          (sum, item) => sum + item.product.price * item.quantity,
          0,
        ),
    }),
    { name: "cart-storage" },
  ),
);
```

### Step 2.5 — Little Things — Frontend

- Always show loading skeletons, not spinners. Skeletons feel faster.
- Debounce search input (300ms delay) before making API calls.
- Handle empty states: "No products found", "Your cart is empty" — with a helpful action.
- Optimistic UI for cart: update UI immediately, rollback on API failure.
- Use `React Query` (`@tanstack/react-query`) for server state — it handles caching, background refetch, and error states automatically.
- Never store sensitive data in `localStorage`. JWTs are okay. Passwords are not.
- Add `loading` and `error` states to every async operation.
- Use `TypeScript` from day one. Type your API responses.

---

## Phase 3 — Security & Authentication

### Step 3.1 — JWT Authentication Flow

```
1. User sends POST /api/v1/auth/login  { email, password }
2. Server validates credentials
3. Server generates JWT token (access: 15min, refresh: 7days)
4. Client stores access token in memory, refresh token in httpOnly cookie
5. Every subsequent request has: Authorization: Bearer <access_token>
6. When access token expires, client uses refresh token to get a new one
7. On logout, invalidate refresh token
```

### Step 3.2 — Dependencies

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
```

### Step 3.3 — Security Config

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // Always BCrypt. Never MD5 or SHA.
    }
}
```

### Step 3.4 — Role-Based Access Control

```java
public enum Role {
    ROLE_USER,
    ROLE_ADMIN,
    ROLE_SELLER    // if multi-vendor
}

// On controller methods
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/products/{id}")
public ResponseEntity<Void> deleteProduct(@PathVariable Long id) { ... }

@PreAuthorize("hasRole('USER') and #userId == authentication.principal.id")
@GetMapping("/users/{userId}/orders")
public ResponseEntity<List<OrderDTO>> getUserOrders(@PathVariable Long userId) { ... }
```

### Step 3.5 — Little Things — Security

- Never store plain-text passwords. Always BCrypt with strength 10-12.
- Validate and sanitize all inputs server-side, even if validated on the frontend.
- Rate-limit your `/auth/login` endpoint (max 5 attempts per IP per 15 minutes) to prevent brute force.
- Set `httpOnly`, `Secure`, and `SameSite=Strict` on cookies.
- Add CORS configuration explicitly — don't open it to `*` in production.
- Never put the JWT secret key in your source code. Use environment variables.
- Add request logging (without logging passwords or tokens).

---

## Phase 4 — Advanced Features

### Step 4.1 — Product Search (Full-Text)

For simple search, JPA `LIKE` queries work. For production-grade search, use Elasticsearch or at least MySQL Full-Text Search.

```java
// MySQL Full-Text Search
@Query("SELECT p FROM Product p WHERE MATCH(p.name, p.description) AGAINST (:query IN BOOLEAN MODE)")
Page<Product> fullTextSearch(@Param("query") String query, Pageable pageable);

// Or simple LIKE for starting out
Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
    String name, String description, Pageable pageable
);
```

### Step 4.2 — Redis Caching

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        return RedisCacheManager.create(factory);
    }
}

// Usage
@Cacheable(value = "products", key = "#id")
public ProductDTO getProductById(Long id) { ... }

@CacheEvict(value = "products", key = "#id")
public ProductDTO updateProduct(Long id, UpdateProductRequest request) { ... }
```

```yaml
# docker-compose.yml — add Redis
redis:
  image: redis:7-alpine
  ports:
    - "6379:6379"
```

### Step 4.3 — Payment Integration (Razorpay / Stripe)

**Flow:**

```
1. User clicks "Place Order"
2. Backend creates order with status PENDING
3. Backend creates payment intent via Stripe API
4. Frontend receives client_secret
5. Frontend renders Stripe Elements (card form)
6. Stripe processes payment and calls your webhook
7. Webhook handler updates order status → PAID
8. Send order confirmation email
```

```java
@PostMapping("/orders/{orderId}/payment")
public ResponseEntity<PaymentIntentResponse> createPaymentIntent(@PathVariable Long orderId) {
    Order order = orderService.getOrder(orderId);
    PaymentIntent intent = PaymentIntent.create(
        PaymentIntentCreateParams.builder()
            .setAmount(order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue())
            .setCurrency("inr")
            .setMetadata(Map.of("orderId", orderId.toString()))
            .build()
    );
    return ResponseEntity.ok(new PaymentIntentResponse(intent.getClientSecret()));
}

// Webhook — ALWAYS verify webhook signature
@PostMapping("/webhooks/stripe")
public ResponseEntity<String> handleStripeWebhook(
    @RequestBody String payload,
    @RequestHeader("Stripe-Signature") String sigHeader
) {
    Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
    if ("payment_intent.succeeded".equals(event.getType())) {
        // Update order to PAID
    }
    return ResponseEntity.ok("Received");
}
```

### Step 4.4 — File Upload (Product Images)

**Local (development):**

```java
@PostMapping("/products/{id}/image")
public ResponseEntity<String> uploadImage(@PathVariable Long id,
                                          @RequestParam("file") MultipartFile file) {
    String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
    Path uploadPath = Paths.get("uploads/" + filename);
    Files.copy(file.getInputStream(), uploadPath, StandardCopyOption.REPLACE_EXISTING);
    String imageUrl = "/uploads/" + filename;
    productService.updateImageUrl(id, imageUrl);
    return ResponseEntity.ok(imageUrl);
}
```

**Production:** Use AWS S3 or Cloudinary instead of local storage.

### Step 4.5 — Email Notifications

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

```java
@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Async  // Don't block the main thread
    public void sendOrderConfirmation(Order order) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(order.getUser().getEmail());
        message.setSubject("Order Confirmed — #" + order.getId());
        message.setText("Your order has been placed. Total: ₹" + order.getTotalAmount());
        mailSender.send(message);
    }
}
```

### Step 4.6 — Little Things — Features

- Always confirm stock availability when placing an order, not just when adding to cart.
- Decrement stock atomically using a database lock or `@Version` for optimistic locking.
- Price stored in the `OrderItem` at time of purchase — never recalculate from current product price.
- Cart items should show "Only X left" when stock is low.
- Use `@Async` for emails so they don't slow down the order placement response.
- Send a webhook response immediately (HTTP 200) before processing — Stripe retries if you're slow.
- Validate file type and size on upload (only jpg/png/webp, max 5MB).

---

## Phase 5 — Microservices Architecture

> Only move here after fully building the monolith. Microservices solve scale problems. Don't create scale problems to fix them.

### Step 5.1 — When to Split

Split a service when:

- It needs to scale independently (payment service handles 10x more load than others)
- It has a different deployment cycle
- It needs a different database (product search → Elasticsearch)
- Different teams own it

### Step 5.2 — Service Breakdown

```
api-gateway          → Routes all requests, handles auth at the edge
service-discovery    → Eureka: services register themselves
config-server        → Centralized configuration
─────────────────────────────────────────────────
user-service         → Register, login, profile   [MySQL]
product-service      → Products, categories        [MySQL or Elasticsearch]
inventory-service    → Stock levels                [MySQL]
cart-service         → Shopping cart               [Redis]
order-service        → Orders, order history       [MySQL]
payment-service      → Stripe integration          [MySQL]
notification-service → Email, SMS                  [MongoDB or stateless]
```

### Step 5.3 — Dependencies

```xml
<!-- API Gateway -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>

<!-- Service Discovery (each service) -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>

<!-- Feign (inter-service HTTP calls) -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>

<!-- Resilience4j (circuit breaker) -->
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
</dependency>
```

### Step 5.4 — API Gateway Config

```yaml
# application.yml in api-gateway
spring:
  cloud:
    gateway:
      routes:
        - id: product-service
          uri: lb://product-service # lb:// = load balanced via Eureka
          predicates:
            - Path=/api/v1/products/**
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/v1/orders/**
          filters:
            - AuthenticationFilter # Custom filter: validate JWT here
```

### Step 5.5 — Feign Client (Inter-Service Calls)

```java
// In order-service, call product-service
@FeignClient(name = "product-service", fallback = ProductFallback.class)
public interface ProductClient {

    @GetMapping("/api/v1/products/{id}")
    ProductDTO getProduct(@PathVariable Long id);

    @PutMapping("/api/v1/products/{id}/stock/decrement")
    void decrementStock(@PathVariable Long id, @RequestParam int quantity);
}

// Fallback — what happens when product-service is down
@Component
public class ProductFallback implements ProductClient {
    @Override
    public ProductDTO getProduct(Long id) {
        return null;  // or throw a meaningful exception
    }
    @Override
    public void decrementStock(Long id, int quantity) {
        // Log the failure, the order will need to be retried
    }
}
```

### Step 5.6 — Circuit Breaker

```java
@CircuitBreaker(name = "product-service", fallbackMethod = "getProductFallback")
@Retry(name = "product-service")
public ProductDTO getProduct(Long id) {
    return productClient.getProduct(id);
}

public ProductDTO getProductFallback(Long id, Exception e) {
    log.error("Product service is down. Returning cached/default product for id: {}", id);
    return cachedProductService.getCached(id);
}
```

```yaml
# Resilience4j config
resilience4j:
  circuitbreaker:
    instances:
      product-service:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
  retry:
    instances:
      product-service:
        maxAttempts: 3
        waitDuration: 500ms
```

### Step 5.7 — Docker Compose for Microservices

```yaml
version: "3.8"
services:
  eureka:
    build: ./service-discovery
    ports:
      - "8761:8761"

  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    environment:
      EUREKA_URI: http://eureka:8761/eureka
    depends_on:
      - eureka

  product-service:
    build: ./product-service
    environment:
      DB_URL: jdbc:mysql://product-db:3306/productdb
      EUREKA_URI: http://eureka:8761/eureka
    depends_on:
      - product-db
      - eureka

  product-db:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: productdb
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD}

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
```

---

## Phase 6 — Messaging & Event-Driven (Kafka)

> Use Kafka when you need services to communicate without being directly coupled to each other.

### Step 6.1 — Why Kafka?

**Without Kafka (direct HTTP call):**

```
Order Service → calls → Inventory Service  (fails if inventory is down)
Order Service → calls → Notification Service  (order fails if email is slow)
```

**With Kafka (event-driven):**

```
Order Service → publishes "order.placed" event → Kafka Topic
Inventory Service subscribes → updates stock
Notification Service subscribes → sends email
(Order Service doesn't care if others are slow or down)
```

### Step 6.2 — Setup

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: ecommerce-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "com.yourname.ecom.*"
```

### Step 6.3 — Producer (Order Service)

```java
@Service
public class OrderEventProducer {

    @Autowired
    private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public void publishOrderPlaced(Order order) {
        OrderPlacedEvent event = OrderPlacedEvent.builder()
            .orderId(order.getId())
            .userId(order.getUser().getId())
            .items(order.getItems())
            .totalAmount(order.getTotalAmount())
            .timestamp(Instant.now())
            .build();

        kafkaTemplate.send("order.placed", String.valueOf(order.getId()), event);
        log.info("Published order.placed event for order: {}", order.getId());
    }
}
```

### Step 6.4 — Consumer (Notification Service)

```java
@Service
public class OrderEventConsumer {

    @Autowired
    private EmailService emailService;

    @KafkaListener(topics = "order.placed", groupId = "notification-group")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("Received order.placed event: {}", event.getOrderId());
        try {
            emailService.sendOrderConfirmation(event);
        } catch (Exception e) {
            log.error("Failed to send email for order: {}", event.getOrderId(), e);
            // Consider dead letter topic for failed messages
        }
    }
}
```

### Step 6.5 — Kafka Topics to Create

```
order.placed          → consumed by: inventory, notification, analytics
order.cancelled       → consumed by: inventory, payment, notification
payment.completed     → consumed by: order, notification
payment.failed        → consumed by: order, notification
inventory.low-stock   → consumed by: admin-notification
user.registered       → consumed by: notification (welcome email)
```

### Step 6.6 — Docker Compose for Kafka

```yaml
zookeeper:
  image: confluentinc/cp-zookeeper:7.4.0
  environment:
    ZOOKEEPER_CLIENT_PORT: 2181

kafka:
  image: confluentinc/cp-kafka:7.4.0
  ports:
    - "9092:9092"
  environment:
    KAFKA_BROKER_ID: 1
    KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
    KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
  depends_on:
    - zookeeper
```

---

## Phase 7 — Observability & Monitoring

> If you can't see what your system is doing, you can't fix it when it breaks in production.

### Step 7.1 — Structured Logging

```java
// Use SLF4J + Logback. Never use System.out.println().
private static final Logger log = LoggerFactory.getLogger(OrderService.class);

// Add context to every log
log.info("Order placed successfully. orderId={}, userId={}, amount={}",
         order.getId(), user.getId(), order.getTotalAmount());

// Log errors with stack trace
log.error("Payment failed for orderId={}. reason={}", orderId, e.getMessage(), e);
```

### Step 7.2 — Distributed Tracing (Zipkin / Micrometer)

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

```properties
management.tracing.sampling.probability=1.0
management.zipkin.tracing.endpoint=http://zipkin:9411/api/v2/spans
```

Zipkin adds a `traceId` to every request. You can trace a single HTTP request through all microservices.

### Step 7.3 — Metrics (Prometheus + Grafana)

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

```properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when-authorized
```

**Key metrics to monitor:**

- HTTP request rate and latency per endpoint
- Error rate (4xx, 5xx)
- JVM heap usage
- Database connection pool usage
- Kafka consumer lag
- Order placement rate (business metric)

### Step 7.4 — Health Checks

```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        try {
            // ping database
            return Health.up().withDetail("database", "reachable").build();
        } catch (Exception e) {
            return Health.down().withDetail("error", e.getMessage()).build();
        }
    }
}
```

```
GET /actuator/health         → {"status": "UP"}
GET /actuator/health/db      → database status
GET /actuator/metrics        → all metrics
GET /actuator/prometheus     → Prometheus scrape endpoint
```

---

## Phase 8 — Kubernetes & Cloud Deployment

### Step 8.1 — Dockerize Each Service

```dockerfile
# Dockerfile for Spring Boot
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Multi-stage build (smaller image):**

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Step 8.2 — Kubernetes Deployment

```yaml
# product-service-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: product-service
spec:
  replicas: 2 # Two instances for availability
  selector:
    matchLabels:
      app: product-service
  template:
    metadata:
      labels:
        app: product-service
    spec:
      containers:
        - name: product-service
          image: yourdockerhub/product-service:latest
          ports:
            - containerPort: 8080
          env:
            - name: DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: db-credentials
                  key: password
          resources:
            requests:
              memory: "256Mi"
              cpu: "250m"
            limits:
              memory: "512Mi"
              cpu: "500m"
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 60
```

### Step 8.3 — CI/CD Pipeline (GitHub Actions)

```yaml
# .github/workflows/deploy.yml
name: Build and Deploy

on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: "17"
          distribution: "temurin"

      - name: Run tests
        run: mvn test

      - name: Build Docker image
        run: docker build -t ${{ secrets.DOCKERHUB_USERNAME }}/product-service:${{ github.sha }} .

      - name: Push to DockerHub
        run: |
          echo ${{ secrets.DOCKERHUB_TOKEN }} | docker login -u ${{ secrets.DOCKERHUB_USERNAME }} --password-stdin
          docker push ${{ secrets.DOCKERHUB_USERNAME }}/product-service:${{ github.sha }}

      - name: Deploy to Kubernetes
        run: |
          kubectl set image deployment/product-service \
            product-service=${{ secrets.DOCKERHUB_USERNAME }}/product-service:${{ github.sha }}
```

---

## Little Things That Matter

> These are the details that separate a student project from a production system.

### Code Quality

- Write unit tests for every service method (use JUnit 5 + Mockito)
- Write integration tests for every controller (use `@SpringBootTest` + `MockMvc`)
- Aim for 70%+ test coverage on service layer
- Use `@Validated` on DTOs and `@Valid` on controller params
- Use `Optional<T>` return types in repositories, never return `null`
- Follow Java naming conventions: `camelCase` for variables, `PascalCase` for classes
- Keep controllers thin: one method should do one thing (validate → delegate → respond)

### Database

- Add database indexes on columns you filter by: `email`, `category_id`, `order_status`
- Use database migrations (Flyway or Liquibase) — never rely on `ddl-auto=update` in production
- Always run queries through the service layer, never raw SQL from controllers
- Use `@Transactional` on service methods that do multiple DB operations
- Avoid N+1 queries: use JOIN FETCH in JPQL for related entities you always need

### API & HTTP

- Return `201 Created` with the created resource in the body, not just `200 OK`
- Use query params for filtering/sorting (`?category=electronics&sort=price`)
- Use path params for identifying a specific resource (`/products/{id}`)
- Document your API with Swagger/OpenAPI (`springdoc-openapi-starter-webmvc-ui`)
- Compress large responses with gzip (`server.compression.enabled=true`)
- Set reasonable timeouts on all external HTTP calls (Feign, RestTemplate)

### Performance

- Use connection pooling (HikariCP is built into Spring Boot — configure pool size)
- Cache frequently-read, rarely-changed data (product catalog, categories)
- Use `SELECT` with specific columns, not `SELECT *` in custom queries
- Enable lazy loading and avoid fetching associations unless needed
- Use pagination everywhere — never load all records from a table

### Security

- Input validation on every endpoint (use `@NotNull`, `@Size`, `@Email`, etc.)
- Sanitize inputs to prevent SQL injection (use JPA — never concatenate SQL strings)
- Prevent XSS: escape output in the frontend
- Use HTTPS in production — no exceptions
- Keep dependencies updated (use Dependabot or `mvn versions:check`)
- Log security events: failed logins, role escalation attempts, suspicious requests

### User Experience

- Show real-time stock count: "Only 3 left"
- Disable the "Place Order" button after click — prevent duplicate orders
- Show order confirmation with order ID immediately after payment
- Email confirmation should arrive within 2 minutes
- Product images should lazy load
- Add breadcrumbs: Home > Electronics > Laptops > MacBook

### Operations

- Have a rollback plan for every deployment
- Never run database migrations and application deployment at the same time
- Use feature flags to release features gradually
- Keep logs for 30 days minimum
- Set up alerts for: error rate > 1%, response time > 2s, memory > 80%
- Test your backup restore procedure — not just the backup

---

## Reference GitHub Repos

### Level 1 — Beginner (Start Here)

| Repo                                                                                                              | What to Study                                       |
| ----------------------------------------------------------------------------------------------------------------- | --------------------------------------------------- |
| [GattiHarishKumar/SpringBoot-Reactjs-Ecommerce](https://github.com/GattiHarishKumar/SpringBoot-Reactjs-Ecommerce) | Project structure, controller → service → repo flow |
| [RAVIVARMA0707/Ecommerce-Website](https://github.com/RAVIVARMA0707/Ecommerce-Website)                             | JPA entity design, CRUD operations                  |

### Level 2 — Intermediate

| Repo                                                                                                                | What to Study                                  |
| ------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------- |
| [ujjavaldesai07/spring-boot-react-ecommerce-app](https://github.com/ujjavaldesai07/spring-boot-react-ecommerce-app) | Redis, Stripe, OAuth, Docker, full feature set |
| [rahulsahay19/Java-React-FullStack](https://github.com/rahulsahay19/Java-React-FullStack)                           | Java 21 + Spring Boot 3.2 patterns             |
| [shivamsingha/ecommerce-spring](https://github.com/shivamsingha/ecommerce-spring)                                   | Next.js + Spring Boot + Keycloak auth          |

### Level 3 — Advanced Microservices

| Repo                                                                                                              | What to Study                                             |
| ----------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------- |
| [haphong463/springboot-kafka-microservices](https://github.com/haphong463/springboot-kafka-microservices)         | Full microservices: Eureka, Gateway, Kafka, Redis, Zipkin |
| [SelimHorri/ecommerce-microservice-backend-app](https://github.com/SelimHorri/ecommerce-microservice-backend-app) | 9 services, service boundaries, Kafka, K8s configs        |

### Level 4 — Expert (Kubernetes + Observability)

| Repo                                                                                                                                        | What to Study                                 |
| ------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------- |
| [benabbouosama/Spring-boot-microservices-E-commerce-project](https://github.com/benabbouosama/Spring-boot-microservices-E-commerce-project) | K8s deployment, Kibana logging, JWT security  |
| [ibatulanandjp/ecommerce-microservices](https://github.com/ibatulanandjp/ecommerce-microservices)                                           | Prometheus, Grafana, Micrometer, Resilience4j |

---

## Checklist Before Going Live

### Backend

- [ ] All endpoints have authentication/authorization
- [ ] All inputs validated with appropriate annotations
- [ ] Global exception handler returns consistent error format
- [ ] Pagination on all list endpoints
- [ ] No sensitive data in logs (passwords, tokens, card numbers)
- [ ] Environment variables for all secrets
- [ ] `ddl-auto=validate` (not `update` or `create-drop`)
- [ ] Database indexes on foreign keys and frequently-queried columns
- [ ] Flyway/Liquibase migrations in place
- [ ] Health check endpoint working
- [ ] CORS configured for production domain only

### Frontend

- [ ] All API errors handled gracefully with user-friendly messages
- [ ] Loading states on all async operations
- [ ] Empty states for all lists
- [ ] Form validation (client-side as UX, server-side for security)
- [ ] No console.log statements left in code
- [ ] Images have `alt` attributes
- [ ] Mobile responsive

### DevOps

- [ ] Docker images built and tagged with version
- [ ] Secrets stored in Kubernetes Secrets or cloud secret manager
- [ ] Readiness and liveness probes configured
- [ ] Resource limits set on all containers
- [ ] CI/CD pipeline runs tests before deploy
- [ ] Monitoring dashboards set up (Grafana)
- [ ] Alerts configured for errors and latency
- [ ] Backup for production database

### Business Logic

- [ ] Stock checked at order placement (not just cart addition)
- [ ] Price stored in OrderItem at purchase time
- [ ] Order confirmation email sent on successful payment
- [ ] Payment webhook verified (not just trusted client-side)
- [ ] Duplicate order prevention (idempotency)
- [ ] Refund flow tested end-to-end

---

_Last updated: June 2026 | Stack: Java 17+, Spring Boot 3.x, React 18 / Next.js 14, MySQL 8, Redis 7, Kafka 3.x, Docker, Kubernetes_
