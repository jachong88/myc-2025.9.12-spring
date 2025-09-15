package web;

import org.springframework.boot.SpringApplication;

import web.WebApplication;

public class TestWebApplication {

	public static void main(String[] args) {
		SpringApplication.from(WebApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
