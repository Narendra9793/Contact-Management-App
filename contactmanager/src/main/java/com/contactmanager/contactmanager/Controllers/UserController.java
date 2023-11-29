package com.contactmanager.contactmanager.Controllers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.contactmanager.contactmanager.Dao.ContactRepository;
import com.contactmanager.contactmanager.Dao.UserRepository;
import com.contactmanager.contactmanager.Helper.Message;
import com.contactmanager.contactmanager.Models.Contact;
import com.contactmanager.contactmanager.Models.User;

import jakarta.servlet.http.HttpSession;

import org.springframework.ui.Model;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @ModelAttribute
    public void addCommonData(Model model, Principal principal){
        String username=principal.getName();
       
        User user=userRepository.getUserByUserName(username);

        model.addAttribute("user", user);
        
    }

    //Dashboard
    @RequestMapping("/user_dashboard")
    public String dashboard(Model model, Principal principal){
        String username=principal.getName();
       
        User user=userRepository.getUserByUserName(username);

        model.addAttribute("user", user);
        return "normal/user_dashboard";
    }


    @GetMapping("/add-contact")
    public String addContact(Model model){
        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());
        return "normal/addContact_Form";
    }

    @PostMapping("/process-contact")
    public String processForm(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session, Model model){
        try{
            // processing and uploading image file.

            if(file.isEmpty()){
                System.out.println("Image file is empity!");
                contact.setImage("contact3.gif");
            }
            else{

                contact.setImage(file.getOriginalFilename());
                File saveFile= new ClassPathResource("static/img").getFile();

                Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }

            String username=principal.getName();
            User user=this.userRepository.getUserByUserName(username);
            contact.setUser(user);
            user.getContacts().add(contact);
            this.userRepository.save(user);

            session.setAttribute("message", new Message("Your Contact is added Successfully!", "success"));

            
        }
        catch(Exception e ){
            session.setAttribute("message", new Message("Attention Something Went Wrong!", "dangerous"));
            e.printStackTrace();
            System.out.println("Error" + e.getMessage());

        }
        return "normal/addContact_Form";
    }

    @GetMapping("/show-contacts/{page}")
    public String viewContacts(@PathVariable Integer page, Model model, Principal principal){
        String name=principal.getName();
        User user=this.userRepository.getUserByUserName(name);
        PageRequest pageable=PageRequest.of(page, 5);
        Page<Contact> contacts = this.contactRepository.findContactById(user.getId(), (org.springframework.data.domain.Pageable) pageable);
        model.addAttribute("title", "User Contacts");
        model.addAttribute("contacts", contacts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPage", contacts.getTotalPages());
        return "normal/Show-Contacts";
    }

    // Getting particular contact's details

    @GetMapping("/{cId}/contact")
    public String singleContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal){
        Optional<Contact> optionalContact = this.contactRepository.findById(cId);
        Contact contact=optionalContact.get();
        String username=principal.getName();
        User user=this.userRepository.getUserByUserName(username);


        // checking whether logged userId  and contact's userid is same or not
        if(user.getId() == contact.getUser().getId()){
            model.addAttribute("contact", contact);
            model.addAttribute("title", contact.getName());
        }
        return "normal/contact_details";
    }

    @GetMapping("/delete-contact/{cId}")
    public String deleteContact(@PathVariable("cId") Integer cId, HttpSession session, Principal principal) {
         System.out.println("Delete Contact Handler!");
        // Getting contact to delete
        Contact contact=this.contactRepository.findById(cId).get();
        User user=this.userRepository.getUserByUserName(principal.getName());

        user.getContacts().remove(contact);

        this.userRepository.save(user);
        session.setAttribute("message", new Message("Your Contact with name " + contact.getName()+" " + contact.getSecondName()+" is deleted!", "success"));
        System.out.println("Constact is deleted Successfully!");

        return "redirect:/user/show-contacts/0";
    }


    @PostMapping("/update-contact/{cId}")
    public String updateContact(@PathVariable("cId") Integer cId, HttpSession session, Principal principal, Model model) {
        // Getting contact to delete
        Contact contact=this.contactRepository.findById(cId).get();
        model.addAttribute("contact", contact);
        return "normal/Update_form";
    }

    @PostMapping("/process-update")
    public String processUpdate(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session, Model model) {
        try{
            // getting oldDetails of Contacts

            Contact oldContactDetails=this.contactRepository.findById(contact.getcId()).get();

            if(!file.isEmpty()){
                // deleting old one
                File deleteFile= new ClassPathResource("static/img").getFile();
                File file2=new File(deleteFile, oldContactDetails.getImage());
                file2.delete();
                // uploading new photo
                File saveFile= new ClassPathResource("static/img").getFile();
                Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                contact.setImage(file.getOriginalFilename());

            }
            else{
                System.out.println("Image file is empity!");
                contact.setImage(oldContactDetails.getImage());

            }

            User user=this.userRepository.getUserByUserName(principal.getName());
            contact.setUser(user);
            this.contactRepository.save(contact);

            session.setAttribute("message", new Message("Your Contact with name " + contact.getName()+" " + contact.getSecondName()+" is updated!", "success"));

            
        }
        catch(Exception e ){
            session.setAttribute("message", new Message("Your Contact with name " + contact.getName()+" " + contact.getSecondName()+" is not updated!", "danger"));
            e.printStackTrace();
            System.out.println("Error" + e.getMessage());

        }
        return "redirect:/user/"+contact.getcId()+"/contact";
    }

    @GetMapping("/profile")
    public String showProfie(Model model, Principal principal){
        String username=principal.getName();
        User user=userRepository.getUserByUserName(username);
        model.addAttribute("user", user);
        model.addAttribute("title", "Profile");
        return "normal/profile";
    }

    @PostMapping("/update-user/{id}")
        public String updateUser(@PathVariable("id") Integer id, HttpSession session, Principal principal, Model model) {
        System.out.println("Going to update User Details!=======================================================");
        User user=this.userRepository.findById(id).get();
        model.addAttribute("user", user);
        return "normal/user_update_form";
    }

    @PostMapping("/process-user-update")
    public String processUpdate(@ModelAttribute User user, @RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session, Model model) {
        try{
            // getting oldDetails of Contacts

            User oldContactDetails=this.userRepository.findById(user.getId()).get();

            if(!file.isEmpty()){
                // deleting old one
                File deleteFile= new ClassPathResource("static/img").getFile();
                File file2=new File(deleteFile, oldContactDetails.getImageUrl());
                file2.delete();
                // uploading new photo
                File saveFile= new ClassPathResource("static/img").getFile();
                Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                user.setImageUrl(file.getOriginalFilename());

            }
            else{
                System.out.println("Image file is empity!");
                user.setImageUrl(oldContactDetails.getImageUrl());

            }
        
            this.userRepository.save(user);

            session.setAttribute("message", new Message("Your Contact with name " + user.getName()+" is updated!", "success"));

            
        }
        catch(Exception e ){
            session.setAttribute("message", new Message("Your Contact with name " + user.getName()+" is not updated!", "danger"));
            e.printStackTrace();
            System.out.println("Error" + e.getMessage());

        }
        return "normal/profile";
    }

}
