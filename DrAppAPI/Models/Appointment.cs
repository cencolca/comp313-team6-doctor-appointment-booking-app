using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace DrAppAPI.Models
{
    public class Appointment
    {
        public int Id_Appointment { get; set; }
        public int Id_User { get; set; }
        public string Clinic { get; set; }
        public string Doctor { get; set; }
        public string AppointmentTime { get; set; }
        public string CreationTime { get; set; }
        public Nullable<int> Id_Doc { get; set; }
        public string DRAVAILABLE { get; set; }

        public string PatientName { get; set; }//replacing PatientName w User cause conflict w DbContext entity model
    }
}