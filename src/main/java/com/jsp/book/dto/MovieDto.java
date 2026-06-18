package com.jsp.book.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MovieDto {
	
	@NotBlank(message="*it is required")
	private String name;
	
	@NotBlank(message="*it is required")
	private String language;
	
	@NotBlank(message="*it is required")
	private String genre;
	
	@NotNull(message="*it is required")
	private LocalTime duration;
	
	private MultipartFile image;
	
	@NotBlank(message="*it is required")
	private String trailerlink;
	
	@NotBlank(message="*it is required")
	private String description;
	
	@NotNull(message="*it is required")
	private LocalDate releaseDate;
	
	@NotBlank(message="*it is required")
	private String cast;

}
