package testmq.demo.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import testmq.demo.dto.request.event.EventRequestDTO;
import testmq.demo.dto.response.event.EventResponseDTO;
import testmq.demo.service.service.EventService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/v1/event")
public class EventController {
    EventService eventService;

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getById(@PathVariable UUID id) {
        EventResponseDTO res = eventService.getById(id);
        return ResponseEntity.ok().body(res);
    }

    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getAll() {
        return ResponseEntity.ok().body(eventService.getAll());
    }

    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(@RequestBody EventRequestDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id){
        eventService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
