package com.example.data.model

data class CyberServiceItem(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val officialUrl: String,
    val standardFee: Double,
    val requiredDocuments: List<String>,
    val tips: String
)

object CyberServicesCatalog {
    val categories = listOf(
        "All Services",
        "Aadhaar Services",
        "PAN Card",
        "Voter ID (ECI)",
        "Bihar RTPS & Land",
        "Govt Jobs & Exams",
        "Scholarships",
        "Admissions",
        "Passport Seva",
        "CSC & Utilities"
    )

    val allServices = listOf(
        // Aadhaar Services
        CyberServiceItem(
            id = "aadhaar_download",
            name = "Aadhaar Card Download & PVC Print",
            category = "Aadhaar Services",
            description = "Download e-Aadhaar PDF or order Official Plastic PVC Card with hologram directly to address.",
            officialUrl = "https://myaadhaar.uidai.gov.in/",
            standardFee = 50.0,
            requiredDocuments = listOf("Aadhaar Number or Enrolment ID (EID)", "Registered Mobile for OTP"),
            tips = "Plastic PVC card print with barcode and lamination available at counter."
        ),
        CyberServiceItem(
            id = "aadhaar_update_status",
            name = "Aadhaar Update & Biometric Lock Status",
            category = "Aadhaar Services",
            description = "Check status of demographic/biometric address or photo updates and lock/unlock biometric security.",
            officialUrl = "https://myaadhaar.uidai.gov.in/check-status",
            standardFee = 30.0,
            requiredDocuments = listOf("SRN (Service Request Number) or URN", "Aadhaar Number"),
            tips = "Biometric lock prevents unauthorized cloning and thumb impression frauds."
        ),

        // PAN Card Services
        CyberServiceItem(
            id = "pan_new_49a",
            name = "New PAN Card Application (Form 49A)",
            category = "PAN Card",
            description = "Instant e-PAN generation and physical PAN card delivery through NSDL or UTIITSL portal.",
            officialUrl = "https://www.onlineservices.nsdl.com/paam/endUserRegisterContact.html",
            standardFee = 200.0,
            requiredDocuments = listOf("Aadhaar Card", "2 Passport Size Photos", "Active Mobile Number & Email"),
            tips = "Physical card arrives by speed post in 10-15 days. Digital e-PAN issued in 48 hours."
        ),
        CyberServiceItem(
            id = "pan_correction",
            name = "PAN Card Correction & Reprint",
            category = "PAN Card",
            description = "Correction in Name, Father's Name, DOB, Photo, or re-ordering lost/damaged PAN card.",
            officialUrl = "https://www.pan.utiitsl.com/PAN/newAOpportunity.do",
            standardFee = 220.0,
            requiredDocuments = listOf("Existing PAN Card Copy", "Aadhaar Card", "Supporting Proof for Correction"),
            tips = "Father's name spellings must match exactly with Matriculation certificate."
        ),
        CyberServiceItem(
            id = "pan_aadhaar_link",
            name = "Link PAN with Aadhaar",
            category = "PAN Card",
            description = "Mandatory Income Tax linkage of PAN with Aadhaar number and challan fee payment.",
            officialUrl = "https://eportal.incometax.gov.in/iec/foservices/#/pre-login/bl-link-aadhaar",
            standardFee = 50.0,
            requiredDocuments = listOf("PAN Card", "Aadhaar Card", "Mobile for OTP"),
            tips = "Check linking status first before paying penalty challan."
        ),

        // Voter ID Services
        CyberServiceItem(
            id = "voter_new_form6",
            name = "New Voter Registration (Form 6)",
            category = "Voter ID (ECI)",
            description = "Apply for voter card for Indian citizens who reached 18 years of age.",
            officialUrl = "https://voters.eci.gov.in/",
            standardFee = 80.0,
            requiredDocuments = listOf("Aadhaar Card / Age Proof", "Address Proof", "Passport Size Photo", "Family Voter ID (Optional)"),
            tips = "EPIC number is generated within 2-4 weeks after BLO physical verification."
        ),
        CyberServiceItem(
            id = "voter_epic_download",
            name = "e-EPIC Voter Card Download",
            category = "Voter ID (ECI)",
            description = "Instant digital download of high resolution official colored Voter ID card.",
            officialUrl = "https://voters.eci.gov.in/",
            standardFee = 40.0,
            requiredDocuments = listOf("EPIC Number or Reference Form Number", "Mobile registered on portal"),
            tips = "Print on glossy 250GSM photo paper with thermal pouch lamination for durability."
        ),

        // Bihar RTPS & Land Records
        CyberServiceItem(
            id = "bihar_niwas",
            name = "Residential Certificate (Niwas Praman Patra)",
            category = "Bihar RTPS & Land",
            description = "Bihar ServicePlus official digital residence certificate issued by CO/SDO/DM.",
            officialUrl = "https://serviceonline.bihar.gov.in/",
            standardFee = 100.0,
            requiredDocuments = listOf("Aadhaar Card", "Recent Passport Photo", "Mobile OTP", "Ward / Panchayat details"),
            tips = "Standard delivery period is 10 working days. Download PDF with digital signature."
        ),
        CyberServiceItem(
            id = "bihar_jati_income",
            name = "Caste & Income Certificate (Jati & Aay)",
            category = "Bihar RTPS & Land",
            description = "Official Caste certificate (SC/ST/EBC/BC) and Family Annual Income certificate for Bihar.",
            officialUrl = "https://serviceonline.bihar.gov.in/",
            standardFee = 100.0,
            requiredDocuments = listOf("Aadhaar Card", "Land Khatiyan/LPC or Salary Proof", "Passport Photo", "Mobile OTP"),
            tips = "Required for student fee concessions, scholarships, and state quota reservations."
        ),
        CyberServiceItem(
            id = "bihar_obc_ncl",
            name = "Non-Creamy Layer (OBC-NCL) & EWS",
            category = "Bihar RTPS & Land",
            description = "OBC Non-Creamy Layer Certificate and 10% EWS General Reservation certificate for state/central jobs.",
            officialUrl = "https://serviceonline.bihar.gov.in/",
            standardFee = 150.0,
            requiredDocuments = listOf("Jati, Niwas, and Aay Certificates", "Aadhaar Card", "Self Declaration Form", "Photo"),
            tips = "Requires previously approved Jati, Niwas and Aay certificate application numbers."
        ),
        CyberServiceItem(
            id = "bihar_bhumi_dakhil",
            name = "Bihar Bhumi - Dakhil Kharij & Rasid",
            category = "Bihar RTPS & Land",
            description = "Online mutation (Dakhil Kharij), Lagan rasid payment, Jamabandi search and LPC application.",
            officialUrl = "https://biharbhumi.bihar.gov.in/",
            standardFee = 120.0,
            requiredDocuments = listOf("Kewala / Registry Deed Copy", "Khatiyan", "Previous Lagan Rasid", "Aadhaar of Buyer"),
            tips = "Verify Circle Officer (Anchal) and Mauza name carefully from the land deed."
        ),

        // Govt Jobs & Exams
        CyberServiceItem(
            id = "job_ssc",
            name = "SSC Online Application (CGL, CHSL, GD, MTS)",
            category = "Govt Jobs & Exams",
            description = "Staff Selection Commission one-time registration (OTR), online form submission and fee payment.",
            officialUrl = "https://ssc.gov.in/",
            standardFee = 150.0,
            requiredDocuments = listOf("SSC OTR Login", "Live Webcam/Selfie Photo", "Scanned Signature (10-20KB)", "Matriculation Marksheet"),
            tips = "Live webcam capture requires plain light background without spectacles or cap."
        ),
        CyberServiceItem(
            id = "job_bpsc_bssc",
            name = "BPSC & BSSC Recruitment Forms",
            category = "Govt Jobs & Exams",
            description = "Bihar Public Service Commission (Teacher TRE, CCE) and BSSC Inter/Graduate Level Exam forms.",
            officialUrl = "https://bpsc.bih.nic.in/",
            standardFee = 150.0,
            requiredDocuments = listOf("Graduation/B.Ed Marksheet", "Aadhaar Card", "Bihar Domicile Certificate", "Caste/NCL/EWS"),
            tips = "Hindi and English signatures must be scanned separately with proper dimensions."
        ),
        CyberServiceItem(
            id = "job_railway_rrb",
            name = "Railway RRB (ALP, Technician, NTPC, Group D)",
            category = "Govt Jobs & Exams",
            description = "Railway Recruitment Board online recruitment application and zone selection.",
            officialUrl = "https://www.rrbapply.gov.in/",
            standardFee = 150.0,
            requiredDocuments = listOf("Aadhaar Card", "ITI / Diploma / 10th Certificate", "Passport Photo with White Background", "Bank Passbook for Fee Refund"),
            tips = "Provide candidate's own bank account details because RRB refunds exam fee after CBT 1."
        ),
        CyberServiceItem(
            id = "job_admit_card",
            name = "Admit Card Download & Color Print",
            category = "Govt Jobs & Exams",
            description = "Search roll numbers, download exam call letters and print full admit card with exam instructions.",
            officialUrl = "https://sarkariresult.com/",
            standardFee = 30.0,
            requiredDocuments = listOf("Registration Number", "Password or Date of Birth"),
            tips = "Print in color on bond paper. Instruct student to check reporting time and photo IDs."
        ),

        // Scholarships
        CyberServiceItem(
            id = "scholarship_nsp",
            name = "National Scholarship Portal (NSP)",
            category = "Scholarships",
            description = "Central Government pre-matric, post-matric and merit-cum-means scholarships for minorities, SC/ST, and OBC.",
            officialUrl = "https://scholarships.gov.in/",
            standardFee = 150.0,
            requiredDocuments = listOf("Aadhaar Seeded Bank Account", "Income & Caste Certificate", "College Bonafide", "Fee Receipt", "Mark Sheet"),
            tips = "Bank account must be Aadhaar seeded in NPCI mapping to receive DBT scholarship."
        ),
        CyberServiceItem(
            id = "scholarship_pms_bihar",
            name = "Bihar Post Matric Scholarship (PMS Online)",
            category = "Scholarships",
            description = "Bihar state scholarship portal for SC, ST, BC, EBC students studying in Bihar or outside colleges.",
            officialUrl = "https://pmsonline.bih.nic.in/",
            standardFee = 120.0,
            requiredDocuments = listOf("Current Year Fee Structure", "Bonafide Certificate signed by Principal", "Previous Exam Marksheet", "Niwas/Jati/Aay"),
            tips = "Ensure student's mobile number is linked with Aadhaar for OTP verification."
        ),

        // Admissions
        CyberServiceItem(
            id = "admission_ofss",
            name = "OFSS Bihar 11th & Degree Admission",
            category = "Admissions",
            description = "Online Facilitation System for Students for Inter-College and University 4-Year Graduation admission.",
            officialUrl = "https://www.ofssbihar.org/",
            standardFee = 100.0,
            requiredDocuments = listOf("10th/12th Roll Code & Roll Number", "Passport Photo", "College/Stream Preferences", "Mobile Number"),
            tips = "Choose 10 to 20 colleges in order of cut-off preference."
        ),

        // Passport Seva
        CyberServiceItem(
            id = "passport_apply",
            name = "Fresh Passport & Tatkaal Appointment",
            category = "Passport Seva",
            description = "Online Passport application, fee payment, and appointment booking for PSK Patna/Varanasi.",
            officialUrl = "https://www.passportindia.gov.in/",
            standardFee = 300.0,
            requiredDocuments = listOf("Aadhaar Card", "Matriculation Certificate for Non-ECR", "PAN Card", "Bank Passbook with photo"),
            tips = "Names and spellings across Aadhaar, PAN and Matric Certificate must match 100%."
        ),

        // CSC & Utilities
        CyberServiceItem(
            id = "csc_pmkisan",
            name = "PM-Kisan e-KYC & New Farmer Registration",
            category = "CSC & Utilities",
            description = "Pradhan Mantri Kisan Samman Nidhi Samman annual Rs. 6000 assistance scheme KYC and beneficiary status.",
            officialUrl = "https://pmkisan.gov.in/",
            standardFee = 50.0,
            requiredDocuments = listOf("Aadhaar Card", "Land Registry / Jamabandi", "Bank Account", "Mobile for OTP"),
            tips = "Biometric e-KYC or OTP KYC ensures uninterrupted instalment payments."
        ),
        CyberServiceItem(
            id = "csc_ayushman",
            name = "Ayushman Bharat Golden Card (PMJAY)",
            category = "CSC & Utilities",
            description = "5 Lakh rupees free health insurance scheme card creation and plastic card download.",
            officialUrl = "https://beneficiary.nha.gov.in/",
            standardFee = 60.0,
            requiredDocuments = listOf("Ration Card Number", "Aadhaar Card of all family members"),
            tips = "E-KYC uses live photo capture and matches with Aadhaar face or biometric."
        ),
        CyberServiceItem(
            id = "csc_electricity_bill",
            name = "Electricity Bill Payment (SBPDCL Bihar)",
            category = "CSC & Utilities",
            description = "South Bihar Power Distribution Company Ltd instant consumer electricity bill check & payment receipt.",
            officialUrl = "https://www.sbpdcl.co.in/",
            standardFee = 20.0,
            requiredDocuments = listOf("Consumer CA Number"),
            tips = "Print the digital payment receipt for customer records."
        )
    )
}
