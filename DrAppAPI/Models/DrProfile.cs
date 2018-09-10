using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace DrAppAPI.Models
{
    public class DrProfile
    {
        public int id_doc { get; set; }
        public Nullable<int> Id_User { get; set; }
        public string name { get; set; }
        public string phone { get; set; }
        public string email { get; set; }
        public string specialty { get; set; }
    }
}