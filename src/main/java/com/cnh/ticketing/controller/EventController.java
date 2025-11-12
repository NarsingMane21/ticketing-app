package com.cnh.ticketing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.cnh.ticketing.model.Event;
import com.cnh.ticketing.model.Gender;
import com.cnh.ticketing.model.User;
import com.cnh.ticketing.repository.EventRepository;
import com.cnh.ticketing.repository.UserRepository;
import com.cnh.ticketing.security.UserPrincipal;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
public class EventController {

	private final EventRepository eventRepository;
	private final UserRepository userRepository;

	public EventController(EventRepository eventRepository, UserRepository userRepository) {
		this.eventRepository = eventRepository;
		this.userRepository = userRepository;
	}

	@GetMapping
	public List<Event> list() {
		return eventRepository.findAll();
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> get(@PathVariable Long id) {
		return eventRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PostMapping("/{id}/attend")
	public ResponseEntity<?> attend(@PathVariable Long id, Authentication auth) {

		// Extract logged-in user from JWT
		UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();

		// Fetch the actual User entity from DB
		User user = userRepository.findByEmail(userPrincipal.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		// Fetch event from DB
		Event event = eventRepository.findById(id).orElse(null);
		if (event == null)
			return ResponseEntity.notFound().build();

		// Business logic: disallow attending past events
		if (event.getStartAt().isBefore(OffsetDateTime.now()))
			return ResponseEntity.badRequest().body("Cannot attend a past event");

		// Prevent duplicate attendance
		if (event.getAttendees().contains(user))
			return ResponseEntity.badRequest().body("Already attending this event");

		// Link user and event
		event.getAttendees().add(user);
		user.getEvents().add(event);

		eventRepository.save(event);

		// Calculate ticket fee (5% discount for female)
		double fee = event.getTicketFee() == null ? 0.0 : event.getTicketFee();
		if (user.getGender() == Gender.FEMALE)
			fee = fee * 0.95;

		return ResponseEntity.ok("Attending. Ticket to pay: " + String.format("%.2f", fee));
	}

	@PostMapping("/{id}/unattend")
	public ResponseEntity<?> unattend(@PathVariable Long id, Authentication auth) {

		//Extract logged-in user from Spring Security
		UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();

		//Fetch actual User entity from DB
		User user = userRepository.findByEmail(userPrincipal.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		//Fetch event from DB
		Event event = eventRepository.findById(id).orElse(null);
		if (event == null)
			return ResponseEntity.notFound().build();

		//Check if user is attending
		if (!event.getAttendees().contains(user))
			return ResponseEntity.badRequest().body("You are not attending this event");

		//Remove user from event and event from user
		event.getAttendees().remove(user);
		user.getEvents().remove(event);

		//Save updated relationship
		eventRepository.save(event);

		return ResponseEntity.ok("You have successfully unattended the event.");
	}

	@GetMapping("/{id}/attendees")
	public ResponseEntity<?> attendees(@PathVariable Long id) {
		Event event = eventRepository.findById(id).orElse(null);
		if (event == null)
			return ResponseEntity.notFound().build();
		Set<User> users = event.getAttendees();
		// avoid returning passwords
		var payload = users.stream().map(u -> java.util.Map.of("id", u.getId(), "email", u.getEmail(), "name",
				u.getName(), "gender", u.getGender())).collect(Collectors.toList());
		return ResponseEntity.ok(payload);
	}
}
