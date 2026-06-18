package com.jsp.book.service;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Set;

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
import com.jsp.book.repository.ScreenRepository;
import com.jsp.book.repository.TheaterRepository;
import com.jsp.book.repository.UserRepository;
import com.jsp.book.util.AES;
import com.jsp.book.util.CloudinaryHelper;
import com.jsp.book.util.EmailHelper;
import com.jsp.book.util.QrHelper;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	
	/* ---------- Repositories ---------- */
	private final UserRepository userRepository;
	private final TheaterRepository theaterRepository;
	private final ScreenRepository screenRepository;
	private final MovieRepository movieRepository;
	private final ShowRepository showRepository;
	private final SeatRepository seatRepository;
	private final ShowSeatRepository showSeatRepository;
	private final TicketRepository ticketRepository;

	/* ---------- Helpers & Utilities ---------- */
	private final SecureRandom secureRandom;
	private final EmailHelper emailHelper;
	private final RedisService redisService;
	private final CloudinaryHelper cloudinaryHelper;
	private final QrHelper qrHelper;

	/* ---------- Razorpay Credentials ---------- */
	private static final String RAZORPAY_KEY = "rzp_test_RaPsJq0rZSFWD1";
	private static final String RAZORPAY_SECRET = "ZO0swacFGMgyE71JVvGuBOFP";

	
	
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
	public String forgotPassword(String email, RedirectAttributes attributes) {

		Optional<User> optionalUser = userRepository.findByEmail(email);

		// Email validation
		if (optionalUser.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Email");
			return "redirect:/forgot-password";
		}

		User user = optionalUser.get();

		int otp = secureRandom.nextInt(100000, 1_000_000);

		emailHelper.sendOtp(otp, user.getName(), email);
		redisService.saveOtp(email, otp);

		attributes.addFlashAttribute("pass", "Sent Success");
		attributes.addFlashAttribute("email", email);

		return "redirect:/reset-password";
	}

	@Override
	public String resetPassword(PasswordDto passwordDto, BindingResult result, RedirectAttributes attributes,
			ModelMap map) {

		// Validation errors (form-level)
		if (result.hasErrors()) {
			map.put("email", passwordDto.getEmail());
			return "reset-password.html";
		}

		Optional<User> optionalUser = userRepository.findByEmail(passwordDto.getEmail());

		// Invalid email
		if (optionalUser.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Email");
			return "redirect:/forgot-password";
		}

		int storedOtp = redisService.getOtp(passwordDto.getEmail());

		// OTP expired
		if (storedOtp == 0) {
			attributes.addFlashAttribute("fail", "OTP Expired, Resend Otp and Try Again");
			attributes.addFlashAttribute("email", passwordDto.getEmail());
			return "redirect:/reset-password";
		}

		// OTP mismatch
		if (passwordDto.getOtp() != storedOtp) {
			attributes.addFlashAttribute("fail", "Invalid OTP Try Again");
			attributes.addFlashAttribute("email", passwordDto.getEmail());
			return "redirect:/reset-password";
		}

		// OTP success → update password
		User user = optionalUser.get();
		user.setPassword(AES.encrypt(passwordDto.getPassword()));
		userRepository.save(user);

		attributes.addFlashAttribute("pass", "Password Reset Success");
		return "redirect:/main";
	}

	@Override
	public String manageUsers(HttpSession session, RedirectAttributes attributes, ModelMap map) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		List<User> users = userRepository.findByRole("USER");

		// No users found
		if (users.isEmpty()) {
			attributes.addFlashAttribute("fail", "No Users Registered Yet");
			return "redirect:/";
		}

		map.put("users", users);
		return "manage-users.html";
	}

	@Override
	public String blockUser(Long id, HttpSession session, RedirectAttributes attributes) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<User> optionalUser = userRepository.findById(id);

		// Target user not found
		if (optionalUser.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		User targetUser = optionalUser.get();
		targetUser.setBlocked(true);
		userRepository.save(targetUser);

		attributes.addFlashAttribute("pass", "Blocked Success");
		return "redirect:/manage-users";
	}

	@Override
	public String unBlockUser(Long id, HttpSession session, RedirectAttributes attributes) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<User> optionalUser = userRepository.findById(id);

		// Target user not found
		if (optionalUser.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		User targetUser = optionalUser.get();
		targetUser.setBlocked(false);
		userRepository.save(targetUser);

		attributes.addFlashAttribute("pass", "Un-Blocked Success");
		return "redirect:/manage-users";
	}

	private User getUserFromSession(HttpSession session) {
		return session != null ? (User) session.getAttribute("user") : null;
	}

	@Override
	public String manageTheater(ModelMap map, RedirectAttributes attributes, HttpSession session) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		List<Theater> theaters = theaterRepository.findAll();
		map.put("theaters", theaters);

		return "manage-theaters.html";
	}

	@Override
	public String loadAddTheater(HttpSession session, RedirectAttributes attributes, TheaterDto theaterDto) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		return "add-theater.html";
	}

	@Override
	public String addTheater(HttpSession session, RedirectAttributes attributes, @Valid TheaterDto theaterDto,
			BindingResult result) throws IOException {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		// Uniqueness check
		if (theaterRepository.existsByNameAndAddress(theaterDto.getName(), theaterDto.getAddress())) {

			result.rejectValue("name", "error.name", "* Theater Already Exists");
		}

		// Image validation
		MultipartFile image = theaterDto.getImage();
		if (image == null || image.isEmpty()) {
			result.rejectValue("image", "error.image", "* Image is Required");
		}

		// Validation failure
		if (result.hasErrors()) {
			return "add-theater.html";
		}

		Theater theater = new Theater();
		theater.setName(theaterDto.getName());
		theater.setAddress(theaterDto.getAddress());
		theater.setLocationLink(theaterDto.getLocationLink());
		theater.setImageLocation(cloudinaryHelper.getTheaterImageLink(image));

		theaterRepository.save(theater);

		attributes.addFlashAttribute("pass", "Theater Added Successfully");
		return "redirect:/manage-theaters";
	}

	@Override
	public String deleteTheater(Long id, HttpSession session, RedirectAttributes attributes) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Theater> optionalTheater = theaterRepository.findById(id);

		// Theater not found
		if (optionalTheater.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Theater theater = optionalTheater.get();

		// Screen dependency check
		if (theater.getScreenCount() != 0) {
			attributes.addFlashAttribute("fail", "First Remove The Screens to Remove Theater");
			return "redirect:/manage-theaters";
		}

		theaterRepository.delete(theater);
		attributes.addFlashAttribute("pass", "Theater Removed Success");

		return "redirect:/manage-theaters";
	}

	@Override
	public String editTheater(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Theater> optionalTheater = theaterRepository.findById(id);

		if (optionalTheater.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Theater");
			return "redirect:/manage-theaters";
		}

		Theater theater = optionalTheater.get();

		TheaterDto theaterDto = new TheaterDto(theater.getName(), theater.getAddress(), theater.getLocationLink(),
				null);

		map.put("id", theater.getId());
		map.put("imageLink", theater.getImageLocation());
		map.put("theaterDto", theaterDto);

		return "edit-theater.html";
	}

	@Override
	public String updateTheater(HttpSession session, RedirectAttributes attributes, @Valid TheaterDto theaterDto,
			BindingResult result, Long id) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Theater> optionalTheater = theaterRepository.findById(id);

		if (optionalTheater.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Theater");
			return "redirect:/manage-theaters";
		}

		Theater theater = optionalTheater.get();
		theater.setName(theaterDto.getName());
		theater.setAddress(theaterDto.getAddress());
		theater.setLocationLink(theaterDto.getLocationLink());

		MultipartFile image = theaterDto.getImage();

		// 🔥 Cloudinary logic (same as addTheater)
		if (image != null && !image.isEmpty()) {
			String imageUrl = cloudinaryHelper.getTheaterImageLink(image);
			theater.setImageLocation(imageUrl);
		}
		// else → keep existing image (no change needed)

		theaterRepository.save(theater);

		attributes.addFlashAttribute("pass", "Theater Updated Successfully");
		return "redirect:/manage-theaters";
	}

	@Override
	public String manageScreens(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Theater> optionalTheater = theaterRepository.findById(id);

		if (optionalTheater.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Theater");
			return "redirect:/manage-theaters";
		}

		Theater theater = optionalTheater.get();
		List<Screen> screens = screenRepository.findByTheater(theater);

		map.put("screens", screens);
		map.put("id", id);

		return "manage-screens.html";
	}

	@Override
	public String addScreen(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map,
			ScreenDto screenDto) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Theater> optionalTheater = theaterRepository.findById(id);

		if (optionalTheater.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Theater");
			return "redirect:/manage-theaters";
		}

		screenDto.setTheaterId(id);
		map.put("screenDto", screenDto);

		return "add-screen.html";
	}

	@Override
	public String addScreen(ScreenDto screenDto, BindingResult result, HttpSession session,
			RedirectAttributes attributes) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Theater> optionalTheater = theaterRepository.findById(screenDto.getTheaterId());

		if (optionalTheater.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Theater");
			return "redirect:/manage-theaters";
		}

		Theater theater = optionalTheater.get();

		// Uniqueness check
		if (screenRepository.existsByNameAndTheater(screenDto.getName(), theater)) {

			result.rejectValue("name", "error.name", "* Screen Already Exist in The Theater");
		}

		if (result.hasErrors()) {
			return "add-screen.html";
		}

		Screen screen = new Screen();
		screen.setName(screenDto.getName());
		screen.setType(screenDto.getType());
		screen.setTheater(theater);

		screenRepository.save(screen);

		theater.setScreenCount(theater.getScreenCount() + 1);
		theaterRepository.save(theater);

		attributes.addFlashAttribute("pass", "Screen Added Success");
		return "redirect:/manage-screens/" + theater.getId();
	}

	@Override
	public String deleteScreen(Long id, HttpSession session, RedirectAttributes attributes) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Screen> optionalScreen = screenRepository.findById(id);

		if (optionalScreen.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Screen");
			return "redirect:/manage-theaters";
		}

		Screen screen = optionalScreen.get();
		Theater theater = screen.getTheater();

		// Dependency check
		if (showRepository.existsByScreen(screen)) {
			attributes.addFlashAttribute("fail", "There are Shows Runing You can not Delete");
			return "redirect:/manage-screens/" + theater.getId();
		}

		// Remove seats first
		List<Seat> seats = seatRepository.findByScreenOrderBySeatRowAscSeatColumnAsc(screen);
		seatRepository.deleteAll(seats);

		// Update theater screen count
		theater.setScreenCount(theater.getScreenCount() - 1);
		theaterRepository.save(theater);

		screenRepository.delete(screen);

		attributes.addFlashAttribute("pass", "Screen Removed Success");
		return "redirect:/manage-screens/" + theater.getId();
	}

	@Override
	public String editScreen(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Screen> optionalScreen = screenRepository.findById(id);

		if (optionalScreen.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Screen");
			return "redirect:/manage-theaters";
		}

		Screen screen = optionalScreen.get();

		ScreenDto screenDto = new ScreenDto(screen.getName(), screen.getType(), screen.getTheater().getId());

		map.put("screenDto", screenDto);
		map.put("id", screen.getId());

		return "edit-screen.html";
	}

	@Override
	public String updateScreen(@Valid ScreenDto screenDto, Long id, BindingResult result, HttpSession session,
			RedirectAttributes attributes, ModelMap map) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		if (result.hasErrors()) {
			map.put("id", id);
			return "edit-screen.html";
		}

		Optional<Screen> optionalScreen = screenRepository.findById(id);

		if (optionalScreen.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Screen");
			return "redirect:/manage-theaters";
		}

		Screen screen = optionalScreen.get();
		screen.setName(screenDto.getName());
		screen.setType(screenDto.getType());

		screenRepository.save(screen);

		attributes.addFlashAttribute("pass", "Screen Updated Success");
		return "redirect:/manage-screens/" + screen.getTheater().getId();
	}

	@Override
	public String manageSeats(Long id, HttpSession session, ModelMap map, RedirectAttributes attributes) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Screen> optionalScreen = screenRepository.findById(id);

		if (optionalScreen.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Screen");
			return "redirect:/manage-theaters";
		}

		Screen screen = optionalScreen.get();

		List<Seat> seats = seatRepository.findByScreenOrderBySeatRowAscSeatColumnAsc(screen);

		// Group seats by row (preserves order)
		Map<String, List<Seat>> seatsByRow = seats.stream()
				.collect(Collectors.groupingBy(Seat::getSeatRow, LinkedHashMap::new, Collectors.toList()));

		map.put("seatsByRow", seatsByRow);
		map.put("screenId", id);

		return "manage-seats";
	}

	@Override
	public String addSeats(Long id, HttpSession session, ModelMap map, RedirectAttributes attributes) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Screen> optionalScreen = screenRepository.findById(id);

		if (optionalScreen.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Screen");
			return "redirect:/manage-theaters";
		}

		map.put("id", id);
		map.put("seatLayoutForm", new SeatLayoutForm());

		return "add-seats.html";
	}

	@Override
	public String saveSeats(Long screenId, SeatLayoutForm form, HttpSession session, RedirectAttributes attributes) {

		User user = getUserFromSession(session);
		if (user == null || !user.getRole().equals("ADMIN")) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Screen screen = screenRepository.findById(screenId).orElseThrow();

		for (SeatRowDto row : form.getRows()) {
			for (int i = 1; i <= row.getTotalSeats(); i++) {

				Seat seat = new Seat();
				seat.setScreen(screen);
				seat.setSeatRow(row.getRowName());
				seat.setSeatColumn(i);
				seat.setSeatNumber(row.getRowName() + i);
				seat.setCategory(row.getCategory());

				seatRepository.save(seat);
			}
		}

		attributes.addFlashAttribute("success", "Seats added successfully");
		return "redirect:/manage-screens/" + screen.getTheater().getId();
	}

	@Override
	public String manageMovies(HttpSession session, RedirectAttributes attributes, ModelMap map) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		List<Movie> movies = movieRepository.findAll();
		map.put("movies", movies);

		return "manage-movies.html";
	}

	@Override
	public String loadAddMovie(MovieDto movieDto, RedirectAttributes attributes, HttpSession session) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		return "add-movie.html";
	}

	@Override
	public String addMovie(MovieDto movieDto, BindingResult result, RedirectAttributes attributes,
			HttpSession session) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		// Duplicate movie check
		if (movieRepository.existsByNameAndReleaseDate(movieDto.getName(), movieDto.getReleaseDate())) {

			result.rejectValue("name", "error.name", "* Movie Already Exists");
		}

		// Image validation
		if (movieDto.getImage() == null || movieDto.getImage().isEmpty()) {
			result.rejectValue("image", "error.image", "* Image is Required");
		}

		if (result.hasErrors()) {
			return "add-movie.html";
		}

		Movie movie = new Movie(null, movieDto.getName(), movieDto.getLanguages(), movieDto.getGenre(),
				movieDto.getDuration(), cloudinaryHelper.generateImageLink(movieDto.getImage()),
				movieDto.getTrailerLink(), movieDto.getDescription(), movieDto.getReleaseDate(), movieDto.getCast());

		movieRepository.save(movie);

		attributes.addFlashAttribute("pass", "Movie Added Success");
		return "redirect:/manage-movies";
	}

	@Override
	public String manageShows(Long id, ModelMap map, RedirectAttributes attributes, HttpSession session) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Screen> optionalScreen = screenRepository.findById(id);

		if (optionalScreen.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Screen");
			return "redirect:/manage-theaters";
		}

		Screen screen = optionalScreen.get();
		List<Show> shows = showRepository.findByScreen(screen);

		map.put("shows", shows);
		map.put("id", id);

		return "manage-shows";
	}

	@Override
	public String addShow(Long id, ModelMap map, RedirectAttributes attributes, HttpSession session) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Screen> optionalScreen = screenRepository.findById(id);

		if (optionalScreen.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Screen");
			return "redirect:/manage-theaters";
		}

		Screen screen = optionalScreen.get();

		List<Seat> seats = seatRepository.findByScreenOrderBySeatRowAscSeatColumnAsc(screen);
		List<Movie> movies = movieRepository.findAll();

		if (seats.isEmpty() || movies.isEmpty()) {
			attributes.addFlashAttribute("fail", "First Add Movie and Add Seat Layout to continue");
			return "redirect:/manage-screens/" + screen.getTheater().getId();
		}

		ShowDto showDto = new ShowDto();
		showDto.setScreenId(screen.getId());

		map.put("movies", movies);
		map.put("showDto", showDto);

		return "add-show";
	}

	@Override
	public String addShow(ShowDto showDto, BindingResult result, RedirectAttributes attributes, HttpSession session,
			ModelMap map) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Movie> optionalMovie = movieRepository.findById(showDto.getMovieId());
		Optional<Screen> optionalScreen = screenRepository.findById(showDto.getScreenId());

		if (optionalMovie.isEmpty() || optionalScreen.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Data");
			return "redirect:/manage-theaters";
		}

		Movie movie = optionalMovie.get();
		Screen screen = optionalScreen.get();

		// Show date validation
		if (showDto.getShowDate().isBefore(movie.getReleaseDate())) {
			result.rejectValue("showDate", "error.showDate", "* Show Date Should be After Movie Release");
		}

		// Time overlap validation
		List<Show> existingShows = showRepository.findByScreen(screen);
		for (Show show : existingShows) {
			if (show.getShowDate().isEqual(showDto.getShowDate())
					&& showDto.getStartTime().isBefore(show.getEndTime())) {

				result.rejectValue("startTime", "error.startTime", "* In Same Time There is One More Show");
				break;
			}
		}

		if (result.hasErrors()) {
			map.put("movies", movieRepository.findAll());
			return "add-show";
		}

		// Create show
		Show show = new Show();
		show.setMovie(movie);
		show.setScreen(screen);
		show.setShowDate(showDto.getShowDate());
		show.setStartTime(showDto.getStartTime());
		show.setTicketPrice(showDto.getTicketPrice());

		show.setEndTime(show.getStartTime().plusHours(movie.getDuration().getHour())
				.plusMinutes(movie.getDuration().getMinute() + 30));

		// Create show seats
		List<ShowSeat> showSeats = new ArrayList<>();
		List<Seat> seats = seatRepository.findByScreenOrderBySeatRowAscSeatColumnAsc(screen);

		for (Seat seat : seats) {
			ShowSeat showSeat = new ShowSeat();
			showSeat.setSeat(seat);
			showSeat.setBooked(false);
			showSeats.add(showSeat);
		}

		show.setSeats(showSeats);
		showRepository.save(show);

		attributes.addFlashAttribute("pass", "Show Added Success");
		return "redirect:/manage-shows/" + showDto.getScreenId();
	}

	@Override
	public String loadMain(ModelMap map) {

		Set<Movie> movies = showRepository.findByShowDateAfter(LocalDate.now().minusDays(1)).stream()
				.map(Show::getMovie).collect(Collectors.toSet());

		map.put("movies", movies);
		return "main";
	}

	@Override
	public String bookMovie(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map) {

		Optional<Movie> optionalMovie = movieRepository.findById(id);

		if (optionalMovie.isEmpty()) {
			attributes.addFlashAttribute("fail", "Movie not found");
			return "redirect:/main";
		}

		Movie movie = optionalMovie.get();

		List<String> showDates = showRepository.findByMovieAndShowDateAfter(movie, LocalDate.now().minusDays(1))
				.stream().map(Show::getShowDate).distinct().sorted().map(LocalDate::toString).toList();

		map.put("movie", movie);
		map.put("showDate", showDates);

		return "display-shows";
	}
}
