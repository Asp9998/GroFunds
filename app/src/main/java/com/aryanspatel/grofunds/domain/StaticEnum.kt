package com.aryanspatel.grofunds.domain

val EXPENSE_CATEGORY_ENUM = listOf(
    "Food & Drink", "Shopping", "Transport", "Bills", "Debt & Loans", "Entertainment",
    "Subscriptions & Services", "Health & Fitness", "Travel", "Personal Care",
    "Childcare & Family", "Pets", "Insurance", "Education", "Gifts & Donations", "Taxes", "Other"
)

private val SUBCATEGORY_MAP = mapOf(
    "Food & Drink" to listOf("Groceries", "Restaurants", "Coffee & Tea", "Snacks", "Alcohol", "Other"),
    "Shopping" to listOf("Clothing", "Electronics", "Household", "Furniture", "Other"),
    "Transport" to listOf("Fuel", "Transit", "Taxi", "Parking", "Tolls", "Maintenance", "Other"),
    "Bills" to listOf("Phone", "Internet", "Utilities", "Rent", "Other"),
    "Debt & Loans" to listOf("Credit Card Interest", "Credit Card Principal", "Student Loan", "Auto Loan", "Personal Loan", "Other"),
    "Entertainment" to listOf("Streaming","Movies", "Games", "Events", "Hobbies", "Other"),
    "Subscriptions & Services" to listOf("Apps & Software", "Cloud Storage", "News & Magazines", "Productivity", "Other"),
    "Health & Fitness" to listOf("Pharmacy","Doctor", "Dental", "Gym", "Other"),
    "Travel" to listOf("Flights", "Hotel","Car Rental", "Baggage", "Other"),
    "Personal Care" to listOf("Hair/Salon", "Toiletries", "Laundry", "Skin Care", "Body care", "Other"),
    "Childcare & Family" to listOf("Daycare", "School Fees", "Baby Supplies", "Allowance", "Other"),
    "Pets" to listOf("Food", "Vet", "Grooming", "Other"),
    "Insurance" to listOf("Auto", "Home", "Health", "Life", "Other"),
    "Education" to listOf("Tuition", "Books", "Courses", "Library", "Other"),
    "Gifts & Donations" to listOf("Gifts", "Charity", "Other"),
    "Taxes" to listOf("Income Tax", "Property Tax", "Sales Tax", "Other"),
    "Other" to listOf("Other")
)

val INCOME_TYPE_ENUM = listOf(
    "Salary",
    "Overtime",
    "Bonus",
    "Commission",
    "Tips",
    "Stock Compensation",
    "Freelance/Contract",
    "Business",
    "Rental",
    "Interest",
    "Dividend",
    "Capital Gains",
    "Royalties",
    "Pension/Annuity",
    "Government Benefits",
    "Alimony/Child Support",
    "Gifts",
    "Prize/Award",
    "Cashback/Rebate",
    "Other"
)

val GOAL_TYPE_ENUM = listOf(
    "Emergency Fund",
    "Down Payment",
    "Home Purchase",
    "Home Renovation",
    "Car Purchase",
    "Education",
    "Retirement/Investing",
    "Travel",
    "Wedding",
    "Gadget/Tech",
    "Furniture/Appliances",
    "Medical/Health",
    "Baby/Child Fund",
    "Pet Fund",
    "Moving/Relocation",
    "Taxes",
    "Charity/Giving",
    "Debt Payoff",
    "Business",
    "Other"
)

fun subcategoriesFor(category: String): List<String> = SUBCATEGORY_MAP[category] ?: listOf("")

val CURRENCY_LIST_ENUM = listOf(
    "CAD","USD","EUR","GBP","INR","AUD"
)