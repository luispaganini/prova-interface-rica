package br.edu.utfpr.trabalhofinal.ui.conta.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.edu.utfpr.trabalhofinal.R
import br.edu.utfpr.trabalhofinal.data.TipoContaEnum
import br.edu.utfpr.trabalhofinal.ui.theme.TrabalhoFinalTheme
import br.edu.utfpr.trabalhofinal.ui.utils.composables.Carregando
import br.edu.utfpr.trabalhofinal.ui.utils.composables.ErroAoCarregar
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun FormularioContaScreen(
    modifier: Modifier = Modifier,
    onVoltarPressed: () -> Unit,
    viewModel: FormularioContaViewModel = viewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    LaunchedEffect(viewModel.state.contaPersistidaOuRemovida) {
        if (viewModel.state.contaPersistidaOuRemovida) {
            onVoltarPressed()
        }
    }
    val context = LocalContext.current
    LaunchedEffect(snackbarHostState, viewModel.state.codigoMensagem) {
        viewModel.state.codigoMensagem
            .takeIf { it > 0 }
            ?.let {
                snackbarHostState.showSnackbar(context.getString(it))
                viewModel.onMensagemExibida()
            }
    }

    if (viewModel.state.mostrarDialogConfirmacao) {
        ConfirmationDialog(
            title = stringResource(R.string.atencao),
            text = stringResource(R.string.mensagem_confirmacao_remover_contato),
            onDismiss = viewModel::ocultarDialogConfirmacao,
            onConfirm = viewModel::removerConta
        )
    }

    val contentModifier: Modifier = modifier.fillMaxSize()
    if (viewModel.state.carregando) {
        Carregando(modifier = contentModifier)
    } else if (viewModel.state.erroAoCarregar) {
        ErroAoCarregar(
            modifier = contentModifier,
            onTryAgainPressed = viewModel::carregarConta
        )
    } else {
        Scaffold(
            modifier = contentModifier,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                AppBar(
                    contaNova = viewModel.state.contaNova,
                    processando = viewModel.state.salvando || viewModel.state.excluindo,
                    onVoltarPressed = onVoltarPressed,
                    onSalvarPressed = viewModel::salvarConta,
                    onExcluirPressed = viewModel::mostrarDialogConfirmacao
                )
            }
        ) { paddingValues ->
            FormContent(
                modifier = Modifier.padding(paddingValues),
                processando = viewModel.state.salvando || viewModel.state.excluindo,
                descricao = viewModel.state.descricao,
                data = viewModel.state.data,
                valor = viewModel.state.valor,
                paga = viewModel.state.paga,
                tipo = viewModel.state.tipo,
                onDescricaoAlterada = viewModel::onDescricaoAlterada,
                onDataAlterada = viewModel::onDataAlterada,
                onValorAlterado = viewModel::onValorAlterado,
                onStatusPagamentoAlterado = viewModel::onStatusPagamentoAlterado,
                onTipoAlterado = viewModel::onTipoAlterado
            )
        }
    }
}

@Composable
fun ConfirmationDialog(
    modifier: Modifier = Modifier,
    title: String? = null,
    text: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    dismissButtonText: String? = null,
    confirmButtonText: String? = null
) {
    AlertDialog(
        modifier = modifier,
        title = title?.let {
            { Text(it) }
        },
        text = { Text(text) },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(confirmButtonText ?: stringResource(R.string.confirmar))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(dismissButtonText ?: stringResource(R.string.cancelar))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar(
    modifier: Modifier = Modifier,
    contaNova: Boolean,
    processando: Boolean,
    onVoltarPressed: () -> Unit,
    onSalvarPressed: () -> Unit,
    onExcluirPressed: () -> Unit
) {
    TopAppBar(
        modifier = modifier.fillMaxWidth(),
        title = {
            Text(
                if (contaNova) {
                    stringResource(R.string.nova_conta)
                } else {
                    stringResource(R.string.editar_conta)
                }
            )
        },
        navigationIcon = {
            IconButton(onClick = onVoltarPressed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.voltar)
                )
            }
        },
        actions = {
            if (processando) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(60.dp)
                        .padding(all = 16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                if (!contaNova) {
                    IconButton(onClick = onExcluirPressed) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.excluir)
                        )
                    }
                }
                IconButton(onClick = onSalvarPressed) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = stringResource(R.string.salvar)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
            navigationIconContentColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun AppBarPreview() {
    TrabalhoFinalTheme {
        AppBar(
            contaNova = true,
            processando = false,
            onVoltarPressed = {},
            onSalvarPressed = {},
            onExcluirPressed = {}
        )
    }
}

@Composable
private fun FormContent(
    modifier: Modifier = Modifier,
    processando: Boolean,
    descricao: CampoFormulario,
    data: CampoFormulario,
    valor: CampoFormulario,
    paga: CampoFormulario,
    tipo: CampoFormulario,
    onDescricaoAlterada: (String) -> Unit,
    onDataAlterada: (String) -> Unit,
    onValorAlterado: (String) -> Unit,
    onStatusPagamentoAlterado: (String) -> Unit,
    onTipoAlterado: (String) -> Unit
) {
    val formTextFieldModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    Column(
        modifier = modifier
            .padding(all = 16.dp)
            .imePadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Notes,
                contentDescription = stringResource(R.string.descricao),
                tint = MaterialTheme.colorScheme.outline
            )
            FormTextField(
                modifier = formTextFieldModifier,
                titulo = stringResource(R.string.descricao),
                campoFormulario = descricao,
                onValorAlterado = onDescricaoAlterada,
                keyboardCapitalization = KeyboardCapitalization.Words,
                enabled = !processando
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.AttachMoney,
                contentDescription = stringResource(R.string.valor),
                tint = MaterialTheme.colorScheme.outline
            )
            FormTextField(
                modifier = formTextFieldModifier,
                titulo = stringResource(R.string.valor),
                campoFormulario = valor,
                onValorAlterado = onValorAlterado,
                enabled = !processando,
                keyboardType = KeyboardType.Number,
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = stringResource(R.string.data),
                tint = MaterialTheme.colorScheme.background
            )
            FormDatePicker(
                modifier = formTextFieldModifier,
                titulo = stringResource(R.string.data),
                campoFormulario = data,
                onValueChanged = onDataAlterada,
                enabled = !processando,
                visualTransformation = VisualTransformation.None
            )
        }
        val checkOptionsModifier = Modifier.padding(vertical = 8.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(R.string.paga),
                tint = MaterialTheme.colorScheme.background
            )
            FormCheckbox(
                modifier = checkOptionsModifier,
                checked = paga.valor.toBoolean(),
                onCheckChanged = { onStatusPagamentoAlterado(it.toString()) },
                enabled = !processando,
                label = stringResource(R.string.paga)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Filled.AccountBalance,
                contentDescription = stringResource(R.string.paga),
                tint = MaterialTheme.colorScheme.background
            )
            FormRadioButton(
                modifier = checkOptionsModifier,
                value = TipoContaEnum.DESPESA,
                groupValue = TipoContaEnum.valueOf(tipo.valor),
                onValueChanged = { onTipoAlterado(it.name) },
                enabled = !processando,
                label = stringResource(R.string.despesa)
            )
            FormRadioButton(
                modifier = checkOptionsModifier,
                value = TipoContaEnum.RECEITA,
                groupValue = TipoContaEnum.valueOf(tipo.valor),
                onValueChanged = { onTipoAlterado(it.name) },
                enabled = !processando,
                label = stringResource(R.string.receita)
            )
        }
    }
}

@Composable
private fun FormCheckbox(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckChanged: (Boolean) -> Unit,
    enabled: Boolean = true,
    label: String
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onCheckChanged(!checked) },
            enabled = enabled
        )
        Text(label)
    }
}

@Composable
fun FormRadioButton(
    modifier: Modifier = Modifier,
    value: TipoContaEnum,
    groupValue: TipoContaEnum,
    onValueChanged: (TipoContaEnum) -> Unit,
    enabled: Boolean = true,
    label: String
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = value == groupValue,
            onClick = { onValueChanged(value) },
            enabled = enabled
        )
        Text(label)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormDatePicker(
    modifier: Modifier = Modifier,
    titulo: String,
    campoFormulario: CampoFormulario,
    onValueChanged: (String) -> Unit,
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = LocalDate.parse(campoFormulario.valor)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    )

    Column(
        modifier = modifier,

    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = LocalDate.parse(
                campoFormulario.valor.ifEmpty { LocalDate.now().toString() }
            )
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            onValueChange = { },
            label = { Text(titulo) },
            maxLines = 1,
            enabled = enabled,
            isError = campoFormulario.contemErro,
            visualTransformation = visualTransformation,
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = !showDatePicker }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = stringResource(R.string.selecione_a_data)
                    )
                }
            }
        )
        if (campoFormulario.contemErro) {
            Text(
                text = stringResource(campoFormulario.codigoMensagemErro),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = !showDatePicker },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = Instant
                            .ofEpochMilli(it)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        onValueChanged(date.toString())
                    }
                    showDatePicker = !showDatePicker
                }) {
                    Text(stringResource(R.string.ok))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun FormTextField(
    modifier: Modifier = Modifier,
    titulo: String,
    campoFormulario: CampoFormulario,
    onValorAlterado: (String) -> Unit,
    enabled: Boolean = true,
    keyboardCapitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    keyboardImeAction: ImeAction = ImeAction.Next,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(
        modifier = modifier,
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = campoFormulario.valor,
            onValueChange = onValorAlterado,
            label = { Text(titulo) },
            maxLines = 1,
            enabled = enabled,
            isError = campoFormulario.contemErro,
            keyboardOptions = KeyboardOptions(
                capitalization = keyboardCapitalization,
                imeAction = keyboardImeAction,
                keyboardType = keyboardType
            ),
            visualTransformation = visualTransformation
        )
        if (campoFormulario.contemErro) {
            Text(
                text = stringResource(campoFormulario.codigoMensagemErro),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FormContentPreview() {
    TrabalhoFinalTheme {
        FormContent(
            processando = false,
            descricao = CampoFormulario(),
            data = CampoFormulario(),
            valor = CampoFormulario(),
            paga = CampoFormulario(),
            tipo = CampoFormulario(),
            onDescricaoAlterada = {},
            onDataAlterada = {},
            onValorAlterado = {},
            onStatusPagamentoAlterado = {},
            onTipoAlterado = {}
        )
    }
}