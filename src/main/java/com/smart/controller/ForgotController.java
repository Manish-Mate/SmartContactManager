package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;


@Controller
public class ForgotController {

	Random random = new Random(1000);
	@Autowired
	private EmailService emailService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@GetMapping("/forget")
	public String openEmailForm() {
		return "forget_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,HttpSession httpSession) {
		System.out.println(email);
		
	
		int otp = random.nextInt(999999);
		System.out.println(otp);
		String subject="Otp From SCM";
		String message="<div style='border: 2px solid #666;'>"
				+"<h1>"
				+"<b>" 
				+"OTP is " +otp
				+"</b>" 
				+"</h1>"
				+"</div>";
				
		String to=email;
		boolean flag = this.emailService.sendEmail(subject, message, to);
		if(flag) {
			
			httpSession.setAttribute("myotp", otp);
			httpSession.setAttribute("email", email);
			return "verify_otp";
			
		}
		else {
			httpSession.setAttribute("message", new Message("check your email id!!", "alert-danger"));
			return "forget_email_form";
		}
		
	}
	
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp,HttpSession session){
		int myOtp = (int)session.getAttribute("myotp");
		String email = (String)session.getAttribute("email");
		
		if(myOtp==otp) {	
			User user = this.userRepository.getUserByUserName(email);
			if(user==null) {
				session.setAttribute("message", new Message("User Does not exist with this email", "alert-danger"));
				return "forget_email_form";
			}
			else {
//				session.setAttribute("message", new Message("check your email id!!", "alert-danger"));
				return "password_change_form";
			}
			
		}
		else {
			session.setAttribute("message", new Message("You have entered wrong otp","alert-danger"));
			return "verify_otp";
		}
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword,HttpSession session) {
		
		String email =(String) session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
		this.userRepository.save(user);
		
		return "redirect:/signin?change=password changed successfully";
	}
	
	
}
