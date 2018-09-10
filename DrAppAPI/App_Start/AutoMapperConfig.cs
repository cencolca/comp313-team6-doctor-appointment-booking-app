using AutoMapper;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

//3rd party AutoMapper gives err if init in Controller : "Mapper already initialized. You must call Initialize once per application domain/process."
//So I used solu here : https://stackoverflow.com/questions/47241708/automapper-mapper-already-initialized-error?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
//call this from Global.asax

namespace DrAppAPI.App_Start
{
    public class AutoMapperConfig
    {
        public static void InitializeAutoMapper()
        {
            Mapper.Initialize(cfg => cfg.CreateMap<DrAppAPI.Appointment, DrAppAPI.Models.Appointment>());
        }
    }
}