package testmq.demo.service.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import testmq.demo.common.exception.BadRequestException;
import testmq.demo.dto.request.event.EventRequestDTO;
import testmq.demo.dto.response.event.EventResponseDTO;
import testmq.demo.entity.Event;
import testmq.demo.repository.EventRepository;
import testmq.demo.service.cache.RedisCacheService;
import testmq.demo.service.service.EventService;
import tools.jackson.core.type.TypeReference;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventServiceImpl implements EventService {
    EventRepository eventRepository;
    RedisCacheService cacheService;

    public List<EventResponseDTO> getAll() {
        List<EventResponseDTO> cache = cacheService.getList("event_list", new TypeReference<List<EventResponseDTO>>() {
        });

        if (cache != null)
            return cache;

        List<EventResponseDTO> list = eventRepository.findAll().stream()
                .map(this::toResponse)
                .toList();

        cacheService.set("event_list", list, Duration.ofMinutes(10));
        return list;
    }

    public EventResponseDTO createEvent(EventRequestDTO request) {

        Event e = Event.builder()
                .name(request.getName())
                .description(request.getDescription())
                .number(request.getNumber())
                .startDate(request.getStartDate())
                .build();

        Event event = eventRepository.save(e);
        cacheService.delete("event_list");
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
        cacheService.delete("event_list");
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
