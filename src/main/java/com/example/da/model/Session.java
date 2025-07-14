package com.example.da.model;

public class Session {
    private static Users currentUser;
    public static void setCurrentUser(Users user) { currentUser = user; }
    public static Users getCurrentUser() { return currentUser; }
} 