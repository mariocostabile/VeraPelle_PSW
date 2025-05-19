// src/main/java/psw/verapelle/controller/ColorController.java
package psw.verapelle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import psw.verapelle.DTO.ColorDTO;
import psw.verapelle.service.ColorService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/colors")
public class ColorController {

    @Autowired
    private ColorService colorService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<ColorDTO> getAll() {
        return colorService.getAllColors();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ColorDTO getById(@PathVariable Long id) {
        return colorService.getColorById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ColorDTO create(@RequestBody ColorDTO dto) {
        return colorService.createColor(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ColorDTO update(@PathVariable Long id, @RequestBody ColorDTO dto) {
        return colorService.updateColor(id, dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        colorService.deleteColor(id);
    }
}
