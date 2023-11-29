package com.contactmanager.contactmanager.Controllers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.contactmanager.contactmanager.Dao.UserRepository;
import com.contactmanager.contactmanager.Helper.Message;
import com.contactmanager.contactmanager.Models.User;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @RequestMapping("/home")
    public String home(Model model){
        model.addAttribute("title", "Home");
        return "home";
    } 

    @RequestMapping("/about")
    public String about(Model model){
        model.addAttribute("title", "About");
        return "about";
    }
    @RequestMapping("/signup")
    public String signup(Model model){
        model.addAttribute("title", "SignUp");
        model.addAttribute("user", new User());
        return "signup";
    }
    @GetMapping("/signIn")
    public String login(Model model){
        model.addAttribute("title", "Login");
        return "signIn";
    }

    @RequestMapping(value="/do_register" ,method=RequestMethod.POST)
    public String register(@Valid @ModelAttribute("user") User user, BindingResult result1, @RequestParam(value="agreement", defaultValue = "false") boolean agreement, @RequestParam("profileImage") MultipartFile file,
    Model model,  HttpSession session ){
        try {

             if(file.isEmpty()){
                System.out.println("Image file is empity!");
                user.setImageUrl("contact3.gif");
            }
            else{

                user.setImageUrl(file.getOriginalFilename());
                File saveFile= new ClassPathResource("static/img").getFile();

                Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }
            if(!agreement){
             System.out.println("You must agree to our terms of service.");
             throw new Exception("You have not agreed to the terms and conditions");
            }
            if(result1.hasErrors()){
                model.addAttribute("user", user);
                return "signup";
            }
            user.setRole("USER");
            user.setEnabled(true);
            System.out.println("this curr password-------------------------------------------->"+user.getPassword());
            System.out.println(user);
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            model.addAttribute("user", user);
            User result=this.userRepository.save(user); 
            model.addAttribute("user", result);
            // model.addAttribute("session", session);
            session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
            return "signup";
        } catch (Exception e) {
            System.out.println("<-----------Problem hai!---------------->");
            e.printStackTrace();
            model.addAttribute("user", user);
            session.setAttribute("message", new Message("Something went wrong !!" +e.getMessage(), "alert-danger"));
            return "signup";
        }
        
    }
}
