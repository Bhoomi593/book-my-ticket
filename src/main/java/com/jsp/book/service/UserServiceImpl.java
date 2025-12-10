package com.jsp.book.service;

import java.io.IOException;
import java.security.SecureRandom;

import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jsp.book.dto.LoginDto;
import com.jsp.book.dto.PasswordDto;
import com.jsp.book.dto.ScreenDto;
import com.jsp.book.dto.TheaterDto;
import com.jsp.book.dto.UserDto;
import com.jsp.book.repository.UserRepository;
import com.jsp.book.util.EmailHelper;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	
	private final UserRepository userRepository;
	private final SecureRandom random;
	private final EmailHelper emailHelper;
	private final RedisService redisService;
	
	
	@Override
	public String register(UserDto userDto, BindingResult result, RedirectAttributes attributes) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String login(LoginDto dto, RedirectAttributes attributes, HttpSession session) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String logout(HttpSession session, RedirectAttributes attributes) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String submitOtp(int otp, String email, RedirectAttributes attributes) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String resendOtp(String email, RedirectAttributes attributes) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String forgetPassword(String email, RedirectAttributes attributes) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String resetPassword(PasswordDto passwordDto, BindingResult result, RedirectAttributes attributes,
			ModelMap map) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String manageUser(HttpSession session, RedirectAttributes attributes, ModelMap map) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String blockUser(Long id, HttpSession session, RedirectAttributes attributes) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String unblockUser(Long id, HttpSession session, RedirectAttributes attributes) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String manageTheater(ModelMap map, RedirectAttributes attributes, HttpSession session) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String loadAddTheater(HttpSession session, RedirectAttributes attributes, TheaterDto theaterDto) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String addTheater(HttpSession session, RedirectAttributes attributes, @Valid TheaterDto theaterDto,
			BindingResult result) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String deleteTheater(Long id, HttpSession session, RedirectAttributes attributes) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String editTheater(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String updateTheater(Long id, HttpSession session, RedirectAttributes attributes,
			@Valid TheaterDto theaterDto, BindingResult result) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String manageScreen(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String addScreen(Long id, HttpSession session, RedirectAttributes attributes, @Valid ScreenDto screenDto,
			BindingResult result, ModelMap map) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String deleteScreen(Long id, HttpSession session, RedirectAttributes attributes) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String editScreen(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String updateScreen(Long id, HttpSession session, RedirectAttributes attributes, @Valid ScreenDto screenDto,
			BindingResult result, ModelMap map) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String manageSeats(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String addSeats(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
