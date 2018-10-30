package com.comp313.models;
/*
 * By: SHAFIQ-UR-REHMAN
 * Purpose: Model class used for data-binding when reading or writing to db
 */
public class Booking
{
    public int getId_Appointment() {
        return Id_Appointment;
    }

    public void setId_Appointment(int id_Appointment) {
        Id_Appointment = id_Appointment;
    }

    public String getId_User() {
        return Id_User;
    }

    public void setId_User(String id_User) {
        Id_User = id_User;
    }

    public int getId_Doc() {
        return Id_Doc;
    }

    public void setId_Doc(int id_Doc) {
        Id_Doc = id_Doc;
    }

    public String getClinic() {
        return Clinic;
    }

    public void setClinic(String clinic) {
        Clinic = clinic;
    }

    public String getDoctor() {
        return Doctor;
    }

    public void setDoctor(String doctor) {
        Doctor = doctor;
    }

    public String getAppointmentTime() {
        return AppointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        AppointmentTime = appointmentTime;
    }

    public String getCreationTime() {
        return CreationTime;
    }

    public void setCreationTime(String creationTime) {
        CreationTime = creationTime;
    }

    public String getUser() {
        return User;
    }

    public void setUser(String user) {
        User = user;
    }

    public String getDRAVAILABLE() {
        return DRAVAILABLE;
    }

    public void setDRAVAILABLE(String DRAVAILABLE) {
        this.DRAVAILABLE = DRAVAILABLE;
    }

    private int Id_Appointment;//PK
    private String Id_User;//Foreign Key
    private int Id_Doc = 0;
    private String Clinic;
    private String Doctor;
    private String AppointmentTime;
    private String CreationTime;
    private String User = "";//Name of Patient
    private String DRAVAILABLE;

    public String getAppointmentKey() {
        return AppointmentKey;
    }

    public void setAppointmentKey(String appointmentKey) {
        AppointmentKey = appointmentKey;
    }

    private String AppointmentKey;


    @Override
    public String toString() {
        return  Clinic +
                "\n" + Doctor +
                "\n" + AppointmentTime;
    }
}
