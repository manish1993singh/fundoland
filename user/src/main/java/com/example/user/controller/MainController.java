package com.example.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.user.entity.User;
import com.example.user.event.UserEventPublisher;
import com.example.user.event.UserCreatedEvent;
import com.example.user.event.UserCreationFailedEvent;
import com.example.user.repository.UserRepository;

/**
 * @Controller: Marks this class as a Spring MVC Controller, allowing it to handle web requests.
 */
@Controller
/**
 * @RequestMapping(path="/rest"): Sets the base URL path for all endpoints in this controller to start with /rest.
 */
@RequestMapping(path="/rest")
public class MainController {

  /**
   * @Autowired: Injects the UserEventPublisher bean for publishing user-related events.
   */
  @Autowired
  private UserEventPublisher eventPublisher;

  /**
   * @Autowired: Injects the UserRepository bean for database operations on User entities.
   */
  @Autowired
  private UserRepository userRepository;


  /**
   * @Autowired: Injects the CacheManager bean for managing cache operations.
   */
  @Autowired
  private org.springframework.cache.CacheManager cacheManager;

  /**
   * Fetch all soft deleted users.
   * @return Iterable<User> - list of soft deleted users
   */
  /**
   * @GetMapping(path="/deleted-users"): Maps HTTP GET requests to /rest/deleted-users to this method.
   */
  @GetMapping(path="/deleted-users")
  /**
   * @ResponseBody: Indicates the return value should be written directly to the HTTP response body.
   */
  @ResponseBody
  public Iterable<User> getDeletedUsers() {
  System.out.println("[LOG] @GetMapping ---------------------------/deleted-users called");
    return userRepository.findAllByDeletedTrue();
  }

  /**
   * Add a new user to the system.
   * If the email is already registered, publishes a failure event and returns error.
   * On success, saves the user, publishes a success event, and evicts the cached user list.
   *
   * @param name  User's name
   * @param email User's email
   * @return String - result message
   */
  /**
   * @PostMapping(path="/add"): Maps HTTP POST requests to /rest/add to this method.
   */
  @PostMapping(path="/add")
  /**
   * @ResponseBody: Indicates the return value should be written directly to the HTTP response body.
   */
  @ResponseBody
  /**
   * @CachePut: Updates the cache with the new user under the key 'userByEmail' using the email as the key.
   */
  @org.springframework.cache.annotation.CachePut(value = "userByEmail", key = "#email")
  /**
   * @RequestParam: Binds the HTTP request parameters 'name' and 'email' to method arguments.
   */
  public String addNewUser (@RequestParam String name, @RequestParam String email) {
    // @ResponseBody means the returned String is the response, not a view name
    // @RequestParam means it is a parameter from the GET or POST request

  System.out.println("[LOG] @PostMapping /add called");
    UserCreatedEvent event = new UserCreatedEvent(name, email);
    if (userRepository.findByEmail(email).isPresent()) {
      System.out.println("Email already registered: " + email);
      eventPublisher.publishUserCreationFailedEvent(
          new UserCreationFailedEvent(email, "Email already registered")
      );
      // publish failure event
      return "Error: Email already registered.";
    }
    User n = new User();
    n.setName(name);
    n.setEmail(email);
    userRepository.save(n);
    eventPublisher.publishUserCreatedEvent(event); // publish success event
    // Evicts the cached user list so next /users call gets fresh data
    return "Saved";
  }

  /**
   * Get all users in the system.
   * Uses Redis caching to store and retrieve the user list for faster response.
   * The result is cached under the key 'allUsers'.
   * Cache is automatically updated when a new user is added via /add endpoint.
   *
   * @return Iterable<User> - list of all users
   */
  /**
   * Get all users in the system.
   * This endpoint is no longer cached as a list. Use /userByEmail for per-user caching.
   */
  @GetMapping(path="/users")
  @ResponseBody
  public Iterable<User> getAllUsers() {
    java.util.List<User> users = new java.util.ArrayList<>();
  System.out.println("[LOG] @GetMapping /users called");
    for (User user : userRepository.findAll()) {
      if (!user.isDeleted()) {
        users.add(user);
      }
    }
    return users;
  }

  /**
   * Get a user by email, cached in Redis with email as the cache key.
   * @param email User's email
   * @return User or error message
   */
  @GetMapping(path="/userByEmail")
  @ResponseBody
  @org.springframework.cache.annotation.Cacheable(value = "userByEmail", key = "#email")
  public Object getUserByEmail(@RequestParam String email) {
  System.out.println("[LOG] @GetMapping /userByEmail called");
    java.util.Optional<User> userOpt = userRepository.findByEmail(email);
    if (userOpt.isPresent() && !userOpt.get().isDeleted()) {
      return userOpt.get();
    } else {
      return "Error: User not found.";
    }
  }
  /**
   * Update an existing user's name and/or email by user ID.
   * Evicts the cached user list after update.
   *
   * @param id    User's ID
   * @param name  New name (optional)
   * @param email New email (optional)
   * @return String - result message
   */
  @PostMapping(path="/update")
  @ResponseBody
  @org.springframework.cache.annotation.CacheEvict(value = "userByEmail", key = "#email")
  public String updateUser(@RequestParam Integer id,
                           @RequestParam(required = false) String name,
                           @RequestParam(required = false) String email) {
  System.out.println("[LOG] @PostMapping /update called");
    java.util.Optional<User> userOpt = userRepository.findById(id);
    if (!userOpt.isPresent()) {
      return "Error: User not found.";
    }
    User user = userOpt.get();
    if (name != null && !name.isEmpty()) user.setName(name);
    if (email != null && !email.isEmpty()) user.setEmail(email);
    userRepository.save(user);
    return "Updated";
  }

  /**
   * Delete a user by ID.
   * Evicts the cached user list after deletion.
   *
   * @param id User's ID
   * @return String - result message
   */
  @PostMapping(path="/delete")
  @ResponseBody
  public String deleteUser(@RequestParam Integer id) {
  System.out.println("[LOG] @PostMapping /delete called");
    java.util.Optional<User> userOpt = userRepository.findById(id);
    if (!userOpt.isPresent()) {
      return "Error: User not found.";
    }
    String email = userOpt.get().getEmail();
  org.springframework.cache.Cache cache = cacheManager.getCache("userByEmail");
  if (cache != null) cache.evict(email);
  User user = userOpt.get();
  user.setDeleted(true);
  userRepository.save(user);
  return "Soft deleted";
  }
}