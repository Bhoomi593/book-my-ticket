package com.jsp.book.dto;


import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDto {
	@Size(min = 3, max = 25, message = "*Enter between 3 - 25 characters")
	private String name;
	@NotBlank(message = "*Email is required")
	@Email(message = "*Enter proper email")
	private String email;
	@DecimalMin(value = "6000000000", message = "*Enter proper mobile number")
	@DecimalMax(value = "9999999999", message = "*Enter proper mobile number")
	private Long mobile;
	@Pattern(regexp = "^.*(?=.{8,})(?=..*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$", message = "*Select stronger password")
	private String password;
	@Pattern(regexp = "^.*(?=.{8,})(?=..*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$", message = "*Select stronger password")
	private String confirmPassword;
	@AssertTrue(message = "*Select the checkbox in order to continue")
	private boolean terms;
}
