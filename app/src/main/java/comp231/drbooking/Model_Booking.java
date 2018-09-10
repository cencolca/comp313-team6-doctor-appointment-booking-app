package comp231.drbooking;
/*
 * By: SHAFIQ-UR-REHMAN
 * Purpose: Model class used for data-binding when reading or writing to db
 */
public class Model_Booking
{
    int Id_Appointment;//PK
    int Id_User;//Foreign Key
    int Id_Doc = 0;
    String Clinic;
    String Doctor;
    String AppointmentTime;
    String CreationTime;
    String User = "";//Name of Patient
    String DRAVAILABLE;


    @Override
    public String toString() {
        return  Clinic +
                "\n" + Doctor +
                "\n" + AppointmentTime;
    }
}
