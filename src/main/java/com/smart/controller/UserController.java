package com.smart.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.smart.dao.ContactRepository;
import com.smart.dao.MyOrderRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;
	@Autowired
	private MyOrderRepository myOrderRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@ModelAttribute
	public void addCommanData(Model model, Principal principal) {
		String userName = principal.getName();
//		System.out.println(userName);

		User user = this.userRepository.getUserByUserName(userName);
//		System.out.println(user);
		model.addAttribute("user", user);
	}

	@GetMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	@GetMapping("add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			if (file.isEmpty()) {
				System.out.println("file not upload its empty");
				contact.setImage("contact.png");
			} else {
				contact.setImage(contact.getCid() + file.getOriginalFilename());

				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(
						saveFile.getAbsolutePath() + File.separator + contact.getCid() + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}

			user.getContacts().add(contact);
			contact.setUser(user);
			this.userRepository.save(user);
			System.out.println("added to database");
//			System.out.println(contact);
			session.setAttribute("message", new Message("Your contact is added!! Add more...", "alert-success"));
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Something wnet wrong!! Try again...", "alert-danger"));
		}
		return "normal/add_contact_form";
	}

	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);
		model.addAttribute("contacts", contacts);
		model.addAttribute("title", "Show User Contact");
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPage", contacts.getTotalPages());
		return "normal/show_contacts";
	}

	@GetMapping("/{cid}/contact")
	public String showContactDetail(@PathVariable("cid") Integer cid, Model model, Principal principal) {
//		System.out.println(cid);

		Optional<Contact> contactOpt = this.contactRepository.findById(cid);
		Contact contact = contactOpt.get();
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}

		return "normal/contact_detail";
	}

	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid, Model model, HttpSession session) {

		try {
			Optional<Contact> contactOptional = this.contactRepository.findById(cid);
			Contact contact = contactOptional.get();
			System.out.println(contact.getImage());
//			if (contact.getImage() != "contact.png") {
//				File deletefile = new ClassPathResource("static/img").getFile();
//				File file1 = new File(deletefile, contact.getImage());
//				file1.delete();
//			}
			contact.setUser(null);
			this.contactRepository.delete(contact);
			session.setAttribute("message", new Message("Contact deleted successfully!!", "alert-success"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "redirect:/user/show-contacts/0";
	}

//	open update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model model) {
		model.addAttribute("title", "Update Contact");
		Contact contact = this.contactRepository.findById(cid).get();
		model.addAttribute("contact", contact);
		return "normal/update_form";
	}
//	update contact handler

	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model model, HttpSession session, Principal principal) {

		try {
			Contact oldContact = this.contactRepository.findById(contact.getCid()).get();
			if (!file.isEmpty()) {
//				delete old photo 

				File deletefile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deletefile, oldContact.getImage());
				file1.delete();

//				update new photo
				File savefile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(savefile.getAbsoluteFile() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			} else {
				contact.setImage(oldContact.getImage());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Your Contact is Updated", "alert-success"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(contact);
		System.out.println(contact.getCid());
		return "redirect:/user/" + contact.getCid() + "/contact";
	}
	
	@GetMapping("/profile")
	public String yourProfile(Model model)
	{
		model.addAttribute("title", "Profile Page");
		return "normal/profile";
	}
	
//	open setting handler
	@GetMapping("/setting")
	public String openSetting() {
		return "normal/settings";
	}
	
	@PostMapping("/changepassword")
	public String changepassword(@RequestParam("oldpassword")String oldPassword,
			@RequestParam("newpassword")String newPassword,Principal principal,HttpSession session) {
		
		System.out.println(oldPassword+newPassword);
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		System.out.println(user);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, user.getPassword())) {
			user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Your password is successfully change", "alert-success"));
		}else {
			session.setAttribute("message", new Message("Please Enter correct old password", "alert-danger"));
		}
		
		return "redirect:/user/index";
	}
	
	
//	order for payment
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data,Principal principal) throws RazorpayException {
		System.out.println("order function executed");
		System.out.println(data);
		int amt=Integer.parseInt(data.get("amount").toString());
		var client=new RazorpayClient("rzp_test_3Q0vQxOHghJlJC", "caZfSlUFbFx40DemlItWem6W");
		
		JSONObject obj=new JSONObject();
		obj.put("amount", amt*100);
		obj.put("currency", "INR");
		obj.put("receipt", "txn_235425");
		
		Order order = client.orders.create(obj);
		System.out.println("order"+order);
		
		MyOrder myOrder = new MyOrder();
		myOrder.setAmount(order.get("amount")+"");
		myOrder.setOrderId(order.get("id"));
		myOrder.setPaymentId(null);
		myOrder.setStatus("created");
		myOrder.setUser(this.userRepository.getUserByUserName(principal.getName()));
		myOrder.setReceipt(order.get("receipt"));
		
		this.myOrderRepository.save(myOrder);
		return order.toString();
	}
	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object>data){
		
		MyOrder myOrder = this.myOrderRepository.findByOrderId(data.get("order_id").toString());
		System.out.println("payment id "+ data.get("payment_id").toString());
		myOrder.setPaymentId(data.get("payment_id").toString());
		myOrder.setStatus(data.get("statuss").toString());
	
		System.out.println(myOrder);

		
		this.myOrderRepository.save(myOrder);
		System.out.println("data"+data);
		
		return ResponseEntity.ok(Map.of("msg","updated"));
	}
}
