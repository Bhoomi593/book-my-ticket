package com.jsp.book.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jsp.book.dto.LoginDto;
import com.jsp.book.dto.PasswordDto;
import com.jsp.book.dto.UserDto;
import com.jsp.book.repository.UserRepository;
import com.jsp.book.util.EmailHelper;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	
	private final UserRepository userRepository;
	private final SecureRandom random;
	private final EmailHelper emailHelper;
	private final RedisService rediceService;
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

	

}
