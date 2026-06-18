package com.jsp.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScreenDto {

	@NotBlank(message="*Screen name is required")
	private String name;
	
	@NotBlank(message="*Screen type is required")
	private String type;
	
	@NotNull(message="*Theater is required")
	private Long theaterId;
}
