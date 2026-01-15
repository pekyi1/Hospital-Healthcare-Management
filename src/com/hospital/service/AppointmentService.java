package com.hospital.service;

import com.hospital.dao.AppointmentDAO;
import com.hospital.model.Appointment;
import com.hospital.util.PerformanceLogger;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppointmentService {

    private final AppointmentDAO appointmentDAO;

    public AppointmentService() {
        this.appointmentDAO = new AppointmentDAO();
    }

    public void scheduleAppointment(Appointment appointment) throws SQLException {
        long start = System.currentTimeMillis();
        appointmentDAO.addAppointment(appointment);
        PerformanceLogger.log("scheduleAppointment", start);
    }

    public List<Appointment> getAllAppointments() throws SQLException {
        long start = System.currentTimeMillis();
        List<Appointment> appointments = appointmentDAO.getAllAppointments();
        PerformanceLogger.log("getAllAppointments", start);
        return appointments;
    }

    public void updateAppointment(Appointment appointment) throws SQLException {
        long start = System.currentTimeMillis();
        appointmentDAO.updateAppointment(appointment);
        PerformanceLogger.log("updateAppointment", start);
    }

    public void cancelAppointment(int id) throws SQLException {
        long start = System.currentTimeMillis();
        appointmentDAO.deleteAppointment(id);
        PerformanceLogger.log("cancelAppointment", start);
    }

    public int getAppointmentCount() throws SQLException {
        return appointmentDAO.getAppointmentCount();
    }

    public Map<String, Integer> getAppointmentsPerDay() throws SQLException {
        long start = System.currentTimeMillis();
        List<Appointment> all = appointmentDAO.getAllAppointments();
        Map<String, Integer> stats = new HashMap<>();

        // Initialize days
        String[] days = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
        for (String day : days)
            stats.put(day, 0);

        // Simple aggregation based on DayOfWeek
        for (Appointment a : all) {
            String day = a.getAppointmentDate().getDayOfWeek().name(); // MONDAY...
            String shortDay = day.substring(0, 1).toUpperCase() + day.substring(1, 3).toLowerCase(); // Mon
            stats.put(shortDay, stats.getOrDefault(shortDay, 0) + 1);
        }

        PerformanceLogger.log("getAppointmentsPerDay", start);
        return stats;
    }
}
