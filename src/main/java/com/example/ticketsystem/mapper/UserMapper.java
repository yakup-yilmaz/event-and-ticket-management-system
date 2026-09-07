package com.example.ticketsystem.mapper;

import com.example.ticketsystem.dto.UserCreateRequest;
import com.example.ticketsystem.dto.UserResponse;
import com.example.ticketsystem.entity.User;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * password ve role MapStruct ile entity'ye yazılmaz:
 * - password → serviste BCrypt encode edilir
 * - role → kayıtta sunucu tarafında USER atanır
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(UserCreateRequest request);

    UserResponse toResponse(User entity);

    List<UserResponse> toResponseList(List<User> users);
}
