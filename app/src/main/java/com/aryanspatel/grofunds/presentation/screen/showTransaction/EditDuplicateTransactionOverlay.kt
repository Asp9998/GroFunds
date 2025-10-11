package com.aryanspatel.grofunds.presentation.screen.showTransaction

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aryanspatel.grofunds.domain.model.EntryKind
import com.aryanspatel.grofunds.presentation.common.model.AddEntryUiState
import com.aryanspatel.grofunds.presentation.components.HorizontalSlidingOverlay
import com.aryanspatel.grofunds.presentation.screen.addEntry.BottomActonButton
import com.aryanspatel.grofunds.presentation.screen.addEntry.EditableDetailsSection
import com.aryanspatel.grofunds.presentation.viewmodel.ShowTransactionViewModel

@Composable
fun EditDuplicateTransaction(
    transaction: AddEntryUiState,
    viewModel: ShowTransactionViewModel = hiltViewModel(),
    screenTitle: String,
    transactionKind: EntryKind,
    createDuplicate: Boolean,
    onDismiss: () -> Unit,
){

    HorizontalSlidingOverlay(
        modifier = Modifier.padding(horizontal = 16.dp),
        title = screenTitle,
        onDismiss = onDismiss
    ) {

        Column (modifier = Modifier.padding(vertical = 12.dp)
            .verticalScroll(rememberScrollState())){
            EditableDetailsSection(
                state = transaction,
                selectedOption = transactionKind,
                onAmountValueSChanged = {viewModel.onAmountUpdate(it)},
                onCurrencyValueChanged = {viewModel.onCurrencyUpdate(it)},
                onCategoryOrTypeValueChanged = {viewModel.onCategoryOrTypeUpdate(it)},
                onExpenseSubcategoryValueChanged = {viewModel.onSubCategoryUpdate(it)},
                onExpenseMerchantValueChanged = { viewModel.onMerchantUpdate(it)},
                onNoteValueChange = {viewModel.onNoteUpdate(it)},
                onWhenTextValueChange = {viewModel.onDateUpdate(it)},
                onGoalTitleValueChange = {},
                onGoalDueDateValueChange = {},
                onGoalStartAmountValueChanged = {}
            )

            Spacer(modifier = Modifier.height(10.dp))

            BottomActonButton(
                isParsed = true ,
                enabled = true,
                onResetClick = {onDismiss()} ,
                onSaveButtonClick = {
                    viewModel.onSaveTransaction(isCreateDuplicate = createDuplicate)
                    onDismiss()
                }
            )
        }
    }

}