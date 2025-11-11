package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/api/drones")
public class DroneController {
    private final HashMap<String, Drone> dispatchedDrones = new HashMap<>();

    public void attachDrone(final String droneId, final Drone drone) {
        this.dispatchedDrones.put(droneId, drone);
    }

    public void detachDrone(final String droneId) {
        this.dispatchedDrones.remove(droneId);
    }

    @GetMapping
    public ResponseEntity<String> createOrder(@RequestBody DroneRequest request) {
        if (dispatchedDrones.containsKey(request.orderId())) {
            return ResponseEntity.ok(dispatchedDrones.get(request.orderId()).toString());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}