// src/main/java/psw/verapelle/service/ColorService.java
package psw.verapelle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import psw.verapelle.DTO.ColorDTO;
import psw.verapelle.entity.Color;
import psw.verapelle.repository.ColorRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ColorService {

    @Autowired
    private ColorRepository colorRepository;

    @Transactional(readOnly = true)
    public List<ColorDTO> getAllColors() {
        return colorRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ColorDTO getColorById(Long id) {
        Color c = colorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Color not found: " + id));
        return toDTO(c);
    }

    @Transactional
    public ColorDTO createColor(ColorDTO dto) {
        Color c = new Color();
        c.setName(dto.getName());
        c.setHexCode(dto.getHexCode());
        Color saved = colorRepository.save(c);
        return toDTO(saved);
    }

    @Transactional
    public ColorDTO updateColor(Long id, ColorDTO dto) {
        Color c = colorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Color not found: " + id));
        if (dto.getName() != null)    c.setName(dto.getName());
        if (dto.getHexCode() != null) c.setHexCode(dto.getHexCode());
        Color saved = colorRepository.save(c);
        return toDTO(saved);
    }

    @Transactional
    public void deleteColor(Long id) {
        Color c = colorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Color not found: " + id));
        colorRepository.delete(c);
    }

    private ColorDTO toDTO(Color c) {
        return new ColorDTO(c.getId(), c.getName(), c.getHexCode());
    }
}
