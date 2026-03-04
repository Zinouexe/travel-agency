package com.example.travelagency;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class TravelController {

    private final JdbcTemplate jdbc;

    public TravelController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/")
    public String home(Model model) {
        String dbStatus;
        String dbName = null;
        Long customersCount = null;
        Long servicesCount = null;
        Long tripsCount = null;
        try {
            dbName = jdbc.queryForObject(
                "SELECT sys_context('userenv','db_name') FROM dual",
                String.class
            );
            customersCount = jdbc.queryForObject("SELECT COUNT(*) FROM Customers", Long.class);
            servicesCount = jdbc.queryForObject("SELECT COUNT(*) FROM Services", Long.class);
            tripsCount = jdbc.queryForObject("SELECT COUNT(*) FROM Trips", Long.class);
            dbStatus = "UP";
        } catch (DataAccessException ex) {
            dbStatus = "DOWN: " + ex.getMostSpecificCause().getMessage();
        }
        model.addAttribute("dbStatus", dbStatus);
        model.addAttribute("dbName", dbName);
        model.addAttribute("customersCount", customersCount);
        model.addAttribute("servicesCount", servicesCount);
        model.addAttribute("tripsCount", tripsCount);
        return "index";
    }

    @GetMapping("/customers")
    public String customers(Model model) {
        List<Map<String, Object>> rows = jdbc.queryForList(
            "SELECT person_id, full_name, email FROM Customers ORDER BY person_id"
        );
        model.addAttribute("customers", rows);
        return "customers";
    }

    @GetMapping("/services")
    public String services(Model model) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
            SELECT s.service_id,
                   TREAT(VALUE(s) AS Service_t).description AS description,
                   TREAT(VALUE(s) AS Service_t).calculate_cost() AS final_cost
            FROM Services s
            ORDER BY s.service_id
            """);
        model.addAttribute("services", rows);
        return "services";
    }

    @GetMapping("/trips")
    public String trips(Model model) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
            SELECT t.trip_id,
                   t.destination,
                   DEREF(t.customer).full_name AS customer_name,
                   t.total_cost() AS total_cost
            FROM Trips t
            ORDER BY t.trip_id
            """);
        model.addAttribute("trips", rows);
        return "trips";
    }
}

