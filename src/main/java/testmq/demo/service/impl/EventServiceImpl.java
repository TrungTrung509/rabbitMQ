package testmq.demo.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import testmq.demo.common.exception.BadRequestException;
import testmq.demo.dto.request.event.EventRequestDTO;
import testmq.demo.dto.response.event.EventResponseDTO;
import testmq.demo.entity.Event;
import testmq.demo.repository.EventRepository;
import testmq.demo.service.EventService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventServiceImpl implements EventService {
    EventRepository eventRepository;


    public List<EventResponseDTO> getAll() {
        return eventRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public EventResponseDTO createEvent(EventRequestDTO request) {
        Event e = Event.builder()
                .name(request.getName())
                .description(request.getDescription())
                .number(request.getNumber())
                .startDate(request.getStartDate())
                .build();

        Event event = eventRepository.save(e);
        return toResponse(event);
    }

    @Override
    public EventResponseDTO getById(UUID id) {
        Event event = eventRepository.findByEventId(id).orElseThrow(() -> new BadRequestException("none"));
        return toResponse(event);
    }

    @Override
    public void delete(UUID id) {
        Event event = eventRepository.findByEventId(id).orElseThrow(() -> new BadRequestException("none"));
        eventRepository.delete(event);
    }

    private EventResponseDTO toResponse(Event event) {
        return EventResponseDTO.builder()
                .evenId(event.getEventId())
                .name(event.getName())
                .description(event.getDescription())
                .number(event.getNumber())
                .startDate(event.getStartDate())
                .build();
    }
}
