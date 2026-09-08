package com.example.ticketsystem.mapper;

import com.example.ticketsystem.dto.TicketResponse;
import com.example.ticketsystem.entity.Ticket;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 *
 * @author Yakup Yılmaz
 */
@Mapper(componentModel = "spring",unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface TicketMapper 
{

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", expression = "java(ticket.getUser().getFirstName() + \" \" + ticket.getUser().getLastName())")
    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventName", source = "event.name")
    @Mapping(target = "eventDate", source = "event.eventDate")
    @Mapping(target = "purchasedAt", source = "createdAt")
    TicketResponse toResponse(Ticket ticket);

    List<TicketResponse> toResponseList(List<Ticket> ticketList);
}
