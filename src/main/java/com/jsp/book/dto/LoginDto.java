package com.jsp.book.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto {
	
	@NotBlank(message= "*Email is required")
	@Email(message="*Enter proper email")
	private String email;
	
	@NotBlank(message="*password is required")
	private String password;
}
