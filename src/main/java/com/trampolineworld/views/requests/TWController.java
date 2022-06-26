package com.trampolineworld.views.requests;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TWController {

  // Aggregate root
  // tag::get-aggregate-root[]
  @GetMapping("/getGreeting")
  String all() {
    return "Hello from TW!";
  }
  // end::get-aggregate-root[]

  @PostMapping("/newMessage")
  String newMessage(@RequestBody String newMessage) {
    System.out.println(newMessage);
    System.out.println("I RECEIVED SOMETHING");
    return "I RECEIVED SOMETHING";
  }

  // Single item
//  
//  @GetMapping("/employees/{id}")
//  Employee one(@PathVariable Long id) {
//    
//    return repository.findById(id)
//      .orElseThrow(() -> new EmployeeNotFoundException(id));
//  }
//
  
  @PutMapping("/message/{msg}")
  String sendMessage(@RequestBody String newMessage, @PathVariable String msg) {
    System.out.println(msg);
    return "You said: " + msg;
  }
  
//
//  @DeleteMapping("/employees/{id}")
//  void deleteEmployee(@PathVariable Long id) {
//    repository.deleteById(id);
//  }
}
