using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Http;
using System.Web.Mvc;
using System.Web.Optimization;
using System.Web.Routing;

namespace DrAppAPI
{
    public class WebApiApplication : System.Web.HttpApplication
    {
        protected void Application_Start()
        {
            AreaRegistration.RegisterAllAreas();
            GlobalConfiguration.Configure(WebApiConfig.Register);
            FilterConfig.RegisterGlobalFilters(GlobalFilters.Filters);
            RouteConfig.RegisterRoutes(RouteTable.Routes);
            BundleConfig.RegisterBundles(BundleTable.Bundles);

            //when return Ok(List of all appointments) => following line is needed or else err while converting list into JSON
            GlobalConfiguration.Configuration.Formatters.JsonFormatter.SerializerSettings.ReferenceLoopHandling = Newtonsoft.Json.ReferenceLoopHandling.Ignore;

            
            /*
             * 3rd party AutoMapper gives err if init in Controller : "Mapper already initialized. You must call Initialize once per application domain/process."
             * So I used solu here : https://stackoverflow.com/questions/47241708/automapper-mapper-already-initialized-error?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
             * call this from Global.asax
             */
            App_Start.AutoMapperConfig.InitializeAutoMapper();
        }
    }
}
