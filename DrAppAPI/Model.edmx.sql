
-- --------------------------------------------------
-- Entity Designer DDL Script for SQL Server 2005, 2008, 2012 and Azure
-- --------------------------------------------------
-- Date Created: 05/19/2018 00:39:52
-- Generated from EDMX file: c:\users\shafi\documents\visual studio 2015\Projects\DrAppAPI\DrAppAPI\Model.edmx
-- --------------------------------------------------

SET QUOTED_IDENTIFIER OFF;
GO
USE [proj1db];
GO
IF SCHEMA_ID(N'dbo') IS NULL EXECUTE(N'CREATE SCHEMA [dbo]');
GO

-- --------------------------------------------------
-- Dropping existing FOREIGN KEY constraints
-- --------------------------------------------------

IF OBJECT_ID(N'[dbo].[FK_UserAppointment]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Appointments] DROP CONSTRAINT [FK_UserAppointment];
GO

-- --------------------------------------------------
-- Dropping existing tables
-- --------------------------------------------------

IF OBJECT_ID(N'[dbo].[Appointments]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Appointments];
GO
IF OBJECT_ID(N'[dbo].[Users]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Users];
GO

-- --------------------------------------------------
-- Creating all tables
-- --------------------------------------------------

-- Creating table 'Appointments'
CREATE TABLE [dbo].[Appointments] (
    [Id_Appointment] int IDENTITY(1,1) NOT NULL,
    [Id_User] int  NOT NULL,
    [Clinic] nvarchar(max)  NOT NULL,
    [Doctor] nvarchar(max)  NOT NULL,
    [AppointmentTime] nvarchar(max)  NOT NULL,
    [CreationTime] nvarchar(max)  NOT NULL
);
GO

-- Creating table 'Users'
CREATE TABLE [dbo].[Users] (
    [Id_User] int IDENTITY(1,1) NOT NULL,
    [nameOfUser] nvarchar(max)  NULL,
    [loginName] nvarchar(max)  NOT NULL,
    [pw] nvarchar(max)  NOT NULL,
    [address] nvarchar(max)  NOT NULL,
    [email] nvarchar(max)  NOT NULL,
    [phone] nvarchar(max)  NOT NULL
);
GO

-- --------------------------------------------------
-- Creating all PRIMARY KEY constraints
-- --------------------------------------------------

-- Creating primary key on [Id_Appointment] in table 'Appointments'
ALTER TABLE [dbo].[Appointments]
ADD CONSTRAINT [PK_Appointments]
    PRIMARY KEY CLUSTERED ([Id_Appointment] ASC);
GO

-- Creating primary key on [Id_User] in table 'Users'
ALTER TABLE [dbo].[Users]
ADD CONSTRAINT [PK_Users]
    PRIMARY KEY CLUSTERED ([Id_User] ASC);
GO

-- --------------------------------------------------
-- Creating all FOREIGN KEY constraints
-- --------------------------------------------------

-- Creating foreign key on [Id_User] in table 'Appointments'
ALTER TABLE [dbo].[Appointments]
ADD CONSTRAINT [FK_UserAppointment]
    FOREIGN KEY ([Id_User])
    REFERENCES [dbo].[Users]
        ([Id_User])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating non-clustered index for FOREIGN KEY 'FK_UserAppointment'
CREATE INDEX [IX_FK_UserAppointment]
ON [dbo].[Appointments]
    ([Id_User]);
GO

-- --------------------------------------------------
-- Script has ended
-- --------------------------------------------------