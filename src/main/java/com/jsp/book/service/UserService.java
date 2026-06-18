package com.jsp.book.service;

import java.io.IOException;

import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jsp.book.dto.LoginDto;
import com.jsp.book.dto.PasswordDto;
import com.jsp.book.dto.ScreenDto;
import com.jsp.book.dto.TheaterDto;
import com.jsp.book.dto.UserDto;

import ch.qos.logback.core.model.Model;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

public interface UserService {
	
	/*------------Common--------------*/
	
	String loadMain(ModelMap map);
	
	String login(LoginDto dto, RedirectAttributes attributes,HttpSession session);

	String logout(HttpSession session, RedirectAttributes attributes);
	
	/*---------------Registration & OTP------------------*/
	
	String register(UserDto userDto, BindingResult result,RedirectAttributes attributes);

	String submitOtp(int otp, String email, RedirectAttributes attributes);
	
	String resendOtp(String email, RedirectAttributes attributes);
	
	/*---------------Password-------------*/
	
	String forgotPassword(String email, RedirectAttributes attributes);
	
	String resetPassword(PasswordDto passwordDto, BindingResult result, RedirectAttributes attributes, ModelMap map);
	
	/*-------------User(Admin)---------------*/
	
	String manageUser(HttpSession session, RedirectAttributes attributes, ModelMap map);
	
	String blockUser(Long id, HttpSession session, RedirectAttributes attributes);
	
	String unBlockUser(Long id, HttpSession session, RedirectAttributes attributes);
	
	/*-----------------Theater---------------*/
	
	String manageTheater(ModelMap map, RedirectAttributes attributes, HttpSession session);
	
	String loadAddTheater(HttpSession session, RedirectAttributes attributes, TheaterDto theaterDto);
	
	String addTheater(HttpSession session, RedirectAttributes attributes, @Valid TheaterDto theaterDto, BindingResult result) throws IOException;

	String deleteTheater(Long id, HttpSession session, RedirectAttributes attributes);
	
	String editTheater(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map);
	
	String updateTheater(HttpSession session, RedirectAttributes attributes, @Valid TheaterDto theaterDto, @Valid BindingResult result, Long id);
	
	/*--------------Screen--------------*/

	String manageScreens(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map);

	String addScreen(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map, ScreenDto screenDto);

	String addScreen(ScreenDto screenDto, BindingResult result, HttpSession session, RedirectAttributes attributes);

	String editScreen(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map);

	String updateScreen(ScreenDto screenDto, Long id, BindingResult result, HttpSession session,
			RedirectAttributes attributes, ModelMap map);

	String deleteScreen(Long id, HttpSession session, RedirectAttributes attributes);

	/* ---------- Seats ---------- */

	String manageSeats(Long id, HttpSession session, ModelMap map, RedirectAttributes attributes);

	String addSeats(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map);
<<<<<<< HEAD:src/main/java/com/jsp/book/service/UserService.java
	
	String saveSeats(Long id, SeatLayoutForm seatLayoutForm, HttpSession session, RedirectAttributes attributes);
=======

	String saveSeats(Long id, SeatLayoutForm seatLayoutForm, HttpSession session, RedirectAttributes attributes);

	/* ---------- Movie ---------- */

	String manageMovies(HttpSession session, RedirectAttributes attributes, ModelMap map);

	String loadAddMovie(MovieDto movieDto, RedirectAttributes attributes, HttpSession session);

	String addMovie(MovieDto movieDto, BindingResult result, RedirectAttributes attributes, HttpSession session);

	String deleteMovie(Long id, HttpSession session, RedirectAttributes attributes);


>>>>>>> fc778ca6467b98d9bb139831bed3d04755a046e6:main/java/com/jsp/book/service/UserService.java
}
