### Domain

The system manages the delivery of packages using drones, involving the following main entities:

- **Customer**: Requests a package delivery.
- **Package**: The item to be delivered.
- **Delivery**: The process and record of transporting a package.
- **Drone**: The vehicle responsible for the delivery.

### Domain Model

![Domain](resource/image1.jpg)

**Description:**

- A Customer creates a Delivery for a Package.
- A Delivery is assigned to a Drone.
- The Drone transports the Package from the source to the destination.

### Ubiquitous language

- **Customer**: A user who requests a delivery.
- **Package**: The object to be delivered.
- **Delivery**: The process and record of moving a package.
- **Drone**: The autonomous vehicle that performs the delivery.

### Bounded context

- **Order Management Context** : Managing customer orders and order lifecycle
- **Delivery Coordination Context** : Coordinating deliveries between orders and drones
- **Drone Context** : Managing drone fleet and individual drone operations

| Bounded Context                | Responsibility                                                                                                     | Mapped Service     |
|:-------------------------------|:-------------------------------------------------------------------------------------------------------------------|:-------------------|
| **Order Acquisition Context**  | Handles the interaction with the customer, validates order requests, and captures the intent to ship a package.    | `customer-service` |
| **Delivery Logistics Context** | Orchestrates the fulfillment of an order. It acts as the "brain" that decides how and when a delivery is executed. | `delivery-service` |
| **Drone Fleet Context**        | Manages the physical resources (drones), their lifecycle (state changes), and physical movement simulation.        | `drone-service`    |

### Context Map

![Context Map](resource/context_map.png)

### Example Order Flow

1. Customer submits a delivery order.
2. System creates a Delivery and assigns a Drone.
3. Drone picks up and delivers the Package.
4. Delivery status is updated and available to the Customer.

---

*See `design.md` for architecture and sequence diagrams, and `api.md` for endpoint details.*