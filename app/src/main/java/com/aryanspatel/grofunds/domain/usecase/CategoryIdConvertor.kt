package com.aryanspatel.grofunds.domain.usecase

import androidx.compose.ui.graphics.Color
import com.aryanspatel.grofunds.R
import com.aryanspatel.grofunds.domain.model.CategoryResolution
import com.aryanspatel.grofunds.domain.model.CategorySeed

val CURRENCY_LIST_ENUM = listOf(
    "CAD","USD","EUR","GBP","INR","AUD"
)

val BuiltInIncomeTypes = listOf(
    CategorySeed("salary", "Salary", iconRes = R.drawable.salary, color = Color(0xFF43A047)),
    CategorySeed("overtime", "Overtime", iconRes = R.drawable.overtime, color = Color(0xFFFB8C00)),
    CategorySeed("bonus", "Bonus", iconRes = R.drawable.bonus, color = Color(0xFFFDD835)),
    CategorySeed("commission", "Commission", iconRes = R.drawable.commission, color = Color(0xFFAED581)),
    CategorySeed("tips", "Tips", iconRes = R.drawable.tips, color = Color(0xFFE53935)),
    CategorySeed("stock-compensation", "Stock Compensation", iconRes = R.drawable.stock, color = Color(0xFF00ACC1)),
    CategorySeed("freeLance-contract", "Freelance/Contract", iconRes = R.drawable.contract, color = Color(0xFF1E88E5)),
    CategorySeed("business", "Business", iconRes = R.drawable.business, color =Color(0xFF5E35B1)),
    CategorySeed("rental", "Rental", iconRes = R.drawable.rent, color = Color(0xFF8E24AA)),
    CategorySeed("interest", "Interest", iconRes = R.drawable.interest, color = Color(0xFFD81B60)),
    CategorySeed("dividend", "Dividend", iconRes = R.drawable.dividends, color = Color(0xFF6D4C41)),
    CategorySeed("capital-gains", "Capital Gains", iconRes = R.drawable.capital_gain, color = Color(0xFF00897B)),
    CategorySeed("royalties", "Royalties", iconRes = R.drawable.royalties, color = Color(0xFF7CB342)),
    CategorySeed("government-benefits", "Government Benefits", iconRes = R.drawable.government_benefit, color = Color(0xFFFF7043)),
    CategorySeed("pension-annuity", "Pension/Annuity", iconRes = R.drawable.pension, color = Color(0xFF26C6DA)),
    CategorySeed("alimony-child-support", "Alimony/Child Support", iconRes = R.drawable.child_upport, color = Color(0xFF8D6E63)),
    CategorySeed("gifts", "Gifts", iconRes = R.drawable.gift, color = Color(0xFF7E57C2)),
    CategorySeed("prize-award", "Prize/Award", iconRes = R.drawable.prize_award, color = Color(0xFFEC407A)),
    CategorySeed("cashback-rebate", "Cashback/Rebate", iconRes = R.drawable.cash_back, color = Color(0xFF42A5F5)),
    CategorySeed("other", "Other", iconRes = R.drawable.other, color = Color(0xFF9CCC65))
)

val BuiltInSavingTypes = listOf(
    CategorySeed("emergency-fund", "Emergency Fund"),
    CategorySeed("down-payment", "Down Payment"),
    CategorySeed("home-purchase", "Home Purchase"),
    CategorySeed("home-renovation", "Home Renovation"),
    CategorySeed("car-purchase", "Car Purchase"),
    CategorySeed("education", "Education"),
    CategorySeed("retirement-investing", "Retirement/Investing"),
    CategorySeed("travel", "Travel"),
    CategorySeed("wedding", "Wedding"),
    CategorySeed("gadget-tech", "Gadget/Tech"),
    CategorySeed("furniture-appliances", "Furniture/Appliances"),
    CategorySeed("medical-health", "Medical/Health"),
    CategorySeed("baby-child-fund", "Baby/Child Fund"),
    CategorySeed("pet-fund", "Pet Fund"),
    CategorySeed("moving-relocation", "Moving/Relocation"),
    CategorySeed("taxes", "Taxes"),
    CategorySeed("charity-giving", "Charity/Giving"),
    CategorySeed("debt-payoff", "Debt Payoff"),
    CategorySeed("business", "Business"),
    CategorySeed("other", "Other"),
)

val BuiltInExpenseCategories = listOf(
    CategorySeed("food-drink", "Food & Drink", iconRes = R.drawable.food_and_drinks, color = Color(0xFF1ABC9C)),
    CategorySeed("shopping", "Shopping", iconRes = R.drawable.shopping, color = Color(0xFF8BC34A)),
    CategorySeed("transport", "Transport", iconRes = R.drawable.transportation, color = Color(0xFF3498DB)),
    CategorySeed("bills", "Bills", iconRes = R.drawable.bills, color = Color(0xFFE91E63)),
    CategorySeed("debt-loans", "Loans & Mortgages", iconRes = R.drawable.debt, color = Color(0xFFF39C12)),
    CategorySeed("entertainment", "Entertainment", iconRes = R.drawable.entertainment, color = Color(0xFF9B59B6)),
    CategorySeed("subscriptions-services", "Subscriptions & Services", iconRes = R.drawable.subscription, color = Color(0xFFCDDC39)),
    CategorySeed("health-fitness", "Health & Fitness", iconRes = R.drawable.health, color = Color(0xFF00BCD4)),
    CategorySeed("travel", "Travel", iconRes = R.drawable.travel, color = Color(0xFFFF5722)),
    CategorySeed("personal-care", "Personal Care", iconRes = R.drawable.personal_care, color = Color(0xFF795548)),
    CategorySeed("childcare-family", "Childcare & Family", iconRes = R.drawable.child_care, color = Color(0xFF607D8B)),
    CategorySeed("pets", "Pets", iconRes = R.drawable.pets, color = Color(0xFFF44336)),
    CategorySeed("insurance", "Insurance", iconRes = R.drawable.insaurance, color = Color(0xFF2ECC71)),
    CategorySeed("education", "Education", iconRes = R.drawable.education, color = Color(0xFF3F51B5)),
    CategorySeed("gifts-donations", "Gifts & Donations", iconRes = R.drawable.gifts, color = Color(0xFF7E57C2)),
    CategorySeed("taxes", "Taxes", iconRes = R.drawable.tax, color = Color(0xFFFFC107)),
    CategorySeed("other", "Other", iconRes = R.drawable.other, color = Color(0xFF960040))
)

val BuiltInExpenseSubcategories = listOf(
    // Food & Drink
    CategorySeed("food-drink.groceries", "Groceries", "food-drink"),
    CategorySeed("food-drink.restaurants", "Restaurants", "food-drink"),
    CategorySeed("food-drink.coffee-tea", "Coffee & Tea", "food-drink"),
    CategorySeed("food-drink.snacks", "Snacks", "food-drink"),
    CategorySeed("food-drink.alcohol", "Alcohol", "food-drink"),
    CategorySeed("food-drink.other", "Other", "food-drink"),

    // Shopping
    CategorySeed("shopping.clothing", "Clothing", "shopping"),
    CategorySeed("shopping.electronics", "Electronics", "shopping"),
    CategorySeed("shopping.household", "Household", "shopping"),
    CategorySeed("shopping.furniture", "Furniture", "shopping"),
    CategorySeed("shopping.other", "Other", "shopping"),

    // Transport
    CategorySeed("transport.fuel", "Fuel", "transport"),
    CategorySeed("transport.transit", "Transit", "transport"),
    CategorySeed("transport.taxi", "Taxi", "transport"),
    CategorySeed("transport.parking", "Parking", "transport"),
    CategorySeed("transport.tolls", "Tolls", "transport"),
    CategorySeed("transport.maintenance", "Maintenance", "transport"),
    CategorySeed("transport.other", "Other", "transport"),

    // Bills
    CategorySeed("bills.phone", "Phone", "bills"),
    CategorySeed("bills.internet", "Internet", "bills"),
    CategorySeed("bills.utilities", "Utilities", "bills"),
    CategorySeed("bills.rent", "Rent", "bills"),
    CategorySeed("bills.other", "Other", "bills"),

    // Debt & Loans
    CategorySeed("debt-loans.house-mortgage", "House Mortgage", "debt-loans"),
    CategorySeed("debt-loans.cc-interest", "Credit Card Interest", "debt-loans"),
    CategorySeed("debt-loans.cc-principal", "Credit Card Principal", "debt-loans"),
    CategorySeed("debt-loans.student-loan", "Student Loan", "debt-loans"),
    CategorySeed("debt-loans.auto-loan", "Auto Loan", "debt-loans"),
    CategorySeed("debt-loans.personal-loan", "Personal Loan", "debt-loans"),
    CategorySeed("debt-loans.other", "Other", "debt-loans"),

    // Entertainment
    CategorySeed("entertainment.streaming", "Streaming", "entertainment"),
    CategorySeed("entertainment.movies", "Movies", "entertainment"),
    CategorySeed("entertainment.games", "Games", "entertainment"),
    CategorySeed("entertainment.events", "Events", "entertainment"),
    CategorySeed("entertainment.hobbies", "Hobbies", "entertainment"),
    CategorySeed("entertainment.other", "Other", "entertainment"),

    // Subscriptions & Services
    CategorySeed(
        "subscriptions-services.apps-software",
        "Apps & Software",
        "subscriptions-services"
    ),
    CategorySeed("subscriptions-services.cloud-storage", "Cloud Storage", "subscriptions-services"),
    CategorySeed(
        "subscriptions-services.news-magazines",
        "News & Magazines",
        "subscriptions-services"
    ),
    CategorySeed("subscriptions-services.productivity", "Productivity", "subscriptions-services"),
    CategorySeed("subscriptions-services.other", "Other", "subscriptions-services"),

    // Health & Fitness
    CategorySeed("health-fitness.pharmacy", "Pharmacy", "health-fitness"),
    CategorySeed("health-fitness.doctor", "Doctor", "health-fitness"),
    CategorySeed("health-fitness.dental", "Dental", "health-fitness"),
    CategorySeed("health-fitness.gym", "Gym", "health-fitness"),
    CategorySeed("health-fitness.other", "Other", "health-fitness"),

    // Travel
    CategorySeed("travel.flights", "Flights", "travel"),
    CategorySeed("travel.hotel", "Hotel", "travel"),
    CategorySeed("travel.car-rental", "Car Rental", "travel"),
    CategorySeed("travel.baggage", "Baggage", "travel"),
    CategorySeed("travel.other", "Other", "travel"),

    // Personal Care
    CategorySeed("personal-care.hair-salon", "Hair/Salon", "personal-care"),
    CategorySeed("personal-care.toiletries", "Toiletries", "personal-care"),
    CategorySeed("personal-care.laundry", "Laundry", "personal-care"),
    CategorySeed("personal-care.skin-care", "Skin Care", "personal-care"),
    CategorySeed("personal-care.body-care", "Body care", "personal-care"),
    CategorySeed("personal-care.other", "Other", "personal-care"),

    // Childcare & Family
    CategorySeed("childcare-family.daycare", "Daycare", "childcare-family"),
    CategorySeed("childcare-family.school-fees", "School Fees", "childcare-family"),
    CategorySeed("childcare-family.baby-supplies", "Baby Supplies", "childcare-family"),
    CategorySeed("childcare-family.allowance", "Allowance", "childcare-family"),
    CategorySeed("childcare-family.other", "Other", "childcare-family"),

    // Pets
    CategorySeed("pets.food", "Food", "pets"),
    CategorySeed("pets.vet", "Vet", "pets"),
    CategorySeed("pets.grooming", "Grooming", "pets"),
    CategorySeed("pets.other", "Other", "pets"),

    // Insurance
    CategorySeed("insurance.auto", "Auto", "insurance"),
    CategorySeed("insurance.home", "Home", "insurance"),
    CategorySeed("insurance.health", "Health", "insurance"),
    CategorySeed("insurance.life", "Life", "insurance"),
    CategorySeed("insurance.other", "Other", "insurance"),

    // Education
    CategorySeed("education.tuition", "Tuition", "education"),
    CategorySeed("education.books", "Books", "education"),
    CategorySeed("education.courses", "Courses", "education"),
    CategorySeed("education.library", "Library", "education"),
    CategorySeed("education.other", "Other", "education"),

    // Gifts & Donations
    CategorySeed("gifts-donations.gifts", "Gifts", "gifts-donations"),
    CategorySeed("gifts-donations.charity", "Charity", "gifts-donations"),
    CategorySeed("gifts-donations.other", "Other", "gifts-donations"),

    // Taxes
    CategorySeed("taxes.income-tax", "Income Tax", "taxes"),
    CategorySeed("taxes.property-tax", "Property Tax", "taxes"),
    CategorySeed("taxes.sales-tax", "Sales Tax", "taxes"),
    CategorySeed("taxes.other", "Other", "taxes"),

    // Other
    CategorySeed("other.other", "Other", "other"),
)


val expenseCategoryById = BuiltInExpenseCategories.associateBy { it.id }
private val expenseCategoryByName = BuiltInExpenseCategories.associateBy { it.name.lowercase() }
private val expenseSubcategoryById = BuiltInExpenseSubcategories.associateBy { it.id }
private val expenseSubcategoryByName = BuiltInExpenseSubcategories.associateBy { it.name.lowercase() }
val incomeTypeById = BuiltInIncomeTypes.associateBy { it.id }
private val incomeTypeByName = BuiltInIncomeTypes.associateBy { it.name.lowercase() }
private val savingTypeById = BuiltInSavingTypes.associateBy { it.id }
private val savingTypeByName = BuiltInSavingTypes.associateBy { it.name.lowercase() }
private const val OTHER_CATEGORY_ID = "other"
private const val OTHER_SUBCATEGORY_ID = "other.other"
private const val OTHER_CAT_SUBCATEGORY_LABEL = "Other"

/** 1) as per category input, return list of subcategory */
fun subcategoriesFor( categoryLabelInput: String): List<String> {
    val category = expenseCategoryByName[categoryLabelInput.lowercase()]
    return if (category != null) {
        BuiltInExpenseSubcategories.filter { it.parentId == category.id }.map { it.name }
    } else {
        emptyList()
    }
}

/** 2) return category and subcategory ids from labels */
fun resolveExpenseCategoryIds(
    categoryLabelInput: String?,
    subcategoryLabelInput: String?
): CategoryResolution {
    val catLabel = categoryLabelInput?.trim().orEmpty()
    val subLabel = subcategoryLabelInput?.trim().orEmpty()

    // If subcategory label is present, use it as the source of truth.
    if (subLabel.isNotEmpty() && subLabel != OTHER_CAT_SUBCATEGORY_LABEL) {
        val sub = expenseSubcategoryByName[subLabel.lowercase()]
        if (sub != null) {
            val parentCat = sub.parentId?.let { expenseCategoryById[it] }
            val parentId = parentCat?.id ?: OTHER_CATEGORY_ID

            // If user-provided category conflicts, we still trust the sub’s parent.
            return CategoryResolution(
                categoryId = parentId,
                subcategoryId = sub.id,
            )
        }
    }

    // No valid sub label; resolve category by label
    if (catLabel.isNotEmpty()) {
        val cat = expenseCategoryByName[catLabel.lowercase()]
        if (cat != null) {
            // try to pick that category's "Other" sub if it exists
            val defaultSubId = "${cat.id}.other"
            val defaultSub = expenseSubcategoryById[defaultSubId]
            return CategoryResolution(
                categoryId = cat.id,
                subcategoryId = defaultSub?.id,     // may be null if not seeded
            )
        }
    }

    // Final fallback → Other
    return CategoryResolution(
        categoryId = OTHER_CATEGORY_ID,
        subcategoryId = OTHER_SUBCATEGORY_ID)
}

/** 3) return category and subcategory Labels from ids */
fun resolveExpenseCategoryLabels(
    categoryIdInput: String?,
    subcategoryIdInput: String?,
): CategoryResolution {
    val categoryId = categoryIdInput?.trim().orEmpty()
    val subcategoryId = subcategoryIdInput?.trim().orEmpty()

    if(subcategoryId.isNotEmpty()){
        val sub = expenseSubcategoryById[subcategoryId]
        if(sub != null){
            val parenCat = sub.parentId?.let { expenseCategoryById[it] }
            val parentLabel = parenCat?.name ?: OTHER_CAT_SUBCATEGORY_LABEL

            return CategoryResolution(parentLabel, sub.name)
        }
    }

    if(categoryId.isNotEmpty()){
        val cat = expenseCategoryById[categoryId]
        if(cat != null){
            val defaultSubId = "${cat.id}.other"
            val defaultSub = expenseSubcategoryById[defaultSubId]
            return CategoryResolution(cat.name, defaultSub?.name)
        }
    }

    return CategoryResolution(OTHER_CAT_SUBCATEGORY_LABEL, OTHER_CAT_SUBCATEGORY_LABEL)
}

/** 4) return income's typeId from label */
fun resolveIncomeTypeId(typeLabelInput: String?) : CategoryResolution {
    val typeLabel = typeLabelInput?.trim().orEmpty()

    if(typeLabel.isNotEmpty()){
        val type = incomeTypeByName[typeLabel.lowercase()]
        if(type != null){
            return CategoryResolution(
                categoryId = type.id)
        }
    }

    return CategoryResolution(categoryId = OTHER_CATEGORY_ID)
}

/** 5) return income's type label from id */
fun resolveIncomeTypeLabel(typeIdInput: String?) : CategoryResolution {
    val typeId = typeIdInput?.trim().orEmpty()

    if(typeId.isNotEmpty()){
        val type = incomeTypeById[typeId]
        if(type != null){
            return CategoryResolution(
                categoryId = type.name)
        }
    }

    return CategoryResolution(categoryId = OTHER_CAT_SUBCATEGORY_LABEL)
}

/** 6) return saving's typeId from label */
fun resolveSavingTypeId(typeLabelInput: String?): CategoryResolution{
    val typeLabel = typeLabelInput?.trim().orEmpty()

    if(typeLabel.isNotEmpty()){
        val type = savingTypeByName[typeLabel.lowercase()]
        if(type != null){
            return CategoryResolution(categoryId = type.id)
        }
    }

    return CategoryResolution(categoryId = OTHER_CATEGORY_ID)
}

/** 7) return saving's type label from id */
fun resolveSavingTypeLabel(typeIdInput: String?): CategoryResolution {
    val typeId = typeIdInput?.trim().orEmpty()

    if(typeId.isNotEmpty()){
        val type = savingTypeById[typeId]
        if(type != null){
            return CategoryResolution(categoryId = type.name)
        }
    }

    return CategoryResolution(categoryId = OTHER_CAT_SUBCATEGORY_LABEL)
}


