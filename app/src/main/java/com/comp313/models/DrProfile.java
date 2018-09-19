package com.comp313.models;

import android.databinding.BaseObservable;

/*
 * By: SHAFIQ-UR-REHMAN
 * Purpose: Model class used for data-binding when reading or writing to db
 */
public class DrProfile extends BaseObservable
{
    public int  id_doc;
    public int     Id_User = 0;
    public String  name;
    public String  phone;
    public String  email;
    public String  specialty;
}
