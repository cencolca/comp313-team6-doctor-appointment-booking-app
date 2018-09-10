using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using DrAppAPI.Models;
using System.Data.Entity;
using AutoMapper;
/// <summary>
/// If a col has auto-assigned constraint name, u can find that name by attempting to drop that col & the err msg will tell u the name of constraint
/// alter table dbo.users drop column [role];
/// err: Failed to execute query. Error: The object 'DF__Users__role__5535A963' is dependent on column 'role'.
/// some other sql:
/// 
///alter table dbo.users drop column[role];
///alter table users drop "DF__Users__role__5441852A";
///alter table users add default '1' for role;
///select* from users
///insert into users(nameofuser, loginname, pw, address, email, phone)
///values('name7','name7','name7','name7','name7@e.e',777);
///update users set role = 1
/// </summary>


namespace DrAppAPI.Controllers
{
    public class ValuesController : ApiController////a WebAPI ctrlr
    {

        // GET api/values
        public IEnumerable<string> Get()
        {
            return new string[] { "value1", "value2" };
        }

        // GET api/values/5
        public string Get(int id)
        {
            return "value";
        }

        // POST api/values/login = check credentials & return user_id
        /// <summary>
        /// For binding to form-data Http req MUST send either JSON str (& must specify Content-Type:application/json)--or--url-encoded (& specify Content-type:application/x-www-form-urlencoded)
        /// text/plain does NOT bind & can only accet data from query-str (not body/form data)
        /// </summary>
        /// <param name="loginPw"></param>
        /// <returns></returns>
        [Route("api/values/login")]
        public IHttpActionResult PostLogin([FromBody] UserBio userBio)//(string login, string pw) doesn't work coz [FromBody] can b applied to only 1 arg. Must have a complex type
        {
            using (ModelContainer db = new ModelContainer())
            {
                var u = db.Users.Where(x => x.loginName == userBio.loginName && x.pw == userBio.pw).FirstOrDefault();
                var user_id = u.Id_User; //db.Users.Where(x => x.loginName == userBio.loginName && x.pw == userBio.pw).Select(a => a.Id_User).FirstOrDefault();//.Any(x => x.loginName == loginPw.login && x.pw == loginPw.pw);
                UserBio userToBeReturned = new UserBio() {  Id_User = u.Id_User, address=u.address, email=u.email, loginName=u.loginName, nameOfUser=u.nameOfUser, phone=u.phone, pw=u.pw, role=u.role};
                if (user_id > 0)
                {
                    //navigation property interferes w serialization
                    return Ok(userToBeReturned);//login success
                }
            }
            return Ok(0);//login fail
        }

        
        //POST - new user biodata
        [Route("api/values/newUser")]
        public IHttpActionResult PostNewUser([FromBody] UserBio u)
        {
            using (ModelContainer db = new ModelContainer())
            {
                if(db.Users.Any(x => x.loginName == u.loginName))//loginName already exists
                {
                    return Ok(0);
                }
                else
                {
                    User newUser = new DrAppAPI.User() { loginName=u.loginName, address=u.address, email=u.email, nameOfUser=u.nameOfUser, phone =u.phone, pw=u.pw, role=u.role };
                    db.Users.Add(newUser);
                    db.SaveChanges();

                    return Ok(newUser.Id_User);//EF track newly inserted entity's auto-generated ID : https://stackoverflow.com/questions/5212751/how-can-i-get-id-of-inserted-entity-in-entity-framework
                }
            }
        }
        
        //POST - new appointment
        [Route("api/values/newAppointment")]
        public IHttpActionResult PostNewAppoint([FromBody] Appointment a)
        {
            Appointment newAppoint = new DrAppAPI.Appointment() { AppointmentTime = a.AppointmentTime, Clinic=a.Clinic, CreationTime=a.CreationTime, Doctor=a.Doctor, Id_User=a.Id_User, DRAVAILABLE=a.DRAVAILABLE};
           
            using (var db = new ModelContainer())
            {
                //chk if an appointment exists at exact same time for same Dr
                var isNotAvailable = db.Appointments.Any(app => app.Doctor == a.Doctor && app.AppointmentTime == a.AppointmentTime);
                if(isNotAvailable)
                {
                    return Ok(0);
                }
                newAppoint.Id_Doc = db.doctors.Where(d => d.name == a.Doctor).Select(d => d.id_doc).FirstOrDefault();
                db.Appointments.Add(newAppoint);
                db.SaveChanges();
                return Ok(newAppoint.Id_Appointment);
            }
        }

        //GET - All appoints for a patient
        [Route("api/values/Appointments/{user_id}")]
        public IHttpActionResult GetAllAppoints(int user_id)
        {
            //return Ok("OK");
            using (var db = new ModelContainer())
            {
                //aft updated model dig to newly added 'doctors' tbl, get err : Error getting value from 'doctors' on 'System.Data.Entity.DynamicProxies.User_08E40257012DB31... & The 'ObjectContent`1' type failed to serialize...
                db.Configuration.ProxyCreationEnabled = false; //https://stackoverflow.com/questions/13077328/serialization-of-entity-framework-objects-with-one-to-many-relationship/13077670



                /*Eager Loading : https://msdn.microsoft.com/en-us/library/jj574232(v=vs.113).aspx
                  3 types of loading asso tbl: {1}Eager .Include() {2}Lazy: just access the rel prop {3}Explicit: .Load()
                */

                /*when return Ok(List of all appointments) => following line is needed in Global.asax, or else err while converting list into JSON
                  //GlobalConfiguration.Configuration.Formatters.JsonFormatter.SerializerSettings.ReferenceLoopHandling = Newtonsoft.Json.ReferenceLoopHandling.Ignore; : https://social.msdn.microsoft.com/Forums/vstudio/en-US/a5adf07b-e622-4a12-872d-40c753417645/web-api-error-the-objectcontent1-type-failed-to-serialize-the-response-body-for-content?forum=wcf
                  Alternately, don't return objs of edmx rather create proxy entities & return those!!! : https://stackoverflow.com/questions/23098191/failed-to-serialize-the-response-in-web-api-with-json?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
                */

                //.Include() reqd using System.Data.Entity;
                var allAppoints = db.Users.Where(x => x.Id_User == user_id).Include(u=>u.Appointments); //Loada a user along w navigational prop "Appointments"
                return Ok(allAppoints.ToList());
            }
        }

        //GET - All appoints for a Dr
        [Route("api/values/AppointmentsForDr/{user_id}")]
        public IHttpActionResult GetAllAppointsForDr(int user_id)
        {
            //IQueryable<Appointment> allAppoints;
            DrAppoints DrApps = new DrAppoints();
            

            //return Ok("OK");
            using (var db = new ModelContainer())
            {
                db.Configuration.ProxyCreationEnabled = false;//solves Proxies err:"Error getting value from 'User' on 'System.Data.Entity.DynamicProxies.Appointment_A75CD158D84F..."
                //https://stackoverflow.com/questions/13077328/serialization-of-entity-framework-objects-with-one-to-many-relationship?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa

                //ViewModel
                DrAppoints drApps = new DrAppoints();
                drApps.Appointments = new List<Models.Appointment>();
                DrAppAPI.Models.Appointment app;



                //Auto Map bw DbContext objs & ViewModel objs(aka DTO = Data Transfer Objects): http://automapper.readthedocs.io/en/latest/Getting-started.html
                //Mapper.Initialize(cfg => cfg.CreateMap<DrAppAPI.Appointment, DrAppAPI.Models.Appointment>());
                
                //3rd party AutoMapper gives err if init in Controller : "Mapper already initialized. You must call Initialize once per application domain/process."
                //So I used solu here : https://stackoverflow.com/questions/47241708/automapper-mapper-already-initialized-error?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
                //call this from Global.asax
                


                /*
                ////or - to use instance instead of Static - do as below:
                var config = new AutoMapper.MapperConfiguration(cfg => cfg.CreateMap<DrAppAPI.Appointment, DrAppAPI.Models.Appointment>());
                var mapper = config.CreateMapper();//or: = new Mapper(config);
                */

                //.Include() reqd using System.Data.Entity;
                var DrName = db.Users.Where(x => x.Id_User == user_id).Select(n=>n.nameOfUser).FirstOrDefault(); //.Include(u => u.Appointments); //all appoints for a user
                var allAppoints = db.Appointments.Where(a => a.Doctor == DrName).Include(a=>a.User);//.Select(a=> Mapper.Map<DrAppAPI.Appointment, Appointment>(a));

                foreach (var item in allAppoints)
                {
                    /*
                    app = new DrAppAPI.Models.Appointment()
                    {
                        
                          Id_Appointment = item.Id_Appointment,
                          Id_User  = item.Id_User,
                    Clinic = item.Clinic,
                    Doctor = item.Doctor,
                    AppointmentTime = item.AppointmentTime,
                                CreationTime = item.CreationTime,
                                User = item.User
                            };
                    drApps.Appointments.Add(app);
                    */


                    //app = mapper.Map<DrAppAPI.Models.Appointment>(item);
                    app = Mapper.Map<DrAppAPI.Appointment, DrAppAPI.Models.Appointment>(item);
                    app.PatientName = item.User.nameOfUser;
                    drApps.Appointments.Add(app);

                }
                /* List of pt's apps return json-arr while for docs it's an obj (not arr) so Android parsing errs..
                 * To solve that, return it as an arr
                 */
                return Ok(new DrAppoints[] { drApps });//Ok(drApps.Appointments.ToList()); will NOT give the JSON with parent array of "Appointments"
                                  //err serialisind  : "The operation cannot be completed because the DbContext has been disposed." : https://stackoverflow.com/questions/13617698/the-operation-cannot-be-completed-because-the-dbcontext-has-been-disposed-error?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa

                //fold selected ctrl+M+H
                //Sample JSON returned is:
                /*
                 {
                    "Appointments": [
                        {
                            "Id_Appointment": 1,
                            "Id_User": 1,
                            "Clinic": "test clinic",
                            "Doctor": "Lady Doctor 1",
                            "AppointmentTime": "Wed, 30 May 2018 11:27 AM",
                            "CreationTime": "Wed, 30 May 2018 11:27 AM",
                            "PatientName": "John Doe"
                        },
                        {
                            "Id_Appointment": 2,
                            "Id_User": 1,
                            "Clinic": "Address : 940 progress Ave Toronto",
                            "Doctor": "Lady Doctor 1",
                            "AppointmentTime": "Sun, 20 May 2018 10:27 PM",
                            "CreationTime": "Sun, 20 May 2018 10:27 PM",
                            "PatientName": "John Doe"
                        },
                        {
                            "Id_Appointment": 4005,
                            "Id_User": 2,
                            "Clinic": "test clinic",
                            "Doctor": "Lady Doctor 1",
                            "AppointmentTime": "Fri, 1 Jun 2018 03:34 PM",
                            "CreationTime": "Tue, 22 May 2018 03:35 AM",
                            "PatientName": "Name1"
                        }
                    ]
                }
                 */
            }
        }

        //GET - array of doc names to populat dropdown
        [Route("api/values/doctors")]
        public IHttpActionResult GetDrNames()
        {
            using (var db = new ModelContainer())
            {
                //return arr o DrProfile objs



                //string[] testing = { "aaa", "bbb", "ccc"};
                var allDrs = db.doctors.Select(d => new DrProfile() { email=d.email, id_doc=d.id_doc, Id_User=d.Id_User, name=d.name, phone=d.phone, specialty=d.specialty} /*d.name + " (" + d.specialty + ")"*/).ToArray();// .Users.Where(x => x.Id_User == user_id).Include(u => u.Appointments); //Loada a user along w navigational prop "Appointments"



                return Ok(allDrs);
            }
        }

        // POST - update appointment
        //{domain/ip+port}/api/values/UpdateAppoint/5=appoint ID
        [Route("api/values/UpdateAppoint/{id_app}")]
        public IHttpActionResult PostEditAppoint(int id_app, [FromBody]Appointment updatedApp)// = modified existing appointment
        {
            /*
            //chk if appoint time has another appoint within 30 min before it
            DateTime timeFrom, timeTo;
            timeTo = DateTime.Parse(appoint.AppointmentTime);
            timeFrom = timeTo.AddMinutes(-30);
            */
            using (var db = new ModelContainer())
            {
                var existingApp = db.Appointments.SingleOrDefault(a => a.Id_Appointment == id_app);
                if(null != existingApp)
                {
                    existingApp.AppointmentTime = updatedApp.AppointmentTime;
                    existingApp.Clinic = updatedApp.Clinic;
                    existingApp.Doctor = updatedApp.Doctor;
                    existingApp.Id_Doc = updatedApp.Id_Doc;


                    db.SaveChanges();
                    return Ok(existingApp.Id_Appointment + " updated successfully");
                }
                return Ok("Could not update appointment! - Please call clinic");//fail
            }

        }

        
       //POST - Delete appointment
       [Route("api/values/DeleteAppointment/{id}")]
       public IHttpActionResult PostEditAppoint([FromUri]int id)
       {
            using (var db = new ModelContainer())
            {
                var existingApp = db.Appointments.SingleOrDefault(a => a.Id_Appointment == id);
                if (null != existingApp)
                {
                    db.Entry(existingApp).State = EntityState.Deleted;
                    db.SaveChanges();
                    return Ok(existingApp.Id_Appointment + " Deleted successfully");
                }
                return Ok("Could not DELETE appointment! - Please call clinic");//fail
            }
        }


        // DELETE api/values/5
        public void Delete(int id)
        {
        }
    }
}
