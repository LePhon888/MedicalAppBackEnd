package com.med.controller;

import com.med.model.*;
import com.med.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.print.Doc;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {
    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private DoctorService doctorService;

    @Autowired
    private HourService hourService;

    @Autowired
    private UserService userService;

    @Autowired
    private FeeService feeService;

    @Autowired
    private DepartmentService departmentService;


    @PostMapping
    public ResponseEntity create(@RequestBody Map<String, Object> appointment) {
        try {
            Appointment savedAppointment = new Appointment();
            Map<String, Object> userObject = (Map<String, Object>) appointment.get("user");
            Map<String, Object> hourObject = (Map<String, Object>) appointment.get("hour");
            Map<String, Object> doctorObject = (Map<String, Object>) appointment.get("doctor");
            Map<String, Object> doctorUser = (Map<String, Object>) doctorObject.get("user");
            Map<String, Object> departmentObject = (Map<String, Object>) doctorObject.get("department");
            Map<String, Object> feeObject = (Map<String, Object>) appointment.get("fee");
            Map<String, Object> registerUserObject = (Map<String, Object>) appointment.get("registerUser");

            User u = new User();
            u.setFirstName((String) userObject.get("firstName"));
            u.setLastName((String) userObject.get("lastName"));
            String dateString = (String) userObject.get("birthday");

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = dateFormat.parse(dateString);

            u.setBirthday(date);
            u.setAddress((String) userObject.get("address"));
            u.setGender((Integer) userObject.get("gender"));
            u.setPhoneNumber((String) userObject.get("phoneNumber"));
            u.setEmail((String) userObject.get("email"));
            u.setIsActive((short) 1);
            u.setUserRole("ROLE_PATIENT");
            userService.create(u);


            Doctor doctor = new Doctor();
            User user = userService.getById((Integer) doctorUser.get("id"));
            Department department = departmentService.getById((Integer) departmentObject.get("id"));
            doctor.setUser(user);
            doctor.setDepartment(department);
            doctorService.create(doctor);

            Hour hour = this.hourService.getById((Integer) hourObject.get("id"));
            Fee fee = this.feeService.getById((Integer) feeObject.get("id"));

            User registerUser = this.userService.getById((Integer) registerUserObject.get("id"));


            savedAppointment.setReason((String) appointment.get("reason"));
            savedAppointment.setReportImage((String) appointment.get("reportImage"));
            savedAppointment.setDate(dateFormat.parse((String) appointment.get("date")));
            savedAppointment.setIsConfirm((short) 0);
            savedAppointment.setIsPaid((short) 0);
            savedAppointment.setFee(fee);
            savedAppointment.setHour(hour);
            savedAppointment.setDoctor(doctor);
            savedAppointment.setUser(u);
            savedAppointment.setRegisterUser(registerUser);
            appointmentService.create(savedAppointment);
            return new ResponseEntity<>(savedAppointment, HttpStatus.CREATED);
        } catch(Exception e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

}

    @GetMapping
    public List<Appointment> getAll() {
        return appointmentService.getAll();
    }

    @GetMapping("/{id}")
    public List<Appointment> getByRegisterUser(@PathVariable Integer id) {
        User u = this.userService.getById(id);
        return appointmentService.getAppointmentsByRegisterUser(u);
    }

    @PutMapping("/{id}/is-confirm")
    public ResponseEntity updateIsConfirm(@PathVariable Integer id, @RequestParam Short isConfirm) {
        try {
            Optional<Appointment> optionalAppointment =
                    Optional.ofNullable(appointmentService.getById(id));

            if (optionalAppointment.isPresent()) {
                Appointment appointment = optionalAppointment.get();
                appointment.setIsConfirm(isConfirm);

                appointmentService.create(appointment);

                return new ResponseEntity<>(appointment, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Appointment not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }


}