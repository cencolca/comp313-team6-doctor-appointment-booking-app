using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace DrAppAPI.Models
{
    public class UserBio
    {
        public int Id_User { get; set; }
        public string nameOfUser { get; set; }
        public string loginName { get; set; }
        public string pw { get; set; }
        public string address { get; set; }
        public string email { get; set; }
        public string phone { get; set; }
        public string role { get; set; }
    }
}