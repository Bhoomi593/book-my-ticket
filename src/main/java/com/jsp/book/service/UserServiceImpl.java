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
import com.jsp.book.entity.User;
import com.jsp.book.repository.UserRepository;
import com.jsp.book.util.AES;
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
		if (!userDto.getPassword().equals(userDto.getConfirmPassword()))
			result.rejectValue("confirmPassword", "error.confirmPassword","*password and confirmPassword should be same");
		if(userRepository.existByEmail(userDto.getEmail()))
			result.rejectValue("email", "error.email","*Email should be unique");
		if(userRepository.existsByMobile(userDto.getMobile()))
			result.rejectValue("mobile", "error.mobile","*Mobile number should be unique");
		
		if(result.hasErrors())
		return "register.html";
		
		else {
			int otp = random.nextInt(100000,1000000);
			emailHelper.sendOtp(otp, userDto.getName(), userDto.getEmail());
			redisService.saveUserDto(userDto.getEmail(), userDto);
			redisService.saveOtp(userDto.getEmail(), otp);
			attributes.addFlashAttribute("pass","otp sent success");
			attributes.addFlashAttribute("email", userDto.getEmail());
			return "redirect:/otp";
		}
	}
	
	@Override
	public String login(LoginDto dto, RedirectAttributes attributes, HttpSession session) {
		User user= userRepository.findByEmail(dto.getEmail());
		if (user==null) {
			attributes.addFlashAttribute("fail", "Invalid email");
			return "redirect:/login";
		} else {
			if (AES.decrypt(user.getPassword()).equals(dto.getPassword())) {
				if (user.isBlocked()) {
					attributes.addFlashAttribute("fail", "account blocked!, contact admin");
					return "redirect:/login";
				}
				session.setAttribute("user", user);
				attributes.addFlashAttribute("pass", "Login success");
				return "redirect:/main";
			} else {
				attributes.addFlashAttribute("fail", "Invalid password");
				return "redirect:/login";
			}
		}
	}
	
	@Override
	public String logout(HttpSession session, RedirectAttributes attributes) {
		session.removeAttribute("user");
		attributes.addFlashAttribute("pass", "Logout success");
		return "redirect:/main";
	}
	
	@Override
	public String submitOtp(int otp, String email, RedirectAttributes attributes) {
		UserDto dto=redisService.getDtoByEmail(email);
		if (dto==null) {
			attributes.addFlashAttribute("fail", "Time out Try again creating new account");
			return "redirect:/register";
		} else {
			int exOtp =redisService.getOtpByEmail(email);
			if (exOtp==0) {
				attributes.addFlashAttribute("fail", "OTP is expired, Resend OTP again");
				attributes.addFlashAttribute("email",email);
				return "redirect:/otp";
			} else {
				if (otp==exOtp) {
					User user=new User(null, dto.getName(),dto.getEmail(),dto.getMobile(),AES.encrypt(dto.getConfirmPassword()),"USER",false);
					userRepository.save(user);
					attributes.addFlashAttribute("pass", "Account registered success");
					return "redirect:/main";
				} else {
					attributes.addFlashAttribute("fail", "Invalid OTP Try again");
					attributes.addFlashAttribute("email",email);
					return "redirect:/otp";
				}
			}
		}
	}
	
	@Override
	public String resendOtp(String email, RedirectAttributes attributes) {
		UserDto dto=redisService.getDtoByEmail(email);
		if(dto==null) {
			attributes.addFlashAttribute("fail","Timeout try agaun creating new account");
			return "redirect:/register";
		} else {
			int otp = random.nextInt(100000,1000000);
			emailHelper.sendOtp(otp, dto.getName(), dto.getEmail());
			redisService.saveUserDto(dto.getEmail(), dto);
			redisService.saveOtp(dto.getEmail(), otp);
			attributes.addFlashAttribute("pass","otp sent success");
			attributes.addFlashAttribute("email", dto.getEmail());
			return "redirect:/otp";
		}
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

	@Override
	public String loadMain(ModelMap map) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
