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
            return Result.fail("Patient already exists");
        }
        Patient patient = new Patient(id, name, phone);
        patientsById.put(id, patient);
        undo.push(new Action(ActionType.ADD_PATIENT, patient));

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
        undo.push(new Action(ActionType.DELETE_PATIENT, removed));
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
            patient.name(),
            patient.phone(),
            date,
            time,
            doctor
        );

        AppointmentKey key = new AppointmentKey(date, time, appointmentId);

        apptsById.put(appointmentId, appointment);
        apptsByTime.put(key, appointment);
        undo.push(new Action(ActionType.ADD_APPT, appointment));

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
            appt.appointmentId()
        );

        apptsByTime.remove(key);
        undo.push(new Action(ActionType.CANCEL_APPT, appt));

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
        List<Appointment> result = new ArrayList<>();
        apptsByTime.inOrder((key, appt) -> {
            if (key.date().equals(date)) result.add(appt);
        });
        return result;
    }

    @Override
    public List<Appointment> viewRange(LocalDate date, LocalTime start, LocalTime end) {
        List<Appointment> result = new ArrayList<>();
        apptsByTime.inOrder((key, appt) -> {
            if (key.date().equals(date)
                    && !key.time().isBefore(start)
                    && !key.time().isAfter(end)) {
                result.add(appt);
            }
        });
        return result;
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
        undo.push(new Action(ActionType.ADD_WALKIN, patient));

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
        if (severity < 1 || severity > 5)
            return Result.fail("Severity must be between 1 and 5.");
        Patient p = patientsById.get(patientId);
        if (p == null) return Result.fail("Patient '" + patientId + "' not found.");
        UrgentPatient up = new UrgentPatient(p, severity, System.currentTimeMillis());
        urgentHeap.push(up);
        undo.push(new Action(ActionType.ADD_URGENT, up));
        return Result.ok(null, "Urgent patient added: " + p.name() + " (severity=" + severity + ")");
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
        String type;
        Patient patient;

        // Priority: urgent > walk-in > earliest appointment
        if (!urgentHeap.isEmpty()) {
            UrgentPatient up = urgentHeap.pop();
            patient = up.patient();
            type = "URGENT";
        } else if (!walkIns.isEmpty()) {
            patient = walkIns.dequeue();
            type = "WALKIN";
        } else {
            AVLTree.Entry<AppointmentKey, Appointment> entry = apptsByTime.minEntry();
            if (entry == null) return Result.fail("No patients to serve.");
            Appointment appt = entry.value();
            apptsById.remove(appt.appointmentId());
            apptsByTime.remove(entry.key());
            patient = patientsById.get(appt.patientId());
            if (patient == null) patient = new Patient(appt.patientId(), appt.patientName(), appt.phone());
            type = "APPOINTMENT";
        }

        VisitLogEntry entry = new VisitLogEntry(
                System.currentTimeMillis(),
                patient.id(), patient.name(),
                type, doctor, note);
        log.addLast(entry);
        undo.push(new Action(ActionType.SERVE, entry));
        return Result.ok(entry, "Served: " + patient.name() + " [" + type + "]");
    }

    @Override
    public List<VisitLogEntry> printLog() {
        return log.toList();
    }

    @Override
    public List<VisitLogEntry> searchLogNaive(String pattern) {
        // TODO: iterate log entries; match pattern in note using NaiveMatcher
        return searchLog(naive, pattern);
    }

    @Override
    public List<VisitLogEntry> searchLogKmp(String pattern) {
        // TODO: iterate log entries; match pattern in note using KMPMatcher
        return searchLog(kmp, pattern);
    }

    @Override
    public Result<Action> undo() {
        // TODO: pop undo stack and reverse last action
        if (undo.isEmpty()) return Result.fail("Nothing to undo.");
        Action action = undo.pop();

        switch (action.type()) {
            case ADD_PATIENT -> {
                Patient p = (Patient) action.payload();
                patientsById.remove(p.id());
                return Result.ok(action, "Undone: ADD_PATIENT " + p.id());
            }
            case DELETE_PATIENT -> {
                Patient p = (Patient) action.payload();
                patientsById.put(p.id(), p);
                return Result.ok(action, "Undone: DELETE_PATIENT " + p.id() + " (restored)");
            }
            case ADD_APPT -> {
                Appointment appt = (Appointment) action.payload();
                // Only undo if it still exists (wasn't already cancelled)
                if (apptsById.get(appt.appointmentId()) != null) {
                    apptsById.remove(appt.appointmentId());
                    apptsByTime.remove(new AppointmentKey(appt.date(), appt.time(), appt.appointmentId()));
                }
                return Result.ok(action, "Undone: ADD_APPT " + appt.appointmentId());
            }
            case CANCEL_APPT -> {
                Appointment appt = (Appointment) action.payload();
                apptsById.put(appt.appointmentId(), appt);
                apptsByTime.put(new AppointmentKey(appt.date(), appt.time(), appt.appointmentId()), appt);
                return Result.ok(action, "Undone: CANCEL_APPT " + appt.appointmentId() + " (restored)");
            }
            case ADD_WALKIN -> {
                Patient p = (Patient) action.payload();
                walkIns.remove(p);
                return Result.ok(action, "Undone: ADD_WALKIN " + p.id());
            }
            case ADD_URGENT -> {
                UrgentPatient up = (UrgentPatient) action.payload();
                urgentHeap.remove(up);
                return Result.ok(action, "Undone: ADD_URGENT " + up.patient().id());
            }
            case SERVE -> {
                ServeUndoData data = (ServeUndoData) action.payload();

                log.removeLast();

                if (data.type().equals("URGENT")) {
                        urgentHeap.push(data.urgentPatient());
                } else if (data.type().equals("WALKIN")) {
                         walkIns.enqueueFront(data.patient());
                } else if (data.type().equals("APPOINTMENT")) {
                         Appointment appt = data.appointment();
                          apptsById.put(appt.appointmentId(), appt);
                          apptsByTime.put(new AppointmentKey(appt.date(), appt.time(), appt.appointmentId()), appt);
                 }

                return Result.ok(action, "Undone: SERVE " + data.patient().id());
        }
    }

    // Helpers you may want
    private String newAppointmentId() {
        return "A" + (nextApptId++);
    }
    private List<VisitLogEntry> searchLog(StringMatcher matcher, String pattern) {
    List<VisitLogEntry> result = new ArrayList<>();

    for (VisitLogEntry entry : log.toList()) {
        if (matcher.contains(entry.notes(), pattern)) {
            result.add(entry);
        }
    }

    return result;
}
}
