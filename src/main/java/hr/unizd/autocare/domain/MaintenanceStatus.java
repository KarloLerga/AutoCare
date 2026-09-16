package hr.unizd.autocare.domain;
/** Izvedeno stanje; ne sprema se u bazu. */
public enum MaintenanceStatus {
    CONDITION_BASED, VEHICLE_INDICATOR, UNKNOWN_INTERVAL, UNKNOWN_HISTORY, OK, SOON, DUE
}
