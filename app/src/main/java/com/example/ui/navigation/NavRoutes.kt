package com.example.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "Dashboard")
    object Customers : Screen("customers", "Customer Manager")
    object Applications : Screen("applications", "Applications")
    object Services : Screen("services", "CSC & Govt Services")
    object PhotoStudio : Screen("photo_studio", "Photo Studio & BG Remover")
    object PdfTools : Screen("pdf_tools", "PDF Utilities")
    object Documents : Screen("documents", "Resume & Letters")
    object Accounting : Screen("accounting", "Income & Expense")
    object PrintCalculator : Screen("print_calculator", "Print Rate Calculator")
    object Settings : Screen("settings", "Admin & Settings")
}
