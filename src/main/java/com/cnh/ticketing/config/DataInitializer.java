package com.cnh.ticketing.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cnh.ticketing.model.Event;
import com.cnh.ticketing.model.Gender;
import com.cnh.ticketing.model.User;
import com.cnh.ticketing.repository.EventRepository;
import com.cnh.ticketing.repository.UserRepository;

import java.time.OffsetDateTime;

@Configuration
public class DataInitializer {
	@Bean
	CommandLineRunner init(UserRepository userRepo, EventRepository eventRepo, PasswordEncoder encoder) {
		return args -> {
			if (userRepo.count() == 0) {
				userRepo.save(User.builder().email("alice@example.com").password(encoder.encode("password"))
						.name("Alice").gender(Gender.FEMALE).build());
				userRepo.save(User.builder().email("bob@example.com").password(encoder.encode("password")).name("Bob")
						.gender(Gender.MALE).build());
			}
			if (eventRepo.count() == 0) {
				eventRepo.save(Event.builder().title("Spring Boot Meetup").description("Talks about Spring Boot")
						.startAt(OffsetDateTime.now().plusDays(2)).ticketFee(100.0).build());
				eventRepo.save(Event.builder().title("Java Conference").description("Big Java event")
						.startAt(OffsetDateTime.now().plusDays(10)).ticketFee(500.0).build());
				eventRepo.save(Event.builder().title("Past Event").description("Already happened")
						.startAt(OffsetDateTime.now().minusDays(5)).ticketFee(50.0).build());
			}
		};
	}
}