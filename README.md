# Corner Shelf - mini e-commerce (Spring Boot + MySQL + plain JS)

A small full-stack project to practise Docker, debugging, and later ELK + ETL.

## Structure
```
ecommerce-mini/
  backend/    Spring Boot 3 (Java 21), JPA, MySQL, validation, global exception handler
  frontend/   Plain HTML/CSS/JS served by nginx
  docker-compose.yml   mysql + backend + frontend
```

## Features
- Product list (6 seeded products), add to cart, change quantity
- Checkout with name + email, stock check, order saved in a transaction
- Order history page

## API
| Method | URL | What it does |
|---|---|---|
| GET | /api/products | all products |
| GET | /api/products/{id} | one product (404 if missing) |
| POST | /api/products | add a product |
| POST | /api/orders | place order: `{customerName, customerEmail, items:[{productId, quantity}]}` |
| GET | /api/orders | order history, newest first |
| GET | /actuator/health | health check |

## Run with Docker (recommended)
```bash
docker compose up --build
```
- Frontend: http://localhost:3000
- Backend:  http://localhost:8080/api/products

Stop: `docker compose down` (add `-v` to also delete DB data).

## Run without Docker
1. Start MySQL locally (user `root`, password `root`) - the DB is created automatically.
2. `cd backend && mvn spring-boot:run`
3. Open `frontend/index.html` in the browser (or use VS Code Live Server).

## Docker debugging cheat sheet
```bash
docker compose ps -a                 # which container exited?
docker compose logs -f backend       # read the real error
docker exec -it shop-mysql mysql -uroot -p shopdb    # look inside the DB
docker inspect shop-backend          # env, network, ports
docker compose build --no-cache backend   # rebuild from scratch
docker compose config                # validate compose file
```

Common errors to try on purpose (break it, then fix it):
- Change `DB_URL` host from `mysql` to `localhost` -> backend can't connect. Why? (*Gujlish: container ni andar `localhost` etle te container pote, biji container nahi.*)
- Remove the `healthcheck`/`condition` -> backend starts before MySQL and crashes.
- Change the host port `8080` to something busy -> "port already allocated".
- Order more items than stock -> read the 400 message in the browser and in `docker compose logs`.

## Practice tasks (try first, ask for hints later)
1. Add `PUT /api/products/{id}` and `DELETE /api/products/{id}`.
2. Add pagination: `GET /api/products?page=0&size=6`.
3. Add Spring Security + JWT (register/login) and attach orders to the logged-in user.
4. Add an order status (PLACED, PAID, SHIPPED) and an endpoint to change it.
5. **ELK**: switch logging to JSON (logstash-logback-encoder), add `elasticsearch`, `logstash`, `kibana` to compose, then build a Kibana dashboard for errors per hour.
6. **ETL**: a Spring Batch job that reads `orders`, calculates daily sales per product, and writes to a `daily_sales` table (or Elasticsearch).
7. Split into microservices: product-service, order-service, gateway.
8. Deploy on AWS EC2 and change `frontend/config.js` to the EC2 URL.
