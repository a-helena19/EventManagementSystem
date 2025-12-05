package everoutproject.Event.rest;

import everoutproject.Event.application.services.OrganizerService;
import everoutproject.Event.rest.dtos.event.request.NewOrganizerRequestDTO;
import everoutproject.Event.rest.dtos.event.response.OrganizerDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizers")
public class OrganizerRestController {

    private final OrganizerService organizerService;

    public OrganizerRestController(OrganizerService organizerService) {
        this.organizerService = organizerService;
    }

    @PostMapping("/create")
    public ResponseEntity<OrganizerDTO> createOrganizer(@RequestBody NewOrganizerRequestDTO dto) {
        OrganizerDTO created = organizerService.createOrganizer(dto);
        return ResponseEntity.ok(created);
    }

    // GET all organizers
    @GetMapping
    public ResponseEntity<List<OrganizerDTO>> getAllOrganizers() {
        return ResponseEntity.ok(organizerService.getAllOrganizers());
    }
}
