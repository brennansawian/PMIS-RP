package com.nic.nerie.landing;

import com.nic.nerie.mt_programdetails.service.MT_ProgramDetailsService;
import com.nic.nerie.mt_userlogin.model.MT_Userlogin;
import com.nic.nerie.mt_userlogin.service.MT_UserloginService;
import com.nic.nerie.utils.RandomPasswordGenerator;
import com.nic.nerie.utils.SHA256Util;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
@RequestMapping("/nerie")
public class LandingController {
    private final MT_ProgramDetailsService mtProgramDetailsService;
    private final MT_UserloginService userloginService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public LandingController(MT_ProgramDetailsService mtProgramDetailsService,MT_UserloginService userloginService,
    PasswordEncoder passwordEncoder) {
        this.mtProgramDetailsService = mtProgramDetailsService;
        this.userloginService = userloginService;
        this.passwordEncoder =  passwordEncoder;
    }

    // TODO: Make a landing package to render all landing pages
    // Changes are to be made in how programs are managed
    // Do this later
    @GetMapping("/index")
    public String renderIndexPage(Model model) {
        model.addAttribute("currentPage", "home");
        model.addAttribute("ongoingprogramlist", mtProgramDetailsService.getOngoingProgramList(0, 3, 0));
        model.addAttribute("upcomingprogramlist", mtProgramDetailsService.getUpcomingProgramList(0, 3, 0));
        model.addAttribute("completedprogramlist", mtProgramDetailsService.getCompletedProgramList(0, 3, 0));
        model.addAttribute("countongoing", mtProgramDetailsService.getCountOngoingProgram());
        model.addAttribute("countupcoming", mtProgramDetailsService.getCountUpcomingProgram());
        model.addAttribute("countcompleted", mtProgramDetailsService.getCountCompletedProgram());
        return "pages/landing/home";
    }

   @GetMapping("/forgotpassword")
    public String forgotpassword() {
        return "pages/landing/resetpassword";
    }

    @PostMapping("/loginresetpassword")
    @ResponseBody
    public JSONObject resetpassword(@RequestParam(value="userid",required=true) String userid) {
        JSONObject jsonobj = new JSONObject();
        try{
            MT_Userlogin user = userloginService.findByUserId(userid);

            String password = RandomPasswordGenerator.generateRandomPassword();
            String pwd;
            if(user.getUseBcrypt()){
                pwd = passwordEncoder.encode(password);
            }
            else{
                pwd = SHA256Util.getHash(password);
            }
            //if () {
            user.setUserpassword(pwd);
            userloginService.save(user);
            jsonobj.put("status", "1");
            jsonobj.put("val", "Password Successfully Changed! New Password is:"+password+".Please login to change your password.");
                
                //IF SENDING EMAIL TO USER
                //1. CREATE A NEW FIELD IN MT_USERLOGIN (PASSWORD RESET TOKEN)
                //2. GENERATE A PASSWORD RESET TOKEN (AGAIN RANDOM) AND SEND TO MAIL
                //3. THE ABOVE PASSWORD RESET TOKEN SET AS THE USER'S PRT IN MT_USERLOGIN
                //4. IN THE EMAIL, SEND A LINK WHICH WHEN CLICKED, LEADS TO A PAGE TO ENTER THE 
                //   NEW PASSWORD ALONG WITH THE PASSWORD RESET TOKEN
                //5. IF PASSWORD RESET TOKEN IN MAIL IS THE SAME AS THE ONE IN MT_USERLOGIN,
                //   THEN UPDATE THE PASSWORD ELSE DON'T ALLOW
                
            //} 
            // else {
            //     jsonobj.put("status", "-1");
            //     jsonobj.put("val", "Error Occured. Please Try Again");
            // }
            return jsonobj;
            }
        catch(Exception e){
            e.printStackTrace();
        }
        jsonobj.put("status", "-2");
        jsonobj.put("val", "No User with given User ID exists!");
        return jsonobj;
    }


    @GetMapping("/about")
    public String renderAboutPage(Model model) {
        model.addAttribute("currentPage", "about");
        return "pages/landing/about";
    }

    @GetMapping("/about/blog1")
    public String renderBlog1Page(Model model) {
        model.addAttribute("currentPage", "about");
        return "pages/landing/blog1";
    }

    @GetMapping("/about/blog2")
    public String renderBlog2Page(Model model) {
        model.addAttribute("currentPage", "about");
        return "pages/landing/blog2";
    }

    @GetMapping("/about/blog3")
    public String renderBlog3Page(Model model) {
        model.addAttribute("currentPage", "about");
        return "pages/landing/blog3";
    }

    @GetMapping("/about/blog4")
    public String renderBlog4Page(Model model) {
        model.addAttribute("currentPage", "about");
        return "pages/landing/blog4";
    }

    @GetMapping("/about/blog5")
    public String renderBlog5Page(Model model) {
        model.addAttribute("currentPage", "about");
        return "pages/landing/blog5";
    }

    @GetMapping("/about/blog6")
    public String renderBlog6Page(Model model) {
        model.addAttribute("currentPage", "about");
        return "pages/landing/blog6";
    }

    @GetMapping("/about/blog7")
    public String renderBlog7Page(Model model) {
        model.addAttribute("currentPage", "about");
        return "pages/landing/blog7";
    }
}
