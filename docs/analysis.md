### 1.1 Order Management

* **FR-01 Schedule Delivery:** The system must allow a customer to request a delivery by specifying:
    * Pick-up address ("From")
    * Destination address ("To")
    * Package weight
    * Requested delivery time (immediate or scheduled future time)
    * Maximum acceptable delivery duration
* **FR-02 Order ID Generation:** The system must generate a unique identifier for every new order to facilitate
  tracking.

### 1.2 Delivery Logistics & Drone Operations

* **FR-03 Drone Assignment:** The system must automatically identify an available drone and assign it to a confirmed
  order.
* **FR-04 Delivery Simulation:** The system must simulate the lifecycle of a delivery, transitioning the drone through
  specific states:
    * *Sleeping* (Idle/Available)
    * *InTransit* (Delivering package)
    * *Returning* (Returning to base after delivery)
* **FR-05 Asynchronous Processing:** The handover of orders from the Customer Service to the Delivery/Drone Service must
  happen asynchronously to prevent blocking the user interface.

### 1.3 Tracking & Visibility

* **FR-06 Real-time Status:** The system must provide an endpoint for users to query the status of their specific order
  using the Order ID.

---

## 2. Non-Functional Requirements (NFRs)

These requirements define the system attributes such as reliability, efficiency, and maintainability.

### 2.1 Architectural Constraints

* **NFR-01 Microservices Architecture:** The system must be composed of loosely coupled services (Customer, Delivery,
  Drone) that can be developed and deployed independently.
* **NFR-02 Domain-Driven Design (DDD):** Service boundaries must strictly follow the defined Bounded Contexts (Order
  Acquisition, Delivery Logistics, Drone Fleet).
* **NFR-03 Containerization:** All services must be containerized using Docker to ensure environment consistency.

### 2.2 Scalability & Availability

* **NFR-04 Horizontal Scalability:** The Drone Service must be capable of scaling independently of the Customer Service
  to handle high volumes of simultaneous drone simulations.
* **NFR-05 Resilience:** The system must use a message broker (RabbitMQ) to buffer requests. If the Drone Service is
  temporarily unavailable, orders must remain in the queue rather than being lost.

### 2.3 Performance & Reliability

* **NFR-06 Latency:** The Customer API must respond to order creation requests immediately (acknowledgment), processing
  the actual logistics in the background (Eventual Consistency).
* **NFR-07 Data Integrity:** Order IDs must be preserved exactly across all services during message transmission.

### 2.4 Interfaces

* **NFR-08 RESTful API:** External communication (Client to System) must use standard HTTP REST methods (POST, GET).