package kfupm.clinic.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import kfupm.clinic.api.Result;
import kfupm.clinic.ds.*;
import kfupm.clinic.model.*;

/**
 * Students implement the system logic here.
 *
 * Rules:
 * - Use the provided custom data structures.
 * - Do NOT use Java built-in maps/trees/priority queues for storage.
 */
public class ClinicServiceImpl implements ClinicService {

    // Hash tables
    private final HashTable<String, Patient> patientsById = new HashTable<>();
    private final HashTable<String, Appointment> apptsById = new HashTable<>();

    // Appointment schedule index (AVL)
    private final AVLTree<AppointmentKey, Appointment> apptsByTime = new AVLTree<>();

    // Walk-ins and urgent
    private final LinkedQueue<Patient> walkIns = new LinkedQueue<>();
    private final MaxHeap<UrgentPatient> urgentHeap = new MaxHeap<>((a, b) -> {
        // Higher severity first; tie-break earlier arrival first.
        if (a.severity() != b.severity()) return Integer.compare(a.severity(), b.severity());
        // earlier arrival should win => invert compare so earlier is "greater"
        return Long.compare(b.arrivalEpochMillis(), a.arrivalEpochMillis());
    });

    // Undo + log
    private final LinkedStack<Action> undo = new LinkedStack<>();
    private final SinglyLinkedList<VisitLogEntry> log = new SinglyLinkedList<>();

    private final StringMatcher naive = new NaiveMatcher();
    private final StringMatcher kmp = new KMPMatcher();

    private int nextApptId = 1;

    @Override
    public Result<Void> addPatient(String id, String name, String phone) {
        // TODO: validate, check duplicates using hash table, insert, record undo
        // check all variables are there
        if (id == null){
            return Result.fail("Patient ID is required");
        }
        if (name == null){
            return Result.fail("Patient name is required");
        }
        if (phone == null){
            return Result.fail("Patient phone is required");
        }
        Patient existing = patientsById.get(id);

        if (existing != null){
            return Result.fail("Patient already exists")
        }
        Patient patient = new Patient(id, name, phone);
        patientsById.put(id, patient);

        return Result.ok(null, "Patient added");
    }

    @Override
    public Result<Patient> findPatient(String id) {
        // TODO: use hash table get
        if (id == null){
            return Result.fail("Patient ID is required");
        }

        Patient patient = patientsById.get(id);

        if (patient == null){
            return Result.fail("Patient not found");
        }

        return Result.ok(patient, "Patient found");
    }

    @Override
    public Result<Void> deletePatient(String id) {
        // TODO: remove from hash table, record undo
        if (id == null){
            return Result.fail("Patient ID is required");
        }
        Patient removed = patientsById.remove(id);

        if (removed == null){
            return Result.fail("Patient not found");
        }
        return Result.ok(null, "Patient deleted");
    }

    @Override
    public Result<String> addAppointment(String patientId, LocalDate date, LocalTime time, String doctor) {
        // TODO: ensure patient exists; create appointmentId; insert into AVL + hash; record undo
        if(patientId == null){
            return Result.fail("Patient ID is required.");
        }
        if(date == null){
            return Result.fail("Date is required.");
        }
        if(time == null){
            return Result.fail("Time is required.");
        }
        if(doctor == null){
            return Result.fail("Doctor name is required.");
        }
        Patient patient = patientsById.get(patientId);

        if (patient == null){
            return Result.fail("Patient not found");
        }

        String appointmentId = newAppointmentId();

        Appointment appointment = new Appointment(
            appointmentId,
            patient.id(),
            date,
            time,
            doctor
        );

        AppointmentKey key = new AppointmentKey(date, time, appointmentId);

        apptsById.put(appointmentId, appointment);
        apptsByTime.put(key, appointment);

        return Result.ok(appointmentId, "Appointment added");
    }

    @Override
    public Result<Void> cancelAppointment(String appointmentId) {
        // TODO: use hash to find appt; remove from AVL + hash; record undo
        if(appointmentId == null){
            return Result.fail("Appointment ID is required");
        }
         Appointment appt = apptsById.get(appointmentId);

        if (appt == null){
            return Result.fail("Appointment not found.");
        }

        apptsById.remove(appointmentId);

        AppointmentKey key = new AppointmentKey(
            appt.date(),
            appt.time(),
            appt.id()
        );

        apptsByTime.remove(key);

        return Result.ok(null, "Appointment cancelled");
    }

    @Override
    public Result<Appointment> findAppointment(String appointmentId) {
        // TODO: use hash table
        if (appointmentId == null){
            return Result.fail("Appointment ID is required");
        }
        Appointment appt = apptsById.get(appointmentId);

        if (appt == null){
            return Result.fail("Appointment not found");
        }
        return Result.ok(appt, "Appointment found");
    }

    @Override
    public List<Appointment> viewDay(LocalDate date) {
        // TODO: in-order traverse AVL and filter by date, OR implement date range traversal
        return new ArrayList<>();
    }

    @Override
    public List<Appointment> viewRange(LocalDate date, LocalTime start, LocalTime end) {
        // TODO: range query traversal on AVL for (date,start) .. (date,end)
        return new ArrayList<>();
    }

    @Override
    public Result<Void> addWalkIn(String patientId) {
        // TODO: ensure patient exists; enqueue; record undo
        if (patientId == null){
            return Result.fail("Patient ID is not found");
        }
        Patient patient = patientsById.get(patientId);

        if (patient == null){
            return Result.fail("Patient not found");
        }

        walkIns.enqueue(patient);

        return Result.ok(null, "Walk_in added");
    }

    @Override
    public List<Patient> viewWalkIns() {
        // Non-destructive view
        return walkIns.toList();
    }

    @Override
    public Result<Void> addUrgent(String patientId, int severity) {
        // TODO: validate severity; ensure patient exists; heap push; record undo
        throw new UnsupportedOperationException("TODO: ClinicServiceImpl.addUrgent");
    }

    @Override
    public Result<UrgentPatient> peekUrgent() {
        UrgentPatient up = urgentHeap.peek();
        if (up == null) return Result.fail("No urgent patients.");
        return Result.ok(up, "Most urgent patient.");
    }

    @Override
    public List<UrgentPatient> viewUrgentsSnapshot() {
        return urgentHeap.toListSnapshot();
    }

    @Override
    public Result<VisitLogEntry> serveNext(String doctor, String note) {
        // TODO: serving policy: urgent > walk-in > earliest appointment
        // TODO: append log entry, record undo
        throw new UnsupportedOperationException("TODO: ClinicServiceImpl.serveNext");
    }

    @Override
    public List<VisitLogEntry> printLog() {
        return log.toList();
    }

    @Override
    public List<VisitLogEntry> searchLogNaive(String pattern) {
        // TODO: iterate log entries; match pattern in note using NaiveMatcher
        throw new UnsupportedOperationException("TODO: ClinicServiceImpl.searchLogNaive");
    }

    @Override
    public List<VisitLogEntry> searchLogKmp(String pattern) {
        // TODO: iterate log entries; match pattern in note using KMPMatcher
        throw new UnsupportedOperationException("TODO: ClinicServiceImpl.searchLogKmp");
    }

    @Override
    public Result<Action> undo() {
        // TODO: pop undo stack and reverse last action
        throw new UnsupportedOperationException("TODO: ClinicServiceImpl.undo");
    }

    // Helpers you may want
    private String newAppointmentId() {
        return "A" + (nextApptId++);
    }
}
