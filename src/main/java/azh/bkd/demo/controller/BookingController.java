package azh.bkd.demo.controller;

import azh.bkd.demo.model.*;
import azh.bkd.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/create")
    public Map<String, Object> createBooking(@RequestBody Map<String, Object> request) {
        Long roomId = Long.valueOf(request.get("roomId").toString());
        Long userId = Long.valueOf(request.get("userId").toString());
        java.time.LocalDateTime start = java.time.LocalDateTime.parse(request.get("startTime").toString());
        java.time.LocalDateTime end = java.time.LocalDateTime.parse(request.get("endTime").toString());
        String type = request.get("type").toString(); 

        Room room = roomRepository.findById(roomId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        List<Booking> conflicts = bookingRepository.findOverlappingBookings(roomId, start, end);
        
        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setUser(user);
        booking.setStartTime(start);
        booking.setEndTime(end);

        Map<String, Object> response = new HashMap<>();

        if (!conflicts.isEmpty()) {
            booking.setStatus("PENDING");
            booking.setConflictDesc("Conflict: Is time slot par pehle se class scheduled hai.");
            response.put("message", "Conflict detected! Booking status set to PENDING.");
        } else {
            booking.setStatus("OCCUPIED_" + type.toUpperCase());
            response.put("message", "Room booked successfully!");
        }

        bookingRepository.save(booking);
        response.put("success", true);
        response.put("bookingStatus", booking.getStatus());
        
        return response;
    }
}