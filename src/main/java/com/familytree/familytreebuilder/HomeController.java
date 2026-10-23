
package com.familytree.familytreebuilder;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private final FamilyTreeService familyTreeService;

    public HomeController(FamilyTreeService familyTreeService) {
        this.familyTreeService = familyTreeService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("tree", familyTreeService.getFamilyTree());
        model.addAttribute("count", familyTreeService.getMemberCount());
        model.addAttribute("generations",
                familyTreeService.getGenerationCount());
        return "index";
    }

    @PostMapping("/add")
    public String addMember(
            @RequestParam String name,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute(
                "message", familyTreeService.addMember(name));
        return "redirect:/";
    }

    @PostMapping("/set-parent")
    public String setParent(
            @RequestParam String parentName,
            @RequestParam String childName,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute(
                "message",
                familyTreeService.setParent(parentName, childName));
        return "redirect:/";
    }

    @PostMapping("/search")
    public String searchMember(
            @RequestParam String name,
            RedirectAttributes redirectAttributes) {

        Person person = familyTreeService.searchMember(name);

        String result = person == null
                ? "Member not found: " + name
                : "Member found: " + person.getName();

        redirectAttributes.addFlashAttribute("message", result);
        return "redirect:/";
    }

    @PostMapping("/update")
    public String updateMember(
            @RequestParam String oldName,
            @RequestParam String newName,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute(
                "message",
                familyTreeService.updateMember(oldName, newName));
        return "redirect:/";
    }

    @PostMapping("/delete")
    public String deleteMember(
            @RequestParam String name,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute(
                "message", familyTreeService.deleteMember(name));
        return "redirect:/";
    }

    @PostMapping("/siblings")
    public String siblings(
            @RequestParam String name,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute(
                "message",
                "Siblings of " + name + ":\n"
                        + familyTreeService.getSiblings(name));
        return "redirect:/";
    }

    @PostMapping("/ancestors")
    public String ancestors(
            @RequestParam String name,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute(
                "message",
                "Ancestors of " + name + ":\n"
                        + familyTreeService.getAncestors(name));
        return "redirect:/";
    }

    @PostMapping("/descendants")
    public String descendants(
            @RequestParam String name,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute(
                "message",
                "Descendants of " + name + ":\n"
                        + familyTreeService.getDescendants(name));
        return "redirect:/";
    }
}
