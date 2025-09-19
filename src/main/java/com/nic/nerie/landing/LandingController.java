package com.nic.nerie.landing;

import com.nic.nerie.mt_programdetails.service.MT_ProgramDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/nerie")
public class LandingController {
    private final MT_ProgramDetailsService mtProgramDetailsService;

    @Autowired
    public LandingController(MT_ProgramDetailsService mtProgramDetailsService) {
        this.mtProgramDetailsService = mtProgramDetailsService;
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
