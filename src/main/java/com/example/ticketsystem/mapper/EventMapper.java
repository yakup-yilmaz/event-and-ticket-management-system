package com.example.ticketsystem.mapper;

import com.example.ticketsystem.dto.EventCreateRequest;
import com.example.ticketsystem.dto.EventResponse;
import com.example.ticketsystem.entity.Event;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 *
 * @author Yakup Yılmaz
 */

@Mapper(componentModel = "spring",unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface EventMapper 
{
    @org.mapstruct.Mapping(target = "id", ignore = true)
    @org.mapstruct.Mapping(target = "availableSeats", ignore = true)
    @org.mapstruct.Mapping(target = "status", ignore = true)
    @org.mapstruct.Mapping(target = "version", ignore = true)
    @org.mapstruct.Mapping(target = "createdAt", ignore = true)
    @org.mapstruct.Mapping(target = "updatedAt", ignore = true)
    Event toEntity(EventCreateRequest request);
    
    EventResponse toResponse(Event entity);
    
    List<EventResponse> toResponseList(List<Event> entityList);
}
