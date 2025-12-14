package org.example;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/")
public class DroneController {
    private final HashMap<String, Drone> dispatchedDrones = new HashMap<>();

    public void attachDrone(final String droneId, final Drone drone) {
        this.dispatchedDrones.put(droneId, drone);
    }

    public void detachDrone(final String droneId) {
        this.dispatchedDrones.remove(droneId);
    }

    public HashMap<String, Drone> getCurrentDispatchedDrones() {
        return this.dispatchedDrones;
    }

    @GetMapping
    public ResponseEntity<String> createOrder(@RequestBody DroneRequest request) {
        if (dispatchedDrones.containsKey(request.orderId())) {
            return ResponseEntity.ok(dispatchedDrones.get(request.orderId()).toString());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Drone service is running");
    }
}