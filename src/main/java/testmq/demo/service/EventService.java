package testmq.demo.service;


import testmq.demo.dto.request.event.EventRequestDTO;
import testmq.demo.dto.response.event.EventResponseDTO;

import java.util.List;
import java.util.UUID;

public interface EventService {
        EventResponseDTO createEvent(EventRequestDTO request);
        EventResponseDTO getById(UUID id);
        List<EventResponseDTO> getAll();
        void delete(UUID id);
}
