package com.monew.monew_server.domain.notification.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.monew.monew_server.domain.notification.dto.NotificationDto;
import com.monew.monew_server.domain.notification.entity.Notification;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

	@Mapping(target = "userId", source = "user.id")
	NotificationDto toDto(Notification notification);
}
